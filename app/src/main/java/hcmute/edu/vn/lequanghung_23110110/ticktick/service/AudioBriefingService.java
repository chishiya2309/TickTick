package hcmute.edu.vn.lequanghung_23110110.ticktick.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

public class AudioBriefingService extends Service implements TextToSpeech.OnInitListener {

    public static final String ACTION_LISTEN = "ACTION_LISTEN";
    public static final String ACTION_DISMISS = "ACTION_DISMISS";
    
    private static final String TTS_CHANNEL_ID = "tts_briefing_channel";
    private static final int TTS_NOTIFICATION_ID = 2001;
    private static final String UTTERANCE_ID = "daily_briefing_utterance";

    private TextToSpeech textToSpeech;
    private ExecutorService executorService;
    private boolean isTtsInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();
        textToSpeech = new TextToSpeech(this, this);
        executorService = Executors.newSingleThreadExecutor();
        createTtsNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (action.equals(ACTION_DISMISS)) {
                handleDismissAction(intent);
            } else if (action.equals(ACTION_LISTEN)) {
                handleListenAction();
            }
        }
        return START_NOT_STICKY;
    }

    private void handleDismissAction(Intent intent) {
        int notiId = intent.getIntExtra("NOTIFICATION_ID", -1);
        if (notiId != -1) {
            NotificationManagerCompat.from(this).cancel(notiId);
        }
        stopSelf();
    }

    private void handleListenAction() {
        startForeground(TTS_NOTIFICATION_ID, createTtsNotification());
        executorService.execute(this::performTtsBriefing);
    }

    private void performTtsBriefing() {
        try {
            // Đợi 1 chút để TTS khởi tạo
            int retryCount = 0;
            while (!isTtsInitialized && retryCount < 10) {
                Thread.sleep(500);
                retryCount++;
            }

            TaskDatabaseHelper dbHelper = TaskDatabaseHelper.getInstance(this);
            
            // Tính toán khoảng thời gian Hôm nay (00:00:00 - 23:59:59) theo Local Time
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long startOfToday = cal.getTimeInMillis();
            
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            long endOfToday = cal.getTimeInMillis();
            
            // Lấy chính xác task hôm nay
            List<TaskModel> todayTasks = dbHelper.getStrictlyTodayTasks(startOfToday, endOfToday);
            
            // Văn nói hóa nội dung
            String briefingText = buildFluentBriefingText(todayTasks);
            
            if (isTtsInitialized && textToSpeech != null) {
                speakBriefing(briefingText);
            } else {
                Log.e("AudioBriefingService", "TTS not ready to speak");
                stopSelf();
            }
            
        } catch (Exception e) {
            Log.e("AudioBriefingService", "Execution error: " + e.getMessage());
            stopSelf();
        }
    }


    private String buildFluentBriefingText(List<TaskModel> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return "Hello. You have no tasks for today. Enjoy your relaxing day!";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Hello. ");
        sb.append("You have ").append(tasks.size()).append(" tasks to complete today. ");

        String[] ordinals = {"first", "second", "third", "fourth", "fifth", 
                             "sixth", "seventh", "eighth", "ninth", "tenth"};

        for (int i = 0; i < tasks.size(); i++) {
            String title = tasks.get(i).getTitle();
            String label = (i < ordinals.length) ? ordinals[i] : ("number " + (i + 1));

            sb.append("Task ").append(label).append(" is, ")
              .append(title).append(". ");
        }

        sb.append("Have a productive day!");
        return sb.toString();
    }

    private void speakBriefing(String text) {
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String utteranceId) {}
            @Override public void onDone(String utteranceId) { stopSelf(); }
            @Override public void onError(String utteranceId) { stopSelf(); }
        });

        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }

            textToSpeech.setSpeechRate(0.85f);
            isTtsInitialized = true;
        } else {
            isTtsInitialized = false;
        }
    }

    private void createTtsNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(TTS_CHANNEL_ID, "Audio Briefing", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification createTtsNotification() {
        return new NotificationCompat.Builder(this, TTS_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle("🎧 Assistant is speaking...")
                .setContentText("Preparing your task list for today")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (executorService != null) executorService.shutdown();
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }
}
