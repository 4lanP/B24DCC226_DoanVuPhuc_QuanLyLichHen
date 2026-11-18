# -*- coding: utf-8 -*-
"""Phân tích trang PDF đã render: mật độ mực, phạm vi nội dung, phát hiện trang trống."""
import glob
import os
import sys

from PIL import Image

folder = sys.argv[1] if len(sys.argv) > 1 else r".preview\render"
files = sorted(glob.glob(os.path.join(folder, "page-*.png")))
print("%-14s %6s %7s %7s %7s" % ("trang", "ink%", "cao%", "rong%", "dòng đen%"))
for path in files:
    im = Image.open(path).convert("L")
    w, h = im.size
    px = im.load()
    step = 3
    ink = 0
    miny, maxy, minx, maxx = h, 0, w, 0
    rows_with_ink = 0
    for y in range(0, h, step):
        row_ink = 0
        for x in range(0, w, step):
            if px[x, y] < 200:
                row_ink += 1
                ink += 1
                miny = min(miny, y)
                maxy = max(maxy, y)
                minx = min(minx, x)
                maxx = max(maxx, x)
        if row_ink:
            rows_with_ink += 1
    total = (h // step) * (w // step)
    rows_total = h // step
    print("%-14s %5.1f %6.1f %7.1f %7.1f" % (
        os.path.basename(path),
        100.0 * ink / total,
        100.0 * (maxy - miny) / h if ink else 0,
        100.0 * (maxx - minx) / w if ink else 0,
        100.0 * rows_with_ink / rows_total))
