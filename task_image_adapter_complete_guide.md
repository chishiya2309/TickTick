# Hướng dẫn hoàn chỉnh: TaskImageAdapter với Glide

## ✅ Đã hoàn thành

### 1. Thêm Glide vào build.gradle.kts

```kotlin
dependencies {
    // ... existing dependencies
    
    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
}
```

**Sau khi thêm, cần Sync Gradle:**
- Click "Sync Now" ở banner trên cùng
- Hoặc: File → Sync Project with Gradle Files

### 2. TaskImageAdapter - Tính năng đầy đủ

#### a) Interface Callback

```java
public interface OnImageActionListener {
    /**
     * Được gọi khi user click vào ảnh (để xem full screen)
     */
    void onImageClick(String imageUri, int position);
    
    /**
     * Được gọi khi user click nút xóa ảnh
     */
    void onImageDelete(String imageUri, int position);
}
```

#### b) Constructor và Properties

```java
private List<String> imageUris;
private OnImageActionListener listener;

public TaskImageAdapter() {
    this.imageUris = new ArrayList<>();
}
```

#### c) Public Methods

**setOnImageActionListener()** - Set callback listener
```java
public void setOnImageActionListener(OnImageActionListener listener) {
    this.listener = listener;
}
```

**setImages()** - Set toàn bộ danh sách ảnh mới
```java
public void setImages(List<String> images) {
    this.imageUris = images != null ? images : new ArrayList<>();
    notifyDataSetChanged();
}
```

**addImage()** - Thêm một ảnh mới
```java
public void addImage(String imageUri) {
    if (imageUri == null || imageUri.isEmpty()) {
        return; // Validate input
    }
    imageUris.add(imageUri);
    notifyItemInserted(imageUris.size() - 1);
}
```

**removeImage()** - Xóa ảnh tại vị trí
```java
public void removeImage(int position) {
    if (position >= 0 && position < imageUris.size()) {
        imageUris.remove(position);
        notifyItemRemoved(position);
        // Cập nhật lại position của các item sau khi xóa
        notifyItemRangeChanged(position, imageUris.size());
    }
}
```

**getImages()** - Lấy copy của danh sách
```java
public List<String> getImages() {
    return new ArrayList<>(imageUris);
}
```

**isEmpty()** - Kiểm tra danh sách rỗng
```java
public boolean isEmpty() {
    return imageUris.isEmpty();
}
```

#### d) onBindViewHolder() - Load ảnh với Glide

```java
@Override
public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
    String imageUri = imageUris.get(position);
    
    // Cấu hình Glide options
    RequestOptions options = new RequestOptions()
            .centerCrop()                              // Crop ảnh vừa khung
            .placeholder(R.drawable.ic_action_grid)    // Hiển thị khi đang load
            .error(R.drawable.ic_action_grid)          // Hiển thị khi có lỗi
            .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cache cả original và resized
            .override(200, 200);                       // Resize để tiết kiệm memory
    
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
```

### 3. Glide Configuration - Chi tiết

#### RequestOptions

**centerCrop():**
- Crop ảnh để fill toàn bộ ImageView
- Giữ tỷ lệ khung hình
- Phần thừa sẽ bị cắt

**placeholder():**
- Hiển thị trong khi ảnh đang được load
- Tránh ImageView trống khi load chậm
- Sử dụng drawable có sẵn

**error():**
- Hiển thị khi load ảnh thất bại
- Xử lý trường hợp URI không hợp lệ
- Xử lý trường hợp file bị xóa

**diskCacheStrategy(DiskCacheStrategy.ALL):**
- Cache cả ảnh gốc và ảnh đã resize
- Tăng tốc độ load lần sau
- Tiết kiệm bandwidth

**override(200, 200):**
- Resize ảnh xuống 200x200px
- Tiết kiệm memory
- Tăng performance khi scroll

### 4. Cách sử dụng trong TaskDetailBottomSheet

#### a) Khởi tạo Adapter

```java
// Trong onViewCreated()
rvTaskImages = view.findViewById(R.id.rvTaskImages);
LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), 
        LinearLayoutManager.HORIZONTAL, false);
rvTaskImages.setLayoutManager(layoutManager);

imageAdapter = new TaskImageAdapter();
imageAdapter.setOnImageActionListener(new TaskImageAdapter.OnImageActionListener() {
    @Override
    public void onImageClick(String imageUri, int position) {
        // TODO: Mở ảnh full screen
        Toast.makeText(requireContext(), "Xem ảnh: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onImageDelete(String imageUri, int position) {
        // Xóa khỏi adapter
        imageAdapter.removeImage(position);
        
        // Xóa khỏi list
        taskImages.remove(position);
        
        // Cập nhật visibility
        updateImageVisibility();
        
        // Lưu vào database
        if (task != null) {
            task.setImageUris(taskImages);
            dbHelper.updateTaskImages(task.getId(), taskImages);
        }
        
        Toast.makeText(requireContext(), "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
    }
});
rvTaskImages.setAdapter(imageAdapter);
```

#### b) Load ảnh hiện có

```java
// Trong onViewCreated(), sau khi bind data
if (task != null && task.getImageUris() != null && !task.getImageUris().isEmpty()) {
    taskImages = new ArrayList<>(task.getImageUris());
    imageAdapter.setImages(taskImages);
    updateImageVisibility();
}
```

#### c) Thêm ảnh mới

```java
private void addImageToTask(String imageUri) {
    // Thêm vào list
    taskImages.add(imageUri);
    
    // Thêm vào adapter
    imageAdapter.addImage(imageUri);
    
    // Hiển thị RecyclerView
    updateImageVisibility();
    
    // Lưu vào database
    if (task != null) {
        task.setImageUris(taskImages);
        dbHelper.updateTaskImages(task.getId(), taskImages);
    }
    
    Toast.makeText(requireContext(), "Đã thêm ảnh", Toast.LENGTH_SHORT).show();
}
```

#### d) Cập nhật visibility

```java
private void updateImageVisibility() {
    if (taskImages.isEmpty()) {
        rvTaskImages.setVisibility(View.GONE);
    } else {
        rvTaskImages.setVisibility(View.VISIBLE);
    }
}
```

### 5. Glide - Các tính năng nâng cao

#### a) Thumbnail (Load ảnh nhỏ trước)

```java
Glide.with(context)
    .load(imageUri)
    .thumbnail(0.1f) // Load 10% size trước
    .into(imageView);
```

#### b) Transition Animation

```java
Glide.with(context)
    .load(imageUri)
    .transition(DrawableTransitionOptions.withCrossFade())
    .into(imageView);
```

#### c) Transform (Bo góc tròn)

```java
Glide.with(context)
    .load(imageUri)
    .transform(new RoundedCorners(16)) // 16dp radius
    .into(imageView);
```

#### d) Preload (Load trước)

```java
Glide.with(context)
    .load(imageUri)
    .preload();
```

#### e) Clear cache

```java
// Clear memory cache
Glide.get(context).clearMemory();

// Clear disk cache (phải chạy trên background thread)
new Thread(() -> {
    Glide.get(context).clearDiskCache();
}).start();
```

### 6. Performance Tips

#### a) Sử dụng RecyclerView.RecycledViewPool

```java
// Trong Activity/Fragment
RecyclerView.RecycledViewPool pool = new RecyclerView.RecycledViewPool();
pool.setMaxRecycledViews(0, 10); // Cache 10 items

rvTaskImages.setRecycledViewPool(pool);
```

#### b) Set fixed size nếu biết trước

```java
rvTaskImages.setHasFixedSize(true);
```

#### c) Disable nested scrolling nếu không cần

```java
rvTaskImages.setNestedScrollingEnabled(false);
```

#### d) Sử dụng DiffUtil cho update hiệu quả

```java
// Thay vì notifyDataSetChanged()
DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
    // Implement methods
});
diffResult.dispatchUpdatesTo(adapter);
```

### 7. Error Handling

#### a) Xử lý URI không hợp lệ

```java
public void addImage(String imageUri) {
    if (imageUri == null || imageUri.isEmpty()) {
        return; // Validate input
    }
    
    try {
        Uri.parse(imageUri); // Test parse
        imageUris.add(imageUri);
        notifyItemInserted(imageUris.size() - 1);
    } catch (Exception e) {
        e.printStackTrace();
        // Log error hoặc hiển thị toast
    }
}
```

#### b) Xử lý position không hợp lệ

```java
holder.btnDeleteImage.setOnClickListener(v -> {
    if (listener != null) {
        int currentPosition = holder.getAdapterPosition();
        if (currentPosition != RecyclerView.NO_POSITION) {
            listener.onImageDelete(imageUri, currentPosition);
        }
    }
});
```

#### c) Xử lý Glide load error

```java
Glide.with(context)
    .load(imageUri)
    .listener(new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, 
                                   Target<Drawable> target, boolean isFirstResource) {
            // Log error
            Log.e("Glide", "Load failed: " + e.getMessage());
            return false; // Return false để hiển thị error drawable
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, 
                                      Target<Drawable> target, DataSource dataSource, 
                                      boolean isFirstResource) {
            // Load thành công
            return false;
        }
    })
    .into(imageView);
```

### 8. Testing Checklist

- [ ] Thêm ảnh từ camera
- [ ] Thêm ảnh từ gallery
- [ ] Thêm nhiều ảnh (test scroll horizontal)
- [ ] Xóa ảnh
- [ ] Xóa ảnh ở giữa list (test notifyItemRangeChanged)
- [ ] Click vào ảnh (test callback)
- [ ] Load ảnh khi mở task (test database)
- [ ] Đóng và mở lại task (test persistence)
- [ ] Test với URI không hợp lệ (test error handling)
- [ ] Test với file đã bị xóa (test error drawable)
- [ ] Test scroll performance (test Glide cache)
- [ ] Test memory usage (test override size)
- [ ] Test animation fade-in
- [ ] Test trên nhiều kích thước màn hình

### 9. Tối ưu hóa bổ sung

#### a) Lazy loading với Paging

Nếu có nhiều ảnh (>20), cân nhắc sử dụng Paging library:

```kotlin
// Trong build.gradle.kts
implementation("androidx.paging:paging-runtime:3.2.1")
```

#### b) Image compression

Nén ảnh trước khi lưu để tiết kiệm storage:

```java
Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
ByteArrayOutputStream baos = new ByteArrayOutputStream();
bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos); // 80% quality
```

#### c) Thumbnail generation

Tạo thumbnail riêng để load nhanh hơn:

```java
Bitmap thumbnail = ThumbnailUtils.extractThumbnail(
    originalBitmap, 
    200, 
    200
);
```

## 🎉 Hoàn thành!

TaskImageAdapter đã được triển khai đầy đủ với:
- ✅ Glide integration
- ✅ Error handling
- ✅ Loading placeholder
- ✅ Fade-in animation
- ✅ Memory optimization
- ✅ Disk caching
- ✅ Callback interface
- ✅ Position validation
- ✅ Input validation

Bạn có thể build và test app ngay bây giờ! 🚀
