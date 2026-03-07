package hcmute.edu.vn.lequanghung_23110110.ticktick.model;

public class TaskHeader implements TaskListItem {
    private String title;
    private int count;
    private int colorResId; // To color text like Overdue (Red)
    private boolean isExpanded;
    private Runnable onToggleListener;

    public TaskHeader(String title, int count, int colorResId, boolean isExpanded, Runnable onToggleListener) {
        this.title = title;
        this.count = count;
        this.colorResId = colorResId;
        this.isExpanded = isExpanded;
        this.onToggleListener = onToggleListener;
    }

    public String getTitle() {
        return title;
    }

    public int getCount() {
        return count;
    }

    public int getColorResId() {
        return colorResId;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public Runnable getOnToggleListener() {
        return onToggleListener;
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }
}
