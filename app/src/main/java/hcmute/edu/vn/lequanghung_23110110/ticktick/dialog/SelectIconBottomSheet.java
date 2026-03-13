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

        // View references
        RecyclerView recyclerEmoji = sheetView.findViewById(R.id.recycler_emoji);
        recyclerEmoji.setLayoutManager(new GridLayoutManager(context, 7)); 
        android.widget.ProgressBar progressEmoji = sheetView.findViewById(R.id.progress_emoji);

        EmojiAdapter adapter = new EmojiAdapter(new ArrayList<>(), emoji -> {
            if (listener != null) {
                listener.onIconSelected(emoji);
            }
            bottomSheet.dismiss();
        });
        recyclerEmoji.setAdapter(adapter);

        hcmute.edu.vn.lequanghung_23110110.ticktick.repository.EmojiRepository.getInstance().getEmojis(context, new hcmute.edu.vn.lequanghung_23110110.ticktick.repository.EmojiRepository.FetchCallback() {
            @Override
            public void onSuccess(List<EmojiResponse> allEmojis) {
                sheetView.post(() -> {
                    progressEmoji.setVisibility(View.GONE);
                    recyclerEmoji.setVisibility(View.VISIBLE);
                    adapter.updateList(allEmojis);

                    // Xử lý tìm kiếm sau khi có dữ liệu
                    EditText inputSearch = sheetView.findViewById(R.id.input_search_emoji);
                    inputSearch.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            String query = s.toString().toLowerCase();
                            if (query.isEmpty()) {
                                adapter.updateList(allEmojis);
                            } else {
                                List<EmojiResponse> filteredList = new ArrayList<>();
                                for (EmojiResponse emoji : allEmojis) {
                                    if (emoji.name != null && emoji.name.toLowerCase().contains(query)) {
                                        filteredList.add(emoji);
                                    }
                                }
                                adapter.updateList(filteredList);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {}
                    });

                    // Xử lý nút Random
                    sheetView.findViewById(R.id.btn_random_emoji).setOnClickListener(v -> {
                        if (!allEmojis.isEmpty()) {
                            Random random = new Random();
                            EmojiResponse randomEmoji = allEmojis.get(random.nextInt(allEmojis.size()));
                            if (listener != null) {
                                listener.onIconSelected(randomEmoji.character);
                            }
                            bottomSheet.dismiss();
                        }
                    });
                });
            }

            @Override
            public void onError(String message) {
                sheetView.post(() -> {
                    progressEmoji.setVisibility(View.GONE);
                    android.widget.Toast.makeText(context, "Lỗi tải emoji: " + message, android.widget.Toast.LENGTH_SHORT).show();
                });
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
}