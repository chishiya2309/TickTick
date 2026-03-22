package hcmute.edu.vn.lequanghung_23110110.ticktick.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;

/**
 * Adapter để hiển thị danh sách ảnh đính kèm trong Task
 * Sử dụng Glide để load ảnh từ URI
 */
public class TaskImageAdapter extends RecyclerView.Adapter<TaskImageAdapter.ImageViewHolder> {

    private List<String> imageUris;
    private OnImageActionListener listener;

    /**
     * Interface callback để xử lý sự kiện click và delete ảnh
     */
    public interface OnImageActionListener {
        /**
         * Được gọi khi user click vào ảnh (để xem full screen)
         * @param imageUri URI của ảnh
         * @param position Vị trí trong list
         */
        void onImageClick(String imageUri, int position);
        
        /**
         * Được gọi khi user click nút xóa ảnh
         * @param imageUri URI của ảnh cần xóa
         * @param position Vị trí trong list
         */
        void onImageDelete(String imageUri, int position);
    }

    public TaskImageAdapter() {
        this.imageUris = new ArrayList<>();
    }

    /**
     * Set listener để nhận callback khi có sự kiện
     */
    public void setOnImageActionListener(OnImageActionListener listener) {
        this.listener = listener;
    }

    /**
     * Set toàn bộ danh sách ảnh mới
     * @param images Danh sách URI ảnh
     */
    public void setImages(List<String> images) {
        this.imageUris = images != null ? images : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Thêm một ảnh mới vào cuối danh sách
     * @param imageUri URI của ảnh mới
     */
    public void addImage(String imageUri) {
        if (imageUri == null || imageUri.isEmpty()) {
            return;
        }
        imageUris.add(imageUri);
        notifyItemInserted(imageUris.size() - 1);
    }

    /**
     * Xóa ảnh tại vị trí chỉ định
     * @param position Vị trí cần xóa
     */
    public void removeImage(int position) {
        if (position >= 0 && position < imageUris.size()) {
            imageUris.remove(position);
            notifyItemRemoved(position);
            // Cập nhật lại position của các item sau khi xóa
            notifyItemRangeChanged(position, imageUris.size());
        }
    }

    /**
     * Lấy danh sách ảnh hiện tại (copy)
     * @return Copy của danh sách URI ảnh
     */
    public List<String> getImages() {
        return new ArrayList<>(imageUris);
    }

    /**
     * Kiểm tra danh sách có rỗng không
     */
    public boolean isEmpty() {
        return imageUris.isEmpty();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUri = imageUris.get(position);
        
        // Cấu hình Glide options
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_action_grid) // Hiển thị khi đang load
                .error(R.drawable.ic_action_grid) // Hiển thị khi có lỗi
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache cả original và resized
                .override(200, 200); // Resize để tiết kiệm memory
        
        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(Uri.parse(imageUri))
                .apply(options)
                .into(holder.ivTaskImage);

        // Click vào ảnh để xem full screen
        holder.ivTaskImage.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(imageUri, holder.getAdapterPosition());
            }
        });

        // Click nút X để xóa ảnh
        holder.btnDeleteImage.setOnClickListener(v -> {
            if (listener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onImageDelete(imageUri, currentPosition);
                }
            }
        });
        
        // Thêm animation fade-in khi item được bind
        holder.itemView.setAlpha(0f);
        holder.itemView.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    /**
     * ViewHolder cho mỗi item ảnh
     */
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTaskImage;
        ImageButton btnDeleteImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTaskImage = itemView.findViewById(R.id.ivTaskImage);
            btnDeleteImage = itemView.findViewById(R.id.btnDeleteImage);
        }
    }
}
