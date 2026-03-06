# 📅 Date Picker Bottom Sheet — Chọn ngày cho Task

> Khi nhấn icon `ic_action_date` trong Bottom Sheet "Thêm task", hiển thị **Date Picker Bottom Sheet** với:
>
> - Tab **Ngày / Thời lượng**
> - Row nút chọn nhanh: **Hôm nay, Ngày mai, Thứ Hai tới, Đến cuối ngày**
> - **CalendarView** chọn ngày
> - 3 tùy chọn phụ: **Thời gian**, **Lời nhắc**, **Lặp lại**

---

## 📋 Mục lục

1. [Tạo Layout](#1-tạo-layout)
2. [Tạo Drawable](#2-tạo-drawable)
3. [Thêm Strings](#3-thêm-strings)
4. [Thêm Colors](#4-thêm-colors)
5. [Tạo Java — DatePickerBottomSheet](#5-tạo-java--datepickerbottomsheet)
6. [Gắn vào Add Task Bottom Sheet](#6-gắn-vào-add-task-bottom-sheet)
7. [Checklist](#7-checklist)

---

## 1. Tạo Layout

📁 `app/src/main/res/layout/layout_bottom_sheet_date_picker.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@color/main_surface">

    <!-- ═══ HEADER: X — Ngày | Thời lượng — ✓ ═══ -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingStart="4dp"
        android:paddingEnd="4dp">

        <!-- Nút đóng (X) -->
        <ImageView
            android:id="@+id/date_btn_close"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:padding="8dp"
            android:src="@drawable/ic_close"
            android:scaleType="centerInside"
            android:clickable="true"
            android:focusable="true" />

        <!-- Spacer -->
        <View
            android:layout_width="0dp"
            android:layout_height="1dp"
            android:layout_weight="1" />

        <!-- Tab: Ngày -->
        <TextView
            android:id="@+id/tab_date"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/date_tab_date"
            android:textColor="@color/main_accent_blue"
            android:textSize="15sp"
            android:textStyle="bold"
            android:paddingStart="16dp"
            android:paddingEnd="16dp"
            android:clickable="true"
            android:focusable="true" />

        <!-- Tab: Thời lượng -->
        <TextView
            android:id="@+id/tab_duration"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/date_tab_duration"
            android:textColor="@color/main_text_secondary"
            android:textSize="15sp"
            android:paddingStart="16dp"
            android:paddingEnd="16dp"
            android:clickable="true"
            android:focusable="true" />

        <!-- Spacer -->
        <View
            android:layout_width="0dp"
            android:layout_height="1dp"
            android:layout_weight="1" />

        <!-- Nút xác nhận (✓) -->
        <ImageView
            android:id="@+id/date_btn_confirm"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:padding="8dp"
            android:src="@drawable/ic_check_blue"
            android:scaleType="centerInside"
            android:clickable="true"
            android:focusable="true" />

    </LinearLayout>

    <!-- ═══ QUICK DATE BUTTONS ═══ -->
    <HorizontalScrollView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:scrollbars="none"
        android:paddingStart="12dp"
        android:paddingEnd="12dp"
        android:paddingTop="8dp"
        android:paddingBottom="12dp">

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal">

            <!-- Hôm nay -->
            <LinearLayout
                android:id="@+id/quick_today"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:gravity="center"
                android:paddingStart="16dp"
                android:paddingEnd="16dp"
                android:clickable="true"
                android:focusable="true">
                <ImageView
                    android:layout_width="32dp"
                    android:layout_height="32dp"
                    android:src="@drawable/ic_quick_today"
                    android:scaleType="centerInside" />
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/date_today"
                    android:textColor="@color/main_text_secondary"
                    android:textSize="11sp"
                    android:layout_marginTop="4dp" />
            </LinearLayout>

            <!-- Ngày mai -->
            <LinearLayout
                android:id="@+id/quick_tomorrow"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:gravity="center"
                android:paddingStart="16dp"
                android:paddingEnd="16dp"
                android:clickable="true"
                android:focusable="true">
                <ImageView
                    android:layout_width="32dp"
                    android:layout_height="32dp"
                    android:src="@drawable/ic_quick_tomorrow"
                    android:scaleType="centerInside" />
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/date_tomorrow"
                    android:textColor="@color/main_text_secondary"
                    android:textSize="11sp"
                    android:layout_marginTop="4dp" />
            </LinearLayout>

            <!-- Thứ Hai tới -->
            <LinearLayout
                android:id="@+id/quick_next_monday"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:gravity="center"
                android:paddingStart="16dp"
                android:paddingEnd="16dp"
                android:clickable="true"
                android:focusable="true">
                <ImageView
                    android:layout_width="32dp"
                    android:layout_height="32dp"
                    android:src="@drawable/ic_quick_next_week"
                    android:scaleType="centerInside" />
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/date_next_monday"
                    android:textColor="@color/main_text_secondary"
                    android:textSize="11sp"
                    android:layout_marginTop="4dp" />
            </LinearLayout>

            <!-- Đến cuối ngày -->
            <LinearLayout
                android:id="@+id/quick_end_of_day"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:gravity="center"
                android:paddingStart="16dp"
                android:paddingEnd="16dp"
                android:clickable="true"
                android:focusable="true">
                <ImageView
                    android:layout_width="32dp"
                    android:layout_height="32dp"
                    android:src="@drawable/ic_quick_end_day"
                    android:scaleType="centerInside" />
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/date_end_of_day"
                    android:textColor="@color/main_text_secondary"
                    android:textSize="11sp"
                    android:layout_marginTop="4dp" />
            </LinearLayout>

        </LinearLayout>
    </HorizontalScrollView>

    <!-- Divider -->
    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/main_divider" />

    <!-- ═══ CALENDAR VIEW ═══ -->
    <CalendarView
        android:id="@+id/calendar_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:theme="@style/CalendarDarkTheme"
        android:firstDayOfWeek="2"
        android:background="@color/main_surface" />

    <!-- Divider -->
    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/main_divider" />

    <!-- ═══ OPTIONS: Thời gian, Lời nhắc, Lặp lại ═══ -->

    <!-- Thời gian -->
    <LinearLayout
        android:id="@+id/option_time"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:clickable="true"
        android:focusable="true">
        <ImageView
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_option_time"
            android:scaleType="centerInside" />
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:text="@string/date_option_time"
            android:textColor="@color/main_text_primary"
            android:textSize="14sp" />
        <View
            android:layout_width="0dp"
            android:layout_height="1dp"
            android:layout_weight="1" />
        <TextView
            android:id="@+id/text_time_value"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/date_none"
            android:textColor="@color/main_text_secondary"
            android:textSize="13sp" />
        <ImageView
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:src="@drawable/ic_chevron_right"
            android:layout_marginStart="4dp" />
    </LinearLayout>

    <!-- Lời nhắc -->
    <LinearLayout
        android:id="@+id/option_reminder"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:clickable="true"
        android:focusable="true">
        <ImageView
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_action_reminder"
            android:scaleType="centerInside" />
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:text="@string/date_option_reminder"
            android:textColor="@color/main_text_primary"
            android:textSize="14sp" />
        <View
            android:layout_width="0dp"
            android:layout_height="1dp"
            android:layout_weight="1" />
        <TextView
            android:id="@+id/text_reminder_value"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/date_none"
            android:textColor="@color/main_text_secondary"
            android:textSize="13sp" />
        <ImageView
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:src="@drawable/ic_chevron_right"
            android:layout_marginStart="4dp" />
    </LinearLayout>

    <!-- Lặp lại -->
    <LinearLayout
        android:id="@+id/option_repeat"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:paddingBottom="8dp"
        android:clickable="true"
        android:focusable="true">
        <ImageView
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_option_repeat"
            android:scaleType="centerInside" />
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:text="@string/date_option_repeat"
            android:textColor="@color/main_text_primary"
            android:textSize="14sp" />
        <View
            android:layout_width="0dp"
            android:layout_height="1dp"
            android:layout_weight="1" />
        <TextView
            android:id="@+id/text_repeat_value"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/date_none"
            android:textColor="@color/main_text_secondary"
            android:textSize="13sp" />
        <ImageView
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:src="@drawable/ic_chevron_right"
            android:layout_marginStart="4dp" />
    </LinearLayout>

</LinearLayout>
```

---

## 2. Tạo Drawable

📁 Tất cả vào `app/src/main/res/drawable/`

### ic_close.xml (X đóng)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#B0B0B0">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z" />
</vector>
```

### ic_check_blue.xml (✓ xanh xác nhận)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#4C6FE0"
        android:pathData="M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z" />
</vector>
```

### ic_quick_today.xml (Calendar hôm nay)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#4C6FE0">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M19,3h-1V1h-2v2H8V1H6v2H5C3.9,3 3,3.9 3,5v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM19,19H5V8h14V19zM12,10h5v5h-5z" />
</vector>
```

### ic_quick_tomorrow.xml (Calendar ngày mai)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#FFA726">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M19,3h-1V1h-2v2H8V1H6v2H5C3.9,3 3,3.9 3,5v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM19,19H5V8h14V19zM7,10h5v5H7z" />
</vector>
```

### ic_quick_next_week.xml (Calendar tuần tới)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#42A5F5">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M19,3h-1V1h-2v2H8V1H6v2H5C3.9,3 3,3.9 3,5v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM19,19H5V8h14V19zM9,14l3,-3 3,3" />
</vector>
```

### ic_quick_end_day.xml (Moon / cuối ngày)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#66BB6A">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M12.1,22c-5.1,0 -9.3,-4 -9.6,-9.1C2.2,7.1 6.6,2.5 12.5,2c0.3,0 0.5,0.3 0.3,0.5c-1.2,1.5 -1.8,3.4 -1.8,5.5c0,4.8 3.9,8.6 8.6,8.6c0.5,0 0.9,0 1.4,-0.1c0.3,0 0.4,0.3 0.2,0.5C19.4,19.8 16,22 12.1,22z" />
</vector>
```

### ic_option_time.xml (Clock / thời gian)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#888888">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M11.99,2C6.47,2 2,6.48 2,12s4.47,10 9.99,10C17.52,22 22,17.52 22,12S17.52,2 11.99,2zM12,20c-4.42,0 -8,-3.58 -8,-8s3.58,-8 8,-8 8,3.58 8,8 -3.58,8 -8,8zM12.5,7H11v6l5.25,3.15 0.75,-1.23 -4.5,-2.67z" />
</vector>
```

### ic_option_repeat.xml (Repeat / lặp lại)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#888888">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M7,7h10v3l4,-4 -4,-4v3H5v6h2V7zM17,17H7v-3l-4,4 4,4v-3h12v-6h-2V17z" />
</vector>
```

---

## 3. Thêm Strings

📁 `app/src/main/res/values/strings.xml` — thêm trước `</resources>`

```xml
<!-- Date Picker Bottom Sheet -->
<string name="date_tab_date">Ngày</string>
<string name="date_tab_duration">Thời lượng</string>
<string name="date_today">Hôm nay</string>
<string name="date_tomorrow">Ngày mai</string>
<string name="date_next_monday">Thứ Hai tới</string>
<string name="date_end_of_day">Đến cuối ngày</string>
<string name="date_option_time">Thời gian</string>
<string name="date_option_reminder">Lời nhắc</string>
<string name="date_option_repeat">Lặp lại</string>
<string name="date_none">Không có</string>
```

---

## 4. Thêm Colors

📁 `app/src/main/res/values/colors.xml` — thêm trước `</resources>`

```xml
<!-- Calendar -->
<color name="calendar_selected_bg">#4C6FE0</color>
<color name="calendar_today_text">#4C6FE0</color>
```

---

## 5. Tạo Java — DatePickerBottomSheet

📁 `app/src/main/java/.../dialog/DatePickerBottomSheet.java`

> Dùng `BottomSheetDialogFragment` để tái sử dụng được, có callback trả ngày đã chọn về Activity.

```java
package hcmute.edu.vn.lequanghung_23110110.ticktick.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;

public class DatePickerBottomSheet extends BottomSheetDialogFragment {

    // Callback interface
    public interface OnDateSelectedListener {
        void onDateSelected(String dateTag, long dateMillis);
    }

    private OnDateSelectedListener listener;
    private long selectedDateMillis;
    private String selectedDateTag = "";

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
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

        CalendarView calendarView = view.findViewById(R.id.calendar_view);
        selectedDateMillis = calendarView.getDate();

        // ── Nút đóng (X) ──
        view.findViewById(R.id.date_btn_close).setOnClickListener(v -> dismiss());

        // ── Nút xác nhận (✓) ──
        view.findViewById(R.id.date_btn_confirm).setOnClickListener(v -> {
            if (listener != null) {
                listener.onDateSelected(selectedDateTag, selectedDateMillis);
            }
            dismiss();
        });

        // ── Tab Ngày / Thời lượng ──
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

        // ── Quick date buttons ──
        view.findViewById(R.id.quick_today).setOnClickListener(v -> {
            selectQuickDate(calendarView, 0, "Hôm nay");
        });
        view.findViewById(R.id.quick_tomorrow).setOnClickListener(v -> {
            selectQuickDate(calendarView, 1, "Ngày mai");
        });
        view.findViewById(R.id.quick_next_monday).setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysUntilMonday = (Calendar.MONDAY - dayOfWeek + 7) % 7;
            if (daysUntilMonday == 0) daysUntilMonday = 7;
            selectQuickDate(calendarView, daysUntilMonday, "Thứ Hai tới");
        });
        view.findViewById(R.id.quick_end_of_day).setOnClickListener(v -> {
            selectQuickDate(calendarView, 0, "Đến cuối ngày");
        });

        // ── Calendar chọn ngày ──
        calendarView.setOnDateChangeListener((cv, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            selectedDateMillis = cal.getTimeInMillis();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            selectedDateTag = sdf.format(cal.getTime());
        });

        // ── Options (placeholder) ──
        view.findViewById(R.id.option_time).setOnClickListener(v ->
                Toast.makeText(getContext(), "Chọn thời gian", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.option_reminder).setOnClickListener(v ->
                Toast.makeText(getContext(), "Đặt lời nhắc", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.option_repeat).setOnClickListener(v ->
                Toast.makeText(getContext(), "Đặt lặp lại", Toast.LENGTH_SHORT).show());
    }

    private void selectQuickDate(CalendarView calendarView, int daysFromNow, String tag) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, daysFromNow);
        selectedDateMillis = cal.getTimeInMillis();
        selectedDateTag = tag;
        calendarView.setDate(selectedDateMillis, true, true);
    }
}
```

---

## 6. Gắn vào Add Task Bottom Sheet

Trong `MainActivity.java`, tìm dòng action_date click handler trong method `showAddTaskBottomSheet()`:

```java
// TRƯỚC (Toast placeholder)
sheetView.findViewById(R.id.action_date).setOnClickListener(v ->
        Toast.makeText(this, "Chọn ngày", Toast.LENGTH_SHORT).show());
```

Thay bằng:

```java
// SAU (mở DatePicker BottomSheet)
sheetView.findViewById(R.id.action_date).setOnClickListener(v -> {
    DatePickerBottomSheet datePicker = new DatePickerBottomSheet();
    datePicker.setOnDateSelectedListener((dateTag, dateMillis) -> {
        // Cập nhật dateTag cho task sẽ được tạo
        // Có thể lưu vào biến instance hoặc hiện text trên bottom sheet
        Toast.makeText(this, "Đã chọn: " + dateTag, Toast.LENGTH_SHORT).show();
    });
    datePicker.show(getSupportFragmentManager(), "date_picker");
});
```

Thêm import:

```java
import hcmute.edu.vn.lequanghung_23110110.ticktick.dialog.DatePickerBottomSheet;
```

---

## 7. Checklist

| #   | File                                  | Loại       | Vị trí          |
| --- | ------------------------------------- | ---------- | --------------- |
| 1   | `layout_bottom_sheet_date_picker.xml` | **NEW**    | `res/layout/`   |
| 2   | `ic_close.xml`                        | **NEW**    | `res/drawable/` |
| 3   | `ic_check_blue.xml`                   | **NEW**    | `res/drawable/` |
| 4   | `ic_quick_today.xml`                  | **NEW**    | `res/drawable/` |
| 5   | `ic_quick_tomorrow.xml`               | **NEW**    | `res/drawable/` |
| 6   | `ic_quick_next_week.xml`              | **NEW**    | `res/drawable/` |
| 7   | `ic_quick_end_day.xml`                | **NEW**    | `res/drawable/` |
| 8   | `ic_option_time.xml`                  | **NEW**    | `res/drawable/` |
| 9   | `ic_option_repeat.xml`                | **NEW**    | `res/drawable/` |
| 10  | `strings.xml`                         | **MODIFY** | `res/values/`   |
| 11  | `colors.xml`                          | **MODIFY** | `res/values/`   |
| 12  | `DatePickerBottomSheet.java`          | **NEW**    | `dialog/`       |
| 13  | `MainActivity.java`                   | **MODIFY** | `activity/`     |

> 💡 `ic_action_reminder.xml` và `ic_chevron_right.xml` đã tạo trước đó — tái sử dụng, không cần tạo lại.
