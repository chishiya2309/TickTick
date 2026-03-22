# Hướng Dẫn Sử Dụng Tính Năng Đính Kèm Ảnh

## ✅ Đã Hoàn Thành

Build thành công! Tính năng đính kèm ảnh đã sẵn sàng để sử dụng.

## 📱 Cách Sử Dụng

### 1. Mở Task Detail
- Nhấn vào một task bất kỳ trong danh sách
- Hoặc tạo task mới

### 2. Thêm Ảnh
- Tìm thanh công cụ ở dưới cùng của Task Detail
- Nhấn vào nút **icon hình ảnh** (nút thứ 3 từ trái sang)
- Chọn một trong hai tùy chọn:
  - **Chụp ảnh mới**: Mở camera để chụp ảnh
  - **Chọn từ thư viện**: Chọn ảnh có sẵn từ thư viện

### 3. Xem Ảnh
- Sau khi thêm ảnh, chúng sẽ hiển thị trong một danh sách ngang
- Vuốt sang trái/phải để xem các ảnh khác
- Mỗi ảnh có kích thước 80x80dp với góc bo tròn

### 4. Xóa Ảnh
- Nhấn vào nút **X** ở góc trên bên phải của ảnh
- Ảnh sẽ bị xóa ngay lập tức

### 5. Lưu Task
- Tất cả ảnh được tự động lưu vào database
- Khi bạn mở lại task, ảnh vẫn hiển thị đúng

## 🔐 Quyền Cần Thiết

Khi lần đầu sử dụng, app sẽ yêu cầu các quyền sau:

### Camera
- **CAMERA**: Để chụp ảnh mới
- Được yêu cầu khi bạn chọn "Chụp ảnh mới"

### Thư viện
- **READ_MEDIA_IMAGES** (Android 13+): Để đọc ảnh từ thư viện
- **READ_EXTERNAL_STORAGE** (Android 12 trở xuống): Để đọc ảnh từ thư viện
- Được yêu cầu khi bạn chọn "Chọn từ thư viện"

## 🎯 Vị Trí Nút Đính Kèm

```
┌─────────────────────────────────────┐
│  Task Detail Bottom Sheet           │
│                                     │
│  [✓] Task Title                     │
│      Task Description               │
│                                     │
│  [Ảnh 1] [Ảnh 2] [Ảnh 3] →         │
│                                     │
├─────────────────────────────────────┤
│  [Tag] [Grid] [📷 Ảnh]              │  ← Nút này!
└─────────────────────────────────────┘
```

Nút đính kèm ảnh nằm ở **thanh công cụ dưới cùng**, là nút thứ 3 (icon hình ảnh).

## 🚀 Cài Đặt và Test

```bash
# Build và cài đặt app
./gradlew installDebug

# Hoặc chỉ build APK
./gradlew assembleDebug
# APK sẽ nằm ở: app/build/outputs/apk/debug/app-debug.apk
```

## 💡 Lưu Ý

1. **Ảnh được lưu ở đâu?**
   - URI của ảnh được lưu trong database SQLite
   - File ảnh thực tế được lưu trong thư mục Pictures của app

2. **Giới hạn số lượng ảnh?**
   - Không có giới hạn cứng
   - Tuy nhiên nên giới hạn khoảng 5-10 ảnh/task để tránh tốn bộ nhớ

3. **Ảnh có bị mất không?**
   - Không, ảnh được lưu vĩnh viễn trong storage của app
   - Chỉ bị xóa khi bạn nhấn nút X hoặc xóa app

4. **Performance?**
   - Glide tự động cache và optimize ảnh
   - Ảnh được resize xuống 200x200 để tiết kiệm memory
   - Cuộn danh sách ảnh rất mượt mà

## 🐛 Nếu Gặp Vấn Đề

### Không thấy nút đính kèm?
- Kiểm tra xem bạn đã mở Task Detail chưa
- Nút nằm ở thanh công cụ dưới cùng, icon hình ảnh

### Không chụp được ảnh?
- Kiểm tra quyền CAMERA đã được cấp chưa
- Vào Settings > Apps > TickTick > Permissions > Camera

### Không chọn được ảnh từ thư viện?
- Kiểm tra quyền READ_MEDIA_IMAGES (Android 13+) hoặc READ_EXTERNAL_STORAGE
- Vào Settings > Apps > TickTick > Permissions > Photos/Media

### Ảnh không hiển thị?
- Kiểm tra Logcat để xem lỗi chi tiết
- Có thể URI không hợp lệ hoặc file đã bị xóa

## ✨ Tính Năng Đã Implement

- ✅ Chụp ảnh từ camera
- ✅ Chọn ảnh từ thư viện
- ✅ Hiển thị danh sách ảnh ngang (horizontal scroll)
- ✅ Xóa ảnh
- ✅ Lưu trữ vĩnh viễn trong database
- ✅ Animation fade-in khi load ảnh
- ✅ Placeholder và error handling
- ✅ Permission handling tự động
- ✅ FileProvider security
- ✅ Glide image caching và optimization

Chúc bạn sử dụng vui vẻ! 🎉
