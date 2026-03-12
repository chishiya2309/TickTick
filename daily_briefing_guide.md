# Hướng dẫn Daily Briefing (Báo cáo đầu ngày)

## Tổng quan
Tính năng Daily Briefing tự động gửi thông báo lúc 7:00 AM mỗi ngày, hiển thị danh sách các nhiệm vụ hôm nay và quá hạn.

## Cấu trúc File

### 1. **DailyBriefingWorker.java**
📁 `app/src/main/java/.../worker/DailyBriefingWorker.java`

**Chức năng:**
- Kế thừa từ `Worker` của WorkManager
- Thực hiện công việc gửi notification khi được trigger

**Các phương thức chính:**

#### `doWork()`
- Entry point của Worker
- Tạo Notification Channel
- Lấy danh sách task từ SQLite
- Hiển thị notification
- Return `Result.success()`

#### `createNotificationChannel()`
- Tạo NotificationChannel cho Android 8.0+ (API 26+)
- Channel ID: `"daily_briefing_channel"`
- Importance: `IMPORTANCE_DEFAULT`

#### `getTasksForToday()`
- Tính toán `startOfToday` (00:00:00) và `endOfToday` (23:59:59)
- Gọi `dbHelper.getTodayAndOverdueTasks()` để lấy:
  - Task có `due_date_millis` trong khoảng hôm nay
  - Task có `due_date_millis` < startOfToday (quá hạn)
  - Chỉ lấy task chưa hoàn thành (`is_completed = 0`)

#### `showDailyBriefingNotification()`
- Sử dụng `NotificationCompat.InboxStyle` để hiển thị nhiều dòng
- Hiển thị tối đa 5 task đầu tiên
- Thêm emoji ⚠️ cho task quá hạn
- Click vào notification → mở MainActivity
- Auto cancel khi click

**Ví dụ Notification:**
```
Báo cáo đầu ngày - 3 nhiệm vụ
1. Mua đồ ăn
2. ⚠️ Họp team (Quá hạn)
3. Hoàn thành báo cáo
```

---

### 2. **DailyBriefingScheduler.java**
📁 `app/src/main/java/.../utils/DailyBriefingScheduler.java`

**Chức năng:**
- Utility class để lập lịch Daily Briefing
- Quản lý WorkManager scheduling

**Các phương thức chính:**

#### `setupDailyBriefingWork(Context context)`
- **Mục đích:** Thiết lập lịch chạy Daily Briefing mỗi ngày lúc 7:00 AM
- **Cách hoạt động:**
  1. Tính toán `initialDelay` đến 7:00 AM tiếp theo
  2. Tạo `PeriodicWorkRequest` với interval 24 giờ
  3. Flex interval: 15 phút (cho phép chạy trong khoảng 7:00-7:15 AM)
  4. Sử dụng `ExistingPeriodicWorkPolicy.REPLACE` để thay thế work cũ

**Code mẫu:**
```java
PeriodicWorkRequest dailyBriefingWork = new PeriodicWorkRequest.Builder(
    DailyBriefingWorker.class,
    24, TimeUnit.HOURS,  // Repeat mỗi 24 giờ
    15, TimeUnit.MINUTES // Flex interval
)
.setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
.build();
```

#### `calculateInitialDelay()`
- **Logic:**
  - Nếu hiện tại < 7:00 AM hôm nay → delay đến 7:00 AM hôm nay
  - Nếu hiện tại ≥ 7:00 AM hôm nay → delay đến 7:00 AM ngày mai
- **Return:** Thời gian delay (milliseconds)

**Ví dụ:**
- Hiện tại: 6:30 AM → delay = 30 phút
- Hiện tại: 8:00 AM → delay = 23 giờ (đến 7:00 AM ngày mai)

#### `cancelDailyBriefingWork(Context context)`
- Hủy Daily Briefing Work
- Dùng khi người dùng muốn tắt tính năng

---

### 3. **MainActivity.java**
📁 `app/src/main/java/.../activity/MainActivity.java`

**Thay đổi:**

#### Import thêm:
```java
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.DailyBriefingScheduler;
```

#### Trong `onCreate()`:
```java
// Thiết lập Daily Briefing (Báo cáo đầu ngày lúc 7h sáng)
DailyBriefingScheduler.setupDailyBriefingWork(this);
```

**Vị trí:** Sau `setupBackPressHandler()`, trước `loadTasksForList()`

---

### 4. **AndroidManifest.xml**
📁 `app/src/main/AndroidManifest.xml`

**Quyền đã thêm:**
```xml
<!-- Quyền cho Daily Briefing Notification -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

**Giải thích:**
- `POST_NOTIFICATIONS`: Hiển thị notification (Android 13+)
- `WAKE_LOCK`: Đánh thức thiết bị để chạy work
- `RECEIVE_BOOT_COMPLETED`: Tự động lên lịch lại sau khi khởi động lại thiết bị

---

### 5. **build.gradle.kts**
📁 `app/build.gradle.kts`

**Dependency đã thêm:**
```kotlin
// WorkManager cho Daily Briefing
implementation("androidx.work:work-runtime:2.9.0")
```

---

## Cách hoạt động

### Flow tổng quát:
```
1. MainActivity.onCreate()
   ↓
2. DailyBriefingScheduler.setupDailyBriefingWork()
   ↓
3. WorkManager lên lịch PeriodicWorkRequest
   ↓
4. Mỗi ngày lúc 7:00 AM, WorkManager trigger DailyBriefingWorker
   ↓
5. DailyBriefingWorker.doWork()
   ↓
6. Query SQLite → Lấy task hôm nay & quá hạn
   ↓
7. Tạo Notification với InboxStyle
   ↓
8. Hiển thị notification cho user
```

### Timeline ví dụ:
- **Ngày 1, 8:00 AM:** User mở app → setupDailyBriefingWork() được gọi
  - Tính delay = 23 giờ (đến 7:00 AM ngày 2)
  - WorkManager lên lịch
  
- **Ngày 2, 7:00 AM:** WorkManager trigger DailyBriefingWorker
  - Query database → Tìm thấy 3 task (1 quá hạn, 2 hôm nay)
  - Hiển thị notification
  
- **Ngày 3, 7:00 AM:** WorkManager tự động trigger lại (repeat mỗi 24h)

---

## Testing

### Test thủ công:
1. **Thay đổi TARGET_HOUR trong DailyBriefingScheduler:**
   ```java
   private static final int TARGET_HOUR = 14; // Test lúc 2:00 PM
   ```

2. **Giảm interval để test nhanh:**
   ```java
   PeriodicWorkRequest dailyBriefingWork = new PeriodicWorkRequest.Builder(
       DailyBriefingWorker.class,
       15, TimeUnit.MINUTES // Test mỗi 15 phút
   )
   ```

3. **Trigger ngay lập tức (OneTimeWorkRequest):**
   ```java
   OneTimeWorkRequest testWork = new OneTimeWorkRequest.Builder(DailyBriefingWorker.class)
       .build();
   WorkManager.getInstance(context).enqueue(testWork);
   ```

### Kiểm tra WorkManager:
```bash
# Xem danh sách work đang chạy
adb shell dumpsys jobscheduler | grep -A 20 "daily_briefing"
```

---

## Tùy chỉnh

### Thay đổi giờ chạy:
Sửa trong `DailyBriefingScheduler.java`:
```java
private static final int TARGET_HOUR = 8;  // 8 giờ sáng
private static final int TARGET_MINUTE = 30; // 30 phút
```

### Thay đổi số task hiển thị:
Sửa trong `DailyBriefingWorker.showDailyBriefingNotification()`:
```java
int count = Math.min(tasks.size(), 10); // Hiển thị 10 task
```

### Thêm âm thanh notification:
```java
builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
```

### Thêm vibration:
```java
builder.setVibrate(new long[]{0, 500, 200, 500});
```

---

## Lưu ý quan trọng

### 1. Android 13+ (API 33+)
- Cần request runtime permission cho `POST_NOTIFICATIONS`
- Thêm code request permission trong MainActivity:
```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
    }
}
```

### 2. Battery Optimization
- Một số thiết bị có thể kill WorkManager khi ở chế độ tiết kiệm pin
- Hướng dẫn user tắt battery optimization cho app:
```java
Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
intent.setData(Uri.parse("package:" + getPackageName()));
startActivity(intent);
```

### 3. WorkManager Constraints
- Hiện tại không yêu cầu mạng (`NetworkType.NOT_REQUIRED`)
- Có thể thêm constraint nếu cần:
```java
.setRequiresBatteryNotLow(true)  // Chỉ chạy khi pin đủ
.setRequiresCharging(true)       // Chỉ chạy khi đang sạc
```

### 4. Persistence
- WorkManager tự động persist work qua app restart
- Không cần gọi lại `setupDailyBriefingWork()` mỗi lần mở app
- Nhưng gọi lại cũng OK vì dùng `ExistingPeriodicWorkPolicy.REPLACE`

---

## Troubleshooting

### Notification không hiển thị:
1. Kiểm tra Notification Channel đã được tạo chưa
2. Kiểm tra permission `POST_NOTIFICATIONS` (Android 13+)
3. Kiểm tra app notification settings trong System Settings

### Work không chạy đúng giờ:
1. WorkManager có thể delay vài phút (flex interval)
2. Kiểm tra battery optimization settings
3. Kiểm tra Doze mode settings

### Work bị cancel sau app restart:
1. WorkManager tự động persist, không cần lo
2. Nếu vẫn bị cancel, check logcat xem có lỗi gì

---

## Kết luận

Tính năng Daily Briefing đã được implement hoàn chỉnh với:
- ✅ WorkManager scheduling (chạy mỗi ngày lúc 7:00 AM)
- ✅ SQLite integration (lấy task hôm nay & quá hạn)
- ✅ Notification với InboxStyle (hiển thị danh sách task)
- ✅ NotificationChannel (Android 8.0+)
- ✅ Clean code với comment tiếng Việt
- ✅ Permissions đầy đủ

Code đã sẵn sàng để chạy và test!
