# 🎯 Hiển thị Icon danh sách trên Toolbar Header

> Khi chọn danh sách trong Drawer, toolbar sẽ hiển thị **icon + tên** danh sách tương ứng.
> Ví dụ: nhấn "Work" → toolbar hiện 💼 **Work**, nhấn "Personal" → 👤 **Personal**

---

## 📋 Mục lục

1. [Sửa Layout — Toolbar trong activity_main.xml](#1-sửa-layout--toolbar-trong-activity_mainxml)
2. [Thêm field `iconResId` vào DrawerMenuItem](#2-thêm-field-iconresid-mapping)
3. [Sửa MainActivity.java](#3-sửa-mainactivityjava)
4. [Sửa Database — thêm method lấy icon](#4-sửa-database--thêm-method-lấy-icon)

---

## 1. Sửa Layout — Toolbar trong activity_main.xml

📁 `app/src/main/res/layout/activity_main.xml`

Tìm đoạn Toolbar bên trong `AppBarLayout`, **thay nội dung bên trong thẻ `<Toolbar>`**:

```xml
<!-- TRƯỚC (chỉ có TextView) -->
<androidx.appcompat.widget.Toolbar
    android:id="@+id/toolbar"
    android:layout_width="match_parent"
    android:layout_height="56dp"
    android:background="@color/main_bg"
    app:navigationIcon="@drawable/ic_menu_hamburger"
    app:titleTextColor="@color/main_text_primary">

    <TextView
        android:id="@+id/toolbar_title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/today"
        android:textColor="@color/main_text_primary"
        android:textSize="20sp"
        android:textStyle="bold" />

</androidx.appcompat.widget.Toolbar>
```

```xml
<!-- SAU (thêm ImageView cho icon danh sách) -->
<androidx.appcompat.widget.Toolbar
    android:id="@+id/toolbar"
    android:layout_width="match_parent"
    android:layout_height="56dp"
    android:background="@color/main_bg"
    app:navigationIcon="@drawable/ic_menu_hamburger"
    app:titleTextColor="@color/main_text_primary">

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical">

        <!-- Icon danh sách (ẩn mặc định, hiện khi chọn list) -->
        <ImageView
            android:id="@+id/toolbar_list_icon"
            android:layout_width="22dp"
            android:layout_height="22dp"
            android:layout_marginEnd="8dp"
            android:scaleType="centerInside"
            android:visibility="gone" />

        <!-- Tên danh sách -->
        <TextView
            android:id="@+id/toolbar_title"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/today"
            android:textColor="@color/main_text_primary"
            android:textSize="20sp"
            android:textStyle="bold" />

    </LinearLayout>

</androidx.appcompat.widget.Toolbar>
```

---

## 2. Thêm field iconResId mapping

Hiện tại `DrawerMenuItem` đã có `iconResId`. Khi user click vào item trong Drawer, ta sẽ truyền `iconResId` đó sang toolbar. **Không cần sửa model.**

Tuy nhiên, nếu muốn lấy icon từ DB (theo `icon_name` string → drawable resource), thêm helper method (xem mục 4).

---

## 3. Sửa MainActivity.java

### 3a. Khai báo thêm field

```java
// Thêm import
import android.widget.ImageView;

// Thêm field trong class
private ImageView toolbarListIcon;
```

### 3b. Khởi tạo trong `onCreate()`

Tìm dòng:

```java
toolbarTitle = findViewById(R.id.toolbar_title);
```

Thêm ngay dưới:

```java
toolbarListIcon = findViewById(R.id.toolbar_list_icon);
```

### 3c. Tạo method cập nhật toolbar

Thêm method mới:

```java
/**
 * Cập nhật toolbar: icon + tên danh sách.
 * @param listName  Tên danh sách (vd: "Work")
 * @param iconResId Resource ID icon (vd: R.drawable.ic_work). Truyền 0 nếu không có icon.
 */
private void updateToolbarForList(String listName, int iconResId) {
    toolbarTitle.setText(listName);

    if (iconResId != 0) {
        toolbarListIcon.setImageResource(iconResId);
        toolbarListIcon.setVisibility(View.VISIBLE);
    } else {
        toolbarListIcon.setVisibility(View.GONE);
    }
}
```

### 3d. Sửa `loadTasksForList()` — gọi `updateToolbarForList()`

Tìm trong `loadTasksForList()` dòng:

```java
// Update toolbar title
String listName = dbHelper.getListNameById(listId);
toolbarTitle.setText(listName);
```

Thay bằng:

```java
// Update toolbar title + icon
String listName = dbHelper.getListNameById(listId);
int iconResId = getIconResIdForList(listName);
updateToolbarForList(listName, iconResId);
```

### 3e. Thêm helper method map tên → icon

```java
/**
 * Map tên danh sách → drawable resource ID.
 * Dùng để hiển thị icon trên toolbar khi chọn danh sách.
 */
private int getIconResIdForList(String listName) {
    switch (listName) {
        case "Hôm nay":          return R.drawable.ic_today;
        case "Hộp thư đến":      return R.drawable.ic_inbox;
        case "Work":             return R.drawable.ic_work;
        case "Personal":         return R.drawable.ic_personal;
        case "Shopping":         return R.drawable.ic_shopping;
        case "Learning":         return R.drawable.ic_learning;
        case "Wish List":        return R.drawable.ic_wishlist;
        case "Fitness":          return R.drawable.ic_fitness;
        default:                 return 0; // Không có icon
    }
}
```

### 3f. (Tùy chọn) Cách khác, lấy icon từ Drawer item click

Nếu không muốn dùng switch-case, có thể truyền `iconResId` trực tiếp từ Drawer click event:

```java
// Trong setupDrawer(), sửa click listener:
drawerAdapter.setOnItemClickListener((item, position) -> {
    if (item.getType() == DrawerMenuItem.ItemType.SEPARATOR) return;

    drawerAdapter.setSelectedPosition(position);

    int listId = dbHelper.getListIdByName(item.getTitle());
    if (listId != -1) {
        loadTasksForList(listId, item.getIconResId());  // Truyền thêm iconResId
    }

    drawerLayout.closeDrawer(GravityCompat.START);
});
```

Và sửa signature `loadTasksForList`:

```java
private void loadTasksForList(int listId, int iconResId) {
    currentListId = listId;

    List<TaskModel> tasks = dbHelper.getTasksByListId(listId);
    String listName = dbHelper.getListNameById(listId);

    // Cập nhật toolbar với cả icon
    updateToolbarForList(listName, iconResId);

    // ... phần còn lại giữ nguyên
}

// Overload cho trường hợp không có icon (load mặc định)
private void loadTasksForList(int listId) {
    String listName = dbHelper.getListNameById(listId);
    loadTasksForList(listId, getIconResIdForList(listName));
}
```

> 💡 **Cách này tối ưu hơn** vì không cần switch-case, icon lấy thẳng từ `DrawerMenuItem` đã có sẵn.

---

## 4. Sửa Database — thêm method lấy icon (tùy chọn)

Nếu muốn lấy icon name từ DB (thay vì switch-case), thêm vào `TaskDatabaseHelper.java`:

```java
/** Lấy icon_name theo list_id, rồi convert sang drawable resource */
public int getListIconResId(Context context, int listId) {
    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.query("lists", new String[]{"icon_name"},
            "_id = ?", new String[]{String.valueOf(listId)},
            null, null, null);
    int resId = 0;
    if (cursor.moveToFirst()) {
        String iconName = cursor.getString(0);  // vd: "ic_work"
        resId = context.getResources().getIdentifier(
                iconName, "drawable", context.getPackageName());
    }
    cursor.close();
    return resId;
}
```

Dùng trong `loadTasksForList()`:

```java
int iconResId = dbHelper.getListIconResId(this, listId);
updateToolbarForList(listName, iconResId);
```

> ⚠️ **Lưu ý**: `getIdentifier()` dùng reflection nên chậm hơn switch-case. Chỉ nên dùng nếu danh sách lists là dynamic (user tự tạo).

---

## Checklist

| #   | File                      | Loại              | Thay đổi                                                                  |
| --- | ------------------------- | ----------------- | ------------------------------------------------------------------------- |
| 1   | `activity_main.xml`       | MODIFY            | Thêm `ImageView` (`toolbar_list_icon`) vào Toolbar                        |
| 2   | `MainActivity.java`       | MODIFY            | Thêm `toolbarListIcon`, `updateToolbarForList()`, `getIconResIdForList()` |
| 3   | `TaskDatabaseHelper.java` | MODIFY (tùy chọn) | Thêm `getListIconResId()`                                                 |
