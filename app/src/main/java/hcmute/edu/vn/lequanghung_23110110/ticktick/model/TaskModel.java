package hcmute.edu.vn.lequanghung_23110110.ticktick.model;

public class TaskModel implements TaskListItem {

    private int id; // DB primary key
    private String title;
    private int listId; // FK → lists._id
    private String dateTag;
    private long dueDateMillis; // Actual time
    private boolean isCompleted;

    // Constructor đầy đủ (từ DB)
    public TaskModel(int id, String title, int listId, String dateTag, long dueDateMillis, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.listId = listId;
        this.dateTag = dateTag;
        this.dueDateMillis = dueDateMillis;
        this.isCompleted = isCompleted;
    }

    // Constructor tạo mới (chưa có id)
    public TaskModel(String title, int listId, String dateTag, long dueDateMillis) {
        this.id = -1;
        this.title = title;
        this.listId = listId;
        this.dateTag = dateTag;
        this.dueDateMillis = dueDateMillis;
        this.isCompleted = false;
    }

    // --- Getters & Setters ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public String getDateTag() {
        return dateTag;
    }

    public void setDateTag(String dateTag) {
        this.dateTag = dateTag;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getDueDateMillis() {
        return dueDateMillis;
    }

    public void setDueDateMillis(long dueDateMillis) {
        this.dueDateMillis = dueDateMillis;
    }

    @Override
    public int getType() {
        return TYPE_TASK;
    }
}