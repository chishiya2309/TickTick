package hcmute.edu.vn.lequanghung_23110110.ticktick.utils;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.lequanghung_23110110.ticktick.worker.DailyBriefingWorker;

/**
 * Utility class để lập lịch Daily Briefing (Báo cáo đầu ngày).
 */
public class DailyBriefingScheduler {

    private static final String TAG = "DailyBriefingScheduler";
    private static final String WORK_NAME = "daily_briefing_work";
    private static final int TARGET_HOUR = 7;  // 7 giờ sáng
    private static final int TARGET_MINUTE = 0;

    public static void setupDailyBriefingWork(Context context) {
        long initialDelay = calculateInitialDelay();
        
        Log.d(TAG, "Lập lịch Daily Briefing. Delay ban đầu: " + (initialDelay / 1000 / 60) + " phút");

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .build();

        PeriodicWorkRequest dailyBriefingWork = new PeriodicWorkRequest.Builder(
                DailyBriefingWorker.class,
                24, TimeUnit.HOURS, // Lặp lại mỗi 24h
                15, TimeUnit.MINUTES // Flex interval
        )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag("daily_briefing")
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE, // Sử dụng UPDATE thay vì REPLACE để tránh reset delay không cần thiết
                dailyBriefingWork
        );
    }

    private static long calculateInitialDelay() {
        Calendar currentTime = Calendar.getInstance();
        Calendar targetTime = Calendar.getInstance();

        targetTime.set(Calendar.HOUR_OF_DAY, TARGET_HOUR);
        targetTime.set(Calendar.MINUTE, TARGET_MINUTE);
        targetTime.set(Calendar.SECOND, 0);
        targetTime.set(Calendar.MILLISECOND, 0);

        if (currentTime.after(targetTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        return targetTime.getTimeInMillis() - currentTime.getTimeInMillis();
    }

    public static void cancelDailyBriefingWork(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}
