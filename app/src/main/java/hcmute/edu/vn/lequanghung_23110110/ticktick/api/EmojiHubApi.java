package hcmute.edu.vn.lequanghung_23110110.ticktick.api;

import java.util.List;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.EmojiHubResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface EmojiHubApi {
    @GET("all")
    Call<List<EmojiHubResponse>> getAllEmojis();
}
