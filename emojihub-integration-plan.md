# Overview
Integrate EmojiHub API (`https://emojihub.yurace.pro/api/all`) to provide a dynamic and extensive list of emojis in the `SelectIconBottomSheet` instead of the static local JSON file. 
To ensure optimal UX and performance, the emojis will be fetched once from the network, parsed, and then cached locally using `SharedPreferences`. Subsequent opens of the bottom sheet will load instantly from the cache. A loading spinner will be displayed during the initial network fetch.

# Expected UX
1. First time opening the icon selector: User sees a loading spinner while the app fetches emojis from the API, converts HTML codes to characters, and caches them.
2. Subsequent opens: The emoji list loads instantaneously from local storage.
3. The emojis are fully searchable by name/category.

# Tech Stack / Dependencies
- Android SDK (Java)
- Retrofit 2 & Gson Converter (needs to be added to Gradle)
- `SharedPreferences` for local caching
- Background threading for network calls (Retrofit Async)

# File Structure
- `gradle/libs.versions.toml` [MODIFY]
- `app/build.gradle.kts` [MODIFY]
- `app/src/main/AndroidManifest.xml` [MODIFY] (Add INTERNET permission if missing)
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/api/EmojiHubApi.java` [NEW]
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/model/EmojiHubResponse.java` [NEW]
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/repository/EmojiRepository.java` [NEW]
- `app/src/main/res/layout/layout_bottom_sheet_select_icon.xml` [MODIFY] (Add ProgressBar)
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/dialog/SelectIconBottomSheet.java` [MODIFY]

# Task Breakdown
- [ ] **Setup Dependencies**
  - Update `libs.versions.toml` with `retrofit` and `converter-gson` versions.
  - Apply them in `app/build.gradle.kts` and sync.
  - Check `AndroidManifest.xml` for `<uses-permission android:name="android.permission.INTERNET" />`.
- [ ] **Create Network Models & API Interface**
  - Create `EmojiHubResponse` matching the API structure (name, category, group, htmlCode array).
  - Create `EmojiHubApi` interface with `@GET("all") Call<List<EmojiHubResponse>> getAllEmojis()`.
- [ ] **Create `EmojiRepository` for Caching**
  - Implement a Singleton pattern.
  - Provide a method `getEmojis(Context, Callback)` that checks `SharedPreferences` first.
  - If empty, call Retrofit, convert `htmlCode` (e.g., `&#128512;`) to String using `Html.fromHtml`, map to `EmojiResponse`, save to `SharedPreferences` using Gson, and return the list.
- [ ] **Update UI and BottomSheet**
  - Add a `ProgressBar` (centered) and hide the `RecyclerView` initially in `layout_bottom_sheet_select_icon.xml`.
  - In `SelectIconBottomSheet`, call `EmojiRepository.getEmojis` to get the list asynchronously. On completion, hide the progress bar, show the RecyclerView, and populate the adapter.
  - Ensure the search logic matches the `name` field from the API.

# Verification Plan
- **Automated/Build Tests:** Run `./gradlew build` to ensure no syntax errors after updating dependencies.
- **Manual User Testing:** 
  1. Open the "Select Icon" bottom sheet while connected to the internet.
  2. Verify the loading spinner appears briefly, followed by the emojis.
  3. Close and reopen the bottom sheet to verify that it loads instantly without the spinner (caching works).
  4. Disconnect from the internet, kill the app, and open the bottom sheet again to verify it still loads from the cache.
  5. Search for an emoji name (e.g., "smile" or "cat") and verify the filter works correctly.
