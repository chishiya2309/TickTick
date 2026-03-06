# 🔔 Reminder Dialog — Lời nhắc cho Task

> Khi nhấn "Lời nhắc" trong Date Picker, hiển thị dialog chọn nhắc nhở:
>
> - Danh sách preset (multi-select): Không có, Đúng ngày, 1/2/3 days early, 1 week early
> - Thời gian mặc định `09:00` (hoặc giờ đã chọn ở option_time)
> - "Tùy chỉnh" → mở spinner picker (Trước ngày / Trước tuần)
> - Toggle "Nhắc nhở liên tục"

---

## 📋 Mục lục

1. [Layout dialog chính](#1-layout-dialog-chính)
2. [Layout Tùy chỉnh (Custom Spinner)](#2-layout-tùy-chỉnh-custom-spinner)
3. [ReminderDialogFragment.java](#3-reminderdialogfragmentjava)
4. [Gắn vào DatePickerBottomSheet](#4-gắn-vào-datepickerbottomsheet)
5. [Drawable & String resources](#5-drawable--string-resources)
6. [Checklist](#6-checklist)

---

## 1. Layout dialog chính

📁 `app/src/main/res/layout/dialog_reminder.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="#2D2D2D"
    android:padding="20dp">

    <!-- Title -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Lời nhắc"
        android:textColor="#E0E0E0"
        android:textSize="18sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp" />

    <!-- ═══ PRESET OPTIONS (multi-select) ═══ -->

    <!-- Không có -->
    <LinearLayout
        android:id="@+id/reminder_none"
        android:layout_width="match_parent"
        android:layout_height="44dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:clickable="true"
        android:focusable="true">
        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Không có"
            android:textColor="#4C6FE0"
            android:textSize="15sp" />
        <ImageView
            android:id="@+id/check_none"
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_check_blue"
            android:visibility="visible" />
    </LinearLayout>

    <!-- Đúng ngày -->
    <LinearLayout
        android:id="@+id/reminder_on_day"
        android:layout_width="match_parent"
        android:layout_height="44dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:clickable="true"
        android:focusable="true">
        <TextView
            android:id="@+id/text_on_day"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Đúng ngày (09:00)"
            android:textColor="#B0B0B0"
            android:textSize="15sp" />
        <ImageView
            android:id="@+id/check_on_day"
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_check_blue"
            android:visibility="gone" />
    </LinearLayout>

    <!-- 1 days early -->
    <LinearLayout
        android:id="@+id/reminder_1_day"
        android:layout_width="match_parent"
        android:layout_height="44dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:clickable="true"
        android:focusable="true">
        <TextView
            android:id="@+id/text_1_day"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="1 days early (09:00)"
            android:textColor="#B0B0B0"
            android:textSize="15sp" />
        <ImageView
            android:id="@+id/check_1_day"
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_check_blue"
            android:visibility="gone" />
    </LinearLayout>

    <!-- 2 days early -->
    <LinearLayout
        android:id="@+id/reminder_2_days"
        android:layout_width="match_parent"
        android:layout_height="44dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:clickable="true"
        android:focusable="true">
        <TextView
            android:id="@+id/text_2_days"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="2 days early (09:00)"
            android:textColor="#B0B0B0"
            android:textSize="15sp" />
        <ImageView
            android:id="@+id/check_2_days"
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_check_blue"
            android:visibility="gone" />
    </LinearLayout>

    <!-- 3 days early -->
    <LinearLayout
        android:id="@+id/reminder_3_days"
        android:layout_width="match_parent"
        android:layout_height="44dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:clickable="true"
        android:focusable="true">
        <TextView
            android:id="@+id/text_3_days"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="3 days early (09:00)"
            android:textColor="#B0B0B0"
            android:textSize="15sp" />
        <ImageView
            android:id="@+id/check_3_days"
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_check_blue"
            android:visibility="gone" />
    </LinearLayout>

    <!-- 1 weeks early -->
    <LinearLayout
        android:id="@+id/reminder_1_week"
        android:layout_width="match_parent"
        android:layout_height="44dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:clickable="true"
        android:focusable="true">
        <TextView
            android:id="@+id/text_1_week"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="1 weeks early (09:00)"
            android:textColor="#B0B0B0"
            android:textSize="15sp" />
        <ImageView
            android:id="@+id/check_1_week"
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_check_blue"
            android:visibility="gone" />
    </LinearLayout>

    <!-- Divider -->
    <View
        android:layout_width="match_parent"
        android:layout_height="0.5dp"
        android:background="#444444"
        android:layout_marginTop="4dp"
        android:layout_marginBottom="4dp" />

    <!-- Tùy chỉnh → mở custom spinner -->
    <LinearLayout
        android:id="@+id/reminder_custom"
        android:layout_width="match_parent"
        android:layout_height="44dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:clickable="true"
        android:focusable="true">
        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Tùy chỉnh"
            android:textColor="#B0B0B0"
            android:textSize="15sp" />
        <ImageView
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:src="@drawable/ic_chevron_right"
            android:scaleType="centerInside" />
    </LinearLayout>

    <!-- Nhắc nhở liên tục toggle -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="44dp"
        android:orientation="horizontal"
        android:gravity="center_vertical">
        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Nhắc nhở liên tục🔔"
            android:textColor="#B0B0B0"
            android:textSize="15sp" />
        <Switch
            android:id="@+id/switch_continuous"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />
    </LinearLayout>

    <!-- Buttons -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="end"
        android:layout_marginTop="8dp">

        <TextView
            android:id="@+id/btn_reminder_cancel"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Hủy bỏ"
            android:textColor="#B0B0B0"
            android:textSize="14sp"
            android:padding="8dp"
            android:clickable="true"
            android:focusable="true" />

        <TextView
            android:id="@+id/btn_reminder_ok"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="OK"
            android:textColor="#4C6FE0"
            android:textSize="14sp"
            android:textStyle="bold"
            android:padding="8dp"
            android:layout_marginStart="12dp"
            android:clickable="true"
            android:focusable="true" />
    </LinearLayout>

</LinearLayout>
```

---

## 2. Layout Tùy chỉnh (Custom Spinner)

📁 `app/src/main/res/layout/dialog_reminder_custom.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="#2D2D2D"
    android:padding="16dp">

    <!-- Title -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Lời nhắc"
        android:textColor="#B0B0B0"
        android:textSize="13sp"
        android:layout_marginBottom="12dp" />

    <!-- Tabs: Trước ngày / Trước tuần -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="36dp"
        android:orientation="horizontal"
        android:gravity="center"
        android:layout_marginBottom="12dp">

        <TextView
            android:id="@+id/tab_before_day"
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:gravity="center"
            android:text="Trước ngày"
            android:textColor="#4C6FE0"
            android:textSize="14sp"
            android:textStyle="bold"
            android:paddingStart="12dp"
            android:paddingEnd="12dp"
            android:background="@drawable/bg_tab_selected"
            android:clickable="true"
            android:focusable="true" />

        <TextView
            android:id="@+id/tab_before_week"
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:gravity="center"
            android:text="Trước tuần"
            android:textColor="#B0B0B0"
            android:textSize="14sp"
            android:paddingStart="12dp"
            android:paddingEnd="12dp"
            android:clickable="true"
            android:focusable="true" />
    </LinearLayout>

    <!-- NumberPicker Row: [Label] [Hour] : [Minute] -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="160dp"
        android:orientation="horizontal"
        android:gravity="center">

        <!-- Label picker (Đúng ngày, 1 days early, ...) -->
        <NumberPicker
            android:id="@+id/picker_label"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="2" />

        <!-- Hour picker -->
        <NumberPicker
            android:id="@+id/picker_hour"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text=":"
            android:textColor="#E0E0E0"
            android:textSize="18sp"
            android:layout_gravity="center" />

        <!-- Minute picker -->
        <NumberPicker
            android:id="@+id/picker_minute"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1" />
    </LinearLayout>

    <!-- Info text -->
    <TextView
        android:id="@+id/text_reminder_info"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Nhắc nhở lúc 09:00 trong 12 thg 3, 2026"
        android:textColor="#B0B0B0"
        android:textSize="12sp"
        android:gravity="center"
        android:layout_marginTop="8dp" />

    <!-- Buttons -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center"
        android:layout_marginTop="12dp">

        <TextView
            android:id="@+id/btn_custom_cancel"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Hủy bỏ"
            android:textColor="#B0B0B0"
            android:textSize="14sp"
            android:padding="8dp"
            android:clickable="true"
            android:focusable="true" />

        <TextView
            android:id="@+id/btn_custom_done"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="ĐÃ XONG"
            android:textColor="#4C6FE0"
            android:textSize="14sp"
            android:textStyle="bold"
            android:padding="8dp"
            android:layout_marginStart="24dp"
            android:clickable="true"
            android:focusable="true" />
    </LinearLayout>

</LinearLayout>
```

---

## 3. ReminderDialogFragment.java

📁 `app/src/main/java/.../dialog/ReminderDialogFragment.java`

```java
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
        void onReminderSelected(List<String> selectedReminders, boolean continuous);
    }

    private OnReminderSelectedListener listener;
    private String defaultTime = "09:00";
    private long taskDateMillis = -1;

    // State
    private final List<String> selectedItems = new ArrayList<>();
    private boolean isContinuous = false;

    public void setOnReminderSelectedListener(OnReminderSelectedListener l) {
        this.listener = l;
    }

    public void setDefaultTime(String time) {
        this.defaultTime = time;
    }

    public void setTaskDateMillis(long millis) {
        this.taskDateMillis = millis;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_reminder, null);

        setupPresetOptions(view);
        setupCustomButton(view);

        Switch switchContinuous = view.findViewById(R.id.switch_continuous);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        view.findViewById(R.id.btn_reminder_cancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_reminder_ok).setOnClickListener(v -> {
            isContinuous = switchContinuous.isChecked();
            if (listener != null) {
                listener.onReminderSelected(new ArrayList<>(selectedItems), isContinuous);
            }
            dismiss();
        });

        return dialog;
    }

    private void setupPresetOptions(View view) {
        // Option data: [viewId, checkId, textId, key]
        int[][] options = {
                {R.id.reminder_on_day, R.id.check_on_day, R.id.text_on_day},
                {R.id.reminder_1_day, R.id.check_1_day, R.id.text_1_day},
                {R.id.reminder_2_days, R.id.check_2_days, R.id.text_2_days},
                {R.id.reminder_3_days, R.id.check_3_days, R.id.text_3_days},
                {R.id.reminder_1_week, R.id.check_1_week, R.id.text_1_week},
        };
        String[] keys = {"on_day", "1_day", "2_days", "3_days", "1_week"};
        String[] labels = {
                "Đúng ngày (" + defaultTime + ")",
                "1 days early (" + defaultTime + ")",
                "2 days early (" + defaultTime + ")",
                "3 days early (" + defaultTime + ")",
                "1 weeks early (" + defaultTime + ")",
        };

        ImageView checkNone = view.findViewById(R.id.check_none);

        // Set labels
        for (int i = 0; i < options.length; i++) {
            ((TextView) view.findViewById(options[i][2])).setText(labels[i]);
        }

        // "Không có" click → clear all
        view.findViewById(R.id.reminder_none).setOnClickListener(v -> {
            selectedItems.clear();
            checkNone.setVisibility(View.VISIBLE);
            for (int[] opt : options) {
                view.findViewById(opt[1]).setVisibility(View.GONE);
            }
        });

        // Preset clicks → multi-select toggle
        for (int i = 0; i < options.length; i++) {
            final String key = keys[i];
            final ImageView check = view.findViewById(options[i][1]);

            view.findViewById(options[i][0]).setOnClickListener(v -> {
                if (selectedItems.contains(key)) {
                    selectedItems.remove(key);
                    check.setVisibility(View.GONE);
                } else {
                    selectedItems.add(key);
                    check.setVisibility(View.VISIBLE);
                    checkNone.setVisibility(View.GONE);
                }
            });
        }
    }

    private void setupCustomButton(View view) {
        view.findViewById(R.id.reminder_custom).setOnClickListener(v -> {
            showCustomSpinnerDialog();
        });
    }

    /**
     * Tùy chỉnh — spinner picker với tabs "Trước ngày" / "Trước tuần"
     */
    private void showCustomSpinnerDialog() {
        View customView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_reminder_custom, null);

        // Label options
        String[] dayLabels = {"Đúng ngày", "1 days early", "2 days early", "3 days early"};
        String[] weekLabels = {"Đúng ngày", "1 weeks early", "2 weeks early", "3 weeks early"};

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
        final boolean[] isDayTab = {true};

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

        customView.findViewById(R.id.btn_custom_cancel).setOnClickListener(v ->
                customDialog.dismiss());

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
```

---

## 4. Gắn vào DatePickerBottomSheet

📁 `DatePickerBottomSheet.java` — sửa `option_reminder` click handler:

Tìm:

```java
view.findViewById(R.id.option_reminder)
        .setOnClickListener(v -> Toast.makeText(getContext(), "Đặt lời nhắc", Toast.LENGTH_SHORT).show());
```

Thay bằng:

```java
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
```

Cần thêm import:

```java
import hcmute.edu.vn.lequanghung_23110110.ticktick.dialog.ReminderDialogFragment;
```

> Import này không cần vì cùng package `dialog`.

---

## 5. Drawable & String resources

### bg_tab_selected.xml

📁 `app/src/main/res/drawable/bg_tab_selected.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@android:color/transparent" />
    <stroke
        android:width="1dp"
        android:color="#4C6FE0" />
    <corners android:radius="16dp" />
</shape>
```

### ic_check_blue.xml

📁 `app/src/main/res/drawable/ic_check_blue.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="20dp"
    android:height="20dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#4C6FE0"
        android:pathData="M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z" />
</vector>
```

---

## 6. Checklist

| #   | File                          | Loại       | Mô tả                                            |
| --- | ----------------------------- | ---------- | ------------------------------------------------ |
| 1   | `dialog_reminder.xml`         | **NEW**    | Layout dialog chính (presets + toggle)           |
| 2   | `dialog_reminder_custom.xml`  | **NEW**    | Layout custom spinner (Trước ngày/tuần)          |
| 3   | `ReminderDialogFragment.java` | **NEW**    | Logic multi-select, custom picker, expired check |
| 4   | `bg_tab_selected.xml`         | **NEW**    | Drawable viền tab selected                       |
| 5   | `ic_check_blue.xml`           | **NEW**    | Check icon xanh                                  |
| 6   | `DatePickerBottomSheet.java`  | **MODIFY** | Thay Toast bằng ReminderDialogFragment           |

> 💡 **Flow**:
>
> 1. Nhấn "Lời nhắc" → Dialog hiện, mặc định "Không có" ✓
> 2. Nhấn "Đúng ngày" → check xanh, "Không có" bỏ check (multi-select)
> 3. Nhấn "Tùy chỉnh" → Spinner picker (Trước ngày/tuần) + giờ:phút
> 4. Nếu ngày nhắc đã qua → text đỏ "Nhắc nhở đã hết hạn"
> 5. OK → cập nhật text "Lời nhắc" thành "X lời nhắc" (xanh)
