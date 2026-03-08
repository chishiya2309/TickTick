package hcmute.edu.vn.lequanghung_23110110.ticktick.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.ListModel;

public class MoveTaskListAdapter extends RecyclerView.Adapter<MoveTaskListAdapter.ViewHolder> {

    private List<ListModel> originalLists;
    private List<ListModel> lists; // effectively filteredLists
    private final Context context;
    private final int currentListId;
    private OnListSelectedListener listener;

    public interface OnListSelectedListener {
        void onListSelected(ListModel list);
    }

    public MoveTaskListAdapter(Context context, List<ListModel> lists, int currentListId) {
        this.context = context;
        this.originalLists = lists;
        this.lists = lists;
        this.currentListId = currentListId;
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            lists = originalLists;
        } else {
            String lowerQuery = query.toLowerCase().trim();
            List<ListModel> filtered = new java.util.ArrayList<>();
            for (ListModel item : originalLists) {
                if (item.getName().toLowerCase().contains(lowerQuery)) {
                    filtered.add(item);
                }
            }
            lists = filtered;
        }
        notifyDataSetChanged();
    }

    public void setOnListSelectedListener(OnListSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_move_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListModel item = lists.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvEmoji;
        TextView tvName;
        ImageView ivCheck;
        View container;

        ViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.item_list_container);
            ivIcon = itemView.findViewById(R.id.iv_list_icon);
            tvEmoji = itemView.findViewById(R.id.tv_list_emoji);
            tvName = itemView.findViewById(R.id.tv_list_name);
            ivCheck = itemView.findViewById(R.id.iv_list_check);
        }

        void bind(ListModel item) {
            tvName.setText(item.getName());

            // Handle Icon/Emoji identically to DrawerMenuAdapter
            String iconName = item.getIconName();
            if (iconName != null && !iconName.isEmpty()) {
                if (iconName.startsWith("ic_")) {
                    int resId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                    if (resId != 0) {
                        ivIcon.setImageResource(resId);
                        ivIcon.setVisibility(View.VISIBLE);
                        tvEmoji.setVisibility(View.GONE);
                    }
                } else {
                    tvEmoji.setText(iconName);
                    tvEmoji.setVisibility(View.VISIBLE);
                    ivIcon.setVisibility(View.GONE);
                }
            } else {
                ivIcon.setVisibility(View.GONE);
                tvEmoji.setVisibility(View.GONE);
            }

            // Show checkmark if this is the current list
            if (item.getId() == currentListId) {
                ivCheck.setVisibility(View.VISIBLE);
                tvName.setTextColor(context.getColor(R.color.main_accent_blue));
            } else {
                ivCheck.setVisibility(View.GONE);
                tvName.setTextColor(context.getColor(R.color.main_text_primary));
            }

            container.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onListSelected(item);
                }
            });
        }
    }
}
