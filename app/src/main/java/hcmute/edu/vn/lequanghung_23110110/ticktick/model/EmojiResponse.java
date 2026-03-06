package hcmute.edu.vn.lequanghung_23110110.ticktick.model;

import com.google.gson.annotations.SerializedName;

public class EmojiResponse {
    @SerializedName("character")
    public String character;

    @SerializedName("unicodeName")
    public String name;

    @SerializedName("group")
    public String group;
}
