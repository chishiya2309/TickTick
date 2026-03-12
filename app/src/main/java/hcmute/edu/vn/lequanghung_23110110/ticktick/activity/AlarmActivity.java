package hcmute.edu.vn.lequanghung_23110110.ticktick.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;
import hcmute.edu.vn.lequanghung_23110110.ticktick.service.AlarmReceiver;

public class AlarmActivity extends AppCompatActivity {

    private Ringtone ringtone;
    private Vibrator vibrator;
    private int taskId;
    private String taskTitle;
    private int snoozeMinutes = 5;
    private Button btnSnooze;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cấu hình Window để hiển thị trên màn hình khóa
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }

        setContentView(R.layout.activity_alarm);

        taskId = getIntent().getIntExtra("TASK_ID", -1);
        taskTitle = getIntent().getStringExtra("TASK_TITLE");

        TextView tvTaskTitle = findViewById(R.id.tvTaskTitle);
        TextView tvCurrentTime = findViewById(R.id.tvTaskTime);
        TextView tvStartsIn = findViewById(R.id.tvStartsIn);
        btnSnooze = findViewById(R.id.btnSnooze);

        if (tvTaskTitle != null) {
            tvTaskTitle.setText(taskTitle);
        }

        // Hiển thị thời gian hiện tại
        if (tvCurrentTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, h:mm a", Locale.getDefault());
            tvCurrentTime.setText(sdf.format(new Date()));
        }

        // Tính toán "Starts in X minutes" nếu có thể
        if (tvStartsIn != null) {
            updateStartsIn(tvStartsIn);
        }

        startAlarm();

        // Nút Dismiss (Hoàn thành)
        View btnDismiss = findViewById(R.id.btnDismiss);
        if (btnDismiss != null) {
            btnDismiss.setOnClickListener(v -> {
                stopAlarm();
                TaskDatabaseHelper.getInstance(this).updateTaskCompleted(taskId, true);
                finish();
            });
        }

        // Nút Snooze (Báo lại)
        if (btnSnooze != null) {
            btnSnooze.setOnClickListener(v -> {
                stopAlarm();
                scheduleSnooze(snoozeMinutes);
                finish();
            });
        }

        // Nút Tăng thời gian Snooze
        View btnSnoozePlus = findViewById(R.id.btnSnoozePlus);
        if (btnSnoozePlus != null) {
            btnSnoozePlus.setOnClickListener(v -> {
                snoozeMinutes += 5;
                updateSnoozeText();
            });
        }

        // Nút Giảm thời gian Snooze
        View btnSnoozeMinus = findViewById(R.id.btnSnoozeMinus);
        if (btnSnoozeMinus != null) {
            btnSnoozeMinus.setOnClickListener(v -> {
                if (snoozeMinutes > 5) {
                    snoozeMinutes -= 5;
                    updateSnoozeText();
                }
            });
        }
    }

    private void updateStartsIn(TextView tvStartsIn) {
        TaskModel task = TaskDatabaseHelper.getInstance(this).getTaskById(taskId);
        if (task != null && task.getDueDateMillis() > 0) {
            long diffMillis = task.getDueDateMillis() - System.currentTimeMillis();
            if (diffMillis > 0) {
                long minutes = diffMillis / (60 * 1000);
                if (minutes > 0) {
                    tvStartsIn.setText("Starts in " + minutes + " minutes");
                } else {
                    tvStartsIn.setText("Starting now");
                }
            } else {
                tvStartsIn.setText("Task is due now");
            }
        } else {
            tvStartsIn.setVisibility(View.GONE);
        }
    }

    private void updateSnoozeText() {
        if (btnSnooze != null) {
            btnSnooze.setText("Snooze " + snoozeMinutes + " mins");
        }
    }

    private void startAlarm() {
        // Âm thanh báo thức
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }
        ringtone = RingtoneManager.getRingtone(this, alarmUri);
        if (ringtone != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ringtone.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
            }
            ringtone.play();
        }

        // Rung
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 500, 500}, 0));
            } else {
                vibrator.vibrate(new long[]{0, 500, 500}, 0);
            }
        }
    }

    private void stopAlarm() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private void scheduleSnooze(int minutes) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("TASK_ID", taskId);
        intent.putExtra("TASK_TITLE", taskTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, taskId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long snoozeTime = System.currentTimeMillis() + (long) minutes * 60 * 1000;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent);
            }
        } catch (SecurityException e) {
            // Handle cases where SCHEDULE_EXACT_ALARM permission is not granted on Android 12+
            alarmManager.set(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent);
        }
    }

    @Override
    protected void onDestroy() {
        stopAlarm();
        super.onDestroy();
    }
}
