package com.example.quanlylichhen;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Chuyển đổi qua lại giữa chuỗi ngày giờ lưu trong cơ sở dữ liệu và chuỗi hiển thị.
 *
 * <p>Trong cơ sở dữ liệu, ngày lưu dạng {@code yyyy-MM-dd} và giờ dạng {@code HH:mm}
 * để {@code ORDER BY} sắp xếp đúng. Trên giao diện, ngày hiển thị dạng
 * {@code dd/MM/yyyy} theo yêu cầu của đề bài.</p>
 */
public final class DateTimeUtils {

    public static final String PATTERN_DATE_DB = "yyyy-MM-dd";
    public static final String PATTERN_TIME_DB = "HH:mm";
    public static final String PATTERN_DATE_DISPLAY = "dd/MM/yyyy";

    private DateTimeUtils() {
        // Lớp tiện ích, không khởi tạo.
    }

    /** "2026-11-25" -> "25/11/2026". Chuỗi không hợp lệ thì trả lại nguyên bản. */
    public static String toDisplayDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return "";
        }
        SimpleDateFormat dbFormat = new SimpleDateFormat(PATTERN_DATE_DB, Locale.US);
        dbFormat.setLenient(false);
        try {
            Date date = dbFormat.parse(isoDate);
            SimpleDateFormat displayFormat = new SimpleDateFormat(PATTERN_DATE_DISPLAY, Locale.US);
            return displayFormat.format(date);
        } catch (ParseException e) {
            return isoDate;
        }
    }

    /** "2026-11-25" + "09:00" -> "25/11/2026 · 09:00". */
    public static String toDisplayDateTime(String isoDate, String isoTime) {
        String time = (isoTime == null) ? "" : isoTime;
        return toDisplayDate(isoDate) + " · " + time;
    }

    /** Đổi ngày + giờ trong cơ sở dữ liệu thành {@link Calendar} để so sánh thời điểm. */
    public static Calendar toCalendar(String isoDate, String isoTime) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(PATTERN_DATE_DB + " " + PATTERN_TIME_DB, Locale.US);
        format.setLenient(false);
        try {
            Date date = format.parse(isoDate + " " + isoTime);
            calendar.setTime(date);
        } catch (ParseException e) {
            // Ngày giờ không hợp lệ: trả về thời điểm hiện tại để nơi gọi tự xử lý.
            calendar.setTimeInMillis(System.currentTimeMillis());
        }
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /** Ngày hôm nay dạng {@code yyyy-MM-dd} (định dạng lưu trong cơ sở dữ liệu). */
    public static String todayIsoDate() {
        return new SimpleDateFormat(PATTERN_DATE_DB, Locale.US).format(new Date());
    }

    /** Giờ hiện tại dạng {@code HH:mm}. */
    public static String nowIsoTime() {
        return new SimpleDateFormat(PATTERN_TIME_DB, Locale.US).format(new Date());
    }
}
