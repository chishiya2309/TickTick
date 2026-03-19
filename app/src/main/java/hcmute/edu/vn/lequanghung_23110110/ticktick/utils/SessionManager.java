package hcmute.edu.vn.lequanghung_23110110.ticktick.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SessionManager {
    public enum SessionType {
        USER, GUEST, NONE
    }

    private static final String PREF_NAME = "TickTickSessionPrefs";
    private static final String KEY_SESSION_TYPE = "session_type";
    private static final String KEY_USER_UID = "user_uid";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";

    private final SharedPreferences prefs;

    public SessionManager(@NonNull Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public SessionType getSessionType() {
        String raw = prefs.getString(KEY_SESSION_TYPE, SessionType.NONE.name());
        try {
            return SessionType.valueOf(raw);
        } catch (Exception ex) {
            return SessionType.NONE;
        }
    }

    public void setGuestSession() {
        prefs.edit()
                .putString(KEY_SESSION_TYPE, SessionType.GUEST.name())
                .remove(KEY_USER_UID)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_NAME)
                .apply();
    }

    public void setUserSession(@NonNull String uid, @Nullable String email, @Nullable String name) {
        prefs.edit()
                .putString(KEY_SESSION_TYPE, SessionType.USER.name())
                .putString(KEY_USER_UID, uid)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_NAME, name)
                .apply();
    }

    public void clearSession() {
        prefs.edit()
                .putString(KEY_SESSION_TYPE, SessionType.NONE.name())
                .remove(KEY_USER_UID)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_NAME)
                .apply();
    }

    @Nullable public String getUserUid() { return prefs.getString(KEY_USER_UID, null); }
    @Nullable public String getUserEmail() { return prefs.getString(KEY_USER_EMAIL, null); }
    @Nullable public String getUserName() { return prefs.getString(KEY_USER_NAME, null); }
}
