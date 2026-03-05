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
import android.widget.ImageView;
import android.text.TextUtils;
import android.widget.EditText;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<TaskModel> taskList;
    private DrawerLayout drawerLayout;
    private View emptyStateContainer;
    private TextView toolbarTitle;
    private ImageView toolbarListIcon;

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
        toolbarListIcon = findViewById(R.id.toolbar_list_icon);

        setupToolbar();
        setupDrawer();
        setupTaskRecyclerView();
        setupFab();
        setupBottomNavigation();
        setupBackPressHandler();

        // Load tasks mặc định cho "Hôm nay"
        loadTasksForList(currentListId);
    }

    private void updateToolbarForList(String listName, int iconResId) {
        toolbarTitle.setText(listName);

        if (iconResId != 0) {
            toolbarListIcon.setImageResource(iconResId);
            toolbarListIcon.setVisibility(View.VISIBLE);
        } else {
            toolbarListIcon.setVisibility(View.GONE);
        }
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
    private void loadTasksForList(int listId, int iconResId) {
        currentListId = listId;

        List<TaskModel> tasks = dbHelper.getTasksByListId(listId);
        String listName = dbHelper.getListNameById(listId);

        // Cập nhật toolbar với cả icon
        updateToolbarForList(listName, iconResId);

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

    private int getIconResIdForList(String listName) {
        switch (listName) {
            case "Hôm nay":          return R.drawable.ic_today;
            case "Hộp thư đến":      return R.drawable.ic_inbox;
            case "Work":             return R.drawable.ic_work;
            case "Personal":         return R.drawable.ic_personal;
            case "Shopping":         return R.drawable.ic_shopping;
            case "Learning":         return R.drawable.ic_learning;
            case "Wish List":        return R.drawable.ic_wishlist;
            case "Fitness":          return R.drawable.ic_fitness;
            default:                 return 0; // Không có icon
        }
    }

    private void loadTasksForList(int listId) {
        String listName = dbHelper.getListNameById(listId);
        loadTasksForList(listId, getIconResIdForList(listName));
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

            int listId = dbHelper.getListIdByName(item.getTitle());
            if (listId != -1) {
                loadTasksForList(listId, item.getIconResId());  // Truyền thêm iconResId
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
        fab.setOnClickListener(v -> showAddTaskBottomSheet());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.post(() -> {
            CoordinatorLayout.LayoutParams params =
                    (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            params.bottomMargin = bottomNav.getHeight() + dpToPx(16);
            fab.setLayoutParams(params);
        });
    }

    private void showAddTaskBottomSheet() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_add_task, null);
        bottomSheet.setContentView(sheetView);

        // Inputs
        EditText inputTitle = sheetView.findViewById(R.id.input_task_title);
        EditText inputDescription = sheetView.findViewById(R.id.input_task_description);
        TextView textCurrentList = sheetView.findViewById(R.id.text_current_list);

        // Hiện tên danh sách hiện tại
        String currentListName = dbHelper.getListNameById(currentListId);
        textCurrentList.setText(currentListName);

        // Action buttons (chỉ Toast placeholder)
        sheetView.findViewById(R.id.action_date).setOnClickListener(v ->
                Toast.makeText(this, "Chọn ngày", Toast.LENGTH_SHORT).show());
        sheetView.findViewById(R.id.action_flag).setOnClickListener(v ->
                Toast.makeText(this, "Đánh dấu ưu tiên", Toast.LENGTH_SHORT).show());
        sheetView.findViewById(R.id.action_reminder).setOnClickListener(v ->
                Toast.makeText(this, "Đặt nhắc nhở", Toast.LENGTH_SHORT).show());
        sheetView.findViewById(R.id.action_more_options).setOnClickListener(v ->
                Toast.makeText(this, "Thêm tùy chọn", Toast.LENGTH_SHORT).show());
        sheetView.findViewById(R.id.action_mic).setOnClickListener(v ->
                Toast.makeText(this, "Ghi âm", Toast.LENGTH_SHORT).show());

        // Nút gửi — Lưu task vào DB
        sheetView.findViewById(R.id.btn_submit_task).setOnClickListener(v -> {
            String title = inputTitle.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                inputTitle.setError("Nhập tiêu đề task");
                inputTitle.requestFocus();
                return;
            }

            // Lưu vào SQLite
            dbHelper.insertTask(title, currentListId, "");

            // Reload danh sách và đóng bottom sheet
            loadTasksForList(currentListId);
            bottomSheet.dismiss();

            Toast.makeText(this, "Đã thêm: " + title, Toast.LENGTH_SHORT).show();
        });

        // Hiển thị Bottom Sheet và auto-focus vào ô tiêu đề
        bottomSheet.show();
        inputTitle.requestFocus();
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