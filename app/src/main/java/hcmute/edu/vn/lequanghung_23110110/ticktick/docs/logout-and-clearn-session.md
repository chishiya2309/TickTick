# Bước 7-9: Session, Guest mode, và trigger sync theo Wi-Fi

## 10) Bước 7 - Quy tắc đa tài khoản trên cùng thiết bị

Luồng chuẩn để đổi account:
1. User A đang dùng app.
2. User chọn `Đăng xuất`.
3. App clear session local + sign out Firebase/Credential state.
4. Điều hướng về `LoginActivity`.
5. User chọn **Continue with Google** và đăng nhập User B.
6. Lưu `SessionType.USER` + profile B.

Nguyên tắc:
- Không cho switch account trực tiếp khi chưa logout để tránh conflict dữ liệu và token session.

## 11) Bước 8 - Guest mode dùng local data

Nguyên tắc cho `GUEST`:
- Dữ liệu task đọc/ghi local SQLite như hiện tại (`TaskDatabaseHelper`).
- Không upload cloud.
- Không tải cloud.
- Logout từ Guest phải về `NONE` (màn `LoginActivity`).

Guard cho mọi luồng sync:

```java
if (sessionType != SessionManager.SessionType.USER) return;
```

## 12) Bước 9 - Trigger sync cloud ngay khi có Wi-Fi

Trong `LoginActivity`, sau khi login USER thành công:
1. Check mạng hiện tại có phải Wi-Fi.
2. Nếu đúng, gọi `SyncManager.syncNow(...)` ngay.
3. Nếu không phải Wi-Fi, bỏ qua (job nền xử lý ở phase cloud sync).

Pseudo code check Wi-Fi:

```java
public static boolean isOnWifi(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (cm == null) return false;
    Network active = cm.getActiveNetwork();
    if (active == null) return false;
    NetworkCapabilities caps = cm.getNetworkCapabilities(active);
    return caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
}
```

## 13) Chuỗi điều hướng chuẩn sau triển khai

App mở -> `SplashActivity`

`SplashActivity` đọc session:
- `NONE` -> `LoginActivity`
- `GUEST`/`USER` -> `MainActivity`

Tại `LoginActivity`:
- Google -> `USER` -> `MainActivity`
- Guest -> `GUEST` -> `MainActivity`

Tại `MainActivity`:
- Logout -> clear session -> `LoginActivity`

## Mapping code đã áp dụng

- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/utils/SyncManager.java`
  - Chặn sync khi session không phải `USER`.
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/utils/NetworkUtils.java`
  - Cung cấp `isOnWifi(...)`.
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/LoginActivity.java`
  - Trigger `SyncManager.syncNow(...)` ngay sau login USER thành công nếu đang Wi-Fi.
- `app/src/main/AndroidManifest.xml`
  - Thêm quyền `android.permission.ACCESS_NETWORK_STATE`.
