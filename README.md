# Ứng dụng Quản lý lịch hẹn (Android – Java + SQLite)

Bài thực hành lập trình Android: quản lý lịch hẹn cá nhân, lưu trữ bằng SQLite
(`SQLiteOpenHelper`), chuyển màn hình bằng `Intent` truyền `id`.

- Package: `com.example.quanlylichhen`
- Cơ sở dữ liệu: `appointments.db`, phiên bản 1
- `minSdk 24`, `targetSdk 36`, `compileSdk 36`, Java 11

## Trạng thái từng phần

| Phần | Nội dung | Trạng thái |
|---|---|---|
| 1 | Khung project, tầng dữ liệu (`Appointment`, `DatabaseHelper` + dữ liệu mẫu), màn hình Đăng nhập | **Xong** |
| 2 | Danh sách lịch hẹn + thống kê (`MainActivity`) | **Xong** |
| 2.5 | Chỉnh lại giao diện danh sách: giãn khoảng cách dòng, khối thống kê đè qua đáy header | **Xong** |
| 3 | Thêm lịch hẹn (`AddAppointmentActivity`) | **Xong** |
| 4 | Chi tiết lịch hẹn (`AppointmentDetailActivity`): đổi nhắc nhở, xóa | **Xong** |

## Cấu trúc thư mục

```
app/src/main/java/com/example/quanlylichhen/
├── Appointment.java          # Lớp mô hình một lịch hẹn (id, tên, địa điểm, ngày, giờ, nhắc nhở)
├── DatabaseHelper.java       # Toàn bộ truy vấn SQLite + tạo bảng + dữ liệu mẫu
├── AppointmentAdapter.java   # Đổ danh sách lịch hẹn vào ListView (có ViewHolder)
├── DateTimeUtils.java        # Đổi ngày giờ giữa dạng lưu CSDL và dạng hiển thị
├── UiUtils.java              # Cấu hình vẽ tràn viền (edge-to-edge)
├── LoginActivity.java        # Màn hình 1: đăng nhập
├── MainActivity.java         # Màn hình 2: danh sách + thống kê
├── AddAppointmentActivity.java     # Màn hình 3: thêm lịch hẹn
└── AppointmentDetailActivity.java  # Màn hình 4: chi tiết, đổi nhắc nhở, xóa
app/src/main/res/
├── layout/activity_login.xml, activity_main.xml, item_appointment.xml,
│        activity_add_appointment.xml, activity_appointment_detail.xml,
│        list_footer_hint.xml
├── drawable/                 # Icon vector, nền logo, nền header, pill ngày giờ, nhãn nhắc nhở
└── values/                   # colors, strings, themes
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
theo đúng bảng trong đề bài.

## Các phương thức của `DatabaseHelper`

`checkLogin`, `insertUser`, `insertAppointment`, `getAllAppointments`,
`getAppointmentById`, `updateReminder`, `deleteAppointment`, `countAll`,
`countReminders`.

## Chạy thử

1. Mở thư mục này bằng Android Studio, đợi Gradle sync.
2. Chạy trên emulator hoặc máy thật (API 24 trở lên).
3. Đăng nhập bằng `admin / 123456`.

Hoặc build bằng dòng lệnh:

```
gradlew.bat assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Ảnh chụp màn hình

- `docs/screenshots/01_login.png` – màn hình đăng nhập
- `docs/screenshots/02_login_error.png` – trạng thái sai tên đăng nhập/mật khẩu
- `docs/screenshots/03_main_list.png` – danh sách lịch hẹn + thống kê
- `docs/screenshots/04_main_empty.png` – trạng thái chưa có lịch hẹn nào
- `docs/screenshots/05_add_form.png` – màn hình thêm lịch hẹn (picker spinner)
- `docs/screenshots/06_add_empty_error.png` – lỗi khi tên lịch hẹn bị bỏ trống
- `docs/screenshots/07_add_time_error.png` – lỗi khi thời điểm hẹn ở quá khứ
- `docs/screenshots/08_main_after_add.png` – danh sách sau khi thêm (lịch mới đứng đầu, thống kê 4)
- `docs/screenshots/09_main_ui.png` – giao diện danh sách theo phác thảo mới
- `docs/screenshots/10_add_ui.png` – giao diện thêm lịch hẹn theo phác thảo mới
- `docs/screenshots/11_detail_ui.png` – màn hình chi tiết lịch hẹn

## Màn hình thêm lịch hẹn (phần 3)

Kiểm tra dữ liệu theo đúng đề khi nhấn **Lưu lịch hẹn**:

1. Tên lịch hẹn hoặc địa điểm bỏ trống → `setError` ngay tại ô đó, không lưu.
2. Thời điểm hẹn (ngày + giờ, so đến phút) không sau thời điểm hiện tại →
   hiện dòng đỏ "Thời điểm hẹn phải sau thời điểm hiện tại", cuộn tới chỗ đó, không lưu.
3. Hợp lệ → `insertAppointment()` ghi vào SQLite, `finish()` quay về danh sách;
   `MainActivity.onResume()` tự tải lại danh sách và thống kê.

Chi tiết dễ sai đã xử lý:

- `DatePicker.getMonth()` trả 0–11 nên phải **cộng 1** khi ghép chuỗi `yyyy-MM-dd`.
- So sánh thời điểm dùng `Calendar` với `SECOND`/`MILLISECOND = 0` để so đúng đến phút.
- `SimpleDateFormat` luôn kèm `Locale.US` (trong `DateTimeUtils`) để không phụ thuộc
  ngôn ngữ máy.
- **Picker spinner**: từ Android 5.0, theme Material ép `DatePicker` hiện dạng lịch và
  `TimePicker` hiện đồng hồ tròn, bỏ qua `spinnersShown`/`timePickerMode`. Đề yêu cầu
  TimePicker chế độ spinner nên phải thêm `android:theme="@android:style/Theme.Holo.Light"`
  cho cả hai view. Khung 24 giờ bật bằng `setIs24HourView(true)` trong code (thuộc tính
  XML `android:is24HourView` không tồn tại, aapt báo lỗi).

## Màn hình chi tiết (phần 4)

- `MainActivity` chỉ truyền `id` qua `Intent` (`EXTRA_APPOINTMENT_ID`); màn hình chi tiết
  gọi `getAppointmentById()` để đọc lại dữ liệu, không truyền cả đối tượng.
- Công tắc nhắc nhở ghi ngay vào SQLite bằng `updateReminder()` và đổi dòng chữ
  "Đang bật" / "Đang tắt" bên dưới.
- Nút **Xóa lịch hẹn** hỏi xác nhận, gọi `deleteAppointment()` rồi `finish()`;
  `MainActivity.onResume()` tải lại danh sách và thống kê.
- Nút **Quay lại** và nút back trên header đều đóng màn hình.

## Ghi chú

- Danh sách dùng `ListView` + `ArrayAdapter` tự viết (đề cho phép ListView hoặc
  RecyclerView): phù hợp với dữ liệu vài dòng, không cần thêm thư viện, và vẫn dùng
  ViewHolder để tránh lỗi tái sử dụng view khi cuộn.
- Ứng dụng vẽ tràn viền (edge-to-edge) vì từ Android 15 đây là bắt buộc với
  `targetSdk 35+`; mỗi màn hình tự cộng khoảng chừa hệ thống. Ở màn hình thêm, nút Lưu
  nằm trong vùng cuộn nên khoảng chừa thanh điều hướng/bàn phím được cộng vào đáy
  `NestedScrollView`.
- Header danh sách dùng `layout_height="wrap_content"` và `headerContent` có
  `paddingBottom="80dp"` để chừa chỗ cho thẻ thống kê. Thẻ neo vào đáy header với
  `layout_marginTop="-56dp"`, nhờ đó nó đè qua ranh giới header mà không che tiêu đề,
  kể cả trên máy có thanh trạng thái cao.
- Giao diện 3 màn hình danh sách, thêm và chi tiết bám theo phác thảo trong đề:
  header teal, pill ngày giờ, nhãn "Có nhắc"/"Không nhắc", nút bo tròn.
- Dữ liệu mẫu dùng ngày 25–27/11/2026 (đề ghi 25–27/10/2026 nhưng thời điểm làm bài
  đã qua tháng 10, đổi sang tháng 11 để lịch hẹn vẫn ở tương lai khi chạy thử).
- Không có màn hình sửa đầy đủ: đề chỉ yêu cầu cập nhật trạng thái nhắc nhở ở màn
  hình chi tiết.
#   B 2 4 D C C 2 2 6 _ D o a n V u P h u c _ Q u a n L y L i c h H e n  
 