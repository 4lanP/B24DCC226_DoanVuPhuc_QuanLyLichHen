package com.example.quanlylichhen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Màn hình 2: danh sách lịch hẹn.
 *
 * <p>Dữ liệu luôn được đọc lại từ cơ sở dữ liệu trong {@link #onResume()}, nên khi quay về
 * từ màn hình thêm hoặc màn hình chi tiết, danh sách và khối thống kê đều được cập nhật.</p>
 */
public class MainActivity extends AppCompatActivity
        implements AppointmentAdapter.OnAppointmentClickListener {

    /** Khóa truyền id lịch hẹn sang màn hình chi tiết. */
    public static final String EXTRA_APPOINTMENT_ID = "extra_appointment_id";

    private DatabaseHelper dbHelper;
    private AppointmentAdapter adapter;
    private TextView tvStatTotal;
    private TextView tvStatReminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Nền phía sau thanh trạng thái là màu teal đậm nên icon thanh trạng thái phải màu trắng.
        UiUtils.applyEdgeToEdge(this, false);
        applyWindowInsets();

        dbHelper = new DatabaseHelper(this);

        tvStatTotal = findViewById(R.id.tvStatTotal);
        tvStatReminders = findViewById(R.id.tvStatReminders);

        adapter = new AppointmentAdapter(this, this);
        ListView listView = findViewById(R.id.listAppointments);
        // Dòng gợi ý nằm ngay sau lịch hẹn cuối cùng và cuộn theo danh sách.
        View footer = getLayoutInflater().inflate(R.layout.list_footer_hint, listView, false);
        listView.addFooterView(footer, null, false);
        listView.setAdapter(adapter);
        // Khi danh sách rỗng, ListView tự ẩn đi và hiện khối thông báo này.
        listView.setEmptyView(findViewById(R.id.emptyState));

        MaterialButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> openAddScreen());
    }

    /**
     * Chừa chỗ cho thanh trạng thái ở phần đầu và thanh điều hướng ở nút bấm dưới cùng,
     * để nội dung không bị che khi ứng dụng vẽ tràn viền.
     */
    private void applyWindowInsets() {
        View headerContent = findViewById(R.id.headerContent);
        View btnAdd = findViewById(R.id.btnAdd);
        final int headerTopPadding = headerContent.getPaddingTop();
        final int btnAddBottomMargin = ((ConstraintLayout.LayoutParams)
                btnAdd.getLayoutParams()).bottomMargin;

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootMain), (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            headerContent.setPadding(headerContent.getPaddingLeft(), headerTopPadding + bars.top,
                    headerContent.getPaddingRight(), headerContent.getPaddingBottom());

            ConstraintLayout.LayoutParams params =
                    (ConstraintLayout.LayoutParams) btnAdd.getLayoutParams();
            params.bottomMargin = btnAddBottomMargin + bars.bottom;
            btnAdd.setLayoutParams(params);

            return windowInsets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại danh sách và thống kê sau khi thêm, xóa hoặc đổi trạng thái nhắc nhở.
        loadAppointments();
    }

    /** Đọc lại toàn bộ lịch hẹn từ cơ sở dữ liệu và cập nhật giao diện. */
    private void loadAppointments() {
        List<Appointment> appointments = dbHelper.getAllAppointments();
        adapter.setItems(appointments);

        tvStatTotal.setText(String.valueOf(dbHelper.countAll()));
        tvStatReminders.setText(String.valueOf(dbHelper.countReminders()));
    }

    @Override
    public void onAppointmentClick(Appointment appointment) {
        // Chỉ truyền id sang màn hình chi tiết; màn hình đó truy vấn lại cơ sở dữ liệu.
        Intent intent = new Intent(MainActivity.this, AppointmentDetailActivity.class);
        intent.putExtra(EXTRA_APPOINTMENT_ID, appointment.getId());
        startActivity(intent);
    }

    private void openAddScreen() {
        // Mở màn hình thêm lịch hẹn; khi nó finish() thì onResume() sẽ tải lại dữ liệu.
        startActivity(new Intent(MainActivity.this, AddAppointmentActivity.class));
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
