package hcmute.edu.vn.lequanghung_23110110.ticktick.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.TaskAdapter;

public class TaskSwipeHelper extends ItemTouchHelper.Callback {

    private final Context context;
    private List<Integer> swipedRightTaskIds = new ArrayList<>();
    private List<Integer> swipedLeftTaskIds = new ArrayList<>();
    private float rightSwipeWidth = 0f;
    private float leftSwipeWidth = 0f;

    public TaskSwipeHelper(Context context) {
        this.context = context;
        this.rightSwipeWidth = dpToPx(context, 140);
        this.leftSwipeWidth = dpToPx(context, 210);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof TaskAdapter.HeaderViewHolder) {
            return 0; // Headers are not swipable
        }
        return makeMovementFlags(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Not used
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return Integer.MAX_VALUE;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState,
            boolean isCurrentlyActive) {

        if (!(viewHolder instanceof TaskAdapter.TaskViewHolder)) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        TaskAdapter.TaskViewHolder holder = (TaskAdapter.TaskViewHolder) viewHolder;
        View foreground = holder.foreground;
        int taskId = holder.getTaskId();

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            float translationX = dX;
            boolean isSwipedRight = swipedRightTaskIds.contains(taskId);
            boolean isSwipedLeft = swipedLeftTaskIds.contains(taskId);

            if (isSwipedRight) {
                if (dX < 0) {
                    translationX = rightSwipeWidth + dX;
                    if (translationX <= rightSwipeWidth / 2 && !isCurrentlyActive) {
                        swipedRightTaskIds.remove(Integer.valueOf(taskId));
                        translationX = 0;
                    }
                } else {
                    translationX = dX > 0 ? rightSwipeWidth + dX : rightSwipeWidth;
                }
            } else if (isSwipedLeft) {
                if (dX > 0) {
                    translationX = -leftSwipeWidth + dX;
                    if (translationX >= -leftSwipeWidth / 2 && !isCurrentlyActive) {
                        swipedLeftTaskIds.remove(Integer.valueOf(taskId));
                        translationX = 0;
                    }
                } else {
                    translationX = dX < 0 ? -leftSwipeWidth + dX : -leftSwipeWidth;
                }
            } else {
                if (dX >= rightSwipeWidth / 2 && !isCurrentlyActive) {
                    swipedRightTaskIds.add(taskId);
                    translationX = rightSwipeWidth;
                } else if (dX <= -leftSwipeWidth / 2 && !isCurrentlyActive) {
                    swipedLeftTaskIds.add(taskId);
                    translationX = -leftSwipeWidth;
                }
            }

            getDefaultUIUtil().onDraw(c, recyclerView, foreground, translationX, dY, actionState, isCurrentlyActive);
            return;
        }

        getDefaultUIUtil().onDraw(c, recyclerView, foreground, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof TaskAdapter.TaskViewHolder) {
            TaskAdapter.TaskViewHolder holder = (TaskAdapter.TaskViewHolder) viewHolder;
            View foreground = holder.foreground;
            int taskId = holder.getTaskId();

            if (swipedRightTaskIds.contains(taskId)) {
                foreground.setTranslationX(rightSwipeWidth);
            } else if (swipedLeftTaskIds.contains(taskId)) {
                foreground.setTranslationX(-leftSwipeWidth);
            } else {
                foreground.setTranslationX(0f);
            }
            getDefaultUIUtil().clearView(foreground);
        } else {
            getDefaultUIUtil().clearView(viewHolder.itemView);
        }
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder instanceof TaskAdapter.TaskViewHolder) {
            View foreground = ((TaskAdapter.TaskViewHolder) viewHolder).foreground;
            getDefaultUIUtil().onSelected(foreground);
        } else if (viewHolder != null) {
            super.onSelectedChanged(viewHolder, actionState);
        }
    }

    private int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
