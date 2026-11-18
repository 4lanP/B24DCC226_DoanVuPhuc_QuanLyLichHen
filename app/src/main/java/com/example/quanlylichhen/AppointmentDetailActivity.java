package com.example.quanlylichhen;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Màn hình 4: chi tiết lịch hẹn.
 *
 * <p>Màn hình chỉ nhận {@code id} qua {@link android.content.Intent} rồi truy vấn lại
 * cơ sở dữ liệu, không truyền cả đối tượng. Người dùng có thể bật/tắt nhắc nhở (ghi ngay
 * vào cơ sở dữ liệu) hoặc xóa lịch hẹn. Khi quay lại, {@code MainActivity.onResume()}
 * sẽ tải lại danh sách và khối thống kê.</p>
 */
public class AppointmentDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    private int appointmentId;
    private Appointment appointment;

    private TextView tvReminderState;
    private MaterialSwitch switchReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);

        // Nền phía sau thanh trạng thái là màu teal đậm nên icon thanh trạng thái phải màu trắng.
        UiUtils.applyEdgeToEdge(this, false);
        applyWindowInsets();

        dbHelper = new DatabaseHelper(this);

        // Nhận id từ màn hình danh sách và truy vấn lại dữ liệu theo id.
        appointmentId = getIntent().getIntExtra(MainActivity.EXTRA_APPOINTMENT_ID, -1);
        appointment = dbHelper.getAppointmentById(appointmentId);
        if (appointment == null) {
            Toast.makeText(this, R.string.error_appointment_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        MaterialButton btnBackToList = findViewById(R.id.btnBackToList);
        btnBackToList.setOnClickListener(v -> finish());

        MaterialButton btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> confirmDelete());

        switchReminder = findViewById(R.id.switchReminder);
        tvReminderState = findViewById(R.id.tvReminderState);

        showAppointment();
    }

    /** Đổ dữ liệu lịch hẹn lên giao diện và gắn sự kiện cho Switch. */
    private void showAppointment() {
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvLocation = findViewById(R.id.tvDetailLocation);
        TextView tvDateTime = findViewById(R.id.tvDetailDateTime);

        tvTitle.setText(appointment.getTitle());
        tvLocation.setText(appointment.getLocation());
        tvDateTime.setText(
                DateTimeUtils.toDisplayDateTime(appointment.getDate(), appointment.getTime()));

        // Gán trạng thái ban đầu trước khi gắn listener để không ghi lại cơ sở dữ liệu.
        switchReminder.setOnCheckedChangeListener(null);
        switchReminder.setChecked(appointment.isReminder());
        updateReminderState(appointment.isReminder());

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateReminder(appointmentId, isChecked);
            appointment.setReminder(isChecked);
            updateReminderState(isChecked);
        });
    }

    /** Cập nhật dòng chữ "Đang bật" / "Đang tắt" dưới công tắc nhắc nhở. */
    private void updateReminderState(boolean on) {
        tvReminderState.setText(on ? R.string.reminder_state_on : R.string.reminder_state_off);
    }

    /** Hỏi lại trước khi xóa để tránh người dùng bấm nhầm. */
    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete_short, (dialog, which) -> deleteAppointment())
                .show();
    }

    private void deleteAppointment() {
        dbHelper.deleteAppointment(appointmentId);
        Toast.makeText(this, R.string.msg_deleted, Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Chừa chỗ cho thanh trạng thái ở header và thanh điều hướng ở cuối nội dung
     * khi ứng dụng vẽ tràn viền.
     */
    private void applyWindowInsets() {
        View headerContent = findViewById(R.id.headerContent);
        NestedScrollView scrollDetail = findViewById(R.id.scrollDetail);
        final int headerTopPadding = headerContent.getPaddingTop();
        final int scrollBottomPadding = scrollDetail.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootDetail),
                (view, windowInsets) -> {
                    Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                    headerContent.setPadding(headerContent.getPaddingLeft(),
                            headerTopPadding + bars.top,
                            headerContent.getPaddingRight(),
                            headerContent.getPaddingBottom());
                    scrollDetail.setPadding(scrollDetail.getPaddingLeft(),
                            scrollDetail.getPaddingTop(),
                            scrollDetail.getPaddingRight(),
                            scrollBottomPadding + bars.bottom);
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
