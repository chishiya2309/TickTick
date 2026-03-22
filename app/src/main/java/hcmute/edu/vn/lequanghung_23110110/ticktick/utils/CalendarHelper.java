package hcmute.edu.vn.lequanghung_23110110.ticktick.utils;

import android.Manifest;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;

import androidx.core.content.ContextCompat;

import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.SessionManager;

import java.util.TimeZone;

/**
 * Singleton helper để tương tác với Android Calendar Provider.
 * Hỗ trợ CRUD events trên Google Calendar từ dữ liệu TaskModel.
 *
 * Event duration: 1 giờ kết thúc tại dueDateMillis.
 * Task không có dueDateMillis (== -1) sẽ bị bỏ qua.
 */
public class CalendarHelper {

    private static final String TAG = "CalendarHelper";
    private static final long ONE_HOUR_MS = 60 * 60 * 1000L;

    private static CalendarHelper instance;
    private final Context appContext;
    private long cachedCalendarId = -1;
    private String cachedAccountName;
    private String cachedAccountType;

    private CalendarHelper(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static synchronized CalendarHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CalendarHelper(context);
        }
        return instance;
    }

    /** Kiểm tra quyền Calendar đã được cấp chưa */
    public boolean hasCalendarPermission() {
        return ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(appContext, Manifest.permission.WRITE_CALENDAR)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Kiểm tra xem tính năng đồng bộ có được bật trong cài đặt không */
    public boolean isSyncEnabled() {
        SharedPreferences prefs = appContext.getSharedPreferences("TickTickPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("google_calendar_sync_enabled", true); // Default is true
    }

    /** Bật/tắt tính năng đồng bộ Calendar */
    public void setSyncEnabled(boolean enabled) {
        SharedPreferences prefs = appContext.getSharedPreferences("TickTickPrefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("google_calendar_sync_enabled", enabled).apply();
    }

    /** Xóa cache calendarId (khi user đổi account) */
    public void invalidateCache() {
        cachedCalendarId = -1;
        cachedAccountName = null;
        cachedAccountType = null;
    }

    /**
     * Tìm Calendar ID chính của tài khoản Google trên thiết bị.
     * Ưu tiên: IS_PRIMARY = 1, ACCOUNT_TYPE = "com.google".
     * Fallback: Calendar Google đầu tiên tìm thấy.
     *
     * @return calendarId hoặc -1 nếu không tìm thấy
     */
    public long getPrimaryCalendarId() {
        if (cachedCalendarId != -1) {
            return cachedCalendarId;
        }

        if (!hasCalendarPermission()) {
            Log.w(TAG, "Calendar permission not granted");
            return -1;
        }

        // Lấy email người dùng hiện tại từ SessionManager
        SessionManager sessionManager = new SessionManager(appContext);
        String userEmail = sessionManager.getUserEmail();

        ContentResolver cr = appContext.getContentResolver();
        String[] projection = {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE,
                CalendarContract.Calendars.IS_PRIMARY,
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        };

        // Query tất cả Google Calendars
        String selection = CalendarContract.Calendars.ACCOUNT_TYPE + " = ? AND "
                + CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " >= ?";
        String[] selectionArgs = {
                "com.google",
                String.valueOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR)
        };

        Cursor cursor = cr.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection, selection, selectionArgs, null);

        if (cursor == null) {
            Log.w(TAG, "Calendar query returned null");
            return -1;
        }

        long fallbackId = -1;
        String fallbackAccountName = null;
        String fallbackAccountType = null;

        try {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID));
                String accountName = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME));
                String accountType = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_TYPE));
                int isPrimary = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.IS_PRIMARY));

                // 1. Ưu tiên tuyệt đối: Account name khớp với email đang đăng nhập trong app
                if (userEmail != null && userEmail.equalsIgnoreCase(accountName)) {
                    cachedCalendarId = id;
                    cachedAccountName = accountName;
                    cachedAccountType = accountType;
                    Log.d(TAG, "Matched logged-in user email: " + accountName);
                    return cachedCalendarId;
                }

                // 2. Fallback 1: Calendar được đánh dấu là IS_PRIMARY trên device
                if (isPrimary == 1) {
                    if (fallbackId == -1 || fallbackAccountName == null) {
                        fallbackId = id;
                        fallbackAccountName = accountName;
                        fallbackAccountType = accountType;
                    }
                }

                // 3. Fallback 2: Bất kỳ Google account nào tìm thấy đầu tiên
                if (fallbackId == -1) {
                    fallbackId = id;
                    fallbackAccountName = accountName;
                    fallbackAccountType = accountType;
                }
            }
        } finally {
            cursor.close();
        }

        if (fallbackId != -1) {
            cachedCalendarId = fallbackId;
            cachedAccountName = fallbackAccountName;
            cachedAccountType = fallbackAccountType;
            Log.d(TAG, "Using fallback Google Calendar: id=" + fallbackId + ", account=" + fallbackAccountName);
        } else {
            Log.w(TAG, "No Google Calendar found on device");
        }

        return cachedCalendarId;
    }

    /**
     * Chèn một sự kiện Calendar từ TaskModel.
     * Event kéo dài 1 giờ, kết thúc tại dueDateMillis.
     *
     * @param task TaskModel cần sync
     * @return eventId của event vừa tạo, hoặc -1 nếu thất bại
     */
    public long insertEvent(TaskModel task) {
        if (task.getDueDateMillis() <= 0) {
            Log.d(TAG, "Skipping task without due date: " + task.getTitle());
            return -1;
        }

        long calendarId = getPrimaryCalendarId();
        if (calendarId == -1) {
            Log.w(TAG, "No calendar available for insert");
            return -1;
        }

        if (!hasCalendarPermission()) {
            return -1;
        }

        ContentResolver cr = appContext.getContentResolver();
        ContentValues values = buildEventValues(calendarId, task);

        try {
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (uri != null) {
                long eventId = Long.parseLong(uri.getLastPathSegment());
                Log.d(TAG, "Inserted calendar event: id=" + eventId + " for task: " + task.getTitle());
                triggerSync();
                return eventId;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert calendar event", e);
        }

        return -1;
    }

    /**
     * Cập nhật sự kiện Calendar đã tồn tại.
     *
     * @param eventId ID của event trên Calendar
     * @param task    TaskModel với dữ liệu mới
     * @return true nếu cập nhật thành công
     */
    public boolean updateEvent(long eventId, TaskModel task) {
        if (eventId <= 0) {
            Log.w(TAG, "Invalid eventId for update: " + eventId);
            return false;
        }

        if (task.getDueDateMillis() <= 0) {
            // Task không còn due date → xóa event
            deleteEvent(eventId);
            return true;
        }

        long calendarId = getPrimaryCalendarId();
        if (calendarId == -1) {
            return false;
        }

        if (!hasCalendarPermission()) {
            return false;
        }

        ContentResolver cr = appContext.getContentResolver();
        ContentValues values = buildEventValues(calendarId, task);
        Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);

        try {
            int rows = cr.update(eventUri, values, null, null);
            Log.d(TAG, "Updated calendar event: id=" + eventId + ", rows=" + rows);
            if (rows > 0) {
                triggerSync();
            }
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to update calendar event: " + eventId, e);
        }

        return false;
    }

    /**
     * Xóa sự kiện Calendar.
     *
     * @param eventId ID của event cần xóa
     * @return true nếu xóa thành công
     */
    public boolean deleteEvent(long eventId) {
        if (eventId <= 0) {
            Log.w(TAG, "Invalid eventId for delete: " + eventId);
            return false;
        }

        if (!hasCalendarPermission()) {
            return false;
        }

        ContentResolver cr = appContext.getContentResolver();
        Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);

        try {
            int rows = cr.delete(eventUri, null, null);
            Log.d(TAG, "Deleted calendar event: id=" + eventId + ", rows=" + rows);
            if (rows > 0) {
                triggerSync();
            }
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete calendar event: " + eventId, e);
        }

        return false;
    }

    /**
     * Xây dựng ContentValues cho Calendar Event từ TaskModel.
     * Event: start = dueDateMillis - 1h, end = dueDateMillis
     */
    private ContentValues buildEventValues(long calendarId, TaskModel task) {
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.TITLE, task.getTitle());

        String description = task.getDescription();
        if (description != null && !description.isEmpty()) {
            values.put(CalendarContract.Events.DESCRIPTION, description);
        }

        long endTime = task.getDueDateMillis();
        long startTime = endTime - ONE_HOUR_MS;

        values.put(CalendarContract.Events.DTSTART, startTime);
        values.put(CalendarContract.Events.DTEND, endTime);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        return values;
    }

    /**
     * Yêu cầu Android System trigger sync ngay lập tức cho account Google hiện tại.
     * Giúp giảm độ trễ khi hiển thị trên ứng dụng Google Calendar.
     */
    private void triggerSync() {
        if (cachedAccountName == null || cachedAccountType == null) {
            getPrimaryCalendarId(); // Đảm bảo đã load account info
        }

        if (cachedAccountName != null && cachedAccountType != null) {
            try {
                Account account = new Account(cachedAccountName, cachedAccountType);
                Bundle extras = new Bundle();
                extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

                ContentResolver.requestSync(account, CalendarContract.AUTHORITY, extras);
                Log.d(TAG, "Requested manual sync for account: " + cachedAccountName);
            } catch (Exception e) {
                Log.e(TAG, "Failed to request calendar sync", e);
            }
        }
    }
}
