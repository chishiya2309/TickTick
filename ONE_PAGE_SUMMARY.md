# 🎯 Android Service Flow - One Page Summary

## The Essence of TickTick Service Flow

```
┌─ USER ─┐
    ↓
┌──────────────────────────────────┐
│ FRONTEND                         │
│ MainActivity                     │
│ - User creates/edits task       │
│ - Binds to ReminderService      │
│ - Shows task list UI            │
└──────────┬───────────────────────┘
           ↓
┌──────────────────────────────────┐
│ REMINDER SERVICE (Background)    │
│ - Loads all upcoming tasks       │
│ - For each task:                 │
│   • If has reminders →           │
│     Set notification alarms      │
│   • If pinned →                  │
│     Set fullscreen alarm         │
└──────────┬───────────────────────┘
           ↓
┌──────────────────────────────────┐
│ ALARM MANAGER (System)           │
│ ⏰ Waits for scheduled time      │
└──────────┬───────────────────────┘
           ↓
┌──────────────────────────────────┐
│ BROADCAST RECEIVER               │
│ AlarmReceiver                    │
│ - Receives intent at trigger     │
│ - Routes to notification/alarm   │
└──────────┬───────────────────────┘
           ↓
     ┌─────┴──────┐
     ↓            ↓
┌─────────┐  ┌─────────────────┐
│Notif.   │  │AlarmActivity    │
│(Dismiss)│  │(Fullscreen)     │
└─────────┘  └─────────────────┘
     ↓            ↓
     └─────┬──────┘
           ↓
┌──────────────────────────────────┐
│ USER ACTION                      │
│ - Mark Complete → Cancel alarms  │
│ - Dismiss → Nothing              │
└──────────────────────────────────┘
```

---

## Core Files

### 1. **ReminderService.java** (282 lines)
**Main job: Schedule & cancel alarms**

```java
onCreate()              → Setup DB & channels
onStartCommand()        → Start foreground, call scheduleAllReminders()
scheduleAllReminders()  → Loop tasks, set alarms
scheduleTaskReminder()  → Set notification alarm (per reminder offset)
scheduleStrictAlarm()   → Set fullscreen alarm (if pinned)
calculateReminderTime() → Convert "5 min" → actual milliseconds
cancelTaskReminder()    → Remove all alarms when task complete
```

### 2. **AlarmReceiver.java** (40 lines)
**Single method: Receive alarm, show notification**

```java
onReceive()
├─ if ACTION_SHOW_NOTIFICATION
│   └─ NotificationHelper.showNotification()
└─ if ACTION_START_ALARM
    ├─ Try: startActivity(AlarmActivity)
    └─ Else: showAlarmNotification() [fullscreen]
```

### 3. **MainActivity.java** (1012 lines)
**Entry point: Start service, bind it, trigger reschedule**

```java
onCreate()              → startService() + bindService()
onTaskUpdated()         → Call reminderService.scheduleAllReminders()
onTaskComplete()        → Call reminderService.cancelTaskReminder()
```

### 4. **TaskDatabaseHelper.java**
**Data source: Get tasks from SQLite**

```java
getUpcomingTasks()      → SELECT tasks WHERE due > now AND !completed
[Trigger reschedule when data changes]
```

### 5. **NotificationHelper.java** (158 lines)
**UI layer: Create channels, show notifications**

```java
createNotificationChannel()     → Setup 3 channels (notification, alarm, service)
showNotification()              → Show dismissible notification
showAlarmNotification()          → Show fullscreen notification
```

---

## The Flow (5 Steps)

### **Step 1: Init** (App Start)
```
MainActivity.onCreate()
  → startForegroundService(ReminderService)
  → bindService(ReminderService)
  → requestPermissions()
```

### **Step 2: Schedule** (Service Start)
```
ReminderService.onStartCommand()
  → startForeground(notification)
  → scheduleAllReminders()
     → for each task:
        → scheduleTaskReminder() [notification per reminder]
        → scheduleStrictAlarm() if pinned [fullscreen]
```

### **Step 3: Wait** (System Level)
```
AlarmManager waits for scheduled time
(No code running, device can sleep)
```

### **Step 4: Trigger** (Alarm Time)
```
AlarmManager wakes device
  → Sends broadcast intent
  → AlarmReceiver.onReceive()
     → Check action
     → Show notification or fullscreen alarm
```

### **Step 5: Complete** (User Action)
```
User marks task complete
  → MainActivity.onTaskComplete()
  → reminderService.cancelTaskReminder()
  → AlarmManager.cancel(all pending intents)
  → Task disappears from UI
```

---

## Key Implementation Details

### Request Code Strategy
```
NOTIFICATION: requestCode = taskId * 1000 + reminderIndex
  Example: taskId=5, reminder=2 → code=5002

STRICT:       requestCode = taskId
  Example: taskId=5 → code=5

WHY? AlarmManager uses (requestCode, action) to identify PendingIntent.
Same requestCode → Same PendingIntent (update instead of create).
Different reminders need different codes!
```

### Reminder Offset Calculation
```
"Đúng giờ"      → dueDateMillis (no offset)
"5 phút trước"  → dueDateMillis - 5*60*1000
"30 phút trước" → dueDateMillis - 30*60*1000
"1 giờ trước"   → dueDateMillis - 60*60*1000
"1 ngày trước"  → dueDateMillis - 24*60*60*1000
"custom:X:Y"    → parse & calculate
```

### Alarm Types
```
ACTION_SHOW_NOTIFICATION
  ├─ Regular notification
  ├─ User can dismiss
  ├─ Tap → Open MainActivity
  └─ requestCode = taskId * 1000 + i

ACTION_START_ALARM
  ├─ Fullscreen notification (Android 10+)
  ├─ Wakes screen, high priority
  ├─ Tap → Open AlarmActivity
  └─ requestCode = taskId (only if pinned)
```

---

## Data Flow: Notifications

```
Task Table (SQLite)
  ├─ id, title, description
  ├─ dueDateMillis
  ├─ isPinned
  └─ reminders (List<String>)
    ↓
ReminderService reads
  ↓
For each reminder:
  ├─ Calculate time = calculateReminderTime(due, offset)
  ├─ Create Intent (action + extras)
  ├─ Create PendingIntent (requestCode = id*1000+i)
  ├─ setExactAndAllowWhileIdle(time, pending)
  └─ AlarmManager stores it
    ↓
At trigger time:
  ├─ AlarmManager sends broadcast
  ├─ AlarmReceiver.onReceive()
  ├─ NotificationHelper.showNotification()
  ├─ NotificationManager.notify(taskId, notification)
  └─ User sees notification
```

---

## Lifecycle

### Service Lifecycle
```
onCreate()          ← 1st time only
  ↓
onStartCommand()    ← Every startService()
  ↓
onBind()            ← Every bindService()
  ↓
[Running]
  ↓
onDestroy()         ← Manual stop or system kill
  ↓
START_STICKY        ← Auto restart
  ↓
onCreate() again
```

### Task Lifecycle
```
CREATED (Not Due)
  ├─ Alarms scheduled ✓
  └─ Awaiting reminder time
    ↓
REMINDER FIRED (Time reached)
  ├─ Notification shown
  └─ User sees reminder
    ↓
COMPLETED (User marks done)
  ├─ Alarms cancelled ✓
  └─ Removed from UI
```

---

## Important Constants

```java
// Actions (for AlarmReceiver to differentiate)
ACTION_SHOW_NOTIFICATION = "hcmute.edu.vn.ticktick.SHOW_NOTIFICATION"
ACTION_START_ALARM = "hcmute.edu.vn.ticktick.START_ALARM"

// Notification Channels (Android 8+)
CHANNEL_ID = "task_reminder_channel_v3"           (regular)
ALARM_CHANNEL_ID = "task_alarm_channel"           (alarm)
SERVICE_CHANNEL_ID = "task_service_channel"       (foreground)

// Intent Extras
"TASK_ID", "TASK_TITLE", "IS_ON_TIME", "TASK_DUE_DATE"
```

---

## Permissions Required

```xml
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

---

## Manifest Declarations

```xml
<service
    android:name=".service.ReminderService"
    android:exported="false"
    android:foregroundServiceType="specialUse" />

<receiver
    android:name=".service.AlarmReceiver"
    android:exported="false" />

<activity
    android:name=".activity.AlarmActivity"
    android:showOnLockScreen="true"
    android:turnScreenOn="true" />
```

---

## When Something Goes Wrong

| Problem | Likely Cause | Solution |
|---------|--------------|----------|
| Alarm never fires | requestCode conflict | Check request code unique |
| Only 1 reminder fires | requestCode same for multiple | Use taskId * 1000 + i |
| Fullscreen never shows | Not pinned | Check task.isPinned() |
| Notification doesn't show | Channel not created | Call createNotificationChannel() |
| Service gets killed | Not foreground | Call startForeground() |
| Alarm fires multiple times | Not cancelled properly | Check cancelTaskReminder() |
| After restart, no reminders | Not START_STICKY | Add return START_STICKY |

---

## Debug Checklist

```
✓ Task created with due date?
✓ Reminders list not empty?
✓ Can see logs: "Đã set Notification thành công"?
✓ Task scheduled time > now?
✓ Permissions granted?
✓ Service foreground notification showing?
✓ Advance device clock to trigger time
✓ Check logcat tag: "ALARM_DEBUG"
✓ If fired: Check notification or AlarmActivity
✓ Mark complete: Check cancelTaskReminder logs
```

---

## One Liner Summary

> **ReminderService schedules alarms via AlarmManager. When time reaches, AlarmReceiver gets broadcast, shows notification. User completes → cancelTaskReminder → AlarmManager.cancel().**

---

## Next Steps

1. Read `SERVICE_FLOW_DIAGRAM.md` (30 min)
2. Read `SERVICE_DETAILED_GUIDE.md` (1 hour)
3. Trace code in IDE following `QUICK_REFERENCE.md`
4. Add a feature or fix a bug
5. Use `ARCHITECTURE_DIAGRAMS.md` to visualize

---

**Good luck learning Android Service! 🚀**


