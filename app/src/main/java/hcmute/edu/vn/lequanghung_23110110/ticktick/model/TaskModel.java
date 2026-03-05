package hcmute.edu.vn.lequanghung_23110110.ticktick.model;

public class TaskModel {

    private String title;
    private String dateTag;
    private boolean isCompleted;

    public TaskModel(String title, String dateTag, boolean isCompleted) {
        this.title = title;
        this.dateTag = dateTag;
        this.isCompleted = isCompleted;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDateTag() { return dateTag; }
    public void setDateTag(String dateTag) { this.dateTag = dateTag; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
