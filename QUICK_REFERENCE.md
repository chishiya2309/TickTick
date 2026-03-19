# ⚡ Quick Reference Guide - Service Flow

## 🎯 Start Here: The 5-Step Service Flow

### **Step 1: App Starts - MainActivity.onCreate()**
```java
// What happens:
1. startForegroundService(ReminderService)  ← Start service
2. bindService(ReminderService)              ← Connect to service
3. requestPermissions()                      ← Ask user

// Result: Service now running, reminders will work
```

### **Step 2: Service Starts - ReminderService.onCreate() & onStartCommand()**
```java
// What happens:
1. dbHelper = get database connection
2. createNotificationChannel()               ← Prepare notifications
3. startForeground(notification)             ← Show foreground notification
4. scheduleAllReminders()                    ← ⭐⭐⭐ MAIN JOB

// Result: All alarms scheduled from database
```

### **Step 3: Schedule All Reminders - scheduleAllReminders()**
```java
// What happens:
tasks = dbHelper.getUpcomingTasks()         ← Get all not-completed tasks

for each task:
    if (task has reminders) {
        scheduleTaskReminder(task)          ← Normal notification
    }
    
    if (task.isPinned()) {
        scheduleStrictAlarm(task)           ← Fullscreen alarm
    }

// Result: All alarms registered with AlarmManager
```

### **Step 4: Alarm Time Arrives - AlarmReceiver.onReceive()**
```java
// What happens:
AlarmManager triggers at the scheduled time
    ↓
AlarmReceiver.onReceive() is called
    ├─ Check action: SHOW_NOTIFICATION or START_ALARM
    │
    ├─ if SHOW_NOTIFICATION:
    │  └─ NotificationHelper.showNotification()
    │     └─ User sees dismissible notification
    │
    └─ if START_ALARM:
       ├─ Try: startActivity(AlarmActivity)
       └─ Fallback: showAlarmNotification()
           └─ User sees fullscreen notification

// Result: User sees notification or alarm
```

### **Step 5: User Interacts - Mark Complete or Dismiss**
```java
// What happens:
User completes task or dismisses alarm
    ↓
MainActivity.onTaskComplete(taskId)
    ├─ dbHelper.updateTask(completed=true)
    └─ reminderService.cancelTaskReminder(taskId)
        ├─ Cancel all 10 notification alarms
        ├─ Cancel 1 strict alarm
        └─ AlarmManager.cancel()

// Result: No more alarms for this task
```

---

## 📌 Key Constants & Request Codes

### **Action Strings**
```java
// In AlarmReceiver
ACTION_SHOW_NOTIFICATION = "hcmute.edu.vn.ticktick.SHOW_NOTIFICATION"
ACTION_START_ALARM = "hcmute.edu.vn.ticktick.START_ALARM"
```

### **Request Codes**
```java
// For Notification Alarms
requestCode = taskId * 1000 + reminderIndex
Example: taskId=5, reminderIndex=2 → requestCode=5002

// For Strict Alarms
requestCode = taskId
Example: taskId=5 → requestCode=5
```

### **Notification Channel IDs**
```java
CHANNEL_ID = "task_reminder_channel_v3"           (Regular notification)
ALARM_CHANNEL_ID = "task_alarm_channel"           (Alarm notification)
SERVICE_CHANNEL_ID = "task_service_channel"       (Foreground service)
```

---

## 🔗 File Cross-Reference

### **When editing MainActivity:**
- Need to: Bind/Start ReminderService
- Import: `import hcmute.edu.vn.lequanghung_23110110.ticktick.service.ReminderService;`
- Method: `bindService()`, `startForegroundService()`

### **When editing ReminderService:**
- Need to: Access DB, call AlarmManager, show notifications
- Import: `TaskDatabaseHelper`, `NotificationHelper`
- Methods: `scheduleTaskReminder()`, `scheduleStrictAlarm()`, `cancelTaskReminder()`

### **When editing AlarmReceiver:**
- Need to: Check intent action, show notification, start activity
- Import: `NotificationHelper`, `AlarmActivity`
- Methods: `onReceive()` - single entry point

### **When editing NotificationHelper:**
- Need to: Create channels, show notifications
- Used by: `ReminderService`, `AlarmReceiver`
- Methods: `createNotificationChannel()`, `showNotification()`, `showAlarmNotification()`

### **When editing TaskDatabaseHelper:**
- Triggers reschedule: In MainActivity when `updateTask()` called
- Need to call: `reminderService.scheduleAllReminders()` after updates

---

## 💡 Common Scenarios

### **❓ "I need to change when reminder fires"**
→ Edit: `ReminderService.calculateReminderTime()`
→ Check: The reminder string parsing logic

### **❓ "Notification not showing"**
→ Check: `NotificationHelper.createNotificationChannel()`
→ Check: Permissions in `AndroidManifest.xml`
→ Check: `AlarmReceiver.onReceive()` is being called

### **❓ "Alarm not waking screen"**
→ Edit: `AlarmReceiver.ACTION_START_ALARM` path
→ Check: `AlarmActivity` has `android:showOnLockScreen="true"`
→ Check: `AlarmManager.setExactAndAllowWhileIdle()` used

### **❓ "Alarm keeps firing after task complete"**
→ Check: `MainActivity.onTaskComplete()` calls `cancelTaskReminder()`
→ Check: `ReminderService.cancelTaskReminder()` cancels all alarms

### **❓ "Service keeps getting killed"**
→ Add: `START_STICKY` in `onStartCommand()`
→ Check: `startForeground()` is called
→ Check: Foreground notification is set

### **❓ "Multiple alarms for one task"**
→ Root cause: Same requestCode used
→ Fix: Ensure unique requestCode = `taskId * 1000 + reminderIndex`

---

## 🧪 Testing Checklist

- [ ] Create task with due date
- [ ] Set multiple reminders (5min, 1 day, etc)
- [ ] Verify correct number of AlarmManager alarms
- [ ] Advance device clock (or use adb time)
- [ ] Verify notification/alarm appears at correct time
- [ ] Mark task complete → Verify alarms cancel
- [ ] Edit task date → Verify alarms reschedule
- [ ] Pinned task → Verify fullscreen alarm (if time = due date)
- [ ] Check logcat for: `Log.d(TAG, "ALARM_DEBUG")`

---

## 🐛 Debug Logs to Monitor

```java
// In ReminderService
Log.d("ALARM_DEBUG", "--- Bắt đầu scheduleAllReminders ---")
Log.d("ALARM_DEBUG", "Tìm thấy X công việc sắp tới")
Log.d("ALARM_DEBUG", "Đang xử lý Task: " + title)
Log.d("ALARM_DEBUG", "Đã set Notification thành công: " + title)
Log.d("ALARM_DEBUG", "Hủy báo thức cho Task ID: " + taskId)

// In AlarmReceiver
// No logs by default, but notified by AlarmManager trigger
```

**To enable debug:**
1. Open Android Studio Logcat
2. Filter by: "ALARM_DEBUG"
3. Create/edit task
4. Look for schedule logs
5. Advance time to see trigger

---

## 📞 Method Call Hierarchy

```
MainActivity.onCreate()
    ├─ startForegroundService()
    │   └─ ReminderService.onCreate()
    │       └─ ReminderService.onStartCommand()
    │           └─ ReminderService.scheduleAllReminders() ⭐
    │               ├─ TaskDatabaseHelper.getUpcomingTasks()
    │               └─ for each task:
    │                   ├─ scheduleTaskReminder()
    │                   │   └─ AlarmManager.setExactAndAllowWhileIdle()
    │                   └─ scheduleStrictAlarm()
    │                       └─ AlarmManager.setExactAndAllowWhileIdle()
    │
    └─ bindService()
        └─ ReminderService.onBind()
            └─ return LocalBinder (MainActivity gets reference)

MainActivity.onTaskUpdated()
    ├─ TaskDatabaseHelper.updateTask()
    ├─ TaskAdapter.notifyDataSetChanged()
    └─ reminderService.scheduleAllReminders()  ← Reschedule

MainActivity.onTaskComplete()
    ├─ TaskDatabaseHelper.updateTask(completed=true)
    ├─ TaskAdapter.notifyDataSetChanged()
    └─ reminderService.cancelTaskReminder()    ← Cancel alarm

[Time Passes]

AlarmManager.trigger()
    └─ AlarmReceiver.onReceive()
        ├─ if ACTION_SHOW_NOTIFICATION:
        │   └─ NotificationHelper.showNotification()
        └─ if ACTION_START_ALARM:
            ├─ startActivity(AlarmActivity) [try]
            └─ NotificationHelper.showAlarmNotification() [fallback]

User interaction:
    └─ AlarmActivity / MainActivity
        └─ onTaskComplete() [as above]
```

---

## 🎓 Understanding REQUEST CODES

**Why unique request codes?**
```
When you call: PendingIntent.getBroadcast()
  └─ System creates a unique PendingIntent for this (requestCode, intent action)

If you call it twice with SAME requestCode:
  └─ System UPDATES (replaces) the previous one

This is why we need DIFFERENT requestCode for EACH reminder!

Example:
Task ID = 5

Reminder 1 (5 min):   requestCode = 5000
Reminder 2 (1 day):   requestCode = 5001

If we used requestCode = 5 for both:
  ├─ First call: setExactAlarm(..., 5, intent1)
  │   → System creates alarm
  │
  └─ Second call: setExactAlarm(..., 5, intent2)
     → System REPLACES first alarm with second!
     → Only second alarm will fire
     → BUG: First reminder never fires!
```

**Solution:**
```
Notification Alarms:    requestCode = taskId * 1000 + reminderIndex
  5000, 5001, 5002, 5003, ... 5009

Strict Alarm:           requestCode = taskId
  5 (no conflict, range is [1-9999])
```

---

## 🔄 Reminder Offset Types

| Type | String | Offset | Example |
|------|--------|--------|---------|
| On Time | "Đúng giờ" | 0 | If due 5pm → remind 5pm |
| 5 min | "5 phút trước" | -5min | If due 5pm → remind 4:55pm |
| 30 min | "30 phút trước" | -30min | If due 5pm → remind 4:30pm |
| 1 hour | "1 giờ trước" | -1hour | If due 5pm → remind 4pm |
| 1 day | "1 ngày trước" | -1day | If due 5pm → remind prev day 5pm |
| Custom | "custom:label:HH:mm" | Parse | If "custom:1 ngày:09:00" → 9am 1 day before |

---

## 🏗️ Layer Architecture Summary

```
┌─────────────────────────────────────┐
│ PRESENTATION (UI)                   │
│ MainActivity, AlarmActivity          │
│ TaskAdapter, Dialogs                 │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ SERVICE/BUSINESS LOGIC              │
│ ReminderService                     │
│ AlarmReceiver                        │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ UTILITIES                           │
│ NotificationHelper                  │
│ DailyBriefingScheduler              │
│ TaskSwipeHelper                     │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ DATA ACCESS (Database)              │
│ TaskDatabaseHelper                  │
│ TaskModel                           │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ SYSTEM SERVICES                     │
│ AlarmManager (Android)              │
│ NotificationManager (Android)       │
│ SQLite Database (Android)           │
└─────────────────────────────────────┘
```

---

## 🚀 How to Add a New Feature

### **Example: Send SMS when reminder fires**

1. **Find the trigger point:**
   - `AlarmReceiver.onReceive()` is called

2. **Add your logic:**
   ```java
   public void onReceive(Context context, Intent intent) {
       if (ACTION_SHOW_NOTIFICATION.equals(action)) {
           NotificationHelper.showNotification(...);
           
           // NEW: Send SMS
           SmsHelper.sendTaskReminder(taskId, taskTitle);
       }
   }
   ```

3. **Create new helper:**
   - New file: `utils/SmsHelper.java`
   - Method: `sendTaskReminder(int taskId, String taskTitle)`

4. **Add permission:**
   - `AndroidManifest.xml`: `<uses-permission android:name="android.permission.SEND_SMS" />`

5. **Request permission:**
   - `MainActivity.requestPermissions()`: Add `Manifest.permission.SEND_SMS`

---

## 📞 Contact Points Between Components

| From | To | Method | Purpose |
|------|----|---------| ---------|
| MainActivity | ReminderService | `startService()` | Start service |
| MainActivity | ReminderService | `bindService()` | Get reference |
| MainActivity | ReminderService | `scheduleAllReminders()` | Reschedule after task change |
| ReminderService | TaskDatabaseHelper | `getUpcomingTasks()` | Fetch tasks |
| ReminderService | AlarmManager | `setExactAndAllowWhileIdle()` | Set alarm |
| AlarmManager | AlarmReceiver | Broadcast intent | Trigger alarm |
| AlarmReceiver | NotificationHelper | `showNotification()` | Show UI |
| AlarmReceiver | AlarmActivity | `startActivity()` | Open fullscreen |
| NotificationHelper | NotificationManager | `notify()` | Display notification |
| TaskDatabaseHelper | TaskModel | Constructor | Create objects |


