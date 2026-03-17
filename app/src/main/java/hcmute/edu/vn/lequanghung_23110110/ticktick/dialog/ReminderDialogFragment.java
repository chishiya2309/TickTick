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

    // Custom Dynamic UI refs
    private View dialogView;
    private View reminderCustomDynamic;
    private TextView textCustomDynamic;
    private ImageView checkCustomDynamic;

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
        dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_reminder, null);

        // Restore state từ lần chọn trước
        selectedItems.clear();
        selectedItems.addAll(preSelectedItems);

        // Bind custom dynamic views
        reminderCustomDynamic = dialogView.findViewById(R.id.reminder_custom_dynamic);
        textCustomDynamic = dialogView.findViewById(R.id.text_custom_dynamic);
        checkCustomDynamic = dialogView.findViewById(R.id.check_custom_dynamic);

        if (hasTimeSelected) {
            // Ẩn day-based presets
            dialogView.findViewById(R.id.reminder_on_day).setVisibility(View.GONE);
            dialogView.findViewById(R.id.reminder_1_day).setVisibility(View.GONE);
            dialogView.findViewById(R.id.reminder_2_days).setVisibility(View.GONE);
            dialogView.findViewById(R.id.reminder_3_days).setVisibility(View.GONE);
            dialogView.findViewById(R.id.reminder_1_week).setVisibility(View.GONE);
            // Hiện time-based presets
            dialogView.findViewById(R.id.reminder_on_time).setVisibility(View.VISIBLE);
            dialogView.findViewById(R.id.reminder_5_mins).setVisibility(View.VISIBLE);
            dialogView.findViewById(R.id.reminder_30_mins).setVisibility(View.VISIBLE);
            dialogView.findViewById(R.id.reminder_1_hour).setVisibility(View.VISIBLE);
            dialogView.findViewById(R.id.reminder_1_day_time).setVisibility(View.VISIBLE);
            setupTimeBasedOptions(dialogView);
        } else {
            setupPresetOptions(dialogView);
        }
        setupCustomDynamicOption(dialogView);
        setupCustomButton(dialogView);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btn_reminder_cancel).setOnClickListener(v -> dismiss());
        dialogView.findViewById(R.id.btn_reminder_ok).setOnClickListener(v -> {
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
                "Sớm 1 ngày (" + defaultTime + ")",
                "Sớm 2 ngày (" + defaultTime + ")",
                "Sớm 3 ngày (" + defaultTime + ")",
                "Sớm 1 tuần (" + defaultTime + ")",
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
            uncheckCustomDynamic();
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
                    if (!isReminderTimeValid(key)) return;
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
            uncheckCustomDynamic();
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
                    if (!isReminderTimeValid(key)) return;
                    selectedItems.add(key);
                    check.setVisibility(View.VISIBLE);
                    text.setTextColor(colorSelected);
                    checkNone.setVisibility(View.GONE);
                    textNone.setTextColor(colorDefault);
                }
            });
        }
    }

    private void setupCustomDynamicOption(View view) {
        // Tìm xem có tùy chỉnh nào đang được chọn không
        String customKey = null;
        for (String item : selectedItems) {
            if (item.startsWith("custom:")) {
                customKey = item;
                break;
            }
        }

        if (customKey != null) {
            String[] parts = customKey.split(":", 3);
            if (parts.length >= 3) {
                // Label format trong custom picker: "Đúng ngày", "1 days early", ...
                String timeLabel = parts[2]; // time
                // Replace "early" => "sớm" (nếu có)
                String displayLabel = parts[1].replace("early", "sớm");

                String displayText = displayLabel;
                if (!displayLabel.equalsIgnoreCase("Đúng ngày")) {
                    displayText = displayLabel + " lúc " + timeLabel;
                } else {
                    displayText = "Đúng ngày lúc " + timeLabel;
                }

                reminderCustomDynamic.setVisibility(View.VISIBLE);
                textCustomDynamic.setText(displayText);
                textCustomDynamic.setTextColor(0xFF4C6FE0);
                checkCustomDynamic.setVisibility(View.VISIBLE);
            }
        } else {
            reminderCustomDynamic.setVisibility(View.GONE);
        }

        // Bấm vào để bỏ chọn
        reminderCustomDynamic.setOnClickListener(v -> {
            String toRemove = null;
            for (String item : selectedItems) {
                if (item.startsWith("custom:")) {
                    toRemove = item;
                    break;
                }
            }
            if (toRemove != null) {
                selectedItems.remove(toRemove);
            }
            uncheckCustomDynamic();
        });
    }

    private void uncheckCustomDynamic() {
        if (textCustomDynamic != null && checkCustomDynamic != null) {
            textCustomDynamic.setTextColor(0xFFB0B0B0);
            checkCustomDynamic.setVisibility(View.GONE);
        }
    }

    private void uncheckAllPresets() {
        // "Không có" click clear toàn bộ, code giả lập thao tác click vào nút
        // reminder_none
        if (dialogView != null) {
            View btnNone = dialogView.findViewById(R.id.reminder_none);
            if (btnNone != null && btnNone.getVisibility() == View.VISIBLE) {
                btnNone.performClick();
            }
        }
    }

    private boolean isReminderTimeValid(String key) {
        if (taskDateMillis <= 0) return true;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(taskDateMillis);
        
        // Luôn đặt về 0 giây/mili để so sánh chính xác theo phút
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (!hasTimeSelected) {
            try {
                String[] parts = defaultTime.split(":");
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                cal.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
            } catch (Exception e) {}
        }

        if (key.startsWith("custom:")) {
            String[] parts = key.split(":", 3);
            if (parts.length >= 3) {
                String label = parts[1];
                String timeStr = parts[2];
                int offset = 0;
                try {
                    String numStr = label.replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) offset = Integer.parseInt(numStr);
                } catch(Exception e) {}

                if (label.contains("tuần") || label.contains("week")) {
                    cal.add(Calendar.WEEK_OF_YEAR, -offset);
                } else {
                    cal.add(Calendar.DAY_OF_MONTH, -offset);
                }

                String[] timeParts = timeStr.split(":");
                if (timeParts.length == 2) {
                    try {
                        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                        cal.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
                    } catch (Exception e) {}
                }
            }
        } else {
            switch (key) {
                case "on_day":
                case "on_time":
                    break;
                case "1_day":
                case "1_day_time":
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    break;
                case "2_days":
                    cal.add(Calendar.DAY_OF_MONTH, -2);
                    break;
                case "3_days":
                    cal.add(Calendar.DAY_OF_MONTH, -3);
                    break;
                case "1_week":
                    cal.add(Calendar.WEEK_OF_YEAR, -1);
                    break;
                case "5_mins":
                    cal.add(Calendar.MINUTE, -5);
                    break;
                case "30_mins":
                    cal.add(Calendar.MINUTE, -30);
                    break;
                case "1_hour":
                    cal.add(Calendar.HOUR_OF_DAY, -1);
                    break;
            }
        }

        Calendar now = Calendar.getInstance();
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        if (cal.before(now)) {
            android.widget.Toast.makeText(getContext(), "Thời gian nhắc nhở không hợp lệ (đã qua)", android.widget.Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Tùy chỉnh — spinner picker với tabs "Trước ngày" / "Trước tuần"
     */
    private void showCustomSpinnerDialog() {
        View customView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_reminder_custom, null);

        // Label options
        String[] dayLabels = { "Đúng ngày", "Sớm 1 ngày", "Sớm 2 ngày", "Sớm 3 ngày" };
        String[] weekLabels = { "Đúng ngày", "Sớm 1 tuần", "Sớm 2 tuần", "Sớm 3 tuần" };

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
            
            String newCustomKey = "custom:" + selectedLabel + ":" + time;
            
            if (!isReminderTimeValid(newCustomKey)) return;
            
            // Xóa custom cũ nếu có, NHƯNG giữ nguyên các lựa chọn preset khác
            List<String> toRemove = new ArrayList<>();
            for (String item : selectedItems) {
                if (item.startsWith("custom:")) {
                    toRemove.add(item);
                }
            }
            selectedItems.removeAll(toRemove);
            
            selectedItems.add(newCustomKey);

            // Bỏ chọn nút Không có (vì formClick reminder_none đã chọn lại nó)
            if (dialogView != null) {
                ImageView checkNone = dialogView.findViewById(R.id.check_none);
                TextView textNone = dialogView.findViewById(R.id.text_none);
                if (checkNone != null) checkNone.setVisibility(View.GONE);
                if (textNone != null) textNone.setTextColor(0xFFB0B0B0);
            }

            // Gọi setup lại custom dynamic option
            if (dialogView != null) {
                setupCustomDynamicOption(dialogView);
            }

            customDialog.dismiss();
        });

        customDialog.show();
    }

    private void updateInfoText(TextView infoText, String[] labels,
            NumberPicker pickerLabel, NumberPicker pickerHour,
            NumberPicker pickerMinute, boolean isDayMode) {
        
        int h = pickerHour.getValue();
        int m = pickerMinute.getValue();
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m);

        if (taskDateMillis > 0) {
            int offset = pickerLabel.getValue();
            Calendar reminderDate = Calendar.getInstance();
            reminderDate.setTimeInMillis(taskDateMillis);
            
            // Áp dụng offset ngày/tuần
            if (isDayMode) {
                reminderDate.add(Calendar.DAY_OF_MONTH, -offset);
            } else {
                reminderDate.add(Calendar.WEEK_OF_YEAR, -offset);
            }
            
            // Áp dụng giờ phút từ picker
            reminderDate.set(Calendar.HOUR_OF_DAY, h);
            reminderDate.set(Calendar.MINUTE, m);
            reminderDate.set(Calendar.SECOND, 0);
            reminderDate.set(Calendar.MILLISECOND, 0);

            SimpleDateFormat sdf = new SimpleDateFormat("d 'thg' M, yyyy", Locale.getDefault());
            String dateFormatted = sdf.format(reminderDate.getTime());

            // Check nếu đã qua (so với phút hiện tại)
            Calendar now = Calendar.getInstance();
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);

            if (reminderDate.before(now)) {
                infoText.setText("Nhắc nhở đã hết hạn");
                infoText.setTextColor(0xFFEF5350); // đỏ
            } else {
                infoText.setText("Nhắc nhở lúc " + timeStr + " trong " + dateFormatted);
                infoText.setTextColor(0xFFB0B0B0);
            }
        } else {
            infoText.setText("Nhắc nhở lúc " + timeStr);
            infoText.setTextColor(0xFFB0B0B0);
        }
    }
}