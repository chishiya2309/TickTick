# 📅 Custom Calendar — Ngày hiện tại trắng, ngày chọn xanh + Nút Xóa

> Thay thế `CalendarView` mặc định bằng **Custom Calendar GridView** để:
>
> - Ngày hiện tại → vòng tròn **viền trắng**
> - Ngày được chọn → vòng tròn **nền xanh dương**
> - Nút **"Xóa"** màu đỏ ở cuối → xóa ngày đã chọn + ẩn date chip

---

## 📋 Mục lục

1. [Tạo Drawable cho ô ngày](#1-tạo-drawable-cho-ô-ngày)
2. [Tạo Layout ô ngày](#2-tạo-layout-ô-ngày)
3. [Tạo CalendarAdapter](#3-tạo-calendaradapter)
4. [Sửa Layout Date Picker](#4-sửa-layout-date-picker)
5. [Sửa DatePickerBottomSheet.java](#5-sửa-datepickerbottomsheetjava)
6. [Sửa MainActivity — xử lý nút Xóa](#6-sửa-mainactivity--xử-lý-nút-xóa)
7. [Thêm Resources](#7-thêm-resources)
8. [Checklist](#8-checklist)

---

## 1. Tạo Drawable cho ô ngày

📁 Tất cả vào `app/src/main/res/drawable/`

### bg_day_today.xml (vòng tròn viền trắng — ngày hiện tại)

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="@android:color/transparent" />
    <stroke
        android:width="1.5dp"
        android:color="#FFFFFF" />
    <size
        android:width="36dp"
        android:height="36dp" />
</shape>
```

### bg_day_selected.xml (vòng tròn nền xanh — ngày được chọn)

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#4C6FE0" />
    <size
        android:width="36dp"
        android:height="36dp" />
</shape>
```

---

## 2. Tạo Layout ô ngày

📁 `app/src/main/res/layout/item_calendar_day.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="44dp"
    android:gravity="center">

    <TextView
        android:id="@+id/day_text"
        android:layout_width="36dp"
        android:layout_height="36dp"
        android:layout_gravity="center"
        android:gravity="center"
        android:textColor="@color/main_text_primary"
        android:textSize="14sp" />

</FrameLayout>
```

---

## 3. Tạo CalendarAdapter

📁 `app/src/main/java/.../adapter/CalendarAdapter.java`

```java
package hcmute.edu.vn.lequanghung_23110110.ticktick.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;

public class CalendarAdapter extends BaseAdapter {

    private final Context context;
    private final List<Integer> days;        // Ngày trong tháng (0 = ô trống)
    private final int todayDay;              // Ngày hôm nay
    private final int todayMonth;
    private final int todayYear;
    private final int displayMonth;          // Tháng đang hiển thị
    private final int displayYear;
    private int selectedDay = -1;            // Ngày được chọn (-1 = chưa chọn)

    public CalendarAdapter(Context context, List<Integer> days,
                           int displayMonth, int displayYear) {
        this.context = context;
        this.days = days;
        this.displayMonth = displayMonth;
        this.displayYear = displayYear;

        Calendar today = Calendar.getInstance();
        this.todayDay = today.get(Calendar.DAY_OF_MONTH);
        this.todayMonth = today.get(Calendar.MONTH);
        this.todayYear = today.get(Calendar.YEAR);
    }

    public void setSelectedDay(int day) {
        this.selectedDay = day;
        notifyDataSetChanged();
    }

    public int getSelectedDay() {
        return selectedDay;
    }

    @Override
    public int getCount() { return days.size(); }

    @Override
    public Object getItem(int position) { return days.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_calendar_day, parent, false);
        }

        TextView dayText = convertView.findViewById(R.id.day_text);
        int day = days.get(position);

        if (day == 0) {
            // Ô trống (padding đầu tháng)
            dayText.setText("");
            dayText.setBackground(null);
            return convertView;
        }

        dayText.setText(String.valueOf(day));

        boolean isToday = (day == todayDay
                && displayMonth == todayMonth
                && displayYear == todayYear);
        boolean isSelected = (day == selectedDay);

        if (isSelected) {
            // Ngày được chọn → vòng tròn xanh, text trắng
            dayText.setBackgroundResource(R.drawable.bg_day_selected);
            dayText.setTextColor(Color.WHITE);
        } else if (isToday) {
            // Ngày hôm nay → vòng tròn viền trắng
            dayText.setBackgroundResource(R.drawable.bg_day_today);
            dayText.setTextColor(Color.WHITE);
        } else {
            // Ngày bình thường
            dayText.setBackground(null);
            dayText.setTextColor(Color.parseColor("#CCCCCC"));
        }

        return convertView;
    }
}
```

---

## 4. Sửa Layout Date Picker

📁 `app/src/main/res/layout/layout_bottom_sheet_date_picker.xml`

**Thay thế** toàn bộ khối `CalendarView` bằng custom calendar + nút Xóa:

Tìm:

```xml
    <!-- ═══ CALENDAR VIEW ═══ -->
    <CalendarView
        android:id="@+id/calendar_view"
        ... />
```

Thay bằng:

```xml
    <!-- ═══ CUSTOM CALENDAR ═══ -->

    <!-- Tháng header: < tháng 3 > -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="40dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingStart="16dp"
        android:paddingEnd="16dp">

        <TextView
            android:id="@+id/calendar_month_title"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="tháng 3"
            android:textColor="@color/main_text_primary"
            android:textSize="15sp"
            android:textStyle="bold" />

        <ImageView
            android:id="@+id/calendar_btn_prev"
            android:layout_width="32dp"
            android:layout_height="32dp"
            android:padding="4dp"
            android:src="@drawable/ic_chevron_left"
            android:scaleType="centerInside"
            android:clickable="true"
            android:focusable="true" />

        <ImageView
            android:id="@+id/calendar_btn_next"
            android:layout_width="32dp"
            android:layout_height="32dp"
            android:padding="4dp"
            android:src="@drawable/ic_chevron_right"
            android:scaleType="centerInside"
            android:clickable="true"
            android:focusable="true" />
    </LinearLayout>

    <!-- Day-of-week headers: Th 2 | Th 3 | ... | CN -->
    <GridView
        android:id="@+id/calendar_day_headers"
        android:layout_width="match_parent"
        android:layout_height="30dp"
        android:numColumns="7"
        android:gravity="center"
        android:enabled="false" />

    <!-- Calendar grid (6 rows x 7 cols) -->
    <GridView
        android:id="@+id/calendar_grid"
        android:layout_width="match_parent"
        android:layout_height="264dp"
        android:numColumns="7"
        android:gravity="center"
        android:verticalSpacing="0dp"
        android:horizontalSpacing="0dp"
        android:stretchMode="columnWidth" />
```

Rồi ở **cuối file** (trước `</LinearLayout>` đóng), thêm nút **Xóa**:

```xml
    <!-- ═══ NÚT XÓA (clear date) ═══ -->
    <TextView
        android:id="@+id/btn_clear_date"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:gravity="center"
        android:text="@string/date_clear"
        android:textColor="#EF5350"
        android:textSize="15sp"
        android:clickable="true"
        android:focusable="true" />

</LinearLayout>
```

---

## 5. Sửa DatePickerBottomSheet.java

📁 `app/src/main/java/.../dialog/DatePickerBottomSheet.java` — **THAY TOÀN BỘ FILE**

```java
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.CalendarAdapter;

public class DatePickerBottomSheet extends BottomSheetDialogFragment {

    public interface OnDateSelectedListener {
        void onDateSelected(String dateTag, long dateMillis);
    }

    public interface OnDateClearedListener {
        void onDateCleared();
    }

    private OnDateSelectedListener dateListener;
    private OnDateClearedListener clearListener;

    private Calendar displayCalendar;  // Tháng đang hiển thị
    private Calendar selectedDate;      // Ngày được chọn (null = chưa chọn)

    private CalendarAdapter calendarAdapter;
    private TextView monthTitle;

    // Cho phép pre-select ngày (từ quick buttons trước đó)
    private long preSelectedDateMillis = -1;

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.dateListener = listener;
    }

    public void setOnDateClearedListener(OnDateClearedListener listener) {
        this.clearListener = listener;
    }

    public void setPreSelectedDate(long millis) {
        this.preSelectedDateMillis = millis;
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
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String dateTag = sdf.format(selectedDate.getTime());
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
        String[] dayHeaders = {"Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7", "CN"};
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
            if (day == 0) return;

            selectedDate = (Calendar) displayCalendar.clone();
            selectedDate.set(Calendar.DAY_OF_MONTH, day);
            calendarAdapter.setSelectedDay(day);
        });

        // ── Options (placeholder) ──
        view.findViewById(R.id.option_time).setOnClickListener(v ->
                Toast.makeText(getContext(), "Chọn thời gian", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.option_reminder).setOnClickListener(v ->
                Toast.makeText(getContext(), "Đặt lời nhắc", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.option_repeat).setOnClickListener(v ->
                Toast.makeText(getContext(), "Đặt lặp lại", Toast.LENGTH_SHORT).show());

        // ── NÚT XÓA ──
        view.findViewById(R.id.btn_clear_date).setOnClickListener(v -> {
            selectedDate = null;
            if (clearListener != null) {
                clearListener.onDateCleared();
            }
            dismiss();
        });
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
                if (daysUntilMonday == 0) daysUntilMonday = 7;
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
        String[] monthNames = {"tháng 1", "tháng 2", "tháng 3", "tháng 4",
                "tháng 5", "tháng 6", "tháng 7", "tháng 8",
                "tháng 9", "tháng 10", "tháng 11", "tháng 12"};
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
```

---

## 6. Sửa MainActivity — xử lý nút Xóa

Trong `showAddTaskBottomSheet()`, sửa `action_date` click handler:

```java
sheetView.findViewById(R.id.action_date).setOnClickListener(v -> {
    DatePickerBottomSheet datePicker = new DatePickerBottomSheet();

    // Callback: ngày được chọn
    datePicker.setOnDateSelectedListener((dateTag, dateMillis) -> {
        selectedDateTag[0] = dateTag;
        selectedDateMillis[0] = dateMillis;
        dateChipText.setText(dateTag);
        dateChipContainer.setVisibility(View.VISIBLE);
        int chipColor = getDateChipColor(dateTag);
        dateChipText.setTextColor(chipColor);
    });

    // Callback: nút Xóa → xóa chip + reset ngày
    datePicker.setOnDateClearedListener(() -> {
        selectedDateTag[0] = "";
        selectedDateMillis[0] = -1;
        dateChipContainer.setVisibility(View.GONE);
    });

    // Nếu đã chọn ngày trước đó → pre-select trên calendar
    if (selectedDateMillis[0] > 0) {
        datePicker.setPreSelectedDate(selectedDateMillis[0]);
    }

    datePicker.show(getSupportFragmentManager(), "date_picker");
});
```

Thêm khai báo biến `selectedDateMillis` cạnh `selectedDateTag`:

```java
final long[] selectedDateMillis = {-1};
```

---

## 7. Thêm Resources

### Tạo layout header cho day-of-week

📁 `app/src/main/res/layout/item_calendar_day_header.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/header_text"
    android:layout_width="match_parent"
    android:layout_height="30dp"
    android:gravity="center"
    android:textColor="#888888"
    android:textSize="12sp" />
```

### Tạo icon chevron left

📁 `app/src/main/res/drawable/ic_chevron_left.xml`

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
        android:pathData="M15.41,7.41L14,6l-6,6 6,6 1.41,-1.41L10.83,12z" />
</vector>
```

### Thêm string

📁 `app/src/main/res/values/strings.xml`

```xml
<string name="date_clear">Xóa</string>
```

### Sửa HorizontalScrollView ID

Trong `layout_bottom_sheet_date_picker.xml`, thêm ID cho `HorizontalScrollView` quick buttons để toggle visibility:

Tìm:

```xml
<HorizontalScrollView
    android:layout_width="match_parent"
```

Thêm ID:

```xml
<HorizontalScrollView
    android:id="@+id/quick_buttons_scroll"
    android:layout_width="match_parent"
```

---

## 8. Checklist

| #   | File                                  | Loại       | Vị trí          |
| --- | ------------------------------------- | ---------- | --------------- |
| 1   | `bg_day_today.xml`                    | **NEW**    | `res/drawable/` |
| 2   | `bg_day_selected.xml`                 | **NEW**    | `res/drawable/` |
| 3   | `ic_chevron_left.xml`                 | **NEW**    | `res/drawable/` |
| 4   | `item_calendar_day.xml`               | **NEW**    | `res/layout/`   |
| 5   | `item_calendar_day_header.xml`        | **NEW**    | `res/layout/`   |
| 6   | `CalendarAdapter.java`                | **NEW**    | `adapter/`      |
| 7   | `layout_bottom_sheet_date_picker.xml` | **MODIFY** | `res/layout/`   |
| 8   | `DatePickerBottomSheet.java`          | **MODIFY** | `dialog/`       |
| 9   | `MainActivity.java`                   | **MODIFY** | `activity/`     |
| 10  | `strings.xml`                         | **MODIFY** | `res/values/`   |

> 💡 **Flow hoàn chỉnh**:
>
> 1. Mở Date Picker lần đầu → Quick buttons hiện, calendar chưa chọn gì
> 2. Chọn "Thứ Hai tới" → đóng picker, chip cam hiện trên add task sheet
> 3. Nhấn 📅 lại → Picker mở, quick buttons ẩn, calendar highlight ngày đã chọn (xanh) + hôm nay (trắng)
> 4. Nhấn **Xóa** → đóng picker, chip biến mất, dateTag reset
