# -*- coding: utf-8 -*-
"""Dò vị trí các ảnh chụp màn hình trên từng trang (nhận diện màu teal của header app)."""
import glob
import os
import sys

from PIL import Image

folder = sys.argv[1] if len(sys.argv) > 1 else r".preview\render"

def is_teal(px):
    r, g, b = px
    return 15 <= r <= 60 and 90 <= g <= 145 and 80 <= b <= 135

for path in sorted(glob.glob(os.path.join(folder, "page-*.png"))):
    im = Image.open(path).convert("RGB")
    w, h = im.size
    px = im.load()
    bands = []
    in_band = False
    start = 0
    for y in range(0, h, 4):
        count = 0
        for x in range(0, w, 8):
            if is_teal(px[x, y]):
                count += 1
        teal_row = count > (w // 8) * 0.15
        if teal_row and not in_band:
            in_band, start = True, y
        elif not teal_row and in_band:
            if y - start > 8:
                bands.append((start, y))
            in_band = False
    pct = [("%d-%d%%" % (100 * a // h, 100 * b // h)) for a, b in bands]
    print("%-14s so vung teal: %d  %s" % (os.path.basename(path), len(bands), pct))
