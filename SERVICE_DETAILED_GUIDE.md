# 🔍 Chi Tiết Từng File Quan Trọng - Service Flow

## 1️⃣ 🔴 ReminderService.java (★★★ CHÍNH)

**Vị Trí:** `app/src/main/java/.../service/ReminderService.java`  
**Dòng:** 282  
**Vai Trò:** Quản lý tất cả alarm & reminder scheduling

### 📍 Các Method Chính:

#### **onCreate()**
```java
public void onCreate() {
    super.onCreate();
    dbHelper = TaskDatabaseHelper.getInstance(this);      // Kết nối DB
    NotificationHelper.createNotificationChannel(this);   // Tạo notification channel
    Log.d(TAG, "ReminderService onCreate");
}
```
- **Gọi lần:** 1 (khi service tạo lần đầu)
- **Mục đích:** Khởi tạo database & notification channel

#### **onStartCommand()**
```java
public int onStartCommand(Intent intent, int flags, int startId) {
    // 1. Start Foreground Service (không bị kill)
    Notification notification = NotificationHelper.createForegroundNotification();
    if (Build.VERSION.SDK_INT >= Q) {
        ServiceCompat.startForeground(this, 1, notification, 
            FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
    } else {
        startForeground(1, notification);
    }
    
    // 2. Schedule tất cả reminder
    scheduleAllReminders();
    
    return START_STICKY;  // Restart nếu bị kill
}
```
- **Gọi lần:** Mỗi lần `startService()` gọi
- **Mục đích:** 
  - Bắt đầu Foreground Service (ensure không bị kill)
  - Schedule lại tất cả reminder

#### **scheduleAllReminders()** (⭐ CỰC KỲ QUAN TRỌNG)
```java
public void scheduleAllReminders() {
    List<TaskModel> upcomingTasks = dbHelper.getUpcomingTasks();
    
    for (TaskModel task : upcomingTasks) {
        // 1️⃣ Notification thường (có thể dismiss)
        scheduleTaskReminder(task);
        
        // 2️⃣ Fullscreen alarm (chỉ nếu pinned)
        if (task.isPinned()) {
            scheduleStrictAlarm(this, task.getId(), task.getTitle(), 
                               task.getDueDateMillis());
        }
    }
}
```
- **Lấy data:** Từ `TaskDatabaseHelper.getUpcomingTasks()`
- **Tác vụ:** 
  - Duyệt qua từng task
  - Gọi `scheduleTaskReminder()` cho notification
  - Gọi `scheduleStrictAlarm()` nếu task pinned

#### **scheduleTaskReminder(TaskModel task)**
```java
public void scheduleTaskReminder(TaskModel task) {
    // 1. Validate
    if (task.getDueDateMillis() <= 0 || task.isCompleted() || 
        task.getReminders().isEmpty()) {
        return; // Bỏ qua
    }
    
    // 2. Duyệt từng reminder offset ("5 phút", "1 ngày", etc)
    for (int i = 0; i < reminders.size(); i++) {
        String reminderStr = reminders.get(i);
        
        // 3. Tính thời gian nhắc nhở
        long reminderTime = calculateReminderTime(task.getDueDateMillis(), reminderStr);
        
        // 4. Bỏ qua nếu đã qua
        if (reminderTime < System.currentTimeMillis()) {
            continue;
        }
        
        // 5. Tạo Intent
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_SHOW_NOTIFICATION);
        intent.putExtra("TASK_ID", task.getId());
        intent.putExtra("TASK_TITLE", task.getTitle());
        
        // 6. Tạo unique request code
        int requestCode = task.getId() * 1000 + i;
        
        // 7. Tạo PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, requestCode, intent, 
            FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE
        );
        
        // 8. Set alarm
        setSafeExactAlarm(alarmManager, reminderTime, pendingIntent);
        Log.d(TAG, "Đã set Notification thành công cho: " + task.getTitle());
    }
}
```
- **Input:** TaskModel object
- **Output:** Đặt multiple AlarmManager alarm (1 alarm per reminder offset)
- **requestCode Strategy:** 
  ```
  taskId=5, reminderIndex=0 → requestCode=5000
  taskId=5, reminderIndex=1 → requestCode=5001
  taskId=5, reminderIndex=2 → requestCode=5002
  ```

#### **scheduleStrictAlarm()** (Fullscreen Alarm)
```java
public static void scheduleStrictAlarm(Context context, int taskId, 
                                       String title, long timeInMillis) {
    AlarmManager alarmManager = (AlarmManager) 
        context.getSystemService(Context.ALARM_SERVICE);
    
    Intent intent = new Intent(context, AlarmReceiver.class);
    intent.setAction(AlarmReceiver.ACTION_START_ALARM);  // ← Khác với SHOW_NOTIFICATION
    intent.putExtra("TASK_ID", taskId);
    intent.putExtra("TASK_TITLE", title);
    
    PendingIntent pendingIntent = PendingIntent.getBroadcast(
        context, taskId,  // ← requestCode = taskId (không như notification)
        intent, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE
    );
    
    setSafeExactAlarm(alarmManager, timeInMillis, pendingIntent);
    Log.d(TAG, "Đã đặt AlarmManager (Strict Alarm) thành công: " + title);
}
```
- **Khi dùng:** `if (task.isPinned())`
- **Action:** `ACTION_START_ALARM` (trigger fullscreen)
- **requestCode:** Chỉ `taskId` (không like reminder)

#### **calculateReminderTime()**
```java
private long calculateReminderTime(long dueDateMillis, String reminderStr) {
    switch(reminderStr) {
        case "Đúng giờ":      return dueDateMillis;
        case "5 phút trước":  return dueDateMillis - 5*60*1000;
        case "30 phút trước": return dueDateMillis - 30*60*1000;
        case "1 giờ trước":   return dueDateMillis - 60*60*1000;
        case "1 ngày trước":  return dueDateMillis - 24*60*60*1000;
        case "custom:...":    return parseCustom(...);
        default:              return dueDateMillis;
    }
}
```
- **Input:** Due date (millis) + reminder offset string
- **Output:** Actual reminder time (millis)

#### **setSafeExactAlarm()** (Android version handling)
```java
private static void setSafeExactAlarm(AlarmManager alarmManager, 
                                      long timeInMillis, 
                                      PendingIntent pendingIntent) {
    try {
        if (Build.VERSION.SDK_INT >= S) {  // Android 12+
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
                );
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
                );
            }
        } else if (Build.VERSION.SDK_INT >= M) {  // Android 6+
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
            );
        } else {  // < Android 6
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, 
                                 timeInMillis, pendingIntent);
        }
    } catch (SecurityException e) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, 
                        timeInMillis, pendingIntent);
    }
}
```
- **Mục đích:** Set alarm với compatibility cho tất cả Android versions

#### **cancelTaskReminder(int taskId)**
```java
public void cancelTaskReminder(int taskId) {
    // 1. Hủy tất cả notification alarm (10 reminder per task)
    for (int i = 0; i < 10; i++) {
        int requestCode = taskId * 1000 + i;
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_SHOW_NOTIFICATION);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, requestCode, intent, 
            FLAG_NO_CREATE | FLAG_IMMUTABLE
        );
        
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
    
    // 2. Hủy strict alarm
    Intent strictIntent = new Intent(this, AlarmReceiver.class);
    strictIntent.setAction(AlarmReceiver.ACTION_START_ALARM);
    
    PendingIntent strictPendingIntent = PendingIntent.getBroadcast(
        this, taskId, strictIntent, 
        FLAG_NO_CREATE | FLAG_IMMUTABLE
    );
    
    if (strictPendingIntent != null) {
        alarmManager.cancel(strictPendingIntent);
        strictPendingIntent.cancel();
    }
}
```
- **Khi gọi:** 
  - Task completed
  - Task deleted
  - Reminder removed
- **Mục đích:** Remove tất cả pending alarm

---

## 2️⃣ 🟡 AlarmReceiver.java (★★★)

**Vị Trí:** `app/src/main/java/.../service/AlarmReceiver.java`  
**Dòng:** 40  
**Vai Trò:** Nhận broadcast từ AlarmManager, trigger notification/alarm

### 📍 Các Method:

#### **onReceive()** (⭐ ENTRY POINT)
```java
@Override
public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    int taskId = intent.getIntExtra("TASK_ID", -1);
    String taskTitle = intent.getStringExtra("TASK_TITLE");
    
    if (ACTION_SHOW_NOTIFICATION.equals(action)) {
        // 📱 Thông báo thường (có thể dismiss)
        boolean isOnTime = intent.getBooleanExtra("IS_ON_TIME", true);
        long dueDate = intent.getLongExtra("TASK_DUE_DATE", 0);
        
        NotificationHelper.showNotification(context, taskId, taskTitle, 
                                           isOnTime, dueDate);
    } 
    else if (ACTION_START_ALARM.equals(action)) {
        // 🚨 Fullscreen alarm (Strict)
        
        // 1. Cố gắng mở AlarmActivity trực tiếp
        try {
            Intent fullScreenIntent = new Intent(context, AlarmActivity.class);
            fullScreenIntent.putExtra("TASK_ID", taskId);
            fullScreenIntent.putExtra("TASK_TITLE", taskTitle);
            fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                                     Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(fullScreenIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 2. Fallback: Show fullscreen notification (Android 10+)
        NotificationHelper.showAlarmNotification(context, taskId, taskTitle);
    }
}
```
- **Được gọi:** Khi AlarmManager trigger
- **Two paths:**
  - `ACTION_SHOW_NOTIFICATION` → Regular notification
  - `ACTION_START_ALARM` → Fullscreen alarm

### 📋 Constants:
```java
public static final String ACTION_SHOW_NOTIFICATION = 
    "hcmute.edu.vn.ticktick.SHOW_NOTIFICATION";
public static final String ACTION_START_ALARM = 
    "hcmute.edu.vn.ticktick.START_ALARM";
```

---

## 3️⃣ 🟢 MainActivity.java (★★★)

**Vị Trí:** `app/src/main/java/.../activity/MainActivity.java`  
**Dòng:** 1012  
**Vai Trò:** Main UI, bind service, trigger reschedule

### 📍 Key Parts:

#### **onCreate()** (Service Binding)
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // 1️⃣ Start Service (Foreground)
    Intent serviceIntent = new Intent(this, ReminderService.class);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(serviceIntent);
    } else {
        startService(serviceIntent);
    }
    
    // 2️⃣ Bind Service (để có reference)
    bindService(serviceIntent, new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ReminderService.LocalBinder binder = 
                (ReminderService.LocalBinder) service;
            reminderService = binder.getService();
            isBound = true;
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    }, BIND_AUTO_CREATE);
    
    // 3️⃣ Request Permissions
    requestPermissions();
}
```

#### **Task Update Callbacks**
```java
// Khi user thêm/sửa task
private void onTaskUpdated() {
    dbHelper.updateTask(task);
    taskAdapter.notifyDataSetChanged();
    
    // ⭐ Reschedule alarm
    if (isBound) {
        reminderService.scheduleAllReminders();
    }
}

// Khi user hoàn thành task
private void onTaskCompleted(int taskId) {
    dbHelper.updateTask(taskId, completed=true);
    taskAdapter.notifyDataSetChanged();
    
    // ⭐ Cancel alarm
    if (isBound) {
        reminderService.cancelTaskReminder(taskId);
    }
}
```

#### **Permission Request**
```java
private void requestPermissions() {
    List<String> permissions = new ArrayList<>();
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS);
    }
    
    permissions.add(Manifest.permission.SCHEDULE_EXACT_ALARM);
    permissions.add(Manifest.permission.USE_FULL_SCREEN_INTENT);
    permissions.add(Manifest.permission.WAKE_LOCK);
    
    if (!permissions.isEmpty()) {
        ActivityCompat.requestPermissions(this, 
            permissions.toArray(new String[0]), 
            PERMISSION_REQUEST_CODE);
    }
}
```

---

## 4️⃣ 🔵 TaskDatabaseHelper.java (★★★)

**Vị Trí:** `app/src/main/java/.../database/TaskDatabaseHelper.java`  
**Vai Trò:** SQLite database operations

### 📍 Key Methods:

#### **getUpcomingTasks()**
```java
public List<TaskModel> getUpcomingTasks() {
    // SELECT * WHERE dueDateMillis > now AND !isCompleted
    // ORDER BY dueDateMillis ASC
    
    // Dùng để load tasks cần schedule alarm
}
```

#### **updateTask() / insertTask() / deleteTask()**
- Khi có thay đổi → MainActivity gọi `rescheduleAllReminders()`

---

## 5️⃣ 🟠 NotificationHelper.java (★★★)

**Vị Trí:** `app/src/main/java/.../utils/NotificationHelper.java`  
**Dòng:** 158  
**Vai Trò:** Create & display notifications

### 📍 Key Methods:

#### **createNotificationChannel()**
```java
public static void createNotificationChannel(Context context) {
    // 3 channels:
    // 1. CHANNEL_ID (Regular notification)
    // 2. ALARM_CHANNEL_ID (Alarm notification)
    // 3. SERVICE_CHANNEL_ID (Foreground service)
}
```

#### **showNotification()**
- Hiển thị notification bình thường
- User có thể dismiss

#### **showAlarmNotification()**
- Hiển thị fullscreen intent notification
- Wakes screen, high priority
- Fallback khi không thể open AlarmActivity

#### **cancelNotification()**
- Tắt notification dựa trên taskId

---

## 6️⃣ 🟠 AlarmActivity.java (★★)

**Vai Trò:** Fullscreen alarm UI khi task due

```java
public class AlarmActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Get task info from intent
        int taskId = getIntent().getIntExtra("TASK_ID", -1);
        String taskTitle = getIntent().getStringExtra("TASK_TITLE");
        
        // 2. Show fullscreen UI
        // 3. Buttons: Complete, Dismiss, Snooze
        
        // 4. When user tap Complete:
        dbHelper.updateTask(taskId, completed=true);
        reminderService.cancelTaskReminder(taskId);
        finish();
    }
}
```

---

## 📊 Complete Service Lifecycle

```
┌─────────────────────────────────────────────────────┐
│ 🔴 ReminderService.java                             │
│ ┌───────────────────────────────────────────────┐   │
│ │ onCreate()                                    │   │
│ │  - dbHelper = getInstance()                  │   │
│ │  - createNotificationChannel()               │   │
│ └───────────────────────────────────────────────┘   │
│         ↓                                            │
│ ┌───────────────────────────────────────────────┐   │
│ │ onStartCommand()                              │   │
│ │  - startForeground(notification)             │   │
│ │  - scheduleAllReminders() ⭐⭐⭐           │   │
│ │    ├─ scheduleTaskReminder()                │   │
│ │    │  └─ set notification alarm              │   │
│ │    └─ scheduleStrictAlarm()                 │   │
│ │       └─ set fullscreen alarm               │   │
│ └───────────────────────────────────────────────┘   │
│         ↓                                            │
│ ┌───────────────────────────────────────────────┐   │
│ │ onBind() [MainActivity bindService]           │   │
│ │  - return binder                              │   │
│ │  - MainActivity holds reminderService ref    │   │
│ └───────────────────────────────────────────────┘   │
│         ↓                                            │
│ ⏱️  ALARM MANAGER WAITS FOR TIME...               │
│         ↓                                            │
│ ┌───────────────────────────────────────────────┐   │
│ │ 🟡 AlarmReceiver.onReceive()                 │   │
│ │  - Check ACTION_SHOW_NOTIFICATION            │   │
│ │  - Check ACTION_START_ALARM                  │   │
│ └───────────────────────────────────────────────┘   │
│         ↓                                            │
│ ┌───────────────────────────────────────────────┐   │
│ │ 🟠 NotificationHelper                         │   │
│ │  - showNotification() / showAlarmNotification│   │
│ └───────────────────────────────────────────────┘   │
│         ↓                                            │
│ ┌───────────────────────────────────────────────┐   │
│ │ 🟢 User sees: Notification or AlarmActivity │   │
│ └───────────────────────────────────────────────┘   │
│         ↓                                            │
│ ┌───────────────────────────────────────────────┐   │
│ │ onDestroy()                                   │   │
│ │  - (Service được kill hoặc stop)            │   │
│ │  - START_STICKY → restart lại               │   │
│ └───────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```


