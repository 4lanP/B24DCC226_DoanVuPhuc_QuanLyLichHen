package com.example.quanlylichhen;

/**
 * Một lịch hẹn của người dùng.
 *
 * <p>Lớp chỉ giữ dữ liệu, không truy cập cơ sở dữ liệu. Ngày và giờ được lưu dưới dạng
 * chuỗi đúng như trong bảng {@code appointments}: ngày {@code yyyy-MM-dd}, giờ {@code HH:mm}.
 * Nhờ định dạng này, câu truy vấn {@code ORDER BY date ASC, time ASC} cho kết quả đúng thứ tự
 * thời gian mà không cần hàm chuyển đổi.</p>
 */
public class Appointment {

    private int id;
    private String title;
    private String location;
    private String date;      // yyyy-MM-dd
    private String time;      // HH:mm
    private boolean reminder; // true: bật nhắc nhở

    public Appointment() {
    }

    /** Dùng khi thêm mới: chưa có id (id do SQLite sinh ra). */
    public Appointment(String title, String location, String date, String time, boolean reminder) {
        this.title = title;
        this.location = location;
        this.date = date;
        this.time = time;
        this.reminder = reminder;
    }

    /** Dùng khi đọc từ cơ sở dữ liệu lên. */
    public Appointment(int id, String title, String location, String date, String time, boolean reminder) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.date = date;
        this.time = time;
        this.reminder = reminder;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isReminder() {
        return reminder;
    }

    public void setReminder(boolean reminder) {
        this.reminder = reminder;
    }
}
