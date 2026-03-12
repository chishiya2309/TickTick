package hcmute.edu.vn.lequanghung_23110110.ticktick.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.TaskAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskListItem;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private View emptyStateContainer;
    private RecyclerView searchResultsRecyclerView;
    private TaskAdapter taskAdapter;
    private ArrayList<TaskListItem> searchResults;

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
        emptyStateContainer = findViewById(R.id.search_empty_state_container);
        searchResultsRecyclerView = findViewById(R.id.search_results_recycler_view);

        // Cancel button
        findViewById(R.id.btn_cancel_search).setOnClickListener(v -> {
            // Hide keyboard before finishing
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
            }
            finish();
        });

        searchResults = new ArrayList<>();
        taskAdapter = new TaskAdapter(searchResults);
        taskAdapter.setOnTaskClickListener(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(TaskModel task) {
                // Handle task click
            }

            @Override
            public void onTaskCheckedChanged(TaskModel task, boolean isChecked) {
                // Handle checkbox
            }

            @Override
            public void onTaskPinClicked(TaskModel task) {
                // Handle pin
            }

            @Override
            public void onTaskDeleteClicked(TaskModel task) {
                // Handle delete
            }

            @Override
            public void onTaskMoveClicked(TaskModel task) {
                // Handle move
            }

            @Override
            public void onTaskDateClicked(TaskModel task) {
                // Handle date
            }
        });

        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecyclerView.setAdapter(taskAdapter);
    }

    private void setupSearchLogic() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    // Show empty state
                    emptyStateContainer.setVisibility(View.VISIBLE);
                    searchResultsRecyclerView.setVisibility(View.GONE);
                    searchResults.clear();
                    taskAdapter.notifyDataSetChanged();
                } else {
                    // TODO: Implement actual search logic here
                    emptyStateContainer.setVisibility(View.GONE);
                    searchResultsRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
