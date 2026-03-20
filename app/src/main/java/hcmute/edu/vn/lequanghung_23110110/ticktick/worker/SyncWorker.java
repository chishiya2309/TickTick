package hcmute.edu.vn.lequanghung_23110110.ticktick.worker;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.SessionManager;

public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        if (sessionManager.getSessionType() != SessionManager.SessionType.USER) {
            Log.d(TAG, "Skip sync: not a USER session");
            return Result.success();
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Log.w(TAG, "Skip sync: FirebaseUser is null");
            return Result.success();
        }

        String uid = firebaseUser.getUid();
        TaskDatabaseHelper dbHelper = TaskDatabaseHelper.getInstance(getApplicationContext());
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        try {
            pushLocalToCloud(dbHelper, firestore, uid);
            pullCloudToLocal(dbHelper, firestore, uid);
            dbHelper.setLastSyncTimestamp(System.currentTimeMillis() / 1000L);
            Log.i(TAG, "Sync completed successfully");
            
            // Thông báo cho UI biết để refresh (đặc biệt quan trọng cho lần đăng nhập đầu tiên)
            android.content.Intent intent = new android.content.Intent("hcmute.edu.vn.ticktick.SYNC_COMPLETED");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Sync failed", e);
            return Result.retry();
        }
    }

    private void pushLocalToCloud(TaskDatabaseHelper db, FirebaseFirestore firestore, String uid) throws Exception {
        // Push pending lists
        List<ContentValues> pendingLists = db.getPendingLists();
        for (ContentValues cv : pendingLists) {
            int listId = cv.getAsInteger("_id");
            String syncState = cv.getAsString("sync_state");

            if ("PENDING_DELETE".equals(syncState)) {
                Tasks.await(firestore.collection("users").document(uid)
                        .collection("lists").document(String.valueOf(listId))
                        .delete());
                db.markListSynced(listId);
                db.purgeDeletedList(listId);
                Log.d(TAG, "Pushed list delete: " + listId);
            } else {
                Map<String, Object> data = contentValuesToMap(cv);
                data.remove("sync_state");
                Tasks.await(firestore.collection("users").document(uid)
                        .collection("lists").document(String.valueOf(listId))
                        .set(data, SetOptions.merge()));
                db.markListSynced(listId);
                Log.d(TAG, "Pushed list: " + listId + " (" + syncState + ")");
            }
        }

        // Push pending tasks
        List<ContentValues> pendingTasks = db.getPendingTasks();
        for (ContentValues cv : pendingTasks) {
            int taskId = cv.getAsInteger("_id");
            String syncState = cv.getAsString("sync_state");

            if ("PENDING_DELETE".equals(syncState)) {
                Tasks.await(firestore.collection("users").document(uid)
                        .collection("tasks").document(String.valueOf(taskId))
                        .delete());
                db.markTaskSynced(taskId);
                db.purgeDeletedTask(taskId);
                Log.d(TAG, "Pushed task delete: " + taskId);
            } else {
                Map<String, Object> data = contentValuesToMap(cv);
                data.remove("sync_state");
                Tasks.await(firestore.collection("users").document(uid)
                        .collection("tasks").document(String.valueOf(taskId))
                        .set(data, SetOptions.merge()));
                db.markTaskSynced(taskId);
                Log.d(TAG, "Pushed task: " + taskId + " (" + syncState + ")");
            }
        }
    }

    private void pullCloudToLocal(TaskDatabaseHelper db, FirebaseFirestore firestore, String uid) throws Exception {
        long lastSync = db.getLastSyncTimestamp();

        // Pull lists
        QuerySnapshot listSnap = Tasks.await(
                firestore.collection("users").document(uid)
                        .collection("lists")
                        .whereGreaterThan("updated_at", lastSync)
                        .get());
        for (DocumentSnapshot doc : listSnap.getDocuments()) {
            db.upsertListFromCloud(
                    intVal(doc, "_id"),
                    strVal(doc, "name"),
                    strVal(doc, "icon_name"),
                    intVal(doc, "order_index"),
                    intVal(doc, "is_pinned"),
                    strVal(doc, "owner_type"),
                    strVal(doc, "owner_id"),
                    longVal(doc, "updated_at"),
                    longVal(doc, "deleted_at")
            );
            Log.d(TAG, "Pulled list from cloud: " + doc.getId());
        }

        // Pull tasks
        QuerySnapshot taskSnap = Tasks.await(
                firestore.collection("users").document(uid)
                        .collection("tasks")
                        .whereGreaterThan("updated_at", lastSync)
                        .get());
        for (DocumentSnapshot doc : taskSnap.getDocuments()) {
            db.upsertTaskFromCloud(
                    intVal(doc, "_id"),
                    strVal(doc, "title"),
                    strVal(doc, "description"),
                    intVal(doc, "list_id"),
                    strVal(doc, "date_tag"),
                    longVal(doc, "due_date_millis"),
                    intVal(doc, "is_completed"),
                    intVal(doc, "is_pinned"),
                    strVal(doc, "reminders"),
                    longVal(doc, "created_at"),
                    strVal(doc, "owner_type"),
                    strVal(doc, "owner_id"),
                    longVal(doc, "updated_at"),
                    longVal(doc, "deleted_at")
            );
            Log.d(TAG, "Pulled task from cloud: " + doc.getId());
        }
    }

    private Map<String, Object> contentValuesToMap(ContentValues cv) {
        Map<String, Object> map = new HashMap<>();
        for (String key : cv.keySet()) {
            map.put(key, cv.get(key));
        }
        return map;
    }

    private int intVal(DocumentSnapshot doc, String key) {
        Long val = doc.getLong(key);
        return val != null ? val.intValue() : 0;
    }

    private long longVal(DocumentSnapshot doc, String key) {
        Long val = doc.getLong(key);
        return val != null ? val : 0L;
    }

    private String strVal(DocumentSnapshot doc, String key) {
        String val = doc.getString(key);
        return val != null ? val : "";
    }
}
