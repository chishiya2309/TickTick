# 😃 Chọn biểu tượng Dialog — Tích hợp API Emoji

> Khi người dùng nhấn vào `changeIcon` (icon danh sách) trong màn hình **Thêm Danh sách**, một **BottomSheetDialog** sẽ trượt lên cho phép tìm kiếm và chọn emoji. Khi chọn xong, popup đóng lại và cập nhật icon trên màn hình thêm danh sách.

---

## 📋 Mục lục

1. [Cấu trúc màn hình Chọn Biểu tượng](#1-cấu-trúc-màn-hình-chọn-biểu-tượng)
2. [Tạo Layout Bottom Sheet](#2-tạo-layout-bottom-sheet)
3. [Tạo Class SelectIconBottomSheet](#3-tạo-class-selecticonbottomsheet)
4. [Tích hợp vào AddListDialogFragment](#4-tích-hợp-vào-addlistdialogfragment)
5. [Checklist tạo file](#5-checklist-tạo-file)

---

## 1. Cấu trúc màn hình Chọn Biểu tượng

1. **Header:** Tiêu đề "Chọn biểu tượng", mô tả và nút ✖️ để đóng.
2. **Thanh tìm kiếm:** Ô nhập liệu để tìm emoji.
3. **Menu bộ lọc:** Các icon tab (gần đây, mặt cười, động vật, v.v.), nút "Ngẫu nhiên" và "Đặt lại".
4. **Danh sách Emoji:** RecyclerView hoặc GridView hiển thị danh sách emoji theo dạng lưới. Có header "Gần đây", "Nhân vật & Cơ thể", v.v.

---

## 2. Tạo Layout Bottom Sheet

📁 `app/src/main/res/layout/layout_bottom_sheet_select_icon.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/main_surface"
    android:padding="16dp">

    <!-- Header -->
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp">

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:layout_alignParentStart="true"
            android:layout_toStartOf="@id/btn_close_icon">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Chọn biểu tượng"
                android:textColor="@color/main_text_primary"
                android:textSize="18sp"
                android:textStyle="bold" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Chọn một biểu tượng để làm cho nó trực quan hơn."
                android:textColor="@color/main_text_secondary"
                android:textSize="12sp"
                android:layout_marginTop="4dp" />
        </LinearLayout>

        <ImageView
            android:id="@+id/btn_close_icon"
            android:layout_width="32dp"
            android:layout_height="32dp"
            android:layout_alignParentEnd="true"
            android:layout_centerVertical="true"
            android:padding="4dp"
            android:src="@drawable/ic_close"
            android:tint="@color/main_text_secondary"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:clickable="true"
            android:focusable="true"/>
    </RelativeLayout>

    <!-- Search bar -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="40dp"
        android:background="@drawable/bg_rounded_dark_gray"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingHorizontal="12dp">

        <ImageView
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@android:drawable/ic_menu_search"
            android:tint="@color/main_text_secondary" />

        <EditText
            android:id="@+id/input_search_emoji"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:background="@null"
            android:hint="Tìm kiếm"
            android:textColor="@color/main_text_primary"
            android:textColorHint="@color/main_text_secondary"
            android:textSize="14sp"
            android:inputType="text"
            android:maxLines="1" />
    </LinearLayout>

    <!-- Filter/Tabs Row -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:layout_marginTop="16dp"
        android:layout_marginBottom="8dp">

        <!-- Các icon filter giả định -->
        <ImageView
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:src="@drawable/ic_action_date"
            android:tint="@color/main_text_secondary"
            android:layout_marginEnd="12dp"/>
        <ImageView
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:src="@drawable/ic_action_mic"
            android:tint="@color/main_text_secondary"
            android:layout_marginEnd="12dp"/>

        <View
            android:layout_width="0dp"
            android:layout_height="1dp"
            android:layout_weight="1" />

        <TextView
            android:id="@+id/btn_random_emoji"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Ngẫu nhiên"
            android:textColor="@color/main_text_primary"
            android:textSize="13sp"
            android:layout_marginEnd="16dp"
            android:clickable="true"
            android:focusable="true"/>

        <TextView
            android:id="@+id/btn_reset_emoji"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Đặt lại"
            android:textColor="@color/main_text_primary"
            android:textSize="13sp"
            android:clickable="true"
            android:focusable="true"/>
    </LinearLayout>

    <!-- Emoji Grid (Cần dùng RecyclerView cho thực tế, đây là layout mẫu lưới) -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <!-- Phân mục "Gần đây" -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Gần đây"
                android:textColor="@color/main_text_secondary"
                android:textSize="14sp"
                android:layout_marginTop="8dp"
                android:layout_marginBottom="8dp" />

            <GridLayout
                android:id="@+id/grid_recent_emoji"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:columnCount="7"
                android:alignmentMode="alignBounds">

                <!-- Emoji ảo -->
                <TextView style="@style/EmojiItem" android:text="😆" />
                <TextView style="@style/EmojiItem" android:text="🎃" />
            </GridLayout>

            <!-- Phân mục "Nhân vật & Cơ thể" -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Nhân vật &amp; Cơ thể \u2304"
                android:textColor="@color/main_text_secondary"
                android:textSize="14sp"
                android:layout_marginTop="16dp"
                android:layout_marginBottom="8dp" />

            <!-- Khung grid chứa emoji thật -->
            <GridLayout
                android:id="@+id/grid_all_emoji"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:columnCount="7">

                <!-- Các TextView hiển thị emoji sẽ được thêm qua Java -->
            </GridLayout>

        </LinearLayout>
    </ScrollView>
</LinearLayout>
```

📁 `app/src/main/res/values/styles.xml` (thêm vào cuối):

```xml
<style name="EmojiItem">
    <item name="android:layout_width">40dp</item>
    <item name="android:layout_height">40dp</item>
    <item name="android:gravity">center</item>
    <item name="android:textSize">28sp</item>
    <item name="android:clickable">true</item>
    <item name="android:focusable">true</item>
    <item name="android:background">?attr/selectableItemBackgroundBorderless</item>
    <!-- Tuỳ biến padding cho đẹp lưới 7 cột -->
</style>
```

---

## 3. Tạo Class SelectIconBottomSheet

Tạo class `SelectIconBottomSheet.java` để xử lý logic BottomSheet mởi lên màn hình này và call API emoji. File lấy giả lập từ một danh sách cố định hoặc fetch API thực tế.

📁 `app/src/main/java/[package_name]/dialog/SelectIconBottomSheet.java`

```java
package hcmute.edu.vn.lequanghung_23110110.ticktick.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;

public class SelectIconBottomSheet {

    public interface OnIconSelectedListener {
        void onIconSelected(String emoji);
    }

    public static void show(Context context, OnIconSelectedListener listener) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_select_icon, null);
        bottomSheet.setContentView(sheetView);

        // Đóng
        sheetView.findViewById(R.id.btn_close_icon).setOnClickListener(v -> bottomSheet.dismiss());

        // Dummy Emoji API data (có thể thay bằng call Retrofit thật)
        List<String> emojis = Arrays.asList(
                "😀", "😃", "😄", "😁", "😆", "😅", "😂",
                "🤣", "🥲", "☺️", "😊", "😇", "🙂", "🙃",
                "😉", "😌", "😍", "🥰", "😘", "😗", "😙",
                "😚", "😋", "😛", "😝", "😜", "🤪", "🤨",
                "🧐", "🤓", "😎", "🥸", "🤩", "🥳", "😏",
                "😒", "😞", "😔", "😟", "😕", "🙁", "☹️",
                "😣", "😖", "😫", "😩", "🥺", "😢", "😭"
        );

        GridLayout gridAllEmoji = sheetView.findViewById(R.id.grid_all_emoji);

        // Populate Grid
        for (String emoji : emojis) {
            TextView textView = new TextView(context);
            textView.setText(emoji);
            textView.setTextSize(28f);
            textView.setGravity(android.view.Gravity.CENTER);

            // Set size 40x40dp
            int sizeInPx = (int) (40 * context.getResources().getDisplayMetrics().density);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = sizeInPx;
            params.height = sizeInPx;
            textView.setLayoutParams(params);

            // Xử lý click
            textView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIconSelected(emoji);
                }
                bottomSheet.dismiss();
            });

            gridAllEmoji.addView(textView);
        }

        // Logic Random
        sheetView.findViewById(R.id.btn_random_emoji).setOnClickListener(v -> {
            Random random = new Random();
            String randomEmoji = emojis.get(random.nextInt(emojis.size()));
            if (listener != null) {
                listener.onIconSelected(randomEmoji);
            }
            bottomSheet.dismiss();
            Toast.makeText(context, "Đã chọn ngẫu nhiên", Toast.LENGTH_SHORT).show();
        });

        bottomSheet.show();
    }
}
```

---

## 4. Tích hợp vào AddListDialogFragment

Sửa class `AddListDialogFragment.java` (màn hình Thêm Danh Sách) để:

1. Lắng nghe event click của icon chọn danh sách (bên trái ô EditText).
2. Hiển thị Emoji / Text ở vị trí đó sau khi User chọn xong từ BottomSheet.

📁 `app/src/main/java/[package_name]/dialog/AddListDialogFragment.java`

Mở file và sửa đoạn code `onCreateView`:

```java
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_list, container, false);

        EditText inputName = view.findViewById(R.id.input_list_name);

        // ---- TÌM IMAGE VIEW CỦA ICON CHỌN DANH SÁCH ----
        ImageView iconList = view.findViewById(R.id.icon_list); // Cần đặt ID cho thẻ ImageView này trong file XML
        // Để hiển thị Emoji dạng Text, ta nên bọc nó bằng FrameLayout có TextView ở trên,
        // Nhưng tạm thời ta có thể dùng TextView riêng rẽ và gán Visibility.

        // Thêm TextView ẩn vào layout `dialog_add_list.xml` tại vị trí Icon:
        // TextView emojiDisplay = view.findViewById(R.id.text_emoji_display);

        // ---- LOGIC CLICK ĐỂ MỞ BOTTOM SHEET CHỌN ICON ----
        iconList.setOnClickListener(v -> {
            SelectIconBottomSheet.show(requireContext(), new SelectIconBottomSheet.OnIconSelectedListener() {
                @Override
                public void onIconSelected(String emoji) {
                     // Ẩn Icon cũ
                     iconList.setVisibility(View.GONE);
                     // Hiển thị TextView chứa mặt cười (bạn cần bổ sung TextView này vào layout)
                     // emojiDisplay.setText(emoji);
                     // emojiDisplay.setVisibility(View.VISIBLE);
                     Toast.makeText(requireContext(), "Đã chọn: " + emoji, Toast.LENGTH_SHORT).show();

                     // TODO: Sửa file XML `dialog_add_list.xml` bổ sung TextView id: text_emoji_display
                }
            });
        });

        // Các logic khác giữ nguyên...
```

**Sửa file `dialog_add_list.xml`:** (Vị trí ô chứa Icon `ic_list` trước EditText "Tên"):

Tìm đoạn:

```xml
                <ImageView
                    android:id="@+id/icon_list"
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:src="@drawable/ic_list"
                    android:tint="@color/main_text_secondary" />
```

Đổi thành:

```xml
                <FrameLayout
                    android:layout_width="32dp"
                    android:layout_height="32dp"
                    android:clickable="true"
                    android:focusable="true">

                    <ImageView
                        android:id="@+id/icon_list"
                        android:layout_width="24dp"
                        android:layout_height="24dp"
                        android:layout_gravity="center"
                        android:src="@drawable/ic_list"
                        android:tint="@color/main_text_secondary" />

                    <TextView
                        android:id="@+id/text_emoji_display"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent"
                        android:gravity="center"
                        android:textSize="20sp"
                        android:visibility="gone"/>
                </FrameLayout>
```

Tiếp tục update logic code `AddListDialogFragment`:

```java
        ImageView iconList = view.findViewById(R.id.icon_list);
        TextView emojiDisplay = view.findViewById(R.id.text_emoji_display);

        View iconContainer = (View) iconList.getParent();
        iconContainer.setOnClickListener(v -> {
            SelectIconBottomSheet.show(requireContext(), emoji -> {
                iconList.setVisibility(View.GONE);
                emojiDisplay.setText(emoji);
                emojiDisplay.setVisibility(View.VISIBLE);
            });
        });
```

---

## 5. Checklist tạo file

| #   | File                                  | Loại   | Vị trí        | Ghi chú                                   |
| --- | ------------------------------------- | ------ | ------------- | ----------------------------------------- |
| 1   | `layout_bottom_sheet_select_icon.xml` | NEW    | `res/layout/` | Giao diện chọn Emoji                      |
| 2   | `SelectIconBottomSheet.java`          | NEW    | `dialog/`     | View/API Controller cho Emoji             |
| 3   | `styles.xml`                          | MODIFY | `res/values/` | Size Text Emoji (EmojiItem)               |
| 4   | `dialog_add_list.xml`                 | MODIFY | `res/layout/` | Đổi ImageView thanh FrameLayout chứa Text |
| 5   | `AddListDialogFragment.java`          | MODIFY | `dialog/`     | Lắng nghe click để update Text Emoji      |
