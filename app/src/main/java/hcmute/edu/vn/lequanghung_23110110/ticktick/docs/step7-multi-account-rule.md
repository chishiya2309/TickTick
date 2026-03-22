# Bước 7 - Quy tắc đa tài khoản trên cùng thiết bị

## Mục tiêu
- Không cho đổi tài khoản trực tiếp khi phiên hiện tại (USER/GUEST) chưa logout.
- Luồng bắt buộc khi đổi account: `Logout -> LoginActivity -> Continue with Google -> Account mới`.
- Tránh conflict dữ liệu local, token đăng nhập, và session cloud.

## Luồng chuẩn đổi account
1. User A đang sử dụng ứng dụng (session `USER`).
2. User chọn **Đăng xuất** ở `MainActivity`.
3. Ứng dụng thực hiện:
   - `FirebaseAuth.getInstance().signOut()`
   - `CredentialManager.clearCredentialStateAsync(...)`
   - `SessionManager.clearSession()`
4. Điều hướng về `LoginActivity` với cờ clear task.
5. User chọn **Continue with Google** và chọn User B.
6. Firebase xác thực thành công, lưu:
   - `SessionType.USER`
   - `uid`, `email`, `displayName`, `avatarUrl` của User B
7. Vào lại `MainActivity` với profile mới.

## Nguyên tắc bắt buộc
- Không cung cấp nút switch account trực tiếp trong `MainActivity`.
- Nếu `LoginActivity` được mở khi session khác `NONE`, phải điều hướng về `MainActivity` ngay.
- Mọi thay đổi account phải đi qua thao tác logout rõ ràng.

## Mapping implementation hiện tại
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/MainActivity.java`
  - `handleLogout()` + `performLogout()` xử lý sign out + clear session + về Login.
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/LoginActivity.java`
  - `onCreate()` kiểm tra `SessionManager`; nếu session != `NONE` thì redirect về `MainActivity`.
  - Nút Google chỉ dành cho trạng thái chưa đăng nhập.
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/utils/SessionManager.java`
  - Quản lý `USER/GUEST/NONE` và thông tin hồ sơ user.

## Test checklist
- [ ] USER A login thành công, hiển thị đúng avatar/name trong drawer.
- [ ] Logout xong không thể back về `MainActivity`.
- [ ] Login lại bằng USER B thành công, drawer đổi đúng profile B.
- [ ] Khi còn session USER/GUEST, mở `LoginActivity` sẽ bị chuyển về `MainActivity`.
- [ ] Guest mode logout về `NONE`, sau đó có thể login Google bình thường.

