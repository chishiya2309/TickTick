package hcmute.edu.vn.lequanghung_23110110.ticktick.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.NotificationHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.worker.DailyBriefingWorker;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_SHOW_NOTIFICATION = "hcmute.edu.vn.ticktick.SHOW_NOTIFICATION";
    public static final String ACTION_START_ALARM = "hcmute.edu.vn.ticktick.START_ALARM";
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if ("DAILY_BRIEFING_ALARM".equals(action)) {
            // Xử lý Daily Briefing Alarm
            Log.d(TAG, "🔔 Nhận Daily Briefing Alarm - Trigger Worker");
            
            // Trigger DailyBriefingWorker ngay lập tức
            OneTimeWorkRequest briefingWork = new OneTimeWorkRequest.Builder(DailyBriefingWorker.class)
                    .addTag("daily_briefing_alarm")
                    .build();
            
            WorkManager.getInstance(context).enqueue(briefingWork);
            Log.d(TAG, "✅ Đã enqueue DailyBriefingWorker từ alarm");
            
            return;
        }
        
        // Xử lý các alarm khác (task reminders)
        int taskId = intent.getIntExtra("TASK_ID", -1);
        String taskTitle = intent.getStringExtra("TASK_TITLE");

        if (ACTION_SHOW_NOTIFICATION.equals(action)) {
            // Hiện thông báo thường (trước 15p)
            NotificationHelper.showNotification(context, taskId, taskTitle);
        } else if (ACTION_START_ALARM.equals(action)) {
            // Hiển thị thông báo tràn viền (FullScreenIntent) để mở AlarmActivity
            NotificationHelper.showAlarmNotification(context, taskId, taskTitle);
        }
    }
}
