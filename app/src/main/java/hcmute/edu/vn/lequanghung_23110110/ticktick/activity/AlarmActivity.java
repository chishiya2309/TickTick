package hcmute.edu.vn.lequanghung_23110110.ticktick.activity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;
import hcmute.edu.vn.lequanghung_23110110.ticktick.service.ReminderService;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.NotificationHelper;

public class AlarmActivity extends AppCompatActivity {

    private Ringtone ringtone;
    private Vibrator vibrator;
    private int taskId;
    private String taskTitle;
    private int snoozeMinutes = 10;

    private TextView tvCurrentTime, tvTaskTitle;
    private FrameLayout btnDismiss, dragBoundary;
    private Button btnSnooze;
    private ObjectAnimator pulseAnim;

    private float startTouchX, startTouchY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hiển thị trên màn hình khóa
        setupLockScreenWindow();

        setContentView(R.layout.activity_alarm);

        initViews();

        // Lấy dữ liệu Task
        taskId = getIntent().getIntExtra("TASK_ID", -1);
        taskTitle = getIntent().getStringExtra("TASK_TITLE");

        setupData();
        startAlarm();
        setupPulseAnimation();
        setupJoystickLogic();
        setupSnoozeLogic();
    }

    private void initViews() {
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTaskTitle = findViewById(R.id.tv_task_title);
        btnDismiss = findViewById(R.id.btnDismiss);
        dragBoundary = findViewById(R.id.dragBoundary);
        btnSnooze = findViewById(R.id.btnSnooze);
    }

    private void setupData() {
        tvTaskTitle.setText(taskTitle != null ? taskTitle : "Công việc");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvCurrentTime.setText(sdf.format(new Date()));
    }

    /**
     * Hiệu ứng Pulse (Mạch đập) cho nút Dismiss
     */
    private void setupPulseAnimation() {
        pulseAnim = ObjectAnimator.ofPropertyValuesHolder(
                btnDismiss,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.15f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.15f)
        );
        pulseAnim.setDuration(800);
        pulseAnim.setRepeatCount(ObjectAnimator.INFINITE);
        pulseAnim.setRepeatMode(ObjectAnimator.REVERSE);
        pulseAnim.start();
    }

    /**
     * Logic kéo Joystick cho nút Dismiss
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupJoystickLogic() {
        btnDismiss.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Dừng hiệu ứng Pulse khi bắt đầu chạm
                    if (pulseAnim != null) pulseAnim.cancel();
                    v.setScaleX(1f);
                    v.setScaleY(1f);

                    startTouchX = event.getRawX();
                    startTouchY = event.getRawY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - startTouchX;
                    float dy = event.getRawY() - startTouchY;

                    // Tính khoảng cách từ tâm bằng định lý Pytago: distance = sqrt(dx^2 + dy^2)
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);

                    // Bán kính tối đa cho phép di chuyển (Bán kính vòng ngoài - Bán kính nút)
                    float maxRadius = (dragBoundary.getWidth() / 2f) - (v.getWidth() / 2f);

                    if (distance > maxRadius) {
                        // Chặn (Clamp) nút lại ở biên vòng tròn
                        float ratio = maxRadius / distance;
                        v.setTranslationX(dx * ratio);
                        v.setTranslationY(dy * ratio);
                    } else {
                        v.setTranslationX(dx);
                        v.setTranslationY(dy);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    float finalDx = v.getTranslationX();
                    float finalDy = v.getTranslationY();
                    float finalDistance = (float) Math.sqrt(finalDx * finalDx + finalDy * finalDy);
                    float limitRadius = (dragBoundary.getWidth() / 2f) - (v.getWidth() / 2f);

                    // Nếu kéo đủ xa (trên 80% bán kính) -> Dismiss
                    if (finalDistance >= limitRadius * 0.8f) {
                        performDismiss();
                    } else {
                        // Nếu chưa đủ xa -> Nảy về tâm và bật lại hiệu ứng Pulse
                        v.animate()
                                .translationX(0)
                                .translationY(0)
                                .setDuration(250)
                                .withEndAction(() -> {
                                    if (pulseAnim != null) pulseAnim.start();
                                })
                                .start();
                    }
                    return true;
            }
            return false;
        });
    }

    private void performDismiss() {
        NotificationHelper.cancelNotification(this, taskId);
        stopAlarm();
        finish();
    }

    private void setupSnoozeLogic() {
        btnSnooze.setOnClickListener(v -> {
            NotificationHelper.cancelNotification(this, taskId);
            long newDueDateMillis = System.currentTimeMillis() + ((long) snoozeMinutes * 60 * 1000);
            TaskDatabaseHelper.getInstance(this).updateTaskDueDate(taskId, newDueDateMillis);
            ReminderService.scheduleStrictAlarm(this, taskId, taskTitle, newDueDateMillis);
            stopAlarm();
            finish();
        });

        findViewById(R.id.btnSnoozePlus).setOnClickListener(v -> {
            snoozeMinutes += 5;
            btnSnooze.setText("Tạm dừng " + snoozeMinutes + " phút");
        });

        findViewById(R.id.btnSnoozeMinus).setOnClickListener(v -> {
            if (snoozeMinutes > 5) {
                snoozeMinutes -= 5;
                btnSnooze.setText("Tạm dừng " + snoozeMinutes + " phút");
            }
        });
    }

    private void setupLockScreenWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
    }

    private void startAlarm() {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

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
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
        if (vibrator != null) vibrator.cancel();
    }

    @Override
    protected void onDestroy() {
        stopAlarm();
        super.onDestroy();
    }
}
