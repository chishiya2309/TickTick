package hcmute.edu.vn.lequanghung_23110110.ticktick.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.EmojiResponse;

public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder> {

    private List<EmojiResponse> emojiList;
    private OnEmojiClickListener listener;

    public interface OnEmojiClickListener {
        void onEmojiClick(String emoji);
    }

    public EmojiAdapter(List<EmojiResponse> emojiList, OnEmojiClickListener listener) {
        this.emojiList = emojiList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emoji, parent, false);
        return new EmojiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
        EmojiResponse emoji = emojiList.get(position);
        holder.textEmoji.setText(emoji.character);
        holder.textEmoji.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEmojiClick(emoji.character);
            }
        });
    }

    @Override
    public int getItemCount() {
        return emojiList != null ? emojiList.size() : 0;
    }

    // Cập nhật danh sách mới khi tìm kiếm
    public void updateList(List<EmojiResponse> newList) {
        this.emojiList = newList;
        notifyDataSetChanged();
    }

    static class EmojiViewHolder extends RecyclerView.ViewHolder {
        TextView textEmoji;

        EmojiViewHolder(@NonNull View itemView) {
            super(itemView);
            textEmoji = itemView.findViewById(R.id.text_emoji);
        }
    }
}
