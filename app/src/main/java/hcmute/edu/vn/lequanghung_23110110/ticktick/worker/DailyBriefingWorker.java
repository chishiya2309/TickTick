package hcmute.edu.vn.lequanghung_23110110.ticktick.worker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.service.AudioBriefingService;


public class DailyBriefingWorker extends Worker {

    private static final String CHANNEL_ID = "daily_briefing_channel";
    private static final int BRIEFING_NOTI_ID = 1001;

    public DailyBriefingWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        // Tạo Notification Channel trước khi hiển thị
        createNotificationChannel(context);

        try {
            int taskCount = 5;
            showStickyBriefingNotification(context, taskCount);
        } catch (Exception e) {
            Log.e("TEST_WORKER", "LỖI CRASH NGẦM: " + e.getMessage(), e);
        }
        return Result.success();
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Daily Briefing",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for Daily Briefing notifications");
            channel.setShowBadge(true);
            channel.enableLights(true);
            channel.enableVibration(true);
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d("TEST_WORKER", "Đã tạo notification channel: " + CHANNEL_ID);
            } else {
                Log.e("TEST_WORKER", "NotificationManager là null");
            }
        } else {
            Log.d("TEST_WORKER", "Android < 8.0, không cần tạo channel");
        }
    }

    private void showStickyBriefingNotification(Context context, int taskCount) {
        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        Intent listenIntent = new Intent(context, AudioBriefingService.class);
        listenIntent.setAction(AudioBriefingService.ACTION_LISTEN);
        PendingIntent listenPendingIntent = PendingIntent.getService(
                context, 100, listenIntent, pendingIntentFlags);

        Intent dismissIntent = new Intent(context, AudioBriefingService.class);
        dismissIntent.setAction(AudioBriefingService.ACTION_DISMISS);
        dismissIntent.putExtra("NOTIFICATION_ID", BRIEFING_NOTI_ID);
        PendingIntent dismissPendingIntent = PendingIntent.getService(
                context, 200, dismissIntent, pendingIntentFlags);

        
                
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("🌅 Chào buổi sáng!")
                .setContentText("Bạn có các công việc ưu tiên đang chờ hôm nay.")
                .setPriority(NotificationCompat.PRIORITY_MAX) // Priority cao nhất
                .setOngoing(true) // Thông báo không thể vuốt để xóa
                .setAutoCancel(false) // Không tự động hủy khi bấm
                .addAction(android.R.drawable.ic_btn_speak_now, "🎧 Nghe lịch trình", listenPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "✖ Bỏ qua", dismissPendingIntent);

        // Hiển thị thông báo (Kiểm tra quyền POST_NOTIFICATIONS cho Android 13+)
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e("TEST_WORKER", "Không có quyền POST_NOTIFICATIONS");
                return;
            } else {
                Log.d("TEST_WORKER", " Có quyền POST_NOTIFICATIONS");
            }
        }
        
        try {
            Notification notification = builder.build();
            notificationManager.notify(BRIEFING_NOTI_ID, notification);
            
            // Kiểm tra xem notification có thực sự được hiển thị không
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NotificationManager nm = context.getSystemService(NotificationManager.class);
                if (nm != null && nm.areNotificationsEnabled()) {
                    Log.d("TEST_WORKER", " Notifications được bật cho app");
                } else {
                    Log.e("TEST_WORKER", " Notifications bị tắt cho app");
                }
            }
        } catch (Exception e) {
            Log.e("TEST_WORKER", " LỖI khi hiển thị notification: " + e.getMessage(), e);
        }
    }
}
