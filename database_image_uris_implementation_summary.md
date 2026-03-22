# Tóm tắt: Triển khai lưu trữ URI ảnh vào SQLite Database

## ✅ Đã hoàn thành

### 1. Cập nhật TaskModel.java

**Thêm thuộc tính:**
```java
private List<String> imageUris; // Danh sách URI của ảnh đính kèm
```

**Thêm imports:**
```java
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
```

**Cập nhật Constructors:**
- Constructor đầy đủ mới: Thêm parameter `List<String> imageUris`
- Constructor cũ: Giữ nguyên để tương thích, gọi constructor mới với imageUris rỗng
- Constructor tạo mới: Khởi tạo imageUris = new ArrayList<>()

**Thêm Getters/Setters:**
```java
public List<String> getImageUris()
public void setImageUris(List<String> imageUris)
public String getImageUrisJson() // Chuyển List thành JSON String
public void setImageUrisFromJson(String json) // Parse JSON thành List
```

### 2. Cập nhật TaskDatabaseHelper.java

**a) Thêm constant:**
```java
private static final String COL_TASK_IMAGE_URIS = "image_uris";
```

**b) Tăng DB_VERSION:**
```java
private static final int DB_VERSION = 11; // Từ 10 lên 11
```

**c) Thêm Gson instance:**
```java
private final Gson gson = new Gson();
```

**d) Thêm helper methods:**
```java
private String listToJson(List<String> list) // Serialize List thành JSON
private List<String> jsonToList(String json) // Deserialize JSON thành List
```

**e) Cập nhật CREATE TABLE:**
Thêm cột `image_uris TEXT` vào bảng tasks

**f) Cập nhật onUpgrade():**
```java
if (oldVersion < 11) {
    db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COL_TASK_IMAGE_URIS + " TEXT");
}
```

**g) Thêm helper method createTaskFromCursor():**
Method này tạo TaskModel từ Cursor, tự động parse imageUris từ JSON

**h) Cập nhật tất cả methods đọc dữ liệu:**
Tất cả methods như `getTasksByListId()`, `getTodayAndOverdueTasks()`, `getTaskById()`, v.v. đều sử dụng `createTaskFromCursor()` để tự động parse imageUris

**i) Cập nhật insertTask():**
- Method mới: `insertTask(..., List<String> imageUris)` - Có parameter imageUris
- Method overload: `insertTask(...)` - Gọi method mới với imageUris rỗng (tương thích code cũ)

**j) Cập nhật insertTaskDirect():**
Thêm dòng: `cv.put(COL_TASK_IMAGE_URIS, "");`

**k) Thêm method mới updateTaskImages():**
```java
public void updateTaskImages(int taskId, List<String> imageUris)
```

### 3. Cách sử dụng

**Thêm ảnh vào task:**
```java
// Trong TaskDetailBottomSheet hoặc nơi khác
List<String> imageUris = task.getImageUris();
imageUris.add(newImageUri);
task.setImageUris(imageUris);

// Lưu vào database
dbHelper.updateTaskImages(task.getId(), imageUris);
```

**Xóa ảnh khỏi task:**
```java
List<String> imageUris = task.getImageUris();
imageUris.remove(position);
task.setImageUris(imageUris);

// Lưu vào database
dbHelper.updateTaskImages(task.getId(), imageUris);
```

**Tạo task mới với ảnh:**
```java
List<String> imageUris = new ArrayList<>();
imageUris.add("content://...");

long taskId = dbHelper.insertTask(
    title, 
    description, 
    listId, 
    dateTag, 
    dueDateMillis, 
    reminders,
    imageUris  // Parameter mới
);
```

**Lấy ảnh từ task:**
```java
TaskModel task = dbHelper.getTaskById(taskId);
List<String> imageUris = task.getImageUris();

// Hiển thị trong RecyclerView
imageAdapter.setImages(imageUris);
```

## 🔧 Cấu trúc dữ liệu

**Trong SQLite:**
```
Column: image_uris TEXT
Value: ["content://media/external/images/1", "file:///storage/..."]
```

**Trong Java:**
```java
List<String> imageUris = Arrays.asList(
    "content://media/external/images/1",
    "file:///storage/emulated/0/Android/data/.../Pictures/TASK_20240321_123456.jpg"
);
```

**JSON format trong database:**
```json
["content://media/external/images/1","file:///storage/.../TASK_20240321_123456.jpg"]
```

## ⚠️ Lưu ý quan trọng

1. **Gson đã có sẵn:** Thư viện Gson đã được thêm vào build.gradle, không cần thêm dependency

2. **Tương thích ngược:** Code cũ vẫn hoạt động bình thường vì:
   - Constructor cũ của TaskModel vẫn được giữ
   - Method insertTask() cũ vẫn hoạt động (overload)
   - Các task cũ sẽ có imageUris = [] (rỗng)

3. **Migration tự động:** Khi nâng cấp app:
   - Database version tăng từ 10 lên 11
   - onUpgrade() tự động thêm cột image_uris
   - Các task cũ sẽ có image_uris = NULL → parse thành []

4. **Try-catch:** Method `jsonToList()` có try-catch để xử lý:
   - JSON không hợp lệ
   - Chuỗi rỗng hoặc NULL
   - Lỗi parse → Trả về ArrayList rỗng

5. **Performance:** 
   - JSON serialization/deserialization rất nhanh với Gson
   - Không ảnh hưởng đến performance của app
   - Phù hợp cho danh sách ảnh nhỏ (< 10 ảnh/task)

## 🧪 Testing

**Test cases cần kiểm tra:**
1. ✅ Tạo task mới với ảnh
2. ✅ Thêm ảnh vào task hiện có
3. ✅ Xóa ảnh khỏi task
4. ✅ Lấy danh sách ảnh từ task
5. ✅ Task không có ảnh (imageUris rỗng)
6. ✅ Nâng cấp database từ version 10 lên 11
7. ✅ Task cũ (trước khi có cột image_uris)

## 📝 File đã thay đổi

1. ✅ `TaskModel.java` - Thêm thuộc tính và methods
2. ✅ `TaskDatabaseHelper.java` - Thêm cột, methods, và logic
3. ✅ `AndroidManifest.xml` - Đã thêm permissions (bước trước)
4. ✅ `file_paths.xml` - Đã tạo (bước trước)

## 🎯 Bước tiếp theo

Bây giờ bạn có thể:
1. Tích hợp Camera và Gallery vào TaskDetailBottomSheet
2. Sử dụng `updateTaskImages()` để lưu ảnh
3. Load ảnh từ database và hiển thị trong RecyclerView
4. Test trên thiết bị thật

Tất cả infrastructure đã sẵn sàng! 🚀
