package hcmute.edu.vn.lequanghung_23110110.ticktick.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;

public class TaskModel implements TaskListItem {

    private int id; // DB primary key
    private String title;
    private String description;
    private int listId; // FK → lists._id
    private String dateTag;
    private long dueDateMillis; // Actual time
    private boolean isCompleted;
    private boolean isPinned;
    private List<String> reminders;
    private List<String> imageUris; // Danh sách URI của ảnh đính kèm

    // Constructor đầy đủ (từ DB)
    public TaskModel(int id, String title, String description, int listId, String dateTag, long dueDateMillis,
            boolean isCompleted, boolean isPinned, List<String> reminders, List<String> imageUris) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.listId = listId;
        this.dateTag = dateTag;
        this.dueDateMillis = dueDateMillis;
        this.isCompleted = isCompleted;
        this.isPinned = isPinned;
        this.reminders = reminders != null ? reminders : new ArrayList<>();
        this.imageUris = imageUris != null ? imageUris : new ArrayList<>();
    }

    // Constructor đầy đủ cũ (để tương thích với code hiện tại)
    public TaskModel(int id, String title, String description, int listId, String dateTag, long dueDateMillis,
            boolean isCompleted, boolean isPinned, List<String> reminders) {
        this(id, title, description, listId, dateTag, dueDateMillis, isCompleted, isPinned, reminders, new ArrayList<>());
    }

    // Constructor tạo mới (chưa có id)
    public TaskModel(String title, String description, int listId, String dateTag, long dueDateMillis, List<String> reminders) {
        this.id = -1;
        this.title = title;
        this.description = description;
        this.listId = listId;
        this.dateTag = dateTag;
        this.dueDateMillis = dueDateMillis;
        this.isCompleted = false;
        this.isPinned = false;
        this.reminders = reminders != null ? reminders : new ArrayList<>();
        this.imageUris = new ArrayList<>();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public List<String> getReminders() {
        return reminders;
    }

    public void setReminders(List<String> reminders) {
        this.reminders = reminders != null ? reminders : new ArrayList<>();
    }

    public List<String> getImageUris() {
        return imageUris;
    }

    public void setImageUris(List<String> imageUris) {
        this.imageUris = imageUris != null ? imageUris : new ArrayList<>();
    }

    /**
     * Chuyển đổi List<String> imageUris thành JSON String để lưu vào database
     * @return JSON string hoặc chuỗi rỗng nếu không có ảnh
     */
    public String getImageUrisJson() {
        if (imageUris == null || imageUris.isEmpty()) {
            return "";
        }
        return new Gson().toJson(imageUris);
    }

    /**
     * Parse JSON String từ database thành List<String> imageUris
     * @param json JSON string từ database
     */
    public void setImageUrisFromJson(String json) {
        if (json == null || json.isEmpty()) {
            this.imageUris = new ArrayList<>();
            return;
        }
        
        try {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> parsed = new Gson().fromJson(json, listType);
            this.imageUris = parsed != null ? parsed : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            this.imageUris = new ArrayList<>();
        }
    }

    @Override
    public int getType() {
        return TYPE_TASK;
    }
}