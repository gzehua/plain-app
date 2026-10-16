import subprocess
import tempfile
import unittest
from pathlib import Path


class Java21CollectionsTest(unittest.TestCase):
    def test_existing_blocking_deque_methods_do_not_allow_java21_calls_on_other_receivers(self):
        root = Path(__file__).resolve().parents[3]
        with tempfile.TemporaryDirectory(prefix="collection-check-", dir=root / "shared/src/androidMain") as directory:
            source = Path(directory) / "CollectionCheckFixture.kt"
            declarations = """import java.util.concurrent.LinkedBlockingDeque
val queue = LinkedBlockingDeque<String>()
val qualified = java.util.concurrent.LinkedBlockingDeque<String>()
fun fixture(map: LinkedHashMap<String, String>, list: MutableList<String>) {
    queue.putFirst("safe")
    qualified.putLast("safe")
"""
            source.write_text(declarations + """    map.putFirst("key", "value")
    list.removeFirst()
    queue.reversed()
}
""")
            result = self.check(root)
            self.assertNotEqual(result.returncode, 0, result.stdout)
            for call in ['map.putFirst("key", "value")', 'list.removeFirst()', 'queue.reversed()']:
                self.assertIn(call, result.stdout)
            self.assertNotIn('queue.putFirst("safe")', result.stdout)
            self.assertNotIn('qualified.putLast("safe")', result.stdout)
            source.write_text(declarations + "}\n")
            result = self.check(root)
            self.assertEqual(result.returncode, 0, result.stdout)

    @staticmethod
    def check(root):
        return subprocess.run(
            [str(root / "gradlew"), ":shared:checkJava21Collisions", "--console=plain"],
            cwd=root, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        )


if __name__ == "__main__":
    unittest.main()
