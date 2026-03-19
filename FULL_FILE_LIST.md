# 📂 TickTick Project - Toàn Bộ Danh Sách File

## 📍 Cấu Trúc Dự Án

```
D:\Workspace\MOP_Project\TickTick/
├── 📄 build.gradle.kts                (Build config chính)
├── 📄 settings.gradle.kts             (Settings gradle)
├── 📄 gradle.properties               (Gradle properties)
├── 📄 local.properties                (Local build properties)
├── 📄 gradlew / gradlew.bat          (Gradle wrapper)
│
├── 📁 gradle/
│   ├── gradle-daemon-jvm.properties
│   ├── libs.versions.toml            (Dependency versions)
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── 📁 app/                           (App module chính)
│   ├── 📄 build.gradle.kts           (App build config)
│   ├── 📄 proguard-rules.pro
│   │
│   ├── 📁 src/main/
│   │   ├── 📄 AndroidManifest.xml   (★ Khai báo components)
│   │   │
│   │   ├── 📁 java/hcmute/edu/vn/lequanghung_23110110/ticktick/
│   │   │   │
│   │   │   ├── 📁 activity/          (★ Activities - UI Screens)
│   │   │   │   ├── MainActivity.java          (★★★ Chính - Hiển thị task list)
│   │   │   │   ├── AlarmActivity.java        (★★ Fullscreen alarm)
│   │   │   │   ├── SearchActivity.java       (Search screen)
│   │   │   │   └── SplashActivity.java       (Splash screen)
│   │   │   │
│   │   │   ├── 📁 service/          (★★★ Background Services)
│   │   │   │   ├── ReminderService.java      (★★★ CHÍNH - Schedule alarms)
│   │   │   │   ├── AlarmReceiver.java        (★★ Nhận alarm trigger)
│   │   │   │   └── ServiceNhac.java         (Dịch vụ phụ)
│   │   │   │
│   │   │   ├── 📁 database/         (★ Database layer)
│   │   │   │   └── TaskDatabaseHelper.java   (★★★ SQLite DB manager)
│   │   │   │
│   │   │   ├── 📁 model/            (★ Data models)
│   │   │   │   ├── TaskModel.java            (★★★ Model chính)
│   │   │   │   ├── ListModel.java           (Task list model)
│   │   │   │   ├── TaskListItem.java        (Interface)
│   │   │   │   ├── TaskHeader.java          (Header UI model)
│   │   │   │   ├── DrawerMenuItem.java      (Menu item)
│   │   │   │   ├── EmojiResponse.java       (API response)
│   │   │   │   └── EmojiHubResponse.java    (API response)
│   │   │   │
│   │   │   ├── 📁 utils/            (★ Utility classes)
│   │   │   │   ├── NotificationHelper.java   (★★★ Notification manager)
│   │   │   │   ├── DailyBriefingScheduler.java (★ Daily schedule)
│   │   │   │   ├── TaskSwipeHelper.java     (Swipe actions)
│   │   │   │   └── OnTimeSelectedListener.java (Listener)
│   │   │   │
│   │   │   ├── 📁 dialog/           (UI Dialogs)
│   │   │   │   ├── TaskDetailBottomSheet.java     (Edit task)
│   │   │   │   ├── DatePickerBottomSheet.java    (Pick date)
│   │   │   │   ├── ReminderDialogFragment.java   (Set reminder)
│   │   │   │   ├── SelectIconBottomSheet.java    (Pick emoji)
│   │   │   │   ├── AddListDialogFragment.java    (New list)
│   │   │   │   └── MoveTaskBottomSheet.java      (Move task)
│   │   │   │
│   │   │   ├── 📁 adapter/          (RecyclerView Adapters)
│   │   │   │   ├── TaskAdapter.java              (Main task list)
│   │   │   │   ├── SearchTaskAdapter.java        (Search results - task)
│   │   │   │   ├── SearchListAdapter.java        (Search results - list)
│   │   │   │   ├── PinnedListAdapter.java        (Pinned list)
│   │   │   │   ├── MoveTaskListAdapter.java      (Move task list)
│   │   │   │   ├── DrawerMenuAdapter.java        (Navigation drawer)
│   │   │   │   ├── EmojiAdapter.java             (Emoji picker)
│   │   │   │   └── CalendarAdapter.java          (Calendar view)
│   │   │   │
│   │   │   ├── 📁 repository/       (Data repositories)
│   │   │   │   └── EmojiRepository.java         (Emoji data source)
│   │   │   │
│   │   │   ├── 📁 api/              (API Clients)
│   │   │   │   └── EmojiHubApi.java            (EmojiHub API call)
│   │   │   │
│   │   │   └── 📁 worker/           (Background workers)
│   │   │       └── DailyBriefingWorker.java    (WorkManager task)
│   │   │
│   │   ├── 📁 res/                  (Resources)
│   │   │   ├── 📁 layout/           (XML layouts)
│   │   │   ├── 📁 drawable/         (Icons & images)
│   │   │   ├── 📁 values/           (Colors, strings, etc)
│   │   │   ├── 📁 navigation/       (Navigation graphs)
│   │   │   └── ...
│   │   │
│   │   ├── 📁 androidTest/          (Android instrumented tests)
│   │   └── 📁 test/                 (Unit tests)
│   │
│   └── 📁 build/                    (Build outputs)
│       ├── 📁 generated/            (Generated files)
│       ├── 📁 intermediates/         (Intermediate build files)
│       ├── 📁 outputs/
│       │   └── 📁 logs/
│       └── 📁 reports/
│
└── 📁 build/                        (Root build files)
    └── 📁 reports/
        └── 📁 problems/
```

---

## 📋 Danh Sách File Chi Tiết

### 🔴 **SERVICE & BROADCAST LAYER** (Backend - Tâm)

| File | Dòng | Mục Đích | Importance |
|------|------|---------|-----------|
| `ReminderService.java` | 282 | Schedule & manage alarms, Foreground service | ⭐⭐⭐ |
| `AlarmReceiver.java` | 40 | Receive broadcast when alarm triggers | ⭐⭐⭐ |
| `ServiceNhac.java` | ? | Auxiliary service (unused?) | ⭐ |

---

### 🟢 **ACTIVITY LAYER** (Frontend - Giao Diện)

| File | Mục Đích | Importance |
|------|---------|-----------|
| `MainActivity.java` | Main screen, task list, bind service | ⭐⭐⭐ |
| `AlarmActivity.java` | Fullscreen alarm display | ⭐⭐ |
| `SearchActivity.java` | Search tasks/lists | ⭐⭐ |
| `SplashActivity.java` | Splash screen on app start | ⭐ |

---

### 🔵 **DATABASE LAYER**

| File | Mục Đích | Importance |
|------|---------|-----------|
| `TaskDatabaseHelper.java` | SQLite DB operations (CRUD) | ⭐⭐⭐ |

---

### 🟡 **MODEL LAYER** (Data Structures)

| File | Mục Đích | Importance |
|------|---------|-----------|
| `TaskModel.java` | Main task object | ⭐⭐⭐ |
| `ListModel.java` | Task list object | ⭐⭐ |
| `TaskListItem.java` | Interface for list items | ⭐⭐ |
| `TaskHeader.java` | Header UI model | ⭐⭐ |
| `DrawerMenuItem.java` | Navigation drawer items | ⭐ |
| `EmojiResponse.java` | API response model | ⭐ |
| `EmojiHubResponse.java` | API response model | ⭐ |

---

### 🟠 **UTILS LAYER** (Helper Classes)

| File | Mục Đích | Importance |
|------|---------|-----------|
| `NotificationHelper.java` | Create & show notifications/alarms | ⭐⭐⭐ |
| `DailyBriefingScheduler.java` | Schedule daily briefing | ⭐⭐ |
| `TaskSwipeHelper.java` | Swipe actions on tasks | ⭐⭐ |
| `OnTimeSelectedListener.java` | Time picker listener | ⭐ |

---

### 🟣 **ADAPTER LAYER** (RecyclerView Adapters)

| File | Mục Đích | Importance |
|------|---------|-----------|
| `TaskAdapter.java` | Main task list display | ⭐⭐⭐ |
| `SearchTaskAdapter.java` | Search task results | ⭐⭐ |
| `SearchListAdapter.java` | Search list results | ⭐⭐ |
| `PinnedListAdapter.java` | Pinned lists display | ⭐⭐ |
| `MoveTaskListAdapter.java` | Move task destination lists | ⭐ |
| `DrawerMenuAdapter.java` | Navigation drawer menu | ⭐⭐ |
| `EmojiAdapter.java` | Emoji picker list | ⭐ |
| `CalendarAdapter.java` | Calendar view | ⭐ |

---

### ⚪ **DIALOG/UI FRAGMENTS**

| File | Mục Đích | Importance |
|------|---------|-----------|
| `TaskDetailBottomSheet.java` | Edit task details | ⭐⭐⭐ |
| `DatePickerBottomSheet.java` | Pick due date | ⭐⭐ |
| `ReminderDialogFragment.java` | Set reminder times | ⭐⭐ |
| `SelectIconBottomSheet.java` | Pick emoji icon | ⭐⭐ |
| `AddListDialogFragment.java` | Create new list | ⭐⭐ |
| `MoveTaskBottomSheet.java` | Move task to list | ⭐ |

---

### 🟤 **API & REPOSITORY**

| File | Mục Đích | Importance |
|------|---------|-----------|
| `EmojiHubApi.java` | EmojiHub API client | ⭐ |
| `EmojiRepository.java` | Emoji data source | ⭐ |

---

### ⬛ **BACKGROUND WORKER**

| File | Mục Đích | Importance |
|------|---------|-----------|
| `DailyBriefingWorker.java` | WorkManager daily task | ⭐⭐ |

---

### 🔲 **CONFIGURATION FILES**

| File | Mục Đích | Importance |
|------|---------|-----------|
| `AndroidManifest.xml` | App manifest, component declaration | ⭐⭐⭐ |
| `build.gradle.kts` | App build config, dependencies | ⭐⭐ |
| `gradle.properties` | Gradle properties | ⭐ |
| `local.properties` | Local SDK/NDK paths | ⭐ |

---

## 🎯 Service Flow - File Interaction Map

```
┌─────────────────────────────────────────────────────────────────┐
│                        USER OPENS APP                            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
         ┌──────────────────────────────────────────────┐
         │     SplashActivity.java (Splash screen)     │
         └──────────────────────────────────────────────┘
                              ↓
         ┌──────────────────────────────────────────────────┐
         │  MainActivity.java (Main task list screen)      │
         │  - onCreate() → bindService(ReminderService)   │
         │  - startService(ReminderService)               │
         │  - requestPermissions()                         │
         └──────────────────────────────────────────────────┘
                              ↓
         ┌──────────────────────────────────────────────────┐
         │    ReminderService.java                         │
         │    - onCreate() → createNotificationChannel()   │
         │    - onStartCommand() → startForeground()       │
         │    - scheduleAllReminders()                     │
         │      └─ Loop: taskList from DB                  │
         │         - scheduleTaskReminder()                │
         │         - scheduleStrictAlarm() if pinned       │
         └──────────────────────────────────────────────────┘
                              ↓
         ┌──────────────────────────────────────────────────┐
         │  TaskDatabaseHelper.java                         │
         │  - getUpcomingTasks() [SELECT WHERE...]         │
         │  - updateTask() → trigger reschedule            │
         │  - deleteTask() → trigger cancel alarm          │
         └──────────────────────────────────────────────────┘
                              ↓
         ┌──────────────────────────────────────────────────┐
         │  NotificationHelper.java                         │
         │  - createNotificationChannel()                  │
         │  - showNotification()                           │
         │  - showAlarmNotification() (FullScreen)         │
         └──────────────────────────────────────────────────┘
                              ↓
         ┌──────────────────────────────────────────────────┐
         │  AlarmManager (System service)                   │
         │  - setExactAndAllowWhileIdle(time, intent)      │
         │  ⏰ Waits for trigger time...                   │
         └──────────────────────────────────────────────────┘
                              ↓
         ┌──────────────────────────────────────────────────┐
         │  🔔 ALARM TRIGGERS AT REMINDER TIME 🔔          │
         └──────────────────────────────────────────────────┘
                              ↓
         ┌──────────────────────────────────────────────────┐
         │  AlarmReceiver.java (BroadcastReceiver)         │
         │  - onReceive() → Check action                   │
         │    - ACTION_SHOW_NOTIFICATION                   │
         │    - ACTION_START_ALARM                         │
         └──────────────────────────────────────────────────┘
                              ↓
    ┌────────────────────────┴────────────────────────┐
    ↓                                                  ↓
┌─────────────────────────────────┐   ┌──────────────────────────────┐
│  Notification (Regular)         │   │  FullScreen Intent (Alarm)   │
│  - showNotification()           │   │  - showAlarmNotification()   │
│  - User can dismiss             │   │  - Wakes screen             │
│  - Taps → MainActivity          │   │  - High priority            │
└─────────────────────────────────┘   └──────────────────────────────┘
                                              ↓
                                    ┌──────────────────────────────┐
                                    │  AlarmActivity.java         │
                                    │  - Fullscreen alarm UI      │
                                    │  - User taps Complete       │
                                    │  - Marks task as done       │
                                    └──────────────────────────────┘
                                              ↓
                                    ┌──────────────────────────────┐
                                    │ Main Activity (Refresh)      │
                                    │ - Update task UI             │
                                    │ - Re-schedule reminders      │
                                    └──────────────────────────────┘
```

---

## 📞 Key Method Calls & Dependencies

### **When User Creates Task:**
```
MainActivity → TaskDetailBottomSheet
       ↓
dbHelper.insertTask(task)
       ↓
MainActivity.taskAdapter.notifyDataSetChanged()
       ↓
reminderService.scheduleAllReminders()
       ↓
AlarmManager.setExactAndAllowWhileIdle()
```

### **When Task Due Date Arrives:**
```
AlarmManager (System)
       ↓
AlarmReceiver.onReceive()
       ↓
NotificationHelper.showNotification() / showAlarmNotification()
       ↓
User sees Notification / AlarmActivity
       ↓
AlarmActivity / MainActivity
```

### **When User Completes Task:**
```
TaskAdapter.onTaskComplete()
       ↓
dbHelper.updateTask(completed=true)
       ↓
MainActivity.taskAdapter.notifyDataSetChanged()
       ↓
reminderService.cancelTaskReminder()
       ↓
AlarmManager.cancel()
```

---

## 🔒 Permissions Defined in AndroidManifest.xml

```
✓ INTERNET
✓ POST_NOTIFICATIONS (Android 13+)
✓ WAKE_LOCK
✓ RECEIVE_BOOT_COMPLETED
✓ SCHEDULE_EXACT_ALARM
✓ USE_FULL_SCREEN_INTENT
✓ VIBRATE
✓ DISABLE_KEYGUARD
✓ FOREGROUND_SERVICE
✓ FOREGROUND_SERVICE_SPECIAL_USE
✓ SYSTEM_ALERT_WINDOW
```

---

## 📊 Build Dependencies (in build.gradle.kts)

```
- AndroidX (appcompat, constraintlayout, etc)
- Material Design 3
- Retrofit (API calls)
- Gson (JSON parsing)
- WorkManager (background tasks)
- Navigation Components
- LiveData & ViewModel
- RecyclerView
- FloatingActionButton
- BottomSheet
- DrawerLayout
- ... (others in gradle config)
```


