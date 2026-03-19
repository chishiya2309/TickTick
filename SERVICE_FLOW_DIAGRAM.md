# 📱 Android Service Flow - TickTick Project

## 🎯 Tổng Quan Luồng Hoạt Động

```
┌─────────────────────────────────────────────────────────────────┐
│                      ANDROID SERVICE FLOW                        │
└─────────────────────────────────────────────────────────────────┘

1️⃣ USER ACTION (MainActivity)
        ↓
2️⃣ START SERVICE → ReminderService
        ↓
3️⃣ SCHEDULE ALARMS → AlarmManager
        ↓
4️⃣ TIME REACHES → AlarmReceiver (BroadcastReceiver)
        ↓
5️⃣ SHOW NOTIFICATION / ALARM ACTIVITY
        ↓
6️⃣ USER INTERACTION → Open MainActivity or AlarmActivity
```

---

## 📂 Danh Sách File Quan Trọng

### 🔴 **SERVICE LAYER** (Lớp dịch vụ)
```
service/
├── ReminderService.java (★★★ QUAN TRỌNG NHẤT)
│   ├─ onCreate() → Khởi tạo DB & Notification Channel
│   ├─ onStartCommand() → Bắt đầu Foreground Service
│   ├─ scheduleAllReminders() → Lấy tất cả task sắp tới, đặt alarm
│   ├─ scheduleTaskReminder() → Đặt notification alarm
│   ├─ scheduleStrictAlarm() → Đặt fullscreen alarm (cho pinned task)
│   └─ cancelTaskReminder() → Hủy alarm khi task complete/delete
│
├── AlarmReceiver.java (★★★ Nhận Alarm)
│   ├─ onReceive() → Nhận intent từ AlarmManager
│   ├─ ACTION_SHOW_NOTIFICATION → Hiển thị notification thường
│   └─ ACTION_START_ALARM → Mở AlarmActivity (fullscreen)
│
└── ServiceNhac.java (Dịch vụ phụ, có thể không dùng)
```

### 🟢 **ACTIVITY LAYER** (Lớp giao diện)
```
activity/
├── MainActivity.java (★★★ Chính)
│   ├─ bindService() → Kết nối với ReminderService
│   ├─ startService() → Khởi động ReminderService
│   ├─ requestPermissions() → Yêu cầu quyền SCHEDULE_EXACT_ALARM, v.v
│   ├─ onCreate() → Thiết lập UI & kết nối service
│   └─ Callbacks khi task thay đổi → Gọi scheduleAllReminders()
│
├── AlarmActivity.java (★★ Nhận task từ AlarmReceiver)
│   ├─ Hiện trực tiếp khi alarm trigger (fullscreen intent)
│   └─ Cho phép user mark task complete hoặc dismiss
│
├── SearchActivity.java
│   └─ Không liên quan trực tiếp service
│
└── SplashActivity.java
    └─ Màn hình splash ban đầu
```

### 🔵 **UTILS LAYER** (Lớp tiện ích)
```
utils/
├── NotificationHelper.java (★★★ Quản lý Notification)
│   ├─ createNotificationChannel() → Tạo channel cho O+ (Android 8+)
│   ├─ showNotification() → Hiển thị notification thường (dismiss được)
│   ├─ showAlarmNotification() → Hiển thị fullscreen intent alarm
│   └─ cancelNotification() → Hủy notification
│
├── DailyBriefingScheduler.java (Lập lịch daily briefing)
│   └─ scheduleDaily() → Đặt lịch cho daily briefing
│
└── TaskSwipeHelper.java
    └─ Xử lý swipe action trên task
```

### 🟡 **DATABASE LAYER**
```
database/
└── TaskDatabaseHelper.java (★★★ Quản lý DB)
    ├─ getUpcomingTasks() → Lấy tất cả task chưa complete, có due date
    ├─ updateTask() → Cập nhật task → Trigger reschedule
    ├─ deleteTask() → Xóa task → Trigger hủy alarm
    └─ insertTask() → Thêm task → Trigger reschedule
```

### 🟠 **MODEL LAYER**
```
model/
├── TaskModel.java (★★★ Model chính)
│   ├─ id, title, description
│   ├─ listId, dateTag
│   ├─ dueDateMillis (⭐ Thời gian deadline)
│   ├─ isCompleted (Đã hoàn thành?)
│   ├─ isPinned (⭐ Trigger fullscreen alarm?)
│   └─ reminders (List<String> - "5 phút trước", "1 ngày trước", v.v)
│
├── ListModel.java
├── TaskListItem.java (Interface)
└── TaskHeader.java
```

### ⚪ **CONFIGURATION**
```
AndroidManifest.xml (★★★ Khai báo Service & Quyền)
├─ <service> tags:
│  ├─ ReminderService (foregroundServiceType="specialUse")
│  └─ ServiceNhac
├─ <receiver> tag:
│  └─ AlarmReceiver (export=false)
└─ <uses-permission>:
   ├─ SCHEDULE_EXACT_ALARM
   ├─ FOREGROUND_SERVICE
   ├─ FOREGROUND_SERVICE_SPECIAL_USE
   ├─ USE_FULL_SCREEN_INTENT
   ├─ WAKE_LOCK
   └─ POST_NOTIFICATIONS
```

---

## 🔄 Chi Tiết Luồng Hoạt Động

### **Scenario 1: User Tạo / Sửa Task Với Reminder**

```
┌─ MainActivity.onCreate()
│   ├─ bindService(ReminderService) → ServiceConnection.onServiceConnected()
│   └─ reminderService = binder.getService()
│
├─ User Thêm Task / Set Reminder Date
│   ├─ TaskDetailBottomSheet.onConfirm()
│   ├─ dbHelper.insertTask(task) hoặc updateTask(task)
│   └─ reminderService.scheduleAllReminders() ⭐
│
└─ ReminderService.scheduleAllReminders()
    ├─ List<TaskModel> upcomingTasks = dbHelper.getUpcomingTasks()
    │   └─ SELECT * WHERE dueDateMillis > now AND !isCompleted
    │
    └─ for each task:
        ├─ scheduleTaskReminder(task) ← Thông báo thường
        │   ├─ for each reminder offset ("5 phút trước", "1 ngày", v.v):
        │   │   ├─ reminderTime = calculateReminderTime()
        │   │   ├─ Intent → AlarmReceiver.ACTION_SHOW_NOTIFICATION
        │   │   ├─ requestCode = taskId * 1000 + reminderIndex
        │   │   ├─ PendingIntent.getBroadcast()
        │   │   └─ AlarmManager.setExactAndAllowWhileIdle() 🔔
        │   │
        │   └─ Log: "Đã set Notification thành công"
        │
        └─ if (task.isPinned()):
            └─ scheduleStrictAlarm(task) ← Fullscreen alarm
                ├─ Intent → AlarmReceiver.ACTION_START_ALARM
                ├─ requestCode = taskId
                ├─ PendingIntent.getBroadcast()
                └─ AlarmManager.setExactAndAllowWhileIdle() 🚨
```

### **Scenario 2: Alarm Trigger (Thời Gian Nhắc Nhở Đến)**

```
┌─ 🕰️ AlarmManager Triggers at reminderTime
│
├─ AlarmReceiver.onReceive(context, intent)
│   ├─ String action = intent.getAction()
│   │
│   ├─ if (ACTION_SHOW_NOTIFICATION):
│   │   ├─ int taskId = intent.getIntExtra("TASK_ID")
│   │   ├─ String title = intent.getStringExtra("TASK_TITLE")
│   │   └─ NotificationHelper.showNotification(context, taskId, title)
│   │
│   └─ else if (ACTION_START_ALARM):
│       ├─ try:
│       │   ├─ Intent fullScreenIntent → AlarmActivity
│       │   ├─ context.startActivity() ✅ (Cố gắng mở trực tiếp)
│       │   └─ catch Exception: Log lỗi
│       │
│       └─ NotificationHelper.showAlarmNotification() (Fallback)
│           └─ FullScreenIntent (Android 10+)
│
└─ ✅ User Sees Notification / Alarm Activity
```

### **Scenario 3: User Mark Task Complete**

```
┌─ User Tap Checkbox / "Complete" Button
│
├─ TaskAdapter.onTaskComplete(taskId)
│   ├─ dbHelper.updateTask(task.setCompleted(true))
│   └─ reminderService.cancelTaskReminder(taskId) ⭐
│
└─ ReminderService.cancelTaskReminder(taskId)
    ├─ for i = 0 to 9: (Cancel up to 10 notification alarms)
    │   ├─ int requestCode = taskId * 1000 + i
    │   ├─ PendingIntent.getBroadcast(...FLAG_NO_CREATE)
    │   └─ if (pendingIntent != null):
    │       ├─ alarmManager.cancel(pendingIntent)
    │       └─ pendingIntent.cancel()
    │
    └─ Cancel strict alarm:
        ├─ int requestCode = taskId
        ├─ PendingIntent.getBroadcast(...FLAG_NO_CREATE)
        └─ if (pendingIntent != null):
            ├─ alarmManager.cancel(pendingIntent)
            └─ pendingIntent.cancel()
```

### **Scenario 4: Service Start & Foreground**

```
┌─ MainActivity.onCreate()
│   └─ startService(Intent → ReminderService)
│
├─ ReminderService.onCreate() ⭐
│   ├─ dbHelper = TaskDatabaseHelper.getInstance(this)
│   ├─ NotificationHelper.createNotificationChannel(this)
│   └─ Log.d("ReminderService onCreate")
│
├─ ReminderService.onStartCommand()
│   ├─ Notification notification = NotificationHelper.createForegroundNotification()
│   ├─ if (SDK >= Android Q):
│   │   └─ ServiceCompat.startForeground(...FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
│   │
│   ├─ else:
│   │   └─ startForeground(1, notification)
│   │
│   ├─ scheduleAllReminders() ⭐⭐⭐
│   └─ return START_STICKY (Tự restart nếu bị kill)
```

---

## 🎛️ Reminder Time Calculation

### **Công Thức Tính Thời Gian Nhắc Nhở:**

```java
long reminderTime = calculateReminderTime(dueDateMillis, reminderStr)

Các loại Reminder:
┌─ "Đúng giờ" / "on_time"
│  └─ reminderTime = dueDateMillis
│
├─ "5 phút trước" / "5_mins"
│  └─ reminderTime = dueDateMillis - 5 * 60 * 1000
│
├─ "30 phút trước" / "30_mins"
│  └─ reminderTime = dueDateMillis - 30 * 60 * 1000
│
├─ "1 giờ trước" / "1_hour"
│  └─ reminderTime = dueDateMillis - 60 * 60 * 1000
│
├─ "1 ngày trước" / "1_day"
│  └─ reminderTime = dueDateMillis - 24 * 60 * 60 * 1000
│
└─ "custom:label:HH:mm" (Custom Reminder)
   ├─ Parse offset từ label ("Sớm 1 ngày" → 1 day)
   ├─ Parse time từ "HH:mm"
   └─ Tính toán calendar với offset và time mới
```

---

## ⚙️ Request Code Strategy

### **Tách biệt requestCode để không conflict:**

```
┌─ NOTIFICATION ALARM:
│  ├─ requestCode = taskId * 1000 + reminderIndex
│  ├─ Example: taskId=5, reminderIndex=0 → requestCode = 5000
│  ├─ Example: taskId=5, reminderIndex=1 → requestCode = 5001
│  └─ Cho phép 10 reminder per task (index 0-9)
│
└─ STRICT ALARM (Fullscreen):
   ├─ requestCode = taskId
   ├─ Example: taskId=5 → requestCode = 5
   └─ Chỉ 1 strict alarm per task
```

---

## 🚀 Start Service Flow in MainActivity

```java
// Trong MainActivity.onCreate():

// 1️⃣ Bind Service để có reference
Intent serviceIntent = new Intent(this, ReminderService.class);
bindService(serviceIntent, new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ReminderService.LocalBinder binder = (ReminderService.LocalBinder) service;
        reminderService = binder.getService();
        isBound = true;
    }
    
    @Override
    public void onServiceDisconnected(ComponentName name) {
        isBound = false;
    }
}, BIND_AUTO_CREATE);

// 2️⃣ Start Service (Foreground)
Intent startIntent = new Intent(this, ReminderService.class);
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    startForegroundService(startIntent);
} else {
    startService(startIntent);
}

// 3️⃣ Request Permissions
ActivityCompat.requestPermissions(this, 
    new String[]{
        Manifest.permission.SCHEDULE_EXACT_ALARM,
        Manifest.permission.USE_FULL_SCREEN_INTENT,
        Manifest.permission.POST_NOTIFICATIONS
    }, 
    PERMISSION_REQUEST_CODE
);
```

---

## 📊 Lifecycle Callbacks

```
┌─────────────────────────────┐
│   ReminderService Lifecycle │
└─────────────────────────────┘

onCreate()              ← Gọi 1 lần khi service tạo
    ↓
onStartCommand()        ← Gọi mỗi lần startService()
    ↓
onBind()                ← Gọi mỗi lần bindService()
    ↓
[Service Running]
    ↓
onDestroy()             ← Gọi khi service bị stop/kill
    ↓
    ↓
(START_STICKY)
    ↓
    ↓ Service Restart tự động
    ↓
onCreate() → onStartCommand()
```

---

## 📝 Manifest Configuration

```xml
<!-- AndroidManifest.xml -->

<!-- Quyền cần thiết -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- Service Declaration -->
<service
    android:name=".service.ReminderService"
    android:exported="false"
    android:foregroundServiceType="specialUse" />

<!-- Broadcast Receiver for Alarms -->
<receiver
    android:name=".service.AlarmReceiver"
    android:exported="false" />

<!-- Activity to Show Alarm -->
<activity
    android:name=".activity.AlarmActivity"
    android:launchMode="singleInstance"
    android:showOnLockScreen="true"
    android:turnScreenOn="true" />
```

---

## 🔍 Debug Logs to Track

```
TAG = "ALARM_DEBUG"

Log.d(TAG, "--- Bắt đầu scheduleAllReminders ---")
Log.d(TAG, "Tìm thấy " + upcomingTasks.size() + " công việc")
Log.d(TAG, "Đang xử lý Task: " + title)
Log.d(TAG, "Task được ghim → Tiến hành scheduleStrictAlarm")
Log.d(TAG, "Đã set Notification thành công cho: " + title)
Log.d(TAG, "=> Đã đặt AlarmManager (Strict Alarm) thành công")
Log.d(TAG, "Hủy báo thức cho Task ID: " + taskId)
```

---

## 🎓 Key Concepts

| Concept | Giải Thích |
|---------|-----------|
| **Service** | Chạy ngầm, không có UI. ReminderService lên lịch alarm |
| **BroadcastReceiver** | Lắng nghe system broadcast (alarm trigger) |
| **AlarmManager** | Đặt exact alarm tại thời gian cụ thể |
| **PendingIntent** | Intent xảy ra sau. AlarmManager sẽ trigger nó |
| **Foreground Service** | Service với notification, không bị kill dễ |
| **FullScreen Intent** | Notification hiển thị toàn màn hình (báo thức) |
| **Binding** | Kết nối Activity ← → Service để gọi method |
| **requestCode** | ID duy nhất cho mỗi PendingIntent |

---

## 📞 How to Call Service Method from MainActivity

```java
// Sau khi bind thành công:
if (isBound) {
    // Gọi method công khai từ service
    reminderService.scheduleAllReminders();
    // hoặc
    reminderService.cancelTaskReminder(taskId);
}
```


