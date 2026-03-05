# ➕ FAB Add Task — Bottom Sheet Dialog

> Khi nhấn FAB (+), hiển thị **BottomSheetDialog** trượt lên từ dưới với 2 ô nhập:
>
> - **"Bạn thích làm gì?"** — tiêu đề task
> - **"Mô tả"** — mô tả chi tiết
> - Thanh action icons phía dưới (ngày, flag, nhắc nhở, v.v.)

---

## 📋 Mục lục

1. [Tạo Layout Bottom Sheet](#1-tạo-layout-bottom-sheet)
2. [Tạo Drawable Icons](#2-tạo-drawable-icons)
3. [Thêm Strings](#3-thêm-strings)
4. [Sửa MainActivity.java](#4-sửa-mainactivityjava)
5. [Checklist](#5-checklist)

---

## 1. Tạo Layout Bottom Sheet

📁 `app/src/main/res/layout/layout_bottom_sheet_add_task.xml`

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

    <!-- ═══ Action Bar Row 1: Ngày, Flag, Nhắc nhở, Thêm ═══ -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="44dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingStart="12dp"
        android:paddingEnd="12dp">

        <!-- Ngày -->
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

        <!-- Flag / Ưu tiên -->
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

        <!-- Nhắc nhở -->
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

        <!-- Thêm tùy chọn -->
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

        <!-- Spacer -->
        <View
            android:layout_width="0dp"
            android:layout_height="1dp"
            android:layout_weight="1" />

        <!-- Microphone -->
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

        <!-- Chọn danh sách -->
        <ImageView
            android:id="@+id/action_select_list"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:padding="6dp"
            android:src="@drawable/ic_action_grid"
            android:scaleType="centerInside"
            android:clickable="true"
            android:focusable="true" />

        <!-- Tên danh sách hiện tại -->
        <TextView
            android:id="@+id/text_current_list"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Hộp thư đến"
            android:textColor="@color/main_text_secondary"
            android:textSize="13sp"
            android:layout_marginStart="4dp" />

        <!-- Nút Gửi / Lưu -->
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

## 2. Tạo Drawable Icons

📁 Tất cả vào `app/src/main/res/drawable/`

### ic_action_date.xml (Calendar outline)

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
        android:pathData="M19,3h-1V1h-2v2H8V1H6v2H5C3.9,3 3,3.9 3,5v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM19,19H5V8h14V19z" />
</vector>
```

### ic_action_flag.xml (Flag / Ưu tiên)

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
        android:pathData="M14.4,6L14,4H5v17h2v-7h5.6l0.4,2h7V6z" />
</vector>
```

### ic_action_reminder.xml (Bell / Nhắc nhở)

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
        android:pathData="M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2zM18,16v-5c0,-3.07 -1.63,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.64,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z" />
</vector>
```

### ic_action_more.xml (Dấu 3 chấm ngang `···`)

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
        android:pathData="M6,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2zM18,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2zM12,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z" />
</vector>
```

### ic_action_mic.xml (Microphone)

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
        android:pathData="M12,14c1.66,0 2.99,-1.34 2.99,-3L15,5c0,-1.66 -1.34,-3 -3,-3S9,3.34 9,5v6c0,1.66 1.34,3 3,3zM17.3,11c0,3 -2.54,5.1 -5.3,5.1S6.7,14 6.7,11H5c0,3.41 2.72,6.23 6,6.72V21h2v-3.28c3.28,-0.48 6,-3.3 6,-6.72h-1.7z" />
</vector>
```

### ic_action_grid.xml (Grid / Chọn danh sách)

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
        android:pathData="M4,8h4V4H4V8zM10,20h4v-4h-4V20zM4,20h4v-4H4V20zM4,14h4v-4H4V14zM10,14h4v-4h-4V14zM16,4v4h4V4H16zM10,8h4V4h-4V8zM16,14h4v-4h-4V14zM16,20h4v-4h-4V20z" />
</vector>
```

### ic_action_send.xml (Dấu tick gửi — nút lưu xanh)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#4C6FE0"
        android:pathData="M2,21l21,-9L2,3v7l15,2L2,14z" />
</vector>
```

---

## 3. Thêm Strings

📁 `app/src/main/res/values/strings.xml` — thêm vào cuối trước `</resources>`

```xml
<!-- Add Task Bottom Sheet -->
<string name="hint_task_title">Bạn thích làm gì?</string>
<string name="hint_task_description">Mô tả</string>
```

---

## 4. Sửa MainActivity.java

### 4a. Thêm imports

```java
import android.text.TextUtils;
import android.widget.EditText;
import com.google.android.material.bottomsheet.BottomSheetDialog;
```

### 4b. Thay thế logic FAB click trong `setupFab()`

Tìm:

```java
fab.setOnClickListener(v -> {
    String defaultTitle = "Task mới";
    dbHelper.insertTask(defaultTitle, currentListId, "");
    loadTasksForList(currentListId);
    Toast.makeText(this, "Đã thêm task vào " +
            dbHelper.getListNameById(currentListId), Toast.LENGTH_SHORT).show();
});
```

Thay bằng:

```java
fab.setOnClickListener(v -> showAddTaskBottomSheet());
```

### 4c. Thêm method `showAddTaskBottomSheet()`

```java
private void showAddTaskBottomSheet() {
    BottomSheetDialog bottomSheet = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
    View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_add_task, null);
    bottomSheet.setContentView(sheetView);

    // Inputs
    EditText inputTitle = sheetView.findViewById(R.id.input_task_title);
    EditText inputDescription = sheetView.findViewById(R.id.input_task_description);
    TextView textCurrentList = sheetView.findViewById(R.id.text_current_list);

    // Hiện tên danh sách hiện tại
    String currentListName = dbHelper.getListNameById(currentListId);
    textCurrentList.setText(currentListName);

    // Action buttons (chỉ Toast placeholder)
    sheetView.findViewById(R.id.action_date).setOnClickListener(v ->
            Toast.makeText(this, "Chọn ngày", Toast.LENGTH_SHORT).show());
    sheetView.findViewById(R.id.action_flag).setOnClickListener(v ->
            Toast.makeText(this, "Đánh dấu ưu tiên", Toast.LENGTH_SHORT).show());
    sheetView.findViewById(R.id.action_reminder).setOnClickListener(v ->
            Toast.makeText(this, "Đặt nhắc nhở", Toast.LENGTH_SHORT).show());
    sheetView.findViewById(R.id.action_more_options).setOnClickListener(v ->
            Toast.makeText(this, "Thêm tùy chọn", Toast.LENGTH_SHORT).show());
    sheetView.findViewById(R.id.action_mic).setOnClickListener(v ->
            Toast.makeText(this, "Ghi âm", Toast.LENGTH_SHORT).show());

    // Nút gửi — Lưu task vào DB
    sheetView.findViewById(R.id.btn_submit_task).setOnClickListener(v -> {
        String title = inputTitle.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            inputTitle.setError("Nhập tiêu đề task");
            inputTitle.requestFocus();
            return;
        }

        // Lưu vào SQLite
        dbHelper.insertTask(title, currentListId, "");

        // Reload danh sách và đóng bottom sheet
        loadTasksForList(currentListId);
        bottomSheet.dismiss();

        Toast.makeText(this, "Đã thêm: " + title, Toast.LENGTH_SHORT).show();
    });

    // Hiển thị Bottom Sheet và auto-focus vào ô tiêu đề
    bottomSheet.show();
    inputTitle.requestFocus();
}
```

### 4d. Thêm Style cho BottomSheetDialog (bo góc và nền tối)

📁 `app/src/main/res/values/themes.xml` — thêm vào trước `</resources>`

```xml
<!-- Bottom Sheet Dialog Theme (dark, rounded corners) -->
<style name="BottomSheetDialogTheme" parent="Theme.Material3.DayNight.BottomSheetDialog">
    <item name="bottomSheetStyle">@style/BottomSheetStyle</item>
    <item name="android:windowIsFloating">false</item>
    <item name="android:windowSoftInputMode">adjustResize</item>
</style>

<style name="BottomSheetStyle" parent="Widget.Material3.BottomSheet.Modal">
    <item name="backgroundTint">@color/main_surface</item>
    <item name="shapeAppearanceOverlay">@style/BottomSheetRounded</item>
</style>

<style name="BottomSheetRounded" parent="">
    <item name="cornerFamily">rounded</item>
    <item name="cornerSizeTopLeft">16dp</item>
    <item name="cornerSizeTopRight">16dp</item>
    <item name="cornerSizeBottomLeft">0dp</item>
    <item name="cornerSizeBottomRight">0dp</item>
</style>
```

---

## 5. Checklist

| #   | File                               | Loại       | Vị trí          |
| --- | ---------------------------------- | ---------- | --------------- |
| 1   | `layout_bottom_sheet_add_task.xml` | **NEW**    | `res/layout/`   |
| 2   | `ic_action_date.xml`               | **NEW**    | `res/drawable/` |
| 3   | `ic_action_flag.xml`               | **NEW**    | `res/drawable/` |
| 4   | `ic_action_reminder.xml`           | **NEW**    | `res/drawable/` |
| 5   | `ic_action_more.xml`               | **NEW**    | `res/drawable/` |
| 6   | `ic_action_mic.xml`                | **NEW**    | `res/drawable/` |
| 7   | `ic_action_grid.xml`               | **NEW**    | `res/drawable/` |
| 8   | `ic_action_send.xml`               | **NEW**    | `res/drawable/` |
| 9   | `strings.xml`                      | **MODIFY** | `res/values/`   |
| 10  | `themes.xml`                       | **MODIFY** | `res/values/`   |
| 11  | `MainActivity.java`                | **MODIFY** | `activity/`     |

> 💡 **Lưu ý**: Keyboard sẽ tự động mở khi BottomSheet hiện (do `inputTitle.requestFocus()`), y hệt screenshot TickTick.
