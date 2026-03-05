# 🚀 Popup Menu "Thêm" — Drawer Bottom Bar

> Khi nhấn vào nút `Thêm` (+) ở bottom bar của Drawer, sẽ hiển thị **PopupWindow** trồi lên với 3 lựa chọn:
> **Danh sách** · **Bộ lọc** · **Thẻ**

---

## 📋 Mục lục

1. [Tạo Drawable Icons](#1-tạo-drawable-icons)
2. [Tạo Drawable Background](#2-tạo-drawable-background)
3. [Tạo Layout Popup](#3-tạo-layout-popup)
4. [Sửa MainActivity.java](#4-sửa-mainactivityjava)
5. [Checklist tạo file](#5-checklist-tạo-file)

---

## 1. Tạo Drawable Icons

📁 Tất cả vào `app/src/main/res/drawable/`

### ic_list.xml (Danh sách)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="@color/drawer_item_text">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M3,13h2v-2H3v2zm0,4h2v-2H3v2zm0,-8h2V7H3v2zm4,4h14v-2H7v2zm0,4h14v-2H7v2zM7,7v2h14V7H7z" />
</vector>
```

### ic_tune_slider.xml (Bộ lọc)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="@color/drawer_item_text">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M3,17v2h6v-2H3zM3,5v2h10V5H3zM13,21v-2h8v-2h-8v-2h-2v6H13zM7,9v2H3v2h4v2h2V9H7zM21,13v-2H11v2H21zM15,9h2V7h4V5h-4V3h-2V9z" />
</vector>
```

### ic_tag.xml (Thẻ)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="@color/drawer_item_text">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M20,10V4h-6L4,14l6,6l10,-10zM17,8c-0.55,0 -1,-0.45 -1,-1s0.45,-1 1,-1 1,0.45 1,1 -0.45,1 -1,1z" />
</vector>
```

---

## 2. Tạo Drawable Background

📁 `app/src/main/res/drawable/bg_popup_menu.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/main_card_bg" />
    <corners android:radius="16dp" />
</shape>
```

---

## 3. Tạo Layout Popup

📁 `app/src/main/res/layout/layout_popup_add.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="160dp"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@drawable/bg_popup_menu"
    android:paddingTop="8dp"
    android:paddingBottom="8dp"
    android:elevation="8dp">

    <!-- Danh sách -->
    <LinearLayout
        android:id="@+id/popup_item_list"
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
            android:src="@drawable/ic_list" />
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:text="Danh sách"
            android:textColor="@color/drawer_item_text"
            android:textSize="14sp" />
    </LinearLayout>

    <!-- Bộ lọc -->
    <LinearLayout
        android:id="@+id/popup_item_filter"
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
            android:src="@drawable/ic_tune_slider" />
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:text="Bộ lọc"
            android:textColor="@color/drawer_item_text"
            android:textSize="14sp" />
    </LinearLayout>

    <!-- Thẻ -->
    <LinearLayout
        android:id="@+id/popup_item_tag"
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
            android:src="@drawable/ic_tag" />
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:text="Thẻ"
            android:textColor="@color/drawer_item_text"
            android:textSize="14sp" />
    </LinearLayout>

</LinearLayout>
```

---

## 4. Sửa MainActivity.java

### 4a. Thêm import

```java
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.widget.PopupWindow;
```

### 4b. Sửa `setupDrawer()` — thay Toast bằng popup

Tìm đoạn:

```java
// Bottom bar buttons
findViewById(R.id.drawer_btn_add).setOnClickListener(v ->
        Toast.makeText(this, "Thêm danh sách", Toast.LENGTH_SHORT).show());
```

Thay bằng:

```java
// Bottom bar buttons — mở Popup Menu
findViewById(R.id.drawer_btn_add).setOnClickListener(this::showAddMenuPopup);
```

### 4c. Thêm method `showAddMenuPopup()`

Thêm method mới vào class `MainActivity`:

```java
private void showAddMenuPopup(View anchorView) {
    View popupView = getLayoutInflater().inflate(R.layout.layout_popup_add, null);

    PopupWindow popupWindow = new PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true  // focusable → bấm ngoài sẽ đóng
    );

    // Background transparent để bo góc XML hiển thị đúng
    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    popupWindow.setElevation(8f);

    // Click handlers
    popupView.findViewById(R.id.popup_item_list).setOnClickListener(v -> {
        Toast.makeText(this, "Tạo Danh sách mới", Toast.LENGTH_SHORT).show();
        popupWindow.dismiss();
    });

    popupView.findViewById(R.id.popup_item_filter).setOnClickListener(v -> {
        Toast.makeText(this, "Tạo Bộ lọc mới", Toast.LENGTH_SHORT).show();
        popupWindow.dismiss();
    });

    popupView.findViewById(R.id.popup_item_tag).setOnClickListener(v -> {
        Toast.makeText(this, "Tạo Thẻ mới", Toast.LENGTH_SHORT).show();
        popupWindow.dismiss();
    });

    // Đo kích thước popup → tính offset trồi lên trên nút
    popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
    int popupHeight = popupView.getMeasuredHeight();
    int yOffset = -(anchorView.getHeight() + popupHeight + dpToPx(8));

    popupWindow.showAsDropDown(anchorView, dpToPx(16), yOffset);
}
```

> 💡 **Giải thích `yOffset`**: Popup mặc định hiển thị **bên dưới** nút bấm. Để trồi **lên trên** giống TickTick, ta tính offset âm = chiều cao popup + chiều cao nút + 8dp padding.

---

## 5. Checklist tạo file

| #   | File                   | Loại   | Vị trí          |
| --- | ---------------------- | ------ | --------------- |
| 1   | `ic_list.xml`          | NEW    | `res/drawable/` |
| 2   | `ic_tune_slider.xml`   | NEW    | `res/drawable/` |
| 3   | `ic_tag.xml`           | NEW    | `res/drawable/` |
| 4   | `bg_popup_menu.xml`    | NEW    | `res/drawable/` |
| 5   | `layout_popup_add.xml` | NEW    | `res/layout/`   |
| 6   | `MainActivity.java`    | MODIFY | `activity/`     |
