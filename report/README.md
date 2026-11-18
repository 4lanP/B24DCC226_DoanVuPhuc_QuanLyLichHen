# Báo cáo bài thực hành — Ứng dụng Quản lý lịch hẹn

Thư mục này chứa báo cáo Word/PDF và bộ script dựng lại báo cáo.

## Tệp bàn giao

| Tệp | Nội dung |
|---|---|
| `B24DCCC226_Đoàn Vũ Phúc_Báo cáo.docx` | Báo cáo Word (13 trang) |
| `B24DCCC226_Đoàn Vũ Phúc_Báo cáo.pdf` | Bản PDF để nộp kèm |

## Cấu trúc báo cáo

Báo cáo dựng theo mẫu của giảng viên (giữ nguyên khối tiêu đề trường, style Heading
1/2 và font Arial 11 của file mẫu):

1. **Giới thiệu ứng dụng** — mục tiêu, phạm vi kỹ thuật, tài khoản và dữ liệu mẫu.
2. **Màn hình đăng nhập** — 3 ảnh minh chứng + mô tả kỹ thuật.
3. **Màn hình danh sách lịch hẹn (MainActivity)** — 3 ảnh + mô tả kỹ thuật.
4. **Màn hình Thêm lịch hẹn (AddAppointmentActivity)** — 3 ảnh + mô tả kỹ thuật.
5. **Màn hình Chi tiết lịch hẹn (AppointmentDetailActivity)** — 3 ảnh + mô tả kỹ thuật.
6. **Cơ sở dữ liệu và lớp DatabaseHelper** — 3 bảng (users, appointments, phương thức).
7. **Kết luận** — kết quả đạt được, kỹ năng, hạn chế.

Mỗi màn hình đều có mục **Hình ảnh minh chứng** (ảnh chụp thật từ emulator, chú thích
dạng "1.1.…") và mục **Mô tả kỹ thuật nội dung đã làm** (gạch đầu dòng theo thành phần
giao diện, xử lý dữ liệu, xử lý sự kiện).

## Dựng lại báo cáo

```powershell
$py   = "C:\Users\Admin\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe"
$node = "C:\Users\Admin\AppData\Local\Programs\DeepSeek Harness\resources\runtime\primary-runtime\dependencies\node\bin\node.exe"
$cli  = "C:\Users\Admin\AppData\Local\Programs\DeepSeek Harness\resources\app.asar.unpacked\dsh\node_modules\@deepseek-ai\libreoffice-kit\lib\cli.js"

cd C:\Users\Admin\OneDrive\Documents\thapp\report
& $py build_report.py                     # sinh .docx
Remove-Item ".\B24DCCC226_Đoàn Vũ Phúc_Báo cáo.pdf" -ErrorAction SilentlyContinue
& $node $cli convert --input ".\B24DCCC226_Đoàn Vũ Phúc_Báo cáo.docx" --output ".\B24DCCC226_Đoàn Vũ Phúc_Báo cáo.pdf"

New-Item -ItemType Directory -Force -Path .\.preview | Out-Null
& $node $cli render --input ".\B24DCCC226_Đoàn Vũ Phúc_Báo cáo.pdf" --output-dir ".\.preview\render" --pages "all"
& $py check_pages.py ".preview\render"     # soi trang trống / lỗ hổng cuối trang
& $py check_figures.py ".preview\render"   # dò vị trí ảnh trên từng trang
& $py qa_report.py                         # soát ký tự markdown, khoảng trắng, tiêu đề
```

## Ghi chú kỹ thuật

- `build_report.py` lấy **chính file mẫu** của giảng viên làm nền
  (`C:\Users\Admin\Downloads\B24DCCC226_Đoàn Vũ Phúc_Báo cáo.docx`), xoá nội dung cũ
  nhưng giữ style, khổ giấy, lề và bảng tiêu đề trường. Vì vậy báo cáo trùng định dạng
  với mẫu: A4, lề 2,5 cm đều, thân bài Arial 11, Heading 1/2/3 = 16/14/12 pt.
- Bảng tiêu đề phải chèn vào **đầu** `body` (trước `sectPr`); nếu `append` thì bảng rơi
  xuống cuối tài liệu.
- Ảnh chụp rộng 4,4 cm (tỷ lệ 1080 × 2400 nên cao 9,8 cm), đặt căn giữa, chú thích
  ngay dưới và bật `keep_with_next` để chú thích không bị tách khỏi ảnh.
- Bảng dữ liệu dùng style `Normal Table` + viền xám 0,5 pt; bề rộng cột đặt cả
  `cell.width` lẫn `w:tblGrid/w:gridCol`.
- `assets/` chứa 12 ảnh minh chứng, lấy từ `../docs/screenshots/` và ảnh chụp bổ sung.

## Kiểm tra đã chạy

- 13 trang, không có trang trắng; trang cuối (Kết luận) đầy 45%.
- 0 ký tự markdown sót, 0 khoảng trắng đôi.
- 12 chú thích hình khớp 12 ảnh; 3 bảng có chú thích "Bảng 1/2/3".
- Mở lại DOCX bằng python-docx: 135 đoạn, 4 bảng, 13 ảnh.
