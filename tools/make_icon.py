#!/usr/bin/env python3
"""Generates assets/nametag_health/icon.png (128x128) with no third-party dependencies.

Run from the project root:  python3 tools/make_icon.py
"""

import struct
import zlib
from pathlib import Path

SIZE = 128
BG = (0x22, 0x26, 0x30)
BG_EDGE = (0x2E, 0x34, 0x42)
CORNER_RADIUS = 18

HEART = [
    "..XXX...XXX..",
    ".XXXXX.XXXXX.",
    "XXXXXXXXXXXXX",
    "XXXXXXXXXXXXX",
    "XXXXXXXXXXXXX",
    ".XXXXXXXXXXX.",
    "..XXXXXXXXX..",
    "...XXXXXXX...",
    "....XXXXX....",
    ".....XXX.....",
    "......X......",
]
SCALE = 8
HEART_TOP = (SIZE - len(HEART) * SCALE) // 2
HEART_LEFT = (SIZE - len(HEART[0]) * SCALE) // 2

HEART_LIGHT = (0xFF, 0x8A, 0x8A)
HEART_MAIN = (0xE8, 0x3B, 0x3B)
HEART_DARK = (0xA8, 0x1F, 0x2B)


def rounded_alpha(x, y):
    """0 outside the rounded square, 255 inside."""
    r = CORNER_RADIUS
    cx = min(max(x, r), SIZE - 1 - r)
    cy = min(max(y, r), SIZE - 1 - r)
    dx, dy = x - cx, y - cy
    return 0 if dx * dx + dy * dy > r * r else 255


def heart_color(px, py):
    """Shade the heart by row so it reads as pixel art rather than a flat blob."""
    rows = len(HEART)
    if py < rows * 0.35:
        return HEART_LIGHT if px < len(HEART[0]) * 0.45 else HEART_MAIN
    if py > rows * 0.75:
        return HEART_DARK
    return HEART_MAIN


def build_rows():
    rows = []
    for y in range(SIZE):
        row = bytearray()
        for x in range(SIZE):
            alpha = rounded_alpha(x, y)
            # Subtle vertical gradient on the plate.
            mix = y / (SIZE - 1)
            color = tuple(round(BG[i] + (BG_EDGE[i] - BG[i]) * mix) for i in range(3))

            hx = (x - HEART_LEFT) // SCALE
            hy = (y - HEART_TOP) // SCALE
            if 0 <= hy < len(HEART) and 0 <= hx < len(HEART[0]) and HEART[hy][hx] == "X":
                color = heart_color(hx, hy)

            row += bytes((*color, alpha))
        rows.append(bytes(row))
    return rows


def write_png(path, rows):
    raw = b"".join(b"\x00" + row for row in rows)

    def chunk(tag, data):
        body = tag + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")

    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)
    return len(png)


if __name__ == "__main__":
    target = Path(__file__).resolve().parents[1] / "src/main/resources/assets/nametag_health/icon.png"
    size = write_png(target, build_rows())
    print(f"wrote {target} ({size} bytes)")
