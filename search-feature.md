# Overview
Implement a Search UI in the TickTick app, accessible via the `drawer_btn_search` button in the Drawer menu. The UI will feature a dark-themed search screen with a search bar and an empty state illustration.

# Project Type
MOBILE (Android/Java)

# Success Criteria
- Tapping `drawer_btn_search` opens `SearchActivity`.
- The new `SearchActivity` accurately reflects the provided design (dark theme, search input, back button, "Tìm kiếm" title, empty state illustration).

# Tech Stack
- Android SDK (Java)
- XML Layouts
- Material Components

# File Structure
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/SearchActivity.java` [NEW]
- `app/src/main/res/layout/activity_search.xml` [NEW]
- `app/src/main/res/drawable/ic_search_empty_state.xml` [NEW]
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/MainActivity.java` [MODIFY]

# Task Breakdown
- [ ] **Analyze existing drawer implementation**
  - **Dependencies**: None
  - **INPUT→OUTPUT→VERIFY**: Input: codebase -> Output: identified click handler -> Verify: exact line found.
- [ ] **Create Search UI Layout (`activity_search.xml`)**
  - **Dependencies**: None
  - **INPUT→OUTPUT→VERIFY**: Input: image -> Output: `activity_search.xml` -> Verify: Layout renders with minimum 48dp touch targets.
- [ ] **Implement `SearchActivity.java`**
  - **Dependencies**: `activity_search.xml`
  - **INPUT→OUTPUT→VERIFY**: Input: Layout -> Output: `SearchActivity.java` -> Verify: Edge-to-Edge handled, back button works.
- [ ] **Link Drawer to SearchActivity**
  - **Dependencies**: `SearchActivity.java`
  - **INPUT→OUTPUT→VERIFY**: Input: `MainActivity.java` -> Output: Click listener -> Verify: Tapping button opens activity.

## ✅ PHASE X COMPLETE
[Pending verification tests]
