package hcmute.edu.vn.lequanghung_23110110.ticktick.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
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
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.DrawerMenuAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.TaskAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.dialog.AddListDialogFragment;
import hcmute.edu.vn.lequanghung_23110110.ticktick.dialog.DatePickerBottomSheet;
import hcmute.edu.vn.lequanghung_23110110.ticktick.dialog.TaskDetailBottomSheet;
import hcmute.edu.vn.lequanghung_23110110.ticktick.dialog.MoveTaskBottomSheet;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.DrawerMenuItem;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskHeader;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskListItem;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;
import hcmute.edu.vn.lequanghung_23110110.ticktick.service.ReminderService;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.TaskSwipeHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.PinnedListAdapter;
import android.widget.ImageView;
import android.text.TextUtils;
import android.widget.EditText;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.DailyBriefingScheduler;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ALARM_DEBUG";
    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<TaskListItem> taskList;
    private DrawerLayout drawerLayout;
    private View emptyStateContainer;
    private TextView toolbarTitle;
    private ImageView toolbarListIcon;
    private TextView toolbarListEmoji;

    private TaskDatabaseHelper dbHelper;
    private int currentListId = 1;

    private DrawerMenuAdapter drawerAdapter;
    private List<DrawerMenuItem> drawerItems;
    private RecyclerView pinnedRecyclerView;
    private PinnedListAdapter pinnedAdapter;
    private List<DrawerMenuItem> pinnedItems;

    private ReminderService reminderService;
    private boolean isBound = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Đã cấp quyền thông báo", Toast.LENGTH_SHORT).show();
                    checkExactAlarmPermission();
                } else {
                    Toast.makeText(this, "Ứng dụng cần quyền thông báo để nhắc nhở", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dbHelper = TaskDatabaseHelper.getInstance(this);

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

        // Xử lý intent nếu có, nếu không thì load mặc định "Hôm nay"
        if (!handleIntent(getIntent())) {
            loadTasksForList(currentListId);
        }

        DailyBriefingScheduler.setupDailyBriefingWork(this);
        loadTasksForList(currentListId);

        checkPermissions();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private boolean handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("EXTRA_LIST_ID")) {
            int listId = intent.getIntExtra("EXTRA_LIST_ID", 1);
            int iconResId = intent.getIntExtra("EXTRA_LIST_ICON_RES_ID", 0);
            String emojiStr = intent.getStringExtra("EXTRA_LIST_EMOJI");

            // Tìm và setSelected trong drawer
            if (drawerItems != null && drawerAdapter != null) {
                for (int i = 0; i < drawerItems.size(); i++) {
                    DrawerMenuItem item = drawerItems.get(i);
                    if (item != null && item.getId() == listId && item.getType() != DrawerMenuItem.ItemType.SEPARATOR) {
                        drawerAdapter.setSelectedPosition(i);
                        break;
                    }
                }
            }

            loadTasksForList(listId, iconResId, emojiStr);
            return true;
        }
        return false;
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                checkExactAlarmPermission();
            }
        } else {
            checkExactAlarmPermission();
        }
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Cấp quyền báo thức")
                        .setMessage("Để báo thức hoạt động chính xác, vui lòng cấp quyền 'Báo thức và nhắc nhở' cho ứng dụng.")
                        .setPositiveButton("Cài đặt", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        }
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

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout != null) {
                refreshDrawerBadges();
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private boolean showOverdue = true;
    private boolean showToday = true;
    private boolean showTomorrow = true;
    private java.util.HashMap<String, Boolean> groupStates = new java.util.HashMap<>();

    private void loadTasksForList(int listId, int iconResId, String emojiIcon) {
        currentListId = listId;
        String listName = dbHelper.getListNameById(listId);
        updateToolbarForList(listName, iconResId, emojiIcon);

        taskList.clear();

        if (listId == 1) {
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

            List<TaskModel> allTasks = dbHelper.getTodayAndOverdueTasks(startOfToday, endOfToday);
            List<TaskModel> overdueTasks = new ArrayList<>();
            List<TaskModel> todayTasks = new ArrayList<>();
            List<TaskModel> completedTasks = new ArrayList<>();
            List<TaskModel> pinnedTasks = new ArrayList<>();

            for (TaskModel t : allTasks) {
                if (t.isCompleted()) completedTasks.add(t);
                else if (t.isPinned()) pinnedTasks.add(t);
                else if (t.getDueDateMillis() > 0 && t.getDueDateMillis() < startOfToday) overdueTasks.add(t);
                else todayTasks.add(t);
            }

            if (!pinnedTasks.isEmpty()) {
                boolean isExpanded = groupStates.getOrDefault("PINNED", true);
                taskList.add(new TaskHeader("ĐÃ GHIM", pinnedTasks.size(), R.color.main_text_secondary, isExpanded, () -> {
                    groupStates.put("PINNED", !isExpanded);
                    loadTasksForList(listId, iconResId, emojiIcon);
                }));
                if (isExpanded) taskList.addAll(pinnedTasks);
            }

            if (!overdueTasks.isEmpty()) {
                taskList.add(new TaskHeader("QUÁ HẠN", overdueTasks.size(), R.color.red_delete, showOverdue, () -> {
                    showOverdue = !showOverdue;
                    loadTasksForList(listId, iconResId, emojiIcon);
                }));
                if (showOverdue) taskList.addAll(overdueTasks);
            }

            if (!todayTasks.isEmpty() || overdueTasks.isEmpty()) {
                if (!todayTasks.isEmpty()) {
                    taskList.add(new TaskHeader("HÔM NAY", todayTasks.size(), R.color.main_text_secondary, showToday, () -> {
                        showToday = !showToday;
                        loadTasksForList(listId, iconResId, emojiIcon);
                    }));
                    if (showToday) taskList.addAll(todayTasks);
                }
            }

            if (!completedTasks.isEmpty()) {
                boolean isExpanded = groupStates.getOrDefault("COMPLETED", false);
                taskList.add(new TaskHeader("ĐÃ HOÀN THÀNH", completedTasks.size(), R.color.main_text_secondary, isExpanded, () -> {
                    groupStates.put("COMPLETED", !isExpanded);
                    loadTasksForList(listId, iconResId, emojiIcon);
                }));
                if (isExpanded) taskList.addAll(completedTasks);
            }
        } else if (listId == 2) {
            long now = System.currentTimeMillis();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(now);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            long startOfTomorrow = cal.getTimeInMillis();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            long endOfTomorrow = cal.getTimeInMillis() - 1;

            List<TaskModel> allTomorrowTasks = dbHelper.getTomorrowTasks(startOfTomorrow, endOfTomorrow);
            List<TaskModel> tomorrowTasks = new ArrayList<>();
            List<TaskModel> completedTasks = new ArrayList<>();
            List<TaskModel> pinnedTasks = new ArrayList<>();

            for (TaskModel t : allTomorrowTasks) {
                if (t.isCompleted()) completedTasks.add(t);
                else if (t.isPinned()) pinnedTasks.add(t);
                else tomorrowTasks.add(t);
            }

            if (!pinnedTasks.isEmpty()) {
                boolean isExpanded = groupStates.getOrDefault("PINNED", true);
                taskList.add(new TaskHeader("ĐÃ GHIM", pinnedTasks.size(), R.color.main_text_secondary, isExpanded, () -> {
                    groupStates.put("PINNED", !isExpanded);
                    loadTasksForList(listId, iconResId, emojiIcon);
                }));
                if (isExpanded) taskList.addAll(pinnedTasks);
            }

            if (!tomorrowTasks.isEmpty()) {
                taskList.add(new TaskHeader("NGÀY MAI", tomorrowTasks.size(), R.color.main_text_secondary, showTomorrow, () -> {
                    showTomorrow = !showTomorrow;
                    loadTasksForList(listId, iconResId, emojiIcon);
                }));
                if (showTomorrow) taskList.addAll(tomorrowTasks);
            }

            if (!completedTasks.isEmpty()) {
                boolean isExpanded = groupStates.getOrDefault("COMPLETED", false);
                taskList.add(new TaskHeader("ĐÃ HOÀN THÀNH", completedTasks.size(), R.color.main_text_secondary, isExpanded, () -> {
                    groupStates.put("COMPLETED", !isExpanded);
                    loadTasksForList(listId, iconResId, emojiIcon);
                }));
                if (isExpanded) taskList.addAll(completedTasks);
            }
        } else if (listId == 3) {
            long now = System.currentTimeMillis();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(now);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long startOfToday = cal.getTimeInMillis();
            cal.add(Calendar.DAY_OF_MONTH, 7);
            long endOfNext7Days = cal.getTimeInMillis() - 1;

            List<TaskModel> allTasks = dbHelper.getAllTasksFor7DaysView(startOfToday, endOfNext7Days);
            List<TaskModel> overdueTasks = new ArrayList<>();
            java.util.Map<String, List<TaskModel>> groupedTasks = new java.util.LinkedHashMap<>();

            java.text.SimpleDateFormat sdfDayOfWeek = new java.text.SimpleDateFormat("E", new java.util.Locale("vi", "VN"));
            java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("d", new java.util.Locale("vi", "VN"));
            java.text.SimpleDateFormat sdfMonth = new java.text.SimpleDateFormat("M", new java.util.Locale("vi", "VN"));

            List<String> orderedKeys = new ArrayList<>();
            Calendar tempCal = Calendar.getInstance();
            tempCal.setTimeInMillis(startOfToday);
            for (int i = 0; i < 7; i++) {
                String key;
                if (i == 0) key = sdfDayOfWeek.format(tempCal.getTime()).toUpperCase() + ", HÔM NAY";
                else if (i == 1) key = sdfDayOfWeek.format(tempCal.getTime()).toUpperCase() + ", NGÀY MAI";
                else key = sdfDayOfWeek.format(tempCal.getTime()).toUpperCase() + ", THG " + sdfMonth.format(tempCal.getTime()) + " " + sdfDate.format(tempCal.getTime());
                orderedKeys.add(key);
                groupedTasks.put(key, new ArrayList<>());
                tempCal.add(Calendar.DAY_OF_MONTH, 1);
            }

            List<TaskModel> completedTasks = new ArrayList<>();
            List<TaskModel> pinnedTasks = new ArrayList<>();
            for (TaskModel t : allTasks) {
                if (t.isCompleted()) completedTasks.add(t);
                else if (t.isPinned()) pinnedTasks.add(t);
                else if (t.getDueDateMillis() > 0 && t.getDueDateMillis() < startOfToday) overdueTasks.add(t);
                else if (t.getDueDateMillis() >= startOfToday && t.getDueDateMillis() <= endOfNext7Days) {
                    long diffMillis = t.getDueDateMillis() - startOfToday;
                    int daysDiff = (int) (diffMillis / (24 * 60 * 60 * 1000L));
                    if (daysDiff >= 0 && daysDiff < 7) {
                        String targetKey = orderedKeys.get(daysDiff);
                        groupedTasks.get(targetKey).add(t);
                    }
                }
            }

            if (!pinnedTasks.isEmpty()) {
                boolean isExpanded = groupStates.getOrDefault("PINNED", true);
                taskList.add(new TaskHeader("ĐÃ GHIM", pinnedTasks.size(), R.color.main_text_secondary, isExpanded, () -> {
                    groupStates.put("PINNED", !isExpanded);
                    loadTasksForList(listId, iconResId, emojiIcon);
                }));
                if (isExpanded) taskList.addAll(pinnedTasks);
            }

            if (!overdueTasks.isEmpty()) {
                boolean isExpanded = groupStates.getOrDefault("QUÁ HẠN", true);
                taskList.add(new TaskHeader("QUÁ HẠN", overdueTasks.size(), R.color.red_delete, isExpanded, () -> {
                    groupStates.put("QUÁ HẠN", !isExpanded);
                    loadTasksForList(listId, iconResId, emojiIcon);
                }));
                if (isExpanded) taskList.addAll(overdueTasks);
            }

            for (String key : orderedKeys) {
                List<TaskModel> tasksInGroup = groupedTasks.get(key);
                if (!tasksInGroup.isEmpty()) {
                    boolean isExpanded = groupStates.getOrDefault(key, true);
                    taskList.add(new TaskHeader(key, tasksInGroup.size(), R.color.main_text_secondary, isExpanded, () -> {
                        groupStates.put(key, !isExpanded);
                        loadTasksForList(listId, iconResId, emojiIcon);
                    }));
                    if (isExpanded) taskList.addAll(tasksInGroup);
                }
            }

            if (!completedTasks.isEmpty()) {
                boolean isExpanded = groupStates.getOrDefault("COMPLETED", false);
                taskList.add(new TaskHeader("ĐÃ HOÀN THÀNH", completedTasks.size(), R.color.main_text_secondary, isExpanded, () -> {
                    groupStates.put("COMPLETED", !isExpanded);
                    loadTasksForList(listId, iconResId, emojiIcon);
                }));
                if (isExpanded) taskList.addAll(completedTasks);
            }
        } else {
            List<TaskModel> allTasks = dbHelper.getTasksByListId(listId);
            List<TaskModel> uncompletedTasks = new ArrayList<>();
            List<TaskModel> completedTasks = new ArrayList<>();
            List<TaskModel> pinnedTasks = new ArrayList<>();

            for (TaskModel t : allTasks) {
                if (t.isCompleted()) completedTasks.add(t);
                else if (t.isPinned()) pinnedTasks.add(t);
                else uncompletedTasks.add(t);
            }

            if (!pinnedTasks.isEmpty()) {
                boolean isExpanded = groupStates.getOrDefault("PINNED", true);
                taskList.add(new TaskHeader("ĐÃ GHIM", pinnedTasks.size(), R.color.main_text_secondary, isExpanded, () -> {
                    groupStates.put("PINNED", !isExpanded);
                    loadTasksForList(listId, iconResId, emojiIcon);
                }));
                if (isExpanded) taskList.addAll(pinnedTasks);
            }

            taskList.addAll(uncompletedTasks);

            if (!completedTasks.isEmpty()) {
                boolean isExpanded = groupStates.getOrDefault("COMPLETED", false);
                taskList.add(new TaskHeader("ĐÃ HOÀN THÀNH", completedTasks.size(), R.color.main_text_secondary, isExpanded, () -> {
                    groupStates.put("COMPLETED", !isExpanded);
                    loadTasksForList(listId, iconResId, emojiIcon);
                }));
                if (isExpanded) taskList.addAll(completedTasks);
            }
        }

        taskAdapter.notifyDataSetChanged();
        if (taskList.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            taskRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            taskRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private int getIconResIdForList(String listName) {
        switch (listName) {
            case "Hôm nay": return R.drawable.ic_today;
            case "Hộp thư đến": return R.drawable.ic_inbox;
            case "Work": return R.drawable.ic_work;
            case "Personal": return R.drawable.ic_personal;
            case "Shopping": return R.drawable.ic_shopping;
            case "Learning": return R.drawable.ic_learning;
            case "Wish List": return R.drawable.ic_wishlist;
            case "Fitness": return R.drawable.ic_fitness;
            default: return 0;
        }
    }

    private void loadTasksForList(int listId) {
        String listName = dbHelper.getListNameById(listId);
        loadTasksForList(listId, getIconResIdForList(listName), null);
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        RecyclerView drawerRecyclerView = findViewById(R.id.drawer_recycler_view);
        drawerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        pinnedRecyclerView = findViewById(R.id.drawer_pinned_recycler_view);
        pinnedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        pinnedItems = dbHelper.getPinnedLists();
        pinnedAdapter = new PinnedListAdapter(pinnedItems, item -> {
            int listId = item.getId();
            if (listId != -1) {
                int resId = dbHelper.getListIconResId(this, listId);
                loadTasksForList(listId, resId, item.getEmojiIcon());
            }
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        pinnedRecyclerView.setAdapter(pinnedAdapter);
        updatePinnedVisibility();

        drawerItems = buildDrawerMenuItems();
        drawerAdapter = new DrawerMenuAdapter(drawerItems);
        drawerRecyclerView.setAdapter(drawerAdapter);

        drawerAdapter.setOnItemClickListener(new DrawerMenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DrawerMenuItem item, int position) {
                if (item.getType() == DrawerMenuItem.ItemType.SEPARATOR) return;
                drawerAdapter.setSelectedPosition(position);
                int listId = item.getId();
                if (listId != -1) loadTasksForList(listId, item.getIconResId(), item.getEmojiIcon());
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            @Override
            public void onItemLongClick(DrawerMenuItem item, int position, View anchorView) {
                if (item.getType() == DrawerMenuItem.ItemType.LIST) showListContextMenu(anchorView, item, position);
            }
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean isLongPressDragEnabled() { return true; }
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    DrawerMenuItem item = drawerItems.get(position);
                    if (item.getType() != DrawerMenuItem.ItemType.LIST || position < 5) return makeMovementFlags(0, 0);
                }
                return super.getMovementFlags(recyclerView, viewHolder);
            }
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int targetPosition = target.getAdapterPosition();
                if (targetPosition < 5 || drawerItems.get(targetPosition).getType() != DrawerMenuItem.ItemType.LIST) return false;
                DrawerMenuItem movedItem = drawerItems.remove(fromPosition);
                drawerItems.add(targetPosition, movedItem);
                drawerAdapter.notifyItemMoved(fromPosition, targetPosition);
                return true;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}
            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                List<String> orderedListNames = new ArrayList<>();
                for (DrawerMenuItem item : drawerItems) {
                    if (item.getType() == DrawerMenuItem.ItemType.LIST && drawerItems.indexOf(item) >= 5) orderedListNames.add(item.getTitle());
                }
                dbHelper.updateListOrder(orderedListNames);
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(drawerRecyclerView);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(drawerRecyclerView);

        // Header buttons
        findViewById(R.id.drawer_btn_search)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    startActivity(intent);
                });
        findViewById(R.id.drawer_btn_settings)
                .setOnClickListener(v -> Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show());

        // Bottom bar
        findViewById(R.id.drawer_btn_add).setOnClickListener(this::showAddMenuPopup);
        findViewById(R.id.drawer_btn_filter).setOnClickListener(v -> Toast.makeText(this, "Bộ lọc", Toast.LENGTH_SHORT).show());
        refreshDrawerBadges();
    }

    private List<DrawerMenuItem> buildDrawerMenuItems() {
        List<DrawerMenuItem> items = new ArrayList<>();
        items.add(new DrawerMenuItem(1, "Hôm nay", R.drawable.ic_today, DrawerMenuItem.ItemType.NAVIGATION).setSelected(true));
        items.add(new DrawerMenuItem(2, "Ngày mai", R.drawable.ic_quick_tomorrow, DrawerMenuItem.ItemType.NAVIGATION));
        items.add(new DrawerMenuItem(3, "7 ngày tới", R.drawable.ic_quick_next_week, DrawerMenuItem.ItemType.NAVIGATION));
        items.add(new DrawerMenuItem(4, "Hộp thư đến", R.drawable.ic_inbox, DrawerMenuItem.ItemType.NAVIGATION));
        items.add(DrawerMenuItem.separator());
        List<DrawerMenuItem> customLists = dbHelper.getAllCustomLists();
        for (DrawerMenuItem customItem : customLists) {
            String iconName = customItem.getEmojiIcon();
            if (iconName != null && iconName.startsWith("ic_")) {
                int resId = getResources().getIdentifier(iconName, "drawable", getPackageName());
                customItem.setIconResId(resId);
                customItem.setEmojiIcon(null);
            }
            items.add(customItem);
        }
        items.add(DrawerMenuItem.separator());
        return items;
    }

    public void addNewListToDrawer(String name, String emojiIcon) {
        drawerItems.clear();
        drawerItems.addAll(buildDrawerMenuItems());
        drawerAdapter.notifyDataSetChanged();
        if (drawerItems.size() > 5) ((RecyclerView)findViewById(R.id.drawer_recycler_view)).smoothScrollToPosition(5);
    }

    public void updateListInDrawer(int listId, String newName, String newEmojiIcon, int position) {
        dbHelper.updateList(listId, newName, newEmojiIcon);
        DrawerMenuItem item = drawerItems.get(position);
        item.setTitle(newName);
        item.setEmojiIcon(newEmojiIcon);
        drawerAdapter.notifyItemChanged(position);
        if (item.isSelected()) updateToolbarForList(newName, item.getIconResId(), newEmojiIcon);
        if (pinnedItems != null && pinnedAdapter != null) {
            pinnedItems.clear();
            pinnedItems.addAll(dbHelper.getPinnedLists());
            pinnedAdapter.notifyDataSetChanged();
            updatePinnedVisibility();
        }
    }

    public void deleteListFromDrawer(int listId, int position) {
        dbHelper.deleteList(listId);
        DrawerMenuItem deletedItem = drawerItems.remove(position);
        drawerAdapter.notifyItemRemoved(position);
        if (deletedItem.isSelected()) {
            DrawerMenuItem todayItem = drawerItems.get(0);
            drawerAdapter.setSelectedPosition(0);
            loadTasksForList(1, todayItem.getIconResId(), todayItem.getEmojiIcon());
        }
        if (pinnedItems != null && pinnedAdapter != null) {
            pinnedItems.clear();
            pinnedItems.addAll(dbHelper.getPinnedLists());
            pinnedAdapter.notifyDataSetChanged();
            updatePinnedVisibility();
        }
    }

    private void refreshDrawerBadges() {
        Map<Integer, Integer> counts = dbHelper.getAllListTaskCounts();
        for (DrawerMenuItem item : drawerItems) {
            if (item.getType() == DrawerMenuItem.ItemType.SEPARATOR) continue;
            int listId = item.getId();
            item.setBadgeCount((listId != -1 && counts.containsKey(listId)) ? counts.get(listId) : 0);
        }
        drawerAdapter.notifyDataSetChanged();
    }

    private void setupTaskRecyclerView() {
        taskRecyclerView = findViewById(R.id.task_recycler_view);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        taskAdapter.setOnTaskClickListener(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(TaskModel task) {
                TaskDetailBottomSheet bottomSheet = new TaskDetailBottomSheet(task);
                bottomSheet.setOnTaskUpdatedListener(() -> {
                    Log.d(TAG, "UI: TaskDetail updated, calling rescheduleReminders");
                    loadTasksForList(currentListId);
                    rescheduleReminders();
                });
                bottomSheet.show(getSupportFragmentManager(), "TaskDetailBottomSheet");
            }
            @Override
            public void onTaskCheckedChanged(TaskModel task, boolean isChecked) {
                dbHelper.updateTaskCompleted(task.getId(), isChecked);
                loadTasksForList(currentListId);
                rescheduleReminders();
            }
            @Override
            public void onTaskPinClicked(TaskModel task) {
                dbHelper.updateTaskPinned(task.getId(), !task.isPinned());
                loadTasksForList(currentListId);
                rescheduleReminders();
            }
            @Override
            public void onTaskDeleteClicked(TaskModel task) {
                dbHelper.deleteTask(task.getId());
                loadTasksForList(currentListId);
                rescheduleReminders();
            }
            @Override
            public void onTaskMoveClicked(TaskModel task) {
                MoveTaskBottomSheet moveSheet = new MoveTaskBottomSheet(task.getId(), task.getListId());
                moveSheet.setOnTaskMovedListener((newListId, newListName, iconName) -> {
                    loadTasksForList(currentListId);
                    rescheduleReminders();
                    showMoveToast(newListName, iconName);
                });
                moveSheet.show(getSupportFragmentManager(), "MoveTaskBottomSheet");
            }
            @Override
            public void onTaskDateClicked(TaskModel task) {
                DatePickerBottomSheet datePicker = new DatePickerBottomSheet();
                datePicker.setPreSelectedDate(task.getDueDateMillis());
                datePicker.setOnDateSelectedListener((dateTag, dateMillis) -> {
                    dbHelper.updateTaskDate(task.getId(), dateTag, dateMillis);
                    loadTasksForList(currentListId);
                    rescheduleReminders();
                });
                datePicker.setOnDateClearedListener(() -> {
                    dbHelper.updateTaskDate(task.getId(), null, 0);
                    loadTasksForList(currentListId);
                    rescheduleReminders();
                });
                datePicker.show(getSupportFragmentManager(), "DatePickerBottomSheet");
            }
        });
        taskRecyclerView.setAdapter(taskAdapter);
        new ItemTouchHelper(new TaskSwipeHelper(this)).attachToRecyclerView(taskRecyclerView);
    }

    private void rescheduleReminders() {
        Log.d(TAG, "rescheduleReminders: isBound=" + isBound + ", reminderService=" + (reminderService != null));
        if (isBound && reminderService != null) {
            reminderService.scheduleAllReminders();
        } else {
            Intent intent = new Intent(this, ReminderService.class);
            startService(intent);
        }
    }

    private void showMoveToast(String newListName, String iconName) {
        View layout = getLayoutInflater().inflate(R.layout.layout_toast_move, null);
        ImageView ivIcon = layout.findViewById(R.id.toast_icon);
        TextView tvEmoji = layout.findViewById(R.id.toast_emoji);
        TextView tvText = layout.findViewById(R.id.toast_text);
        tvText.setText("Đã di chuyển đến " + newListName);
        if (iconName != null && !iconName.isEmpty()) {
            if (iconName.startsWith("ic_")) {
                int resId = getResources().getIdentifier(iconName, "drawable", getPackageName());
                if (resId != 0) { ivIcon.setImageResource(resId); ivIcon.setVisibility(View.VISIBLE); }
            } else { tvEmoji.setText(iconName); tvEmoji.setVisibility(View.VISIBLE); }
        }
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

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
        View dateChipContainer = sheetView.findViewById(R.id.date_chip_container);
        TextView dateChipText = sheetView.findViewById(R.id.date_chip_text);
        View actionDate = sheetView.findViewById(R.id.action_date);

        final String[] selectedDateTag = { "" };
        final long[] selectedDateMillis = { -1 };
        final int[] selectedHour = { -1 };
        final int[] selectedMinute = { -1 };

        textCurrentList.setText(dbHelper.getListNameById(currentListId));

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
                selectedDateTag[0] = ""; selectedDateMillis[0] = -1;
                selectedHour[0] = -1; selectedMinute[0] = -1;
                dateChipContainer.setVisibility(View.GONE);
                actionDate.setVisibility(View.VISIBLE);
            });
            datePicker.setOnTimeSelectedListener((h, m) -> { selectedHour[0] = h; selectedMinute[0] = m; });
            if (selectedDateMillis[0] > 0) datePicker.setPreSelectedDate(selectedDateMillis[0]);
            if (selectedHour[0] >= 0) datePicker.setPreSelectedTime(selectedHour[0], selectedMinute[0]);
            datePicker.show(getSupportFragmentManager(), "date_picker");
        };

        actionDate.setOnClickListener(v -> openDatePicker.run());
        dateChipContainer.setOnClickListener(v -> openDatePicker.run());
        sheetView.findViewById(R.id.btn_submit_task).setOnClickListener(v -> {
            String title = inputTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) { inputTitle.setError("Nhập tiêu đề task"); inputTitle.requestFocus(); return; }

            // FIX: Gộp giờ phút vào Millis nếu có chọn thời gian
            long finalDueDate = selectedDateMillis[0];
            if (finalDueDate > 0 && selectedHour[0] >= 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(finalDueDate);
                cal.set(Calendar.HOUR_OF_DAY, selectedHour[0]);
                cal.set(Calendar.MINUTE, selectedMinute[0]);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                finalDueDate = cal.getTimeInMillis();
            }

            Log.d(TAG, "UI: Click Save. Title=" + title + ", FinalMillis=" + finalDueDate);

            dbHelper.insertTask(title, inputDescription.getText().toString().trim(), currentListId, selectedDateTag[0], finalDueDate);

            loadTasksForList(currentListId);
            rescheduleReminders();
            bottomSheet.dismiss();
            Toast.makeText(this, "Đã thêm: " + title, Toast.LENGTH_SHORT).show();
        });
        bottomSheet.show();
        inputTitle.requestFocus();
    }

    private void setupBottomNavigation() {
        ((BottomNavigationView)findViewById(R.id.bottom_navigation)).setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_calendar) Toast.makeText(this, "Lịch", Toast.LENGTH_SHORT).show();
            else if (id == R.id.nav_settings) Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START);
                else { setEnabled(false); getOnBackPressedDispatcher().onBackPressed(); }
            }
        });
    }

    private int getDateChipColor(String dateTag) {
        switch (dateTag) {
            case "Hôm nay": return Color.parseColor("#4C6FE0");
            case "Ngày mai": return Color.parseColor("#FFA726");
            case "Thứ Hai tới": return Color.parseColor("#42A5F5");
            case "Đến cuối ngày": return Color.parseColor("#66BB6A");
            default: return Color.parseColor("#B0B0B0");
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) { getMenuInflater().inflate(R.menu.toolbar_menu, menu); return true; }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_smart_suggest) Toast.makeText(this, "Gợi ý thông minh", Toast.LENGTH_SHORT).show();
        else if (id == R.id.action_more) Toast.makeText(this, "Thêm tùy chọn", Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    private void showAddMenuPopup(View anchorView) {
        View popupView = getLayoutInflater().inflate(R.layout.layout_popup_add, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(8f);
        popupView.findViewById(R.id.popup_item_list).setOnClickListener(v -> { popupWindow.dismiss(); AddListDialogFragment.newInstance().show(getSupportFragmentManager(), "AddListDialog"); });
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWindow.showAsDropDown(anchorView, dpToPx(16), -(anchorView.getHeight() + popupView.getMeasuredHeight() + dpToPx(8)));
    }

    private void showListContextMenu(View anchorView, DrawerMenuItem item, int position) {
        View popupView = getLayoutInflater().inflate(R.layout.layout_popup_list_options, null);
        PopupWindow popupWindow = new PopupWindow(popupView, dpToPx(180), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(8f);
        popupView.findViewById(R.id.popup_action_edit).setOnClickListener(v -> {
            popupWindow.dismiss();
            if (item.getId() != -1) AddListDialogFragment.newInstanceForEdit(item.getId(), item.getTitle(), item.getEmojiIcon(), position).show(getSupportFragmentManager(), "EditListDialog");
        });
        int listId = item.getId();
        TextView pinActionText = popupView.findViewById(R.id.popup_action_pin).findViewById(R.id.text_pin_action);
        if (pinActionText != null && listId != -1) pinActionText.setText(dbHelper.isListPinned(listId) ? "Bỏ ghim" : "Đính ghim");
        popupView.findViewById(R.id.popup_action_pin).setOnClickListener(v -> {
            popupWindow.dismiss();
            if (listId != -1) {
                boolean isCurrentlyPinned = dbHelper.isListPinned(listId);
                dbHelper.togglePinList(listId, !isCurrentlyPinned);
                pinnedItems.clear(); pinnedItems.addAll(dbHelper.getPinnedLists());
                pinnedAdapter.notifyDataSetChanged(); updatePinnedVisibility();
                Toast.makeText(this, (!isCurrentlyPinned ? "Đã đính ghim: " : "Đã bỏ ghim: ") + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        popupView.findViewById(R.id.popup_action_delete).setOnClickListener(v -> {
            popupWindow.dismiss();
            if (listId != -1) {
                String title = "Bạn có muốn xóa danh sách \"" + (item.getEmojiIcon() != null ? item.getEmojiIcon() + " " : "") + item.getTitle() + "\" không?";
                new MaterialAlertDialogBuilder(this).setTitle(title).setMessage("Tất cả các nhiệm vụ trong danh sách sẽ bị xóa.")
                        .setNegativeButton("Hủy bỏ", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Xóa", (dialog, which) -> { deleteListFromDrawer(listId, position); Toast.makeText(this, "Đã xóa danh sách", Toast.LENGTH_SHORT).show(); }).show();
            }
        });
        popupWindow.showAsDropDown(anchorView, dpToPx(32), -dpToPx(24));
    }

    private void updatePinnedVisibility() { pinnedRecyclerView.setVisibility((pinnedItems != null && !pinnedItems.isEmpty()) ? View.VISIBLE : View.GONE); }

    private ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
            reminderService = ((ReminderService.LocalBinder) service).getService();
            isBound = true;
            Log.d(TAG, "onServiceConnected: Service Connected");
            reminderService.scheduleAllReminders();
        }
        @Override public void onServiceDisconnected(ComponentName name) { isBound = false; }
    };
    @Override protected void onStart() { super.onStart(); bindService(new Intent(this, ReminderService.class), connection, Context.BIND_AUTO_CREATE); }
    @Override protected void onStop() { super.onStop(); if (isBound) { unbindService(connection); isBound = false; } }
    @Override protected void onResume() { super.onResume(); if (getIntent().hasExtra("EXTRA_TASK_ID")) { int taskId = getIntent().getIntExtra("EXTRA_TASK_ID", -1); getIntent().removeExtra("EXTRA_TASK_ID"); if (taskId != -1) showTaskDetailById(taskId); } }
    private void showTaskDetailById(int taskId) {
        TaskModel task = dbHelper.getTaskById(taskId);
        if (task != null) new TaskDetailBottomSheet(task).show(getSupportFragmentManager(), "TaskDetailBottomSheet");
    }
}