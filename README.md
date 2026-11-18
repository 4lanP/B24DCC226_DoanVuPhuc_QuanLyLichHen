# B24DCC226_DoanVuPhuc_QuanLyLichHen

Ứng dụng Android **Quản lý lịch hẹn** — bài thực hành lập trình Android, lưu trữ bằng
SQLite (`SQLiteOpenHelper`), chuyển màn hình bằng `Intent` truyền `id`.

- Package: `com.example.quanlylichhen`
- Cơ sở dữ liệu: `appointments.db`, phiên bản 1
- `minSdk 24`, `targetSdk 36`, `compileSdk 36`, Java 11
- 4 màn hình: Đăng nhập → Danh sách → Thêm lịch hẹn / Chi tiết lịch hẹn

## Trạng thái

| Phần | Nội dung | Trạng thái |
|---|---|---|
| 1 | Khung project, tầng dữ liệu (`Appointment`, `DatabaseHelper` + dữ liệu mẫu), màn hình Đăng nhập | Xong |
| 2 | Danh sách lịch hẹn + thống kê (`MainActivity`) | Xong |
| 2.5 | Chỉnh giao diện danh sách theo phác thảo: giãn khoảng cách dòng, thống kê đè qua đáy header | Xong |
| 3 | Thêm lịch hẹn (`AddAppointmentActivity`) | Xong |
| 4 | Chi tiết lịch hẹn (`AppointmentDetailActivity`): đổi nhắc nhở, xóa | Xong |

## Cấu trúc thư mục

```
app/src/main/java/com/example/quanlylichhen/
├── Appointment.java               # Lớp mô hình một lịch hẹn
├── DatabaseHelper.java            # Toàn bộ truy vấn SQLite + tạo bảng + dữ liệu mẫu
├── AppointmentAdapter.java        # Đổ danh sách lịch hẹn vào ListView (có ViewHolder)
├── DateTimeUtils.java             # Đổi ngày giờ giữa dạng lưu CSDL và dạng hiển thị
├── UiUtils.java                   # Cấu hình vẽ tràn viền (edge-to-edge)
├── LoginActivity.java             # Màn hình 1: đăng nhập
├── MainActivity.java              # Màn hình 2: danh sách + thống kê
├── AddAppointmentActivity.java    # Màn hình 3: thêm lịch hẹn
└── AppointmentDetailActivity.java # Màn hình 4: chi tiết, đổi nhắc nhở, xóa
app/src/main/res/
├── layout/  activity_login.xml, activity_main.xml, item_appointment.xml,
│           activity_add_appointment.xml, activity_appointment_detail.xml,
│           list_footer_hint.xml
├── drawable/                      # Icon vector, nền logo, nền header, pill ngày giờ
└── values/                        # colors, strings, themes
```

## Cơ sở dữ liệu

Bảng `users`

| Cột | Kiểu | Ràng buộc |
|---|---|---|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| username | TEXT | NOT NULL, UNIQUE |
| password | TEXT | NOT NULL |

Bảng `appointments`

| Cột | Kiểu | Ràng buộc |
|---|---|---|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| title | TEXT | NOT NULL |
| location | TEXT | NOT NULL |
| date | TEXT | NOT NULL (yyyy-MM-dd) |
| time | TEXT | NOT NULL (HH:mm) |
| reminder | INTEGER | NOT NULL (1: bật, 0: tắt) |

Ngày và giờ lưu dạng chuỗi có định dạng cố định nên `ORDER BY date ASC, time ASC`
cho đúng thứ tự thời gian mà không cần hàm chuyển đổi.

Dữ liệu mẫu được chèn trong `onCreate()`: tài khoản `admin / 123456` và 3 lịch hẹn
theo bảng trong đề bài.

## Các phương thức của `DatabaseHelper`

`checkLogin`, `insertUser`, `insertAppointment`, `getAllAppointments`,
`getAppointmentById`, `updateReminder`, `deleteAppointment`, `countAll`,
`countReminders` — mọi truy vấn đều dùng tham số `?` + `selectionArgs`, `Cursor`
đóng bằng try-with-resources.

## Chức năng từng màn hình

**1. Đăng nhập** — kiểm tra bảng `users` bằng `checkLogin()`. Bỏ trống ô nào báo lỗi ô
đó; sai tài khoản hiện "Sai tên đăng nhập hoặc mật khẩu"; đúng thì mở danh sách và
`finish()` để nút Back không quay lại.

**2. Danh sách** — đọc lại dữ liệu trong `onResume()` nên tự cập nhật sau khi thêm,
xóa hoặc đổi nhắc nhở. Khối thống kê hiển thị tổng số lịch hẹn và số đang bật nhắc
nhở; mỗi dòng có tên, địa điểm, ngày giờ `dd/MM/yyyy · HH:mm` và nhãn "Có nhắc" /
"Không nhắc"; danh sách rỗng hiện thông báo thay cho danh sách.

**3. Thêm lịch hẹn** — kiểm tra theo đề khi nhấn **Lưu lịch hẹn**:

1. Tên hoặc địa điểm bỏ trống → `setError` tại ô đó, không lưu.
2. Thời điểm hẹn (so đến phút) không sau thời điểm hiện tại → hiện dòng đỏ
   "Thời điểm hẹn phải sau thời điểm hiện tại", không lưu.
3. Hợp lệ → `insertAppointment()` rồi `finish()`; danh sách và thống kê tự cập nhật.

**4. Chi tiết** — nhận `id` qua `Intent`, gọi `getAppointmentById()` để đọc lại (không
truyền cả đối tượng). Công tắc nhắc nhở ghi ngay vào SQLite bằng `updateReminder()` và
đổi dòng chữ "Đang bật" / "Đang tắt". Nút **Xóa lịch hẹn** hỏi xác nhận rồi
`deleteAppointment()` và `finish()`; nút **Quay lại** đóng màn hình.

## Chạy thử

1. Mở thư mục này bằng Android Studio, đợi Gradle sync.
2. Chạy trên emulator hoặc máy thật (API 24 trở lên).
3. Đăng nhập bằng `admin / 123456`.

Hoặc build bằng dòng lệnh:

```
gradlew.bat assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Báo cáo

Báo cáo Word/PDF nằm trong thư mục [eport/](report/README.md):
B24DCCC226_Đoàn Vũ Phúc_Báo cáo.docx và bản PDF (13 trang, 12 ảnh minh chứng).
Dựng lại bằng eport/build_report.py.

## Ảnh chụp màn hình

| Tệp | Nội dung |
|---|---|
| `docs/screenshots/01_login.png` | Màn hình đăng nhập |
| `docs/screenshots/02_login_error.png` | Sai tên đăng nhập hoặc mật khẩu |
| `docs/screenshots/03_main_list.png` | Danh sách lịch hẹn + thống kê (3 lịch mẫu) |
| `docs/screenshots/04_main_empty.png` | Trạng thái chưa có lịch hẹn nào |
| `docs/screenshots/05_add_form.png` | Màn hình thêm lịch hẹn (DatePicker/TimePicker spinner) |
| `docs/screenshots/06_add_empty_error.png` | Lỗi khi tên lịch hẹn bỏ trống |
| `docs/screenshots/07_main_after_add.png` | Danh sách sau khi thêm (4 lịch, thống kê cập nhật) |
| `docs/screenshots/08_detail.png` | Chi tiết lịch hẹn |
| `docs/screenshots/09_delete_confirm.png` | Hộp thoại xác nhận xóa |

## Ghi chú kỹ thuật

- **ListView + ArrayAdapter tự viết** (đề cho phép ListView hoặc RecyclerView): phù hợp
  với danh sách vài dòng, không cần thêm thư viện, vẫn dùng ViewHolder để tránh lỗi tái
  sử dụng view khi cuộn.
- **Picker spinner**: từ Android 5.0, theme Material ép `DatePicker` hiện dạng lịch và
  `TimePicker` hiện đồng hồ tròn, bỏ qua `spinnersShown`/`timePickerMode`. Đề yêu cầu
  TimePicker chế độ spinner nên cả hai view được ép
  `android:theme="@android:style/Theme.Holo.Light"`. Khung 24 giờ bật bằng
  `setIs24HourView(true)` trong code (thuộc tính XML `android:is24HourView` không tồn tại).
- **`DatePicker.getMonth()` trả 0–11** nên phải cộng 1 khi ghép chuỗi `yyyy-MM-dd`;
  so sánh thời điểm bằng `Calendar` với `SECOND`/`MILLISECOND = 0` để so đúng đến phút.
- **Edge-to-edge**: từ Android 15 là bắt buộc với `targetSdk 35+`, mỗi màn hình tự cộng
  khoảng chừa thanh trạng thái/thanh điều hướng (và bàn phím ở màn hình thêm).
- **Nút Lưu** của màn hình thêm được ghim ngoài vùng cuộn nên luôn nhìn thấy đủ; phần
  form phía trên cuộn độc lập.
- **Thẻ thống kê** neo đỉnh vào đáy header với `layout_marginTop="-56dp"`, nên đè qua
  ranh giới header mà không che tiêu đề, kể cả khi thanh trạng thái cao.
- Dữ liệu mẫu dùng ngày 25–27/11/2026 (đề ghi 25–27/10/2026, nhưng thời điểm làm bài đã
  qua tháng 10 nên đổi sang tháng 11 để lịch hẹn vẫn ở tương lai khi chạy thử).
- Không có màn hình sửa đầy đủ: đề chỉ yêu cầu cập nhật trạng thái nhắc nhở ở màn hình
  chi tiết.
