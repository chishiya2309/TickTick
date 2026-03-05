package hcmute.edu.vn.lequanghung_23110110.ticktick.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<TaskModel> taskList;

    public TaskAdapter(List<TaskModel> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskModel task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkbox;
        private final TextView title;
        private final TextView dateTag;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.task_checkbox);
            title = itemView.findViewById(R.id.task_title);
            dateTag = itemView.findViewById(R.id.task_date_tag);
        }

        void bind(TaskModel task) {
            title.setText(task.getTitle());
            dateTag.setText(task.getDateTag());
            checkbox.setChecked(task.isCompleted());

            // Strikethrough effect when completed
            updateStrikethrough(task.isCompleted());

            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                updateStrikethrough(isChecked);
            });
        }

        private void updateStrikethrough(boolean completed) {
            if (completed) {
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                title.setAlpha(0.5f);
            } else {
                title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                title.setAlpha(1f);
            }
        }
    }
}