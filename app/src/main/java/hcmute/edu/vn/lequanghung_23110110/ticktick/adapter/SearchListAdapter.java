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
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.DrawerMenuItem;

public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.ViewHolder> {

    private List<DrawerMenuItem> lists;
    private String currentQuery = "";
    private OnItemClickListener listener;
    private final int highlightColor = Color.parseColor("#f59e0b"); // Orange

    public interface OnItemClickListener {
        void onItemClick(DrawerMenuItem list);
    }

    public SearchListAdapter(List<DrawerMenuItem> lists, OnItemClickListener listener) {
        this.lists = lists;
        this.listener = listener;
    }

    public void updateData(List<DrawerMenuItem> newLists, String query) {
        this.lists = newLists;
        this.currentQuery = query == null ? "" : query.toLowerCase();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DrawerMenuItem list = lists.get(position);
        holder.bind(list, currentQuery, highlightColor, listener);
    }

    @Override
    public int getItemCount() {
        return lists != null ? lists.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvEmoji;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.item_search_list_title);
            tvEmoji = itemView.findViewById(R.id.item_search_list_emoji);
            ivIcon = itemView.findViewById(R.id.item_search_list_icon);
        }

        public void bind(DrawerMenuItem list, String query, int highlightColor, OnItemClickListener listener) {
            // Icon logic based on DrawerAdapter
            if (list.getIconResId() != 0) {
                ivIcon.setImageResource(list.getIconResId());
                ivIcon.setVisibility(View.VISIBLE);
                tvEmoji.setVisibility(View.GONE);
            } else if (list.getEmojiIcon() != null && list.getEmojiIcon().startsWith("ic_")) {
                // Determine resource from string
                int resId = itemView.getContext().getResources().getIdentifier(list.getEmojiIcon(), "drawable", itemView.getContext().getPackageName());
                if (resId != 0) {
                    ivIcon.setImageResource(resId);
                    ivIcon.setVisibility(View.VISIBLE);
                    tvEmoji.setVisibility(View.GONE);
                } else {
                    ivIcon.setImageResource(R.drawable.ic_list);
                    ivIcon.setVisibility(View.VISIBLE);
                    tvEmoji.setVisibility(View.GONE);
                }
            } else if (list.getEmojiIcon() != null && !list.getEmojiIcon().isEmpty()) {
                tvEmoji.setText(list.getEmojiIcon());
                tvEmoji.setVisibility(View.VISIBLE);
                ivIcon.setVisibility(View.GONE);
            } else {
                ivIcon.setImageResource(R.drawable.ic_list);
                ivIcon.setVisibility(View.VISIBLE);
                tvEmoji.setVisibility(View.GONE);
            }

            // Highlight title
            String title = list.getTitle();
            tvTitle.setText(getHighlightedText(title, query, highlightColor));

            // Setup click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(list);
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
