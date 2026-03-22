package hcmute.edu.vn.lequanghung_23110110.ticktick.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.lequanghung_23110110.ticktick.worker.SyncWorker;

public final class SyncManager {
    private static final String TAG = "SyncManager";
    private static final String WORK_NAME_PERIODIC = "ticktick_periodic_sync";
    private static final String WORK_NAME_ONETIME = "ticktick_onetime_sync";

    private SyncManager() {
    }

    /**
     * Trigger đồng bộ ngay lập tức (OneTimeWorkRequest với CONNECTED constraint)
     */
    public static void syncNow(@NonNull Context context, @NonNull String reason) {
        SessionManager sessionManager = new SessionManager(context);
        if (sessionManager.getSessionType() != SessionManager.SessionType.USER) {
            Log.d(TAG, "Skip cloud sync: not USER session. reason=" + reason);
            return;
        }

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME_ONETIME, ExistingWorkPolicy.REPLACE, request);

        Log.i(TAG, "Enqueued one-time sync. reason=" + reason);
    }

    /** Đăng ký đồng bộ định kỳ (mỗi 15 phút, chỉ khi có WiFi) */
    public static void schedulePeriodic(@NonNull Context context) {
        SessionManager sessionManager = new SessionManager(context);
        if (sessionManager.getSessionType() != SessionManager.SessionType.USER) {
            Log.d(TAG, "Skip periodic sync: not USER session");
            return;
        }

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SyncWorker.class,
                15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request);

        Log.i(TAG, "Scheduled periodic sync (15min, UNMETERED)");
    }

    /** Hủy tất cả sync jobs (dùng khi logout) */
    public static void cancelAll(@NonNull Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_ONETIME);
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC);
        Log.i(TAG, "Cancelled all sync jobs");
    }
}
