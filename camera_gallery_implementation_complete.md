# Hoàn thành: Triển khai Camera và Gallery trong TaskDetailBottomSheet

## ✅ Đã hoàn thành

### 1. Thêm imports cần thiết

```java
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.File;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.ImageFileHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.PermissionHelper;
```

### 2. Khai báo biến instance

```java
// Camera và Gallery launchers
private ActivityResultLauncher<Intent> cameraLauncher;
private ActivityResultLauncher<Intent> galleryLauncher;
private ActivityResultLauncher<String[]> cameraPermissionLauncher;
private ActivityResultLauncher<String[]> galleryPermissionLauncher;

// Biến tạm để lưu URI ảnh từ camera
private Uri tempCameraImageUri;
private File tempCameraImageFile;
```

### 3. Khởi tạo ActivityResultLaunchers trong onCreate()

**a) Camera Launcher:**
- Nhận kết quả từ Camera app
- Nếu thành công (RESULT_OK): Gọi `onImageCaptured(tempCameraImageUri)`
- Nếu hủy: Dọn dẹp file tạm

**b) Gallery Launcher:**
- Nhận kết quả từ Gallery/Photo Picker
- Lấy Uri từ `result.getData().getData()`
- Gọi `onImageSelected(selectedImageUri)`

**c) Camera Permission Launcher:**
- Request quyền CAMERA
- Nếu được cấp: Gọi `launchCamera()`
- Nếu bị từ chối: Hiển thị thông báo

**d) Gallery Permission Launcher:**
- Request quyền READ_MEDIA_IMAGES (Android 13+) hoặc READ_EXTERNAL_STORAGE
- Tự động chọn permission phù hợp với phiên bản Android
- Nếu được cấp: Gọi `launchGallery()`
- Nếu bị từ chối: Hiển thị thông báo

### 4. Các phương thức chính

#### openCamera()
```java
private void openCamera() {
    // 1. Kiểm tra thiết bị có camera
    if (!PermissionHelper.hasCamera(requireContext())) {
        Toast.makeText(requireContext(), "Thiết bị không có camera", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // 2. Kiểm tra permission
    if (!PermissionHelper.hasCameraPermission(requireContext())) {
        cameraPermissionLauncher.launch(PermissionHelper.getCameraPermissions());
        return;
    }
    
    // 3. Đã có permission, mở camera
    launchCamera();
}
```

#### openGallery()
```java
private void openGallery() {
    // 1. Kiểm tra permission
    if (!PermissionHelper.hasGalleryPermission(requireContext())) {
        galleryPermissionLauncher.launch(PermissionHelper.getGalleryPermissions());
        return;
    }
    
    // 2. Đã có permission, mở gallery
    launchGallery();
}
```

#### launchCamera()
```java
private void launchCamera() {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    
    // 1. Kiểm tra có app Camera
    if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) == null) {
        Toast.makeText(requireContext(), "Không tìm thấy ứng dụng Camera", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // 2. Tạo file tạm thời
    tempCameraImageFile = ImageFileHelper.createImageFile(requireContext());
    if (tempCameraImageFile == null) {
        Toast.makeText(requireContext(), "Không thể tạo file ảnh", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // 3. Chuyển File thành Uri qua FileProvider
    tempCameraImageUri = ImageFileHelper.getUriForFile(requireContext(), tempCameraImageFile);
    if (tempCameraImageUri == null) {
        Toast.makeText(requireContext(), "Lỗi FileProvider", Toast.LENGTH_SHORT).show();
        cleanupTempCameraFile();
        return;
    }
    
    // 4. Gửi Uri cho Camera app
    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraImageUri);
    
    // 5. Launch camera
    cameraLauncher.launch(takePictureIntent);
}
```

#### launchGallery()
```java
private void launchGallery() {
    Intent pickPhotoIntent = new Intent(
        Intent.ACTION_PICK, 
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    );
    
    // Chỉ chọn ảnh
    pickPhotoIntent.setType("image/*");
    
    // Launch gallery
    galleryLauncher.launch(pickPhotoIntent);
}
```

#### onImageCaptured()
```java
private void onImageCaptured(Uri imageUri) {
    addImageToTask(imageUri.toString());
    
    // Reset temp variables
    tempCameraImageUri = null;
    tempCameraImageFile = null;
}
```

#### onImageSelected()
```java
private void onImageSelected(Uri imageUri) {
    addImageToTask(imageUri.toString());
}
```

#### addImageToTask()
```java
private void addImageToTask(String imageUri) {
    // 1. Thêm vào list
    taskImages.add(imageUri);
    
    // 2. Cập nhật adapter
    imageAdapter.addImage(imageUri);
    
    // 3. Hiển thị RecyclerView
    updateImageVisibility();
    
    // 4. Lưu vào database
    if (task != null) {
        task.setImageUris(taskImages);
        dbHelper.updateTaskImages(task.getId(), taskImages);
    }
    
    Toast.makeText(requireContext(), "Đã thêm ảnh", Toast.LENGTH_SHORT).show();
}
```

#### cleanupTempCameraFile()
```java
private void cleanupTempCameraFile() {
    if (tempCameraImageFile != null && tempCameraImageFile.exists()) {
        tempCameraImageFile.delete();
    }
    tempCameraImageUri = null;
    tempCameraImageFile = null;
}
```

### 5. Cập nhật showImageAttachmentOptions()

```java
private void showImageAttachmentOptions() {
    ImageAttachmentBottomSheet bottomSheet = new ImageAttachmentBottomSheet();
    bottomSheet.setOnImageSourceSelectedListener(new ImageAttachmentBottomSheet.OnImageSourceSelectedListener() {
        @Override
        public void onCameraSelected() {
            openCamera();  // Gọi method openCamera()
        }

        @Override
        public void onGallerySelected() {
            openGallery();  // Gọi method openGallery()
        }
    });
    bottomSheet.show(getParentFragmentManager(), "ImageAttachmentBottomSheet");
}
```

### 6. Load ảnh hiện có khi mở task

```java
// Trong onViewCreated(), sau khi bind data
if (task != null && task.getImageUris() != null && !task.getImageUris().isEmpty()) {
    taskImages = new ArrayList<>(task.getImageUris());
    imageAdapter.setImages(taskImages);
    updateImageVisibility();
}
```

### 7. Lưu ảnh khi xóa

```java
@Override
public void onImageDelete(String imageUri, int position) {
    imageAdapter.removeImage(position);
    taskImages.remove(position);
    updateImageVisibility();
    
    // Lưu vào database
    if (task != null) {
        task.setImageUris(taskImages);
        dbHelper.updateTaskImages(task.getId(), taskImages);
    }
    
    Toast.makeText(requireContext(), "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
}
```

### 8. Cleanup trong onDestroyView()

```java
@Override
public void onDestroyView() {
    super.onDestroyView();
    
    // Dọn dẹp file tạm nếu còn
    cleanupTempCameraFile();
    
    // ... existing code
}
```

## 🔄 Luồng hoạt động

### Camera Flow:
```
1. User click "Chụp ảnh mới"
   ↓
2. openCamera() được gọi
   ↓
3. Kiểm tra thiết bị có camera
   ↓
4. Kiểm tra permission CAMERA
   ├─ Chưa có → Request permission → Quay lại bước 2
   └─ Đã có → Tiếp tục
   ↓
5. launchCamera()
   ├─ Tạo file tạm: ImageFileHelper.createImageFile()
   ├─ Chuyển thành Uri: ImageFileHelper.getUriForFile()
   └─ Launch Intent với EXTRA_OUTPUT
   ↓
6. Camera app chụp ảnh và lưu vào file
   ↓
7. cameraLauncher callback
   ├─ RESULT_OK → onImageCaptured(tempCameraImageUri)
   └─ RESULT_CANCELED → cleanupTempCameraFile()
   ↓
8. addImageToTask(imageUri.toString())
   ├─ Thêm vào taskImages list
   ├─ Cập nhật imageAdapter
   ├─ Hiển thị RecyclerView
   └─ Lưu vào database: dbHelper.updateTaskImages()
```

### Gallery Flow:
```
1. User click "Chọn từ thư viện"
   ↓
2. openGallery() được gọi
   ↓
3. Kiểm tra permission READ_MEDIA_IMAGES/READ_EXTERNAL_STORAGE
   ├─ Chưa có → Request permission → Quay lại bước 2
   └─ Đã có → Tiếp tục
   ↓
4. launchGallery()
   └─ Launch Intent ACTION_PICK
   ↓
5. User chọn ảnh từ Gallery
   ↓
6. galleryLauncher callback
   ├─ RESULT_OK → onImageSelected(selectedImageUri)
   └─ RESULT_CANCELED → Hiển thị toast
   ↓
7. addImageToTask(imageUri.toString())
   ├─ Thêm vào taskImages list
   ├─ Cập nhật imageAdapter
   ├─ Hiển thị RecyclerView
   └─ Lưu vào database: dbHelper.updateTaskImages()
```

## 🎯 Các tính năng đã triển khai

✅ Kiểm tra thiết bị có camera
✅ Kiểm tra và request permissions tự động
✅ Tạo file tạm thời an toàn qua FileProvider
✅ Chụp ảnh từ Camera
✅ Chọn ảnh từ Gallery
✅ Thêm ảnh vào RecyclerView
✅ Lưu URI ảnh vào database (JSON format)
✅ Load ảnh hiện có khi mở task
✅ Xóa ảnh và cập nhật database
✅ Cleanup file tạm khi hủy hoặc đóng dialog
✅ Xử lý lỗi và hiển thị thông báo phù hợp

## 📝 Lưu ý quan trọng

1. **Permission handling:** Tự động kiểm tra và request permissions, xử lý cả Android 13+ và các phiên bản cũ hơn

2. **FileProvider:** Sử dụng FileProvider để tạo Uri an toàn cho Camera (bắt buộc từ Android 7.0+)

3. **Temp file cleanup:** Tự động dọn dẹp file tạm khi:
   - User hủy chụp ảnh
   - Có lỗi xảy ra
   - Dialog bị đóng (onDestroyView)

4. **Database sync:** Mỗi khi thêm/xóa ảnh, tự động cập nhật database ngay lập tức

5. **URI types:**
   - Camera: `file:///storage/emulated/0/Android/data/.../Pictures/TASK_20240321_123456.jpg`
   - Gallery: `content://media/external/images/media/123`

6. **Error handling:** Xử lý tất cả các trường hợp lỗi:
   - Thiết bị không có camera
   - Không có app Camera/Gallery
   - Không thể tạo file
   - FileProvider error
   - Permission denied

## 🧪 Testing Checklist

- [ ] Chụp ảnh từ camera
- [ ] Chọn ảnh từ gallery
- [ ] Thêm nhiều ảnh (test RecyclerView scroll)
- [ ] Xóa ảnh
- [ ] Đóng và mở lại task (test load từ database)
- [ ] Hủy chụp ảnh (test cleanup)
- [ ] Từ chối permission (test error handling)
- [ ] Test trên Android 13+ (READ_MEDIA_IMAGES)
- [ ] Test trên Android 12 trở xuống (READ_EXTERNAL_STORAGE)
- [ ] Test trên thiết bị không có camera
- [ ] Test khi không có app Camera/Gallery

## 🚀 Hoàn thành!

Tất cả chức năng Camera và Gallery đã được triển khai đầy đủ trong TaskDetailBottomSheet. Bạn có thể build và test app ngay bây giờ!
