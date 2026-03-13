# Overview
Extend the `TaskDetailBottomSheet` to accept a search keyword and highlight it within the task's title and description. Connect the search results in `SearchActivity` to open the `TaskDetailBottomSheet` upon clicking a task, passing the current query for highlighting.

# Project Type
MOBILE (Android/Java)

# Success Criteria
- Clicking a task in the search results opens the `TaskDetailBottomSheet`.
- The task's title and description inside the bottom sheet have the search keyword highlighted in orange (`#f59e0b`).
- The bottom sheet functions normally (saving title/description changes on dismiss).
- After closing the bottom sheet, the search results update if changes were made.

# Tech Stack
- Android SDK (Java)
- `SpannableString` / `ForegroundColorSpan`
- `BottomSheetDialogFragment`

# File Structure
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/dialog/TaskDetailBottomSheet.java` [MODIFY]
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/SearchActivity.java` [MODIFY]

# Task Breakdown
- [ ] **Modify `TaskDetailBottomSheet`**
  - **Dependencies**: None
  - **INPUT→OUTPUT→VERIFY**: Input: `TaskDetailBottomSheet` -> Output: Add `setHighlightKeyword(String)` method. Add `getHighlightedText` logic in `onViewCreated` to apply orange foreground color span to `editTitle` and `editDescription`. -> Verify: Text input correctly displays spans.
- [ ] **Connect `SearchActivity`**
  - **Dependencies**: `TaskDetailBottomSheet` changes
  - **INPUT→OUTPUT→VERIFY**: Input: `SearchActivity` -> Output: Implement `OnItemClickListener` of `SearchTaskAdapter`. Open `TaskDetailBottomSheet` and call `setHighlightKeyword` with current `searchInput` text. -> Verify: Clicking a search result opens the bottom sheet with correct keyword passed.
- [ ] **Verify End-to-End Flow**
  - **Dependencies**: Step 1 & 2
  - **INPUT→OUTPUT→VERIFY**: AssembleDebug, open search, search for a task, click it, verify highlight appears in bottom sheet.

## ✅ PHASE X COMPLETE
[Pending verification tests]
