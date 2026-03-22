package hcmute.edu.vn.lequanghung_23110110.ticktick.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.ServiceCompat;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.NotificationHelper;

import java.util.Calendar;
import java.util.List;

public class ReminderService extends Service {
    private static final String TAG = "ALARM_DEBUG";
    private final IBinder binder = new LocalBinder();
    private TaskDatabaseHelper dbHelper;

    public class LocalBinder extends Binder {
        public ReminderService getService() {
            return ReminderService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = TaskDatabaseHelper.getInstance(this);
        NotificationHelper.createNotificationChannel(this);
        Log.d(TAG, "ReminderService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ReminderService onStartCommand");

        // Start Foreground Service
        android.app.Notification notification = NotificationHelper.createForegroundNotification(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(this, 1, notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, notification);
        }

        scheduleAllReminders();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "ReminderService onBind");
        return binder;
    }

    public void scheduleAllReminders() {
        Log.d(TAG, "--- Bắt đầu scheduleAllReminders ---");
        List<TaskModel> upcomingTasks = dbHelper.getUpcomingTasks();
        Log.d(TAG, "Tìm thấy " + upcomingTasks.size() + " công việc sắp tới");

        for (TaskModel task : upcomingTasks) {
            Log.d(TAG, "Đang xử lý Task: " + task.getTitle() + " (ID: " + task.getId() + ", Pinned: " + task.isPinned()
                    + ")");

            // 1. Thông báo bình thường
            scheduleTaskReminder(task);

            // 2. Báo thức toàn màn hình (Strict Alarm)
            // Fix: Nếu bạn muốn mọi task đều báo thức, hãy bỏ điều kiện isPinned()
            // Hoặc ít nhất log ra để biết tại sao bị bỏ qua
            if (task.isPinned()) {
                Log.d(TAG, "Task được ghim -> Tiến hành gọi scheduleStrictAlarm");
                scheduleStrictAlarm(this, task.getId(), task.getTitle(), task.getDueDateMillis());
            } else {
                Log.d(TAG, "Task KHÔNG được ghim -> Bỏ qua scheduleStrictAlarm (Chỉ hiện thông báo)");
            }
        }
        Log.d(TAG, "--- Kết thúc scheduleAllReminders ---");
    }

    public void scheduleTaskReminder(TaskModel task) {
        if (task.getDueDateMillis() <= 0) {
            Log.d(TAG, "Task " + task.getId() + " không có ngày hạn, bỏ qua Notification");
            return;
        }
        if (task.isCompleted()) {
            Log.d(TAG, "Task " + task.getId() + " đã hoàn thành, bỏ qua Notification");
            return;
        }

        List<String> reminders = task.getReminders();
        if (reminders == null || reminders.isEmpty()) {
            Log.d(TAG, "Task " + task.getId() + " không có mục nhắc nhở nào, bỏ qua Notification");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;

        for (int i = 0; i < reminders.size(); i++) {
            String reminderStr = reminders.get(i);
            long reminderTime = calculateReminderTime(task.getDueDateMillis(), reminderStr);

            if (reminderTime < System.currentTimeMillis()) {
                Log.d(TAG, "Thời gian " + reminderTime + " của nhắc nhở '" + reminderStr + "' đã qua so với hiện tại "
                        + System.currentTimeMillis() + ", bỏ qua");
                continue;
            }

            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.setAction(AlarmReceiver.ACTION_SHOW_NOTIFICATION);
            intent.putExtra("TASK_ID", task.getId());
            intent.putExtra("TASK_TITLE", task.getTitle());
            boolean isOnTime = "Đúng giờ".equals(reminderStr) || "on_time".equals(reminderStr);
            intent.putExtra("IS_ON_TIME", isOnTime);
            intent.putExtra("TASK_DUE_DATE", task.getDueDateMillis());

            // Unique Request ID per task and per reminder offset
            int requestCode = task.getId() * 1000 + i;

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            setSafeExactAlarm(alarmManager, reminderTime, pendingIntent);
            Log.d(TAG, "Đã set Notification thành công cho: " + task.getTitle() + " lúc " + reminderTime + " (Offset: "
                    + reminderStr + ")");
        }
    }

    private long calculateReminderTime(long dueDateMillis, String reminderStr) {
        if (reminderStr == null)
            return dueDateMillis;
            
        if (reminderStr.startsWith("custom:")) {
            String[] parts = reminderStr.split(":", 3);
            if (parts.length >= 3) {
                String label = parts[1];
                String timeStr = parts[2];

                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(dueDateMillis);

                // Lấy offset số từ chuỗi label (VD: "Sớm 1 ngày" -> 1)
                int offset = 0;
                try {
                    String numStr = label.replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) {
                        offset = Integer.parseInt(numStr);
                    }
                } catch(Exception e) {}

                if (label.contains("tuần") || label.contains("week")) {
                    cal.add(Calendar.WEEK_OF_YEAR, -offset);
                } else {
                    cal.add(Calendar.DAY_OF_MONTH, -offset);
                }

                // Parse time HH:mm
                String[] timeParts = timeStr.split(":");
                if (timeParts.length == 2) {
                    try {
                        int h = Integer.parseInt(timeParts[0]);
                        int m = Integer.parseInt(timeParts[1]);
                        cal.set(Calendar.HOUR_OF_DAY, h);
                        cal.set(Calendar.MINUTE, m);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                    } catch (Exception e) {}
                }

                return cal.getTimeInMillis();
            }
        }
            
        switch (reminderStr) {
            case "Đúng giờ":
            case "on_time":
                return dueDateMillis;
            case "5 phút trước":
            case "5_mins":
                return dueDateMillis - 5L * 60 * 1000;
            case "30 phút trước":
            case "30_mins":
                return dueDateMillis - 30L * 60 * 1000;
            case "1 giờ trước":
            case "1_hour":
                return dueDateMillis - 60L * 60 * 1000;
            case "1 ngày trước":
            case "1_day":
                return dueDateMillis - 24L * 60 * 60 * 1000;
            default:
                return dueDateMillis; // Fallback
        }
    }

    public static void scheduleStrictAlarm(Context context, int taskId, String title, long timeInMillis) {
        Log.d(TAG, "Gọi scheduleStrictAlarm cho Task ID: " + taskId + ", Title: " + title);

        if (timeInMillis < System.currentTimeMillis()) {
            Log.d(TAG, "LỖI: Thời gian báo thức (" + timeInMillis + ") đã qua so với hiện tại ("
                    + System.currentTimeMillis() + ")");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_START_ALARM);
        intent.putExtra("TASK_ID", taskId);
        intent.putExtra("TASK_TITLE", title);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            setSafeExactAlarm(alarmManager, timeInMillis, pendingIntent);
            Log.d(TAG, "=> Đã đặt AlarmManager (Strict Alarm) thành công cho: " + title + " vào lúc: " + timeInMillis);
        } else {
            Log.e(TAG, "LỖI: AlarmManager is NULL");
        }
    }

    private static void setSafeExactAlarm(AlarmManager alarmManager, long timeInMillis, PendingIntent pendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Không thể đặt báo thức chính xác: " + e.getMessage());
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }

    public void cancelTaskReminder(int taskId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Log.d(TAG, "Hủy báo thức cho Task ID: " + taskId);

        // Hủy 10 intent tiềm năng cho từng task cho an toàn
        for (int i = 0; i < 10; i++) {
            int requestCode = taskId * 1000 + i;
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.setAction(AlarmReceiver.ACTION_SHOW_NOTIFICATION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, requestCode, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }

        Intent strictIntent = new Intent(this, AlarmReceiver.class);
        strictIntent.setAction(AlarmReceiver.ACTION_START_ALARM);
        PendingIntent strictPendingIntent = PendingIntent.getBroadcast(
                this, taskId, strictIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (strictPendingIntent != null) {
            alarmManager.cancel(strictPendingIntent);
            strictPendingIntent.cancel();
        }
    }

    /** Hủy tất cả alarm/notification (dùng khi logout) */
    public static void cancelAllReminders(Context context) {
        Log.d(TAG, "=== cancelAllReminders: Bắt đầu dọn dẹp ===");

        // 1. Cancel tất cả notification đang hiển thị
        android.app.NotificationManager nm =
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancelAll();
            Log.d(TAG, "Đã xóa tất cả notification");
        }

        // 2. Cancel tất cả alarm cho từng task
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            TaskDatabaseHelper dbHelper = TaskDatabaseHelper.getInstance(context);
            List<Integer> allTaskIds = dbHelper.getAllTaskIds();

            for (int taskId : allTaskIds) {
                // Cancel notification alarms (requestCode = taskId * 1000 + i)
                for (int i = 0; i < 10; i++) {
                    int requestCode = taskId * 1000 + i;
                    Intent intent = new Intent(context, AlarmReceiver.class);
                    intent.setAction(AlarmReceiver.ACTION_SHOW_NOTIFICATION);
                    PendingIntent pi = PendingIntent.getBroadcast(
                            context, requestCode, intent,
                            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
                    if (pi != null) {
                        alarmManager.cancel(pi);
                        pi.cancel();
                    }
                }

                // Cancel strict alarms (requestCode = taskId)
                Intent strictIntent = new Intent(context, AlarmReceiver.class);
                strictIntent.setAction(AlarmReceiver.ACTION_START_ALARM);
                PendingIntent strictPi = PendingIntent.getBroadcast(
                        context, taskId, strictIntent,
                        PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
                if (strictPi != null) {
                    alarmManager.cancel(strictPi);
                    strictPi.cancel();
                }
            }
            Log.d(TAG, "Đã hủy alarm cho " + allTaskIds.size() + " tasks");
        }

        // 3. Cancel DailyBriefing WorkManager job
        androidx.work.WorkManager.getInstance(context).cancelUniqueWork("daily_briefing_work");
        Log.d(TAG, "Đã hủy DailyBriefing worker");
        Log.d(TAG, "=== cancelAllReminders: Hoàn tất ===");
    }
}

