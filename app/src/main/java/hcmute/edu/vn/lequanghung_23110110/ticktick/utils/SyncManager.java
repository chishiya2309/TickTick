package hcmute.edu.vn.lequanghung_23110110.ticktick.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

public final class SyncManager {
    private static final String TAG = "SyncManager";

    private SyncManager() {
    }

    public static void syncNow(@NonNull Context context, @NonNull String reason) {
        SessionManager sessionManager = new SessionManager(context);
        if (sessionManager.getSessionType() != SessionManager.SessionType.USER) {
            Log.d(TAG, "Skip cloud sync because session is not USER. reason=" + reason);
            return;
        }

        // Phase 1 hook: cloud sync engine will be implemented in next phase.
        Log.i(TAG, "Sync requested. reason=" + reason);
    }
}

