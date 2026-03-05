# 🎨 Navigation Drawer (Hamburger Menu) — TickTick Clone

> Hướng dẫn triển khai giao diện Navigation Drawer giống TickTick.
> Sử dụng **DrawerLayout + Custom View + RecyclerView** (không dùng NavigationView mặc định) để có thể tùy chỉnh icon, badge, chevron cho mỗi item.

---

## 📋 Mục lục

1. [Tổng quan kiến trúc](#1-tổng-quan-kiến-trúc)
2. [Thêm Colors](#2-thêm-colors)
3. [Thêm Strings](#3-thêm-strings)
4. [Tạo Drawable Icons](#4-tạo-drawable-icons)
5. [Tạo Drawable Background](#5-tạo-drawable-background)
6. [Tạo Model — DrawerMenuItem](#6-tạo-model--drawermenuitem)
7. [Tạo Adapter — DrawerMenuAdapter](#7-tạo-adapter--drawermenuadapter)
8. [Tạo Layout — item_drawer_menu.xml](#8-tạo-layout--item_drawer_menuxml)
9. [Tạo Layout — layout_drawer_content.xml](#9-tạo-layout--layout_drawer_contentxml)
10. [Sửa Layout — activity_main.xml](#10-sửa-layout--activity_mainxml)
11. [Sửa Java — MainActivity.java](#11-sửa-java--mainactivityjava)

---

## 1. Tổng quan kiến trúc

```
DrawerLayout (root)
├── CoordinatorLayout (main content — giữ nguyên)
│   ├── AppBarLayout + Toolbar
│   ├── RecyclerView (task list)
│   ├── FAB
│   └── BottomNavigationView
└── LinearLayout (drawer content — gravity="start")
    ├── Header (avatar + tên + icons)
    ├── ScrollView
    │   ├── Navigation Items (Hôm nay, Hộp thư đến, Lịch)
    │   ├── Divider
    │   └── List Items (Work, Personal, Shopping...)  ← RecyclerView + Custom Adapter
    └── Bottom Bar ("Thêm" + filter icon)
```

**Tại sao dùng Custom Adapter thay vì NavigationView?**

- ✅ Mỗi item có thể có icon khác nhau (emoji, vector, image)
- ✅ Badge count tùy chỉnh (kiểu TickTick)
- ✅ Chevron `>` cho một số item
- ✅ Selected state với rounded background
- ✅ Dễ thêm/xóa/sắp xếp lại items dynamically

---

## 2. Thêm Colors

📁 `app/src/main/res/values/colors.xml` — **thêm vào cuối trước `</resources>`**

```xml
<!-- Navigation Drawer Colors -->
<color name="drawer_bg">#1E1E1E</color>
<color name="drawer_header_bg">#1E1E1E</color>
<color name="drawer_item_selected_bg">#2A3A5C</color>
<color name="drawer_item_text">#E0E0E0</color>
<color name="drawer_item_text_selected">#FFFFFF</color>
<color name="drawer_icon_tint">#B0B0B0</color>
<color name="drawer_badge_bg">#4C6FE0</color>
<color name="drawer_badge_text">#FFFFFF</color>
<color name="drawer_separator">#333333</color>
<color name="drawer_bottom_bar_bg">#1A1A1A</color>
```

---

## 3. Thêm Strings

📁 `app/src/main/res/values/strings.xml` — **thêm vào cuối trước `</resources>`**

```xml
<!-- Drawer -->
<string name="drawer_user_name">Hung Le</string>
<string name="drawer_today">Hôm nay</string>
<string name="drawer_inbox">Hộp thư đến</string>
<string name="drawer_calendar_subscribed">Đã đăng ký Lịch</string>
<string name="drawer_work">Work</string>
<string name="drawer_personal">Personal</string>
<string name="drawer_shopping">Shopping</string>
<string name="drawer_learning">Learning</string>
<string name="drawer_wishlist">Wish List</string>
<string name="drawer_fitness">Fitness</string>
<string name="drawer_add">Thêm</string>
<string name="drawer_search">Tìm kiếm</string>
<string name="drawer_settings">Cài đặt</string>
```

---

## 4. Tạo Drawable Icons

📁 Tất cả vào `app/src/main/res/drawable/`

### ic_today.xml (Calendar — Hôm nay)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#4C6FE0"
        android:pathData="M19,3h-1V1h-2v2H8V1H6v2H5C3.9,3 3,3.9 3,5v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM19,19H5V8h14V19zM9,10H7v2h2V10zM13,10h-2v2h2V10zM17,10h-2v2h2V10z" />
</vector>
```

### ic_inbox.xml (Inbox — Hộp thư đến)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#B0B0B0"
        android:pathData="M19,3H4.99C3.89,3 3,3.9 3,5v14c0,1.1 0.89,2 1.99,2H19c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM19,15h-4c0,1.66 -1.34,3 -3,3s-3,-1.34 -3,-3H5V5h14V15z" />
</vector>
```

### ic_calendar_subscribed.xml (Calendar check — Đã đăng ký Lịch)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#B0B0B0"
        android:pathData="M19,3h-1V1h-2v2H8V1H6v2H5C3.9,3 3,3.9 3,5v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM19,19H5V8h14V19zM16.53,10.06l-1.06,-1.06L11,13.47l-2.47,-2.47l-1.06,1.06L11,15.59L16.53,10.06z" />
</vector>
```

### ic_work.xml (Briefcase — Work)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#4C6FE0"
        android:pathData="M20,6h-4V4c0,-1.11 -0.89,-2 -2,-2h-4c-1.11,0 -2,0.89 -2,2v2H4c-1.11,0 -1.99,0.89 -1.99,2L2,19c0,1.11 0.89,2 2,2h16c1.11,0 2,-0.89 2,-2V8C22,6.89 21.11,6 20,6zM14,6h-4V4h4V6z" />
</vector>
```

### ic_personal.xml (Person — Personal)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#66BB6A"
        android:pathData="M12,12c2.21,0 4,-1.79 4,-4s-1.79,-4 -4,-4 -4,1.79 -4,4 1.79,4 4,4zM12,14c-2.67,0 -8,1.34 -8,4v2h16v-2c0,-2.66 -5.33,-4 -8,-4z" />
</vector>
```

### ic_shopping.xml (Shopping bag — Shopping)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFA726"
        android:pathData="M18,6h-2c0,-2.21 -1.79,-4 -4,-4S8,3.79 8,6H6C4.9,6 4,6.9 4,8v12c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V8C20,6.9 19.1,6 18,6zM12,4c1.1,0 2,0.9 2,2h-4C10,4.9 10.9,4 12,4zM18,20H6V8h2v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1V8h4v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1V8h2V20z" />
</vector>
```

### ic_learning.xml (Book — Learning)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#42A5F5"
        android:pathData="M18,2H6c-1.1,0 -2,0.9 -2,2v16c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V4C20,2.9 19.1,2 18,2zM6,4h5v8l-2.5,-1.5L6,12V4z" />
</vector>
```

### ic_wishlist.xml (Heart — Wish List)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#EF5350"
        android:pathData="M12,21.35l-1.45,-1.32C5.4,15.36 2,12.28 2,8.5 2,5.42 4.42,3 7.5,3c1.74,0 3.41,0.81 4.5,2.09C13.09,3.81 14.76,3 16.5,3 19.58,3 22,5.42 22,8.5c0,3.78 -3.4,6.86 -8.55,11.54L12,21.35z" />
</vector>
```

### ic_fitness.xml (Flame — Fitness)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FF7043"
        android:pathData="M13.5,0.67s0.74,2.65 0.74,4.8c0,2.06 -1.35,3.73 -3.41,3.73 -2.07,0 -3.63,-1.67 -3.63,-3.73l0.03,-0.36C5.21,7.51 4,10.62 4,14c0,4.42 3.58,8 8,8s8,-3.58 8,-8C20,8.61 17.41,3.8 13.5,0.67zM11.71,19c-1.78,0 -3.22,-1.4 -3.22,-3.14 0,-1.62 1.05,-2.76 2.81,-3.12 1.77,-0.36 3.6,-1.21 4.62,-2.58 0.39,1.29 0.59,2.65 0.59,4.04 0,2.65 -2.15,4.8 -4.8,4.8z" />
</vector>
```

### ic_search.xml (Search)

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
        android:pathData="M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5zM9.5,14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z" />
</vector>
```

### ic_settings_outline.xml (Settings gear)

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
        android:pathData="M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z" />
</vector>
```

### ic_add_list.xml (Add/Plus)

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
        android:pathData="M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" />
</vector>
```

### ic_filter.xml (Tune/Filter)

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
        android:pathData="M3,17v2h6v-2H3zM3,5v2h10V5H3zM13,21v-2h8v-2h-8v-2h-2v6H13zM7,9v2H3v2h4v2h2V9H7zM21,13v-2H11v2H21zM15,9h2V7h4V5h-4V3h-2V9z" />
</vector>
```

### ic_chevron_right.xml (Mũi tên phải `>`)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#666666">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M10,6L8.59,7.41 13.17,12l-4.58,4.59L10,18l6,-6z" />
</vector>
```

---

## 5. Tạo Drawable Background

📁 `app/src/main/res/drawable/bg_drawer_item_selected.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/drawer_item_selected_bg" />
    <corners android:radius="12dp" />
</shape>
```

📁 `app/src/main/res/drawable/bg_avatar_circle.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#555555" />
    <size android:width="40dp" android:height="40dp" />
</shape>
```

---

## 6. Tạo Model — DrawerMenuItem

📁 `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/model/DrawerMenuItem.java`

> **Giải thích**: Model chứa thông tin cho mỗi item trong drawer.
> Có thể set `iconResId` cho vector drawable, `badgeCount` cho badge, `hasChevron` cho mũi tên phải.

```java
package hcmute.edu.vn.lequanghung_23110110.ticktick.model;

public class DrawerMenuItem {

    public enum ItemType {
        NAVIGATION,  // Hôm nay, Hộp thư đến, Lịch
        LIST,        // Work, Personal, Shopping...
        SEPARATOR    // Đường kẻ phân cách
    }

    private String title;
    private int iconResId;
    private ItemType type;
    private int badgeCount;
    private boolean hasChevron;
    private boolean selected;

    // Constructor cho item bình thường
    public DrawerMenuItem(String title, int iconResId, ItemType type) {
        this.title = title;
        this.iconResId = iconResId;
        this.type = type;
    }

    // Constructor cho separator
    public static DrawerMenuItem separator() {
        DrawerMenuItem item = new DrawerMenuItem("", 0, ItemType.SEPARATOR);
        return item;
    }

    // --- Getters & Setters ---

    public String getTitle() { return title; }
    public int getIconResId() { return iconResId; }
    public ItemType getType() { return type; }

    public int getBadgeCount() { return badgeCount; }
    public DrawerMenuItem setBadgeCount(int count) {
        this.badgeCount = count;
        return this;
    }

    public boolean hasChevron() { return hasChevron; }
    public DrawerMenuItem setHasChevron(boolean hasChevron) {
        this.hasChevron = hasChevron;
        return this;
    }

    public boolean isSelected() { return selected; }
    public DrawerMenuItem setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }
}
```

---

## 7. Tạo Adapter — DrawerMenuAdapter

📁 `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/adapter/DrawerMenuAdapter.java`

> **Giải thích Custom Adapter**:
>
> - `getItemViewType()` trả về loại item (NAVIGATION, LIST, SEPARATOR) → inflate layout khác nhau
> - `onBindViewHolder()` handle hiển thị icon, text, badge, chevron, selected state
> - Click listener cho mỗi item → callback lên Activity

```java
package hcmute.edu.vn.lequanghung_23110110.ticktick.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.DrawerMenuItem;

public class DrawerMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_SEPARATOR = 1;

    private final List<DrawerMenuItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DrawerMenuItem item, int position);
    }

    public DrawerMenuAdapter(List<DrawerMenuItem> items) {
        this.items = items;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType() == DrawerMenuItem.ItemType.SEPARATOR
                ? VIEW_TYPE_SEPARATOR : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SEPARATOR) {
            View view = inflater.inflate(R.layout.item_drawer_separator, parent, false);
            return new SeparatorViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_drawer_menu, parent, false);
            return new MenuItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MenuItemViewHolder) {
            ((MenuItemViewHolder) holder).bind(items.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // === ViewHolder cho menu item ===
    class MenuItemViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView badge;
        ImageView chevron;
        View itemContainer;

        MenuItemViewHolder(View itemView) {
            super(itemView);
            itemContainer = itemView.findViewById(R.id.drawer_item_container);
            icon = itemView.findViewById(R.id.drawer_item_icon);
            title = itemView.findViewById(R.id.drawer_item_title);
            badge = itemView.findViewById(R.id.drawer_item_badge);
            chevron = itemView.findViewById(R.id.drawer_item_chevron);
        }

        void bind(DrawerMenuItem item, int position) {
            // Icon
            if (item.getIconResId() != 0) {
                icon.setImageResource(item.getIconResId());
                icon.setVisibility(View.VISIBLE);
            } else {
                icon.setVisibility(View.GONE);
            }

            // Title
            title.setText(item.getTitle());

            // Badge
            if (item.getBadgeCount() > 0) {
                badge.setText(String.valueOf(item.getBadgeCount()));
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setVisibility(View.GONE);
            }

            // Chevron
            chevron.setVisibility(item.hasChevron() ? View.VISIBLE : View.GONE);

            // Selected state
            if (item.isSelected()) {
                itemContainer.setBackgroundResource(R.drawable.bg_drawer_item_selected);
                title.setTextColor(itemContainer.getContext().getColor(R.color.drawer_item_text_selected));
            } else {
                itemContainer.setBackground(null);
                title.setTextColor(itemContainer.getContext().getColor(R.color.drawer_item_text));
            }

            // Click
            itemContainer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item, position);
                }
            });
        }
    }

    // === ViewHolder cho separator ===
    static class SeparatorViewHolder extends RecyclerView.ViewHolder {
        SeparatorViewHolder(View itemView) {
            super(itemView);
        }
    }

    // === Public method: update selected item ===
    public void setSelectedPosition(int position) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setSelected(i == position);
        }
        notifyDataSetChanged();
    }
}
```

---

## 8. Tạo Layout — item_drawer_menu.xml

📁 `app/src/main/res/layout/item_drawer_menu.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/drawer_item_container"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:paddingStart="16dp"
    android:paddingEnd="16dp"
    android:layout_marginStart="8dp"
    android:layout_marginEnd="8dp"
    android:layout_marginTop="2dp"
    android:layout_marginBottom="2dp"
    android:clickable="true"
    android:focusable="true">

    <!-- Icon -->
    <ImageView
        android:id="@+id/drawer_item_icon"
        android:layout_width="24dp"
        android:layout_height="24dp"
        android:scaleType="centerInside" />

    <!-- Title -->
    <TextView
        android:id="@+id/drawer_item_title"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:layout_marginStart="16dp"
        android:textColor="@color/drawer_item_text"
        android:textSize="15sp"
        android:singleLine="true" />

    <!-- Badge (count) -->
    <TextView
        android:id="@+id/drawer_item_badge"
        android:layout_width="wrap_content"
        android:layout_height="20dp"
        android:minWidth="20dp"
        android:gravity="center"
        android:paddingStart="6dp"
        android:paddingEnd="6dp"
        android:background="@drawable/bg_drawer_badge"
        android:textColor="@color/drawer_badge_text"
        android:textSize="11sp"
        android:textStyle="bold"
        android:visibility="gone" />

    <!-- Chevron (>) -->
    <ImageView
        android:id="@+id/drawer_item_chevron"
        android:layout_width="20dp"
        android:layout_height="20dp"
        android:src="@drawable/ic_chevron_right"
        android:scaleType="centerInside"
        android:visibility="gone" />

</LinearLayout>
```

### Thêm badge background drawable

📁 `app/src/main/res/drawable/bg_drawer_badge.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/drawer_badge_bg" />
    <corners android:radius="10dp" />
</shape>
```

### Separator layout

📁 `app/src/main/res/layout/item_drawer_separator.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<View xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="1dp"
    android:layout_marginTop="8dp"
    android:layout_marginBottom="8dp"
    android:layout_marginStart="16dp"
    android:layout_marginEnd="16dp"
    android:background="@color/drawer_separator" />
```

---

## 9. Tạo Layout — layout_drawer_content.xml

📁 `app/src/main/res/layout/layout_drawer_content.xml`

> Đây là toàn bộ content của drawer (bên trái). Gồm header + RecyclerView + bottom bar.

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="300dp"
    android:layout_height="match_parent"
    android:layout_gravity="start"
    android:orientation="vertical"
    android:background="@color/drawer_bg"
    android:fitsSystemWindows="true">

    <!-- ===== HEADER: Avatar + Name + Icons ===== -->
    <LinearLayout
        android:id="@+id/drawer_header"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingStart="16dp"
        android:paddingEnd="12dp"
        android:paddingTop="16dp"
        android:paddingBottom="16dp"
        android:background="@color/drawer_header_bg">

        <!-- Avatar -->
        <ImageView
            android:id="@+id/drawer_avatar"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:background="@drawable/bg_avatar_circle"
            android:scaleType="centerCrop"
            android:contentDescription="Avatar" />

        <!-- User name -->
        <TextView
            android:id="@+id/drawer_user_name"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginStart="12dp"
            android:text="@string/drawer_user_name"
            android:textColor="@color/main_text_primary"
            android:textSize="16sp"
            android:textStyle="bold" />

        <!-- Search icon -->
        <ImageView
            android:id="@+id/drawer_btn_search"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:padding="6dp"
            android:src="@drawable/ic_search"
            android:scaleType="centerInside"
            android:contentDescription="@string/drawer_search"
            android:clickable="true"
            android:focusable="true" />

        <!-- Settings icon -->
        <ImageView
            android:id="@+id/drawer_btn_settings"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:padding="6dp"
            android:src="@drawable/ic_settings_outline"
            android:scaleType="centerInside"
            android:contentDescription="@string/drawer_settings"
            android:clickable="true"
            android:focusable="true" />

    </LinearLayout>

    <!-- ===== MENU ITEMS (RecyclerView) ===== -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/drawer_recycler_view"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:paddingTop="8dp"
        android:clipToPadding="false"
        android:overScrollMode="never" />

    <!-- ===== BOTTOM BAR: Thêm + Filter ===== -->
    <LinearLayout
        android:id="@+id/drawer_bottom_bar"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingStart="16dp"
        android:paddingEnd="12dp"
        android:background="@color/drawer_bottom_bar_bg">

        <!-- "Thêm" icon + text -->
        <ImageView
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_add_list"
            android:scaleType="centerInside" />

        <TextView
            android:id="@+id/drawer_btn_add"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginStart="12dp"
            android:text="@string/drawer_add"
            android:textColor="@color/drawer_item_text"
            android:textSize="14sp" />

        <!-- Filter icon -->
        <ImageView
            android:id="@+id/drawer_btn_filter"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:padding="6dp"
            android:src="@drawable/ic_filter"
            android:scaleType="centerInside"
            android:clickable="true"
            android:focusable="true" />

    </LinearLayout>

</LinearLayout>
```

---

## 10. Sửa Layout — activity_main.xml

📁 `app/src/main/res/layout/activity_main.xml`

> **Thay toàn bộ file** — Wrap CoordinatorLayout bên trong DrawerLayout, thêm `<include>` cho drawer content.

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true"
    tools:context=".activity.MainActivity">

    <!-- ========== MAIN CONTENT (giữ nguyên) ========== -->
    <androidx.coordinatorlayout.widget.CoordinatorLayout
        android:id="@+id/main"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@color/main_bg">

        <!-- TOP TOOLBAR -->
        <com.google.android.material.appbar.AppBarLayout
            android:id="@+id/app_bar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@color/main_bg"
            app:elevation="0dp">

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

        </com.google.android.material.appbar.AppBarLayout>

        <!-- TASK LIST -->
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/task_recycler_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="@color/main_bg"
            android:clipToPadding="false"
            android:paddingBottom="80dp"
            app:layout_behavior="@string/appbar_scrolling_view_behavior"
            tools:listitem="@layout/item_task" />

        <!-- FAB -->
        <com.google.android.material.floatingactionbutton.FloatingActionButton
            android:id="@+id/fab_add_task"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="bottom|end"
            android:layout_marginEnd="20dp"
            android:layout_marginBottom="72dp"
            android:contentDescription="@string/add_task"
            android:src="@android:drawable/ic_input_add"
            app:backgroundTint="@color/main_accent_blue"
            app:elevation="6dp"
            app:fabSize="normal"
            app:tint="@color/white" />

        <!-- BOTTOM NAV -->
        <com.google.android.material.bottomnavigation.BottomNavigationView
            android:id="@+id/bottom_navigation"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="bottom"
            android:background="@color/main_bottom_nav_bg"
            app:itemIconTint="@color/bottom_nav_icon_color"
            app:itemTextColor="@color/bottom_nav_icon_color"
            app:labelVisibilityMode="unlabeled"
            app:menu="@menu/bottom_nav_menu" />

    </androidx.coordinatorlayout.widget.CoordinatorLayout>

    <!-- ========== DRAWER CONTENT ========== -->
    <include layout="@layout/layout_drawer_content" />

</androidx.drawerlayout.widget.DrawerLayout>
```

---

## 11. Sửa Java — MainActivity.java

📁 `app/src/main/java/.../activity/MainActivity.java`

> **Thay toàn bộ file** — Thêm drawer setup, xử lý hamburger click, back press, menu items click.

```java
package hcmute.edu.vn.lequanghung_23110110.ticktick.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.DrawerMenuAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.TaskAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.DrawerMenuItem;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

public class MainActivity extends AppCompatActivity {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<TaskModel> taskList;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Edge-to-edge: chỉ padding top cho root, bottom nav tự xử lý bottom
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Bottom Navigation: padding bottom
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        setupToolbar();
        setupDrawer();        // ← MỚI
        setupRecyclerView();
        setupFab();
        setupBottomNavigation();
        setupBackPressHandler(); // ← MỚI
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Hamburger → mở drawer
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    // ═══════════════════════════════════════
    //  DRAWER SETUP — Custom RecyclerView
    // ═══════════════════════════════════════
    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);

        // RecyclerView trong drawer
        RecyclerView drawerRecyclerView = findViewById(R.id.drawer_recycler_view);
        drawerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tạo danh sách items
        List<DrawerMenuItem> drawerItems = buildDrawerMenuItems();

        // Adapter
        DrawerMenuAdapter drawerAdapter = new DrawerMenuAdapter(drawerItems);
        drawerRecyclerView.setAdapter(drawerAdapter);

        // Click listener
        drawerAdapter.setOnItemClickListener((item, position) -> {
            drawerAdapter.setSelectedPosition(position);
            Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Header buttons
        findViewById(R.id.drawer_btn_search).setOnClickListener(v ->
                Toast.makeText(this, "Tìm kiếm", Toast.LENGTH_SHORT).show());
        findViewById(R.id.drawer_btn_settings).setOnClickListener(v ->
                Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show());

        // Bottom bar buttons
        findViewById(R.id.drawer_btn_add).setOnClickListener(v ->
                Toast.makeText(this, "Thêm danh sách", Toast.LENGTH_SHORT).show());
        findViewById(R.id.drawer_btn_filter).setOnClickListener(v ->
                Toast.makeText(this, "Bộ lọc", Toast.LENGTH_SHORT).show());
    }

    private List<DrawerMenuItem> buildDrawerMenuItems() {
        List<DrawerMenuItem> items = new ArrayList<>();

        // === Navigation items ===
        items.add(new DrawerMenuItem(
                getString(R.string.drawer_today),
                R.drawable.ic_today,
                DrawerMenuItem.ItemType.NAVIGATION
        ).setBadgeCount(1).setSelected(true));  // "Hôm nay" được highlight mặc định

        items.add(new DrawerMenuItem(
                getString(R.string.drawer_inbox),
                R.drawable.ic_inbox,
                DrawerMenuItem.ItemType.NAVIGATION
        ));

        items.add(new DrawerMenuItem(
                getString(R.string.drawer_calendar_subscribed),
                R.drawable.ic_calendar_subscribed,
                DrawerMenuItem.ItemType.NAVIGATION
        ).setHasChevron(true));  // Có mũi tên >

        // === Separator ===
        items.add(DrawerMenuItem.separator());

        // === List items (với icon màu riêng) ===
        items.add(new DrawerMenuItem(
                getString(R.string.drawer_work),
                R.drawable.ic_work,
                DrawerMenuItem.ItemType.LIST
        ));

        items.add(new DrawerMenuItem(
                getString(R.string.drawer_personal),
                R.drawable.ic_personal,
                DrawerMenuItem.ItemType.LIST
        ));

        items.add(new DrawerMenuItem(
                getString(R.string.drawer_shopping),
                R.drawable.ic_shopping,
                DrawerMenuItem.ItemType.LIST
        ));

        items.add(new DrawerMenuItem(
                getString(R.string.drawer_learning),
                R.drawable.ic_learning,
                DrawerMenuItem.ItemType.LIST
        ));

        items.add(new DrawerMenuItem(
                getString(R.string.drawer_wishlist),
                R.drawable.ic_wishlist,
                DrawerMenuItem.ItemType.LIST
        ));

        items.add(new DrawerMenuItem(
                getString(R.string.drawer_fitness),
                R.drawable.ic_fitness,
                DrawerMenuItem.ItemType.LIST
        ));

        return items;
    }

    // Xử lý Back press: đóng drawer thay vì thoát app
    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    // ═══════════════════════════════════════

    private void setupRecyclerView() {
        taskRecyclerView = findViewById(R.id.task_recycler_view);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>();
        taskList.add(new TaskModel("Test", "Hôm nay", false));
        taskList.add(new TaskModel("Coursera learning time", "Hôm nay", false));

        taskAdapter = new TaskAdapter(taskList);
        taskRecyclerView.setAdapter(taskAdapter);
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab_add_task);
        fab.setOnClickListener(v -> Toast.makeText(this, "Thêm công việc mới", Toast.LENGTH_SHORT).show());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.post(() -> {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            params.bottomMargin = bottomNav.getHeight() + dpToPx(16);
            fab.setLayoutParams(params);
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_tasks) {
                // Already on tasks
            } else if (id == R.id.nav_calendar) {
                Toast.makeText(this, "Lịch", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_smart_suggest) {
            Toast.makeText(this, "Gợi ý thông minh", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_more) {
            Toast.makeText(this, "Thêm tùy chọn", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
```

---

## 📌 Checklist tạo file

| #   | File                          | Loại   | Vị trí          |
| --- | ----------------------------- | ------ | --------------- |
| 1   | `colors.xml`                  | MODIFY | `res/values/`   |
| 2   | `strings.xml`                 | MODIFY | `res/values/`   |
| 3   | `ic_today.xml`                | NEW    | `res/drawable/` |
| 4   | `ic_inbox.xml`                | NEW    | `res/drawable/` |
| 5   | `ic_calendar_subscribed.xml`  | NEW    | `res/drawable/` |
| 6   | `ic_work.xml`                 | NEW    | `res/drawable/` |
| 7   | `ic_personal.xml`             | NEW    | `res/drawable/` |
| 8   | `ic_shopping.xml`             | NEW    | `res/drawable/` |
| 9   | `ic_learning.xml`             | NEW    | `res/drawable/` |
| 10  | `ic_wishlist.xml`             | NEW    | `res/drawable/` |
| 11  | `ic_fitness.xml`              | NEW    | `res/drawable/` |
| 12  | `ic_search.xml`               | NEW    | `res/drawable/` |
| 13  | `ic_settings_outline.xml`     | NEW    | `res/drawable/` |
| 14  | `ic_add_list.xml`             | NEW    | `res/drawable/` |
| 15  | `ic_filter.xml`               | NEW    | `res/drawable/` |
| 16  | `ic_chevron_right.xml`        | NEW    | `res/drawable/` |
| 17  | `bg_drawer_item_selected.xml` | NEW    | `res/drawable/` |
| 18  | `bg_avatar_circle.xml`        | NEW    | `res/drawable/` |
| 19  | `bg_drawer_badge.xml`         | NEW    | `res/drawable/` |
| 20  | `item_drawer_menu.xml`        | NEW    | `res/layout/`   |
| 21  | `item_drawer_separator.xml`   | NEW    | `res/layout/`   |
| 22  | `layout_drawer_content.xml`   | NEW    | `res/layout/`   |
| 23  | `activity_main.xml`           | MODIFY | `res/layout/`   |
| 24  | `DrawerMenuItem.java`         | NEW    | `model/`        |
| 25  | `DrawerMenuAdapter.java`      | NEW    | `adapter/`      |
| 26  | `MainActivity.java`           | MODIFY | `activity/`     |

---

## 🧠 Giải thích Custom ListView/RecyclerView với Icons

### Tại sao RecyclerView + Custom Adapter?

Android **ListView** mặc định chỉ hiển thị text. Để thêm icon cho mỗi item, cần:

1. **Custom Layout** (`item_drawer_menu.xml`) — định nghĩa giao diện mỗi hàng: icon + text + badge + chevron
2. **Model class** (`DrawerMenuItem`) — giữ dữ liệu: tiêu đề, icon resource ID, badge count, trạng thái selected
3. **Custom Adapter** (`DrawerMenuAdapter`) — nối dữ liệu từ Model vào Layout, xử lý logic hiển thị

### Flow hoạt động:

```
DrawerMenuItem (data)
    ↓
DrawerMenuAdapter.onBindViewHolder()
    ↓ set icon, text, badge, chevron visibility
item_drawer_menu.xml (UI)
    ↓
RecyclerView hiển thị danh sách
```

### So sánh ListView vs RecyclerView:

| Tiêu chí   | ListView          | RecyclerView                   |
| ---------- | ----------------- | ------------------------------ |
| ViewHolder | Tùy chọn (nên có) | **Bắt buộc** (tốt hơn)         |
| Hiệu năng  | Tốt               | **Rất tốt** (tái sử dụng view) |
| ViewType   | Phức tạp          | **Dễ** (`getItemViewType()`)   |
| Animation  | Không có sẵn      | **Có sẵn**                     |
| Linh hoạt  | Hạn chế           | **Cao**                        |

> **Kết luận**: Dùng **RecyclerView + Custom Adapter** là cách chuẩn nhất trong Android hiện đại để tạo danh sách có icon tùy chỉnh.

---

## 🖼️ Hướng dẫn gắn hình Avatar để test

### Cách 1: Dùng ảnh có sẵn trong drawable (Đơn giản nhất)

**Bước 1**: Copy file ảnh (`.png` hoặc `.jpg`) vào thư mục:
```
app/src/main/res/drawable/
```
Ví dụ: `avatar_test.png` (khuyên dùng kích thước ~200x200px)

**Bước 2**: Trong `layout_drawer_content.xml`, sửa ImageView avatar:

```xml
<!-- Trước -->
<ImageView
    android:id="@+id/drawer_avatar"
    android:layout_width="36dp"
    android:layout_height="36dp"
    android:background="@drawable/bg_avatar_circle"
    android:scaleType="centerCrop"
    android:contentDescription="Avatar" />

<!-- Sau — thêm android:src -->
<ImageView
    android:id="@+id/drawer_avatar"
    android:layout_width="36dp"
    android:layout_height="36dp"
    android:src="@drawable/avatar_test"
    android:scaleType="centerCrop"
    android:contentDescription="Avatar" />
```

> ⚠️ **Vấn đề**: `ImageView` + `centerCrop` sẽ cắt ảnh cho vừa khung, nhưng ảnh vẫn là **hình vuông**, không phải hình tròn.

---

### Cách 2: Ảnh hình tròn bằng `ShapeableImageView` (Khuyên dùng ✅)

Dùng Material `ShapeableImageView` — không cần thư viện bên ngoài.

**Trong `layout_drawer_content.xml`**, thay `ImageView` bằng:

```xml
<com.google.android.material.imageview.ShapeableImageView
    android:id="@+id/drawer_avatar"
    android:layout_width="36dp"
    android:layout_height="36dp"
    android:src="@drawable/avatar_test"
    android:scaleType="centerCrop"
    android:contentDescription="Avatar"
    app:shapeAppearanceOverlay="@style/CircleImageView" />
```

**Trong `res/values/themes.xml`**, thêm style:

```xml
<!-- Circular Image Style -->
<style name="CircleImageView" parent="">
    <item name="cornerFamily">rounded</item>
    <item name="cornerSize">50%</item>
</style>
```

**Kết quả**: Ảnh tự động crop thành hình tròn, giống TickTick.

---

### Cách 3: Load ảnh từ URL bằng Glide (Nâng cao)

Nếu muốn load avatar từ internet hoặc từ file path.

**Bước 1**: Thêm dependency vào `build.gradle.kts`:

```kotlin
implementation("com.github.bumptech.glide:glide:4.16.0")
```

**Bước 2**: Trong `MainActivity.java`, trong `setupDrawer()`:

```java
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;

// Trong setupDrawer():
ShapeableImageView avatar = findViewById(R.id.drawer_avatar);

// Load từ URL
Glide.with(this)
    .load("https://i.pravatar.cc/200")  // URL ảnh test
    .apply(RequestOptions.circleCropTransform())
    .placeholder(R.drawable.bg_avatar_circle)
    .into(avatar);

// Hoặc load từ drawable resource
Glide.with(this)
    .load(R.drawable.avatar_test)
    .apply(RequestOptions.circleCropTransform())
    .into(avatar);
```

---

### Tóm tắt nhanh

| Cách | Độ khó | Hình tròn | Cần thư viện |
|------|--------|-----------|-------------|
| 1. `android:src` + drawable | ⭐ | ❌ Vuông | Không |
| 2. `ShapeableImageView` | ⭐⭐ | ✅ Tròn | Không (Material có sẵn) |
| 3. Glide | ⭐⭐⭐ | ✅ Tròn | Có (Glide) |

> 💡 **Khuyên dùng Cách 2** — đơn giản, hình tròn, không cần thêm thư viện.
