# ✨ Hiển thị Danh sách & Emoji trên Navigation Drawer

> Khi người dùng tạo một danh sách mới với Icon (Emoji) từ màn hình **Thêm Danh sách**, danh sách đó sẽ được hiển thị trên **Navigation Drawer** (Menu trượt bên trái). Document này hướng dẫn cách tùy chỉnh `DrawerMenuItem`.

---

## 📋 Mục lục

1. [Sửa Layout item_drawer_menu.xml](#1-sửa-layout-item_drawer_menuxml)
2. [Sửa Model DrawerMenuItem.java](#2-sửa-model-drawermenuitemjava)
3. [Sửa Adapter DrawerMenuAdapter.java](#3-sửa-adapter-drawermenuadapterjava)
4. [Tích hợp Data tĩnh vào MainActivity.java](#4-tích-hợp-data-tĩnh-vào-mainactivityjava)
5. [Tích hợp Data động (SQLite) khi click Gửi](#5-tích-hợp-data-động-sqlite-khi-click-gửi)

---

## 1. Sửa Layout item_drawer_menu.xml

Cấu trúc hiện tại của `item_drawer_menu.xml` đang dùng `<ImageView>` cho Icon. Để hỗ trợ hiển thị Emoji (dạng text), chúng ta cần thêm một `<TextView>` đè lên hoặc nằm cạnh `ImageView` đó và ẩn hiện linh hoạt.

📁 `app/src/main/res/layout/item_drawer_menu.xml`

Tìm đoạn:

```xml
    <!-- Icon -->
    <ImageView
        android:id="@+id/drawer_item_icon"
        android:layout_width="24dp"
        android:layout_height="24dp"
        android:scaleType="centerInside" />
```

Thay bằng:

```xml
    <!-- Icon Container chứa cả Image và Text(Emoji) -->
    <FrameLayout
        android:layout_width="24dp"
        android:layout_height="24dp">

        <!-- Vector Drawable Icon -->
        <ImageView
            android:id="@+id/drawer_item_icon"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scaleType="centerInside" />

        <!-- Emoji Text Icon -->
        <TextView
            android:id="@+id/drawer_item_emoji"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:gravity="center"
            android:textSize="18sp"
            android:visibility="gone" />
    </FrameLayout>
```

---

## 2. Sửa Model DrawerMenuItem.java

Thêm thuộc tính `emojiIcon` kiểu String để lưu trữ Emoji (VD: "😆", "🎃").

📁 `app/src/main/java/[package_name]/model/DrawerMenuItem.java`

```java
package hcmute.edu.vn.lequanghung_23110110.ticktick.model;

public class DrawerMenuItem {

    public enum ItemType {
        NAVIGATION,  // Hôm nay, Hộp thư đến, Lịch
        LIST,        // Work, Personal, Shopping...
        SEPARATOR    // Đường kẻ phân cách
    }

    private String title;
    private int iconResId;       // Dành cho Vector/Drawable
    private String emojiIcon;    // Dành cho Emoji dạng text (ưu tiên nếu khác null)
    private ItemType type;
    private int badgeCount;
    private boolean hasChevron;
    private boolean selected;

    // Constructor cũ: cho icon drawable (Mặc định)
    public DrawerMenuItem(String title, int iconResId, ItemType type) {
        this.title = title;
        this.iconResId = iconResId;
        this.type = type;
        this.emojiIcon = null; // null nghĩa là dùng iconResId
    }

    // Constructor mới: cho emoji icon (dạng text)
    public DrawerMenuItem(String title, String emojiIcon, ItemType type) {
        this.title = title;
        this.emojiIcon = emojiIcon;
        this.type = type;
        this.iconResId = 0; // 0 nghĩa là không dùng Drawable
    }

    // Constructor cho separator
    public static DrawerMenuItem separator() {
        return new DrawerMenuItem("", 0, ItemType.SEPARATOR);
    }

    // --- Getters & Setters bổ sung ---
    public String getEmojiIcon() {
        return emojiIcon;
    }

    // ... Giữ nguyên các getter/setter cũ của badge, chevron, title...
}
```

---

## 3. Sửa Adapter DrawerMenuAdapter.java

Cập nhật `onBindViewHolder` để hiển thị `drawer_item_emoji` nếu đối tượng truyền vào chứa chuỗi Emoji, ngược lại hiển thị `drawer_item_icon` với Drawable ID.

📁 `app/src/main/java/[package_name]/adapter/DrawerMenuAdapter.java`

Tìm class `MenuItemViewHolder` và sửa:

```java
    class MenuItemViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView emojiIcon; // Thêm dòng này
        TextView title;
        TextView badge;
        ImageView chevron;
        View itemContainer;

        MenuItemViewHolder(View itemView) {
            super(itemView);
            itemContainer = itemView.findViewById(R.id.drawer_item_container);
            icon = itemView.findViewById(R.id.drawer_item_icon);
            emojiIcon = itemView.findViewById(R.id.drawer_item_emoji); // Tìm id
            title = itemView.findViewById(R.id.drawer_item_title);
            badge = itemView.findViewById(R.id.drawer_item_badge);
            chevron = itemView.findViewById(R.id.drawer_item_chevron);
        }

        void bind(DrawerMenuItem item, int position) {
            // Xử lý Icon hiển thị: ưu tiên Emoji, nếu không có thì dùng Vector Drawables
            if (item.getEmojiIcon() != null && !item.getEmojiIcon().isEmpty()) {
                // Hiển thị dạng Text (Emoji)
                emojiIcon.setText(item.getEmojiIcon());
                emojiIcon.setVisibility(View.VISIBLE);
                icon.setVisibility(View.GONE);
            } else if (item.getIconResId() != 0) {
                // Hiển thị Drawable
                icon.setImageResource(item.getIconResId());
                icon.setVisibility(View.VISIBLE);
                emojiIcon.setVisibility(View.GONE);
            } else {
                // Không có icon nào cả
                icon.setVisibility(View.GONE);
                emojiIcon.setVisibility(View.GONE);
            }

            // Title
            title.setText(item.getTitle());

            // ... Giữ nguyên phần Badge, Chevron, Selected State cũ...
```

---

## 4. Tích hợp Data tĩnh vào MainActivity.java

Khi thiết lập dữ liệu `setupDrawer()`, bạn có thể thêm trực tiếp danh sách mới bằng Constructor chứa Emoji.

Ví dụ trong `MainActivity.java`:

```java
private void setupDrawer() {
    // ...
    menuItems.add(new DrawerMenuItem(getString(R.string.drawer_today), R.drawable.ic_today, DrawerMenuItem.ItemType.NAVIGATION).setBadgeCount(2));
    menuItems.add(new DrawerMenuItem(getString(R.string.drawer_inbox), R.drawable.ic_inbox, DrawerMenuItem.ItemType.NAVIGATION));

    menuItems.add(DrawerMenuItem.separator());

    // --- CÁC DANH SÁCH LIST DÙNG EMOJI DỰA TRÊN HÌNH ẢNH ---
    menuItems.add(new DrawerMenuItem("Một h", "😆", DrawerMenuItem.ItemType.LIST).setBadgeCount(1));
    menuItems.add(new DrawerMenuItem("Test", "🎃", DrawerMenuItem.ItemType.LIST).setBadgeCount(1));
    menuItems.add(new DrawerMenuItem("Work", "💼", DrawerMenuItem.ItemType.LIST).setBadgeCount(1));

    // ...
}
```

---

## 5. Tích hợp Data động (SQLite) khi click Gửi

Trong `AddListDialogFragment.java`, sau khi lấy giá trị Tên và Emoji từ giao diện:

```java
// Trong AddListDialogFragment - Nút Lưu
view.findViewById(R.id.btn_save).setOnClickListener(v -> {
    String name = inputName.getText().toString().trim();
    String emoji = emojiDisplay.getText().toString(); // "😆"

    // TODO: Lưu vào SQLite gồm (Tên, Mã màu, Emoji)

    // Notify Main Activity chèn item vào DrawerMenuAdapter
    // Gợi ý: ((MainActivity) getActivity()).addNewListToDrawer(name, emoji);

    dismiss();
});
```

Tiếp đó trong `MainActivity.java`:

```java
public void addNewListToDrawer(String name, String emojiIcon) {
    // Thêm danh sách vào cuối Navigation Drawer
    DrawerMenuItem newItem = new DrawerMenuItem(name, emojiIcon, DrawerMenuItem.ItemType.LIST);
    menuItems.add(newItem);

    // Cập nhật giao diện của RecyclerView Drawer
    drawerMenuAdapter.notifyItemInserted(menuItems.size() - 1);
}
```

---

## ✅ Tóm tắt nhanh:

1. Đổi `<ImageView>` thành nhóm `FrameLayout` chứa `<ImageView>` và `<TextView>` trong `item_drawer_menu.xml`.
2. Hỗ trợ truyền `String emojiIcon` vào class `DrawerMenuItem`.
3. Cập nhật `DrawerMenuAdapter` kiểm tra `getEmojiIcon() != null` thì gán chữ vào `<TextView>`, ngược lại dùng `<ImageView>` resource integer.
4. Trả kết quả mới gọi Update `RecyclerView`.
