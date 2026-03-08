package hcmute.edu.vn.lequanghung_23110110.ticktick.model;

public class ListModel {
    private int id;
    private String name;
    private String iconName;

    public ListModel(int id, String name, String iconName) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIconName() {
        return iconName;
    }
}
