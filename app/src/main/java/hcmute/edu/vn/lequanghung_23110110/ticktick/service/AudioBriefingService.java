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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

/**
 * Service xử lý Audio Daily Briefing theo kiến trúc "1 Service - 2 Nhiệm vụ"
 * - ACTION_DISMISS: Hủy thông báo và dừng service
 * - ACTION_LISTEN: Chạy TTS để đọc danh sách công việc
 */
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
        Log.d("AudioBriefingService", "Service được khởi tạo");
        
        // Khởi tạo TextToSpeech với ngôn ngữ Tiếng Việt
        textToSpeech = new TextToSpeech(this, this);
        
        // Khởi tạo ExecutorService cho background thread
        executorService = Executors.newSingleThreadExecutor();
        
        // Tạo notification channel cho TTS foreground service
        createTtsNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            Log.d("AudioBriefingService", "Nhận action: " + action);

            if (action.equals(ACTION_DISMISS)) {
                handleDismissAction(intent);
            } else if (action.equals(ACTION_LISTEN)) {
                handleListenAction();
            }
        }

        // Return START_NOT_STICKY: Nếu hệ thống kill service, không tự động restart
        return START_NOT_STICKY;
    }

    /**
     * Xử lý ACTION_DISMISS: Hủy thông báo và dừng service ngay lập tức
     */
    private void handleDismissAction(Intent intent) {
        Log.d("AudioBriefingService", "Xử lý ACTION_DISMISS");
        
        // Lấy notification ID và hủy thông báo
        int notiId = intent.getIntExtra("NOTIFICATION_ID", -1);
        if (notiId != -1) {
            NotificationManagerCompat.from(this).cancel(notiId);
            Log.d("AudioBriefingService", "Đã hủy notification ID: " + notiId);
        }
        
        // Dừng service ngay lập tức
        stopSelf();
    }

    /**
     * Xử lý ACTION_LISTEN: Chạy foreground service và đọc danh sách công việc
     */
    private void handleListenAction() {
        Log.d("AudioBriefingService", "Xử lý ACTION_LISTEN");
        
        // BẮT BUỘC gọi startForeground() ngay lập tức để tránh ForegroundServiceDidNotStartInTimeException
        startForeground(TTS_NOTIFICATION_ID, createTtsNotification());
        
        // Chạy việc query database và TTS trên background thread để tránh ANR
        executorService.execute(this::performTtsBriefing);
    }

    /**
     * Thực hiện việc đọc briefing trên background thread
     */
    private void performTtsBriefing() {
        try {
            Log.d("AudioBriefingService", "Bắt đầu thực hiện TTS briefing");
            
            // Giả lập việc query database (sleep 1s như yêu cầu)
            Thread.sleep(1000);
            
            // Lấy danh sách task từ database
            TaskDatabaseHelper dbHelper = TaskDatabaseHelper.getInstance(this);
            
            // Lấy tasks hôm nay và quá hạn
            long currentTime = System.currentTimeMillis();
            long startOfDay = currentTime - (currentTime % (24 * 60 * 60 * 1000));
            long endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1;
            
            List<TaskModel> todayTasks = dbHelper.getTodayAndOverdueTasks(startOfDay, endOfDay);
            
            // Tạo nội dung để đọc
            String briefingText = createBriefingText(todayTasks);
            
            // Chờ TTS khởi tạo xong rồi mới đọc
            if (isTtsInitialized && textToSpeech != null) {
                speakBriefing(briefingText);
            } else {
                Log.w("AudioBriefingService", "TTS chưa được khởi tạo");
                stopSelf();
            }
            
        } catch (InterruptedException e) {
            Log.e("AudioBriefingService", "Thread bị gián đoạn: " + e.getMessage());
            stopSelf();
        } catch (Exception e) {
            Log.e("AudioBriefingService", "Lỗi khi thực hiện TTS briefing: " + e.getMessage());
            stopSelf();
        }
    }

    /**
     * Tạo nội dung briefing từ danh sách tasks
     */
    private String createBriefingText(List<TaskModel> tasks) {
        StringBuilder briefing = new StringBuilder();
        briefing.append("Chào buổi sáng! ");
        
        if (tasks.isEmpty()) {
            briefing.append("Hôm nay bạn không có công việc nào cần làm. Chúc bạn một ngày tuyệt vời!");
        } else {
            briefing.append("Hôm nay bạn có ").append(tasks.size()).append(" công việc cần hoàn thành. ");
            
            // Đọc tối đa 5 task đầu tiên để tránh quá dài
            int maxTasks = Math.min(tasks.size(), 5);
            for (int i = 0; i < maxTasks; i++) {
                TaskModel task = tasks.get(i);
                briefing.append("Thứ ").append(i + 1).append(": ").append(task.getTitle()).append(". ");
            }
            
            if (tasks.size() > 5) {
                briefing.append("Và còn ").append(tasks.size() - 5).append(" công việc khác. ");
            }
            
            briefing.append("Chúc bạn một ngày làm việc hiệu quả!");
        }
        
        return briefing.toString();
    }

    /**
     * Sử dụng TTS để đọc briefing
     */
    private void speakBriefing(String text) {
        Log.d("AudioBriefingService", "Bắt đầu đọc: " + text);
        
        // Cài đặt UtteranceProgressListener để tự động dừng service khi đọc xong
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                Log.d("AudioBriefingService", "TTS bắt đầu đọc");
            }

            @Override
            public void onDone(String utteranceId) {
                Log.d("AudioBriefingService", "TTS đọc xong, dừng service");
                stopSelf(); // Tự động dừng service khi đọc xong
            }

            @Override
            public void onError(String utteranceId) {
                Log.e("AudioBriefingService", "TTS gặp lỗi");
                stopSelf();
            }
        });

        // Tạo params cho TTS
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
        
        // Bắt đầu đọc
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Cài đặt ngôn ngữ Tiếng Việt
            int result = textToSpeech.setLanguage(new Locale("vi", "VN"));
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("AudioBriefingService", "Tiếng Việt không được hỗ trợ, sử dụng ngôn ngữ mặc định");
                textToSpeech.setLanguage(Locale.getDefault());
            }
            
            // Cài đặt tốc độ đọc
            textToSpeech.setSpeechRate(0.9f);
            
            isTtsInitialized = true;
            Log.d("AudioBriefingService", "TTS đã được khởi tạo thành công");
        } else {
            Log.e("AudioBriefingService", "Khởi tạo TTS thất bại");
            isTtsInitialized = false;
        }
    }

    /**
     * Tạo notification channel cho TTS foreground service
     */
    private void createTtsNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    TTS_CHANNEL_ID,
                    "Audio Briefing TTS",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Channel for Audio Briefing TTS service");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Tạo notification cho TTS foreground service
     */
    private Notification createTtsNotification() {
        return new NotificationCompat.Builder(this, TTS_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle("🎧 Trợ lý đang đọc...")
                .setContentText("Đang đọc lịch trình công việc hôm nay")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("AudioBriefingService", "Service bị hủy");
        
        // Dừng và giải phóng TTS để tránh memory leak
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        
        // Shutdown executor service
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Không sử dụng Bound Service
    }
}