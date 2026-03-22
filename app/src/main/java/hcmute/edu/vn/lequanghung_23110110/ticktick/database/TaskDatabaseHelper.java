package hcmute.edu.vn.lequanghung_23110110.ticktick.database;

import static hcmute.edu.vn.lequanghung_23110110.ticktick.utils.SessionManager.OWNER_ID_GUEST_LOCAL;
import static hcmute.edu.vn.lequanghung_23110110.ticktick.utils.SessionManager.OWNER_TYPE_GUEST;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.lequanghung_23110110.ticktick.model.DrawerMenuItem;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.ListModel;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.SessionManager;

public class TaskDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ticktick.db";
    private static final int DB_VERSION = 12;

    // Thêm helper function parse List<String>
    private String listToString(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    private List<String> stringToList(String str) {
        List<String> list = new ArrayList<>();
        if (str == null || str.isEmpty()) return list;
        String[] parts = str.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    // === Table: lists ===
    private static final String TABLE_LISTS = "lists";
    private static final String COL_LIST_ID = "_id";
    private static final String COL_LIST_NAME = "name";
    private static final String COL_LIST_ICON = "icon_name";
    private static final String COL_LIST_ORDER = "order_index";
    private static final String COL_LIST_IS_PINNED = "is_pinned";
    private static final String COL_LIST_OWNER_TYPE = "owner_type";
    private static final String COL_LIST_OWNER_ID = "owner_id";
    private static final String COL_LIST_UPDATED_AT = "updated_at";
    private static final String COL_LIST_SYNC_STATE = "sync_state";
    private static final String COL_LIST_DELETED_AT = "deleted_at";

    // === Table: tasks ===
    private static final String TABLE_TASKS = "tasks";
    private static final String COL_TASK_ID = "_id";
    private static final String COL_TASK_TITLE = "title";
    private static final String COL_TASK_DESCRIPTION = "description";
    private static final String COL_TASK_LIST_ID = "list_id";
    private static final String COL_TASK_DATE_TAG = "date_tag";
    private static final String COL_TASK_DUE_DATE = "due_date_millis";
    private static final String COL_TASK_COMPLETED = "is_completed";
    private static final String COL_TASK_CREATED = "created_at";
    private static final String COL_TASK_IS_PINNED = "is_pinned"; // New column cho ghim task
    private static final String COL_TASK_REMINDERS = "reminders"; // New column cho lời nhắc
    private static final String COL_TASK_OWNER_TYPE = "owner_type";
    private static final String COL_TASK_OWNER_ID = "owner_id";
    private static final String COL_TASK_UPDATED_AT = "updated_at";
    private static final String COL_TASK_SYNC_STATE = "sync_state";
    private static final String COL_TASK_DELETED_AT = "deleted_at";
    private static final String COL_TASK_CALENDAR_EVENT_ID = "calendar_event_id";

    private static final String SYNC_STATE_SYNCED = "SYNCED";
    private static final String SYNC_STATE_PENDING_CREATE = "PENDING_CREATE";
    private static final String SYNC_STATE_PENDING_UPDATE = "PENDING_UPDATE";
    private static final String SYNC_STATE_PENDING_DELETE = "PENDING_DELETE";

    private static final String SYSTEM_OWNER_TYPE = "system";
    private static final String SYSTEM_OWNER_ID = "system";

    // Singleton
    private static TaskDatabaseHelper instance;
    private final Context appContext;

    private static final class OwnerScope {
        final String ownerType;
        final String ownerId;

        OwnerScope(String ownerType, String ownerId) {
            this.ownerType = ownerType;
            this.ownerId = ownerId;
        }
    }

    public static synchronized TaskDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TaskDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private TaskDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.appContext = context.getApplicationContext();
    }

    private OwnerScope resolveOwnerScope() {
        SessionManager sessionManager = new SessionManager(appContext);
        return new OwnerScope(sessionManager.getOwnerType(), sessionManager.getOwnerId());
    }

    private long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }

    private String combineSelection(String baseSelection, String extraSelection) {
        if (TextUtils.isEmpty(baseSelection)) {
            return extraSelection;
        }
        return "(" + baseSelection + ") AND (" + extraSelection + ")";
    }

    private String[] appendArgs(String[] baseArgs, String... extraArgs) {
        int baseLength = baseArgs == null ? 0 : baseArgs.length;
        String[] merged = new String[baseLength + extraArgs.length];
        if (baseLength > 0) {
            System.arraycopy(baseArgs, 0, merged, 0, baseLength);
        }
        System.arraycopy(extraArgs, 0, merged, baseLength, extraArgs.length);
        return merged;
    }

    private String taskOwnerFilter() {
        return COL_TASK_OWNER_TYPE + " = ? AND " + COL_TASK_OWNER_ID + " = ? AND " + COL_TASK_DELETED_AT + " = 0";
    }

    private String listCustomOwnerFilter() {
        return COL_LIST_ID + " > 4 AND " + COL_LIST_OWNER_TYPE + " = ? AND " + COL_LIST_OWNER_ID + " = ? AND " + COL_LIST_DELETED_AT + " = 0";
    }

    private void addColumnIfMissing(SQLiteDatabase db, String table, String column, String alterSql) {
        if (!hasColumn(db, table, column)) {
            db.execSQL(alterSql);
        }
    }

    private boolean hasColumn(SQLiteDatabase db, String table, String column) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);
        try {
            while (cursor.moveToNext()) {
                if (column.equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow("name")))) {
                    return true;
                }
            }
            return false;
        } finally {
            cursor.close();
        }
    }

    private void createIndexes(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_task_owner_list ON " + TABLE_TASKS + " (" + COL_TASK_OWNER_TYPE + ", " + COL_TASK_OWNER_ID + ", " + COL_TASK_LIST_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_task_owner_due ON " + TABLE_TASKS + " (" + COL_TASK_OWNER_TYPE + ", " + COL_TASK_OWNER_ID + ", " + COL_TASK_DUE_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_list_owner_order ON " + TABLE_LISTS + " (" + COL_LIST_OWNER_TYPE + ", " + COL_LIST_OWNER_ID + ", " + COL_LIST_ORDER + ")");
    }

    private void migrateToVersion11(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            addColumnIfMissing(db, TABLE_LISTS, COL_LIST_OWNER_TYPE, "ALTER TABLE " + TABLE_LISTS + " ADD COLUMN " + COL_LIST_OWNER_TYPE + " TEXT NOT NULL DEFAULT '" + OWNER_TYPE_GUEST + "'");
            addColumnIfMissing(db, TABLE_LISTS, COL_LIST_OWNER_ID, "ALTER TABLE " + TABLE_LISTS + " ADD COLUMN " + COL_LIST_OWNER_ID + " TEXT NOT NULL DEFAULT '" + OWNER_ID_GUEST_LOCAL + "'");
            addColumnIfMissing(db, TABLE_LISTS, COL_LIST_IS_PINNED, "ALTER TABLE " + TABLE_LISTS + " ADD COLUMN " + COL_LIST_IS_PINNED + " INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_LISTS, COL_LIST_UPDATED_AT, "ALTER TABLE " + TABLE_LISTS + " ADD COLUMN " + COL_LIST_UPDATED_AT + " INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_LISTS, COL_LIST_SYNC_STATE, "ALTER TABLE " + TABLE_LISTS + " ADD COLUMN " + COL_LIST_SYNC_STATE + " TEXT DEFAULT '" + SYNC_STATE_SYNCED + "'");
            addColumnIfMissing(db, TABLE_LISTS, COL_LIST_DELETED_AT, "ALTER TABLE " + TABLE_LISTS + " ADD COLUMN " + COL_LIST_DELETED_AT + " INTEGER DEFAULT 0");

            addColumnIfMissing(db, TABLE_TASKS, COL_TASK_OWNER_TYPE, "ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COL_TASK_OWNER_TYPE + " TEXT NOT NULL DEFAULT '" + OWNER_TYPE_GUEST + "'");
            addColumnIfMissing(db, TABLE_TASKS, COL_TASK_OWNER_ID, "ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COL_TASK_OWNER_ID + " TEXT NOT NULL DEFAULT '" + OWNER_ID_GUEST_LOCAL + "'");
            addColumnIfMissing(db, TABLE_TASKS, COL_TASK_IS_PINNED, "ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COL_TASK_IS_PINNED + " INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_TASKS, COL_TASK_REMINDERS, "ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COL_TASK_REMINDERS + " TEXT DEFAULT ''");
            addColumnIfMissing(db, TABLE_TASKS, COL_TASK_CREATED, "ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COL_TASK_CREATED + " INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_TASKS, COL_TASK_UPDATED_AT, "ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COL_TASK_UPDATED_AT + " INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_TASKS, COL_TASK_SYNC_STATE, "ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COL_TASK_SYNC_STATE + " TEXT DEFAULT '" + SYNC_STATE_SYNCED + "'");
            addColumnIfMissing(db, TABLE_TASKS, COL_TASK_DELETED_AT, "ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COL_TASK_DELETED_AT + " INTEGER DEFAULT 0");

            db.execSQL("UPDATE " + TABLE_LISTS + " SET " + COL_LIST_OWNER_TYPE + "='" + SYSTEM_OWNER_TYPE + "', " + COL_LIST_OWNER_ID + "='" + SYSTEM_OWNER_ID + "' WHERE " + COL_LIST_ID + " <= 4");
            db.execSQL("UPDATE " + TABLE_LISTS + " SET " + COL_LIST_OWNER_TYPE + "='" + OWNER_TYPE_GUEST + "', " + COL_LIST_OWNER_ID + "='" + OWNER_ID_GUEST_LOCAL + "' WHERE " + COL_LIST_ID + " > 4");
            db.execSQL("UPDATE " + TABLE_TASKS + " SET " + COL_TASK_OWNER_TYPE + "='" + OWNER_TYPE_GUEST + "' WHERE " + COL_TASK_OWNER_TYPE + " IS NULL OR " + COL_TASK_OWNER_TYPE + " = ''");
            db.execSQL("UPDATE " + TABLE_TASKS + " SET " + COL_TASK_OWNER_ID + "='" + OWNER_ID_GUEST_LOCAL + "' WHERE " + COL_TASK_OWNER_ID + " IS NULL OR " + COL_TASK_OWNER_ID + " = ''");
            db.execSQL("UPDATE " + TABLE_LISTS + " SET " + COL_LIST_SYNC_STATE + "='" + SYNC_STATE_SYNCED + "' WHERE " + COL_LIST_SYNC_STATE + " IS NULL OR " + COL_LIST_SYNC_STATE + " = ''");
            db.execSQL("UPDATE " + TABLE_TASKS + " SET " + COL_TASK_SYNC_STATE + "='" + SYNC_STATE_SYNCED + "' WHERE " + COL_TASK_SYNC_STATE + " IS NULL OR " + COL_TASK_SYNC_STATE + " = ''");
            db.execSQL("UPDATE " + TABLE_LISTS + " SET " + COL_LIST_UPDATED_AT + " = strftime('%s','now') WHERE " + COL_LIST_UPDATED_AT + " IS NULL OR " + COL_LIST_UPDATED_AT + " = 0");
            db.execSQL("UPDATE " + TABLE_TASKS + " SET " + COL_TASK_UPDATED_AT + " = strftime('%s','now') WHERE " + COL_TASK_UPDATED_AT + " IS NULL OR " + COL_TASK_UPDATED_AT + " = 0");
            db.execSQL("UPDATE " + TABLE_TASKS + " SET " + COL_TASK_CREATED + " = strftime('%s','now') WHERE " + COL_TASK_CREATED + " IS NULL OR " + COL_TASK_CREATED + " = 0");
            db.execSQL("UPDATE " + TABLE_LISTS + " SET " + COL_LIST_DELETED_AT + "=0 WHERE " + COL_LIST_DELETED_AT + " IS NULL");
            db.execSQL("UPDATE " + TABLE_TASKS + " SET " + COL_TASK_DELETED_AT + "=0 WHERE " + COL_TASK_DELETED_AT + " IS NULL");
            db.execSQL("UPDATE " + TABLE_TASKS + " SET " + COL_TASK_IS_PINNED + "=0 WHERE " + COL_TASK_IS_PINNED + " IS NULL");
            db.execSQL("UPDATE " + TABLE_LISTS + " SET " + COL_LIST_IS_PINNED + "=0 WHERE " + COL_LIST_IS_PINNED + " IS NULL");
            db.execSQL("UPDATE " + TABLE_TASKS + " SET " + COL_TASK_REMINDERS + "='' WHERE " + COL_TASK_REMINDERS + " IS NULL");

            createIndexes(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void migrateToVersion12(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            addColumnIfMissing(db, TABLE_TASKS, COL_TASK_CALENDAR_EVENT_ID,
                    "ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COL_TASK_CALENDAR_EVENT_ID + " INTEGER DEFAULT -1");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_LISTS + " ("
                + COL_LIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_LIST_NAME + " TEXT NOT NULL, "
                + COL_LIST_ICON + " TEXT, "
                + COL_LIST_ORDER + " INTEGER DEFAULT 0, "
                + COL_LIST_IS_PINNED + " INTEGER DEFAULT 0, "
                + COL_LIST_OWNER_TYPE + " TEXT NOT NULL DEFAULT '" + OWNER_TYPE_GUEST + "', "
                + COL_LIST_OWNER_ID + " TEXT NOT NULL DEFAULT '" + OWNER_ID_GUEST_LOCAL + "', "
                + COL_LIST_UPDATED_AT + " INTEGER DEFAULT (strftime('%s','now')), "
                + COL_LIST_SYNC_STATE + " TEXT DEFAULT '" + SYNC_STATE_SYNCED + "', "
                + COL_LIST_DELETED_AT + " INTEGER DEFAULT 0"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_TASKS + " ("
                + COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TASK_TITLE + " TEXT NOT NULL, "
                + COL_TASK_DESCRIPTION + " TEXT, "
                + COL_TASK_LIST_ID + " INTEGER NOT NULL, "
                + COL_TASK_DATE_TAG + " TEXT, "
                + COL_TASK_DUE_DATE + " INTEGER DEFAULT -1, "
                + COL_TASK_COMPLETED + " INTEGER DEFAULT 0, "
                + COL_TASK_IS_PINNED + " INTEGER DEFAULT 0, "
                + COL_TASK_CREATED + " INTEGER DEFAULT (strftime('%s','now')), "
                + COL_TASK_OWNER_TYPE + " TEXT NOT NULL DEFAULT '" + OWNER_TYPE_GUEST + "', "
                + COL_TASK_OWNER_ID + " TEXT NOT NULL DEFAULT '" + OWNER_ID_GUEST_LOCAL + "', "
                + COL_TASK_UPDATED_AT + " INTEGER DEFAULT (strftime('%s','now')), "
                + COL_TASK_SYNC_STATE + " TEXT DEFAULT '" + SYNC_STATE_SYNCED + "', "
                + COL_TASK_DELETED_AT + " INTEGER DEFAULT 0, "
                + COL_TASK_CALENDAR_EVENT_ID + " INTEGER DEFAULT -1, "
                + COL_TASK_REMINDERS + " TEXT, "
                + "FOREIGN KEY (" + COL_TASK_LIST_ID + ") REFERENCES "
                + TABLE_LISTS + "(" + COL_LIST_ID + ")"
                + ")");

        createIndexes(db);

        // Seed dữ liệu mặc định (các danh sách)
        seedDefaultLists(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 11) {
            migrateToVersion11(db);
        }
        if (oldVersion < 12) {
            migrateToVersion12(db);
        }
    }

    private void seedDefaultLists(SQLiteDatabase db) {
        String[] lists = { "Hôm nay", "Ngày mai", "7 ngày tới", "Hộp thư đến", "Work", "Personal",
                "Shopping", "Learning", "Wish List", "Fitness" };
        String[] icons = { "ic_today", "ic_quick_tomorrow", "ic_quick_next_week", "ic_inbox", "ic_work", "ic_personal",
                "ic_shopping", "ic_learning", "ic_wishlist", "ic_fitness" };

        for (int i = 0; i < lists.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put(COL_LIST_NAME, lists[i]);
            cv.put(COL_LIST_ICON, icons[i]);
            cv.put(COL_LIST_ORDER, i);
            cv.put(COL_LIST_OWNER_TYPE, i < 4 ? SYSTEM_OWNER_TYPE : OWNER_TYPE_GUEST);
            cv.put(COL_LIST_OWNER_ID, i < 4 ? SYSTEM_OWNER_ID : OWNER_ID_GUEST_LOCAL);
            cv.put(COL_LIST_UPDATED_AT, nowSeconds());
            cv.put(COL_LIST_SYNC_STATE, SYNC_STATE_SYNCED);
            cv.put(COL_LIST_DELETED_AT, 0);
            db.insert(TABLE_LISTS, null, cv);
        }

        // Seed sample tasks
        long todayMillis = System.currentTimeMillis();
        long yesterdayMillis = todayMillis - (24 * 60 * 60 * 1000L);
        long tomorrowMillis = todayMillis + (24 * 60 * 60 * 1000L);

        insertTaskDirect(db, "Test", "", 1, "Hôm nay", todayMillis, false, false, ""); // Hôm nay
        insertTaskDirect(db, "Overdue Sample", "", 1, "Hôm qua", yesterdayMillis, false, false, ""); // Quá hạn
        insertTaskDirect(db, "Test Work", "", 5, "", -1, false, false, ""); // Work (ID=5 do 2 system list mới đẩy)
        insertTaskDirect(db, "Test Tomorrow", "", 5, "Ngày mai", tomorrowMillis, false, false, "");
    }

    private void insertTaskDirect(SQLiteDatabase db, String title, String description,
            int listId, String dateTag, long dueDateMillis, boolean completed, boolean isPinned, String reminders) {
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, title);
        cv.put(COL_TASK_DESCRIPTION, description);
        cv.put(COL_TASK_LIST_ID, listId);
        cv.put(COL_TASK_DATE_TAG, dateTag);
        cv.put(COL_TASK_DUE_DATE, dueDateMillis);
        cv.put(COL_TASK_COMPLETED, completed ? 1 : 0);
        cv.put(COL_TASK_IS_PINNED, isPinned ? 1 : 0);
        cv.put(COL_TASK_REMINDERS, reminders);
        cv.put(COL_TASK_OWNER_TYPE, OWNER_TYPE_GUEST);
        cv.put(COL_TASK_OWNER_ID, OWNER_ID_GUEST_LOCAL);
        cv.put(COL_TASK_UPDATED_AT, nowSeconds());
        cv.put(COL_TASK_SYNC_STATE, SYNC_STATE_SYNCED);
        cv.put(COL_TASK_DELETED_AT, 0);
        db.insert(TABLE_TASKS, null, cv);
    }

    // ═══════════════════════════════════════
    // CRUD Operations
    // ═══════════════════════════════════════

    /** Lấy tất cả tasks theo list_id */
    public List<TaskModel> getTasksByListId(int listId) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();

        Cursor cursor = db.query(TABLE_TASKS, null,
                combineSelection(COL_TASK_LIST_ID + " = ?", taskOwnerFilter()),
                appendArgs(new String[] { String.valueOf(listId) }, owner.ownerType, owner.ownerId),
                null, null,
                COL_TASK_IS_PINNED + " DESC, " + COL_TASK_COMPLETED + " ASC, " + COL_TASK_CREATED + " DESC");

        while (cursor.moveToNext()) {
            TaskModel task = new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_IS_PINNED)) == 1,
                    stringToList(cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_REMINDERS))),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_CALENDAR_EVENT_ID)));
            tasks.add(task);
        }
        cursor.close();
        return tasks;
    }

    /** Lấy tất cả tasks quá hạn và hôm nay */
    public List<TaskModel> getTodayAndOverdueTasks(long startOfTodayMillis, long endOfTodayMillis) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();

        // Lấy tất cả Task:
        // 1. Quá hạn: due_date_millis > 0 AND due_date_millis < startOfTodayMillis AND
        // chưa hoàn thành
        // 2. Hôm nay: due_date_millis >= startOfTodayMillis AND due_date_millis <=
        // endOfTodayMillis AND chưa hoàn thành
        // (Hoặc date_tag = 'Hôm nay' phòng trường hợp task cũ)
        String selection = "((" + COL_TASK_DUE_DATE + " > 0 AND " + COL_TASK_DUE_DATE + " < ?) OR " +
                "(" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " = 'Hôm nay'))";

        String[] selectionArgs = new String[] {
                String.valueOf(startOfTodayMillis),
                String.valueOf(startOfTodayMillis),
                String.valueOf(endOfTodayMillis)
        };

        Cursor cursor = db.query(TABLE_TASKS, null,
                combineSelection(selection, taskOwnerFilter()),
                appendArgs(selectionArgs, owner.ownerType, owner.ownerId), null, null,
                COL_TASK_IS_PINNED + " DESC, " + COL_TASK_DUE_DATE + " ASC");

        while (cursor.moveToNext()) {
            TaskModel task = new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_IS_PINNED)) == 1,
                    stringToList(cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_REMINDERS))),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_CALENDAR_EVENT_ID)));
            tasks.add(task);
        }
        cursor.close();
        return tasks;
    }

    /** Lấy chính xác tasks của hôm nay (không lấy quá hạn) */
    public List<TaskModel> getStrictlyTodayTasks(long startOfTodayMillis, long endOfTodayMillis) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Chỉ lấy của hôm nay: due_date_millis >= startOfTodayMillis AND due_date_millis <= endOfTodayMillis
        // AND chưa hoàn thành (COL_TASK_COMPLETED = 0)
        String selection = "((" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR (" + COL_TASK_DATE_TAG + " = 'Hôm nay')) AND " + COL_TASK_COMPLETED + " = 0";

        String[] selectionArgs = new String[] {
                String.valueOf(startOfTodayMillis),
                String.valueOf(endOfTodayMillis)
        };

        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null,
                COL_TASK_IS_PINNED + " DESC, " + COL_TASK_DUE_DATE + " ASC");

        while (cursor.moveToNext()) {
            TaskModel task = new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_IS_PINNED)) == 1,
                    stringToList(cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_REMINDERS))),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_CALENDAR_EVENT_ID)));
            tasks.add(task);
        }
        cursor.close();
        return tasks;
    }

    /** Lấy một task theo task_id */
    public TaskModel getTaskById(int taskId) {
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();
        Cursor cursor = db.query(TABLE_TASKS, null,
                combineSelection(COL_TASK_ID + " = ?", taskOwnerFilter()),
                appendArgs(new String[] { String.valueOf(taskId) }, owner.ownerType, owner.ownerId),
                null, null, null);

        TaskModel task = null;
        if (cursor.moveToFirst()) {
            task = new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_IS_PINNED)) == 1,
                    stringToList(cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_REMINDERS))),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_CALENDAR_EVENT_ID)));
        }
        cursor.close();
        return task;
    }

    /** Lấy tất cả tasks của ngày mai (không có quá hạn) */
    public List<TaskModel> getTomorrowTasks(long startOfTomorrowMillis, long endOfTomorrowMillis) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();

        // 1. Chỉ của ngày mai: due_date_millis >= startOfTomorrowMillis AND
        // due_date_millis <= endOfTomorrowMillis AND chưa hoàn thành
        // (Hoặc date_tag = 'Ngày mai' phòng trường hợp task cũ)
        String selection = "((" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " = 'Ngày mai'))";

        String[] selectionArgs = new String[] {
                String.valueOf(startOfTomorrowMillis),
                String.valueOf(endOfTomorrowMillis)
        };

        Cursor cursor = db.query(TABLE_TASKS, null,
                combineSelection(selection, taskOwnerFilter()),
                appendArgs(selectionArgs, owner.ownerType, owner.ownerId), null, null,
                COL_TASK_IS_PINNED + " DESC, " + COL_TASK_DUE_DATE + " ASC");

        while (cursor.moveToNext()) {
            TaskModel task = new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_IS_PINNED)) == 1,
                    stringToList(cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_REMINDERS))),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_CALENDAR_EVENT_ID)));
            tasks.add(task);
        }
        cursor.close();
        return tasks;
    }

    /** Lấy tất cả tasks của 7 ngày tới (không lấy quá hạn) */
    public List<TaskModel> getNext7DaysTasks(long startOfNext7DaysMillis, long endOfNext7DaysMillis) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();

        // Chỉ lấy trong 7 ngày tới
        String selection = "((" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " = '7 ngày tới'))";

        String[] selectionArgs = new String[] {
                String.valueOf(startOfNext7DaysMillis),
                String.valueOf(endOfNext7DaysMillis)
        };

        Cursor cursor = db.query(TABLE_TASKS, null,
                combineSelection(selection, taskOwnerFilter()),
                appendArgs(selectionArgs, owner.ownerType, owner.ownerId), null, null,
                COL_TASK_IS_PINNED + " DESC, " + COL_TASK_DUE_DATE + " ASC");

        while (cursor.moveToNext()) {
            TaskModel task = new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_IS_PINNED)) == 1,
                    stringToList(cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_REMINDERS))),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_CALENDAR_EVENT_ID)));
            tasks.add(task);
        }
        cursor.close();
        return tasks;
    }

    /**
     * Lấy tất cả tasks cho màn hình "7 ngày tới" (Gồm Quá hạn, Hôm nay, Ngày mai, 7
     * ngày tới)
     */
    public List<TaskModel> getAllTasksFor7DaysView(long startOfTodayMillis, long endOfNext7DaysMillis) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();

        // Lấy tất cả Task:
        // 1. Quá hạn: due_date_millis > 0 AND due_date_millis < startOfTodayMillis
        // 2. Trong 7 ngày: due_date_millis >= startOfTodayMillis AND due_date_millis <=
        // endOfNext7DaysMillis
        // 3. Các task cũ chưa có millis nhưng có date_tag hợp lệ
        String selection = "((" + COL_TASK_DUE_DATE + " > 0 AND " + COL_TASK_DUE_DATE + " < ?) OR " +
                "(" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " IN ('Hôm nay', 'Ngày mai', '7 ngày tới', 'Hôm qua')))";

        String[] selectionArgs = new String[] {
                String.valueOf(startOfTodayMillis),
                String.valueOf(startOfTodayMillis),
                String.valueOf(endOfNext7DaysMillis)
        };

        Cursor cursor = db.query(TABLE_TASKS, null,
                combineSelection(selection, taskOwnerFilter()),
                appendArgs(selectionArgs, owner.ownerType, owner.ownerId), null, null,
                COL_TASK_IS_PINNED + " DESC, " + COL_TASK_DUE_DATE + " ASC");

        while (cursor.moveToNext()) {
            TaskModel task = new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_IS_PINNED)) == 1,
                    stringToList(cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_REMINDERS))),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_CALENDAR_EVENT_ID)));
            tasks.add(task);
        }
        cursor.close();
        return tasks;
    }

    /** Đếm số task (chưa hoàn thành) theo list_id */
    public int getTaskCountByListId(int listId) {
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_TASKS
                        + " WHERE " + COL_TASK_LIST_ID + " = ? AND "
                        + COL_TASK_COMPLETED + " = 0 AND "
                        + COL_TASK_OWNER_TYPE + " = ? AND " + COL_TASK_OWNER_ID + " = ? AND " + COL_TASK_DELETED_AT + " = 0",
                new String[] { String.valueOf(listId), owner.ownerType, owner.ownerId });
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /** Lấy badge count cho TẤT CẢ lists (dùng cho Drawer) */
    public Map<Integer, Integer> getAllListTaskCounts() {
        Map<Integer, Integer> counts = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_TASK_LIST_ID + ", COUNT(*) FROM " + TABLE_TASKS
                        + " WHERE " + COL_TASK_COMPLETED + " = 0"
                        + " AND " + COL_TASK_OWNER_TYPE + " = ? AND " + COL_TASK_OWNER_ID + " = ?"
                        + " AND " + COL_TASK_DELETED_AT + " = 0"
                        + " GROUP BY " + COL_TASK_LIST_ID,
                new String[] { owner.ownerType, owner.ownerId });
        while (cursor.moveToNext()) {
            int listId = cursor.getInt(0);
            if (listId > 3) {
                counts.put(listId, cursor.getInt(1));
            }
        }
        cursor.close();

        // --- TÍNH TOÁN SMART LIST (ID 1, 2, 3) ---
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfToday = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long endOfToday = cal.getTimeInMillis() - 1;

        long startOfTomorrow = endOfToday + 1;
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long endOfTomorrow = cal.getTimeInMillis() - 1;

        cal.add(Calendar.DAY_OF_MONTH, 6);
        long endOfNext7Days = cal.getTimeInMillis() - 1;

        // 1. Hôm nay & Quá hạn
        String todaySelection = "((" + COL_TASK_DUE_DATE + " > 0 AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " = 'Hôm nay')) AND " + COL_TASK_COMPLETED + " = 0";
        String[] todayArgs = { String.valueOf(endOfToday) };

        Cursor cursorToday = db.query(TABLE_TASKS, new String[] { "COUNT(*)" },
                combineSelection(todaySelection, taskOwnerFilter()),
                appendArgs(todayArgs, owner.ownerType, owner.ownerId), null, null,
                null);
        if (cursorToday.moveToFirst())
            counts.put(1, cursorToday.getInt(0));
        cursorToday.close();

        // 2. Ngày mai (không lấy quá hạn)
        String tomorrowSelection = "((" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " = 'Ngày mai')) AND " + COL_TASK_COMPLETED + " = 0";
        String[] tomorrowArgs = { String.valueOf(startOfTomorrow), String.valueOf(endOfTomorrow) };

        Cursor cursorTomorrow = db.query(TABLE_TASKS, new String[] { "COUNT(*)" },
                combineSelection(tomorrowSelection, taskOwnerFilter()),
                appendArgs(tomorrowArgs, owner.ownerType, owner.ownerId),
                null, null, null);
        if (cursorTomorrow.moveToFirst())
            counts.put(2, cursorTomorrow.getInt(0));
        cursorTomorrow.close();

        // 3. 7 ngày tới (không lấy quá hạn - nhưng BAO GỒM Hôm nay và Ngày mai)
        String next7DaysSelection = "((" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " IN ('Hôm nay', 'Ngày mai', '7 ngày tới'))) AND " + COL_TASK_COMPLETED
                + " = 0";
        String[] next7DaysArgs = { String.valueOf(startOfToday), String.valueOf(endOfNext7Days) };

        Cursor cursorNext7 = db.query(TABLE_TASKS, new String[] { "COUNT(*)" },
                combineSelection(next7DaysSelection, taskOwnerFilter()),
                appendArgs(next7DaysArgs, owner.ownerType, owner.ownerId), null,
                null, null);
        if (cursorNext7.moveToFirst())
            counts.put(3, cursorNext7.getInt(0));
        cursorNext7.close();

        return counts;
    }

    /** Thêm một task mới */
    public long insertTask(String title, String description, int listId, String dateTag, long dueDateMillis, List<String> reminders) {
        SQLiteDatabase db = getWritableDatabase();
        OwnerScope owner = resolveOwnerScope();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, title);
        cv.put(COL_TASK_DESCRIPTION, description);
        cv.put(COL_TASK_LIST_ID, listId);
        cv.put(COL_TASK_DATE_TAG, dateTag);
        cv.put(COL_TASK_DUE_DATE, dueDateMillis);
        cv.put(COL_TASK_COMPLETED, 0);
        cv.put(COL_TASK_IS_PINNED, 0);
        cv.put(COL_TASK_REMINDERS, listToString(reminders));
        cv.put(COL_TASK_OWNER_TYPE, owner.ownerType);
        cv.put(COL_TASK_OWNER_ID, owner.ownerId);
        cv.put(COL_TASK_UPDATED_AT, nowSeconds());
        cv.put(COL_TASK_SYNC_STATE, SYNC_STATE_PENDING_CREATE);
        cv.put(COL_TASK_DELETED_AT, 0);
        return db.insert(TABLE_TASKS, null, cv);
    }

    /** Thêm danh sách mới (Custom List) */
    public long insertList(String name, String iconName) {
        SQLiteDatabase db = getWritableDatabase();
        OwnerScope owner = resolveOwnerScope();
        ContentValues cv = new ContentValues();
        cv.put(COL_LIST_NAME, name);
        cv.put(COL_LIST_ICON, iconName);
        cv.put(COL_LIST_ORDER, 0); // Đặt Item này lên đầu danh sách Custom
        cv.put(COL_LIST_OWNER_TYPE, owner.ownerType);
        cv.put(COL_LIST_OWNER_ID, owner.ownerId);
        cv.put(COL_LIST_UPDATED_AT, nowSeconds());
        cv.put(COL_LIST_SYNC_STATE, SYNC_STATE_PENDING_CREATE);
        cv.put(COL_LIST_DELETED_AT, 0);
        return db.insert(TABLE_LISTS, null, cv);
    }

    /** Cập nhật danh sách (Custom List) */
    public void updateList(int listId, String newName, String newIconName) {
        if (listId <= 4) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        OwnerScope owner = resolveOwnerScope();
        ContentValues cv = new ContentValues();
        cv.put(COL_LIST_NAME, newName);
        cv.put(COL_LIST_ICON, newIconName);
        cv.put(COL_LIST_UPDATED_AT, nowSeconds());
        cv.put(COL_LIST_SYNC_STATE, SYNC_STATE_PENDING_UPDATE);
        db.update(TABLE_LISTS, cv,
                COL_LIST_ID + " = ? AND " + COL_LIST_OWNER_TYPE + " = ? AND " + COL_LIST_OWNER_ID + " = ? AND " + COL_LIST_DELETED_AT + " = 0",
                new String[] { String.valueOf(listId), owner.ownerType, owner.ownerId });
    }

    /** Cập nhật lại số thứ tự danh sách do người dùng kéo thả dựa theo tên */
    public void updateListOrder(List<String> orderedNames) {
        SQLiteDatabase db = getWritableDatabase();
        OwnerScope owner = resolveOwnerScope();
        db.beginTransaction();
        try {
            for (int i = 0; i < orderedNames.size(); i++) {
                ContentValues cv = new ContentValues();
                cv.put(COL_LIST_ORDER, i);
                cv.put(COL_LIST_UPDATED_AT, nowSeconds());
                cv.put(COL_LIST_SYNC_STATE, SYNC_STATE_PENDING_UPDATE);
                db.update(TABLE_LISTS, cv,
                        COL_LIST_NAME + " = ? AND " + listCustomOwnerFilter(),
                        appendArgs(new String[] { orderedNames.get(i) }, owner.ownerType, owner.ownerId));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Cập nhật trạng thái completed */
    public void updateTaskCompleted(int taskId, boolean completed) {
        SQLiteDatabase db = getWritableDatabase();
        OwnerScope owner = resolveOwnerScope();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_COMPLETED, completed ? 1 : 0);
        cv.put(COL_TASK_UPDATED_AT, nowSeconds());
        cv.put(COL_TASK_SYNC_STATE, SYNC_STATE_PENDING_UPDATE);
        db.update(TABLE_TASKS, cv,
                COL_TASK_ID + " = ? AND " + taskOwnerFilter(),
                appendArgs(new String[] { String.valueOf(taskId) }, owner.ownerType, owner.ownerId));
    }

    /** Cập nhật trạng thái ghim của task */
    public void updateTaskPinned(int taskId, boolean isPinned) {
        SQLiteDatabase db = getWritableDatabase();
        OwnerScope owner = resolveOwnerScope();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_IS_PINNED, isPinned ? 1 : 0);
        cv.put(COL_TASK_UPDATED_AT, nowSeconds());
        cv.put(COL_TASK_SYNC_STATE, SYNC_STATE_PENDING_UPDATE);
        db.update(TABLE_TASKS, cv,
                COL_TASK_ID + " = ? AND " + taskOwnerFilter(),
                appendArgs(new String[] { String.valueOf(taskId) }, owner.ownerType, owner.ownerId));
    }

    /** Cập nhật toàn bộ thông tin cơ bản của task */
    public void updateTaskDetails(int taskId, String title, String description) {
        SQLiteDatabase db = getWritableDatabase();
        OwnerScope owner = resolveOwnerScope();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, title);
        cv.put(COL_TASK_DESCRIPTION, description);
        cv.put(COL_TASK_UPDATED_AT, nowSeconds());
        cv.put(COL_TASK_SYNC_STATE, SYNC_STATE_PENDING_UPDATE);
        db.update(TABLE_TASKS, cv,
                COL_TASK_ID + " = ? AND " + taskOwnerFilter(),
                appendArgs(new String[] { String.valueOf(taskId) }, owner.ownerType, owner.ownerId));
    }

    /** Cập nhật ngày và lời nhắc của task */
    public void updateTaskDate(int taskId, String dateTag, long dueDateMillis, List<String> reminders) {
        SQLiteDatabase db = getWritableDatabase();
        OwnerScope owner = resolveOwnerScope();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_DATE_TAG, dateTag);
        cv.put(COL_TASK_DUE_DATE, dueDateMillis);
        cv.put(COL_TASK_REMINDERS, listToString(reminders));
        cv.put(COL_TASK_UPDATED_AT, nowSeconds());
        cv.put(COL_TASK_SYNC_STATE, SYNC_STATE_PENDING_UPDATE);
        db.update(TABLE_TASKS, cv,
                COL_TASK_ID + " = ? AND " + taskOwnerFilter(),
                appendArgs(new String[] { String.valueOf(taskId) }, owner.ownerType, owner.ownerId));
    }

    /** Cập nhật thời gian nhắc nhở của Task */
    public void updateTaskDueDate(int taskId, long newDueDateMillis) {
        SQLiteDatabase db = getWritableDatabase();
        OwnerScope owner = resolveOwnerScope();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_DUE_DATE, newDueDateMillis);
        cv.put(COL_TASK_UPDATED_AT, nowSeconds());
        cv.put(COL_TASK_SYNC_STATE, SYNC_STATE_PENDING_UPDATE);
        db.update(TABLE_TASKS, cv,
                COL_TASK_ID + " = ? AND " + taskOwnerFilter(),
                appendArgs(new String[] { String.valueOf(taskId) }, owner.ownerType, owner.ownerId));
    }

    /** Cập nhật ID sự kiện trên Google Calendar */
    public void updateTaskCalendarEventId(int taskId, long calendarEventId) {
        SQLiteDatabase db = getWritableDatabase();
        OwnerScope owner = resolveOwnerScope();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_CALENDAR_EVENT_ID, calendarEventId);
        // Do not update sync state to PENDING_UPDATE or UPDATED_AT, 
        // as this field is local to the device's calendar integration.
        db.update(TABLE_TASKS, cv,
                COL_TASK_ID + " = ? AND " + taskOwnerFilter(),
                appendArgs(new String[] { String.valueOf(taskId) }, owner.ownerType, owner.ownerId));
    }

    /** Di chuyển task sang một danh sách khác */
    public void moveTaskToList(int taskId, int newListId) {
        SQLiteDatabase db = getWritableDatabase();
        OwnerScope owner = resolveOwnerScope();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_LIST_ID, newListId);
        cv.put(COL_TASK_UPDATED_AT, nowSeconds());
        cv.put(COL_TASK_SYNC_STATE, SYNC_STATE_PENDING_UPDATE);
        db.update(TABLE_TASKS, cv,
                COL_TASK_ID + " = ? AND " + taskOwnerFilter(),
                appendArgs(new String[] { String.valueOf(taskId) }, owner.ownerType, owner.ownerId));
    }

    /** Xóa task */
    public void deleteTask(int taskId) {
        SQLiteDatabase db = getWritableDatabase();
        OwnerScope owner = resolveOwnerScope();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_DELETED_AT, nowSeconds());
        cv.put(COL_TASK_UPDATED_AT, nowSeconds());
        cv.put(COL_TASK_SYNC_STATE, SYNC_STATE_PENDING_DELETE);
        db.update(TABLE_TASKS, cv,
                COL_TASK_ID + " = ? AND " + taskOwnerFilter(),
                appendArgs(new String[] { String.valueOf(taskId) }, owner.ownerType, owner.ownerId));
    }

    /** Xóa danh sách và toàn bộ Tasks nằm trong danh sách đó */
    public void deleteList(int listId) {
        if (listId <= 4) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        OwnerScope owner = resolveOwnerScope();
        db.beginTransaction();
        try {
            ContentValues taskCv = new ContentValues();
            taskCv.put(COL_TASK_DELETED_AT, nowSeconds());
            taskCv.put(COL_TASK_UPDATED_AT, nowSeconds());
            taskCv.put(COL_TASK_SYNC_STATE, SYNC_STATE_PENDING_DELETE);
            db.update(TABLE_TASKS, taskCv,
                    COL_TASK_LIST_ID + " = ? AND " + taskOwnerFilter(),
                    appendArgs(new String[] { String.valueOf(listId) }, owner.ownerType, owner.ownerId));

            ContentValues listCv = new ContentValues();
            listCv.put(COL_LIST_DELETED_AT, nowSeconds());
            listCv.put(COL_LIST_UPDATED_AT, nowSeconds());
            listCv.put(COL_LIST_SYNC_STATE, SYNC_STATE_PENDING_DELETE);
            db.update(TABLE_LISTS, listCv,
                    COL_LIST_ID + " = ? AND " + COL_LIST_OWNER_TYPE + " = ? AND " + COL_LIST_OWNER_ID + " = ? AND " + COL_LIST_DELETED_AT + " = 0",
                    new String[] { String.valueOf(listId), owner.ownerType, owner.ownerId });
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Lấy list_id theo tên danh sách */
    public int getListIdByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();
        Cursor cursor = db.query(TABLE_LISTS, new String[] { COL_LIST_ID },
                "(" + COL_LIST_NAME + " = ?) AND (((" + COL_LIST_ID + " <= 4) OR (" + COL_LIST_OWNER_TYPE + " = ? AND " + COL_LIST_OWNER_ID + " = ?)) AND " + COL_LIST_DELETED_AT + " = 0)",
                new String[] { name, owner.ownerType, owner.ownerId },
                null, null, null);
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    /** Lấy tên danh sách theo list_id */
    public String getListNameById(int listId) {
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();
        Cursor cursor = db.query(TABLE_LISTS, new String[] { COL_LIST_NAME },
                COL_LIST_ID + " = ? AND ((" + COL_LIST_ID + " <= 4) OR (" + COL_LIST_OWNER_TYPE + " = ? AND " + COL_LIST_OWNER_ID + " = ?)) AND " + COL_LIST_DELETED_AT + " = 0",
                new String[] { String.valueOf(listId), owner.ownerType, owner.ownerId },
                null, null, null);
        String name = "";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    /** Lấy emoji của danh sách theo list_id */
    public String getListEmojiById(int listId) {
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();
        Cursor cursor = db.query(TABLE_LISTS, new String[] { COL_LIST_ICON },
                COL_LIST_ID + " = ? AND ((" + COL_LIST_ID + " <= 4) OR (" + COL_LIST_OWNER_TYPE + " = ? AND " + COL_LIST_OWNER_ID + " = ?)) AND " + COL_LIST_DELETED_AT + " = 0",
                new String[] { String.valueOf(listId), owner.ownerType, owner.ownerId },
                null, null, null);
        String emoji = "";
        if (cursor.moveToFirst()) {
            emoji = cursor.getString(0);
        }
        cursor.close();
        return emoji;
    }

    /** Đổi trạng thái ghim của danh sách */
    public void togglePinList(int listId, boolean isPinned) {
        if (listId <= 4) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        OwnerScope owner = resolveOwnerScope();
        ContentValues cv = new ContentValues();
        cv.put(COL_LIST_IS_PINNED, isPinned ? 1 : 0);
        cv.put(COL_LIST_UPDATED_AT, nowSeconds());
        cv.put(COL_LIST_SYNC_STATE, SYNC_STATE_PENDING_UPDATE);
        db.update(TABLE_LISTS, cv,
                COL_LIST_ID + " = ? AND " + COL_LIST_OWNER_TYPE + " = ? AND " + COL_LIST_OWNER_ID + " = ? AND " + COL_LIST_DELETED_AT + " = 0",
                new String[] { String.valueOf(listId), owner.ownerType, owner.ownerId });
    }

    /** Kiểm tra xem sách có đang được ghim hay không */
    public boolean isListPinned(int listId) {
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();
        Cursor cursor = db.query(TABLE_LISTS, new String[] { COL_LIST_IS_PINNED },
                COL_LIST_ID + " = ? AND ((" + COL_LIST_ID + " <= 4) OR (" + COL_LIST_OWNER_TYPE + " = ? AND " + COL_LIST_OWNER_ID + " = ?)) AND " + COL_LIST_DELETED_AT + " = 0",
                new String[] { String.valueOf(listId), owner.ownerType, owner.ownerId },
                null, null, null);
        boolean isPinned = false;
        if (cursor.moveToFirst()) {
            isPinned = cursor.getInt(0) == 1;
        }
        cursor.close();
        return isPinned;
    }

    /** Lấy tất cả danh sách đang được đính ghim */
    public List<DrawerMenuItem> getPinnedLists() {
        List<DrawerMenuItem> pinnedLists = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();
        Cursor cursor = db.query(TABLE_LISTS, new String[] { COL_LIST_ID, COL_LIST_NAME, COL_LIST_ICON },
                COL_LIST_IS_PINNED + " = 1 AND " + listCustomOwnerFilter(),
                new String[] { owner.ownerType, owner.ownerId },
                null, null, COL_LIST_ORDER + " ASC, " + COL_LIST_ID + " ASC");

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String listName = cursor.getString(1);
            String iconName = cursor.getString(2);

            if (iconName != null && iconName.startsWith("ic_")) {
                int resId = 0; // MainActivity sẽ resolve logic này
                pinnedLists.add(new DrawerMenuItem(id, listName, resId, DrawerMenuItem.ItemType.LIST));
            } else {
                pinnedLists.add(new DrawerMenuItem(id, listName, iconName, DrawerMenuItem.ItemType.LIST));
            }
        }
        cursor.close();
        return pinnedLists;
    }

    /** Lấy danh sách các List hợp lệ (ID >= 4) cho chức năng Move To */
    public List<ListModel> getMoveToOptions() {
        List<ListModel> lists = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();

        // Chỉ lấy Hộp thư đến (ID=4, shared) và custom list theo owner hiện tại
        Cursor cursor = db.query(TABLE_LISTS,
                new String[] { COL_LIST_ID, COL_LIST_NAME, COL_LIST_ICON },
                "(" + COL_LIST_ID + " = 4 AND " + COL_LIST_DELETED_AT + " = 0) OR (" + listCustomOwnerFilter() + ")",
                new String[] { owner.ownerType, owner.ownerId }, null, null, COL_LIST_ORDER + " ASC, " + COL_LIST_ID + " ASC");

        while (cursor.moveToNext()) {
            lists.add(new ListModel(
                    cursor.getInt(0), // ID
                    cursor.getString(1), // Name
                    cursor.getString(2) // Icon
            ));
        }
        cursor.close();
        return lists;
    }

    public int getListIconResId(Context context, int listId) {
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();
        Cursor cursor = db.query(TABLE_LISTS, new String[] { COL_LIST_ICON },
                COL_LIST_ID + " = ? AND ((" + COL_LIST_ID + " <= 4) OR (" + COL_LIST_OWNER_TYPE + " = ? AND " + COL_LIST_OWNER_ID + " = ?)) AND " + COL_LIST_DELETED_AT + " = 0",
                new String[] { String.valueOf(listId), owner.ownerType, owner.ownerId },
                null, null, null);
        int resId = 0;
        if (cursor.moveToFirst()) {
            String iconName = cursor.getString(0); // vd: "ic_work"
            resId = context.getResources().getIdentifier(
                    iconName, "drawable", context.getPackageName());
        }
        cursor.close();
        return resId;
    }

    /**
     * Lấy danh sách icon name cho các list (dùng để load DrawerItems động) -> trả
     * về Map<Name, IconName>
     */
    public List<DrawerMenuItem> getAllCustomLists() {
        List<DrawerMenuItem> lists = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();
        // Skip list_id 1 đến 4 ("Hôm nay", "Ngày mai", "7 ngày tới", "Hộp thư đến")
        Cursor cursor = db.query(TABLE_LISTS, new String[] { COL_LIST_ID, COL_LIST_NAME, COL_LIST_ICON },
                listCustomOwnerFilter(), new String[] { owner.ownerType, owner.ownerId },
                null, null, COL_LIST_ORDER + " ASC, " + COL_LIST_ID + " ASC");

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String listName = cursor.getString(1);
            String iconName = cursor.getString(2);

            if (iconName != null && iconName.startsWith("ic_")) {
                lists.add(new DrawerMenuItem(id, listName, 0, DrawerMenuItem.ItemType.LIST));
            } else {
                lists.add(new DrawerMenuItem(id, listName, iconName, DrawerMenuItem.ItemType.LIST));
            }
        }
        cursor.close();
        return lists;
    }

    // Thêm vào TaskDatabaseHelper.java
    public List<TaskModel> getUpcomingTasks() {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();
        long now = System.currentTimeMillis();

        // Lấy các task chưa hoàn thành và có ngày đến hạn trong tương lai
        String selection = COL_TASK_COMPLETED + " = 0 AND " + COL_TASK_DUE_DATE + " > ?";
        String[] selectionArgs = { String.valueOf(now) };

        Cursor cursor = db.query(TABLE_TASKS, null,
                combineSelection(selection, taskOwnerFilter()),
                appendArgs(selectionArgs, owner.ownerType, owner.ownerId),
                null, null, null);
        while (cursor.moveToNext()) {
            tasks.add(new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_IS_PINNED)) == 1,
                    stringToList(cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_REMINDERS))),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_CALENDAR_EVENT_ID))));
        }
        cursor.close();
        return tasks;
    }

    public List<TaskModel> searchTasks(String query) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();

        String selection = COL_TASK_TITLE + " LIKE ? OR " + COL_TASK_DESCRIPTION + " LIKE ?";
        String[] selectionArgs = new String[] { "%" + query + "%", "%" + query + "%" };

        Cursor cursor = db.query(TABLE_TASKS, null,
                combineSelection(selection, taskOwnerFilter()),
                appendArgs(selectionArgs, owner.ownerType, owner.ownerId), null, null,
                COL_TASK_COMPLETED + " ASC, " + COL_TASK_CREATED + " DESC");

        while (cursor.moveToNext()) {
            tasks.add(new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_IS_PINNED)) == 1,
                    stringToList(cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_REMINDERS))),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_CALENDAR_EVENT_ID))));
        }
        cursor.close();
        return tasks;
    }

    public List<DrawerMenuItem> searchLists(String query) {
        List<DrawerMenuItem> lists = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();

        String selection = combineSelection(COL_LIST_NAME + " LIKE ?", listCustomOwnerFilter());
        String[] selectionArgs = appendArgs(new String[] { "%" + query + "%" }, owner.ownerType, owner.ownerId);

        Cursor cursor = db.query(TABLE_LISTS, new String[] { COL_LIST_ID, COL_LIST_NAME, COL_LIST_ICON },
                selection, selectionArgs, null, null, COL_LIST_ORDER + " ASC, " + COL_LIST_ID + " ASC");

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String listName = cursor.getString(1);
            String iconName = cursor.getString(2);

            if (iconName != null && iconName.startsWith("ic_")) {
                lists.add(new DrawerMenuItem(id, listName, 0, DrawerMenuItem.ItemType.LIST));
            } else {
                lists.add(new DrawerMenuItem(id, listName, iconName, DrawerMenuItem.ItemType.LIST));
            }
        }
        cursor.close();
        return lists;
    }

    /** Lấy tất cả task IDs (dùng cho cancelAllReminders) */
    public List<Integer> getAllTaskIds() {
        List<Integer> ids = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, new String[]{COL_TASK_ID},
                COL_TASK_DELETED_AT + " = 0", null, null, null, null);
        while (cursor.moveToNext()) {
            ids.add(cursor.getInt(0));
        }
        cursor.close();
        return ids;
    }

    // ═══════════════════════════════════════
    // SYNC Operations
    // ═══════════════════════════════════════

    /** Lấy tất cả lists có sync_state != SYNCED cho owner hiện tại */
    public List<ContentValues> getPendingLists() {
        List<ContentValues> results = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();

        Cursor cursor = db.query(TABLE_LISTS, null,
                COL_LIST_SYNC_STATE + " != ? AND " + COL_LIST_OWNER_TYPE + " = ? AND " + COL_LIST_OWNER_ID + " = ?",
                new String[]{SYNC_STATE_SYNCED, owner.ownerType, owner.ownerId},
                null, null, null);

        while (cursor.moveToNext()) {
            ContentValues cv = new ContentValues();
            cv.put(COL_LIST_ID, cursor.getInt(cursor.getColumnIndexOrThrow(COL_LIST_ID)));
            cv.put(COL_LIST_NAME, cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_NAME)));
            cv.put(COL_LIST_ICON, cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_ICON)));
            cv.put(COL_LIST_ORDER, cursor.getInt(cursor.getColumnIndexOrThrow(COL_LIST_ORDER)));
            cv.put(COL_LIST_IS_PINNED, cursor.getInt(cursor.getColumnIndexOrThrow(COL_LIST_IS_PINNED)));
            cv.put(COL_LIST_OWNER_TYPE, cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_OWNER_TYPE)));
            cv.put(COL_LIST_OWNER_ID, cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_OWNER_ID)));
            cv.put(COL_LIST_UPDATED_AT, cursor.getLong(cursor.getColumnIndexOrThrow(COL_LIST_UPDATED_AT)));
            cv.put(COL_LIST_SYNC_STATE, cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_SYNC_STATE)));
            cv.put(COL_LIST_DELETED_AT, cursor.getLong(cursor.getColumnIndexOrThrow(COL_LIST_DELETED_AT)));
            results.add(cv);
        }
        cursor.close();
        return results;
    }

    /** Lấy tất cả tasks có sync_state != SYNCED cho owner hiện tại */
    public List<ContentValues> getPendingTasks() {
        List<ContentValues> results = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        OwnerScope owner = resolveOwnerScope();

        Cursor cursor = db.query(TABLE_TASKS, null,
                COL_TASK_SYNC_STATE + " != ? AND " + COL_TASK_OWNER_TYPE + " = ? AND " + COL_TASK_OWNER_ID + " = ?",
                new String[]{SYNC_STATE_SYNCED, owner.ownerType, owner.ownerId},
                null, null, null);

        while (cursor.moveToNext()) {
            ContentValues cv = new ContentValues();
            cv.put(COL_TASK_ID, cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)));
            cv.put(COL_TASK_TITLE, cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)));
            cv.put(COL_TASK_DESCRIPTION, cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESCRIPTION)));
            cv.put(COL_TASK_LIST_ID, cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)));
            cv.put(COL_TASK_DATE_TAG, cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)));
            cv.put(COL_TASK_DUE_DATE, cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)));
            cv.put(COL_TASK_COMPLETED, cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)));
            cv.put(COL_TASK_IS_PINNED, cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_IS_PINNED)));
            cv.put(COL_TASK_REMINDERS, cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_REMINDERS)));
            cv.put(COL_TASK_CREATED, cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_CREATED)));
            cv.put(COL_TASK_OWNER_TYPE, cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_OWNER_TYPE)));
            cv.put(COL_TASK_OWNER_ID, cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_OWNER_ID)));
            cv.put(COL_TASK_UPDATED_AT, cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_UPDATED_AT)));
            cv.put(COL_TASK_SYNC_STATE, cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_SYNC_STATE)));
            cv.put(COL_TASK_DELETED_AT, cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DELETED_AT)));
            results.add(cv);
        }
        cursor.close();
        return results;
    }

    /** Đánh dấu list đã đồng bộ thành công */
    public void markListSynced(int listId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LIST_SYNC_STATE, SYNC_STATE_SYNCED);
        db.update(TABLE_LISTS, cv, COL_LIST_ID + " = ?", new String[]{String.valueOf(listId)});
    }

    /** Đánh dấu task đã đồng bộ thành công */
    public void markTaskSynced(int taskId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_SYNC_STATE, SYNC_STATE_SYNCED);
        db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    /** Xóa hẳn list đã bị soft-delete và đã sync thành công */
    public void purgeDeletedList(int listId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LISTS, COL_LIST_ID + " = ? AND " + COL_LIST_DELETED_AT + " > 0",
                new String[]{String.valueOf(listId)});
    }

    /** Xóa hẳn task đã bị soft-delete và đã sync thành công */
    public void purgeDeletedTask(int taskId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TASKS, COL_TASK_ID + " = ? AND " + COL_TASK_DELETED_AT + " > 0",
                new String[]{String.valueOf(taskId)});
    }

    /** Upsert list từ cloud (dùng cho pull). Nếu local đã có → cập nhật, nếu chưa → insert */
    public void upsertListFromCloud(int localId, String name, String iconName, int order,
                                     int isPinned, String ownerType, String ownerId,
                                     long updatedAt, long deletedAt) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(TABLE_LISTS, new String[]{COL_LIST_ID, COL_LIST_UPDATED_AT, COL_LIST_SYNC_STATE},
                COL_LIST_ID + " = ?", new String[]{String.valueOf(localId)}, null, null, null);

        ContentValues cv = new ContentValues();
        cv.put(COL_LIST_NAME, name);
        cv.put(COL_LIST_ICON, iconName);
        cv.put(COL_LIST_ORDER, order);
        cv.put(COL_LIST_IS_PINNED, isPinned);
        cv.put(COL_LIST_OWNER_TYPE, ownerType);
        cv.put(COL_LIST_OWNER_ID, ownerId);
        cv.put(COL_LIST_UPDATED_AT, updatedAt);
        cv.put(COL_LIST_SYNC_STATE, SYNC_STATE_SYNCED);
        cv.put(COL_LIST_DELETED_AT, deletedAt);

        if (cursor.moveToFirst()) {
            long localUpdatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_LIST_UPDATED_AT));
            String localSyncState = cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_SYNC_STATE));
            // Last-Write-Wins: chỉ ghi đè nếu cloud mới hơn VÀ local đã synced
            if (updatedAt > localUpdatedAt && SYNC_STATE_SYNCED.equals(localSyncState)) {
                db.update(TABLE_LISTS, cv, COL_LIST_ID + " = ?", new String[]{String.valueOf(localId)});
            }
        } else {
            cv.put(COL_LIST_ID, localId);
            db.insertWithOnConflict(TABLE_LISTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
        cursor.close();
    }

    /** Upsert task từ cloud (dùng cho pull). Last-Write-Wins */
    public void upsertTaskFromCloud(int localId, String title, String description, int listId,
                                     String dateTag, long dueDateMillis, int isCompleted,
                                     int isPinned, String reminders, long createdAt,
                                     String ownerType, String ownerId, long updatedAt, long deletedAt) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(TABLE_TASKS, new String[]{COL_TASK_ID, COL_TASK_UPDATED_AT, COL_TASK_SYNC_STATE},
                COL_TASK_ID + " = ?", new String[]{String.valueOf(localId)}, null, null, null);

        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, title);
        cv.put(COL_TASK_DESCRIPTION, description);
        cv.put(COL_TASK_LIST_ID, listId);
        cv.put(COL_TASK_DATE_TAG, dateTag);
        cv.put(COL_TASK_DUE_DATE, dueDateMillis);
        cv.put(COL_TASK_COMPLETED, isCompleted);
        cv.put(COL_TASK_IS_PINNED, isPinned);
        cv.put(COL_TASK_REMINDERS, reminders);
        cv.put(COL_TASK_CREATED, createdAt);
        cv.put(COL_TASK_OWNER_TYPE, ownerType);
        cv.put(COL_TASK_OWNER_ID, ownerId);
        cv.put(COL_TASK_UPDATED_AT, updatedAt);
        cv.put(COL_TASK_SYNC_STATE, SYNC_STATE_SYNCED);
        cv.put(COL_TASK_DELETED_AT, deletedAt);

        if (cursor.moveToFirst()) {
            long localUpdatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_UPDATED_AT));
            String localSyncState = cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_SYNC_STATE));
            if (updatedAt > localUpdatedAt && SYNC_STATE_SYNCED.equals(localSyncState)) {
                db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?", new String[]{String.valueOf(localId)});
            }
        } else {
            cv.put(COL_TASK_ID, localId);
            db.insertWithOnConflict(TABLE_TASKS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
        cursor.close();
    }

    // ═══════════════════════════════════════
    // Guest-to-User Migration (Phase 5)
    // ═══════════════════════════════════════

    /** Đếm số bản ghi guest local (lists custom + tasks) */
    public int countGuestData() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor listCur = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_LISTS + " WHERE " + COL_LIST_OWNER_TYPE + " = ? AND " + COL_LIST_OWNER_ID + " = ? AND " + COL_LIST_ID + " > 4 AND " + COL_LIST_DELETED_AT + " = 0",
                new String[]{OWNER_TYPE_GUEST, OWNER_ID_GUEST_LOCAL});
        int listCount = 0;
        if (listCur.moveToFirst()) listCount = listCur.getInt(0);
        listCur.close();

        Cursor taskCur = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COL_TASK_OWNER_TYPE + " = ? AND " + COL_TASK_OWNER_ID + " = ? AND " + COL_TASK_DELETED_AT + " = 0",
                new String[]{OWNER_TYPE_GUEST, OWNER_ID_GUEST_LOCAL});
        int taskCount = 0;
        if (taskCur.moveToFirst()) taskCount = taskCur.getInt(0);
        taskCur.close();

        return listCount + taskCount;
    }

    /** Chuyển toàn bộ dữ liệu guest → user, đánh dấu pending sync */
    public void migrateGuestDataToUser(String uid) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues listCv = new ContentValues();
            listCv.put(COL_LIST_OWNER_TYPE, SessionManager.OWNER_TYPE_USER);
            listCv.put(COL_LIST_OWNER_ID, uid);
            listCv.put(COL_LIST_UPDATED_AT, nowSeconds());
            listCv.put(COL_LIST_SYNC_STATE, SYNC_STATE_PENDING_CREATE);
            db.update(TABLE_LISTS, listCv,
                    COL_LIST_OWNER_TYPE + " = ? AND " + COL_LIST_OWNER_ID + " = ? AND " + COL_LIST_ID + " > 4",
                    new String[]{OWNER_TYPE_GUEST, OWNER_ID_GUEST_LOCAL});

            ContentValues taskCv = new ContentValues();
            taskCv.put(COL_TASK_OWNER_TYPE, SessionManager.OWNER_TYPE_USER);
            taskCv.put(COL_TASK_OWNER_ID, uid);
            taskCv.put(COL_TASK_UPDATED_AT, nowSeconds());
            taskCv.put(COL_TASK_SYNC_STATE, SYNC_STATE_PENDING_CREATE);
            db.update(TABLE_TASKS, taskCv,
                    COL_TASK_OWNER_TYPE + " = ? AND " + COL_TASK_OWNER_ID + " = ?",
                    new String[]{OWNER_TYPE_GUEST, OWNER_ID_GUEST_LOCAL});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // ═══════════════════════════════════════
    // Sync Timestamp Management
    // ═══════════════════════════════════════

    private static final String PREF_NAME_SYNC = "TickTickSyncPrefs";
    private static final String KEY_LAST_SYNC = "last_sync_timestamp";

    public long getLastSyncTimestamp() {
        return appContext.getSharedPreferences(PREF_NAME_SYNC, Context.MODE_PRIVATE)
                .getLong(KEY_LAST_SYNC, 0L);
    }

    public void setLastSyncTimestamp(long timestamp) {
        appContext.getSharedPreferences(PREF_NAME_SYNC, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_LAST_SYNC, timestamp)
                .apply();
    }
}
