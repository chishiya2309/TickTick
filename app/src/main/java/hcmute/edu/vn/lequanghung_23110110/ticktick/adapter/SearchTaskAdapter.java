package hcmute.edu.vn.lequanghung_23110110.ticktick.adapter;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

public class SearchTaskAdapter extends RecyclerView.Adapter<SearchTaskAdapter.ViewHolder> {

    private List<TaskModel> tasks;
    private String currentQuery = "";
    private OnItemClickListener listener;
    private final int highlightColor = Color.parseColor("#f59e0b"); // Orange as seen in screenshot

    public interface OnItemClickListener {
        void onItemClick(TaskModel task);
    }

    public SearchTaskAdapter(List<TaskModel> tasks, OnItemClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    public void updateData(List<TaskModel> newTasks, String query) {
        this.tasks = newTasks;
        this.currentQuery = query == null ? "" : query.toLowerCase();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskModel task = tasks.get(position);
        
        holder.bind(task, currentQuery, highlightColor, listener);
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageView ivCheckbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.item_search_task_title);
            tvSubtitle = itemView.findViewById(R.id.item_search_task_subtitle);
            ivCheckbox = itemView.findViewById(R.id.item_search_task_checkbox);
        }

        public void bind(TaskModel task, String query, int highlightColor, OnItemClickListener listener) {
            ivCheckbox.setImageResource(task.isCompleted() ? R.drawable.checkbox_checked : R.drawable.checkbox_unchecked);
            tvSubtitle.setText(task.getDateTag() != null ? task.getDateTag() : "");

            // Highlight title
            String title = task.getTitle();
            tvTitle.setText(getHighlightedText(title, query, highlightColor));

            // Setup click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(task);
                }
            });
        }

        private CharSequence getHighlightedText(String text, String query, int color) {
            if (query == null || query.isEmpty() || text == null || text.isEmpty()) {
                return text != null ? text : "";
            }
            
            String lowerText = text.toLowerCase();
            SpannableString spannable = new SpannableString(text);
            
            int start = lowerText.indexOf(query);
            while (start >= 0) {
                int end = start + query.length();
                spannable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = lowerText.indexOf(query, end);
            }
            return spannable;
        }
    }
}
