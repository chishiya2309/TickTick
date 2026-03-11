package hcmute.edu.vn.lequanghung_23110110.ticktick.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
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
        if (intent != null && intent.hasExtra("TASK_ID")) {
            int taskId = intent.getIntExtra("TASK_ID", -1);
            String taskTitle = intent.getStringExtra("TASK_TITLE");
            Log.d(TAG, "Nhận báo thức cho task: " + taskTitle);
            NotificationHelper.showNotification(this, taskId, taskTitle);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Lập lịch báo thức cho tất cả các task chưa hoàn thành
     */
    public void scheduleAllReminders() {
        List<TaskModel> upcomingTasks = dbHelper.getUpcomingTasks();
        for (TaskModel task : upcomingTasks) {
            scheduleTaskReminder(task);
        }
    }

    public void scheduleTaskReminder(TaskModel task) {
        if (task.getDueDateMillis() <= 0 || task.isCompleted()) return;

        long reminderTime = task.getDueDateMillis() - (15 * 60 * 1000); // 15 phút trước
        if (reminderTime < System.currentTimeMillis()) return; // Bỏ qua nếu đã qua thời điểm nhắc

        Intent intent = new Intent(this, ReminderService.class);
        intent.putExtra("TASK_ID", task.getId());
        intent.putExtra("TASK_TITLE", task.getTitle());

        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            Log.d(TAG, "Đã đặt nhắc nhở cho: " + task.getTitle() + " lúc " + reminderTime);
        }
    }

    public void cancelTaskReminder(int taskId) {
        Intent intent = new Intent(this, ReminderService.class);
        PendingIntent pendingIntent = PendingIntent.getService(
                this, taskId, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}
