package hcmute.edu.vn.lequanghung_23110110.ticktick.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.CalendarAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.OnTimeSelectedListener;

public class DatePickerBottomSheet extends BottomSheetDialogFragment {
    private int selectedHour = -1;
    private int selectedMinute = -1;
    private OnTimeSelectedListener timeListener;

    public void setOnTimeSelectedListener(OnTimeSelectedListener listener) {
        this.timeListener = listener;
    }

    public interface OnDateSelectedListener {
        void onDateSelected(String dateTag, long dateMillis);
    }

    public interface OnDateClearedListener {
        void onDateCleared();
    }

    private OnDateSelectedListener dateListener;
    private OnDateClearedListener clearListener;

    private Calendar displayCalendar; // Tháng đang hiển thị
    private Calendar selectedDate; // Ngày được chọn (null = chưa chọn)

    private CalendarAdapter calendarAdapter;
    private TextView monthTitle;

    // Cho phép pre-select ngày (từ quick buttons trước đó)
    private long preSelectedDateMillis = -1;
    private int preSelectedHour = -1;
    private int preSelectedMinute = -1;

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.dateListener = listener;
    }

    public void setOnDateClearedListener(OnDateClearedListener listener) {
        this.clearListener = listener;
    }

    public void setPreSelectedDate(long millis) {
        this.preSelectedDateMillis = millis;
    }

    public void setPreSelectedTime(int hour, int minute) {
        this.preSelectedHour = hour;
        this.preSelectedMinute = minute;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_date_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayCalendar = Calendar.getInstance();
        selectedDate = null;
        selectedHour = preSelectedHour;
        selectedMinute = preSelectedMinute;

        // Nếu có pre-selected date
        if (preSelectedDateMillis > 0) {
            selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(preSelectedDateMillis);
            displayCalendar.setTimeInMillis(preSelectedDateMillis);
        }

        monthTitle = view.findViewById(R.id.calendar_month_title);

        // ── Header buttons ──
        view.findViewById(R.id.date_btn_close).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.date_btn_confirm).setOnClickListener(v -> {
            if (selectedDate != null && dateListener != null) {
                // Kiểm tra ngày quá khứ
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                Calendar selDay = (Calendar) selectedDate.clone();
                selDay.set(Calendar.HOUR_OF_DAY, 0);
                selDay.set(Calendar.MINUTE, 0);
                selDay.set(Calendar.SECOND, 0);
                selDay.set(Calendar.MILLISECOND, 0);

                if (selDay.before(today)) {
                    Toast.makeText(getContext(),
                            R.string.date_past_warning, Toast.LENGTH_SHORT).show();
                    return; // Không dismiss, cho chọn lại
                }

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String dateTag = sdf.format(selectedDate.getTime());

                // Nếu có giờ, thêm vào tag
                if (selectedHour >= 0) {
                    dateTag += String.format(Locale.getDefault(), " %02d:%02d", selectedHour, selectedMinute);
                }

                dateListener.onDateSelected(dateTag, selectedDate.getTimeInMillis());
            }
            dismiss();
        });

        // ── Tabs ──
        TextView tabDate = view.findViewById(R.id.tab_date);
        TextView tabDuration = view.findViewById(R.id.tab_duration);
        tabDate.setOnClickListener(v -> {
            tabDate.setTextColor(requireContext().getColor(R.color.main_accent_blue));
            tabDuration.setTextColor(requireContext().getColor(R.color.main_text_secondary));
        });
        tabDuration.setOnClickListener(v -> {
            tabDuration.setTextColor(requireContext().getColor(R.color.main_accent_blue));
            tabDate.setTextColor(requireContext().getColor(R.color.main_text_secondary));
            Toast.makeText(getContext(), "Thời lượng (chưa triển khai)", Toast.LENGTH_SHORT).show();
        });

        // ── Quick buttons (ẩn khi đã chọn ngày, hiện khi chưa chọn) ──
        View quickContainer = view.findViewById(R.id.quick_buttons_scroll);
        if (quickContainer != null) {
            quickContainer.setVisibility(preSelectedDateMillis > 0 ? View.GONE : View.VISIBLE);
        }

        setupQuickButtons(view);

        // ── Day-of-week headers ──
        GridView headerGrid = view.findViewById(R.id.calendar_day_headers);
        String[] dayHeaders = { "Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7", "CN" };
        headerGrid.setAdapter(new ArrayAdapter<>(requireContext(),
                R.layout.item_calendar_day_header, R.id.header_text, dayHeaders));

        // ── Calendar grid ──
        GridView calendarGrid = view.findViewById(R.id.calendar_grid);
        buildCalendar(calendarGrid);

        // ── Month navigation ──
        view.findViewById(R.id.calendar_btn_prev).setOnClickListener(v -> {
            displayCalendar.add(Calendar.MONTH, -1);
            buildCalendar(calendarGrid);
        });
        view.findViewById(R.id.calendar_btn_next).setOnClickListener(v -> {
            displayCalendar.add(Calendar.MONTH, 1);
            buildCalendar(calendarGrid);
        });

        // ── Grid click → select day ──
        calendarGrid.setOnItemClickListener((parent, v, position, id) -> {
            int day = (int) calendarAdapter.getItem(position);
            if (day == 0)
                return;

            selectedDate = (Calendar) displayCalendar.clone();
            selectedDate.set(Calendar.DAY_OF_MONTH, day);
            calendarAdapter.setSelectedDay(day);
        });

        // ── Options ──
        TextView textTimeValue = view.findViewById(R.id.text_time_value);
        if (selectedHour >= 0) {
            textTimeValue.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
            textTimeValue.setTextColor(requireContext().getColor(R.color.main_accent_blue));
        }
        view.findViewById(R.id.option_time).setOnClickListener(v -> showTimePicker(textTimeValue));
        TextView textReminderValue = view.findViewById(R.id.text_reminder_value);
        view.findViewById(R.id.option_reminder).setOnClickListener(v -> {
            ReminderDialogFragment reminderDialog = new ReminderDialogFragment();

            // Truyền time mặc định (nếu đã chọn giờ)
            if (selectedHour >= 0) {
                reminderDialog.setDefaultTime(
                        String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
            }

            // Truyền ngày task (nếu đã chọn)
            if (selectedDate != null) {
                reminderDialog.setTaskDateMillis(selectedDate.getTimeInMillis());
            }

            reminderDialog.setOnReminderSelectedListener((reminders, continuous) -> {
                if (reminders.isEmpty()) {
                    textReminderValue.setText("Không có");
                    textReminderValue.setTextColor(requireContext().getColor(R.color.main_text_secondary));
                } else {
                    textReminderValue.setText(reminders.size() + " lời nhắc");
                    textReminderValue.setTextColor(requireContext().getColor(R.color.main_accent_blue));
                }
            });

            reminderDialog.show(getChildFragmentManager(), "reminder_dialog");
        });
        view.findViewById(R.id.option_repeat)
                .setOnClickListener(v -> Toast.makeText(getContext(), "Đặt lặp lại", Toast.LENGTH_SHORT).show());

        // ── NÚT XÓA ──
        view.findViewById(R.id.btn_clear_date).setOnClickListener(v -> {
            selectedDate = null;
            if (clearListener != null) {
                clearListener.onDateCleared();
            }
            dismiss();
        });
    }

    private void showTimePicker(TextView textTimeValue) {
        int hour = selectedHour >= 0 ? selectedHour : Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = selectedMinute >= 0 ? selectedMinute : Calendar.getInstance().get(Calendar.MINUTE);

        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("Thời gian")
                .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            selectedHour = timePicker.getHour();
            selectedMinute = timePicker.getMinute();

            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
            textTimeValue.setText(timeStr);
            textTimeValue.setTextColor(requireContext().getColor(R.color.main_accent_blue));

            if (timeListener != null) {
                timeListener.onTimeSelected(selectedHour, selectedMinute);
            }
        });

        timePicker.show(getChildFragmentManager(), "time_picker");
    }

    private void setupQuickButtons(View view) {
        View quickToday = view.findViewById(R.id.quick_today);
        View quickTomorrow = view.findViewById(R.id.quick_tomorrow);
        View quickNextMonday = view.findViewById(R.id.quick_next_monday);
        View quickEndDay = view.findViewById(R.id.quick_end_of_day);

        if (quickToday != null) {
            quickToday.setOnClickListener(v -> selectQuickAndClose("Hôm nay", 0));
        }
        if (quickTomorrow != null) {
            quickTomorrow.setOnClickListener(v -> selectQuickAndClose("Ngày mai", 1));
        }
        if (quickNextMonday != null) {
            quickNextMonday.setOnClickListener(v -> {
                Calendar cal = Calendar.getInstance();
                int dow = cal.get(Calendar.DAY_OF_WEEK);
                int daysUntilMonday = (Calendar.MONDAY - dow + 7) % 7;
                if (daysUntilMonday == 0)
                    daysUntilMonday = 7;
                selectQuickAndClose("Thứ Hai tới", daysUntilMonday);
            });
        }
        if (quickEndDay != null) {
            quickEndDay.setOnClickListener(v -> selectQuickAndClose("Đến cuối ngày", 0));
        }
    }

    private void selectQuickAndClose(String tag, int daysFromNow) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, daysFromNow);
        if (dateListener != null) {
            dateListener.onDateSelected(tag, cal.getTimeInMillis());
        }
        dismiss();
    }

    /**
     * Xây dựng lưới calendar cho tháng hiện tại.
     * Tính offset ngày đầu tiên để căn vào đúng cột thứ Hai.
     */
    private void buildCalendar(GridView gridView) {
        int year = displayCalendar.get(Calendar.YEAR);
        int month = displayCalendar.get(Calendar.MONTH);

        // Update tiêu đề tháng
        String[] monthNames = { "tháng 1", "tháng 2", "tháng 3", "tháng 4",
                "tháng 5", "tháng 6", "tháng 7", "tháng 8",
                "tháng 9", "tháng 10", "tháng 11", "tháng 12" };
        monthTitle.setText(monthNames[month]);

        // Tính ngày đầu tiên của tháng
        Calendar firstDay = Calendar.getInstance();
        firstDay.set(year, month, 1);
        int dayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);

        // Convert sang offset Monday-first (Th2=0, Th3=1, ..., CN=6)
        int offset = (dayOfWeek - Calendar.MONDAY + 7) % 7;

        int maxDay = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<Integer> days = new ArrayList<>();

        // Padding đầu tháng
        for (int i = 0; i < offset; i++) {
            days.add(0);
        }
        // Ngày trong tháng
        for (int d = 1; d <= maxDay; d++) {
            days.add(d);
        }
        // Padding cuối (lấp đầy 6 hàng = 42 ô)
        while (days.size() < 42) {
            days.add(0);
        }

        calendarAdapter = new CalendarAdapter(requireContext(), days, month, year);

        // Nếu có ngày đã chọn thuộc tháng này → highlight
        if (selectedDate != null
                && selectedDate.get(Calendar.MONTH) == month
                && selectedDate.get(Calendar.YEAR) == year) {
            calendarAdapter.setSelectedDay(selectedDate.get(Calendar.DAY_OF_MONTH));
        }

        gridView.setAdapter(calendarAdapter);
    }
}