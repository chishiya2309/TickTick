package hcmute.edu.vn.lequanghung_23110110.ticktick.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.activity.AlarmActivity;
import hcmute.edu.vn.lequanghung_23110110.ticktick.activity.MainActivity;

public class NotificationHelper {
    public static final String CHANNEL_ID = "task_reminder_channel_v2";
    public static final String ALARM_CHANNEL_ID = "task_alarm_channel";
    public static final String SERVICE_CHANNEL_ID = "task_service_channel";
    public static final String CHANNEL_NAME = "Task Reminders";
    public static final String ALARM_CHANNEL_NAME = "Strict Alarms";
    public static final String SERVICE_CHANNEL_NAME = "Background Sync";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null)
                return;

            // Channel cho thông báo thường
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Thông báo nhắc nhở công việc");

            // Set âm thanh custom
            android.net.Uri soundUri = android.net.Uri
                    .parse("android.resource://" + context.getPackageName() + "/" + R.raw.custom_notification);
            android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            manager.createNotificationChannel(channel);

            // Channel cho báo thức (quan trọng hơn)
            NotificationChannel alarmChannel = new NotificationChannel(
                    ALARM_CHANNEL_ID, ALARM_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            alarmChannel.setSound(null, null); // Có thể set nhạc chuông riêng ở đây
            alarmChannel.enableVibration(true);
            manager.createNotificationChannel(alarmChannel);

            // Channel cho Foreground Service
            NotificationChannel serviceChannel = new NotificationChannel(
                    SERVICE_CHANNEL_ID, SERVICE_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            serviceChannel.setDescription("Đảm bảo ứng dụng luôn chạy ngầm để nhắc nhở");
            serviceChannel.setShowBadge(false);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public static void showNotification(Context context, int taskId, String taskTitle, boolean isOnTime,
            long dueDateMillis) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("EXTRA_TASK_ID", taskId);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String contentTitle = "Công việc sắp đến hạn!";
        String contentText = "Task: " + taskTitle;

        if (isOnTime) {
            contentTitle = "Công việc đã đến hạn!";
            contentText = taskTitle + " đã đến hạn ngay bây giờ.";
        } else {
            if (dueDateMillis > 0) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy",
                        java.util.Locale.getDefault());
                String formattedDate = sdf.format(new java.util.Date(dueDateMillis));
                contentText = taskTitle + " - Đến hạn lúc: " + formattedDate;
            }
        }

        android.net.Uri soundUri = android.net.Uri
                .parse("android.resource://" + context.getPackageName() + "/" + R.raw.custom_notification);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_today)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null)
            manager.notify(taskId, builder.build());
    }

    public static void showAlarmNotification(Context context, int taskId, String taskTitle) {
        // Intent để mở AlarmActivity
        Intent fullScreenIntent = new Intent(context, AlarmActivity.class);
        fullScreenIntent.putExtra("TASK_ID", taskId);
        fullScreenIntent.putExtra("TASK_TITLE", taskTitle);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, taskId,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_today)
                .setContentTitle("Báo thức công việc!")
                .setContentText(taskTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true) // ĐÂY LÀ KHÓA ĐỂ HIỆN ALARM
                .setAutoCancel(false)
                .setOngoing(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(taskId + 1000, builder.build()); // Dùng ID khác để không đè thông báo thường
        }
    }

    /**
     * Tắt thông báo dựa trên Task ID
     * 
     * @param context Context
     * @param taskId  ID của task
     */
    public static void cancelNotification(Context context, int taskId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            // Tắt cả thông báo thường và thông báo báo thức (được cộng thêm 1000 ở
            // showAlarmNotification)
            manager.cancel(taskId);
            manager.cancel(taskId + 1000);
        }
    }

    public static android.app.Notification createForegroundNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_today)
                .setContentTitle("TickTick Sync")
                .setContentText("Đang chạy ngầm để đảm bảo lời nhắc")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }
}
