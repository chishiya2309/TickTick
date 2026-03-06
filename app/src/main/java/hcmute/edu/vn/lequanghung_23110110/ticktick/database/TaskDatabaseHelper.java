package hcmute.edu.vn.lequanghung_23110110.ticktick.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

public class TaskDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ticktick.db";
    private static final int DB_VERSION = 2; // Nâng cấp để thêm cột order_index

    // === Table: lists ===
    private static final String TABLE_LISTS = "lists";
    private static final String COL_LIST_ID = "_id";
    private static final String COL_LIST_NAME = "name";
    private static final String COL_LIST_ICON = "icon_name";
    private static final String COL_LIST_ORDER = "order_index";

    // === Table: tasks ===
    private static final String TABLE_TASKS = "tasks";
    private static final String COL_TASK_ID = "_id";
    private static final String COL_TASK_TITLE = "title";
    private static final String COL_TASK_LIST_ID = "list_id";
    private static final String COL_TASK_DATE_TAG = "date_tag";
    private static final String COL_TASK_COMPLETED = "is_completed";
    private static final String COL_TASK_CREATED = "created_at";

    // Singleton
    private static TaskDatabaseHelper instance;

    public static synchronized TaskDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TaskDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private TaskDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_LISTS + " ("
                + COL_LIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_LIST_NAME + " TEXT NOT NULL, "
                + COL_LIST_ICON + " TEXT, "
                + COL_LIST_ORDER + " INTEGER DEFAULT 0"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_TASKS + " ("
                + COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TASK_TITLE + " TEXT NOT NULL, "
                + COL_TASK_LIST_ID + " INTEGER NOT NULL, "
                + COL_TASK_DATE_TAG + " TEXT, "
                + COL_TASK_COMPLETED + " INTEGER DEFAULT 0, "
                + COL_TASK_CREATED + " INTEGER DEFAULT (strftime('%s','now')), "
                + "FOREIGN KEY (" + COL_TASK_LIST_ID + ") REFERENCES "
                + TABLE_LISTS + "(" + COL_LIST_ID + ")"
                + ")");

        // Index cho query nhanh theo list_id
        db.execSQL("CREATE INDEX idx_task_list ON " + TABLE_TASKS
                + "(" + COL_TASK_LIST_ID + ")");

        // Seed dữ liệu mặc định (các danh sách)
        seedDefaultLists(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_LISTS + " ADD COLUMN " + COL_LIST_ORDER + " INTEGER DEFAULT 0");
        }
    }

    private void seedDefaultLists(SQLiteDatabase db) {
        String[] lists = { "Hôm nay", "Hộp thư đến", "Work", "Personal",
                "Shopping", "Learning", "Wish List", "Fitness" };
        String[] icons = { "ic_today", "ic_inbox", "ic_work", "ic_personal",
                "ic_shopping", "ic_learning", "ic_wishlist", "ic_fitness" };

        for (int i = 0; i < lists.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put(COL_LIST_NAME, lists[i]);
            cv.put(COL_LIST_ICON, icons[i]);
            cv.put(COL_LIST_ORDER, i);
            db.insert(TABLE_LISTS, null, cv);
        }

        // Seed sample tasks
        insertTaskDirect(db, "Test", 1, "Hôm nay", false); // list_id=1 (Hôm nay)
        insertTaskDirect(db, "Coursera learning time", 1, "Hôm nay", false);
        insertTaskDirect(db, "Test", 3, "", false); // list_id=3 (Work)
        insertTaskDirect(db, "Test", 3, "Ngày mai", false);
    }

    private void insertTaskDirect(SQLiteDatabase db, String title,
            int listId, String dateTag, boolean completed) {
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, title);
        cv.put(COL_TASK_LIST_ID, listId);
        cv.put(COL_TASK_DATE_TAG, dateTag);
        cv.put(COL_TASK_COMPLETED, completed ? 1 : 0);
        db.insert(TABLE_TASKS, null, cv);
    }

    // ═══════════════════════════════════════
    // CRUD Operations
    // ═══════════════════════════════════════

    /** Lấy tất cả tasks theo list_id */
    public List<TaskModel> getTasksByListId(int listId) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASKS, null,
                COL_TASK_LIST_ID + " = ?",
                new String[] { String.valueOf(listId) },
                null, null,
                COL_TASK_COMPLETED + " ASC, " + COL_TASK_CREATED + " DESC");

        while (cursor.moveToNext()) {
            TaskModel task = new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1);
            tasks.add(task);
        }
        cursor.close();
        return tasks;
    }

    /** Đếm số task (chưa hoàn thành) theo list_id */
    public int getTaskCountByListId(int listId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_TASKS
                        + " WHERE " + COL_TASK_LIST_ID + " = ? AND "
                        + COL_TASK_COMPLETED + " = 0",
                new String[] { String.valueOf(listId) });
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
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_TASK_LIST_ID + ", COUNT(*) FROM " + TABLE_TASKS
                        + " WHERE " + COL_TASK_COMPLETED + " = 0"
                        + " GROUP BY " + COL_TASK_LIST_ID,
                null);
        while (cursor.moveToNext()) {
            counts.put(cursor.getInt(0), cursor.getInt(1));
        }
        cursor.close();
        return counts;
    }

    /** Thêm task mới */
    public long insertTask(String title, int listId, String dateTag) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, title);
        cv.put(COL_TASK_LIST_ID, listId);
        cv.put(COL_TASK_DATE_TAG, dateTag);
        cv.put(COL_TASK_COMPLETED, 0);
        return db.insert(TABLE_TASKS, null, cv);
    }

    /** Thêm danh sách mới (Custom List) - Thêm vào trên cùng */
    public long insertList(String name, String iconName) {
        SQLiteDatabase db = getWritableDatabase();

        // Đẩy toàn bộ các Custom List (ngoài Hôm nay & Hộp thư đến) xuống 1 bậc
        db.execSQL("UPDATE " + TABLE_LISTS +
                " SET " + COL_LIST_ORDER + " = " + COL_LIST_ORDER + " + 1 " +
                " WHERE " + COL_LIST_ID + " > 2");

        ContentValues cv = new ContentValues();
        cv.put(COL_LIST_NAME, name);
        cv.put(COL_LIST_ICON, iconName);
        cv.put(COL_LIST_ORDER, 0); // Đặt Item này lên đầu danh sách Custom
        return db.insert(TABLE_LISTS, null, cv);
    }

    /** Cập nhật danh sách (Custom List) */
    public void updateList(int listId, String newName, String newIconName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LIST_NAME, newName);
        cv.put(COL_LIST_ICON, newIconName);
        db.update(TABLE_LISTS, cv, COL_LIST_ID + " = ?", new String[] { String.valueOf(listId) });
    }

    /** Cập nhật lại số thứ tự danh sách do người dùng kéo thả dựa theo tên */
    public void updateListOrder(List<String> orderedNames) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < orderedNames.size(); i++) {
                ContentValues cv = new ContentValues();
                cv.put(COL_LIST_ORDER, i);
                db.update(TABLE_LISTS, cv, COL_LIST_NAME + " = ?", new String[] { orderedNames.get(i) });
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Cập nhật trạng thái completed */
    public void updateTaskCompleted(int taskId, boolean completed) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_COMPLETED, completed ? 1 : 0);
        db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?",
                new String[] { String.valueOf(taskId) });
    }

    /** Xóa task */
    public void deleteTask(int taskId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TASKS, COL_TASK_ID + " = ?",
                new String[] { String.valueOf(taskId) });
    }

    /** Lấy list_id theo tên danh sách */
    public int getListIdByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LISTS, new String[] { COL_LIST_ID },
                COL_LIST_NAME + " = ?", new String[] { name },
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
        Cursor cursor = db.query(TABLE_LISTS, new String[] { COL_LIST_NAME },
                COL_LIST_ID + " = ?", new String[] { String.valueOf(listId) },
                null, null, null);
        String name = "";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    public int getListIconResId(Context context, int listId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("lists", new String[] { "icon_name" },
                "_id = ?", new String[] { String.valueOf(listId) },
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
    public Map<String, String> getAllCustomLists() {
        Map<String, String> lists = new java.util.LinkedHashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        // Skip list_id 1 và 2 ("Hôm nay", "Hộp thư đến") vì đó thuộc Navigation items
        // mặc định
        Cursor cursor = db.query(TABLE_LISTS, new String[] { COL_LIST_NAME, COL_LIST_ICON },
                COL_LIST_ID + " > 2", null, null, null, COL_LIST_ORDER + " ASC, " + COL_LIST_ID + " ASC");

        while (cursor.moveToNext()) {
            lists.put(cursor.getString(0), cursor.getString(1));
        }
        cursor.close();
        return lists;
    }
}