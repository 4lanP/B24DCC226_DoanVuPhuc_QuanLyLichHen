# -*- coding: utf-8 -*-
"""Soát lỗi văn bản báo cáo: ký tự markdown sót, khoảng trắng đôi, tham chiếu Bảng/Hình."""
import re

import docx

PATH = r"C:\Users\Admin\OneDrive\Documents\thapp\report\B24DCCC226_DoanVuPhuc_BaoCao.docx"

d = docx.Document(PATH)

paras = [p for p in d.paragraphs]
texts = [p.text for p in paras]

print("So doan:", len(paras), "| So bang:", len(d.tables), "| So anh:", len(d.inline_shapes))

md_chars = ["**", "__", "`", "##", "\\*"]
found = []
for i, t in enumerate(texts):
    for ch in md_chars:
        if ch in t:
            found.append((i, ch, t[:70]))
print("Ky tu markdown sot:", len(found))
for f in found[:5]:
    print("   ", f)

double_space = [(i, t[:60]) for i, t in enumerate(texts) if "  " in t]
print("Doan co 2 khoang trang lien tiep:", len(double_space))
for f in double_space[:5]:
    print("   ", f)

refs = [t for t in texts if re.search(r"(Bảng|Hình)\s+\d", t)]
print("Cau van tham chieu 'Bang N'/'Hinh N':", len(refs))
for r in refs[:8]:
    print("   ", r[:80])

print()
print("=== TOAN BO TIEU DE + CHU THICH ===")
for p in paras:
    t = p.text.strip()
    if not t:
        continue
    if p.style.name.startswith("Heading") or re.match(r"^\d\.\d\.", t):
        print("  [%s] %s" % (p.style.name, t))

words = sum(len(t.split()) for t in texts)
print()
print("Tong so tu (khong tinh bang):", words)
