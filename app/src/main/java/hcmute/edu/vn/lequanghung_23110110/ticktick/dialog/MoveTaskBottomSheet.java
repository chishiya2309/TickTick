package hcmute.edu.vn.lequanghung_23110110.ticktick.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.MoveTaskListAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.ListModel;

public class MoveTaskBottomSheet extends BottomSheetDialogFragment {

    private final int taskId;
    private final int currentListId;
    private OnTaskMovedListener listener;

    public interface OnTaskMovedListener {
        void onTaskMoved(int newId, String newName, String iconName);
    }

    public MoveTaskBottomSheet(int taskId, int currentListId) {
        this.taskId = taskId;
        this.currentListId = currentListId;
    }

    public void setOnTaskMovedListener(OnTaskMovedListener listener) {
        this.listener = listener;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_move_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header
        view.findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());

        // Setup RecyclerView
        RecyclerView rvLists = view.findViewById(R.id.rv_move_lists);
        rvLists.setLayoutManager(new LinearLayoutManager(getContext()));

        // Fetch valid lists (Hộp thư đến + Custom Lists)
        TaskDatabaseHelper dbHelper = TaskDatabaseHelper.getInstance(getContext());
        List<ListModel> lists = dbHelper.getMoveToOptions();

        MoveTaskListAdapter adapter = new MoveTaskListAdapter(getContext(), lists, currentListId);

        // Search Functionality
        EditText etSearch = view.findViewById(R.id.et_search_list);
        ImageView btnClearSearch = view.findViewById(R.id.btn_clear_search);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnClearSearch.setVisibility(View.VISIBLE);
                } else {
                    btnClearSearch.setVisibility(View.GONE);
                }
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            adapter.filter("");
        });
        adapter.setOnListSelectedListener(list -> {
            if (list.getId() == currentListId) {
                // Task is already in this list, do nothing but close
                dismiss();
                return;
            }

            // Move the task
            dbHelper.moveTaskToList(taskId, list.getId());

            // Notify listener (to show toast and reload UI)
            if (listener != null) {
                listener.onTaskMoved(list.getId(), list.getName(), list.getIconName());
            }

            dismiss();
        });
        rvLists.setAdapter(adapter);

        // Add List Button
        view.findViewById(R.id.btn_add_list).setOnClickListener(v -> {
            dismiss();
            Toast.makeText(getContext(), "Vui lòng thêm danh sách ở menu chính", Toast.LENGTH_SHORT).show();
        });
    }
}
