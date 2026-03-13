package hcmute.edu.vn.lequanghung_23110110.ticktick.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EmojiHubResponse {
    @SerializedName("name")
    public String name;

    @SerializedName("category")
    public String category;

    @SerializedName("group")
    public String group;

    @SerializedName("htmlCode")
    public List<String> htmlCode;

    @SerializedName("unicode")
    public List<String> unicode;
}
