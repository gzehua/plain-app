import argparse
import json
import re
import subprocess
from pathlib import Path


def render(keys_source):
    keys = re.findall(r'^\s+([A-Z][A-Z_0-9]*) = "([a-z_0-9]+)",$', keys_source, re.M)
    keys.extend(re.findall(r'^pub const ([A-Z][A-Z_0-9]*): &str = "([a-z_0-9]+)";$', keys_source, re.M))
    names = dict(keys)
    if not keys or len(names) != len(keys) or len(set(names.values())) != len(keys):
        raise ValueError("Rust preference keys must be nonempty and unique")
    lines = ["package com.ismartcoding.plain.preferences", "", "internal object PrefKeys {"]
    lines.extend(f'    const val {name} = "{key}"' for name, key in keys)
    lines.extend(["}", ""])
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--cargo", default=str(Path.home() / ".cargo/bin/cargo"))
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    metadata = json.loads(subprocess.check_output([
        args.cargo, "metadata", "--locked", "--filter-platform", "aarch64-linux-android",
        "--format-version", "1", "--manifest-path", str(root / "plain-rs/Cargo.toml"),
    ], cwd=root))
    packages = [package for package in metadata["packages"] if package["name"] == "plain-rs"]
    if len(packages) != 1:
        raise ValueError("Expected exactly one resolved plain-rs dependency")
    manifest = packages[0]["manifest_path"]
    prefs = Path(manifest).parent / "src/prefs"
    content = render((prefs / "pref_keys.rs").read_text())
    if not args.output.exists() or args.output.read_text() != content:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(content)


if __name__ == "__main__":
    main()
