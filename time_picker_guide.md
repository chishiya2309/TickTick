# ⏰ Time Picker Dialog — Spinner + Clock Mode

> Khi nhấn "Thời gian" trong Date Picker Bottom Sheet:
>
> - **Hình 1** (mặc định): Spinner mode (cuộn giờ/phút)
> - Nhấn icon 🕐 nhỏ → chuyển sang **Hình 2**: Clock face mode
> - Nhấn icon ⌨️ nhỏ → quay lại **Hình 1**: Spinner mode
> - Nút **OK** → xác nhận, **Hủy bỏ** → đóng

---

## 📋 Mục lục

1. [Dùng MaterialTimePicker](#1-dùng-materialtimepicker)
2. [Sửa DatePickerBottomSheet.java](#2-sửa-datepickerbottomsheetjava)
3. [Thêm Style cho TimePicker](#3-thêm-style-cho-timepicker)
4. [Checklist](#4-checklist)

---

## 1. Dùng MaterialTimePicker

> **Không cần tạo layout** — Material 3 đã hỗ trợ sẵn `MaterialTimePicker` với 2 mode:
>
> - `INPUT_MODE_KEYBOARD` → spinner (hình 1)
> - `INPUT_MODE_CLOCK` → clock face (hình 2)
>
> Nút toggle giữa 2 mode **đã có sẵn** trong MaterialTimePicker (icon góc dưới trái).

### Dependency (đã có nếu dùng Material 3)

📁 `app/build.gradle` — kiểm tra:

```groovy
implementation 'com.google.android.material:material:1.12.0'
```

---

## 2. Sửa DatePickerBottomSheet.java

📁 `app/src/main/java/.../dialog/DatePickerBottomSheet.java`

### 2a. Thêm import

```java
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
```

### 2b. Thêm biến lưu giờ đã chọn

Thêm vào đầu class, cạnh các field khác:

```java
private int selectedHour = -1;
private int selectedMinute = -1;
```

### 2c. Thêm interface callback cho thời gian

Thêm interface mới:

```java
public interface OnTimeSelectedListener {
    void onTimeSelected(int hour, int minute);
}

private OnTimeSelectedListener timeListener;

public void setOnTimeSelectedListener(OnTimeSelectedListener listener) {
    this.timeListener = listener;
}
```

### 2d. Sửa option_time click handler

Tìm trong `onViewCreated()`:

```java
// TRƯỚC
view.findViewById(R.id.option_time).setOnClickListener(v ->
        Toast.makeText(getContext(), "Chọn thời gian", Toast.LENGTH_SHORT).show());
```

Thay bằng:

```java
// SAU
TextView textTimeValue = view.findViewById(R.id.text_time_value);
view.findViewById(R.id.option_time).setOnClickListener(v -> showTimePicker(textTimeValue));
```

### 2e. Thêm method `showTimePicker()`

```java
private void showTimePicker(TextView textTimeValue) {
    // Giờ mặc định: giờ hiện tại hoặc giờ đã chọn trước đó
    int hour = selectedHour >= 0 ? selectedHour : Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    int minute = selectedMinute >= 0 ? selectedMinute : Calendar.getInstance().get(Calendar.MINUTE);

    MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText("Thời gian")
            .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)  // Spinner mặc định (hình 1)
            .setTheme(R.style.TimePickerDarkTheme)
            .build();

    timePicker.addOnPositiveButtonClickListener(v -> {
        selectedHour = timePicker.getHour();
        selectedMinute = timePicker.getMinute();

        // Hiển thị giờ đã chọn
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
        textTimeValue.setText(timeStr);
        textTimeValue.setTextColor(requireContext().getColor(R.color.main_accent_blue));

        if (timeListener != null) {
            timeListener.onTimeSelected(selectedHour, selectedMinute);
        }
    });

    timePicker.show(getChildFragmentManager(), "time_picker");
}
```

---

## 3. Thêm Style cho TimePicker

📁 `app/src/main/res/values/themes.xml` — thêm trước `</resources>`

```xml
<!-- Time Picker Dark Theme -->
<style name="TimePickerDarkTheme" parent="ThemeOverlay.Material3.MaterialTimePicker">
    <!-- Dialog background -->
    <item name="colorSurface">#2D2D2D</item>
    <item name="colorSurfaceContainerHigh">#2D2D2D</item>
    <!-- Text trên dialog -->
    <item name="colorOnSurface">#E0E0E0</item>
    <item name="colorOnSurfaceVariant">#B0B0B0</item>
    <!-- Accent (selected state) -->
    <item name="colorPrimary">#4C6FE0</item>
    <item name="colorOnPrimary">#FFFFFF</item>
    <!-- Input fields -->
    <item name="colorPrimaryContainer">#3A3A3A</item>
    <item name="colorOnPrimaryContainer">#FFFFFF</item>
    <!-- Tertiary (unselected input) -->
    <item name="colorTertiaryContainer">#3A3A3A</item>
    <item name="colorOnTertiaryContainer">#B0B0B0</item>
    <!-- Clock face -->
    <item name="colorSurfaceVariant">#3A3A3A</item>
    <!-- Button text -->
    <item name="materialTimePickerTitleStyle">@style/TimePickerTitleStyle</item>
</style>

<style name="TimePickerTitleStyle" parent="Widget.Material3.MaterialTimePicker.Display.TextInputLayout.OutlinedBox">
    <!-- Kế thừa style mặc định -->
</style>
```

---

## 4. Kết nối với callback Date Picker → Add Task

Nếu muốn hiển thị giờ đã chọn trên chip hoặc gửi về MainActivity, thêm vào `onViewCreated()` phần confirm button:

```java
// Trong date_btn_confirm click handler, thêm giờ vào dateTag
view.findViewById(R.id.date_btn_confirm).setOnClickListener(v -> {
    if (selectedDate != null && dateListener != null) {
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
```

---

## 5. Checklist

| #   | File                         | Loại       | Thay đổi                                                                                  |
| --- | ---------------------------- | ---------- | ----------------------------------------------------------------------------------------- |
| 1   | `DatePickerBottomSheet.java` | **MODIFY** | Thêm `showTimePicker()`, fields `selectedHour/Minute`, interface `OnTimeSelectedListener` |
| 2   | `themes.xml`                 | **MODIFY** | Thêm `TimePickerDarkTheme` style                                                          |

> 💡 **Không cần tạo layout mới!** `MaterialTimePicker` đã có sẵn:
>
> - Spinner mode (INPUT_MODE_KEYBOARD) = hình 1
> - Clock mode (INPUT_MODE_CLOCK) = hình 2
> - Nút toggle tự động hiện ở góc dưới trái
> - Nút "Hủy bỏ" + "OK" có sẵn
