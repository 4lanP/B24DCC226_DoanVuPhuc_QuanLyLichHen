package com.example.quanlylichhen;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

/**
 * Màn hình 3: thêm lịch hẹn mới.
 *
 * <p>Người dùng nhập tên và địa điểm, chọn ngày bằng {@link DatePicker}, giờ bằng
 * {@link TimePicker} (chế độ spinner) và bật/tắt nhắc nhở bằng Switch. Khi nhấn Lưu:
 * tên hoặc địa điểm bỏ trống thì báo lỗi tại ô đó và không lưu; thời điểm hẹn phải
 * sau thời điểm hiện tại, nếu không thì báo lỗi và không lưu. Lưu thành công thì
 * ghi vào cơ sở dữ liệu và {@code finish()} quay về màn hình danh sách (danh sách và
 * thống kê được {@code MainActivity.onResume()} tải lại).</p>
 */
public class AddAppointmentActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    private TextInputLayout tilTitle;
    private TextInputLayout tilLocation;
    private TextInputEditText edtTitle;
    private TextInputEditText edtLocation;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private MaterialSwitch switchReminder;
    private TextView tvTimeError;
    private NestedScrollView scrollForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);

        // Nền phía sau thanh trạng thái là màu teal đậm nên icon thanh trạng thái phải màu trắng.
        UiUtils.applyEdgeToEdge(this, false);

        dbHelper = new DatabaseHelper(this);

        tilTitle = findViewById(R.id.tilTitle);
        tilLocation = findViewById(R.id.tilLocation);
        edtTitle = findViewById(R.id.edtTitle);
        edtLocation = findViewById(R.id.edtLocation);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        // Giờ lưu trong CSDL dạng HH:mm nên dùng khung 24 giờ.
        timePicker.setIs24HourView(true);
        switchReminder = findViewById(R.id.switchReminder);
        tvTimeError = findViewById(R.id.tvTimeError);
        scrollForm = findViewById(R.id.scrollForm);

        applyWindowInsets();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveAppointment());

        // Người dùng sửa lại nội dung thì xóa thông báo lỗi cũ của ô đó.
        clearErrorWhenTyping(edtTitle, tilTitle);
        clearErrorWhenTyping(edtLocation, tilLocation);

        // Đổi ngày hoặc giờ thì ẩn thông báo lỗi thời gian cũ đi.
        datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                (view, year, month, dayOfMonth) -> hideTimeError());
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> hideTimeError());
    }

    /**
     * Kiểm tra dữ liệu nhập rồi lưu lịch hẹn vào cơ sở dữ liệu.
     * Thứ tự kiểm tra theo đề bài: (1) tên, địa điểm không được trống;
     * (2) thời điểm hẹn phải sau thời điểm hiện tại.
     */
    private void saveAppointment() {
        String title = textOf(edtTitle);
        String location = textOf(edtLocation);

        tilTitle.setError(null);
        tilLocation.setError(null);
        hideTimeError();

        // 1. Không cho phép lưu khi tên lịch hẹn hoặc địa điểm bị bỏ trống.
        if (title.isEmpty()) {
            tilTitle.setError(getString(R.string.error_title_empty));
            edtTitle.requestFocus();
            return;
        }
        if (location.isEmpty()) {
            tilLocation.setError(getString(R.string.error_location_empty));
            edtLocation.requestFocus();
            return;
        }

        // Đọc ngày từ DatePicker (getMonth() trả 0-11 nên phải cộng 1) và giờ từ TimePicker.
        String date = String.format(Locale.US, "%04d-%02d-%02d",
                datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
        String time = String.format(Locale.US, "%02d:%02d",
                timePicker.getHour(), timePicker.getMinute());

        // 2. Thời điểm hẹn phải sau thời điểm hiện tại (so đến phút).
        Calendar appointmentTime = DateTimeUtils.toCalendar(date, time);
        Calendar now = Calendar.getInstance();
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        if (!appointmentTime.after(now)) {
            tvTimeError.setVisibility(View.VISIBLE);
            // Cuộn xuống để người dùng thấy thông báo và đổi giờ.
            scrollForm.post(() -> scrollForm.fullScroll(View.FOCUS_DOWN));
            return;
        }

        // Hợp lệ: lưu vào cơ sở dữ liệu rồi quay về màn hình danh sách.
        Appointment appointment = new Appointment(
                title, location, date, time, switchReminder.isChecked());
        dbHelper.insertAppointment(appointment);

        Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private String textOf(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void hideTimeError() {
        tvTimeError.setVisibility(View.GONE);
    }

    /** Xóa thông báo lỗi của một ô nhập ngay khi người dùng sửa nội dung ô đó. */
    private void clearErrorWhenTyping(TextInputEditText editText, TextInputLayout layout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (layout.getError() != null) {
                    layout.setError(null);
                }
                hideTimeError();
            }
        });
    }

    /**
     * Chừa chỗ cho thanh trạng thái ở header và thanh điều hướng/bàn phím ở nút Lưu.
     */
    private void applyWindowInsets() {
        View headerContent = findViewById(R.id.headerContent);
        View btnSave = findViewById(R.id.btnSave);
        final int headerTopPadding = headerContent.getPaddingTop();
        final int btnSaveBottomMargin =
                ((ConstraintLayout.LayoutParams) btnSave.getLayoutParams()).bottomMargin;

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootAdd), (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            headerContent.setPadding(headerContent.getPaddingLeft(), headerTopPadding + bars.top,
                    headerContent.getPaddingRight(), headerContent.getPaddingBottom());

            // Nút Lưu ghim dưới cùng nên phải nằm trên cả thanh điều hướng lẫn bàn phím.
            ConstraintLayout.LayoutParams params =
                    (ConstraintLayout.LayoutParams) btnSave.getLayoutParams();
            params.bottomMargin = btnSaveBottomMargin + Math.max(bars.bottom, ime.bottom);
            btnSave.setLayoutParams(params);

            return windowInsets;
        });
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
