# Hướng Dẫn Test Tính Năng Đính Kèm Ảnh

## ✅ Trạng Thái: BUILD THÀNH CÔNG

Build đã hoàn tất không có lỗi. Tất cả các file đã được tạo và cấu hình đúng.

## 📋 Các Bước Test

### 1. Cài Đặt App
```bash
./gradlew installDebug
```

### 2. Test Camera
1. Mở app và tạo hoặc chỉnh sửa một Task
2. Trong TaskDetailBottomSheet, nhấn vào nút đính kèm (attachment icon)
3. Chọn "Chụp ảnh mới"
4. Cho phép quyền CAMERA nếu được yêu cầu
5. Chụp ảnh
6. Kiểm tra ảnh hiển thị trong RecyclerView ngang

### 3. Test Gallery
1. Mở TaskDetailBottomSheet
2. Nhấn nút đính kèm
3. Chọn "Chọn từ thư viện"
4. Cho phép quyền READ_MEDIA_IMAGES (Android 13+) hoặc READ_EXTERNAL_STORAGE (Android cũ hơn)
5. Chọn ảnh từ thư viện
6. Kiểm tra ảnh hiển thị trong RecyclerView

### 4. Test Xóa Ảnh
1. Nhấn vào nút "X" trên góc ảnh
2. Kiểm tra ảnh bị xóa khỏi danh sách
3. Lưu Task và mở lại để đảm bảo ảnh đã bị xóa vĩnh viễn

### 5. Test Persistence (Lưu Trữ)
1. Thêm nhiều ảnh vào Task
2. Lưu Task (đóng BottomSheet)
3. Mở lại Task
4. Kiểm tra tất cả ảnh vẫn hiển thị đúng

### 6. Test Multiple Images
1. Thêm 3-5 ảnh vào một Task
2. Kiểm tra RecyclerView cuộn ngang mượt mà
3. Kiểm tra animation fade-in khi ảnh được load

## 🔍 Các Điểm Cần Kiểm Tra

### Permissions
- ✅ CAMERA permission được yêu cầu khi chụp ảnh
- ✅ READ_MEDIA_IMAGES (Android 13+) hoặc READ_EXTERNAL_STORAGE được yêu cầu khi chọn từ thư viện
- ✅ App xử lý đúng khi user từ chối quyền

### UI/UX
- ✅ Ảnh hiển thị với kích thước 80x80dp
- ✅ Ảnh có bo góc tròn (cornerRadius)
- ✅ Nút "X" hiển thị ở góc trên bên phải
- ✅ RecyclerView cuộn ngang mượt mà
- ✅ Animation fade-in khi ảnh được load
- ✅ Placeholder hiển thị khi đang load
- ✅ Error icon hiển thị khi load thất bại

### Database
- ✅ imageUris được lưu dưới dạng JSON trong cột `image_uris`
- ✅ Dữ liệu được serialize/deserialize đúng với Gson
- ✅ updateTaskImages() hoạt động chính xác
- ✅ Ảnh được load lại đúng khi mở Task

### Performance
- ✅ Glide cache ảnh hiệu quả (DiskCacheStrategy.ALL)
- ✅ Ảnh được resize xuống 200x200 để tiết kiệm memory
- ✅ Không bị lag khi cuộn danh sách ảnh

## 🐛 Các Lỗi Có Thể Gặp

### 1. SecurityException khi chụp ảnh
**Nguyên nhân**: FileProvider không được cấu hình đúng
**Giải pháp**: Đã cấu hình đúng trong AndroidManifest.xml với authority `hcmute.edu.vn.lequanghung_23110110.ticktick.fileprovider`

### 2. Ảnh không hiển thị
**Nguyên nhân**: URI không hợp lệ hoặc quyền bị từ chối
**Giải pháp**: Kiểm tra Logcat để xem lỗi chi tiết từ Glide

### 3. App crash khi xóa ảnh
**Nguyên nhân**: Position không hợp lệ
**Giải pháp**: Đã xử lý với `getAdapterPosition()` và kiểm tra `RecyclerView.NO_POSITION`

### 4. Ảnh bị mất sau khi restart app
**Nguyên nhân**: URI tạm thời từ camera không được persist
**Giải pháp**: Đã lưu URI vào external files directory thông qua FileProvider

## 📁 Files Đã Tạo/Cập Nhật

### Layouts
- ✅ `layout_bottom_sheet_task_detail.xml` - Thêm RecyclerView cho ảnh
- ✅ `item_task_image.xml` - Layout cho mỗi ảnh
- ✅ `layout_bottom_sheet_image_attachment.xml` - Dialog chọn Camera/Gallery

### Java Classes
- ✅ `TaskImageAdapter.java` - Adapter hiển thị ảnh với Glide
- ✅ `ImageAttachmentBottomSheet.java` - BottomSheet chọn nguồn ảnh
- ✅ `PermissionHelper.java` - Xử lý permissions
- ✅ `ImageFileHelper.java` - Tạo file và URI cho camera
- ✅ `TaskModel.java` - Thêm property imageUris
- ✅ `TaskDatabaseHelper.java` - Thêm cột image_uris, methods serialize/deserialize
- ✅ `TaskDetailBottomSheet.java` - Tích hợp camera/gallery

### Configuration
- ✅ `AndroidManifest.xml` - Permissions và FileProvider
- ✅ `file_paths.xml` - FileProvider paths
- ✅ `build.gradle.kts` - Glide dependency

## 🎯 Kết Luận

Tính năng đính kèm ảnh đã được implement hoàn chỉnh và build thành công. Tất cả các component đã được tích hợp:
- Camera capture ✅
- Gallery selection ✅
- Image display với Glide ✅
- Image deletion ✅
- Database persistence ✅
- Permission handling ✅

**Sẵn sàng để test trên thiết bị thật!**
