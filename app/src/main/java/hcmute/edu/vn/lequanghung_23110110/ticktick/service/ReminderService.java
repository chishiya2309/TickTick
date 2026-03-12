package hcmute.edu.vn.lequanghung_23110110.ticktick.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.NotificationHelper;
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
            Log.d(TAG, "Đang xử lý Task: " + task.getTitle() + " (ID: " + task.getId() + ", Pinned: " + task.isPinned() + ")");
            
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

        long reminderTime = task.getDueDateMillis();
        if (reminderTime < System.currentTimeMillis()) {
            Log.d(TAG, "Thời gian " + reminderTime + " đã qua so với hiện tại " + System.currentTimeMillis() + ", bỏ qua Notification");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_SHOW_NOTIFICATION);
        intent.putExtra("TASK_ID", task.getId());
        intent.putExtra("TASK_TITLE", task.getTitle());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            setSafeExactAlarm(alarmManager, reminderTime, pendingIntent);
            Log.d(TAG, "Đã set Notification thành công cho: " + task.getTitle());
        }
    }

    public static void scheduleStrictAlarm(Context context, int taskId, String title, long timeInMillis) {
        Log.d(TAG, "Gọi scheduleStrictAlarm cho Task ID: " + taskId + ", Title: " + title);
        
        if (timeInMillis < System.currentTimeMillis()) {
            Log.d(TAG, "LỖI: Thời gian báo thức (" + timeInMillis + ") đã qua so với hiện tại (" + System.currentTimeMillis() + ")");
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
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

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
        
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_SHOW_NOTIFICATION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, taskId, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        Intent strictIntent = new Intent(this, AlarmReceiver.class);
        strictIntent.setAction(AlarmReceiver.ACTION_START_ALARM);
        PendingIntent strictPendingIntent = PendingIntent.getBroadcast(
                this, taskId, strictIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (strictPendingIntent != null) {
            alarmManager.cancel(strictPendingIntent);
            strictPendingIntent.cancel();
        }
    }
}
