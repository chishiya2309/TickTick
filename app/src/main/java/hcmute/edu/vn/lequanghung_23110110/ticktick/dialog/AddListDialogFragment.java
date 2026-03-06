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

    private static final String ARG_LIST_ID = "list_id";
    private static final String ARG_LIST_NAME = "list_name";
    private static final String ARG_LIST_ICON = "list_icon";
    private static final String ARG_ITEM_POSITION = "item_position";

    public static AddListDialogFragment newInstance() {
        return new AddListDialogFragment();
    }

    public static AddListDialogFragment newInstanceForEdit(int listId, String name, String emojiIcon, int position) {
        AddListDialogFragment fragment = new AddListDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LIST_ID, listId);
        args.putString(ARG_LIST_NAME, name);
        args.putString(ARG_LIST_ICON, emojiIcon);
        args.putInt(ARG_ITEM_POSITION, position);
        fragment.setArguments(args);
        return fragment;
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
        TextView dialogTitle = view.findViewById(R.id.dialog_title);

        // Kiểm tra xem có phải chế độ Edit không
        int editListId = -1;
        int editPosition = -1;
        if (getArguments() != null) {
            editListId = getArguments().getInt(ARG_LIST_ID, -1);
            editPosition = getArguments().getInt(ARG_ITEM_POSITION, -1);
            String initialName = getArguments().getString(ARG_LIST_NAME, "");
            String initialIcon = getArguments().getString(ARG_LIST_ICON, null);

            if (editListId != -1) {
                dialogTitle.setText("Sửa Danh sách");
                inputName.setText(initialName);
                if (initialIcon != null && !initialIcon.isEmpty() && !initialIcon.startsWith("ic_")) {
                    iconList.setVisibility(View.GONE);
                    emojiDisplay.setText(initialIcon);
                    emojiDisplay.setVisibility(View.VISIBLE);
                }
            }
        }

        final int finalEditListId = editListId;
        final int finalEditPosition = editPosition;

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

            if (getActivity() instanceof hcmute.edu.vn.lequanghung_23110110.ticktick.activity.MainActivity) {
                hcmute.edu.vn.lequanghung_23110110.ticktick.activity.MainActivity mainActivity = (hcmute.edu.vn.lequanghung_23110110.ticktick.activity.MainActivity) getActivity();

                if (finalEditListId != -1) {
                    mainActivity.updateListInDrawer(finalEditListId, name, emoji, finalEditPosition);
                    Toast.makeText(getContext(), "Đã cập nhật: " + name, Toast.LENGTH_SHORT).show();
                } else {
                    mainActivity.addNewListToDrawer(name, emoji);
                    Toast.makeText(getContext(), "Đã tạo: " + name, Toast.LENGTH_SHORT).show();
                }
            }

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