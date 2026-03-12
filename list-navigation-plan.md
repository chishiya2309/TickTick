# Overview
Handle clicks on list items within the Search results by navigating the user back to `MainActivity` and opening the selected list's tasks. This matches the behavior of clicking a list in the Navigation Drawer.

# Success Criteria
- Clicking a list in the search results closes `SearchActivity`.
- Evaluates back to `MainActivity` and displays tasks for the selected list.
- The UI in `MainActivity` correctly displays the list title, icon/emoji, and updates the drawer's selected state.

# Tech Stack
- Android SDK (Java)
- Intents (`FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP`)

# File Structure
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/SearchActivity.java` [MODIFY]
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/MainActivity.java` [MODIFY]

# Task Breakdown
- [ ] **Modify `SearchActivity`**
  - **INPUT→OUTPUT→VERIFY**: Input: `SearchActivity.java` -> Output: Add click handler to `searchListAdapter` that creates an Intent, puts `listId`, `iconResId`, `emojiStr` as extras, sets flags, starts `MainActivity`, and finishes. -> Verify: Clicking a list closes search.
- [ ] **Modify `MainActivity`**
  - **INPUT→OUTPUT→VERIFY**: Input: `MainActivity.java` -> Output: Add `handleIntent(Intent)` method, call it in `onCreate` and `onNewIntent`. Extract the extras and call `loadTasksForList`. Also create a method to find and set the selected position in `drawerAdapter`. -> Verify: MainActivity switches to the correct list and drawer updates.
- [ ] **Verify Flow**
  - **INPUT→OUTPUT→VERIFY**: Run `assembleDebug`, manually search for a custom list or default list, click it, verify MainActivity loads the correct list with the right icon/emoji.
