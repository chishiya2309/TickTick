# 🏷️ Date Tag Chip — Hiển thị ngày đã chọn trên Add Task Bottom Sheet

> Sau khi chọn ngày (ví dụ "Ngày mai") trong Date Picker, quay lại Add Task Bottom Sheet và hiển thị **chip/tag** ngày đã chọn bên dưới ô Mô tả.

---

## 📋 Mục lục

1. [Tạo Drawable cho chip](#1-tạo-drawable-cho-chip)
2. [Sửa Layout — Add Task Bottom Sheet](#2-sửa-layout--add-task-bottom-sheet)
3. [Sửa MainActivity.java](#3-sửa-mainactivityjava)
4. [Checklist](#4-checklist)

---

## 1. Tạo Drawable cho chip

📁 `app/src/main/res/drawable/bg_date_chip.xml`

> Bo góc tròn, nền trong suốt nhẹ, viền mờ — giống chip trong screenshot.

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#1A4C6FE0" />
    <corners android:radius="16dp" />
    <stroke
        android:width="1dp"
        android:color="#334C6FE0" />
</shape>
```

---

## 2. Sửa Layout — Add Task Bottom Sheet

📁 `app/src/main/res/layout/layout_bottom_sheet_add_task.xml`

Tìm đoạn `input_task_description` EditText, thêm **ngay dưới nó** (trước Action Bar Row 1):

```xml
    <!-- ═══ Input: Mô tả ═══ -->
    <EditText
        android:id="@+id/input_task_description"
        ... />

    <!-- ═══ DATE TAG CHIP (ẩn mặc định, hiện khi chọn ngày) ═══ -->
    <LinearLayout
        android:id="@+id/date_chip_container"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:paddingTop="4dp"
        android:paddingBottom="8dp"
        android:visibility="gone">

        <LinearLayout
            android:id="@+id/date_chip"
            android:layout_width="wrap_content"
            android:layout_height="28dp"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:background="@drawable/bg_date_chip"
            android:paddingStart="10dp"
            android:paddingEnd="10dp">

            <!-- Icon calendar nhỏ -->
            <ImageView
                android:layout_width="14dp"
                android:layout_height="14dp"
                android:src="@drawable/ic_action_date"
                android:scaleType="centerInside" />

            <!-- Text ngày -->
            <TextView
                android:id="@+id/date_chip_text"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="6dp"
                android:text="Ngày mai"
                android:textColor="#FFA726"
                android:textSize="12sp" />

        </LinearLayout>

    </LinearLayout>

    <!-- ═══ Action Bar Row 1 ═══ -->
    ...
```

**Toàn bộ file sửa** — thêm khối `date_chip_container` ngay sau `input_task_description`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@color/main_surface"
    android:paddingTop="16dp">

    <!-- ═══ Input: Tiêu đề task ═══ -->
    <EditText
        android:id="@+id/input_task_title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:paddingTop="8dp"
        android:paddingBottom="4dp"
        android:background="@android:color/transparent"
        android:hint="@string/hint_task_title"
        android:textColor="@color/main_text_primary"
        android:textColorHint="@color/main_text_secondary"
        android:textSize="16sp"
        android:inputType="textCapSentences"
        android:maxLines="3"
        android:importantForAutofill="no" />

    <!-- ═══ Input: Mô tả ═══ -->
    <EditText
        android:id="@+id/input_task_description"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:paddingTop="4dp"
        android:paddingBottom="8dp"
        android:background="@android:color/transparent"
        android:hint="@string/hint_task_description"
        android:textColor="@color/main_text_secondary"
        android:textColorHint="#666666"
        android:textSize="14sp"
        android:inputType="textMultiLine|textCapSentences"
        android:maxLines="5"
        android:importantForAutofill="no" />

    <!-- ═══ DATE TAG CHIP (ẩn mặc định, hiện sau khi chọn ngày) ═══ -->
    <LinearLayout
        android:id="@+id/date_chip_container"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:paddingTop="4dp"
        android:paddingBottom="8dp"
        android:visibility="gone">

        <LinearLayout
            android:id="@+id/date_chip"
            android:layout_width="wrap_content"
            android:layout_height="28dp"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:background="@drawable/bg_date_chip"
            android:paddingStart="10dp"
            android:paddingEnd="10dp">

            <ImageView
                android:id="@+id/date_chip_icon"
                android:layout_width="14dp"
                android:layout_height="14dp"
                android:src="@drawable/ic_action_date"
                android:scaleType="centerInside" />

            <TextView
                android:id="@+id/date_chip_text"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="6dp"
                android:textColor="#FFA726"
                android:textSize="12sp" />

        </LinearLayout>

    </LinearLayout>

    <!-- ═══ Action Bar Row 1: Ngày, Flag, Nhắc nhở, Thêm ═══ -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="44dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingStart="12dp"
        android:paddingEnd="12dp">

        <ImageView
            android:id="@+id/action_date"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:padding="6dp"
            android:src="@drawable/ic_action_date"
            android:scaleType="centerInside"
            android:clickable="true"
            android:focusable="true"
            android:contentDescription="Chọn ngày" />

        <ImageView
            android:id="@+id/action_flag"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:padding="6dp"
            android:src="@drawable/ic_action_flag"
            android:scaleType="centerInside"
            android:clickable="true"
            android:focusable="true"
            android:contentDescription="Đánh dấu" />

        <ImageView
            android:id="@+id/action_reminder"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:padding="6dp"
            android:src="@drawable/ic_action_reminder"
            android:scaleType="centerInside"
            android:clickable="true"
            android:focusable="true"
            android:contentDescription="Nhắc nhở" />

        <ImageView
            android:id="@+id/action_more_options"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:padding="6dp"
            android:src="@drawable/ic_action_more"
            android:scaleType="centerInside"
            android:clickable="true"
            android:focusable="true"
            android:contentDescription="Thêm" />

        <View
            android:layout_width="0dp"
            android:layout_height="1dp"
            android:layout_weight="1" />

        <ImageView
            android:id="@+id/action_mic"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:padding="6dp"
            android:src="@drawable/ic_action_mic"
            android:scaleType="centerInside"
            android:clickable="true"
            android:focusable="true"
            android:contentDescription="Ghi âm" />

    </LinearLayout>

    <!-- ═══ Action Bar Row 2: Danh sách + Nút gửi ═══ -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingStart="12dp"
        android:paddingEnd="12dp"
        android:paddingBottom="4dp">

        <ImageView
            android:id="@+id/action_select_list"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:padding="6dp"
            android:src="@drawable/ic_action_grid"
            android:scaleType="centerInside"
            android:clickable="true"
            android:focusable="true" />

        <TextView
            android:id="@+id/text_current_list"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Hộp thư đến"
            android:textColor="@color/main_text_secondary"
            android:textSize="13sp"
            android:layout_marginStart="4dp" />

        <ImageView
            android:id="@+id/btn_submit_task"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:padding="8dp"
            android:src="@drawable/ic_action_send"
            android:scaleType="centerInside"
            android:clickable="true"
            android:focusable="true"
            android:contentDescription="Lưu task" />

    </LinearLayout>

</LinearLayout>
```

---

## 3. Sửa MainActivity.java

Trong method `showAddTaskBottomSheet()`, sửa phần `action_date` click handler.

### Thêm biến lưu ngày đã chọn + cập nhật chip

```java
private void showAddTaskBottomSheet() {
    BottomSheetDialog bottomSheet = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
    View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_add_task, null);
    bottomSheet.setContentView(sheetView);

    EditText inputTitle = sheetView.findViewById(R.id.input_task_title);
    EditText inputDescription = sheetView.findViewById(R.id.input_task_description);
    TextView textCurrentList = sheetView.findViewById(R.id.text_current_list);

    // Date chip views
    View dateChipContainer = sheetView.findViewById(R.id.date_chip_container);
    TextView dateChipText = sheetView.findViewById(R.id.date_chip_text);

    // Biến lưu ngày đã chọn (dùng mảng 1 phần tử để truy cập trong lambda)
    final String[] selectedDateTag = {""};

    String currentListName = dbHelper.getListNameById(currentListId);
    textCurrentList.setText(currentListName);

    // ═══ DATE PICKER — Mở Date Picker, nhận callback, hiện chip ═══
    sheetView.findViewById(R.id.action_date).setOnClickListener(v -> {
        DatePickerBottomSheet datePicker = new DatePickerBottomSheet();
        datePicker.setOnDateSelectedListener((dateTag, dateMillis) -> {
            // Lưu dateTag
            selectedDateTag[0] = dateTag;

            // Hiện chip với text ngày đã chọn
            dateChipText.setText(dateTag);
            dateChipContainer.setVisibility(View.VISIBLE);

            // Đổi màu chip text theo loại ngày
            int chipColor = getDateChipColor(dateTag);
            dateChipText.setTextColor(chipColor);
        });
        datePicker.show(getSupportFragmentManager(), "date_picker");
    });

    // Nhấn vào chip để xóa ngày (tùy chọn)
    sheetView.findViewById(R.id.date_chip).setOnClickListener(v -> {
        // Mở lại date picker để chọn ngày khác
        DatePickerBottomSheet datePicker = new DatePickerBottomSheet();
        datePicker.setOnDateSelectedListener((dateTag, dateMillis) -> {
            selectedDateTag[0] = dateTag;
            dateChipText.setText(dateTag);
            int chipColor = getDateChipColor(dateTag);
            dateChipText.setTextColor(chipColor);
        });
        datePicker.show(getSupportFragmentManager(), "date_picker");
    });

    // Các action khác giữ nguyên...
    sheetView.findViewById(R.id.action_flag).setOnClickListener(v ->
            Toast.makeText(this, "Đánh dấu ưu tiên", Toast.LENGTH_SHORT).show());
    sheetView.findViewById(R.id.action_reminder).setOnClickListener(v ->
            Toast.makeText(this, "Đặt nhắc nhở", Toast.LENGTH_SHORT).show());
    sheetView.findViewById(R.id.action_more_options).setOnClickListener(v ->
            Toast.makeText(this, "Thêm tùy chọn", Toast.LENGTH_SHORT).show());
    sheetView.findViewById(R.id.action_mic).setOnClickListener(v ->
            Toast.makeText(this, "Ghi âm", Toast.LENGTH_SHORT).show());

    // ═══ NÚT GỬI — Lưu task với dateTag ═══
    sheetView.findViewById(R.id.btn_submit_task).setOnClickListener(v -> {
        String title = inputTitle.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            inputTitle.setError("Nhập tiêu đề task");
            inputTitle.requestFocus();
            return;
        }

        // Lưu vào SQLite — dùng selectedDateTag[0] làm dateTag
        dbHelper.insertTask(title, currentListId, selectedDateTag[0]);

        loadTasksForList(currentListId);
        bottomSheet.dismiss();
        Toast.makeText(this, "Đã thêm: " + title, Toast.LENGTH_SHORT).show();
    });

    bottomSheet.show();
    inputTitle.requestFocus();
}
```

### Thêm helper method cho màu chip

```java
/**
 * Trả về màu chip phù hợp với loại ngày:
 * - Hôm nay → xanh dương
 * - Ngày mai → cam
 * - Thứ Hai tới → xanh nhạt
 * - Đến cuối ngày → xanh lá
 * - Ngày cụ thể → trắng xám
 */
private int getDateChipColor(String dateTag) {
    switch (dateTag) {
        case "Hôm nay":       return Color.parseColor("#4C6FE0");
        case "Ngày mai":      return Color.parseColor("#FFA726");
        case "Thứ Hai tới":   return Color.parseColor("#42A5F5");
        case "Đến cuối ngày": return Color.parseColor("#66BB6A");
        default:              return Color.parseColor("#B0B0B0");
    }
}
```

---

## 4. Checklist

| #   | File                               | Loại       | Thay đổi                                                        |
| --- | ---------------------------------- | ---------- | --------------------------------------------------------------- |
| 1   | `bg_date_chip.xml`                 | **NEW**    | `res/drawable/`                                                 |
| 2   | `layout_bottom_sheet_add_task.xml` | **MODIFY** | Thêm `date_chip_container` giữa Mô tả và Action Bar             |
| 3   | `MainActivity.java`                | **MODIFY** | Cập nhật `showAddTaskBottomSheet()` + thêm `getDateChipColor()` |

> 💡 **Flow hoàn chỉnh**:
>
> 1. Nhấn FAB → Bottom Sheet mở (chip ẩn)
> 2. Nhấn icon 📅 → Date Picker mở
> 3. Chọn "Ngày mai" → Date Picker đóng, callback trả `dateTag = "Ngày mai"`
> 4. Chip hiện: `📅 Ngày mai` (màu cam `#FFA726`)
> 5. Nhấn "Gửi" → `insertTask(title, listId, "Ngày mai")` → lưu DB
