# 📝 Add List Dialog — Thêm Danh Sách

> Khi nhấn **Danh sách** trong `popup_item_list` (menu Thêm), hiển thị màn hình **DialogFragment** (hoạt động như Full Screen) cho phép người dùng nhập tên, chọn màu và loại chế độ xem theo chuẩn giao diện của TickTick.

---

## 📋 Mục lục

1. [Tạo Layout Dialog](#1-tạo-layout-dialog)
2. [Tạo Layout Item Chế độ xem & Background](#2-tạo-layout-item-chế-độ-xem--background)
3. [Tạo Class AddListDialogFragment](#3-tạo-class-addlistdialogfragment)
4. [Sửa MainActivity.java](#4-sửa-mainactivityjava)
5. [Checklist tạo file](#5-checklist-tạo-file)

---

## 1. Tạo Layout Dialog

📁 `app/src/main/res/layout/dialog_add_list.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/main_surface">

    <!-- ═══ Toolbar Header ═══ -->
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:paddingHorizontal="16dp">

        <ImageView
            android:id="@+id/btn_close"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:layout_centerVertical="true"
            android:padding="8dp"
            android:src="@drawable/ic_close"
            android:tint="@color/main_text_primary"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:clickable="true"
            android:focusable="true"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:text="Thêm Danh sách"
            android:textColor="@color/main_text_primary"
            android:textSize="18sp"
            android:textStyle="bold" />

        <ImageView
            android:id="@+id/btn_save"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:layout_alignParentEnd="true"
            android:layout_centerVertical="true"
            android:padding="8dp"
            android:src="@drawable/ic_check"
            android:tint="@color/main_text_primary"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:clickable="true"
            android:focusable="true"/>
    </RelativeLayout>

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingHorizontal="16dp"
            android:paddingTop="8dp">

            <!-- ═══ Tên Danh Sách ═══ -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="56dp"
                android:background="@drawable/bg_rounded_dark_gray"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:paddingHorizontal="16dp">

                <ImageView
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:src="@drawable/ic_list"
                    android:tint="@color/main_text_secondary" />

                <EditText
                    android:id="@+id/input_list_name"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="16dp"
                    android:background="@null"
                    android:hint="Tên"
                    android:textColor="@color/main_text_primary"
                    android:textColorHint="@color/main_text_secondary"
                    android:textSize="16sp"
                    android:inputType="textCapSentences"
                    android:maxLines="1" />
            </LinearLayout>

            <!-- ═══ Khung Cài Đặt (Màu & Chế độ xem) ═══ -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:background="@drawable/bg_rounded_dark_gray"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Màu danh sách"
                    android:textColor="@color/main_text_primary"
                    android:textSize="15sp"
                    android:layout_marginBottom="12dp" />

                <!-- Danh sách Màu sắc -->
                <HorizontalScrollView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:scrollbars="none">

                    <LinearLayout
                        android:id="@+id/color_container"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:gravity="center_vertical">

                        <!-- Nút Không Màu (Mặc định) -->
                        <ImageView
                            android:layout_width="32dp"
                            android:layout_height="32dp"
                            android:src="@drawable/bg_circle_no_color"
                            android:layout_marginEnd="12dp"
                            android:background="@drawable/bg_color_selected_ring" />

                        <!-- Các màu khác (Red, Orange, Yellow, v.v) sinh tự động qua Java -->
                    </LinearLayout>
                </HorizontalScrollView>

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Loại chế độ xem"
                    android:textColor="@color/main_text_primary"
                    android:textSize="15sp"
                    android:layout_marginTop="20dp"
                    android:layout_marginBottom="12dp" />

                <!-- Các Loại Chế Độ Xem -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:weightSum="3"
                    android:baselineAligned="false">

                    <!-- Danh sách (Selected) -->
                    <include layout="@layout/item_view_type"
                        android:id="@+id/view_type_list"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:layout_marginEnd="8dp" />

                    <!-- Kanban -->
                    <include layout="@layout/item_view_type"
                        android:id="@+id/view_type_kanban"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:layout_marginStart="4dp"
                        android:layout_marginEnd="4dp" />

                    <!-- Dòng thời gian -->
                    <include layout="@layout/item_view_type"
                        android:id="@+id/view_type_timeline"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:layout_marginStart="8dp" />
                </LinearLayout>

            </LinearLayout>

            <!-- ═══ Nút Thêm Cài Đặt ═══ -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_gravity="center_horizontal"
                android:layout_marginTop="24dp"
                android:padding="8dp"
                android:text="Thêm cài đặt \u2304"
                android:textColor="@color/main_text_secondary"
                android:textSize="14sp"
                android:clickable="true"
                android:focusable="true"/>

        </LinearLayout>
    </ScrollView>
</LinearLayout>
```

---

## 2. Tạo Layout Item Chế độ xem & Background

### 2a. Background Rounded Dark Gray (`bg_rounded_dark_gray.xml`)

📁 `app/src/main/res/drawable/bg_rounded_dark_gray.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#2C2C2E" /> <!-- Màu xám tối kiểu của hộp thoại -->
    <corners android:radius="16dp" />
</shape>
```

### 2b. Viền chọn màu sắc (`bg_color_selected_ring.xml`)

📁 `app/src/main/res/drawable/bg_color_selected_ring.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <stroke android:width="2dp" android:color="#4C6FE0" /> <!-- Màu primary xanh -->
    <padding android:bottom="4dp" android:left="4dp" android:right="4dp" android:top="4dp"/>
</shape>
```

### 2c. Item Chế độ xem (`item_view_type.xml`)

📁 `app/src/main/res/layout/item_view_type.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:gravity="center_horizontal">

    <FrameLayout
        android:id="@+id/card_view_type"
        android:layout_width="match_parent"
        android:layout_height="72dp"
        android:background="@drawable/bg_rounded_dark_gray">
        <!-- Có thể chèn Image Thumbnail đại diện cho List / Kanban / Timeline tại đây -->

        <!-- Nút Check hiển thị khi được chọn -->
        <ImageView
            android:id="@+id/ic_check_selected"
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:layout_gravity="top|end"
            android:layout_margin="4dp"
            android:src="@drawable/ic_check"
            android:background="@drawable/bg_circle_blue"
            android:padding="4dp"
            android:tint="@android:color/white"
            android:visibility="gone" /> <!-- Mặc định ẩn, bật lên khi active -->
    </FrameLayout>

    <TextView
        android:id="@+id/text_view_type"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="Danh sách"
        android:textColor="@color/main_text_secondary"
        android:textSize="13sp" />
</LinearLayout>
```

---

## 3. Tạo Class AddListDialogFragment

📁 `app/src/main/java/[package_name]/dialog/AddListDialogFragment.java`

```java
package hcmute.edu.vn.lequanghung_23110110.ticktick.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;

public class AddListDialogFragment extends DialogFragment {

    public static AddListDialogFragment newInstance() {
        return new AddListDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Thiết lập full screen style
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_TickTick_FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_list, container, false);

        EditText inputName = view.findViewById(R.id.input_list_name);

        view.findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            if (name.isEmpty()) {
                inputName.setError("Vui lòng nhập tên danh sách");
                return;
            }
            // TODO: Lưu vào SQLite hoặc Database
            Toast.makeText(getContext(), "Đã tạo: " + name, Toast.LENGTH_SHORT).show();
            dismiss();
        });

        // Tự động focus ô nhập tên
        inputName.requestFocus();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // Thiết lập Dialog chiếm full chiều rộng & cao
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}
```

Mở File `res/values/themes.xml` thêm đoạn này (để dialog chiếm full không có viền window):

```xml
<style name="Theme.TickTick.FullScreenDialog" parent="Theme.Material3.DayNight.NoActionBar">
    <item name="android:windowIsTranslucent">true</item>
    <item name="android:windowBackground">@color/main_surface</item>
    <!-- Tránh top status bar cover mất content -->
</style>
```

---

## 4. Sửa MainActivity.java

Trong `MainActivity`, tìm đoạn logic khi click vào "Danh sách" trong menu Popup (`showAddMenuPopup`):

```java
    popupView.findViewById(R.id.popup_item_list).setOnClickListener(v -> {
        // Đóng popup menu
        popupWindow.dismiss();

        // Mở màn hình Thêm Danh Sách
        AddListDialogFragment dialog = AddListDialogFragment.newInstance();
        dialog.show(getSupportFragmentManager(), "AddListDialog");
    });
```

---

## 5. Checklist tạo file

| #   | File                         | Loại   | Vị trí          | Ghi chú                        |
| --- | ---------------------------- | ------ | --------------- | ------------------------------ |
| 1   | `bg_rounded_dark_gray.xml`   | NEW    | `res/drawable/` | Nền tối 16dp bo góc            |
| 2   | `bg_color_selected_ring.xml` | NEW    | `res/drawable/` | Vòng tròn xanh chọn màu        |
| 3   | `item_view_type.xml`         | NEW    | `res/layout/`   | Card cho Chế độ xem            |
| 4   | `dialog_add_list.xml`        | NEW    | `res/layout/`   | Layout chức năng               |
| 5   | `AddListDialogFragment.java` | NEW    | `dialog/`       | Logic của màn hình Create List |
| 6   | `MainActivity.java`          | MODIFY | `activity/`     | Gọi `.show()` cho Dialog       |
| 7   | `themes.xml`                 | MODIFY | `res/values/`   | Style Dialog                   |
