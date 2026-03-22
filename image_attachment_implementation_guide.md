# Hướng dẫn triển khai tính năng đính kèm ảnh vào Task

## ✅ Đã hoàn thành

### 1. Giao diện (UI)
- ✅ Đã thêm RecyclerView `rvTaskImages` vào `layout_bottom_sheet_task_detail.xml`
- ✅ Đã tạo `item_task_image.xml` cho từng ảnh (80x80dp, bo góc, nút xóa)
- ✅ Đã tạo `ImageAttachmentBottomSheet` với 2 tùy chọn: Chụp ảnh mới và Chọn từ thư viện
- ✅ Đã tạo `TaskImageAdapter` để quản lý danh sách ảnh
- ✅ Đã tích hợp vào `TaskDetailBottomSheet.java`

### 2. Các file đã tạo/cập nhật
- `app/src/main/res/layout/layout_bottom_sheet_task_detail.xml` - Đã thêm RecyclerView
- `app/src/main/res/layout/item_task_image.xml` - Layout cho từng ảnh
- `app/src/main/res/layout/layout_bottom_sheet_image_attachment.xml` - Dialog chọn nguồn ảnh
- `app/src/main/java/.../dialog/ImageAttachmentBottomSheet.java` - BottomSheet chọn nguồn
- `app/src/main/java/.../adapter/TaskImageAdapter.java` - Adapter cho RecyclerView
- `app/src/main/java/.../dialog/TaskDetailBottomSheet.java` - Đã tích hợp các thành phần

## 📋 Cần triển khai tiếp

### 1. Thêm thư viện Glide vào build.gradle
Mở file `app/build.gradle.kts` và thêm dependency:

```kotlin
dependencies {
    // ... các dependency hiện có
    
    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
}
```

### 2. Thêm permissions vào AndroidManifest.xml
Mở `app/src/main/AndroidManifest.xml` và thêm:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

### 3. Cập nhật TaskModel để lưu danh sách ảnh
Mở `TaskModel.java` và thêm:

```java
private String imageUris; // Lưu dạng JSON string hoặc comma-separated

public String getImageUris() {
    return imageUris;
}

public void setImageUris(String imageUris) {
    this.imageUris = imageUris;
}

// Helper methods
public List<String> getImageList() {
    if (imageUris == null || imageUris.isEmpty()) {
        return new ArrayList<>();
    }
    return Arrays.asList(imageUris.split(","));
}

public void setImageList(List<String> images) {
    if (images == null || images.isEmpty()) {
        this.imageUris = "";
    } else {
        this.imageUris = TextUtils.join(",", images);
    }
}
```

### 4. Cập nhật Database Schema
Mở `TaskDatabaseHelper.java` và:

**a) Thêm cột mới:**
```java
private static final String COLUMN_IMAGE_URIS = "image_uris";
```

**b) Cập nhật CREATE TABLE:**
```java
private static final String CREATE_TABLE_TASKS = "CREATE TABLE " + TABLE_TASKS + " ("
    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
    + COLUMN_TITLE + " TEXT NOT NULL, "
    + COLUMN_DESCRIPTION + " TEXT, "
    + COLUMN_LIST_ID + " INTEGER, "
    + COLUMN_COMPLETED + " INTEGER DEFAULT 0, "
    + COLUMN_DATE_TAG + " TEXT, "
    + COLUMN_IMAGE_URIS + " TEXT, "  // <-- Thêm dòng này
    + "FOREIGN KEY(" + COLUMN_LIST_ID + ") REFERENCES " + TABLE_LISTS + "(" + COLUMN_ID + ")"
    + ");";
```

**c) Thêm method update images:**
```java
public void updateTaskImages(long taskId, String imageUris) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(COLUMN_IMAGE_URIS, imageUris);
    db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", 
              new String[]{String.valueOf(taskId)});
}
```

**d) Cập nhật onUpgrade để migrate database:**
```java
@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion < 2) { // Giả sử version mới là 2
        db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + 
                   COLUMN_IMAGE_URIS + " TEXT");
    }
}
```

**e) Tăng DATABASE_VERSION:**
```java
private static final int DATABASE_VERSION = 2; // Tăng từ 1 lên 2
```

### 5. Triển khai chức năng chụp ảnh và chọn từ thư viện

Trong `TaskDetailBottomSheet.java`, thêm:

**a) Khai báo ActivityResultLauncher:**
```java
private ActivityResultLauncher<Intent> cameraLauncher;
private ActivityResultLauncher<Intent> galleryLauncher;
private Uri tempImageUri;
```

**b) Khởi tạo launchers trong onCreate hoặc onAttach:**
```java
@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // Camera launcher
    cameraLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (tempImageUri != null) {
                    addImageToTask(tempImageUri.toString());
                }
            }
        }
    );
    
    // Gallery launcher
    galleryLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri selectedImage = result.getData().getData();
                if (selectedImage != null) {
                    addImageToTask(selectedImage.toString());
                }
            }
        }
    );
}
```

**c) Implement methods để mở camera và gallery:**
```java
private void openCamera() {
    if (ContextCompat.checkSelfPermission(requireContext(), 
            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, 
                          REQUEST_CAMERA_PERMISSION);
        return;
    }
    
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
        File photoFile = createImageFile();
        if (photoFile != null) {
            tempImageUri = FileProvider.getUriForFile(requireContext(),
                "hcmute.edu.vn.lequanghung_23110110.ticktick.fileprovider",
                photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);
            cameraLauncher.launch(takePictureIntent);
        }
    }
}

private void openGallery() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 
                              REQUEST_GALLERY_PERMISSION);
            return;
        }
    } else {
        if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
                              REQUEST_GALLERY_PERMISSION);
            return;
        }
    }
    
    Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, 
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    galleryLauncher.launch(pickPhotoIntent);
}

private File createImageFile() {
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    String imageFileName = "TASK_" + timeStamp + "_";
    File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    try {
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
}

private void addImageToTask(String imageUri) {
    taskImages.add(imageUri);
    imageAdapter.addImage(imageUri);
    updateImageVisibility();
    
    // Save to database
    if (task != null) {
        task.setImageList(taskImages);
        dbHelper.updateTaskImages(task.getId(), task.getImageUris());
    }
}
```

**d) Cập nhật showImageAttachmentOptions:**
```java
private void showImageAttachmentOptions() {
    ImageAttachmentBottomSheet bottomSheet = new ImageAttachmentBottomSheet();
    bottomSheet.setOnImageSourceSelectedListener(new ImageAttachmentBottomSheet.OnImageSourceSelectedListener() {
        @Override
        public void onCameraSelected() {
            openCamera();
        }

        @Override
        public void onGallerySelected() {
            openGallery();
        }
    });
    bottomSheet.show(getParentFragmentManager(), "ImageAttachmentBottomSheet");
}
```

**e) Load images khi mở task:**
```java
// Trong onViewCreated, sau khi bind data
if (task != null && task.getImageUris() != null && !task.getImageUris().isEmpty()) {
    taskImages = new ArrayList<>(task.getImageList());
    imageAdapter.setImages(taskImages);
    updateImageVisibility();
}
```

### 6. Thêm FileProvider configuration

**a) Tạo file `res/xml/file_paths.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path name="task_images" path="Pictures/" />
</paths>
```

**b) Thêm vào AndroidManifest.xml trong thẻ <application>:**
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="hcmute.edu.vn.lequanghung_23110110.ticktick.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### 7. Thêm imports cần thiết
```java
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
```

### 8. Constants
```java
private static final int REQUEST_CAMERA_PERMISSION = 100;
private static final int REQUEST_GALLERY_PERMISSION = 101;
```

## 🎨 Tùy chỉnh thêm (Optional)

### 1. Tạo icon attachment đẹp hơn
Tạo file `res/drawable/ic_attachment.xml`:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@color/main_text_secondary"
        android:pathData="M16.5,6v11.5c0,2.21 -1.79,4 -4,4s-4,-1.79 -4,-4V5c0,-1.38 1.12,-2.5 2.5,-2.5s2.5,1.12 2.5,2.5v10.5c0,0.55 -0.45,1 -1,1s-1,-0.45 -1,-1V6H10v9.5c0,1.38 1.12,2.5 2.5,2.5s2.5,-1.12 2.5,-2.5V5c0,-2.21 -1.79,-4 -4,-4S7,2.79 7,5v12.5c0,3.04 2.46,5.5 5.5,5.5s5.5,-2.46 5.5,-5.5V6H16.5z"/>
</vector>
```

Sau đó cập nhật trong layout:
```xml
android:src="@drawable/ic_attachment"
```

### 2. Thêm animation khi thêm/xóa ảnh
Trong `TaskImageAdapter`:
```java
@Override
public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
    // ... existing code
    
    // Add fade-in animation
    holder.itemView.setAlpha(0f);
    holder.itemView.animate()
        .alpha(1f)
        .setDuration(300)
        .start();
}
```

### 3. Xem ảnh full screen
Tạo một Activity hoặc Dialog mới để hiển thị ảnh full screen khi click vào ảnh.

## 🧪 Testing

1. Build và chạy app
2. Mở một task
3. Click vào nút đính kèm
4. Chọn "Chụp ảnh mới" hoặc "Chọn từ thư viện"
5. Kiểm tra ảnh hiển thị trong RecyclerView
6. Thử xóa ảnh bằng nút X
7. Đóng và mở lại task để kiểm tra ảnh đã được lưu

## ⚠️ Lưu ý

- Cần test trên thiết bị thật để kiểm tra camera
- Xử lý trường hợp thiết bị không có camera
- Xử lý trường hợp người dùng từ chối quyền
- Cân nhắc giới hạn số lượng ảnh tối đa (ví dụ: 5 ảnh/task)
- Cân nhắc nén ảnh để tiết kiệm dung lượng

Chúc bạn triển khai thành công! 🚀
