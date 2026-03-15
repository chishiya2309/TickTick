package hcmute.edu.vn.lequanghung_23110110.ticktick.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

import hcmute.edu.vn.lequanghung_23110110.ticktick.service.AlarmReceiver;

/**
 * Utility class để lập lịch Daily Briefing sử dụng AlarmManager thay vì WorkManager
 */
public class DailyBriefingScheduler {

    private static final String TAG = "DailyBriefingScheduler";
    private static final int ALARM_REQUEST_CODE = 1001;
    private static final int TARGET_HOUR = 22;
    private static final int TARGET_MINUTE = 20;

    public static void setupDailyBriefingWork(Context context) {
        Log.d(TAG, "🚀 setupDailyBriefingWork() với AlarmManager");
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "❌ AlarmManager không khả dụng");
            return;
        }

        // Tạo Intent cho AlarmReceiver
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("DAILY_BRIEFING_ALARM");
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, ALARM_REQUEST_CODE, intent, flags);

        // Tính toán thời gian alarm
        Calendar calendar = Calendar.getInstance();
        Calendar targetTime = Calendar.getInstance();
        
        targetTime.set(Calendar.HOUR_OF_DAY, TARGET_HOUR);
        targetTime.set(Calendar.MINUTE, TARGET_MINUTE);
        targetTime.set(Calendar.SECOND, 0);
        targetTime.set(Calendar.MILLISECOND, 0);

        // Nếu thời gian đã qua hôm nay, lên lịch cho ngày mai
        if (calendar.after(targetTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault());
        Log.d(TAG, "=== SETUP DAILY BRIEFING ALARM ===");
        Log.d(TAG, "Thời gian hiện tại: " + sdf.format(calendar.getTime()));
        Log.d(TAG, "Target time: " + TARGET_HOUR + ":" + TARGET_MINUTE);
        Log.d(TAG, "Sẽ chạy vào: " + sdf.format(targetTime.getTime()));

        try {
            // Sử dụng setExactAndAllowWhileIdle để đảm bảo chạy ngay cả khi thiết bị ở chế độ Doze
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 
                        targetTime.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, 
                        targetTime.getTimeInMillis(), pendingIntent);
            }
            
            Log.d(TAG, "✅ Đã setup alarm thành công");
        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi setup alarm: " + e.getMessage(), e);
        }
        
        Log.d(TAG, "=== END SETUP ALARM ===");
    }

    public static void cancelDailyBriefingWork(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.setAction("DAILY_BRIEFING_ALARM");
            
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, ALARM_REQUEST_CODE, intent, flags);
            
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "✅ Đã hủy alarm");
        }
    }
}