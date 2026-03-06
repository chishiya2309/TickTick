package hcmute.edu.vn.lequanghung_23110110.ticktick.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_list, container, false);

        EditText inputName = view.findViewById(R.id.input_list_name);

        ImageView iconList = view.findViewById(R.id.icon_list);
        TextView emojiDisplay = view.findViewById(R.id.text_emoji_display);

        View iconContainer = (View) iconList.getParent();
        iconContainer.setOnClickListener(v -> {
            SelectIconBottomSheet.show(requireContext(), emoji -> {
                if (emoji == null) {
                    // Reset to default icon
                    emojiDisplay.setVisibility(View.GONE);
                    iconList.setVisibility(View.VISIBLE);
                } else {
                    // Display selected emoji
                    iconList.setVisibility(View.GONE);
                    emojiDisplay.setText(emoji);
                    emojiDisplay.setVisibility(View.VISIBLE);
                }
            });
        });

        view.findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            if (name.isEmpty()) {
                inputName.setError("Vui lòng nhập tên danh sách");
                return;
            }

            String emoji = emojiDisplay.getVisibility() == View.VISIBLE ? emojiDisplay.getText().toString() : null;

            // Notify MainActivity to update drawer
            if (getActivity() instanceof hcmute.edu.vn.lequanghung_23110110.ticktick.activity.MainActivity) {
                ((hcmute.edu.vn.lequanghung_23110110.ticktick.activity.MainActivity) getActivity())
                        .addNewListToDrawer(name, emoji);
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