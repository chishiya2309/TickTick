package hcmute.edu.vn.lequanghung_23110110.ticktick.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.activity.MainActivity;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

/**
 * Worker thực hiện Daily Briefing (Báo cáo đầu ngày) lúc 7h sáng mỗi ngày.
 */
public class DailyBriefingWorker extends Worker {

    private static final String CHANNEL_ID = "daily_briefing_channel";
    private static final String CHANNEL_NAME = "Daily Briefing";
    private static final int NOTIFICATION_ID = 1001;

    public DailyBriefingWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        createNotificationChannel();
        List<TaskModel> tasks = getTasksForToday();
        showDailyBriefingNotification(tasks);
        return Result.success();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // Đặt quan trọng cao để hiện thông báo lên màn hình
            );
            channel.setDescription("Thông báo báo cáo đầu ngày về các nhiệm vụ");

            NotificationManager notificationManager = 
                    getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private List<TaskModel> getTasksForToday() {
        TaskDatabaseHelper dbHelper = TaskDatabaseHelper.getInstance(getApplicationContext());
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfToday = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_MONTH, 1);
        long endOfToday = cal.getTimeInMillis() - 1;

        return dbHelper.getTodayAndOverdueTasks(startOfToday, endOfToday);
    }

    private void showDailyBriefingNotification(List<TaskModel> tasks) {
        Context context = getApplicationContext();

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        if (tasks.isEmpty()) {
            inboxStyle.setBigContentTitle("Chúc mừng ngày mới! 🎉");
            inboxStyle.addLine("Bạn không có nhiệm vụ nào hôm nay.");
        } else {
            inboxStyle.setBigContentTitle("Báo cáo đầu ngày - " + tasks.size() + " nhiệm vụ");
            int count = Math.min(tasks.size(), 5);
            for (int i = 0; i < count; i++) {
                TaskModel task = tasks.get(i);
                String line = (i + 1) + ". " + task.getTitle();
                if (task.getDueDateMillis() > 0 && task.getDueDateMillis() < System.currentTimeMillis()) {
                    line = "⚠️ " + line + " (Quá hạn)";
                }
                inboxStyle.addLine(line);
            }
            if (tasks.size() > 5) {
                inboxStyle.addLine("... và " + (tasks.size() - 5) + " nhiệm vụ khác");
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_today)
                .setContentTitle("Báo cáo đầu ngày")
                .setContentText(tasks.isEmpty() ? 
                        "Không có nhiệm vụ hôm nay" : 
                        tasks.size() + " nhiệm vụ đang chờ bạn")
                .setStyle(inboxStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Đặt Priority High
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
