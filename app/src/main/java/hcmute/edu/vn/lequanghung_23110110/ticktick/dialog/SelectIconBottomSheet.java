package hcmute.edu.vn.lequanghung_23110110.ticktick.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.EmojiAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.EmojiResponse;

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

        // Setup RecyclerView
        RecyclerView recyclerEmoji = sheetView.findViewById(R.id.recycler_emoji);
        recyclerEmoji.setLayoutManager(new GridLayoutManager(context, 7)); // 7 cột giống app thật

        List<EmojiResponse> allEmojis = loadEmojisFromAssets(context);

        EmojiAdapter adapter = new EmojiAdapter(allEmojis, emoji -> {
            if (listener != null) {
                listener.onIconSelected(emoji);
            }
            bottomSheet.dismiss();
        });
        recyclerEmoji.setAdapter(adapter);

        // Xử lý tìm kiếm
        EditText inputSearch = sheetView.findViewById(R.id.input_search_emoji);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();
                List<EmojiResponse> filteredList = new ArrayList<>();
                for (EmojiResponse emoji : allEmojis) {
                    // Nếu có unicodeName trong file json thì lọc, không có thì bỏ qua (file json
                    // rút gọn chỉ có character)
                    // Vì file `emojis.json` hiện tại chỉ chứa `character`, ta có thể không cần
                    // search hoặc search theo group (nếu có).
                    // Nếu bạn cập nhật file `emojis.json` đầy đủ thuộc tính `unicodeName`, bạn có
                    // thể dùng code sau:
                    if (emoji.name != null && emoji.name.toLowerCase().contains(query)) {
                        filteredList.add(emoji);
                    }
                }

                // Demo logic: Nếu query trống, trả lại toàn bộ. Ngược lại (nếu file JSON hiện
                // tại của ta trống unicodeName) thì list sẽ rỗng.
                if (query.isEmpty()) {
                    adapter.updateList(allEmojis);
                } else {
                    adapter.updateList(filteredList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Xử lý nút Random
        sheetView.findViewById(R.id.btn_random_emoji).setOnClickListener(v -> {
            if (allEmojis != null && !allEmojis.isEmpty()) {
                Random random = new Random();
                EmojiResponse randomEmoji = allEmojis.get(random.nextInt(allEmojis.size()));
                if (listener != null) {
                    listener.onIconSelected(randomEmoji.character);
                }
                bottomSheet.dismiss();
            }
        });

        // Xử lý nút Đặt lại (Reset icon về null hoặc mặc định)
        sheetView.findViewById(R.id.btn_reset_emoji).setOnClickListener(v -> {
            if (listener != null) {
                listener.onIconSelected(null); // Truyền null để báo reset
            }
            bottomSheet.dismiss();
        });

        bottomSheet.show();
    }

    private static List<EmojiResponse> loadEmojisFromAssets(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("emojis.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SelectIconBottomSheet", "Error loading JSON: " + ex.getMessage());
            return new ArrayList<>();
        }

        Type listType = new TypeToken<List<EmojiResponse>>() {
        }.getType();
        return new Gson().fromJson(json, listType);
    }
}