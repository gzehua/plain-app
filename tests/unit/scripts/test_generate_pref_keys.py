import importlib.util
import unittest
from pathlib import Path


spec = importlib.util.spec_from_file_location(
    "generate_pref_keys", Path(__file__).resolve().parents[3] / "scripts/generate-pref-keys.py"
)
generator = importlib.util.module_from_spec(spec)
spec.loader.exec_module(generator)


class GeneratePrefKeysTest(unittest.TestCase):
    def test_storage_names_and_snapshot_aliases_come_from_rust(self):
        output = generator.render(
            '    INTERVAL = "interval_seconds",\n    SORT = "sort_key",\n',
            'pub fn set_entry(\nkeys::INTERVAL => "intervalSec",\nkeys::SORT => "packageSort",',
        )
        self.assertIn('const val INTERVAL = "interval_seconds"', output)
        self.assertIn('const val SORT = "sort_key"', output)
        self.assertIn('PrefKeys.INTERVAL -> "intervalSec"', output)
        self.assertIn('PrefKeys.SORT -> "packageSort"', output)

    def test_missing_or_duplicate_keys_fail_generation(self):
        for keys in ['', '    A = "same",\n    B = "same",\n', '    A = "one",\n    A = "two",\n']:
            with self.subTest(keys=keys), self.assertRaises(ValueError):
                generator.render(keys, 'pub fn set_entry(\nkeys::A => "a",')

    def test_unknown_storage_keys_in_snapshot_mapping_fail_generation(self):
        with self.assertRaises(ValueError):
            generator.render('    A = "one",\n', 'pub fn set_entry(\nkeys::B => "b",')


if __name__ == "__main__":
    unittest.main()
