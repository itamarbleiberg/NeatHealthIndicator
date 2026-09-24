#!/usr/bin/env python3
"""Cross-checks every translation key the Java sources ask for against en_us.json.

A missing key shows up in game as a raw identifier rather than an error, so nothing else would
catch it. Run from the project root, or let CI do it:

    python3 tools/check_lang.py
"""

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LANG = ROOT / "src/main/resources/assets/nametag_health/lang/en_us.json"
SRC = ROOT / "src/main/java"

# Screen helpers take the option key as their third argument: bool(category, entries, "enabled", ...)
HELPERS = r"bool|slider|string|colour|stringList|enumOption"
HELPER_CALL = re.compile(rf"\b(?:{HELPERS})\(\s*category\s*,\s*entries\s*,\s*\"([^\"]+)\"")
CATEGORY_CALL = re.compile(r"\bcategory\(\s*builder\s*,\s*\"([^\"]+)\"\s*\)")
# Any literal that is already a fully-qualified key, including the unit and keybind constants.
LITERAL_KEY = re.compile(r"\"((?:key\.)?nametag_health\.[a-z0-9_.]+)\"")
# The keybind category label is derived from an Identifier rather than written as a literal.
CATEGORY_ID = re.compile(r"Identifier\.of\(\s*\"nametag_health\"\s*,\s*\"([a-z0-9_]+)\"\s*\)")
ENUM_PREFIX = re.compile(r"return\s+\"([^\"]+)\"\s*\+\s*name\(\)")
ENUM_CONSTANT = re.compile(r"^\s{4}([A-Z][A-Z0-9_]*)\s*(?:\(|,|;)", re.MULTILINE)


def collect_required() -> set[str]:
    required: set[str] = set()

    for java in SRC.rglob("*.java"):
        text = java.read_text(encoding="utf-8")

        for key in LITERAL_KEY.findall(text):
            # Concatenated prefixes such as "nametag_health.option." are not keys themselves.
            if not key.endswith("."):
                required.add(key)

        for key in HELPER_CALL.findall(text):
            required.add(f"nametag_health.option.{key}")
            required.add(f"nametag_health.option.{key}.tooltip")

        for key in CATEGORY_CALL.findall(text):
            required.add(f"nametag_health.category.{key}")

        for path in CATEGORY_ID.findall(text):
            required.add(f"key.categories.nametag_health.{path}")

        if "implements OptionLabel" in text:
            prefix_match = ENUM_PREFIX.search(text)
            if prefix_match:
                prefix = prefix_match.group(1)
                body = text.split("{", 1)[1]
                for constant in ENUM_CONSTANT.findall(body):
                    required.add(f"{prefix}{constant.lower()}")

    return required


def main() -> int:
    translations = json.loads(LANG.read_text(encoding="utf-8"))
    required = collect_required()

    missing = sorted(required - translations.keys())
    unused = sorted(translations.keys() - required)

    print(f"required by code : {len(required)}")
    print(f"present in en_us : {len(translations)}")
    if missing:
        print(f"MISSING ({len(missing)}):")
        for key in missing:
            print(f"  - {key}")
    if unused:
        print(f"UNUSED ({len(unused)}):")
        for key in unused:
            print(f"  - {key}")
    if not missing and not unused:
        print("all keys accounted for")
    return 1 if missing else 0


if __name__ == "__main__":
    sys.exit(main())
