# ✅ Sửa Lỗi Đính Kèm Ảnh - Hoàn Tất

## 🎯 Các Vấn Đề Đã Được Sửa

### 1. ✅ Lỗi NullPointerException trong TaskDetailBottomSheet
**Vấn đề**: Khi nhấn vào task có ảnh, ứng dụng bị crash với lỗi:
```
java.lang.NullPointerException: Attempt to invoke virtual method 
'void hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.TaskImageAdapter.setImages(java.util.List)' 
on a null object reference at TaskDetailBottomSheet.java:289
```

**Nguyên nhân**: Adapter `imageAdapter` được sử dụng trước khi được khởi tạo.

**Giải pháp**: Di chuyển đoạn code khởi tạo RecyclerView và Adapter lên TRƯỚC phần bind data, đảm bảo adapter đã sẵn sàng trước khi gọi `imageAdapter.setImages()`.

**File đã sửa**: `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/dialog/TaskDetailBottomSheet.java`

---

### 2. ✅ Nút Đính Kèm Ảnh Không Hoạt Động trong Add Task Dialog
**Vấn đề**: Nút đính kèm ảnh (`action_attach_image`) trong `layout_bottom_sheet_add_task.xml` không hiện dialog chọn ảnh.

**Nguyên nhân**: Chưa có code xử lý sự kiện click và chưa có ActivityResultLauncher cho Camera/Gallery trong MainActivity.

**Giải pháp**: 
1. Thêm các ActivityResultLauncher cho Camera và Gallery
2. Thêm RecyclerView và Adapter để hiển thị ảnh đã chọn
3. Kết nối nút đính kèm với ImageAttachmentBottomSheet
4. Lưu danh sách ảnh vào database khi tạo task mới

**File đã sửa**: `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/MainActivity.java`

---

## 📝 Chi Tiết Thay Đổi

### TaskDetailBottomSheet.java
**Thay đổi cấu trúc code**:
```java
// TRƯỚC (SAI - adapter chưa khởi tạo)
// Bind data
if (task != null) {
    // ... bind data ...
    imageAdapter.setImages(taskImages); // ❌ Lỗi: adapter = null
}

// Setup RecyclerView (quá muộn)
imageAdapter = new TaskImageAdapter();

// SAU (ĐÚNG - adapter được khởi tạo trước)
// Setup RecyclerView FIRST
imageAdapter = new TaskImageAdapter();
rvTaskImages.setAdapter(imageAdapter);

// Bind data
if (task != null) {
    // ... bind data ...
    imageAdapter.setImages(taskImages); // ✅ OK: adapter đã sẵn sàng
}
```

### MainActivity.java
**Thêm các thành phần mới**:

1. **Instance Variables** (dòng ~105):
```java
private ActivityResultLauncher<Intent> addTaskCameraLauncher;
private ActivityResultLauncher<Intent> addTaskGalleryLauncher;
private ActivityResultLauncher<String[]> addTaskCameraPermissionLauncher;
private ActivityResultLauncher<String[]> addTaskGalleryPermissionLauncher;
private Uri addTaskTempCameraImageUri;
private File addTaskTempCameraImageFile;
private List<String> addTaskImageUris = new ArrayList<>();
private TaskImageAdapter addTaskImageAdapter;
private RecyclerView addTaskRvImages;
```

2. **ActivityResultLauncher Initialization** (sau requestPermissionLauncher):
- Camera Launcher: Xử lý kết quả chụp ảnh
- Gallery Launcher: Xử lý kết quả chọn ảnh từ thư viện
- Camera Permission Launcher: Xin quyền camera
- Gallery Permission Launcher: Xin quyền đọc ảnh

3. **Cập nhật showAddTaskBottomSheet()**:
```java
// Setup RecyclerView cho ảnh
addTaskRvImages = sheetView.findViewById(R.id.rvAddTaskImages);
addTaskImageAdapter = new TaskImageAdapter();
// ... setup adapter với listener ...

// Xử lý nút đính kèm
actionAttachImage.setOnClickListener(v -> {
    ImageAttachmentBottomSheet imageBottomSheet = new ImageAttachmentBottomSheet();
    imageBottomSheet.setOnImageSourceSelectedListener(...);
    imageBottomSheet.show(getSupportFragmentManager(), "ImageAttachmentBottomSheet");
});

// Lưu ảnh khi submit
long taskId = dbHelper.insertTask(...);
if (!addTaskImageUris.isEmpty() && taskId > 0) {
    dbHelper.updateTaskImages((int) taskId, addTaskImageUris);
}
```

4. **Helper Methods** (cuối file):
- `openAddTaskCamera()`: Kiểm tra permission và mở camera
- `openAddTaskGallery()`: Kiểm tra permission và mở gallery
- `launchAddTaskCamera()`: Tạo file và launch camera intent
- `launchAddTaskGallery()`: Launch gallery intent
- `updateAddTaskImageVisibility()`: Ẩn/hiện RecyclerView dựa trên số ảnh

---

## 🧪 Cách Test

### Test 1: Xem Task Có Ảnh (Fix NullPointerException)
1. Mở một task đã có ảnh đính kèm
2. ✅ Task detail mở thành công, không crash
3. ✅ Ảnh hiển thị trong RecyclerView ngang
4. ✅ Có thể xóa ảnh bằng nút X

### Test 2: Thêm Task Mới Với Ảnh
1. Nhấn nút FAB để thêm task mới
2. Nhập tiêu đề task
3. Nhấn nút đính kèm ảnh (📎)
4. ✅ Bottom sheet hiện 2 tùy chọn: "Chụp ảnh mới" và "Chọn từ thư viện"

### Test 3: Chụp Ảnh Mới
1. Chọn "Chụp ảnh mới"
2. ✅ Nếu chưa có quyền → Hiện dialog xin quyền
3. ✅ Sau khi cấp quyền → Mở camera
4. Chụp ảnh
5. ✅ Ảnh hiện trong RecyclerView
6. ✅ Có thể chụp thêm nhiều ảnh
7. Nhấn nút gửi
8. ✅ Task được tạo với ảnh đính kèm

### Test 4: Chọn Từ Thư Viện
1. Chọn "Chọn từ thư viện"
2. ✅ Nếu chưa có quyền → Hiện dialog xin quyền
3. ✅ Sau khi cấp quyền → Mở gallery
4. Chọn ảnh
5. ✅ Ảnh hiện trong RecyclerView
6. ✅ Có thể chọn thêm nhiều ảnh
7. Nhấn nút gửi
8. ✅ Task được tạo với ảnh đính kèm

### Test 5: Xóa Ảnh Trước Khi Lưu
1. Thêm task mới
2. Đính kèm 2-3 ảnh
3. Nhấn nút X trên một ảnh
4. ✅ Ảnh bị xóa khỏi danh sách
5. ✅ RecyclerView cập nhật
6. Nhấn nút gửi
7. ✅ Task được tạo chỉ với các ảnh còn lại

---

## 📦 Files Đã Thay Đổi

| File | Thay Đổi | Mô Tả |
|------|----------|-------|
| `TaskDetailBottomSheet.java` | ✏️ Modified | Di chuyển adapter initialization lên trước bind data |
| `MainActivity.java` | ✏️ Modified | Thêm image attachment cho add task dialog |

---

## 🎉 Kết Quả

✅ **Lỗi NullPointerException đã được sửa** - Có thể mở task có ảnh mà không crash

✅ **Nút đính kèm ảnh hoạt động** - Có thể thêm ảnh khi tạo task mới

✅ **Camera hoạt động** - Chụp ảnh và đính kèm vào task

✅ **Gallery hoạt động** - Chọn ảnh từ thư viện và đính kèm vào task

✅ **Xóa ảnh hoạt động** - Có thể xóa ảnh trước và sau khi lưu task

✅ **Lưu ảnh vào database** - Ảnh được lưu và hiển thị lại khi mở task

---

## 🔍 Lưu Ý Kỹ Thuật

1. **Thứ tự khởi tạo quan trọng**: Adapter phải được khởi tạo TRƯỚC khi sử dụng
2. **ActivityResultLauncher**: Phải được register trong onCreate hoặc initialization block
3. **Permission handling**: Tự động kiểm tra và request permission khi cần
4. **FileProvider**: Sử dụng để chia sẻ file ảnh an toàn với Camera app
5. **Database**: insertTask() trả về task ID để cập nhật ảnh sau khi insert

---

## 📱 Tương Thích

- ✅ Android 13+ (READ_MEDIA_IMAGES)
- ✅ Android 6-12 (READ_EXTERNAL_STORAGE)
- ✅ Camera permission handling
- ✅ FileProvider cho Camera intent

---

**Tất cả tính năng đính kèm ảnh đã hoạt động hoàn chỉnh!** 🎊
