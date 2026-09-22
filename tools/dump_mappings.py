#!/usr/bin/env python3
"""Prints the Yarn-mapped members of given Minecraft classes, straight from the mappings jar.

Handy when porting: it answers "what is this field called in this version?" without needing an
IDE or a decompiled source tree.

usage: python3 tools/dump_mappings.py 1.21.11+build.6 EntityRenderState EntityRenderer
"""

import io
import sys
import urllib.request
import zipfile


def load_mappings(version: str) -> str:
    url = (f"https://maven.fabricmc.net/net/fabricmc/yarn/{version}/"
           f"yarn-{version}-mergedv2.jar")
    print(f"fetching {url}", file=sys.stderr)
    with urllib.request.urlopen(url) as response:
        payload = response.read()
    with zipfile.ZipFile(io.BytesIO(payload)) as archive:
        return archive.read("mappings/mappings.tiny").decode("utf-8")


def dump(mappings: str, wanted: set[str]) -> None:
    inside = False
    for line in mappings.split("\n"):
        if line.startswith("c\t"):
            named = line.split("\t")[-1]
            inside = named.rsplit("/", 1)[-1] in wanted
            if inside:
                print(f"\n=== {named} ===")
        elif inside and line.startswith("\tf\t"):
            parts = line.split("\t")
            print(f"  field   {parts[-1]:34} {parts[2]}")
        elif inside and line.startswith("\tm\t"):
            parts = line.split("\t")
            print(f"  method  {parts[-1]:34} {parts[2]}")


if __name__ == "__main__":
    if len(sys.argv) < 3:
        sys.exit(__doc__)
    dump(load_mappings(sys.argv[1]), set(sys.argv[2:]))
