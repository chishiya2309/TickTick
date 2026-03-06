package hcmute.edu.vn.lequanghung_23110110.ticktick.adapter;

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

public class PinnedListAdapter extends RecyclerView.Adapter<PinnedListAdapter.ViewHolder> {

    private List<DrawerMenuItem> pinnedItems;
    private OnPinnedItemClickListener listener;

    public interface OnPinnedItemClickListener {
        void onItemClick(DrawerMenuItem item);
    }

    public PinnedListAdapter(List<DrawerMenuItem> pinnedItems, OnPinnedItemClickListener listener) {
        this.pinnedItems = pinnedItems;
        this.listener = listener;
    }

    public void updateData(List<DrawerMenuItem> newItems) {
        this.pinnedItems = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pinned_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DrawerMenuItem item = pinnedItems.get(position);

        holder.name.setText(item.getTitle());

        if (item.getEmojiIcon() != null && !item.getEmojiIcon().isEmpty()) {
            holder.emoji.setText(item.getEmojiIcon());
            holder.emoji.setVisibility(View.VISIBLE);
            holder.icon.setVisibility(View.GONE);
        } else if (item.getIconResId() != 0) {
            holder.icon.setImageResource(item.getIconResId());
            holder.icon.setVisibility(View.VISIBLE);
            holder.emoji.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pinnedItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView emoji;
        TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.pinned_list_icon);
            emoji = itemView.findViewById(R.id.pinned_list_emoji);
            name = itemView.findViewById(R.id.pinned_list_name);
        }
    }
}
