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
    private static final String TAG = "ReminderService";
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Thay vì xử lý logic ở đây, chúng ta dùng scheduleAllReminders qua Binder hoặc khi Service khởi chạy
        scheduleAllReminders();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void scheduleAllReminders() {
        List<TaskModel> upcomingTasks = dbHelper.getUpcomingTasks();
        for (TaskModel task : upcomingTasks) {
            scheduleTaskReminder(task);
            if (task.isPinned()) {
                scheduleStrictAlarm(this, task.getId(), task.getTitle(), task.getDueDateMillis());
            }
        }
    }

    public void scheduleTaskReminder(TaskModel task) {
        if (task.getDueDateMillis() <= 0 || task.isCompleted()) return;

        long reminderTime = task.getDueDateMillis() - (15 * 60 * 1000); // 15 phút trước
        if (reminderTime < System.currentTimeMillis()) return;

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            }
        }
    }

    public static void scheduleStrictAlarm(Context context, int taskId, String title, long timeInMillis) {
        if (timeInMillis < System.currentTimeMillis()) return;

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            }
        }
    }

    public void cancelTaskReminder(int taskId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        
        // Hủy thông báo thường
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_SHOW_NOTIFICATION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, taskId, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        // Hủy Strict Alarm
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
