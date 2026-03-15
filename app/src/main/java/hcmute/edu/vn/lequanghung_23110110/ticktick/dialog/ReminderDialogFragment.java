package hcmute.edu.vn.lequanghung_23110110.ticktick.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;

public class ReminderDialogFragment extends DialogFragment {

    public interface OnReminderSelectedListener {
        void onReminderSelected(List<String> selectedReminders);
    }

    private OnReminderSelectedListener listener;
    private String defaultTime = "09:00";
    private long taskDateMillis = -1;
    private boolean hasTimeSelected = false;
    private List<String> preSelectedItems = new ArrayList<>();

    // State
    private final List<String> selectedItems = new ArrayList<>();

    public void setOnReminderSelectedListener(OnReminderSelectedListener l) {
        this.listener = l;
    }

    public void setDefaultTime(String time) {
        this.defaultTime = time;
    }

    public void setTaskDateMillis(long millis) {
        this.taskDateMillis = millis;
    }

    public void setHasTimeSelected(boolean hasTime) {
        this.hasTimeSelected = hasTime;
    }

    public void setPreSelectedItems(List<String> items) {
        this.preSelectedItems = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_reminder, null);

        // Restore state từ lần chọn trước
        selectedItems.clear();
        selectedItems.addAll(preSelectedItems);

        if (hasTimeSelected) {
            // Ẩn day-based presets
            view.findViewById(R.id.reminder_on_day).setVisibility(View.GONE);
            view.findViewById(R.id.reminder_1_day).setVisibility(View.GONE);
            view.findViewById(R.id.reminder_2_days).setVisibility(View.GONE);
            view.findViewById(R.id.reminder_3_days).setVisibility(View.GONE);
            view.findViewById(R.id.reminder_1_week).setVisibility(View.GONE);
            // Hiện time-based presets
            view.findViewById(R.id.reminder_on_time).setVisibility(View.VISIBLE);
            view.findViewById(R.id.reminder_5_mins).setVisibility(View.VISIBLE);
            view.findViewById(R.id.reminder_30_mins).setVisibility(View.VISIBLE);
            view.findViewById(R.id.reminder_1_hour).setVisibility(View.VISIBLE);
            view.findViewById(R.id.reminder_1_day_time).setVisibility(View.VISIBLE);
            setupTimeBasedOptions(view);
        } else {
            setupPresetOptions(view);
        }
        setupCustomButton(view);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        view.findViewById(R.id.btn_reminder_cancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_reminder_ok).setOnClickListener(v -> {
            if (listener != null) {
                listener.onReminderSelected(new ArrayList<>(selectedItems));
            }
            dismiss();
        });

        return dialog;
    }

    private void setupPresetOptions(View view) {
        // Option data: [viewId, checkId, textId, key]
        int[][] options = {
                { R.id.reminder_on_day, R.id.check_on_day, R.id.text_on_day },
                { R.id.reminder_1_day, R.id.check_1_day, R.id.text_1_day },
                { R.id.reminder_2_days, R.id.check_2_days, R.id.text_2_days },
                { R.id.reminder_3_days, R.id.check_3_days, R.id.text_3_days },
                { R.id.reminder_1_week, R.id.check_1_week, R.id.text_1_week },
        };
        String[] keys = { "on_day", "1_day", "2_days", "3_days", "1_week" };
        String[] labels = {
                "Đúng ngày (" + defaultTime + ")",
                "1 days early (" + defaultTime + ")",
                "2 days early (" + defaultTime + ")",
                "3 days early (" + defaultTime + ")",
                "1 weeks early (" + defaultTime + ")",
        };

        int colorSelected = 0xFF4C6FE0;
        int colorDefault = 0xFFB0B0B0;

        ImageView checkNone = view.findViewById(R.id.check_none);
        TextView textNone = view.findViewById(R.id.text_none);

        // Set labels
        for (int i = 0; i < options.length; i++) {
            ((TextView) view.findViewById(options[i][2])).setText(labels[i]);
        }

        // Restore pre-selected state
        if (!selectedItems.isEmpty()) {
            checkNone.setVisibility(View.GONE);
            textNone.setTextColor(colorDefault);
            for (int i = 0; i < keys.length; i++) {
                if (selectedItems.contains(keys[i])) {
                    view.findViewById(options[i][1]).setVisibility(View.VISIBLE);
                    ((TextView) view.findViewById(options[i][2])).setTextColor(colorSelected);
                }
            }
        }

        // "Không có" click → clear all
        view.findViewById(R.id.reminder_none).setOnClickListener(v -> {
            selectedItems.clear();
            checkNone.setVisibility(View.VISIBLE);
            textNone.setTextColor(colorSelected);
            for (int[] opt : options) {
                view.findViewById(opt[1]).setVisibility(View.GONE);
                ((TextView) view.findViewById(opt[2])).setTextColor(colorDefault);
            }
        });

        // Preset clicks → multi-select toggle
        for (int i = 0; i < options.length; i++) {
            final String key = keys[i];
            final ImageView check = view.findViewById(options[i][1]);
            final TextView text = (TextView) view.findViewById(options[i][2]);

            view.findViewById(options[i][0]).setOnClickListener(v -> {
                if (selectedItems.contains(key)) {
                    selectedItems.remove(key);
                    check.setVisibility(View.GONE);
                    text.setTextColor(colorDefault);
                } else {
                    selectedItems.add(key);
                    check.setVisibility(View.VISIBLE);
                    text.setTextColor(colorSelected);
                    checkNone.setVisibility(View.GONE);
                    textNone.setTextColor(colorDefault);
                }
            });
        }
    }

    private void setupCustomButton(View view) {
        view.findViewById(R.id.reminder_custom).setOnClickListener(v -> {
            showCustomSpinnerDialog();
        });
    }

    private void setupTimeBasedOptions(View view) {
        int[][] options = {
                { R.id.reminder_on_time, R.id.check_on_time, R.id.text_on_time },
                { R.id.reminder_5_mins, R.id.check_5_mins, R.id.text_5_mins },
                { R.id.reminder_30_mins, R.id.check_30_mins, R.id.text_30_mins },
                { R.id.reminder_1_hour, R.id.check_1_hour, R.id.text_1_hour },
                { R.id.reminder_1_day_time, R.id.check_1_day_time, R.id.text_1_day_time },
        };
        String[] keys = { "on_time", "5_mins", "30_mins", "1_hour", "1_day_time" };

        int colorSelected = 0xFF4C6FE0;
        int colorDefault = 0xFFB0B0B0;

        ImageView checkNone = view.findViewById(R.id.check_none);
        TextView textNone = view.findViewById(R.id.text_none);

        // Restore pre-selected state
        if (!selectedItems.isEmpty()) {
            checkNone.setVisibility(View.GONE);
            textNone.setTextColor(colorDefault);
            for (int i = 0; i < keys.length; i++) {
                if (selectedItems.contains(keys[i])) {
                    view.findViewById(options[i][1]).setVisibility(View.VISIBLE);
                    ((TextView) view.findViewById(options[i][2])).setTextColor(colorSelected);
                }
            }
        }

        // "Không có" click → clear all
        view.findViewById(R.id.reminder_none).setOnClickListener(v -> {
            selectedItems.clear();
            checkNone.setVisibility(View.VISIBLE);
            textNone.setTextColor(colorSelected);
            for (int[] opt : options) {
                view.findViewById(opt[1]).setVisibility(View.GONE);
                ((TextView) view.findViewById(opt[2])).setTextColor(colorDefault);
            }
        });

        // Preset clicks → multi-select toggle
        for (int i = 0; i < options.length; i++) {
            final String key = keys[i];
            final ImageView check = view.findViewById(options[i][1]);
            final TextView text = (TextView) view.findViewById(options[i][2]);

            view.findViewById(options[i][0]).setOnClickListener(v -> {
                if (selectedItems.contains(key)) {
                    selectedItems.remove(key);
                    check.setVisibility(View.GONE);
                    text.setTextColor(colorDefault);
                } else {
                    selectedItems.add(key);
                    check.setVisibility(View.VISIBLE);
                    text.setTextColor(colorSelected);
                    checkNone.setVisibility(View.GONE);
                    textNone.setTextColor(colorDefault);
                }
            });
        }
    }

    /**
     * Tùy chỉnh — spinner picker với tabs "Trước ngày" / "Trước tuần"
     */
    private void showCustomSpinnerDialog() {
        View customView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_reminder_custom, null);

        // Label options
        String[] dayLabels = { "Đúng ngày", "1 days early", "2 days early", "3 days early" };
        String[] weekLabels = { "Đúng ngày", "1 weeks early", "2 weeks early", "3 weeks early" };

        NumberPicker pickerLabel = customView.findViewById(R.id.picker_label);
        NumberPicker pickerHour = customView.findViewById(R.id.picker_hour);
        NumberPicker pickerMinute = customView.findViewById(R.id.picker_minute);
        TextView infoText = customView.findViewById(R.id.text_reminder_info);
        TextView tabDay = customView.findViewById(R.id.tab_before_day);
        TextView tabWeek = customView.findViewById(R.id.tab_before_week);

        // Setup hour/minute pickers
        pickerHour.setMinValue(0);
        pickerHour.setMaxValue(23);
        pickerHour.setFormatter(i -> String.format(Locale.getDefault(), "%02d", i));

        pickerMinute.setMinValue(0);
        pickerMinute.setMaxValue(59);
        pickerMinute.setFormatter(i -> String.format(Locale.getDefault(), "%02d", i));

        // Parse default time
        String[] timeParts = defaultTime.split(":");
        int defHour = timeParts.length == 2 ? Integer.parseInt(timeParts[0]) : 9;
        int defMinute = timeParts.length == 2 ? Integer.parseInt(timeParts[1]) : 0;
        pickerHour.setValue(defHour);
        pickerMinute.setValue(defMinute);

        // State: which tab
        final boolean[] isDayTab = { true };

        // Setup label picker for "Trước ngày"
        Runnable setupDayLabels = () -> {
            pickerLabel.setDisplayedValues(null);
            pickerLabel.setMinValue(0);
            pickerLabel.setMaxValue(dayLabels.length - 1);
            pickerLabel.setDisplayedValues(dayLabels);
            isDayTab[0] = true;
            updateInfoText(infoText, dayLabels, pickerLabel, pickerHour, pickerMinute, true);
        };

        Runnable setupWeekLabels = () -> {
            pickerLabel.setDisplayedValues(null);
            pickerLabel.setMinValue(0);
            pickerLabel.setMaxValue(weekLabels.length - 1);
            pickerLabel.setDisplayedValues(weekLabels);
            isDayTab[0] = false;
            updateInfoText(infoText, weekLabels, pickerLabel, pickerHour, pickerMinute, false);
        };

        setupDayLabels.run();

        // Tab clicks
        tabDay.setOnClickListener(v -> {
            tabDay.setTextColor(requireContext().getColor(R.color.main_accent_blue));
            tabDay.setTextSize(14);
            tabDay.setTypeface(null, android.graphics.Typeface.BOLD);
            tabWeek.setTextColor(requireContext().getColor(R.color.main_text_secondary));
            tabWeek.setTypeface(null, android.graphics.Typeface.NORMAL);
            setupDayLabels.run();
        });

        tabWeek.setOnClickListener(v -> {
            tabWeek.setTextColor(requireContext().getColor(R.color.main_accent_blue));
            tabWeek.setTextSize(14);
            tabWeek.setTypeface(null, android.graphics.Typeface.BOLD);
            tabDay.setTextColor(requireContext().getColor(R.color.main_text_secondary));
            tabDay.setTypeface(null, android.graphics.Typeface.NORMAL);
            setupWeekLabels.run();
        });

        // Update info on value change
        NumberPicker.OnValueChangeListener valChange = (p, o, n) -> {
            String[] labels = isDayTab[0] ? dayLabels : weekLabels;
            updateInfoText(infoText, labels, pickerLabel, pickerHour, pickerMinute, isDayTab[0]);
        };
        pickerLabel.setOnValueChangedListener(valChange);
        pickerHour.setOnValueChangedListener(valChange);
        pickerMinute.setOnValueChangedListener(valChange);

        AlertDialog customDialog = new AlertDialog.Builder(requireContext())
                .setView(customView)
                .create();

        if (customDialog.getWindow() != null) {
            customDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        customView.findViewById(R.id.btn_custom_cancel).setOnClickListener(v -> customDialog.dismiss());

        customView.findViewById(R.id.btn_custom_done).setOnClickListener(v -> {
            String[] labels = isDayTab[0] ? dayLabels : weekLabels;
            String selectedLabel = labels[pickerLabel.getValue()];
            String time = String.format(Locale.getDefault(), "%02d:%02d",
                    pickerHour.getValue(), pickerMinute.getValue());
            selectedItems.clear();
            selectedItems.add("custom:" + selectedLabel + ":" + time);
            customDialog.dismiss();
        });

        customDialog.show();
    }

    private void updateInfoText(TextView infoText, String[] labels,
            NumberPicker pickerLabel, NumberPicker pickerHour,
            NumberPicker pickerMinute, boolean isDayMode) {
        String time = String.format(Locale.getDefault(), "%02d:%02d",
                pickerHour.getValue(), pickerMinute.getValue());

        if (taskDateMillis > 0) {
            int offset = pickerLabel.getValue();
            Calendar reminderDate = Calendar.getInstance();
            reminderDate.setTimeInMillis(taskDateMillis);

            if (isDayMode) {
                reminderDate.add(Calendar.DAY_OF_MONTH, -offset);
            } else {
                reminderDate.add(Calendar.WEEK_OF_YEAR, -offset);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("d 'thg' M, yyyy", Locale.getDefault());
            String dateStr = sdf.format(reminderDate.getTime());

            // Check nếu đã qua
            Calendar now = Calendar.getInstance();
            if (reminderDate.before(now)) {
                infoText.setText("Nhắc nhở đã hết hạn");
                infoText.setTextColor(0xFFEF5350); // đỏ
            } else {
                infoText.setText("Nhắc nhở lúc " + time + " trong " + dateStr);
                infoText.setTextColor(0xFFB0B0B0);
            }
        } else {
            infoText.setText("Nhắc nhở lúc " + time);
            infoText.setTextColor(0xFFB0B0B0);
        }
    }
}