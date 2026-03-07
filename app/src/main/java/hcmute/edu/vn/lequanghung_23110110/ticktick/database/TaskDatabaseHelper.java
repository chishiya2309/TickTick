package hcmute.edu.vn.lequanghung_23110110.ticktick.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.lequanghung_23110110.ticktick.model.DrawerMenuItem;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

public class TaskDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ticktick.db";
    private static final int DB_VERSION = 6; // Nâng cấp để reset database

    // === Table: lists ===
    private static final String TABLE_LISTS = "lists";
    private static final String COL_LIST_ID = "_id";
    private static final String COL_LIST_NAME = "name";
    private static final String COL_LIST_ICON = "icon_name";
    private static final String COL_LIST_ORDER = "order_index";
    private static final String COL_LIST_IS_PINNED = "is_pinned";

    // === Table: tasks ===
    private static final String TABLE_TASKS = "tasks";
    private static final String COL_TASK_ID = "_id";
    private static final String COL_TASK_TITLE = "title";
    private static final String COL_TASK_LIST_ID = "list_id";
    private static final String COL_TASK_DATE_TAG = "date_tag";
    private static final String COL_TASK_DUE_DATE = "due_date_millis";
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
                + COL_LIST_ORDER + " INTEGER DEFAULT 0, "
                + COL_LIST_IS_PINNED + " INTEGER DEFAULT 0"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_TASKS + " ("
                + COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TASK_TITLE + " TEXT NOT NULL, "
                + COL_TASK_LIST_ID + " INTEGER NOT NULL, "
                + COL_TASK_DATE_TAG + " TEXT, "
                + COL_TASK_DUE_DATE + " INTEGER DEFAULT -1, "
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
        if (oldVersion < 6) {
            // Reset DB theo yêu cầu của User để test Default Lists
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTS);
            onCreate(db);
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
            db.insert(TABLE_LISTS, null, cv);
        }

        // Seed sample tasks
        long todayMillis = System.currentTimeMillis();
        long yesterdayMillis = todayMillis - (24 * 60 * 60 * 1000L);
        long tomorrowMillis = todayMillis + (24 * 60 * 60 * 1000L);

        insertTaskDirect(db, "Test", 1, "Hôm nay", todayMillis, false); // Hôm nay
        insertTaskDirect(db, "Overdue Sample", 1, "Hôm qua", yesterdayMillis, false); // Quá hạn
        insertTaskDirect(db, "Test Work", 5, "", -1, false); // Work (ID=5 do 2 system list mới đẩy)
        insertTaskDirect(db, "Test Tomorrow", 5, "Ngày mai", tomorrowMillis, false);
    }

    private void insertTaskDirect(SQLiteDatabase db, String title,
            int listId, String dateTag, long dueDateMillis, boolean completed) {
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, title);
        cv.put(COL_TASK_LIST_ID, listId);
        cv.put(COL_TASK_DATE_TAG, dateTag);
        cv.put(COL_TASK_DUE_DATE, dueDateMillis);
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
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1);
            tasks.add(task);
        }
        cursor.close();
        return tasks;
    }

    /** Lấy tất cả tasks quá hạn và hôm nay */
    public List<TaskModel> getTodayAndOverdueTasks(long startOfTodayMillis, long endOfTodayMillis) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Lấy tất cả Task:
        // 1. Quá hạn: due_date_millis > 0 AND due_date_millis < startOfTodayMillis AND
        // chưa hoàn thành
        // 2. Hôm nay: due_date_millis >= startOfTodayMillis AND due_date_millis <=
        // endOfTodayMillis AND chưa hoàn thành
        // (Hoặc date_tag = 'Hôm nay' phòng trường hợp task cũ)
        String selection = "((" + COL_TASK_DUE_DATE + " > 0 AND " + COL_TASK_DUE_DATE + " < ?) OR " +
                "(" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " = 'Hôm nay')) AND " + COL_TASK_COMPLETED + " = 0";

        String[] selectionArgs = new String[] {
                String.valueOf(startOfTodayMillis),
                String.valueOf(startOfTodayMillis),
                String.valueOf(endOfTodayMillis)
        };

        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null,
                COL_TASK_DUE_DATE + " ASC");

        while (cursor.moveToNext()) {
            TaskModel task = new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1);
            tasks.add(task);
        }
        cursor.close();
        return tasks;
    }

    /** Lấy tất cả tasks của ngày mai (không có quá hạn) */
    public List<TaskModel> getTomorrowTasks(long startOfTomorrowMillis, long endOfTomorrowMillis) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // 1. Chỉ của ngày mai: due_date_millis >= startOfTomorrowMillis AND
        // due_date_millis <= endOfTomorrowMillis AND chưa hoàn thành
        // (Hoặc date_tag = 'Ngày mai' phòng trường hợp task cũ)
        String selection = "((" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " = 'Ngày mai')) AND " + COL_TASK_COMPLETED + " = 0";

        String[] selectionArgs = new String[] {
                String.valueOf(startOfTomorrowMillis),
                String.valueOf(endOfTomorrowMillis)
        };

        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null,
                COL_TASK_DUE_DATE + " ASC");

        while (cursor.moveToNext()) {
            TaskModel task = new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1);
            tasks.add(task);
        }
        cursor.close();
        return tasks;
    }

    /** Lấy tất cả tasks của 7 ngày tới (không lấy quá hạn) */
    public List<TaskModel> getNext7DaysTasks(long startOfNext7DaysMillis, long endOfNext7DaysMillis) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Chỉ lấy trong 7 ngày tới
        String selection = "((" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " = '7 ngày tới')) AND " + COL_TASK_COMPLETED + " = 0";

        String[] selectionArgs = new String[] {
                String.valueOf(startOfNext7DaysMillis),
                String.valueOf(endOfNext7DaysMillis)
        };

        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null,
                COL_TASK_DUE_DATE + " ASC");

        while (cursor.moveToNext()) {
            TaskModel task = new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1);
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

        // Lấy tất cả Task:
        // 1. Quá hạn: due_date_millis > 0 AND due_date_millis < startOfTodayMillis
        // 2. Trong 7 ngày: due_date_millis >= startOfTodayMillis AND due_date_millis <=
        // endOfNext7DaysMillis
        // 3. Các task cũ chưa có millis nhưng có date_tag hợp lệ
        String selection = "((" + COL_TASK_DUE_DATE + " > 0 AND " + COL_TASK_DUE_DATE + " < ?) OR " +
                "(" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " IN ('Hôm nay', 'Ngày mai', '7 ngày tới', 'Hôm qua'))) AND "
                + COL_TASK_COMPLETED + " = 0";

        String[] selectionArgs = new String[] {
                String.valueOf(startOfTodayMillis),
                String.valueOf(startOfTodayMillis),
                String.valueOf(endOfNext7DaysMillis)
        };

        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null,
                COL_TASK_DUE_DATE + " ASC");

        while (cursor.moveToNext()) {
            TaskModel task = new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
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

        Cursor cursorToday = db.query(TABLE_TASKS, new String[] { "COUNT(*)" }, todaySelection, todayArgs, null, null,
                null);
        if (cursorToday.moveToFirst())
            counts.put(1, cursorToday.getInt(0));
        cursorToday.close();

        // 2. Ngày mai (không lấy quá hạn)
        String tomorrowSelection = "((" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " = 'Ngày mai')) AND " + COL_TASK_COMPLETED + " = 0";
        String[] tomorrowArgs = { String.valueOf(startOfTomorrow), String.valueOf(endOfTomorrow) };

        Cursor cursorTomorrow = db.query(TABLE_TASKS, new String[] { "COUNT(*)" }, tomorrowSelection, tomorrowArgs,
                null, null, null);
        if (cursorTomorrow.moveToFirst())
            counts.put(2, cursorTomorrow.getInt(0));
        cursorTomorrow.close();

        // 3. 7 ngày tới (không lấy quá hạn - nhưng BAO GỒM Hôm nay và Ngày mai)
        String next7DaysSelection = "((" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " IN ('Hôm nay', 'Ngày mai', '7 ngày tới'))) AND " + COL_TASK_COMPLETED
                + " = 0";
        String[] next7DaysArgs = { String.valueOf(startOfToday), String.valueOf(endOfNext7Days) };

        Cursor cursorNext7 = db.query(TABLE_TASKS, new String[] { "COUNT(*)" }, next7DaysSelection, next7DaysArgs, null,
                null, null);
        if (cursorNext7.moveToFirst())
            counts.put(3, cursorNext7.getInt(0));
        cursorNext7.close();

        return counts;
    }

    /** Thêm một task mới */
    public long insertTask(String title, int listId, String dateTag, long dueDateMillis) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, title);
        cv.put(COL_TASK_LIST_ID, listId);
        cv.put(COL_TASK_DATE_TAG, dateTag);
        cv.put(COL_TASK_DUE_DATE, dueDateMillis);
        cv.put(COL_TASK_COMPLETED, 0);
        return db.insert(TABLE_TASKS, null, cv);
    }

    /** Thêm danh sách mới (Custom List) */
    public void insertList(String name, String iconName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LIST_NAME, name);
        cv.put(COL_LIST_ICON, iconName);
        cv.put(COL_LIST_ORDER, 0); // Đặt Item này lên đầu danh sách Custom
        db.insert(TABLE_LISTS, null, cv);
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

    /** Xóa danh sách và toàn bộ Tasks nằm trong danh sách đó */
    public void deleteList(int listId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Xóa tất cả task thuộc về list này
            db.delete(TABLE_TASKS, COL_TASK_LIST_ID + " = ?", new String[] { String.valueOf(listId) });
            // Xóa list
            db.delete(TABLE_LISTS, COL_LIST_ID + " = ?", new String[] { String.valueOf(listId) });
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
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

    /** Đổi trạng thái ghim của danh sách */
    public void togglePinList(int listId, boolean isPinned) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LIST_IS_PINNED, isPinned ? 1 : 0);
        db.update(TABLE_LISTS, cv, COL_LIST_ID + " = ?", new String[] { String.valueOf(listId) });
    }

    /** Kiểm tra xem sách có đang được ghim hay không */
    public boolean isListPinned(int listId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LISTS, new String[] { COL_LIST_IS_PINNED },
                COL_LIST_ID + " = ?", new String[] { String.valueOf(listId) },
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
        Cursor cursor = db.query(TABLE_LISTS, new String[] { COL_LIST_NAME, COL_LIST_ICON },
                COL_LIST_IS_PINNED + " = 1", null, null, null, COL_LIST_ORDER + " ASC, " + COL_LIST_ID + " ASC");

        while (cursor.moveToNext()) {
            String listName = cursor.getString(0);
            String iconName = cursor.getString(1);

            if (iconName != null && iconName.startsWith("ic_")) {
                int resId = 0; // MainActivity sẽ resolve logic này, tạm bỏ nếu chưa cần
                pinnedLists.add(new DrawerMenuItem(listName, resId, DrawerMenuItem.ItemType.LIST));
            } else {
                pinnedLists.add(new DrawerMenuItem(listName, iconName, DrawerMenuItem.ItemType.LIST));
            }
        }
        cursor.close();
        return pinnedLists;
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
        // Skip list_id 1 đến 4 ("Hôm nay", "Ngày mai", "7 ngày tới", "Hộp thư đến") vì
        // đó thuộc Navigation items mặc định
        Cursor cursor = db.query(TABLE_LISTS, new String[] { COL_LIST_NAME, COL_LIST_ICON },
                COL_LIST_ID + " > 4", null, null, null, COL_LIST_ORDER + " ASC, " + COL_LIST_ID + " ASC");

        while (cursor.moveToNext()) {
            lists.put(cursor.getString(0), cursor.getString(1));
        }
        cursor.close();
        return lists;
    }
}