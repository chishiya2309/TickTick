# 📋 Task-List Feature với SQLite — TickTick Clone

> Hướng dẫn triển khai: Nhấn vào danh sách trong Drawer → hiển thị task của danh sách đó.
>
> - **Không có task** → Empty state (hình 1)
> - **Có task** → Hiển thị danh sách task (hình 2)
> - **Drawer badge** → Hiển thị số task ở từng danh sách (hình 3)
>
> **Lưu trữ**: SQLite (tối ưu nhất — built-in Android, query badge count nhanh, không cần thêm dependency)

---

## 📋 Mục lục

1. [Tổng quan kiến trúc](#1-tổng-quan-kiến-trúc)
2. [Tại sao chọn SQLite](#2-tại-sao-chọn-sqlite)
3. [Database Helper](#3-database-helper)
4. [Sửa Model — TaskModel](#4-sửa-model--taskmodel)
5. [Tạo Layout — Empty State](#5-tạo-layout--empty-state)
6. [Sửa Layout — activity_main.xml](#6-sửa-layout--activity_mainxml)
7. [Tạo Drawable — Empty State Illustration](#7-tạo-drawable--empty-state-illustration)
8. [Thêm Strings](#8-thêm-strings)
9. [Sửa MainActivity.java](#9-sửa-mainactivityjava)
10. [Cập nhật Drawer Badge Count](#10-cập-nhật-drawer-badge-count)
11. [Checklist tạo file](#11-checklist-tạo-file)

---

## 1. Tổng quan kiến trúc

```
┌─────────────────────────────────────┐
│           SQLite Database           │
│  ┌───────────┐   ┌───────────────┐  │
│  │   lists   │   │     tasks     │  │
│  │ _id       │──▶│ _id           │  │
│  │ name      │   │ title         │  │
│  │ icon_name │   │ list_id (FK)  │  │
│  └───────────┘   │ date_tag      │  │
│                  │ is_completed  │  │
│                  │ created_at    │  │
│                  └───────────────┘  │
└─────────────────────────────────────┘
          ↕ (DAO layer)
┌─────────────────────────────────────┐
│        TaskDatabaseHelper           │
│  - getTasksByListId(listId)         │
│  - getTaskCountByListId(listId)     │
│  - insertTask(task)                 │
│  - deleteTask(taskId)               │
│  - getAllListsWithCount()           │
└─────────────────────────────────────┘
          ↕
┌─────────────────────────────────────┐
│          MainActivity               │
│  - Drawer click → load tasks        │
│  - Update toolbar title             │
│  - Show empty/list state            │
│  - Refresh badge counts             │
└─────────────────────────────────────┘
```

**Flow khi nhấn vào danh sách trong Drawer:**

1. User nhấn "Work" trong Drawer
2. `MainActivity` → query SQLite: `SELECT * FROM tasks WHERE list_id = ?`
3. Nếu **0 tasks** → hiện Empty State layout
4. Nếu **≥1 task** → hiện RecyclerView với tasks
5. Toolbar title đổi thành "Work" + icon danh sách
6. Drawer đóng lại

---

## 2. Tại sao chọn SQLite

| Tiêu chí            | SQLite ✅                | Đọc/Ghi File          |
| ------------------- | ------------------------ | --------------------- |
| Query phức tạp      | `SELECT COUNT(*)` nhanh  | Parse toàn bộ file    |
| Cấu trúc dữ liệu    | Bảng + quan hệ FK        | JSON/CSV phẳng        |
| Dependency          | **Không cần** (built-in) | Không cần             |
| Tốc độ (nhiều data) | **Nhanh** (indexed)      | Chậm (linear scan)    |
| CRUD riêng lẻ       | **Nhanh** (1 row)        | Đọc/ghi lại toàn file |
| Thread safety       | **Có sẵn**               | Tự quản lý            |

> **Kết luận**: SQLite là lựa chọn tối ưu cho ứng dụng quản lý task.

---

## 3. Database Helper

📁 `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/database/TaskDatabaseHelper.java`

```java
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
    private static final int DB_VERSION = 1;

    // === Table: lists ===
    private static final String TABLE_LISTS = "lists";
    private static final String COL_LIST_ID = "_id";
    private static final String COL_LIST_NAME = "name";
    private static final String COL_LIST_ICON = "icon_name";

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
                + COL_LIST_ICON + " TEXT"
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTS);
        onCreate(db);
    }

    private void seedDefaultLists(SQLiteDatabase db) {
        String[] lists = {"Hôm nay", "Hộp thư đến", "Work", "Personal",
                "Shopping", "Learning", "Wish List", "Fitness"};
        String[] icons = {"ic_today", "ic_inbox", "ic_work", "ic_personal",
                "ic_shopping", "ic_learning", "ic_wishlist", "ic_fitness"};

        for (int i = 0; i < lists.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put(COL_LIST_NAME, lists[i]);
            cv.put(COL_LIST_ICON, icons[i]);
            db.insert(TABLE_LISTS, null, cv);
        }

        // Seed sample tasks
        insertTaskDirect(db, "Test", 1, "Hôm nay", false);       // list_id=1 (Hôm nay)
        insertTaskDirect(db, "Coursera learning time", 1, "Hôm nay", false);
        insertTaskDirect(db, "Test", 3, "", false);               // list_id=3 (Work)
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
    //  CRUD Operations
    // ═══════════════════════════════════════

    /** Lấy tất cả tasks theo list_id */
    public List<TaskModel> getTasksByListId(int listId) {
        List<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASKS, null,
                COL_TASK_LIST_ID + " = ?",
                new String[]{String.valueOf(listId)},
                null, null,
                COL_TASK_COMPLETED + " ASC, " + COL_TASK_CREATED + " DESC");

        while (cursor.moveToNext()) {
            TaskModel task = new TaskModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_LIST_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE_TAG)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1
            );
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
                new String[]{String.valueOf(listId)});
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

    /** Cập nhật trạng thái completed */
    public void updateTaskCompleted(int taskId, boolean completed) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_COMPLETED, completed ? 1 : 0);
        db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)});
    }

    /** Xóa task */
    public void deleteTask(int taskId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TASKS, COL_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)});
    }

    /** Lấy list_id theo tên danh sách */
    public int getListIdByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LISTS, new String[]{COL_LIST_ID},
                COL_LIST_NAME + " = ?", new String[]{name},
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
        Cursor cursor = db.query(TABLE_LISTS, new String[]{COL_LIST_NAME},
                COL_LIST_ID + " = ?", new String[]{String.valueOf(listId)},
                null, null, null);
        String name = "";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }
}
```

---

## 4. Sửa Model — TaskModel

📁 `app/src/main/java/.../model/TaskModel.java` — **THAY TOÀN BỘ FILE**

> Thêm `id` (từ DB) và `listId` (task thuộc danh sách nào)

```java
package hcmute.edu.vn.lequanghung_23110110.ticktick.model;

public class TaskModel {

    private int id;         // DB primary key
    private String title;
    private int listId;     // FK → lists._id
    private String dateTag;
    private boolean isCompleted;

    // Constructor đầy đủ (từ DB)
    public TaskModel(int id, String title, int listId, String dateTag, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.listId = listId;
        this.dateTag = dateTag;
        this.isCompleted = isCompleted;
    }

    // Constructor tạo mới (chưa có id)
    public TaskModel(String title, int listId, String dateTag) {
        this.id = -1;
        this.title = title;
        this.listId = listId;
        this.dateTag = dateTag;
        this.isCompleted = false;
    }

    // --- Getters & Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getListId() { return listId; }
    public void setListId(int listId) { this.listId = listId; }

    public String getDateTag() { return dateTag; }
    public void setDateTag(String dateTag) { this.dateTag = dateTag; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
```

---

## 5. Tạo Layout — Empty State

📁 `app/src/main/res/layout/layout_empty_state.xml`

> Hiển thị khi danh sách không có task nào (hình 1 trong screenshot)

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/empty_state_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:background="@color/main_bg"
    android:visibility="gone">

    <!-- Illustration -->
    <ImageView
        android:layout_width="160dp"
        android:layout_height="160dp"
        android:src="@drawable/ic_empty_tasks"
        android:contentDescription="No tasks"
        android:layout_marginBottom="24dp" />

    <!-- Title -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/empty_no_tasks"
        android:textColor="@color/main_text_primary"
        android:textSize="18sp"
        android:textStyle="bold"
        android:layout_marginBottom="8dp" />

    <!-- Subtitle -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/empty_tap_to_add"
        android:textColor="@color/main_text_secondary"
        android:textSize="14sp" />

</LinearLayout>
```

---

## 6. Sửa Layout — activity_main.xml

📁 `app/src/main/res/layout/activity_main.xml`

> Thêm `<include>` cho Empty State bên trong CoordinatorLayout, ngay sau RecyclerView.
> Empty State và RecyclerView sẽ toggle visibility: 1 hiện thì cái kia ẩn.

Tìm đoạn Task List RecyclerView và thêm phía dưới:

```xml
        <!-- TASK LIST -->
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/task_recycler_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="@color/main_bg"
            android:clipToPadding="false"
            android:paddingBottom="80dp"
            app:layout_behavior="@string/appbar_scrolling_view_behavior"
            tools:listitem="@layout/item_task" />

        <!-- EMPTY STATE (thêm mới) -->
        <include
            layout="@layout/layout_empty_state"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:layout_behavior="@string/appbar_scrolling_view_behavior" />
```

---

## 7. Tạo Drawable — Empty State Illustration

📁 `app/src/main/res/drawable/ic_empty_tasks.xml`

> Icon minh họa clipboard/checklist khi không có task (tương tự hình 1 screenshot)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="160dp"
    android:height="160dp"
    android:viewportWidth="200"
    android:viewportHeight="200">

    <!-- Background circle -->
    <path
        android:fillColor="#2A2A2A"
        android:pathData="M100,100m-80,0a80,80 0,1 1,160 0a80,80 0,1 1,-160 0" />

    <!-- Clipboard body -->
    <path
        android:fillColor="#3A3A3A"
        android:pathData="M65,45 L135,45 C140,45 145,50 145,55 L145,155 C145,160 140,165 135,165 L65,165 C60,165 55,160 55,155 L55,55 C55,50 60,45 65,45 Z" />

    <!-- Clipboard top clip -->
    <path
        android:fillColor="#4C6FE0"
        android:pathData="M85,35 L115,35 C118,35 120,37 120,40 L120,50 L80,50 L80,40 C80,37 82,35 85,35 Z" />

    <!-- Check line 1 -->
    <path
        android:fillColor="#4C6FE0"
        android:pathData="M75,75 L85,85 L95,70"
        android:strokeColor="#4C6FE0"
        android:strokeWidth="4" />
    <path
        android:fillColor="#555555"
        android:pathData="M105,78 L130,78 Q132,78 132,80 L132,82 Q132,84 130,84 L105,84 Q103,84 103,82 L103,80 Q103,78 105,78 Z" />

    <!-- Check line 2 -->
    <path
        android:fillColor="#4C6FE0"
        android:pathData="M75,100 L85,110 L95,95"
        android:strokeColor="#4C6FE0"
        android:strokeWidth="4" />
    <path
        android:fillColor="#555555"
        android:pathData="M105,103 L130,103 Q132,103 132,105 L132,107 Q132,109 130,109 L105,109 Q103,109 103,107 L103,105 Q103,103 105,103 Z" />

    <!-- Empty line 3 -->
    <path
        android:fillColor="#444444"
        android:pathData="M75,128 L82,128 Q84,128 84,130 L84,136 Q84,138 82,138 L75,138 Q73,138 73,136 L73,130 Q73,128 75,128 Z" />
    <path
        android:fillColor="#555555"
        android:pathData="M105,130 L130,130 Q132,130 132,132 L132,134 Q132,136 130,136 L105,136 Q103,136 103,134 L103,132 Q103,130 105,130 Z" />

    <!-- Pencil decoration -->
    <path
        android:fillColor="#FFA726"
        android:pathData="M148,52 L155,45 L162,52 L155,59 Z" />
    <path
        android:fillColor="#FFA726"
        android:pathData="M140,60 L148,52 L155,59 L147,67 Z" />
    <path
        android:fillColor="#FF7043"
        android:pathData="M137,63 L140,60 L147,67 L144,70 Z" />
</vector>
```

---

## 8. Thêm Strings

📁 `app/src/main/res/values/strings.xml` — thêm vào cuối trước `</resources>`

```xml
<!-- Empty State -->
<string name="empty_no_tasks">Không có nhiệm vụ</string>
<string name="empty_tap_to_add">Nhấn vào nút + để thêm</string>

<!-- Task sections -->
<string name="section_unsorted">Không được Phân đoạn</string>
```

---

## 9. Sửa MainActivity.java

📁 `app/src/main/java/.../activity/MainActivity.java` — **THAY TOÀN BỘ FILE**

> Thay đổi chính:
>
> - Thêm `TaskDatabaseHelper` (SQLite)
> - `currentListId` theo dõi danh sách đang xem
> - `loadTasksForList(listId)` query DB → show tasks hoặc empty state
> - Drawer click → `loadTasksForList()` + đổi toolbar title
> - `refreshDrawerBadges()` cập nhật badge count từ DB
> - FAB → thêm task vào danh sách hiện tại

```java
package hcmute.edu.vn.lequanghung_23110110.ticktick.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.DrawerMenuAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.TaskAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.DrawerMenuItem;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

public class MainActivity extends AppCompatActivity {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<TaskModel> taskList;
    private DrawerLayout drawerLayout;
    private View emptyStateContainer;
    private TextView toolbarTitle;

    // SQLite
    private TaskDatabaseHelper dbHelper;
    private int currentListId = 1;  // Mặc định: "Hôm nay" (list_id=1)

    // Drawer
    private DrawerMenuAdapter drawerAdapter;
    private List<DrawerMenuItem> drawerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Khởi tạo Database
        dbHelper = TaskDatabaseHelper.getInstance(this);

        // Edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Views
        emptyStateContainer = findViewById(R.id.empty_state_container);
        toolbarTitle = findViewById(R.id.toolbar_title);

        setupToolbar();
        setupDrawer();
        setupTaskRecyclerView();
        setupFab();
        setupBottomNavigation();
        setupBackPressHandler();

        // Load tasks mặc định cho "Hôm nay"
        loadTasksForList(currentListId);
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    // ═══════════════════════════════════════
    //  TOOLBAR
    // ═══════════════════════════════════════
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout != null) {
                // Refresh badge counts mỗi khi mở drawer
                refreshDrawerBadges();
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    // ═══════════════════════════════════════
    //  LOAD TASKS TỪ SQLITE
    // ═══════════════════════════════════════

    /**
     * Load tasks cho danh sách được chọn.
     * - Nếu 0 tasks → hiện Empty State
     * - Nếu ≥1 task → hiện RecyclerView
     */
    private void loadTasksForList(int listId) {
        currentListId = listId;

        // Query tasks từ DB
        List<TaskModel> tasks = dbHelper.getTasksByListId(listId);

        // Update toolbar title
        String listName = dbHelper.getListNameById(listId);
        toolbarTitle.setText(listName);

        // Update task list
        taskList.clear();
        taskList.addAll(tasks);
        taskAdapter.notifyDataSetChanged();

        // Toggle empty state vs task list
        if (tasks.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            taskRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            taskRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    // ═══════════════════════════════════════
    //  DRAWER SETUP
    // ═══════════════════════════════════════
    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);

        RecyclerView drawerRecyclerView = findViewById(R.id.drawer_recycler_view);
        drawerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        drawerItems = buildDrawerMenuItems();
        drawerAdapter = new DrawerMenuAdapter(drawerItems);
        drawerRecyclerView.setAdapter(drawerAdapter);

        // Click → load tasks cho danh sách đó
        drawerAdapter.setOnItemClickListener((item, position) -> {
            if (item.getType() == DrawerMenuItem.ItemType.SEPARATOR) return;

            drawerAdapter.setSelectedPosition(position);

            // Tìm list_id từ tên danh sách
            int listId = dbHelper.getListIdByName(item.getTitle());
            if (listId != -1) {
                loadTasksForList(listId);
            }

            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Header buttons
        findViewById(R.id.drawer_btn_search).setOnClickListener(v ->
                Toast.makeText(this, "Tìm kiếm", Toast.LENGTH_SHORT).show());
        findViewById(R.id.drawer_btn_settings).setOnClickListener(v ->
                Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show());

        // Bottom bar
        findViewById(R.id.drawer_btn_add).setOnClickListener(this::showAddMenuPopup);
        findViewById(R.id.drawer_btn_filter).setOnClickListener(v ->
                Toast.makeText(this, "Bộ lọc", Toast.LENGTH_SHORT).show());

        // Load badge counts ban đầu
        refreshDrawerBadges();
    }

    private List<DrawerMenuItem> buildDrawerMenuItems() {
        List<DrawerMenuItem> items = new ArrayList<>();

        // Navigation items
        items.add(new DrawerMenuItem(
                "Hôm nay", R.drawable.ic_today,
                DrawerMenuItem.ItemType.NAVIGATION
        ).setSelected(true));

        items.add(new DrawerMenuItem(
                "Hộp thư đến", R.drawable.ic_inbox,
                DrawerMenuItem.ItemType.NAVIGATION
        ));

        items.add(new DrawerMenuItem(
                "Đã đăng ký Lịch", R.drawable.ic_calendar_subscribed,
                DrawerMenuItem.ItemType.NAVIGATION
        ).setHasChevron(true));

        // Separator
        items.add(DrawerMenuItem.separator());

        // List items
        items.add(new DrawerMenuItem("Work", R.drawable.ic_work, DrawerMenuItem.ItemType.LIST));
        items.add(new DrawerMenuItem("Personal", R.drawable.ic_personal, DrawerMenuItem.ItemType.LIST));
        items.add(new DrawerMenuItem("Shopping", R.drawable.ic_shopping, DrawerMenuItem.ItemType.LIST));
        items.add(new DrawerMenuItem("Learning", R.drawable.ic_learning, DrawerMenuItem.ItemType.LIST));
        items.add(new DrawerMenuItem("Wish List", R.drawable.ic_wishlist, DrawerMenuItem.ItemType.LIST));
        items.add(new DrawerMenuItem("Fitness", R.drawable.ic_fitness, DrawerMenuItem.ItemType.LIST));

        items.add(DrawerMenuItem.separator());

        return items;
    }

    /**
     * Cập nhật badge count cho mỗi item trong Drawer.
     * Query 1 lần duy nhất: getAllListTaskCounts() → Map<listId, count>
     * Rồi match tên danh sách → set badgeCount
     */
    private void refreshDrawerBadges() {
        Map<Integer, Integer> counts = dbHelper.getAllListTaskCounts();

        for (DrawerMenuItem item : drawerItems) {
            if (item.getType() == DrawerMenuItem.ItemType.SEPARATOR) continue;

            int listId = dbHelper.getListIdByName(item.getTitle());
            if (listId != -1 && counts.containsKey(listId)) {
                item.setBadgeCount(counts.get(listId));
            } else {
                item.setBadgeCount(0);
            }
        }
        drawerAdapter.notifyDataSetChanged();
    }

    // ═══════════════════════════════════════
    //  TASK RECYCLERVIEW
    // ═══════════════════════════════════════
    private void setupTaskRecyclerView() {
        taskRecyclerView = findViewById(R.id.task_recycler_view);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        taskRecyclerView.setAdapter(taskAdapter);
    }

    // ═══════════════════════════════════════
    //  FAB — THÊM TASK MỚI
    // ═══════════════════════════════════════
    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab_add_task);
        fab.setOnClickListener(v -> {
            // Thêm task mới vào danh sách hiện tại
            String defaultTitle = "Task mới";
            dbHelper.insertTask(defaultTitle, currentListId, "");

            // Reload danh sách
            loadTasksForList(currentListId);
            Toast.makeText(this, "Đã thêm task vào " +
                    dbHelper.getListNameById(currentListId), Toast.LENGTH_SHORT).show();
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.post(() -> {
            CoordinatorLayout.LayoutParams params =
                    (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            params.bottomMargin = bottomNav.getHeight() + dpToPx(16);
            fab.setLayoutParams(params);
        });
    }

    // ═══════════════════════════════════════
    //  BOTTOM NAVIGATION
    // ═══════════════════════════════════════
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_tasks) {
                // Already on tasks
            } else if (id == R.id.nav_calendar) {
                Toast.makeText(this, "Lịch", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    // ═══════════════════════════════════════
    //  BACK PRESS
    // ═══════════════════════════════════════
    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    // ═══════════════════════════════════════
    //  MENU
    // ═══════════════════════════════════════
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_smart_suggest) {
            Toast.makeText(this, "Gợi ý thông minh", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_more) {
            Toast.makeText(this, "Thêm tùy chọn", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ═══════════════════════════════════════
    //  POPUP MENU
    // ═══════════════════════════════════════
    private void showAddMenuPopup(View anchorView) {
        View popupView = getLayoutInflater().inflate(R.layout.layout_popup_add, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(8f);

        popupView.findViewById(R.id.popup_item_list).setOnClickListener(v -> {
            Toast.makeText(this, "Tạo Danh sách mới", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.popup_item_filter).setOnClickListener(v -> {
            Toast.makeText(this, "Tạo Bộ lọc mới", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.popup_item_tag).setOnClickListener(v -> {
            Toast.makeText(this, "Tạo Thẻ mới", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupView.getMeasuredHeight();
        int yOffset = -(anchorView.getHeight() + popupHeight + dpToPx(8));

        popupWindow.showAsDropDown(anchorView, dpToPx(16), yOffset);
    }
}
```

---

## 10. Cập nhật Drawer Badge Count

### Cách hoạt động

```
User mở Drawer (nhấn hamburger)
        ↓
refreshDrawerBadges() được gọi
        ↓
dbHelper.getAllListTaskCounts()
→ SQL: SELECT list_id, COUNT(*) FROM tasks
        WHERE is_completed = 0 GROUP BY list_id
→ Returns: {1: 2, 3: 2, 4: 1}  (Hôm nay=2, Work=2, Personal=1)
        ↓
Loop qua drawerItems → match tên → setBadgeCount()
        ↓
drawerAdapter.notifyDataSetChanged()
→ Badge hiển thị: "Hôm nay [2]", "Work [2]", "Personal [1]"
```

### Khi nào badge tự cập nhật?

| Hành động                  | Badge tự update?                                     |
| -------------------------- | ---------------------------------------------------- |
| Mở Drawer                  | ✅ Có (`refreshDrawerBadges()` trong `setupToolbar`) |
| Thêm task (FAB)            | ✅ Có (sau `loadTasksForList()`)                     |
| Hoàn thành task (checkbox) | ⚠️ Cần thêm callback (xem bên dưới)                  |

### Thêm callback khi checkbox thay đổi (TaskAdapter)

Để badge cập nhật khi user tick/untick checkbox, sửa `TaskAdapter`:

```java
// Trong TaskAdapter.java, thêm interface:
public interface OnTaskStatusChangeListener {
    void onTaskStatusChanged(TaskModel task, boolean completed);
}

private OnTaskStatusChangeListener statusListener;

public void setOnTaskStatusChangeListener(OnTaskStatusChangeListener listener) {
    this.statusListener = listener;
}

// Trong bind() method, sửa checkbox listener:
checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
    task.setCompleted(isChecked);
    updateStrikethrough(isChecked);
    if (statusListener != null) {
        statusListener.onTaskStatusChanged(task, isChecked);
    }
});
```

Trong `MainActivity.setupTaskRecyclerView()`, thêm:

```java
taskAdapter.setOnTaskStatusChangeListener((task, completed) -> {
    // Cập nhật DB
    dbHelper.updateTaskCompleted(task.getId(), completed);
    // Badge sẽ refresh khi mở drawer
});
```

---

## 11. Checklist tạo file

| #   | File                      | Loại       | Vị trí          |
| --- | ------------------------- | ---------- | --------------- |
| 1   | `TaskDatabaseHelper.java` | **NEW**    | `database/`     |
| 2   | `TaskModel.java`          | **MODIFY** | `model/`        |
| 3   | `layout_empty_state.xml`  | **NEW**    | `res/layout/`   |
| 4   | `ic_empty_tasks.xml`      | **NEW**    | `res/drawable/` |
| 5   | `activity_main.xml`       | **MODIFY** | `res/layout/`   |
| 6   | `strings.xml`             | **MODIFY** | `res/values/`   |
| 7   | `MainActivity.java`       | **MODIFY** | `activity/`     |
| 8   | `TaskAdapter.java`        | **MODIFY** | `adapter/`      |

> ⚠️ Ghi chú: Vì `TaskModel` thay đổi constructor, cần đảm bảo `TaskAdapter` tương thích.
> Cụ thể, `TaskAdapter` vẫn gọi `task.getTitle()`, `task.getDateTag()`, `task.isCompleted()` — các method này vẫn giữ nguyên, nên adapter **không cần sửa layout** (chỉ thêm callback listener).
