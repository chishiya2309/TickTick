# 🎨 Visual Architecture Diagrams

## 📐 Overall Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         TICKTICK APP ARCHITECTURE                        │
└─────────────────────────────────────────────────────────────────────────┘

                              ┌──────────────┐
                              │   DATABASE   │
                              │  (SQLite)    │
                              │              │
                              │ - Task List  │
                              │ - Reminders  │
                              │ - Lists      │
                              └───────┬──────┘
                                      ↓
                   ┌─────────────────────────────────────┐
                   │      DATABASE HELPER LAYER          │
                   │  TaskDatabaseHelper.java (CRUD)     │
                   │                                     │
                   │ - getUpcomingTasks()               │
                   │ - updateTask()                     │
                   │ - insertTask()                     │
                   │ - deleteTask()                     │
                   └──────────┬──────────────┬──────────┘
                              │              │
         ┌────────────────────┘              └────────────────────┐
         ↓                                                        ↓
    ┌─────────────────────────┐                    ┌──────────────────────┐
    │   SERVICE LAYER         │                    │   ACTIVITY LAYER     │
    │ ┌──────────────────────┐ │                    │ ┌──────────────────┐ │
    │ │ ReminderService      │ │                    │ │ MainActivity     │ │
    │ │ ┌────────────────────┤ │                    │ │ ┌──────────────┐ │ │
    │ │ │ - onCreate()       │ │                    │ │ │ - onCreate() │ │ │
    │ │ │ - onStartCommand() │ │◄──── Bind Service ─┼─┤ │ - bindService│ │ │
    │ │ │ - onBind()         │ │                    │ │ │ - startService│ │ │
    │ │ │ - onDestroy()      │ │                    │ │ └──────────────┘ │ │
    │ │ └────────────────────┤ │                    │ │                  │ │
    │ │ scheduleAllReminders()│ │                    │ │ Task Operations: │ │
    │ │ ├─ scheduleTaskReminder() │                 │ │ - Add Task       │ │
    │ │ └─ scheduleStrictAlarm()  │                 │ │ - Edit Task      │ │
    │ │                            │                 │ │ - Complete Task  │ │
    │ │ cancelTaskReminder()       │                 │ │ - Delete Task    │ │
    │ └────────────────────────────┘                │ └──────────────────┘ │
    │                                               │                      │
    │      ┌─────────────────────────────────────┐  │  ┌────────────────┐ │
    │      │  AlarmReceiver (BroadcastReceiver)  │  │  │ SearchActivity │ │
    │      │                                     │  │  └────────────────┘ │
    │      │ onReceive(Intent)                   │  │                      │
    │      │ ├─ ACTION_SHOW_NOTIFICATION        │  │  ┌────────────────┐ │
    │      │ └─ ACTION_START_ALARM               │  │  │ AlarmActivity  │ │
    │      └─────────────────────────────────────┘  │  │ (Fullscreen)   │ │
    │                                               │  └────────────────┘ │
    └───────────────────────────────────────────────┴──────────────────────┘
         ↓                                    ↓                     ↓
    ┌──────────────┐              ┌──────────────────┐    ┌─────────────┐
    │ AlarmManager │              │ NotificationHelper│    │    Utils    │
    │ (System)     │              │                  │    │             │
    │              │              │ - Notification   │    │ - Swipe     │
    │ setExact()   │              │ - Alarm          │    │ - Listeners │
    │ setAnd       │              │ - Channel        │    │ - Schedulers│
    │ AllowWhileIdle│              └──────────────────┘    └─────────────┘
    └──────────────┘
```

---

## 🔄 Complete Task Lifecycle Flow

```
┌────────────────────────────────────────────────────────────────────────────┐
│                        TASK LIFECYCLE FLOW                                 │
└────────────────────────────────────────────────────────────────────────────┘

1️⃣  USER CREATES TASK
    ┌──────────────────┐
    │ MainActivity     │
    │ + Click FAB      │
    │ + TaskDetailBS   │
    └────────┬─────────┘
             ↓
    ┌──────────────────┐
    │ User Input:      │
    │ - Title          │
    │ - Due Date ⭐    │
    │ - Reminders ⭐   │
    │ - Pinned? ⭐     │
    └────────┬─────────┘
             ↓
    ┌──────────────────────────────────────┐
    │ DatabaseHelper.insertTask(task)      │
    │ └─ INSERT INTO tasks (...)           │
    └────────┬─────────────────────────────┘
             ↓
    ┌──────────────────────────────────────┐
    │ ReminderService.scheduleAllReminders()│ ⭐⭐⭐
    │ ├─ getUpcomingTasks()                │
    │ └─ for each task:                    │
    │    ├─ scheduleTaskReminder()         │
    │    │  └─ SET alarm per reminder      │
    │    │     requestCode = id*1000 + i   │
    │    └─ if (isPinned):                 │
    │       └─ scheduleStrictAlarm()       │
    │          requestCode = id            │
    └────────┬─────────────────────────────┘
             ↓
    ┌──────────────────────────────────────┐
    │ AlarmManager.setExactAndAllowWhileIdle│
    │ (time = remindTime, pendingIntent)   │
    └────────┬─────────────────────────────┘
             ↓
    ✅ TASK CREATED & ALARM SET


2️⃣  TIME REACHES → ALARM TRIGGERS
    ┌──────────────────────────────────────┐
    │ System AlarmManager                  │
    │ ⏰ reminderTime reached               │
    └────────┬─────────────────────────────┘
             ↓
    ┌──────────────────────────────────────┐
    │ 🎙️  Broadcast Intent                 │
    │ Intent(AlarmReceiver)                │
    │ + ACTION                             │
    │ + TASK_ID, TASK_TITLE, ...          │
    └────────┬─────────────────────────────┘
             ↓
    ┌──────────────────────────────────────┐
    │ AlarmReceiver.onReceive()            │
    │ └─ Check action:                     │
    │    ├─ ACTION_SHOW_NOTIFICATION       │
    │    └─ ACTION_START_ALARM             │
    └────────┬─────────────────────────────┘
             ↓
    ┌─────────────────────────────┬────────────────────┐
    │                             │                    │
    ↓                             ↓                    ↓
┌──────────────────────┐ ┌──────────────────┐ ┌──────────────┐
│ Regular Notification │ │ FullScreen Intent│ │ AlarmActivity│
│ (Dismiss-able)       │ │ Notification     │ │ (Locked)     │
│                      │ │ (High Priority)  │ │              │
│ NotificationHelper   │ │                  │ │ - Full UI    │
│ .showNotification()  │ │ NotificationHelper│ │ - Wake Up    │
│                      │ │ .showAlarmNotif..│ │ - Vibrate    │
└──────────────────────┘ └──────────────────┘ └──────────────┘
    ↓                             ↓                    ↓
┌──────────────────────┐ ┌──────────────────┐ ┌──────────────┐
│ User taps notify     │ │ User taps notify │ │ User taps:   │
│ → Open MainActivity  │ │ → Open AlarmAct  │ │ - Complete   │
└──────────────────────┘ └──────────────────┘ │ - Dismiss    │
                                               └──────────────┘


3️⃣  USER MARKS TASK COMPLETE
    ┌──────────────────────────────┐
    │ TaskAdapter                  │
    │ User taps Checkbox ✓         │
    └────────┬─────────────────────┘
             ↓
    ┌──────────────────────────────┐
    │ MainActivity.onTaskComplete()│
    │ ├─ taskAdapter.notifyItemChange()
    │ └─ reminderService.         │
    │    cancelTaskReminder(id)    │
    └────────┬─────────────────────┘
             ↓
    ┌──────────────────────────────┐
    │ ReminderService.             │
    │ cancelTaskReminder(taskId)   │
    │ ├─ for i=0..9:              │
    │ │  └─ Cancel alarm[i*1000+i]│
    │ └─ Cancel alarm[id]         │
    │    (strict alarm)            │
    └────────┬─────────────────────┘
             ↓
    ┌──────────────────────────────┐
    │ AlarmManager.cancel()        │
    │ PendingIntent.cancel()       │
    └────────┬─────────────────────┘
             ↓
    ✅ ALARMS CANCELLED


4️⃣  USER EDITS TASK (Change date/reminder)
    ┌──────────────────────────────┐
    │ MainActivity                 │
    │ Edit Task → TaskDetailBS     │
    └────────┬─────────────────────┘
             ↓
    ┌──────────────────────────────┐
    │ User changes:                │
    │ - Due Date                   │
    │ - Reminders                  │
    │ - Pinned status              │
    └────────┬─────────────────────┘
             ↓
    ┌──────────────────────────────┐
    │ DatabaseHelper.updateTask()  │
    │ ├─ UPDATE tasks SET ...      │
    │ └─ taskAdapter.notifyChange()
    └────────┬─────────────────────┘
             ↓
    ┌──────────────────────────────┐
    │ ReminderService.             │
    │ scheduleAllReminders()       │
    │ (Reschedule with new time)   │
    └────────┬─────────────────────┘
             ↓
    ✅ NEW ALARMS SET


5️⃣  USER DELETES TASK
    ┌──────────────────────────────┐
    │ TaskAdapter                  │
    │ User swipes delete           │
    └────────┬─────────────────────┘
             ↓
    ┌──────────────────────────────┐
    │ DatabaseHelper.deleteTask()  │
    │ ├─ DELETE FROM tasks         │
    │ └─ taskAdapter.notifyRemove()
    └────────┬─────────────────────┘
             ↓
    ┌──────────────────────────────┐
    │ ReminderService.             │
    │ cancelTaskReminder(id)       │
    │ (Same as complete)           │
    └────────┬─────────────────────┘
             ↓
    ✅ ALARMS CANCELLED, TASK DELETED
```

---

## 🎯 Request Code Mapping Strategy

```
REQUEST CODE = Unique identifier per PendingIntent

┌─────────────────────────────────────────────────┐
│ NOTIFICATION ALARMS (Dismissible)              │
├─────────────────────────────────────────────────┤
│                                                 │
│ taskId = 1                                     │
│ ├─ Reminder 0 ("5 phút")   → code = 1000 + 0 = 1000
│ ├─ Reminder 1 ("30 phút")  → code = 1000 + 1 = 1001
│ ├─ Reminder 2 ("1 ngày")   → code = 1000 + 2 = 1002
│ └─ ...
│
│ taskId = 2
│ ├─ Reminder 0 → code = 2000 + 0 = 2000
│ ├─ Reminder 1 → code = 2000 + 1 = 2001
│ └─ ...
│
│ Range: [taskId * 1000] to [taskId * 1000 + 9]
│
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│ STRICT ALARMS (Fullscreen, only if pinned)     │
├─────────────────────────────────────────────────┤
│                                                 │
│ taskId = 1 (if pinned=true)   → code = 1      │
│ taskId = 2 (if pinned=true)   → code = 2      │
│ taskId = 3 (if pinned=true)   → code = 3      │
│ ...                                            │
│                                                 │
│ Range: [taskId]                                │
│                                                 │
└─────────────────────────────────────────────────┘

⚠️  NO CONFLICT because:
   - Notification: 1000-1009, 2000-2009, 3000-3009, ...
   - Strict:      1, 2, 3, 4, 5, ...
```

---

## 📲 Intent Flow Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                 INTENT FLOW IN SERVICE                       │
└──────────────────────────────────────────────────────────────┘

1️⃣  FROM ReminderService → to AlarmReceiver
    ┌─────────────────────────────────┐
    │ Intent (For Notification)       │
    ├─────────────────────────────────┤
    │ - Component: AlarmReceiver      │
    │ - Action: SHOW_NOTIFICATION     │
    │ - Extra:  TASK_ID               │
    │ - Extra:  TASK_TITLE            │
    │ - Extra:  IS_ON_TIME            │
    │ - Extra:  TASK_DUE_DATE         │
    │                                 │
    │ Delivered via: AlarmManager     │
    │ Trigger Time: [calculated time] │
    │ RequestCode:  id*1000 + i       │
    └────────┬────────────────────────┘
             ↓
    ┌─────────────────────────────────┐
    │ Intent (For Strict Alarm)       │
    ├─────────────────────────────────┤
    │ - Component: AlarmReceiver      │
    │ - Action: START_ALARM ⭐        │
    │ - Extra:  TASK_ID               │
    │ - Extra:  TASK_TITLE            │
    │                                 │
    │ Delivered via: AlarmManager     │
    │ Trigger Time: [due date]        │
    │ RequestCode:  id                │
    └────────┬────────────────────────┘
             ↓
    ┌─────────────────────────────────┐
    │ AlarmReceiver.onReceive()       │
    └─────────────────────────────────┘


2️⃣  FROM AlarmReceiver → to Activities/Notifications
    
    Path A: SHOW_NOTIFICATION
    ┌─────────────────────────────────────┐
    │ NotificationHelper.showNotification()│
    │ ├─ Create Notification              │
    │ ├─ PendingIntent → MainActivity     │
    │ │  (when user taps notification)   │
    │ └─ manager.notify(taskId, noti)    │
    └────────┬────────────────────────────┘
             ↓
    ┌─────────────────────────────────────┐
    │ 📱 Notification appears             │
    │ User can dismiss or tap             │
    └─────────────────────────────────────┘


    Path B: START_ALARM
    ┌─────────────────────────────────────┐
    │ Try: startActivity(AlarmActivity)   │
    │ Catch: (if failed)                  │
    └────────┬────────────────────────────┘
             ↓
    ┌─────────────────────────────────────┐
    │ NotificationHelper.                 │
    │ showAlarmNotification()              │
    │ ├─ Create FullScreenIntent Notif    │
    │ ├─ HIGH priority                    │
    │ └─ manager.notify()                 │
    └────────┬────────────────────────────┘
             ↓
    ┌─────────────────────────────────────┐
    │ 🚨 FullScreen Alarm appears         │
    │ (Wakes screen, high priority)       │
    └─────────────────────────────────────┘
```

---

## 🏗️ Class Dependency Graph

```
┌─────────────────────────────────────────────────────────────────┐
│                    DEPENDENCY GRAPH                              │
└─────────────────────────────────────────────────────────────────┘

MainActivity
    ├─ dependsOn: ReminderService
    ├─ dependsOn: TaskDatabaseHelper
    ├─ dependsOn: TaskAdapter
    ├─ dependsOn: TaskModel
    ├─ dependsOn: TaskDetailBottomSheet (Dialog)
    ├─ dependsOn: DatePickerBottomSheet (Dialog)
    ├─ dependsOn: ReminderDialogFragment (Dialog)
    └─ dependsOn: NotificationHelper (Utils)

ReminderService
    ├─ dependsOn: TaskDatabaseHelper
    │   ├─ uses: getUpcomingTasks()
    │   ├─ uses: updateTask()
    │   └─ uses: deleteTask()
    ├─ dependsOn: TaskModel
    ├─ dependsOn: NotificationHelper
    ├─ uses: AlarmManager (Android System)
    └─ communicates via: BroadcastReceiver → AlarmReceiver

AlarmReceiver
    ├─ dependsOn: NotificationHelper
    ├─ dependsOn: AlarmActivity
    └─ receives from: AlarmManager

TaskDatabaseHelper
    ├─ depends on: TaskModel
    └─ manages: SQLite Database

NotificationHelper
    ├─ creates: Notification channels
    ├─ depends on: MainActivity (for PendingIntent)
    ├─ depends on: AlarmActivity (for FullScreen)
    └─ uses: NotificationManager (Android System)

TaskAdapter
    ├─ depends on: TaskModel
    ├─ depends on: TaskListItem
    ├─ depends on: TaskHeader
    └─ depends on: TaskSwipeHelper (Utils)
```

---

## 📊 State Transition Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│              TASK STATE TRANSITIONS                              │
└─────────────────────────────────────────────────────────────────┘

                    ┌──────────────┐
                    │  NOT CREATED │
                    └──────┬───────┘
                           │ User creates task
                           ↓
                    ┌──────────────────────┐
                    │ CREATED (Not Due)    │
                    │                      │
                    │ ├─ Reminders Set ✓   │
                    │ ├─ Alarms Scheduled ✓│
                    │ └─ Awaiting reminder │
                    └──────┬───────────────┘
                    ↙              ↘
                   ↙                ↘
        Time Passes               User Changes
                ↓                        ↓
        ┌───────────────────┐  ┌─────────────────┐
        │ REMINDER FIRED    │  │ EDITED          │
        │                   │  │ New alarm set   │
        │ ├─ Notification   │  │                 │
        │ │   shown         │  └─────────────────┘
        │ │                 │          ↓
        │ └─ or AlarmActivity│  [Same as CREATED]
        │                   │
        └───────┬───────────┘
                │
                │ User taps Complete
                │ or Timer completes
                ↓
        ┌───────────────────┐
        │ COMPLETED         │
        │                   │
        │ ├─ Marked done ✓  │
        │ ├─ Alarms canceled│
        │ └─ Hides from UI  │
        └───────────────────┘
```

---

## 🔐 Permission Requirements Flow

```
┌──────────────────────────────────────────────────────────────┐
│           PERMISSION ACQUISITION FLOW                        │
└──────────────────────────────────────────────────────────────┘

App Starts
    ↓
MainActivity.onCreate()
    ├─ requestPermissions() called
    ↓
Permission Checker
    ├─ if SDK >= 13 (Tiramisu):
    │  ├─ POST_NOTIFICATIONS ← Android 13+
    │  └─ (Show notification on lockscreen)
    │
    ├─ SCHEDULE_EXACT_ALARM ← For precise alarm
    │  └─ AlarmManager.canScheduleExactAlarms()
    │
    ├─ USE_FULL_SCREEN_INTENT ← For full-screen intent
    │  └─ High-priority notifications
    │
    ├─ WAKE_LOCK ← Keep device awake during alarm
    │
    ├─ FOREGROUND_SERVICE ← Service with notification
    │
    └─ FOREGROUND_SERVICE_SPECIAL_USE ← For special use
       └─ Reserved for specific purposes
    ↓
ActivityResultLauncher
    ├─ Dialog shows to user
    ├─ User grants/denies
    ↓
onPermissionResult()
    ├─ if (all granted):
    │  └─ startForegroundService()
    │
    └─ if (denied):
       ├─ Show toast/dialog
       └─ Limit functionality
```


