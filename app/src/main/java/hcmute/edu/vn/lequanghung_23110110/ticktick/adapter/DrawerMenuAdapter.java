package hcmute.edu.vn.lequanghung_23110110.ticktick.adapter;
/*
* > **Giải thích Custom Adapter**:
>
> - `getItemViewType()` trả về loại item (NAVIGATION, LIST, SEPARATOR) → inflate layout khác nhau
> - `onBindViewHolder()` handle hiển thị icon, text, badge, chevron, selected state
> - Click listener cho mỗi item → callback lên Activity
* */
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

public class DrawerMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_SEPARATOR = 1;

    private final List<DrawerMenuItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DrawerMenuItem item, int position);
    }

    public DrawerMenuAdapter(List<DrawerMenuItem> items) {
        this.items = items;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType() == DrawerMenuItem.ItemType.SEPARATOR
                ? VIEW_TYPE_SEPARATOR : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SEPARATOR) {
            View view = inflater.inflate(R.layout.item_drawer_separator, parent, false);
            return new SeparatorViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_drawer_menu, parent, false);
            return new MenuItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MenuItemViewHolder) {
            ((MenuItemViewHolder) holder).bind(items.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // === ViewHolder cho menu item ===
    class MenuItemViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView badge;
        ImageView chevron;
        View itemContainer;

        MenuItemViewHolder(View itemView) {
            super(itemView);
            itemContainer = itemView.findViewById(R.id.drawer_item_container);
            icon = itemView.findViewById(R.id.drawer_item_icon);
            title = itemView.findViewById(R.id.drawer_item_title);
            badge = itemView.findViewById(R.id.drawer_item_badge);
            chevron = itemView.findViewById(R.id.drawer_item_chevron);
        }

        void bind(DrawerMenuItem item, int position) {
            // Icon
            if (item.getIconResId() != 0) {
                icon.setImageResource(item.getIconResId());
                icon.setVisibility(View.VISIBLE);
            } else {
                icon.setVisibility(View.GONE);
            }

            // Title
            title.setText(item.getTitle());

            // Badge
            if (item.getBadgeCount() > 0) {
                badge.setText(String.valueOf(item.getBadgeCount()));
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setVisibility(View.GONE);
            }

            // Chevron
            chevron.setVisibility(item.hasChevron() ? View.VISIBLE : View.GONE);

            // Selected state
            if (item.isSelected()) {
                itemContainer.setBackgroundResource(R.drawable.bg_drawer_item_selected);
                title.setTextColor(itemContainer.getContext().getColor(R.color.drawer_item_text_selected));
            } else {
                itemContainer.setBackground(null);
                title.setTextColor(itemContainer.getContext().getColor(R.color.drawer_item_text));
            }

            // Click
            itemContainer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item, position);
                }
            });
        }
    }

    // === ViewHolder cho separator ===
    static class SeparatorViewHolder extends RecyclerView.ViewHolder {
        SeparatorViewHolder(View itemView) {
            super(itemView);
        }
    }

    // === Public method: update selected item ===
    public void setSelectedPosition(int position) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setSelected(i == position);
        }
        notifyDataSetChanged();
    }
}