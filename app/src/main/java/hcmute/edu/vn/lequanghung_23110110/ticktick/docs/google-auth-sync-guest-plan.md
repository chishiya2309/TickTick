# Google Auth + Cloud Sync + Guest Mode Plan (TickTick)

## 1. Mục tiêu và quyết định chính

Tài liệu này triển khai theo 4 yêu cầu đã chốt:

1. **Đồng bộ cloud ngay khi có Wi-Fi**
2. **Cho phép đăng nhập tài khoản khác trên cùng thiết bị nhưng phải logout trước**
3. **Chế độ khách (Guest) dùng dữ liệu local**
4. **Bổ sung Guest mode trong luồng vào app**

## Quyết định kỹ thuật
- Dùng **Google Sign-In + Firebase Auth** cho user có tài khoản.
- Dùng **SQLite local** làm source cho trải nghiệm offline.
- Dùng **Cloud sync theo hàng đợi (sync queue)**, trigger ngay khi Wi-Fi available.
- Dùng **owner scope** để tách dữ liệu theo `guest` hoặc `firebase_uid`.

---

## 2. Hiện trạng codebase liên quan

- App Android Java, local DB qua `TaskDatabaseHelper`.
    - `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/database/TaskDatabaseHelper.java`
- Launcher hiện tại là `SplashActivity` và đang chuyển thẳng `MainActivity`.
    - `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/SplashActivity.java`
    - `app/src/main/AndroidManifest.xml`
- Chưa có dependency auth Google/Firebase trong build hiện tại.
    - `app/build.gradle.kts`
    - `gradle/libs.versions.toml`

---

## 3. Nghiệp vụ chi tiết

## 3.1 Chế độ đăng nhập
- Màn hình vào app phải cho 2 lựa chọn:
    - `Continue with Google`
    - `Continue as Guest`
- Nếu user đã có session:
    - Session `USER` -> vào `MainActivity`
    - Session `GUEST` -> vào `MainActivity` với profile guest

## 3.2 Multi-account trên cùng thiết bị
- Chỉ cho phép **1 session active** tại một thời điểm.
- Muốn đổi tài khoản bắt buộc:
    1) Logout tài khoản hiện tại
    2) Đăng nhập tài khoản Google khác
- Dữ liệu local phải tách theo owner để không lẫn giữa account A/B/Guest.

## 3.3 Guest mode
- Guest mode **không sync cloud**.
- Toàn bộ thao tác đọc/ghi vẫn qua local DB.
- Có thể có flow tùy chọn sau (khuyến nghị):  
  `Guest -> Login` => hỏi người dùng có merge data guest vào account không.

## 3.4 Cloud sync khi có Wi-Fi
- Khi app đang chạy và phát hiện Wi-Fi khả dụng:
    - Trigger sync ngay (foreground trigger).
- Khi app background/đóng:
    - Dùng WorkManager job với constraint `UNMETERED` để sync khi có Wi-Fi.
- Sync gồm 2 chiều:
    - Pull remote changes
    - Push local pending changes

---

## 4. Thiết kế kiến trúc

## 4.1 Session model
`SessionType = USER | GUEST | NONE`

Thông tin tối thiểu trong session store:
- `session_type`
- `firebase_uid` (nếu USER)
- `display_name`, `email`, `avatar_url` (nếu USER)
- `guest_id` (nếu GUEST, ví dụ UUID generate 1 lần)

## 4.2 Data ownership model
Thêm trường owner vào dữ liệu local:

- `owner_type` (`guest` | `user`)
- `owner_id` (`guest_id` hoặc `firebase_uid`)
- `updated_at`
- `sync_state` (`PENDING_CREATE`, `PENDING_UPDATE`, `PENDING_DELETE`, `SYNCED`)
- `deleted_at` (soft delete để sync xóa)

Áp dụng cho bảng:
- `lists`
- `tasks`

Mọi query hiện tại phải thêm filter owner scope:
- Guest chỉ thấy dữ liệu guest
- User A chỉ thấy dữ liệu User A
- User B chỉ thấy dữ liệu User B

## 4.3 Sync components
- `SyncManager`: điều phối sync và chống chạy trùng.
- `SyncRepository`: map local <-> cloud DTO.
- `ConnectivityObserver`: lắng nghe trạng thái mạng/Wi-Fi.
- `SyncWorker` (WorkManager): chạy theo constraint Wi-Fi.

## 4.4 Conflict policy (đề xuất)
- Mặc định: **Last-write-wins** dựa trên `updated_at`.
- Nếu bản local và cloud cùng sửa, giữ bản mới hơn.
- Log conflict để có thể nâng cấp policy sau.

---

## 5. Thay đổi file dự kiến

## File mới
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/LoginActivity.java`
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/auth/AuthManager.java`
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/auth/SessionManager.java`
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/sync/SyncManager.java`
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/sync/SyncWorker.java`
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/sync/ConnectivityObserver.java`
- `app/src/main/res/layout/activity_login.xml`

## File chỉnh sửa
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/SplashActivity.java`
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/MainActivity.java`
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/database/TaskDatabaseHelper.java`
- `app/src/main/AndroidManifest.xml`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `app/src/main/res/values/strings.xml`

---

## 6. Kế hoạch triển khai theo phase

## Phase 0 - Config nền tảng auth/cloud
- Tạo Firebase project và add Android app.
- Thêm `google-services.json`.
- Cấu hình SHA-1/SHA-256 debug/release.

## Phase 1 - Auth + Session + Guest mode
- Tạo `LoginActivity` với 2 nút:
    - Continue with Google
    - Continue as Guest
- Implement `SessionManager` cho `USER/GUEST/NONE`.
- Sửa `SplashActivity` để route theo session.
- Sửa `MainActivity` thêm logout action.

## Phase 2 - Data ownership trong SQLite
- Tăng `DB_VERSION`.
- Migration thêm cột owner và sync metadata.
- Refactor tất cả CRUD/query hiện có để luôn filter owner.
- Đảm bảo guest và user tách dữ liệu tuyệt đối.

## Phase 3 - Cloud sync qua Wi-Fi
- Tạo `SyncManager` + `SyncWorker`.
- Trigger sync ngay khi có Wi-Fi trong app foreground.
- Register periodic/one-time work với `UNMETERED`.
- Đồng bộ 2 chiều local <-> cloud theo `sync_state`.

## Phase 4 - Multi-account + đổi tài khoản
- Luồng logout sạch:
    - Firebase signOut
    - clear session active
    - quay về `LoginActivity` và clear back stack
- Cho phép login tài khoản khác sau logout.
- Không hiển thị dữ liệu account cũ khi account mới đăng nhập.

## Phase 5 - Guest to User migration (khuyến nghị)
- Khi guest chọn login Google:
    - Hỏi: merge dữ liệu guest vào account?
    - Nếu Yes: chuyển owner + mark pending sync
    - Nếu No: giữ nguyên namespace guest

---

## 7. Checklist kiểm thử

## 7.1 Auth/Session
- [ ] Chưa login -> vào `LoginActivity`
- [ ] Login Google thành công -> vào `MainActivity`
- [ ] Chọn Guest -> vào `MainActivity`
- [ ] Reopen app giữ đúng session USER/GUEST

## 7.2 Multi-account
- [ ] Account A login -> dữ liệu A
- [ ] Logout -> login account B -> không thấy dữ liệu A
- [ ] Logout B -> login lại A -> thấy lại dữ liệu A local scope

## 7.3 Guest mode
- [ ] Guest tạo/sửa/xóa task hoạt động local bình thường
- [ ] Guest không trigger push cloud
- [ ] Guest -> User merge hoạt động đúng (nếu bật)

## 7.4 Wi-Fi sync
- [ ] Khi có Wi-Fi, sync được trigger ngay
- [ ] Khi chỉ có mobile data, job Wi-Fi-only không chạy
- [ ] Dữ liệu pending được đẩy cloud khi Wi-Fi quay lại
- [ ] Pull cloud không làm mất dữ liệu local mới hơn

---

## 8. Rủi ro và phương án giảm thiểu

- **Rủi ro migration DB**: mất dữ liệu cũ nếu migration sai  
  -> Viết migration rõ ràng + backup test data trước khi nâng version.
- **Rủi ro conflict sync**: ghi đè dữ liệu ngoài ý muốn  
  -> Áp dụng `updated_at` thống nhất + logging conflict.
- **Rủi ro leak dữ liệu giữa account**  
  -> Bắt buộc owner filter ở mọi query repository.
- **Rủi ro UX phức tạp khi guest chuyển sang user**  
  -> Thêm confirm dialog merge/keep-separate.

---

## 9. Definition of Done

- [ ] Có `LoginActivity` với Google + Guest mode
- [ ] Session routing chuẩn trong `SplashActivity`
- [ ] Logout và đổi account chạy đúng
- [ ] Dữ liệu tách owner hoàn chỉnh trong local DB
- [ ] Sync Wi-Fi trigger ngay khi available + background worker hoạt động
- [ ] Không crash ở các case cancel login/network loss/conflict cơ bản
- [ ] Regression pass cho các chức năng task/list hiện tại

---

## 10. Ước lượng

- Phase 0-1: 1 ngày
- Phase 2: 1-1.5 ngày
- Phase 3: 1-1.5 ngày
- Phase 4-5 + test: 1 ngày

**Tổng**: ~4-5 ngày làm việc.
