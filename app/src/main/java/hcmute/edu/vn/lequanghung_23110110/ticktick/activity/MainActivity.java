package hcmute.edu.vn.lequanghung_23110110.ticktick.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.DrawerMenuAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.TaskAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.DrawerMenuItem;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

public class MainActivity extends AppCompatActivity {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<TaskModel> taskList;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Edge-to-edge: chỉ padding top cho root, bottom nav tự xử lý bottom
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Bottom Navigation: padding bottom
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        setupToolbar();
        setupDrawer();        // ← MỚI
        setupRecyclerView();
        setupFab();
        setupBottomNavigation();
        setupBackPressHandler(); // ← MỚI
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Hamburger → mở drawer
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    // ═══════════════════════════════════════
    //  DRAWER SETUP — Custom RecyclerView
    // ═══════════════════════════════════════
    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);

        // RecyclerView trong drawer
        RecyclerView drawerRecyclerView = findViewById(R.id.drawer_recycler_view);
        drawerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tạo danh sách items
        List<DrawerMenuItem> drawerItems = buildDrawerMenuItems();

        // Adapter
        DrawerMenuAdapter drawerAdapter = new DrawerMenuAdapter(drawerItems);
        drawerRecyclerView.setAdapter(drawerAdapter);

        // Click listener
        drawerAdapter.setOnItemClickListener((item, position) -> {
            drawerAdapter.setSelectedPosition(position);
            Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Header buttons
        findViewById(R.id.drawer_btn_search).setOnClickListener(v ->
                Toast.makeText(this, "Tìm kiếm", Toast.LENGTH_SHORT).show());
        findViewById(R.id.drawer_btn_settings).setOnClickListener(v ->
                Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show());

        // Bottom bar buttons
        findViewById(R.id.drawer_btn_add).setOnClickListener(v ->
                Toast.makeText(this, "Thêm danh sách", Toast.LENGTH_SHORT).show());
        findViewById(R.id.drawer_btn_filter).setOnClickListener(v ->
                Toast.makeText(this, "Bộ lọc", Toast.LENGTH_SHORT).show());
    }

    private List<DrawerMenuItem> buildDrawerMenuItems() {
        List<DrawerMenuItem> items = new ArrayList<>();

        // === Navigation items ===
        items.add(new DrawerMenuItem(
                getString(R.string.drawer_today),
                R.drawable.ic_today,
                DrawerMenuItem.ItemType.NAVIGATION
        ).setBadgeCount(1).setSelected(true));  // "Hôm nay" được highlight mặc định

        items.add(new DrawerMenuItem(
                getString(R.string.drawer_inbox),
                R.drawable.ic_inbox,
                DrawerMenuItem.ItemType.NAVIGATION
        ));

        items.add(new DrawerMenuItem(
                getString(R.string.drawer_calendar_subscribed),
                R.drawable.ic_calendar_subscribed,
                DrawerMenuItem.ItemType.NAVIGATION
        ).setHasChevron(true));  // Có mũi tên >

        // === Separator ===
        items.add(DrawerMenuItem.separator());

        // === List items (với icon màu riêng) ===
        items.add(new DrawerMenuItem(
                getString(R.string.drawer_work),
                R.drawable.ic_work,
                DrawerMenuItem.ItemType.LIST
        ));

        items.add(new DrawerMenuItem(
                getString(R.string.drawer_personal),
                R.drawable.ic_personal,
                DrawerMenuItem.ItemType.LIST
        ));

        items.add(new DrawerMenuItem(
                getString(R.string.drawer_shopping),
                R.drawable.ic_shopping,
                DrawerMenuItem.ItemType.LIST
        ));

        items.add(new DrawerMenuItem(
                getString(R.string.drawer_learning),
                R.drawable.ic_learning,
                DrawerMenuItem.ItemType.LIST
        ));

        items.add(new DrawerMenuItem(
                getString(R.string.drawer_wishlist),
                R.drawable.ic_wishlist,
                DrawerMenuItem.ItemType.LIST
        ));

        items.add(new DrawerMenuItem(
                getString(R.string.drawer_fitness),
                R.drawable.ic_fitness,
                DrawerMenuItem.ItemType.LIST
        ));

        items.add(DrawerMenuItem.separator());

        return items;
    }

    // Xử lý Back press: đóng drawer thay vì thoát app
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

    private void setupRecyclerView() {
        taskRecyclerView = findViewById(R.id.task_recycler_view);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>();
        taskList.add(new TaskModel("Test", "Hôm nay", false));
        taskList.add(new TaskModel("Coursera learning time", "Hôm nay", false));

        taskAdapter = new TaskAdapter(taskList);
        taskRecyclerView.setAdapter(taskAdapter);
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab_add_task);
        fab.setOnClickListener(v -> Toast.makeText(this, "Thêm công việc mới", Toast.LENGTH_SHORT).show());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.post(() -> {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            params.bottomMargin = bottomNav.getHeight() + dpToPx(16);
            fab.setLayoutParams(params);
        });
    }

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
}