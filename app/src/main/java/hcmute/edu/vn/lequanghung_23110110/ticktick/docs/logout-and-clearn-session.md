10) Bước 7 - Quy tắc đa tài khoản trên cùng thiết bị
    Luồng chuẩn để đổi account:
    User A đang dùng app.
    User chọn Đăng xuất.
    App clear session local + sign out Firebase/Google.
    Về LoginActivity.
    User chọn Continue with Google và đăng nhập User B.
    Lưu SessionType.USER + profile B.
    Không cho switch account trực tiếp khi chưa logout để tránh conflict dữ liệu và session token.

11) Bước 8 - Guest mode dùng local data
    Nguyên tắc cho GUEST:
    Dữ liệu tác vụ đọc/ghi local SQLite như hiện tại (TaskDatabaseHelper).
    Không upload cloud.
    Không tải cloud.
    Logout từ Guest -> về NONE (màn Login).
    Khuyến nghị:
    Trong các luồng sync sau này, check session:
    if (sessionType != USER) return;

12) Bước 9 - Trigger sync cloud ngay khi có Wi-Fi
    Trong LoginActivity sau khi login USER thành công:
    check mạng đang là Wi-Fi
    nếu đúng -> gọi sync ngay (SyncManager.syncNow(...))
    nếu không phải Wi-Fi -> bỏ qua, để job nền xử lý sau
    Pseudo check Wi-Fi:
    public static boolean isOnWifi(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (cm == null) return false;
    Network active = cm.getActiveNetwork();
    if (active == null) return false;
    NetworkCapabilities caps = cm.getNetworkCapabilities(active);
    return caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }
    Ở Phase 1 có thể đặt hook gọi hàm sync; phần engine sync chi tiết triển khai ở Phase Cloud Sync kế tiếp.

13) Chuỗi điều hướng chuẩn sau triển khai
    App mở -> SplashActivity
    SplashActivity đọc session:
    NONE -> LoginActivity
    GUEST/USER -> MainActivity
    Tại LoginActivity:
    Google -> USER -> MainActivity
    Guest -> GUEST -> MainActivity
    Tại MainActivity:
    Logout -> clear session -> LoginActivity