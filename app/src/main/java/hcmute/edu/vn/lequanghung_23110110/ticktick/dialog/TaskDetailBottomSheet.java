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
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.CalendarHelper;

public class TaskDetailBottomSheet extends BottomSheetDialogFragment {

    private TaskModel task;
    private TaskDatabaseHelper dbHelper;
    private OnTaskUpdatedListener updateListener;
    private String highlightKeyword = "";

    public interface OnTaskUpdatedListener {
        void onTaskUpdated();
    }

    public TaskDetailBottomSheet(TaskModel task) {
        this.task = task;
    }

    public void setHighlightKeyword(String keyword) {
        this.highlightKeyword = keyword != null ? keyword : "";
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
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                
                // Cần set height của bottom sheet container là MATCH_PARENT để cho phép vuốt lên full screen
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    bottomSheet.setLayoutParams(layoutParams);
                }

                // Set peek height to 50% of screen height for Figure 1
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                behavior.setPeekHeight(screenHeight / 2);
                behavior.setSkipCollapsed(false); // Enable collapsed state
                
                // Start at peek height (collapsed)
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                
                behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        View backBtn = getView() != null ? getView().findViewById(R.id.detail_back_button) : null;
                        if (backBtn != null) {
                            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                                backBtn.setVisibility(View.VISIBLE);
                            } else {
                                backBtn.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                        // Optional: Fade in the back button based on slideOffset
                        View backBtn = getView() != null ? getView().findViewById(R.id.detail_back_button) : null;
                        if (backBtn != null) {
                            if (slideOffset > 0.8f) {
                                backBtn.setVisibility(View.VISIBLE);
                                backBtn.setAlpha((slideOffset - 0.8f) * 5f);
                            } else if (slideOffset < 0.8f) {
                                backBtn.setVisibility(View.GONE);
                            }
                        }
                    }
                });
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
            int colorHighlight = android.graphics.Color.parseColor("#f59e0b");
            editTitle.setText(getHighlightedText(task.getTitle(), highlightKeyword, colorHighlight), TextView.BufferType.EDITABLE);
            if (task.getDescription() != null) {
                editDescription.setText(getHighlightedText(task.getDescription(), highlightKeyword, colorHighlight), TextView.BufferType.EDITABLE);
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
                String emoji = dbHelper.getListEmojiById(task.getListId());
                if (emoji != null && !emoji.isEmpty()) {
                    listEmoji.setText(emoji);
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

                CalendarHelper calHelper = CalendarHelper.getInstance(requireContext());
                if (isChecked) {
                    if (task.getCalendarEventId() > 0) {
                        calHelper.deleteEvent(task.getCalendarEventId());
                        dbHelper.updateTaskCalendarEventId(task.getId(), -1);
                    }
                } else {
                    if (task.getDueDateMillis() > 0 && calHelper.hasCalendarPermission() && calHelper.isSyncEnabled()) {
                        long eventId = calHelper.insertEvent(task);
                        if (eventId > 0) {
                            dbHelper.updateTaskCalendarEventId(task.getId(), eventId);
                        }
                    }
                }

                if (updateListener != null) {
                    updateListener.onTaskUpdated();
                }
            }
        });

        // Save modifications when the dialog is dismissed
        // Since sqlite database for TickTick doesn't have a description column, we only
        // save title.
        
        // Back Button
        ImageView backButton = view.findViewById(R.id.detail_back_button);
        backButton.setOnClickListener(v -> {
            // "về màn hình activity_main" -> Dismiss
            dismiss();
        });
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

                    // Cập nhật event trên Google Calendar nếu có
                    if (task.getCalendarEventId() > 0) {
                        CalendarHelper calHelper = CalendarHelper.getInstance(requireContext());
                        if (calHelper.hasCalendarPermission() && calHelper.isSyncEnabled()) {
                            calHelper.updateEvent(task.getCalendarEventId(), task);
                        }
                    }

                    if (updateListener != null) {
                        updateListener.onTaskUpdated();
                    }
                }
            }
        }
    }

    private CharSequence getHighlightedText(String text, String keyword, int color) {
        if (keyword == null || keyword.isEmpty() || text == null || text.isEmpty()) {
            return text != null ? text : "";
        }

        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        android.text.SpannableString spannable = new android.text.SpannableString(text);

        int start = lowerText.indexOf(lowerKeyword);
        while (start >= 0) {
            int end = start + lowerKeyword.length();
            spannable.setSpan(new android.text.style.ForegroundColorSpan(color), start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = lowerText.indexOf(lowerKeyword, end);
        }
        return spannable;
    }
}
