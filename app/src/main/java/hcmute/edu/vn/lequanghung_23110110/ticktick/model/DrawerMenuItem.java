package hcmute.edu.vn.lequanghung_23110110.ticktick.model;

//> **Giải thích**: Model chứa thông tin cho mỗi item trong drawer.
//> Có thể set `iconResId` cho vector drawable, `badgeCount` cho badge, `hasChevron` cho mũi tên phải.
public class DrawerMenuItem {

    public enum ItemType {
        NAVIGATION, // Hôm nay, Hộp thư đến, Lịch
        LIST, // Work, Personal, Shopping...
        SEPARATOR // Đường kẻ phân cách
    }

    private int id; // ID từ Database (vd: list_id)
    private String title;
    private int iconResId; // Dành cho Vector/Drawable
    private String emojiIcon; // Dành cho Emoji dạng text (ưu tiên nếu khác null)
    private ItemType type;
    private int badgeCount;
    private boolean hasChevron;
    private boolean selected;

    // Constructor cho item bình thường
    public DrawerMenuItem(int id, String title, int iconResId, ItemType type) {
        this.id = id;
        this.title = title;
        this.iconResId = iconResId;
        this.type = type;
        this.emojiIcon = null;
    }

    // Constructor mới: cho emoji icon (dạng text)
    public DrawerMenuItem(int id, String title, String emojiIcon, ItemType type) {
        this.id = id;
        this.title = title;
        this.emojiIcon = emojiIcon;
        this.type = type;
        this.iconResId = 0;
    }

    // Constructor cho separator
    public static DrawerMenuItem separator() {
        DrawerMenuItem item = new DrawerMenuItem(-1, "", 0, ItemType.SEPARATOR);
        return item;
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

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getEmojiIcon() {
        return emojiIcon;
    }

    public DrawerMenuItem setEmojiIcon(String emojiIcon) {
        this.emojiIcon = emojiIcon;
        return this;
    }

    public ItemType getType() {
        return type;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public DrawerMenuItem setBadgeCount(int count) {
        this.badgeCount = count;
        return this;
    }

    public boolean hasChevron() {
        return hasChevron;
    }

    public DrawerMenuItem setHasChevron(boolean hasChevron) {
        this.hasChevron = hasChevron;
        return this;
    }

    public boolean isSelected() {
        return selected;
    }

    public DrawerMenuItem setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }
}
