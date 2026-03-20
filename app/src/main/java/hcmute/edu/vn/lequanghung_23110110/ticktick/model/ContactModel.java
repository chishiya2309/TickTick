package hcmute.edu.vn.lequanghung_23110110.ticktick.model;

import java.util.Objects;

public class ContactModel {
    private String id;
    private String name;
    private String phoneNumber;
    private String photoUri;

    public ContactModel(String id, String name, String phoneNumber, String photoUri) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.photoUri = photoUri;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactModel that = (ContactModel) o;
        return Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber);
    }
}
