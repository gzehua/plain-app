import json
import os
from pathlib import Path
import subprocess
import tempfile
import tomllib
import unittest

ROOT = Path(__file__).resolve().parents[2]

INIT = r"""
import groovy.json.JsonOutput

gradle.taskGraph.whenReady { graph ->
    def app = gradle.rootProject.project(':app')
    def nativeTasks = app.tasks.withType(Exec).findAll { it.name.startsWith('buildPlainRustAndroid') }
    def nativePlans = nativeTasks.collectEntries { task ->
        [(task.name): [command: task.commandLine.collect { it.toString() },
            outputs: task.outputs.files.files.collect { it.path }]]
    }
    def selected = graph.allTasks.findAll { nativeTasks.contains(it) }.collect { it.name }.sort()
    println('RUST_BUILD_PLAN=' + JsonOutput.toJson([native: nativePlans, selected: selected]))
}
"""


class AndroidRustBuildTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.plans = {}
        with tempfile.TemporaryDirectory(prefix='plain-build-plan-') as temporary:
            init = Path(temporary) / 'inspect.gradle'
            init.write_text(INIT)
            for variant in ['Debug', 'Release']:
                run = subprocess.run(['./gradlew', '--quiet', '--console=plain', '--no-configuration-cache',
                                      '-I', str(init), f':app:assembleGithub{variant}', '--dry-run'], cwd=ROOT,
                                     env=os.environ, text=True, capture_output=True, timeout=180)
                if run.returncode:
                    raise RuntimeError(run.stderr[-6000:])
                cls.plans[variant] = json.loads(next(line.removeprefix('RUST_BUILD_PLAN=')
                                                    for line in run.stdout.splitlines() if line.startswith('RUST_BUILD_PLAN=')))
        cls.plan = cls.plans['Debug']

    def test_debug_uses_only_the_incremental_native_profile(self):
        self.assertEqual(self.plans['Debug']['selected'], ['buildPlainRustAndroidDebug'])
        command = self.plan['native']['buildPlainRustAndroidDebug']['command']
        self.assertIn('rustc', command)
        self.assertEqual(command[command.index('--profile') + 1], 'dev')
        self.assertEqual(command[command.index('--crate-type') + 1], 'cdylib')
        self.assertNotIn('--release', command)

    def test_debug_does_not_inherit_release_optimization_for_shared_business_code(self):
        profile = tomllib.loads((ROOT / 'plain-rs/Cargo.toml').read_text())['profile']['dev']
        self.assertFalse(profile['lto'])
        self.assertTrue(profile['incremental'])
        self.assertGreater(profile['codegen-units'], 1)
        for package in ['plain-server', 'plain-rs', 'plain-inference']:
            self.assertEqual(profile['package'][package]['opt-level'], 1)
        self.assertEqual(profile['package']['*']['opt-level'], 3)

    def test_release_keeps_the_optimized_native_profile(self):
        self.assertEqual(self.plans['Release']['selected'], ['buildPlainRustAndroidRelease'])
        command = self.plan['native']['buildPlainRustAndroidRelease']['command']
        self.assertEqual(command[command.index('--profile') + 1], 'release')
        self.assertEqual(command[command.index('--crate-type') + 1], 'cdylib')

    def test_profiles_never_package_each_others_library(self):
        debug = self.plan['native']['buildPlainRustAndroidDebug']['outputs']
        release = self.plan['native']['buildPlainRustAndroidRelease']['outputs']
        self.assertTrue(any('/debug/libplain_rust.so' in path for path in debug))
        self.assertTrue(any('/release/libplain_rust.so' in path for path in release))
        self.assertFalse(set(debug) & set(release))


if __name__ == '__main__':
    unittest.main()
