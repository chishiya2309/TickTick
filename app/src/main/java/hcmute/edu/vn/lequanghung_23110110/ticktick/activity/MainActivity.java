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
import androidx.recyclerview.widget.ItemTouchHelper;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import hcmute.edu.vn.lequanghung_23110110.ticktick.dialog.AddListDialogFragment;
import hcmute.edu.vn.lequanghung_23110110.ticktick.dialog.DatePickerBottomSheet;
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
    private TextView toolbarListEmoji;

    // SQLite
    private TaskDatabaseHelper dbHelper;
    private int currentListId = 1; // Mặc định: "Hôm nay" (list_id=1)

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
        toolbarListEmoji = findViewById(R.id.toolbar_list_emoji);

        setupToolbar();
        setupDrawer();
        setupTaskRecyclerView();
        setupFab();
        setupBottomNavigation();
        setupBackPressHandler();

        // Load tasks mặc định cho "Hôm nay"
        loadTasksForList(currentListId);
    }

    private void updateToolbarForList(String listName, int iconResId, String emojiIcon) {
        toolbarTitle.setText(listName);

        if (emojiIcon != null && !emojiIcon.isEmpty()) {
            toolbarListEmoji.setText(emojiIcon);
            toolbarListEmoji.setVisibility(View.VISIBLE);
            toolbarListIcon.setVisibility(View.GONE);
        } else if (iconResId != 0) {
            toolbarListIcon.setImageResource(iconResId);
            toolbarListIcon.setVisibility(View.VISIBLE);
            toolbarListEmoji.setVisibility(View.GONE);
        } else {
            toolbarListIcon.setVisibility(View.GONE);
            toolbarListEmoji.setVisibility(View.GONE);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    // ═══════════════════════════════════════
    // TOOLBAR
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
    // LOAD TASKS TỪ SQLITE
    // ═══════════════════════════════════════

    /**
     * Load tasks cho danh sách được chọn.
     * - Nếu 0 tasks → hiện Empty State
     * - Nếu ≥1 task → hiện RecyclerView
     */
    private void loadTasksForList(int listId, int iconResId, String emojiIcon) {
        currentListId = listId;

        List<TaskModel> tasks = dbHelper.getTasksByListId(listId);
        String listName = dbHelper.getListNameById(listId);

        // Cập nhật toolbar với cả icon
        updateToolbarForList(listName, iconResId, emojiIcon);

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
            case "Hôm nay":
                return R.drawable.ic_today;
            case "Hộp thư đến":
                return R.drawable.ic_inbox;
            case "Work":
                return R.drawable.ic_work;
            case "Personal":
                return R.drawable.ic_personal;
            case "Shopping":
                return R.drawable.ic_shopping;
            case "Learning":
                return R.drawable.ic_learning;
            case "Wish List":
                return R.drawable.ic_wishlist;
            case "Fitness":
                return R.drawable.ic_fitness;
            default:
                return 0; // Không có icon
        }
    }

    private void loadTasksForList(int listId) {
        String listName = dbHelper.getListNameById(listId);
        // By default, text-based loaded lists without an explicit click might not have
        // their emoji readily available here
        // without a DB lookup. Since we load 'Today' by default, it uses a resource ID.
        // We'll pass null for emoji initially.
        loadTasksForList(listId, getIconResIdForList(listName), null);
    }

    // ═══════════════════════════════════════
    // DRAWER SETUP
    // ═══════════════════════════════════════
    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);

        RecyclerView drawerRecyclerView = findViewById(R.id.drawer_recycler_view);
        drawerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        drawerItems = buildDrawerMenuItems();
        drawerAdapter = new DrawerMenuAdapter(drawerItems);
        drawerRecyclerView.setAdapter(drawerAdapter);

        // Click và Long Click → load tasks hoặc show popup
        drawerAdapter.setOnItemClickListener(new DrawerMenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DrawerMenuItem item, int position) {
                if (item.getType() == DrawerMenuItem.ItemType.SEPARATOR)
                    return;

                drawerAdapter.setSelectedPosition(position);

                int listId = dbHelper.getListIdByName(item.getTitle());
                if (listId != -1) {
                    loadTasksForList(listId, item.getIconResId(), item.getEmojiIcon()); // Truyền thêm emoji
                }

                drawerLayout.closeDrawer(GravityCompat.START);
            }

            @Override
            public void onItemLongClick(DrawerMenuItem item, int position, View anchorView) {
                if (item.getType() == DrawerMenuItem.ItemType.LIST) {
                    showListContextMenu(anchorView, item, position);
                }
            }
        });

        // Kéo thả thay đổi thứ tự (Drag & Drop)
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                // Chỉ cho phép kéo các item có loại là LIST (ngăn chặn List hệ thống)
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    DrawerMenuItem item = drawerItems.get(position);
                    // Giả định hệ thống item & separator ở đầu có index từ 0 -> (số lượng item cố
                    // định)
                    // Ở đây, "Hôm nay", "Hộp thư đến" và 1 phân cách = 3 items đầu.
                    if (item.getType() != DrawerMenuItem.ItemType.LIST || position < 3) {
                        return makeMovementFlags(0, 0); // Vô hiệu hóa kéo thả
                    }
                }
                return super.getMovementFlags(recyclerView, viewHolder);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                    RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int targetPosition = target.getAdapterPosition();

                // Kiểm tra xem vị trí đích có thuộc về Custom List không
                if (targetPosition < 3 || drawerItems.get(targetPosition).getType() != DrawerMenuItem.ItemType.LIST) {
                    return false; // Không cho phép hoán đổi vào đây
                }

                // Hoán đổi vị trí trong list
                DrawerMenuItem movedItem = drawerItems.remove(fromPosition);
                drawerItems.add(targetPosition, movedItem);

                // Thông báo adapter vẽ lại UI
                drawerAdapter.notifyItemMoved(fromPosition, targetPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Không hỗ trợ Swipe để xóa ở Drawer
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                // Cập nhật Database khi ngón tay thẻ vuốt ra
                List<String> orderedListNames = new ArrayList<>();
                for (DrawerMenuItem item : drawerItems) {
                    // Chúng ta chỉ lấy ra các danh sách tùy chỉnh
                    if (item.getType() == DrawerMenuItem.ItemType.LIST && drawerItems.indexOf(item) >= 3) {
                        orderedListNames.add(item.getTitle());
                    }
                }
                dbHelper.updateListOrder(orderedListNames);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(drawerRecyclerView);

        // Header buttons
        findViewById(R.id.drawer_btn_search)
                .setOnClickListener(v -> Toast.makeText(this, "Tìm kiếm", Toast.LENGTH_SHORT).show());
        findViewById(R.id.drawer_btn_settings)
                .setOnClickListener(v -> Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show());

        // Bottom bar
        findViewById(R.id.drawer_btn_add).setOnClickListener(this::showAddMenuPopup);
        findViewById(R.id.drawer_btn_filter)
                .setOnClickListener(v -> Toast.makeText(this, "Bộ lọc", Toast.LENGTH_SHORT).show());

        // Load badge counts ban đầu
        refreshDrawerBadges();
    }

    private List<DrawerMenuItem> buildDrawerMenuItems() {
        List<DrawerMenuItem> items = new ArrayList<>();

        // Navigation items
        items.add(new DrawerMenuItem(
                "Hôm nay", R.drawable.ic_today,
                DrawerMenuItem.ItemType.NAVIGATION).setSelected(true));

        items.add(new DrawerMenuItem(
                "Hộp thư đến", R.drawable.ic_inbox,
                DrawerMenuItem.ItemType.NAVIGATION));

        // Separator
        items.add(DrawerMenuItem.separator());

        // Lấy tất cả danh sách (list_id > 2) từ SQLite
        Map<String, String> customLists = dbHelper.getAllCustomLists();
        for (Map.Entry<String, String> entry : customLists.entrySet()) {
            String listName = entry.getKey();
            String iconName = entry.getValue();

            // Phân biệt Icon Drawable và Emoji Text
            if (iconName != null && iconName.startsWith("ic_")) {
                int resId = getResources().getIdentifier(iconName, "drawable", getPackageName());
                items.add(new DrawerMenuItem(listName, resId, DrawerMenuItem.ItemType.LIST));
            } else {
                // iconName có thể là null (List không icon) hoặc emoji
                items.add(new DrawerMenuItem(listName, iconName, DrawerMenuItem.ItemType.LIST));
            }
        }

        items.add(DrawerMenuItem.separator());

        return items;
    }

    public void addNewListToDrawer(String name, String emojiIcon) {
        // Lưu vào DB
        dbHelper.insertList(name, emojiIcon);

        DrawerMenuItem newItem = new DrawerMenuItem(name, emojiIcon, DrawerMenuItem.ItemType.LIST);
        // Chèn item mới vào đầu phần custom list (sau separator ở vị trí 2)
        drawerItems.add(3, newItem);
        drawerAdapter.notifyItemInserted(3);
    }

    public void updateListInDrawer(int listId, String newName, String newEmojiIcon, int position) {
        // Lưu vào DB
        dbHelper.updateList(listId, newName, newEmojiIcon);

        DrawerMenuItem item = drawerItems.get(position);
        item.setTitle(newName);
        item.setEmojiIcon(newEmojiIcon);
        drawerAdapter.notifyItemChanged(position);

        if (item.isSelected()) {
            updateToolbarForList(newName, item.getIconResId(), newEmojiIcon);
        }
    }

    public void deleteListFromDrawer(int listId, int position) {
        // Xóa khỏi DB (Bao gồm List và toàn bộ Tasks trong đó)
        dbHelper.deleteList(listId);

        // Xóa khỏi Drawer UI
        DrawerMenuItem deletedItem = drawerItems.remove(position);
        drawerAdapter.notifyItemRemoved(position);

        // Nếu danh sách bị xóa đang được chọn, chuyển về danh sách "Hôm nay" (listId
        // mặc định = 1)
        if (deletedItem.isSelected()) {
            // "Hôm nay" thường ở vị trí index = 0 trong drawerItems
            DrawerMenuItem todayItem = drawerItems.get(0);
            drawerAdapter.setSelectedPosition(0);
            loadTasksForList(1, todayItem.getIconResId(), todayItem.getEmojiIcon());
        }
    }

    /**
     * Cập nhật badge count cho mỗi item trong Drawer.
     * Query 1 lần duy nhất: getAllListTaskCounts() → Map<listId, count>
     * Rồi match tên danh sách → set badgeCount
     */
    private void refreshDrawerBadges() {
        Map<Integer, Integer> counts = dbHelper.getAllListTaskCounts();

        for (DrawerMenuItem item : drawerItems) {
            if (item.getType() == DrawerMenuItem.ItemType.SEPARATOR)
                continue;

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
    // TASK RECYCLERVIEW
    // ═══════════════════════════════════════
    private void setupTaskRecyclerView() {
        taskRecyclerView = findViewById(R.id.task_recycler_view);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        taskRecyclerView.setAdapter(taskAdapter);
    }

    // ═══════════════════════════════════════
    // FAB — THÊM TASK MỚI
    // ═══════════════════════════════════════
    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab_add_task);
        fab.setOnClickListener(v -> showAddTaskBottomSheet());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.post(() -> {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            params.bottomMargin = bottomNav.getHeight() + dpToPx(16);
            fab.setLayoutParams(params);
        });
    }

    private void showAddTaskBottomSheet() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_add_task, null);
        bottomSheet.setContentView(sheetView);

        EditText inputTitle = sheetView.findViewById(R.id.input_task_title);
        EditText inputDescription = sheetView.findViewById(R.id.input_task_description);
        TextView textCurrentList = sheetView.findViewById(R.id.text_current_list);

        // Date chip views
        View dateChipContainer = sheetView.findViewById(R.id.date_chip_container);
        TextView dateChipText = sheetView.findViewById(R.id.date_chip_text);
        View actionDate = sheetView.findViewById(R.id.action_date);

        // Biến lưu ngày đã chọn (dùng mảng 1 phần tử để truy cập trong lambda)
        final String[] selectedDateTag = { "" };
        final long[] selectedDateMillis = { -1 };
        final int[] selectedHour = { -1 };
        final int[] selectedMinute = { -1 };

        String currentListName = dbHelper.getListNameById(currentListId);
        textCurrentList.setText(currentListName);

        // ═══ DATE PICKER — Logic chung cho cả icon lịch và chip ═══
        Runnable openDatePicker = () -> {
            DatePickerBottomSheet datePicker = new DatePickerBottomSheet();

            datePicker.setOnDateSelectedListener((dateTag, dateMillis) -> {
                selectedDateTag[0] = dateTag;
                selectedDateMillis[0] = dateMillis;
                dateChipText.setText(dateTag);
                dateChipContainer.setVisibility(View.VISIBLE);
                actionDate.setVisibility(View.GONE);
                dateChipText.setTextColor(getDateChipColor(dateTag));
            });

            datePicker.setOnDateClearedListener(() -> {
                selectedDateTag[0] = "";
                selectedDateMillis[0] = -1;
                selectedHour[0] = -1;
                selectedMinute[0] = -1;
                dateChipContainer.setVisibility(View.GONE);
                actionDate.setVisibility(View.VISIBLE);
            });

            datePicker.setOnTimeSelectedListener((h, m) -> {
                selectedHour[0] = h;
                selectedMinute[0] = m;
            });

            if (selectedDateMillis[0] > 0) {
                datePicker.setPreSelectedDate(selectedDateMillis[0]);
            }
            if (selectedHour[0] >= 0) {
                datePicker.setPreSelectedTime(selectedHour[0], selectedMinute[0]);
            }

            datePicker.show(getSupportFragmentManager(), "date_picker");
        };

        actionDate.setOnClickListener(v -> openDatePicker.run());
        dateChipContainer.setOnClickListener(v -> openDatePicker.run());

        // Các action khác giữ nguyên...
        sheetView.findViewById(R.id.action_flag)
                .setOnClickListener(v -> Toast.makeText(this, "Đánh dấu ưu tiên", Toast.LENGTH_SHORT).show());
        sheetView.findViewById(R.id.action_reminder)
                .setOnClickListener(v -> Toast.makeText(this, "Đặt nhắc nhở", Toast.LENGTH_SHORT).show());
        sheetView.findViewById(R.id.action_more_options)
                .setOnClickListener(v -> Toast.makeText(this, "Thêm tùy chọn", Toast.LENGTH_SHORT).show());
        sheetView.findViewById(R.id.action_mic)
                .setOnClickListener(v -> Toast.makeText(this, "Ghi âm", Toast.LENGTH_SHORT).show());

        // ═══ NÚT GỬI — Lưu task với dateTag ═══
        sheetView.findViewById(R.id.btn_submit_task).setOnClickListener(v -> {
            String title = inputTitle.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                inputTitle.setError("Nhập tiêu đề task");
                inputTitle.requestFocus();
                return;
            }

            // Lưu vào SQLite — dùng selectedDateTag[0] làm dateTag
            dbHelper.insertTask(title, currentListId, selectedDateTag[0]);

            loadTasksForList(currentListId);
            bottomSheet.dismiss();
            Toast.makeText(this, "Đã thêm: " + title, Toast.LENGTH_SHORT).show();
        });

        bottomSheet.show();
        inputTitle.requestFocus();
    }

    // ═══════════════════════════════════════
    // BOTTOM NAVIGATION
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
    // BACK PRESS
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

    private int getDateChipColor(String dateTag) {
        switch (dateTag) {
            case "Hôm nay":
                return Color.parseColor("#4C6FE0");
            case "Ngày mai":
                return Color.parseColor("#FFA726");
            case "Thứ Hai tới":
                return Color.parseColor("#42A5F5");
            case "Đến cuối ngày":
                return Color.parseColor("#66BB6A");
            default:
                return Color.parseColor("#B0B0B0");
        }
    }

    // ═══════════════════════════════════════
    // MENU
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
    // POPUP MENU
    // ═══════════════════════════════════════
    private void showAddMenuPopup(View anchorView) {
        View popupView = getLayoutInflater().inflate(R.layout.layout_popup_add, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(8f);

        popupView.findViewById(R.id.popup_item_list).setOnClickListener(v -> {
            // Đóng popup menu
            popupWindow.dismiss();

            // Mở màn hình Thêm Danh Sách
            AddListDialogFragment dialog = AddListDialogFragment.newInstance();
            dialog.show(getSupportFragmentManager(), "AddListDialog");
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

    private void showListContextMenu(View anchorView, DrawerMenuItem item, int position) {
        View popupView = getLayoutInflater().inflate(R.layout.layout_popup_list_options, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                dpToPx(180),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(8f);

        // Bắt sự kiện click các nút chức năng
        popupView.findViewById(R.id.popup_action_edit).setOnClickListener(v -> {
            popupWindow.dismiss();
            int listId = dbHelper.getListIdByName(item.getTitle());
            if (listId != -1) {
                hcmute.edu.vn.lequanghung_23110110.ticktick.dialog.AddListDialogFragment dialog = hcmute.edu.vn.lequanghung_23110110.ticktick.dialog.AddListDialogFragment
                        .newInstanceForEdit(listId, item.getTitle(), item.getEmojiIcon(), position);
                dialog.show(getSupportFragmentManager(), "EditListDialog");
            }
        });

        popupView.findViewById(R.id.popup_action_pin).setOnClickListener(v -> {
            popupWindow.dismiss();
            Toast.makeText(this, "Đính ghim: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        });

        popupView.findViewById(R.id.popup_action_delete).setOnClickListener(v -> {
            popupWindow.dismiss();

            int listId = dbHelper.getListIdByName(item.getTitle());
            if (listId != -1) {
                String title = "Bạn có muốn xóa danh sách \"";
                if (item.getEmojiIcon() != null && !item.getEmojiIcon().isEmpty()) {
                    title += item.getEmojiIcon() + " ";
                }
                title += item.getTitle() + "\" không?";

                new MaterialAlertDialogBuilder(this)
                        .setTitle(title)
                        .setMessage("Tất cả các nhiệm vụ trong danh sách sẽ bị xóa.")
                        .setNegativeButton("Hủy bỏ", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            deleteListFromDrawer(listId, position);
                            Toast.makeText(this, "Đã xóa danh sách", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            }
        });

        popupWindow.showAsDropDown(anchorView, dpToPx(32), -dpToPx(24));
    }
}