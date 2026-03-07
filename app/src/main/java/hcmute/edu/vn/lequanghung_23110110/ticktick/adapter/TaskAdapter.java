package hcmute.edu.vn.lequanghung_23110110.ticktick.adapter;

import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskHeader;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskListItem;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<TaskListItem> items;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(TaskModel task);

        void onTaskCheckedChanged(TaskModel task, boolean isChecked);
    }

    public TaskAdapter(List<TaskListItem> items) {
        this.items = items;
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TaskListItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_task_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            TaskHeader header = (TaskHeader) items.get(position);
            ((HeaderViewHolder) holder).bind(header);
        } else if (holder instanceof TaskViewHolder) {
            TaskModel task = (TaskModel) items.get(position);
            ((TaskViewHolder) holder).bind(task, listener);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView title, count;
        private final ImageView iconExpand;
        private final View container;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView;
            title = itemView.findViewById(R.id.header_title);
            count = itemView.findViewById(R.id.header_count);
            iconExpand = itemView.findViewById(R.id.header_icon);
        }

        void bind(TaskHeader header) {
            title.setText(header.getTitle());
            count.setText(String.valueOf(header.getCount()));

            // Apply text colors
            int color = itemView.getContext().getColor(header.getColorResId());
            title.setTextColor(color);
            count.setTextColor(color);
            iconExpand.setImageTintList(ColorStateList.valueOf(color));

            // Set chevron state
            iconExpand.setRotation(header.isExpanded() ? 0 : -90); // Down is expanded, Right is collapsed

            // Setup toggling click
            container.setOnClickListener(v -> {
                if (header.getOnToggleListener() != null) {
                    header.getOnToggleListener().run();
                }
            });
        }
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

        void bind(TaskModel task, OnTaskClickListener listener) {
            title.setText(task.getTitle());
            dateTag.setText(task.getDateTag());
            checkbox.setChecked(task.isCompleted());

            // Strikethrough effect when completed
            updateStrikethrough(task.isCompleted());

            // Using setOnClickListener to only trigger on manual clicks, bypassing
            // RecyclerView recycling
            checkbox.setOnClickListener(v -> {
                boolean isChecked = checkbox.isChecked();
                task.setCompleted(isChecked);
                updateStrikethrough(isChecked);
                if (listener != null) {
                    listener.onTaskCheckedChanged(task, isChecked);
                }
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