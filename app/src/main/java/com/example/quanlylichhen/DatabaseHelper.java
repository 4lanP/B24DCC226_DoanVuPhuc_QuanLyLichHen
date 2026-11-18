package com.example.quanlylichhen;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp duy nhất làm việc với cơ sở dữ liệu SQLite của ứng dụng.
 *
 * <p>Cơ sở dữ liệu {@code appointments.db} phiên bản 1 gồm hai bảng: {@code users} (tài khoản
 * đăng nhập) và {@code appointments} (lịch hẹn). Bảng và dữ liệu mẫu được tạo trong
 * {@link #onCreate(SQLiteDatabase)}.</p>
 *
 * <p>Mọi câu truy vấn đều dùng tham số {@code ?} kèm {@code selectionArgs} để tránh lỗi
 * ghép chuỗi, và mọi {@link Cursor} đều được đóng bằng try-with-resources.</p>
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "appointments.db";
    public static final int DB_VERSION = 1;

    // Tên bảng
    public static final String TABLE_USERS = "users";
    public static final String TABLE_APPOINTMENTS = "appointments";

    // Cột bảng users
    public static final String COL_USER_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";

    // Cột bảng appointments
    public static final String COL_APPOINTMENT_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_LOCATION = "location";
    public static final String COL_DATE = "date";
    public static final String COL_TIME = "time";
    public static final String COL_REMINDER = "reminder";

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " ("
                    + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_USERNAME + " TEXT NOT NULL UNIQUE, "
                    + COL_PASSWORD + " TEXT NOT NULL)";

    private static final String SQL_CREATE_APPOINTMENTS =
            "CREATE TABLE " + TABLE_APPOINTMENTS + " ("
                    + COL_APPOINTMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_TITLE + " TEXT NOT NULL, "
                    + COL_LOCATION + " TEXT NOT NULL, "
                    + COL_DATE + " TEXT NOT NULL, "
                    + COL_TIME + " TEXT NOT NULL, "
                    + COL_REMINDER + " INTEGER NOT NULL DEFAULT 0)";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_APPOINTMENTS);
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Chèn dữ liệu mẫu của đề bài: tài khoản admin / 123456 và 3 lịch hẹn.
     * Dùng chính {@code db} đang được mở để tránh gọi lại {@link #getWritableDatabase()}
     * ngay trong lúc {@link #onCreate(SQLiteDatabase)} đang chạy.
     */
    private void insertSampleData(SQLiteDatabase db) {
        insertUser(db, "admin", "123456");

        insertAppointment(db, new Appointment(
                "Họp nhóm đồ án", "Phòng B203", "2026-11-25", "09:00", true));
        insertAppointment(db, new Appointment(
                "Khám răng", "Nha khoa Minh Tâm", "2026-11-26", "14:30", true));
        insertAppointment(db, new Appointment(
                "Đi cà phê với bạn", "Highland Coffee", "2026-11-27", "19:00", false));
    }

    // ------------------------------------------------------------------
    // Bảng users
    // ------------------------------------------------------------------

    /** Trả về true nếu có dòng khớp cả tên đăng nhập và mật khẩu. */
    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ?";
        String[] selectionArgs = new String[]{username, password};
        try (Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                selection, selectionArgs, null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    /** Thêm tài khoản mới, trả về id vừa thêm (hoặc -1 nếu lỗi). */
    public long insertUser(String username, String password) {
        return insertUser(getWritableDatabase(), username, password);
    }

    private long insertUser(SQLiteDatabase db, String username, String password) {
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        return db.insert(TABLE_USERS, null, values);
    }

    // ------------------------------------------------------------------
    // Bảng appointments
    // ------------------------------------------------------------------

    /** Thêm lịch hẹn, trả về id vừa thêm (hoặc -1 nếu lỗi). */
    public long insertAppointment(Appointment appointment) {
        return insertAppointment(getWritableDatabase(), appointment);
    }

    private long insertAppointment(SQLiteDatabase db, Appointment appointment) {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, appointment.getTitle());
        values.put(COL_LOCATION, appointment.getLocation());
        values.put(COL_DATE, appointment.getDate());
        values.put(COL_TIME, appointment.getTime());
        values.put(COL_REMINDER, appointment.isReminder() ? 1 : 0);
        return db.insert(TABLE_APPOINTMENTS, null, values);
    }

    /** Lấy toàn bộ lịch hẹn, sắp xếp tăng dần theo ngày rồi theo giờ. */
    public List<Appointment> getAllAppointments() {
        List<Appointment> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String orderBy = COL_DATE + " ASC, " + COL_TIME + " ASC";
        try (Cursor cursor = db.query(TABLE_APPOINTMENTS, null, null, null, null, null, orderBy)) {
            while (cursor.moveToNext()) {
                result.add(readAppointment(cursor));
            }
        }
        return result;
    }

    /** Lấy một lịch hẹn theo id; trả về null nếu không tồn tại. */
    public Appointment getAppointmentById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = COL_APPOINTMENT_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        try (Cursor cursor = db.query(TABLE_APPOINTMENTS, null, selection, selectionArgs,
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return readAppointment(cursor);
            }
        }
        return null;
    }

    /** Cập nhật trạng thái nhắc nhở, trả về số dòng đã sửa. */
    public int updateReminder(int id, boolean on) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_REMINDER, on ? 1 : 0);
        String whereClause = COL_APPOINTMENT_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        return db.update(TABLE_APPOINTMENTS, values, whereClause, whereArgs);
    }

    /** Xóa lịch hẹn theo id, trả về số dòng đã xóa. */
    public int deleteAppointment(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COL_APPOINTMENT_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        return db.delete(TABLE_APPOINTMENTS, whereClause, whereArgs);
    }

    /** Tổng số lịch hẹn. */
    public int countAll() {
        return count("SELECT COUNT(*) FROM " + TABLE_APPOINTMENTS);
    }

    /** Số lịch hẹn đang bật nhắc nhở. */
    public int countReminders() {
        return count("SELECT COUNT(*) FROM " + TABLE_APPOINTMENTS + " WHERE " + COL_REMINDER + " = 1");
    }

    private int count(String sql) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    /** Đọc một dòng con trỏ thành đối tượng Appointment. */
    private Appointment readAppointment(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_APPOINTMENT_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
        String location = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
        String time = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME));
        boolean reminder = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REMINDER)) == 1;
        return new Appointment(id, title, location, date, time, reminder);
    }
}
