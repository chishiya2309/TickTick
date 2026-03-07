package hcmute.edu.vn.lequanghung_23110110.ticktick.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Map;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

public class TaskDetailBottomSheet extends BottomSheetDialogFragment {

    private TaskModel task;
    private TaskDatabaseHelper dbHelper;
    private OnTaskUpdatedListener updateListener;

    public interface OnTaskUpdatedListener {
        void onTaskUpdated();
    }

    public TaskDetailBottomSheet(TaskModel task) {
        this.task = task;
    }

    public void setOnTaskUpdatedListener(OnTaskUpdatedListener listener) {
        this.updateListener = listener;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_task_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = TaskDatabaseHelper.getInstance(requireContext());

        // Header Views
        TextView textListName = view.findViewById(R.id.detail_list_name);
        ImageView listIcon = view.findViewById(R.id.detail_list_icon);
        TextView listEmoji = view.findViewById(R.id.detail_list_emoji);

        // Content Views
        CheckBox checkbox = view.findViewById(R.id.detail_checkbox);
        TextView textDateTag = view.findViewById(R.id.detail_date_tag);
        EditText editTitle = view.findViewById(R.id.detail_task_title);
        EditText editDescription = view.findViewById(R.id.detail_task_description);

        // Bind data
        if (task != null) {
            editTitle.setText(task.getTitle());
            if (task.getDescription() != null) {
                editDescription.setText(task.getDescription());
            }
            checkbox.setChecked(task.isCompleted());

            // Date tag
            if (task.getDateTag() != null && !task.getDateTag().isEmpty()) {
                textDateTag.setText(task.getDateTag() + " ");
                textDateTag.setVisibility(View.VISIBLE);
            } else {
                textDateTag.setVisibility(View.GONE);
            }

            // List Name & Icon
            String listName = dbHelper.getListNameById(task.getListId());
            textListName.setText(listName);

            int iconResId = dbHelper.getListIconResId(requireContext(), task.getListId());
            if (iconResId != 0) {
                listIcon.setImageResource(iconResId);
                listIcon.setVisibility(View.VISIBLE);
                listEmoji.setVisibility(View.GONE);
            } else {
                Map<String, String> customLists = dbHelper.getAllCustomLists();
                String iconNameOrEmoji = customLists.get(listName);
                if (iconNameOrEmoji != null && !iconNameOrEmoji.startsWith("ic_")) {
                    listEmoji.setText(iconNameOrEmoji);
                    listEmoji.setVisibility(View.VISIBLE);
                    listIcon.setVisibility(View.GONE);
                }
            }
        }

        // Actions
        checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (task != null) {
                task.setCompleted(isChecked);
                dbHelper.updateTaskCompleted(task.getId(), isChecked);
                if (updateListener != null) {
                    updateListener.onTaskUpdated();
                }
            }
        });

        // Add dismiss listener or bottom sheet close listener to save title/description
        // changes
        // Since sqlite database for TickTick doesn't have a description column, we only
        // save title.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Save modifications when the dialog is dismissed
        if (task != null) {
            View view = getView();
            if (view != null) {
                EditText editTitle = view.findViewById(R.id.detail_task_title);
                EditText editDescription = view.findViewById(R.id.detail_task_description);
                String newTitle = editTitle.getText().toString().trim();
                String newDescription = editDescription.getText().toString().trim();

                boolean hasChanged = false;
                if (!newTitle.isEmpty() && !newTitle.equals(task.getTitle())) {
                    task.setTitle(newTitle);
                    hasChanged = true;
                }

                String oldDescription = task.getDescription() == null ? "" : task.getDescription();
                if (!newDescription.equals(oldDescription)) {
                    task.setDescription(newDescription);
                    hasChanged = true;
                }

                if (hasChanged) {
                    dbHelper.updateTaskDetails(task.getId(), task.getTitle(), task.getDescription());
                    if (updateListener != null) {
                        updateListener.onTaskUpdated();
                    }
                }
            }
        }
    }
}
