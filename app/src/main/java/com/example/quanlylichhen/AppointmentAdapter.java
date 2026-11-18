package com.example.quanlylichhen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter đổ danh sách lịch hẹn (kết quả truy vấn) vào {@code ListView}.
 *
 * <p>Dùng ViewHolder để không phải {@code findViewById} lại mỗi lần cuộn, và luôn gán lại
 * đủ cả 4 trường của một dòng — nếu bỏ sót, view được tái sử dụng sẽ hiện dữ liệu của
 * dòng khác.</p>
 */
public class AppointmentAdapter extends ArrayAdapter<Appointment> {

    /** Báo cho màn hình biết người dùng vừa chọn một lịch hẹn. */
    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    private final LayoutInflater inflater;
    private final OnAppointmentClickListener listener;
    private final List<Appointment> items = new ArrayList<>();

    public AppointmentAdapter(Context context, OnAppointmentClickListener listener) {
        super(context, R.layout.item_appointment);
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    /** Thay toàn bộ dữ liệu đang hiển thị bằng kết quả truy vấn mới. */
    public void setItems(List<Appointment> appointments) {
        items.clear();
        if (appointments != null) {
            items.addAll(appointments);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Nullable
    @Override
    public Appointment getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_appointment, parent, false);
            holder = new ViewHolder();
            holder.tvTitle = convertView.findViewById(R.id.tvTitle);
            holder.tvLocation = convertView.findViewById(R.id.tvLocation);
            holder.tvDateTime = convertView.findViewById(R.id.tvDateTime);
            holder.tvReminder = convertView.findViewById(R.id.tvReminder);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Appointment appointment = items.get(position);
        Context context = convertView.getContext();

        holder.tvTitle.setText(appointment.getTitle());
        holder.tvLocation.setText(
                context.getString(R.string.item_location_format, appointment.getLocation()));
        holder.tvDateTime.setText(
                DateTimeUtils.toDisplayDateTime(appointment.getDate(), appointment.getTime()));

        if (appointment.isReminder()) {
            holder.tvReminder.setText(R.string.label_reminder_on);
            holder.tvReminder.setBackgroundResource(R.drawable.bg_chip_on);
            holder.tvReminder.setTextColor(ContextCompat.getColor(context, R.color.amber_700));
        } else {
            holder.tvReminder.setText(R.string.label_reminder_off);
            holder.tvReminder.setBackgroundResource(R.drawable.bg_chip_off);
            holder.tvReminder.setTextColor(ContextCompat.getColor(context, R.color.ink_600));
        }

        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppointmentClick(appointment);
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView tvTitle;
        TextView tvLocation;
        TextView tvDateTime;
        TextView tvReminder;
    }
}
