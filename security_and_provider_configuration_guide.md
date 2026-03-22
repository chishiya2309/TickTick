# Hướng dẫn Cấu hình Bảo mật và Content Provider

## ✅ Đã hoàn thành

### 1. Permissions trong AndroidManifest.xml

Đã thêm các quyền sau vào `AndroidManifest.xml`:

```xml
<!-- Quyền cho Camera và Thư viện ảnh -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

**Giải thích:**
- `CAMERA`: Quyền truy cập camera để chụp ảnh
- `READ_EXTERNAL_STORAGE`: Quyền đọc storage cho Android 12 trở xuống (API ≤ 32)
- `READ_MEDIA_IMAGES`: Quyền đọc ảnh cho Android 13+ (API 33+)
- `android:maxSdkVersion="32"`: Chỉ yêu cầu READ_EXTERNAL_STORAGE trên Android 12 trở xuống
- `android:required="false"`: Camera không bắt buộc (app vẫn chạy được trên thiết bị không có camera)

### 2. FileProvider Configuration

Đã thêm FileProvider vào `AndroidManifest.xml`:

```xml
<!-- FileProvider cho Camera và chia sẻ file ảnh -->
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

**Giải thích:**
- `android:name`: Sử dụng FileProvider của AndroidX
- `android:authorities`: Định danh duy nhất cho provider (phải khớp với code)
- `android:exported="false"`: Không cho phép app khác truy cập trực tiếp
- `android:grantUriPermissions="true"`: Cho phép cấp quyền tạm thời cho URI
- `meta-data`: Trỏ đến file cấu hình đường dẫn

### 3. File Paths Configuration (file_paths.xml)

Đã tạo file `app/src/main/res/xml/file_paths.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Thư mục Pictures trong external files -->
    <external-files-path 
        name="task_images" 
        path="Pictures/" />
    
    <!-- Cache cho ảnh tạm thời -->
    <external-cache-path 
        name="temp_images" 
        path="/" />
</paths>
```

**Giải thích:**
- `external-files-path`: Trỏ đến `Context.getExternalFilesDir()`
  - Đường dẫn thực tế: `/storage/emulated/0/Android/data/[package]/files/Pictures/`
  - Dữ liệu sẽ bị xóa khi gỡ app
  - An toàn, không cần permission từ Android 4.4+
  
- `external-cache-path`: Trỏ đến `Context.getExternalCacheDir()`
  - Dùng cho file tạm thời
  - Hệ thống có thể tự động xóa khi thiếu dung lượng

**Các loại path khác có thể dùng:**
- `files-path`: Internal storage (`Context.getFilesDir()`)
- `cache-path`: Internal cache (`Context.getCacheDir()`)
- `external-path`: Root external storage (cần permission)

### 4. Helper Classes

#### a) PermissionHelper.java
Class tiện ích để kiểm tra và quản lý permissions:

**Các method chính:**
- `hasCameraPermission(Context)`: Kiểm tra quyền camera
- `hasGalleryPermission(Context)`: Kiểm tra quyền đọc ảnh (tự động xử lý Android 13+)
- `getCameraPermissions()`: Lấy array permissions cho camera
- `getGalleryPermissions()`: Lấy array permissions cho gallery (tự động chọn đúng permission)
- `hasCamera(Context)`: Kiểm tra thiết bị có camera không

**Ví dụ sử dụng:**
```java
// Kiểm tra quyền camera
if (!PermissionHelper.hasCameraPermission(context)) {
    requestPermissions(
        PermissionHelper.getCameraPermissions(),
        PermissionHelper.REQUEST_CAMERA_PERMISSION
    );
}

// Kiểm tra quyền gallery
if (!PermissionHelper.hasGalleryPermission(context)) {
    requestPermissions(
        PermissionHelper.getGalleryPermissions(),
        PermissionHelper.REQUEST_GALLERY_PERMISSION
    );
}
```

#### b) ImageFileHelper.java
Class tiện ích để quản lý file ảnh:

**Các method chính:**
- `createImageFile(Context)`: Tạo file ảnh tạm thời với tên unique
- `getUriForFile(Context, File)`: Chuyển File thành Uri an toàn qua FileProvider
- `deleteImageFile(String)`: Xóa file ảnh
- `fileExists(String)`: Kiểm tra file có tồn tại
- `getFileSizeMB(String)`: Lấy kích thước file
- `getPicturesDirectory(Context)`: Lấy thư mục Pictures

**Ví dụ sử dụng:**
```java
// Tạo file cho camera
File photoFile = ImageFileHelper.createImageFile(context);
if (photoFile != null) {
    Uri photoUri = ImageFileHelper.getUriForFile(context, photoFile);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
}

// Xóa ảnh
ImageFileHelper.deleteImageFile(imagePath);
```

## 🔒 Bảo mật và Best Practices

### 1. Tại sao cần FileProvider?

**Vấn đề:** Từ Android 7.0 (API 24), việc chia sẻ `file://` URI trực tiếp bị cấm vì lý do bảo mật.

**Giải pháp:** Sử dụng FileProvider để tạo `content://` URI an toàn:
- Cấp quyền tạm thời cho app khác
- Không lộ đường dẫn file thực tế
- Tự động thu hồi quyền sau khi sử dụng

### 2. Scoped Storage (Android 10+)

**Lợi ích của external-files-path:**
- Không cần permission READ_EXTERNAL_STORAGE
- App có quyền đầy đủ trong thư mục riêng
- Dữ liệu tự động xóa khi gỡ app
- Không làm rối shared storage

### 3. Permission Runtime (Android 6.0+)

**Lưu ý:**
- Phải request permission lúc runtime, không chỉ khai báo trong Manifest
- Kiểm tra permission trước mỗi lần sử dụng
- Xử lý trường hợp user từ chối permission

### 4. Android 13+ Changes

**Thay đổi quan trọng:**
- `READ_EXTERNAL_STORAGE` không còn hiệu lực
- Phải dùng `READ_MEDIA_IMAGES` cho ảnh
- Granular permissions (ảnh, video, audio riêng biệt)

## 📱 Luồng hoạt động

### Camera Flow:
```
1. Kiểm tra permission CAMERA
2. Tạo file tạm thời bằng ImageFileHelper.createImageFile()
3. Chuyển File thành Uri bằng ImageFileHelper.getUriForFile()
4. Gửi Uri cho Camera app qua Intent với EXTRA_OUTPUT
5. Camera lưu ảnh vào file đã chỉ định
6. Lấy Uri để hiển thị và lưu vào database
```

### Gallery Flow:
```
1. Kiểm tra permission READ_MEDIA_IMAGES (Android 13+) hoặc READ_EXTERNAL_STORAGE
2. Mở Intent ACTION_PICK với MediaStore.Images.Media.EXTERNAL_CONTENT_URI
3. Nhận Uri của ảnh đã chọn từ result
4. Sử dụng Uri trực tiếp (không cần copy file)
5. Lưu Uri vào database
```

## 🧪 Testing Checklist

- [ ] Test trên Android 6.0 (Runtime permissions)
- [ ] Test trên Android 10 (Scoped Storage)
- [ ] Test trên Android 13+ (READ_MEDIA_IMAGES)
- [ ] Test khi user từ chối permission
- [ ] Test trên thiết bị không có camera
- [ ] Test khi storage đầy
- [ ] Test xóa và cài lại app (dữ liệu phải bị xóa)

## 🔧 Troubleshooting

### Lỗi: FileUriExposedException
**Nguyên nhân:** Sử dụng `file://` URI thay vì `content://`
**Giải pháp:** Dùng FileProvider.getUriForFile()

### Lỗi: Permission Denial
**Nguyên nhân:** Chưa request runtime permission
**Giải pháp:** Kiểm tra và request permission trước khi sử dụng

### Lỗi: IllegalArgumentException (FileProvider)
**Nguyên nhân:** File path không nằm trong các path đã khai báo trong file_paths.xml
**Giải pháp:** Kiểm tra lại cấu hình file_paths.xml

### Camera không mở được
**Nguyên nhân:** Thiết bị không có camera hoặc camera đang được sử dụng
**Giải pháp:** Kiểm tra hasCamera() trước khi mở camera

## 📚 Tài liệu tham khảo

- [FileProvider Documentation](https://developer.android.com/reference/androidx/core/content/FileProvider)
- [Request App Permissions](https://developer.android.com/training/permissions/requesting)
- [Scoped Storage](https://developer.android.com/about/versions/11/privacy/storage)
- [Photo Picker](https://developer.android.com/training/data-storage/shared/photopicker)

## ✨ Tính năng nâng cao (Optional)

### 1. Photo Picker (Android 13+)
Thay vì dùng ACTION_PICK, có thể dùng Photo Picker mới:
```java
ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
    registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
        if (uri != null) {
            // Handle selected image
        }
    });

// Launch picker
pickMedia.launch(new PickVisualMediaRequest.Builder()
    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
    .build());
```

**Lợi ích:**
- Không cần permission
- UI thân thiện hơn
- Tích hợp với Google Photos

### 2. Image Compression
Nén ảnh trước khi lưu để tiết kiệm dung lượng:
```java
Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
ByteArrayOutputStream baos = new ByteArrayOutputStream();
bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
```

### 3. Thumbnail Generation
Tạo thumbnail để load nhanh hơn trong RecyclerView:
```java
Bitmap thumbnail = ThumbnailUtils.extractThumbnail(
    originalBitmap, 
    THUMB_WIDTH, 
    THUMB_HEIGHT
);
```

---

**Hoàn thành!** Cấu hình bảo mật và FileProvider đã sẵn sàng để sử dụng. 🎉
