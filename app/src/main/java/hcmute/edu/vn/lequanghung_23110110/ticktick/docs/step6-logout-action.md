# Bước 6 - Thêm Logout action ở MainActivity

## Tóm tắt
Chức năng Logout đã được triển khai hoàn chỉnh, cho phép người dùng đăng xuất khỏi ứng dụng một cách an toàn.

## Chi tiết triển khai

### 1. Sửa Menu (toolbar_menu.xml)
**File:** `app/src/main/res/menu/toolbar_menu.xml`

Thêm menu item logout:
```xml
<item
    android:id="@+id/action_logout"
    android:title="Đăng xuất"
    app:showAsAction="never" />
```

**Giải thích:**
- `android:id="@+id/action_logout"`: ID duy nhất để xác định item
- `android:title="Đăng xuất"`: Tên hiển thị trên menu
- `app:showAsAction="never"`: Item sẽ không hiển thị trên toolbar, chỉ trong dropdown menu "..."

### 2. Thêm Imports ở MainActivity.java
Thêm các import cần thiết:
```java
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.SessionManager;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.CredentialManagerCallback;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.NonNull;
```

### 3. Sửa onOptionsItemSelected ở MainActivity.java
Thêm xử lý cho action_logout:
```java
@Override public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_smart_suggest) Toast.makeText(this, "Gợi ý thông minh", Toast.LENGTH_SHORT).show();
    else if (id == R.id.action_more) Toast.makeText(this, "Thêm tùy chọn", Toast.LENGTH_SHORT).show();
    else if (id == R.id.action_logout) {
        handleLogout();
        return true;
    }
    // ... rest of the code
}
```

### 4. Thêm Phương thức handleLogout()
```java
private void handleLogout() {
    // Hiển thị dialog xác nhận
    new MaterialAlertDialogBuilder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất?")
            .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
            .setPositiveButton("Đăng xuất", (dialog, which) -> {
                performLogout();
            })
            .show();
}
```

**Giải thích:**
- Hiển thị dialog xác nhận để tránh logout vô tình
- Người dùng có thể hủy bỏ hoặc xác nhận

### 5. Thêm Phương thức performLogout()
```java
private void performLogout() {
    try {
        // 1. Sign out Firebase
        FirebaseAuth.getInstance().signOut();

        // 2. Clear credential state từ CredentialManager
        CredentialManager credentialManager = CredentialManager.create(this);
        credentialManager.clearCredentialStateAsync(
                new ClearCredentialStateRequest(),
                null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override
                    public void onResult(Void result) {
                        Log.d(TAG, "Credential state cleared successfully");
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        Log.w(TAG, "Failed to clear credential state", e);
                    }
                }
        );

        // 3. Clear session từ SessionManager
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.clearSession();

        // 4. Điều hướng về LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // 5. Finish current activity stack
        finishAffinity();

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Log.e(TAG, "Error during logout", e);
        Toast.makeText(this, "Lỗi khi đăng xuất", Toast.LENGTH_SHORT).show();
    }
}
```

**Giải thích từng bước:**

1. **SignOut Firebase** - Xóa session Firebase
   ```java
   FirebaseAuth.getInstance().signOut();
   ```

2. **Clear Credential State** - Xóa credential khỏi CredentialManager
   - Đó là API mới của Google cho phép clear stored credentials
   - Cách cũ dùng GoogleSignInClient.signOut() đã deprecated

3. **Clear Session** - Xóa session local
   ```java
   sessionManager.clearSession();
   // Kết quả: SessionType.NONE
   ```

4. **Điều hướng về LoginActivity**
   ```java
   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
   ```
   - `FLAG_ACTIVITY_NEW_TASK`: Tạo task mới
   - `FLAG_ACTIVITY_CLEAR_TASK`: Xóa hết activity stack cũ

5. **Finish Activity Stack**
   ```java
   finishAffinity();
   ```
   - Đảm bảo người dùng không thể back về MainActivity

## Luồng Logout Chuẩn

1. Người dùng nhấn "Đăng xuất" trong menu
2. Hiển thị dialog xác nhận
3. Nếu xác nhận:
   - ✅ Sign out Firebase
   - ✅ Clear credential state
   - ✅ Clear local session
   - ✅ Quay về LoginActivity
   - ✅ Xóa activity stack
4. Người dùng có thể đăng nhập tài khoản khác

## Quy tắc Đa Tài Khoản
- **User A đang dùng app** → chọn Đăng xuất
- **Logout hoàn toàn** → clear session local + Firebase + credential
- **Quay về LoginActivity** → sẵn sàng cho login User B
- **Login User B** → lưu session mới (uid, email, name)
- **Không được switch trực tiếp** → phải logout trước để tránh conflict dữ liệu

## Kiểm Thử
1. Chạy app
2. Đăng nhập Google (hoặc Guest)
3. Mở menu (3 chấm) ⋮
4. Chọn "Đăng xuất"
5. Xác nhận trong dialog
6. ✅ Quay về LoginActivity
7. ✅ Có thể đăng nhập tài khoản khác

## Ghi Chú
- Sử dụng **CredentialManager** API mới (thay vì deprecated GoogleSignInClient)
- Logout được xử lý **bất đồng bộ** cho credential clear
- Có **dialog xác nhận** để tránh logout vô tình
- **finishAffinity()** đảm bảo không back về MainActivity

