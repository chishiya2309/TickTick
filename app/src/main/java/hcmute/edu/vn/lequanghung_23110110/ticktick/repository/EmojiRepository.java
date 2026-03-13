package hcmute.edu.vn.lequanghung_23110110.ticktick.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.api.EmojiHubApi;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.EmojiHubResponse;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.EmojiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EmojiRepository {
    private static final String PREF_NAME = "ticktick_emojis";
    private static final String KEY_EMOJIS_JSON = "emojis_json_cache";
    private static EmojiRepository instance;
    private final EmojiHubApi api;
    private final Gson gson;

    private EmojiRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://emojihub.yurace.pro/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(EmojiHubApi.class);
        gson = new Gson();
    }

    public static synchronized EmojiRepository getInstance() {
        if (instance == null) {
            instance = new EmojiRepository();
        }
        return instance;
    }

    public interface FetchCallback {
        void onSuccess(List<EmojiResponse> emojis);
        void onError(String message);
    }

    public void getEmojis(Context context, FetchCallback callback) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String cachedJson = prefs.getString(KEY_EMOJIS_JSON, null);

        if (cachedJson != null && !cachedJson.isEmpty()) {
            Type listType = new TypeToken<List<EmojiResponse>>() {}.getType();
            List<EmojiResponse> cachedEmojis = gson.fromJson(cachedJson, listType);
            if (cachedEmojis != null && !cachedEmojis.isEmpty()) {
                Log.d("EmojiRepository", "Loaded emojis from cache");
                callback.onSuccess(cachedEmojis);
                return;
            }
        }

        Log.d("EmojiRepository", "Fetching emojis from API");
        api.getAllEmojis().enqueue(new Callback<List<EmojiHubResponse>>() {
            @Override
            public void onResponse(Call<List<EmojiHubResponse>> call, Response<List<EmojiHubResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EmojiResponse> parsedEmojis = new ArrayList<>();
                    for (EmojiHubResponse hubEmoji : response.body()) {
                        if (hubEmoji.htmlCode != null && !hubEmoji.htmlCode.isEmpty()) {
                            EmojiResponse emoji = new EmojiResponse();
                            emoji.name = hubEmoji.name;
                            emoji.group = hubEmoji.group;
                            
                            // Convert HTML entity (e.g., &#128512;) to String Emoji
                            String html = hubEmoji.htmlCode.get(0);
                            emoji.character = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
                            
                            parsedEmojis.add(emoji);
                        }
                    }

                    // Save to cache
                    String jsonToCache = gson.toJson(parsedEmojis);
                    prefs.edit().putString(KEY_EMOJIS_JSON, jsonToCache).apply();
                    Log.d("EmojiRepository", "Saved emojis to cache");

                    callback.onSuccess(parsedEmojis);
                } else {
                    callback.onError("Failed to fetch emojis: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<EmojiHubResponse>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
