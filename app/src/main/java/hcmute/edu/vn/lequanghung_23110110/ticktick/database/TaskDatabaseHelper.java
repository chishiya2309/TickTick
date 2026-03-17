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
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.ListModel;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

public class TaskDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ticktick.db";
    private static final int DB_VERSION = 9;

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
    private static final String COL_TASK_DESCRIPTION = "description";
    private static final String COL_TASK_LIST_ID = "list_id";
    private static final String COL_TASK_DATE_TAG = "date_tag";
    private static final String COL_TASK_DUE_DATE = "due_date_millis";
    private static final String COL_TASK_COMPLETED = "is_completed";
    private static final String COL_TASK_CREATED = "created_at";
    private static final String COL_TASK_IS_PINNED = "is_pinned";

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
                + COL_TASK_DESCRIPTION + " TEXT, "
                + COL_TASK_LIST_ID + " INTEGER NOT NULL, "
                + COL_TASK_DATE_TAG + " TEXT, "
                + COL_TASK_DUE_DATE + " INTEGER DEFAULT -1, "
                + COL_TASK_COMPLETED + " INTEGER DEFAULT 0, "
                + COL_TASK_IS_PINNED + " INTEGER DEFAULT 0, "
                + COL_TASK_CREATED + " INTEGER DEFAULT (strftime('%s','now')), "
                + "FOREIGN KEY (" + COL_TASK_LIST_ID + ") REFERENCES "
                + TABLE_LISTS + "(" + COL_LIST_ID + ")"
                + ")");

        db.execSQL("CREATE INDEX idx_task_list ON " + TABLE_TASKS + "(" + COL_TASK_LIST_ID + ")");
        seedDefaultLists(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 9) {
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
    }

    // --- CRUD Tasks ---

    public List<TaskModel> getTasksByListId(int listId) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, COL_TASK_LIST_ID + " = ?",
                new String[] { String.valueOf(listId) }, null, null,
                COL_TASK_IS_PINNED + " DESC, " + COL_TASK_COMPLETED + " ASC, " + COL_TASK_CREATED + " DESC");
        while (cursor.moveToNext()) {
            tasks.add(mapCursorToTask(cursor));
        }
        cursor.close();
        return tasks;
    }

    public List<TaskModel> getStrictlyTodayTasks(long startOfTodayMillis, long endOfTodayMillis) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = COL_TASK_COMPLETED + " = 0 AND (" +
                "(" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " = 'Hôm nay' AND (" + COL_TASK_DUE_DATE + " >= ? OR " + COL_TASK_DUE_DATE + " = -1))" +
                ")";
        String[] selectionArgs = { String.valueOf(startOfTodayMillis), String.valueOf(endOfTodayMillis), String.valueOf(startOfTodayMillis) };
        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null, COL_TASK_DUE_DATE + " ASC");
        while (cursor.moveToNext()) { tasks.add(mapCursorToTask(cursor)); }
        cursor.close();
        return tasks;
    }

    public List<TaskModel> getTodayAndOverdueTasks(long startOfTodayMillis, long endOfTodayMillis) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = "((" + COL_TASK_DUE_DATE + " > 0 AND " + COL_TASK_DUE_DATE + " < ?) OR " +
                "(" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " = 'Hôm nay')) AND " + COL_TASK_COMPLETED + " = 0";
        String[] selectionArgs = { String.valueOf(startOfTodayMillis), String.valueOf(startOfTodayMillis), String.valueOf(endOfTodayMillis) };
        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null, COL_TASK_DUE_DATE + " ASC");
        while (cursor.moveToNext()) { tasks.add(mapCursorToTask(cursor)); }
        cursor.close();
        return tasks;
    }

    public List<TaskModel> getTomorrowTasks(long startOfTomorrow, long endOfTomorrow) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = "(" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " = 'Ngày mai')";
        String[] selectionArgs = { String.valueOf(startOfTomorrow), String.valueOf(endOfTomorrow) };
        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null, COL_TASK_DUE_DATE + " ASC");
        while (cursor.moveToNext()) { tasks.add(mapCursorToTask(cursor)); }
        cursor.close();
        return tasks;
    }

    public List<TaskModel> getAllTasksFor7DaysView(long startOfToday, long endOfNext7Days) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = "(" + COL_TASK_DUE_DATE + " > 0 AND " + COL_TASK_DUE_DATE + " < ?) OR " +
                "(" + COL_TASK_DUE_DATE + " >= ? AND " + COL_TASK_DUE_DATE + " <= ?) OR " +
                "(" + COL_TASK_DATE_TAG + " = 'Hôm nay' OR " + COL_TASK_DATE_TAG + " = 'Ngày mai' OR " + COL_TASK_DATE_TAG + " = '7 ngày tới')";
        String[] selectionArgs = { String.valueOf(startOfToday), String.valueOf(startOfToday), String.valueOf(endOfNext7Days) };
        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null, COL_TASK_DUE_DATE + " ASC");
        while (cursor.moveToNext()) { tasks.add(mapCursorToTask(cursor)); }
        cursor.close();
        return tasks;
    }

    private TaskModel mapCursorToTask(Cursor cursor) {
        return new TaskModel(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_IS_PINNED)) == 1
        );
    }

    public TaskModel getTaskById(int taskId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            TaskModel task = mapCursorToTask(cursor);
            cursor.close();
            return task;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public void updateTaskCompleted(int taskId, boolean completed) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_COMPLETED, completed ? 1 : 0);
        db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?", new String[] { String.valueOf(taskId) });
    }

    public void updateTaskDueDate(int taskId, long dueDateMillis) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_DUE_DATE, dueDateMillis);
        db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?", new String[] { String.valueOf(taskId) });
    }

    public void updateTaskPinned(int taskId, boolean pinned) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_IS_PINNED, pinned ? 1 : 0);
        db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public void updateTaskDate(int taskId, String dateTag, long dateMillis) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_DATE_TAG, dateTag);
        cv.put(COL_TASK_DUE_DATE, dateMillis);
        db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public void updateTaskDetails(int taskId, String title, String description) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, title);
        cv.put(COL_TASK_DESCRIPTION, description);
        db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public void moveTaskToList(int taskId, int listId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_LIST_ID, listId);
        db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public long insertTask(String title, String description, int listId, String dateTag, long dueDateMillis) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, title);
        cv.put(COL_TASK_DESCRIPTION, description);
        cv.put(COL_TASK_LIST_ID, listId);
        cv.put(COL_TASK_DATE_TAG, dateTag);
        cv.put(COL_TASK_DUE_DATE, dueDateMillis);
        cv.put(COL_TASK_COMPLETED, 0);
        cv.put(COL_TASK_IS_PINNED, 0);
        return db.insert(TABLE_TASKS, null, cv);
    }

    public void deleteTask(int taskId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TASKS, COL_TASK_ID + " = ?", new String[] { String.valueOf(taskId) });
    }

    public List<TaskModel> getUpcomingTasks() {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        long now = System.currentTimeMillis();
        String selection = COL_TASK_DUE_DATE + " > ? AND " + COL_TASK_COMPLETED + " = 0";
        String[] selectionArgs = { String.valueOf(now) };
        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null, COL_TASK_DUE_DATE + " ASC");
        while (cursor.moveToNext()) { tasks.add(mapCursorToTask(cursor)); }
        cursor.close();
        return tasks;
    }

    public List<TaskModel> searchTasks(String query) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = COL_TASK_TITLE + " LIKE ? OR " + COL_TASK_DESCRIPTION + " LIKE ?";
        String[] selectionArgs = { "%" + query + "%", "%" + query + "%" };
        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null, null);
        while (cursor.moveToNext()) { tasks.add(mapCursorToTask(cursor)); }
        cursor.close();
        return tasks;
    }

    // --- List Operations ---

    public String getListNameById(int listId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LISTS, new String[]{COL_LIST_NAME}, COL_LIST_ID + " = ?", new String[]{String.valueOf(listId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(0);
            cursor.close();
            return name;
        }
        if (cursor != null) cursor.close();
        return "";
    }

    public String getListEmojiById(int listId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LISTS, new String[]{COL_LIST_ICON}, COL_LIST_ID + " = ?", new String[]{String.valueOf(listId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String icon = cursor.getString(0);
            cursor.close();
            if (icon != null && !icon.startsWith("ic_")) return icon;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public int getListIconResId(Context context, int listId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LISTS, new String[]{COL_LIST_ICON}, COL_LIST_ID + " = ?", new String[]{String.valueOf(listId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String iconName = cursor.getString(0);
            cursor.close();
            if (iconName != null && iconName.startsWith("ic_")) {
                return context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
            }
        }
        if (cursor != null) cursor.close();
        return 0;
    }

    public List<DrawerMenuItem> getPinnedLists() {
        List<DrawerMenuItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LISTS, null, COL_LIST_IS_PINNED + " = 1", null, null, null, COL_LIST_ORDER + " ASC");
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LIST_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_NAME));
            String icon = cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_ICON));
            items.add(new DrawerMenuItem(id, name, 0, DrawerMenuItem.ItemType.LIST).setEmojiIcon(icon));
        }
        cursor.close();
        return items;
    }

    public List<DrawerMenuItem> getAllCustomLists() {
        List<DrawerMenuItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        // Assuming first 4 are navigation items, but let's just get all and filter in UI if needed.
        // Or based on seedDefaultLists, first 10 are default.
        Cursor cursor = db.query(TABLE_LISTS, null, COL_LIST_ID + " > 4", null, null, null, COL_LIST_ORDER + " ASC");
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LIST_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_NAME));
            String icon = cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_ICON));
            items.add(new DrawerMenuItem(id, name, 0, DrawerMenuItem.ItemType.LIST).setEmojiIcon(icon));
        }
        cursor.close();
        return items;
    }

    public List<DrawerMenuItem> searchLists(String query) {
        List<DrawerMenuItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = COL_LIST_NAME + " LIKE ?";
        String[] selectionArgs = { "%" + query + "%" };
        Cursor cursor = db.query(TABLE_LISTS, null, selection, selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LIST_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_NAME));
            String icon = cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_ICON));
            items.add(new DrawerMenuItem(id, name, 0, DrawerMenuItem.ItemType.LIST).setEmojiIcon(icon));
        }
        cursor.close();
        return items;
    }

    public List<ListModel> getMoveToOptions() {
        List<ListModel> lists = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LISTS, null, null, null, null, null, COL_LIST_ORDER + " ASC");
        while (cursor.moveToNext()) {
            lists.add(new ListModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_ICON))
            ));
        }
        cursor.close();
        return lists;
    }

    public long insertList(String name, String icon) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LIST_NAME, name);
        cv.put(COL_LIST_ICON, icon);
        return db.insert(TABLE_LISTS, null, cv);
    }

    public void updateList(int listId, String name, String icon) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LIST_NAME, name);
        cv.put(COL_LIST_ICON, icon);
        db.update(TABLE_LISTS, cv, COL_LIST_ID + " = ?", new String[]{String.valueOf(listId)});
    }

    public void deleteList(int listId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TASKS, COL_TASK_LIST_ID + " = ?", new String[]{String.valueOf(listId)});
        db.delete(TABLE_LISTS, COL_LIST_ID + " = ?", new String[]{String.valueOf(listId)});
    }

    public void updateListOrder(List<String> orderedNames) {
        SQLiteDatabase db = getWritableDatabase();
        for (int i = 0; i < orderedNames.size(); i++) {
            ContentValues cv = new ContentValues();
            cv.put(COL_LIST_ORDER, i + 5); // Assuming custom lists start order after default ones
            db.update(TABLE_LISTS, cv, COL_LIST_NAME + " = ?", new String[]{orderedNames.get(i)});
        }
    }

    public boolean isListPinned(int listId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LISTS, new String[]{COL_LIST_IS_PINNED}, COL_LIST_ID + " = ?", new String[]{String.valueOf(listId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            boolean pinned = cursor.getInt(0) == 1;
            cursor.close();
            return pinned;
        }
        if (cursor != null) cursor.close();
        return false;
    }

    public void togglePinList(int listId, boolean pinned) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LIST_IS_PINNED, pinned ? 1 : 0);
        db.update(TABLE_LISTS, cv, COL_LIST_ID + " = ?", new String[]{String.valueOf(listId)});
    }

    public Map<Integer, Integer> getAllListTaskCounts() {
        Map<Integer, Integer> counts = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_TASK_LIST_ID + ", COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COL_TASK_COMPLETED + " = 0 GROUP BY " + COL_TASK_LIST_ID, null);
        while (cursor.moveToNext()) {
            counts.put(cursor.getInt(0), cursor.getInt(1));
        }
        cursor.close();
        return counts;
    }
}
