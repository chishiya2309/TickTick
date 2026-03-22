# PLAN-google-calendar-sync

## 1. Context Check

**Goal:** Synchronize TickTick tasks with Google Calendar using Android's `Calendar Provider` (ContentProvider).
**Scope:**

- Request `READ_CALENDAR` & `WRITE_CALENDAR` runtime permissions.
- Query local device calendars (associated with the user's Google account).
- Insert tasks with `due_date` as Calendar Events.
- Update/Delete events when tasks are modified/deleted in the app.
- (Optional) Read events from Calendar and import them as tasks.

## 2. Requirements & Constraints

- Must handle runtime permissions properly (Android 6.0+).
- Need to store mapping between `task_id` (TickTick) and `event_id` (Calendar) to allow updates/deletes.
- Sync should ideally happen in the background or immediately upon task creation/modification.

## 3. High-Level Architecture

- **CalendarSyncManager (Utility):** Handles querying calendars, inserting, updating, and deleting events using `ContentResolver`.
- **Database Update:** Add a column `calendar_event_id` (LONG) to the `tasks` table to track the linked event.
- **UI Integration:** Add a toggle in Settings ("Sync with Google Calendar") or a button in `TaskDetailBottomSheet`.

## 4. Implementation Steps (Phases)

### Phase 1: Preparation & Permissions

- [x] Update `AndroidManifest.xml` with `READ_CALENDAR` and `WRITE_CALENDAR`.
- [x] Update `TaskDatabaseHelper` schema: Add `calendar_event_id` to `tasks` table.

### Phase 2: Calendar Provider Logic

- [x] Create `CalendarHelper.java` (Singleton in `utils/`).
- [x] Implement `getPrimaryCalendarId()`: Find the default Google Calendar (with cache).
- [x] Implement `insertEvent(TaskModel task)`: 1h event ending at dueDateMillis.
- [x] Implement `updateEvent(long eventId, TaskModel task)`: Update existing event.
- [x] Implement `deleteEvent(long eventId)`: Delete event from calendar.

### Phase 3: Integration & UI

- [x] Task Creation → auto-sync to Calendar.
- [x] Task Deletion → delete linked Calendar event.
- [x] Task Update (date/title/desc) → update Calendar event.
- [x] Task Completion → Delete event (to declutter).
- [x] UI: Runtime permission request dialog (Just-In-Time).
- [x] **Option B**: Integrated Migration Sync (via `SyncWorker`).
- [x] **Settings UI**: Toggle sync in `SettingsBottomSheet`.

### Phase 4: Refinement (Optional)
- [ ] Support for multiple calendars selection.
- [ ] Syncing task priority to event color.

## 5. Verification Checklist

- [ ] Permissions request dialog shows up correctly.
- [ ] Task with due date appears in Google Calendar app.
- [ ] Changing task title/date updates the Google Calendar event.
- [ ] Deleting the task removes the event from Google Calendar.
