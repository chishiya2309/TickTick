# Hướng dẫn Hiển thị Chức năng khi Nhấn giữ Danh sách

Mục tiêu: Khi người dùng nhấn giữ (long-click) vào một danh sách tự tạo (tức là không phải "Hôm nay" hay "Hộp thư đến"), một Popup Menu nhỏ sẽ hiện ra với các tùy chọn:

- Chỉnh sửa
- Đính ghim
- Xóa

Dưới đây là các bước để triển khai chức năng này vào source code hiện tại:

## 1. Tạo Layout cho Custom Popup Menu (Mới)

Lý do tạo Custom Layout thay vì dùng `PopupMenu` mặc định của Android là để bạn có thể tùy biến giao diện giống y hệt như trong hình vẽ thiết kế (icon bo góc, màu sắc background dark, ...).

Tạo file mới `res/layout/layout_popup_list_options.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="180dp"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@drawable/bg_bottom_sheet"
    android:paddingTop="8dp"
    android:paddingBottom="8dp"
    android:elevation="8dp">

    <!-- Chỉnh sửa -->
    <LinearLayout
        android:id="@+id/popup_action_edit"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:background="?attr/selectableItemBackground"
        android:paddingStart="16dp"
        android:paddingEnd="16dp">

        <ImageView
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_edit"
            android:contentDescription="Chỉnh sửa" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:text="Chỉnh sửa"
            android:textColor="@color/main_text_primary"
            android:textSize="15sp" />
    </LinearLayout>

    <!-- Đính ghim -->
    <LinearLayout
        android:id="@+id/popup_action_pin"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:background="?attr/selectableItemBackground"
        android:paddingStart="16dp"
        android:paddingEnd="16dp">

        <ImageView
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_pin"
            android:contentDescription="Đính ghim" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:text="Đính ghim"
            android:textColor="@color/main_text_primary"
            android:textSize="15sp" />
    </LinearLayout>

    <!-- Phân cách (Tùy chọn) -->
    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/divider"
        android:layout_marginTop="4dp"
        android:layout_marginBottom="4dp"/>

    <!-- Xóa -->
    <LinearLayout
        android:id="@+id/popup_action_delete"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:background="?attr/selectableItemBackground"
        android:paddingStart="16dp"
        android:paddingEnd="16dp">

        <ImageView
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_delete"
            app:tint="@color/red_delete"
            android:contentDescription="Xóa" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:text="Xóa"
            android:textColor="@color/red_delete"
            android:textSize="15sp" />
    </LinearLayout>

</LinearLayout>
```

> **Ghi chú**: Đảm bảo rằng bạn đã tạo các Drawable Vector tương ứng như `ic_edit.xml`, `ic_pin.xml`, `ic_delete.xml` và set màu đỏ cho chữ Xóa (`@color/red_delete`).

## 2. Cập nhật `DrawerMenuAdapter.java`

Bạn cần bổ sung interface để báo cáo khi một Item bị Long-Click (Nhấn giữ) từ Adapter về cho `MainActivity`.

**Bổ sung Interface:**

```java
    public interface OnItemClickListener {
        void onItemClick(DrawerMenuItem item, int position);
        // THÊM: Callback cho Long Click
        void onItemLongClick(DrawerMenuItem item, int position, View anchorView);
    }
```

**Gọi event Long Click trong `onBindViewHolder()`:**

Trong hàm `bind` của `MenuItemViewHolder`:

```java
            // Click ngắn
            itemContainer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item, position);
                }
            });

            // THÊM: Long Click (Nhấn giữ)
            itemContainer.setOnLongClickListener(v -> {
                if (listener != null && item.getType() == DrawerMenuItem.ItemType.LIST) {
                    listener.onItemLongClick(item, position, v); // Truyền view làm mốc mỏ neo để hiển thị Popup
                    return true; // Tiêu thụ sự kiện, không để nó lan truyền thành Click thường
                }
                return false;
            });
```

_(Lưu ý: Chỉ bắt sự kiện Long Click cho các phần tử có `type` là `ItemType.LIST`)_

## 3. Cập nhật `MainActivity.java`

Trong `MainActivity`, bạn bắt lấy sự kiện `onItemLongClick` và hiển thị một `PopupWindow` neo vào View của item đó.

**Thêm thư viện cần thiết ở đầu class:**

```java
import android.widget.PopupWindow;
import android.graphics.drawable.ColorDrawable;
```

**Cập nhật phần `setupDrawer()`:**

```java
        drawerAdapter.setOnItemClickListener(new DrawerMenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DrawerMenuItem item, int position) {
                if (item.getType() == DrawerMenuItem.ItemType.SEPARATOR) return;
                drawerAdapter.setSelectedPosition(position);
                int listId = dbHelper.getListIdByName(item.getTitle());
                if (listId != -1) {
                    loadTasksForList(listId, item.getIconResId(), item.getEmojiIcon());
                }
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            // THÊM: Xử lý sự kiện nhấn giữ
            @Override
            public void onItemLongClick(DrawerMenuItem item, int position, View anchorView) {
                // Kiểm tra loại trừ "Hôm nay" và "Hộp thư đến" (Navigation)
                if(item.getType() == DrawerMenuItem.ItemType.LIST) {
                    showListContextMenu(anchorView, item, position);
                }
            }
        });
```

**Bổ sung hàm `showListContextMenu()`:**

```java
    private void showListContextMenu(View anchorView, DrawerMenuItem item, int position) {
        View popupView = getLayoutInflater().inflate(R.layout.layout_popup_list_options, null);

        // Kích thước Popup (ví dụ 180dp x wrap_content)
        PopupWindow popupWindow = new PopupWindow(popupView,
                dpToPx(180),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true); // 'true' để có outside-touch-dismiss

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(8f);

        // Bắt sự kiện click các nút chức năng
        popupView.findViewById(R.id.popup_action_edit).setOnClickListener(v -> {
            popupWindow.dismiss();
            Toast.makeText(this, "Chỉnh sửa: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Mở Dialog sửa danh sách
        });

        popupView.findViewById(R.id.popup_action_pin).setOnClickListener(v -> {
            popupWindow.dismiss();
            Toast.makeText(this, "Đính ghim: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        });

        popupView.findViewById(R.id.popup_action_delete).setOnClickListener(v -> {
            popupWindow.dismiss();
            Toast.makeText(this, "Xóa: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Hiện Dialog Xác Nhận Xóa, sau đó DataBase Delete và Reload ListView
        });

        // Vị trí xuất hiện: Có thể showDropDrown ngay bên dưới view
        // hoặc showAsDropDown với offsetX tùy vào giao diện bạn muốn.
        // Ở đây hiển thị lùi vào phải 32dp và chệch lên trên xíu để che đi phần bottom của item
        popupWindow.showAsDropDown(anchorView, dpToPx(32), -dpToPx(24));
    }
```

Với cấu trúc như trên, khi người dùng giữ tay lên một Custom List bất kì trong Drawer, một Dropdown Menu đẹp mắt mang giao diện Custom sẽ nảy lên để cho phép Thao Tác (Edit, Pin, Delete)!
