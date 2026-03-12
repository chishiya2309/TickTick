package hcmute.edu.vn.lequanghung_23110110.ticktick.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.SearchListAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.SearchTaskAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.DrawerMenuItem;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private ImageView btnClearSearch;
    private View emptyStateContainer;
    private View searchResultsContainer;
    
    private View tasksSection;
    private RecyclerView tasksRecyclerView;
    private TextView tvTasksSeeMore;
    
    private View listsSection;
    private RecyclerView listsRecyclerView;

    private SearchTaskAdapter searchTaskAdapter;
    private SearchListAdapter searchListAdapter;
    private TaskDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        // Edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = TaskDatabaseHelper.getInstance(this);

        setupViews();
        setupSearchLogic();

        // Auto-focus the search input and show keyboard
        searchInput.requestFocus();
        searchInput.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);
    }

    private void setupViews() {
        searchInput = findViewById(R.id.search_input);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        emptyStateContainer = findViewById(R.id.search_empty_state_container);
        searchResultsContainer = findViewById(R.id.search_results_container);

        tasksSection = findViewById(R.id.search_tasks_section);
        tasksRecyclerView = findViewById(R.id.search_tasks_recycler_view);
        tvTasksSeeMore = findViewById(R.id.search_tasks_see_more);

        listsSection = findViewById(R.id.search_lists_section);
        listsRecyclerView = findViewById(R.id.search_lists_recycler_view);

        // Cancel button
        findViewById(R.id.btn_cancel_search).setOnClickListener(v -> {
            // Hide keyboard before finishing
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
            }
            finish();
        });

        // Clear button
        btnClearSearch.setOnClickListener(v -> {
            searchInput.setText("");
        });

        // Adapters
        searchTaskAdapter = new SearchTaskAdapter(new ArrayList<>(), task -> {
            hcmute.edu.vn.lequanghung_23110110.ticktick.dialog.TaskDetailBottomSheet bottomSheet = 
                    new hcmute.edu.vn.lequanghung_23110110.ticktick.dialog.TaskDetailBottomSheet(task);
            bottomSheet.setHighlightKeyword(searchInput.getText().toString().trim());
            bottomSheet.setOnTaskUpdatedListener(() -> {
                performSearch(searchInput.getText().toString().trim());
            });
            bottomSheet.show(getSupportFragmentManager(), "TaskDetailBottomSheet");
        });
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksRecyclerView.setAdapter(searchTaskAdapter);

        searchListAdapter = new SearchListAdapter(new ArrayList<>(), list -> {
            android.content.Intent intent = new android.content.Intent(SearchActivity.this, MainActivity.class);
            intent.putExtra("EXTRA_LIST_ID", list.getId());
            intent.putExtra("EXTRA_LIST_ICON_RES_ID", list.getIconResId());
            if (list.getEmojiIcon() != null) {
                intent.putExtra("EXTRA_LIST_EMOJI", list.getEmojiIcon());
            }
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        listsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listsRecyclerView.setAdapter(searchListAdapter);
    }

    private void setupSearchLogic() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    btnClearSearch.setVisibility(View.GONE);
                    emptyStateContainer.setVisibility(View.VISIBLE);
                    searchResultsContainer.setVisibility(View.GONE);
                    searchTaskAdapter.updateData(new ArrayList<>(), "");
                    searchListAdapter.updateData(new ArrayList<>(), "");
                } else {
                    btnClearSearch.setVisibility(View.VISIBLE);
                    performSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        List<TaskModel> matchedTasks = dbHelper.searchTasks(query);
        List<DrawerMenuItem> matchedLists = dbHelper.searchLists(query);

        if (matchedTasks.isEmpty() && matchedLists.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            searchResultsContainer.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            searchResultsContainer.setVisibility(View.VISIBLE);

            // Handle Tasks Section
            if (matchedTasks.isEmpty()) {
                tasksSection.setVisibility(View.GONE);
            } else {
                tasksSection.setVisibility(View.VISIBLE);
                if (matchedTasks.size() > 4) {
                    searchTaskAdapter.updateData(matchedTasks.subList(0, 4), query);
                    tvTasksSeeMore.setVisibility(View.VISIBLE);
                } else {
                    searchTaskAdapter.updateData(matchedTasks, query);
                    tvTasksSeeMore.setVisibility(View.GONE);
                }
            }

            // Handle Lists Section
            if (matchedLists.isEmpty()) {
                listsSection.setVisibility(View.GONE);
            } else {
                listsSection.setVisibility(View.VISIBLE);
                searchListAdapter.updateData(matchedLists, query);
            }
        }
    }
}
