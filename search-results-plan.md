# Overview
Implement the Search Results UI in `SearchActivity` to match the provided screenshots. When the user types a keyword, the UI will query the `TaskDatabaseHelper` for matching Tasks and Lists, and display a grouped interface with highlighted matching text.

# Project Type
MOBILE (Android/Java)

# Success Criteria
- The Search input bar includes an "x" button to clear the text.
- When text is entered (e.g., "t"), the Empty State is hidden and the Results Container is shown.
- The Results contain two distinct rounded cards/sections: "Nhiệm vụ" (Tasks) and "Danh sách" (Lists).
- The App queries the SQLite database for real tasks matching the keyword in title/description, and real lists matching the keyword in their name.
- "Nhiệm vụ" section displays up to 4 tasks, highlighting the search keyword (e.g., "t" highlighted in orange), and a "Xem thêm" (See more) button.
- "Danh sách" section displays matched Custom and Default lists with their respective icons/emojis, also demonstrating text highlighting.

# Tech Stack
- Android SDK (Java)
- SQLite (`TaskDatabaseHelper`)
- XML Layouts
- `SpannableString` for text highlighting.

# File Structure
- `app/src/main/res/layout/activity_search.xml` [MODIFY]
- `app/src/main/res/layout/item_search_task.xml` [NEW]
- `app/src/main/res/layout/item_search_list.xml` [NEW]
- `app/src/main/res/drawable/bg_search_result_card.xml` [NEW]
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/database/TaskDatabaseHelper.java` [MODIFY]
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/SearchActivity.java` [MODIFY]
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/adapter/SearchTaskAdapter.java` [NEW]
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/adapter/SearchListAdapter.java` [NEW]

# Task Breakdown
- [ ] **Database Setup**
  - **Dependencies**: None
  - **INPUT→OUTPUT→VERIFY**: Input: `TaskDatabaseHelper` -> Output: Add `searchLists(query)` method -> Verify: Returns lists matching the name.
- [ ] **Create Card Background & Item Layouts**
  - **Dependencies**: None
  - **INPUT→OUTPUT→VERIFY**: Input: screenshots -> Output: `bg_search_result_card.xml`, `item_search_task.xml`, `item_search_list.xml` -> Verify: Layouts render correctly in preview.
- [ ] **Update `activity_search.xml` Structure**
  - **Dependencies**: Item layouts
  - **INPUT→OUTPUT→VERIFY**: Input: current layout -> Output: Added clear button (`x`), NestedScrollView with two grouped cards -> Verify: Minimum 48dp touch targets and correct structure.
- [ ] **Create Adapters with Highlighting Logic**
  - **Dependencies**: Item layouts
  - **INPUT→OUTPUT→VERIFY**: Input: Item layouts -> Output: `SearchTaskAdapter.java` and `SearchListAdapter.java` with `SpannableString` logic -> Verify: Text highlighting works based on a search query.
- [ ] **Implement UI Logic in `SearchActivity.java`**
  - **Dependencies**: Adapters, XML layouts, Database
  - **INPUT→OUTPUT→VERIFY**: Input: `SearchActivity.java` -> Output: TextWatcher queries DB, populates adapters with real data, handles clear button. -> Verify: Typing shows real matched results with highlighted text.

## ✅ PHASE X COMPLETE
[Pending verification tests]
