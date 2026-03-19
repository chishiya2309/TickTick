# Chọn Tài Khoản Google Khác - Chi Tiết Triển Khai

## Tóm Tắt
Đã thêm nút "Chọn tài khoản Google khác" vào LoginActivity, cho phép người dùng dễ dàng chuyển đổi giữa các tài khoản Google mà không cần đăng xuất toàn bộ.

## Vấn Đề Ban Đầu
- Người dùng không thể dễ dàng chọn tài khoản Google khác
- Phải logout rồi login lại nếu muốn đổi tài khoản
- Luồng không thân thiện với người dùng có nhiều tài khoản

## Giải Pháp Triển Khai

### 1. Sửa Layout (activity_login.xml)
**File:** `app/src/main/res/layout/activity_login.xml`

Thêm nút "Chọn tài khoản Google khác":
```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/btn_choose_account"
    style="@style/Widget.Material3.Button.OutlinedButton"
    android:layout_width="0dp"
    android:layout_height="56dp"
    android:layout_marginBottom="12dp"
    android:text="@string/choose_another_account"
    android:textAllCaps="false"
    app:cornerRadius="14dp"
    app:layout_constraintBottom_toTopOf="@id/btn_guest_mode"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:strokeColor="@color/main_divider"
    app:strokeWidth="1dp" />
```

**Thứ tự nút trên giao diện:**
1. **"Tiếp tục với Google"** (button xanh, mặc định)
   - Dùng authorized accounts nếu có
   - Fallback sang account chooser nếu không có

2. **"Chọn tài khoản Google khác"** (button outline, yêu cầu)
   - Luôn hiển thị tất cả tài khoản Google
   - Cho phép chọn bất kỳ tài khoản nào

3. **"Tiếp tục như Guest"** (button outline)
   - Sử dụng chế độ Guest (local data)

### 2. Thêm String Resource
**File:** `app/src/main/res/values/strings.xml`

```xml
<string name="choose_another_account">Chọn tài khoản Google khác</string>
```

### 3. Sửa LoginActivity.java
**File:** `app/src/main/java/.../LoginActivity.java`

Thêm click listener:
```java
findViewById(R.id.btn_choose_account).setOnClickListener(v -> chooseAnotherAccount());
```

Thêm phương thức `chooseAnotherAccount()`:
```java
private void chooseAnotherAccount() {
    requestGoogleCredential(false);
}
```

**Giải thích:**
- `requestGoogleCredential(true)` - chỉ hiển thị authorized accounts
- `requestGoogleCredential(false)` - hiển thị tất cả tài khoản Google

### 4. Cách Hoạt Động Chi Tiết

#### Khi nhấn "Tiếp tục với Google":
```
requestGoogleCredential(true)
    ↓
setFilterByAuthorizedAccounts(true)
    ↓
Nếu có authorized account → Hiển thị credential đó
    ↓
Nếu không → Fallback sang requestGoogleCredential(false)
    ↓
Hiển thị account chooser để chọn tài khoản
```

#### Khi nhấn "Chọn tài khoản Google khác":
```
chooseAnotherAccount()
    ↓
requestGoogleCredential(false)
    ↓
setFilterByAuthorizedAccounts(false)
    ↓
Luôn hiển thị account chooser
    ↓
Người dùng chọn tài khoản mong muốn
```

## Luồng Sử Dụng

### Trường Hợp 1: Người dùng mới / Chưa authorize tài khoản nào
```
1. Mở app → LoginActivity
2. Nhấn "Tiếp tục với Google"
3. Fallback sang account chooser (không có authorized account)
4. Hiển thị danh sách tài khoản Google
5. Chọn tài khoản → Đăng nhập thành công
```

### Trường Hợp 2: Người dùng đã authorize 1 tài khoản
```
1. Mở app → LoginActivity
2. Nhấn "Tiếp tục với Google"
3. Hiển thị tài khoản đã authorize (Hung Le)
4. Người dùng có thể:
   - Nhấn "Tiếp tục" → Dùng tài khoản hiện tại
   - Nhấn back → Quay lại
   - Nhấn "Chọn tài khoản Google khác" → Account chooser
```

### Trường Hợp 3: Người dùng muốn đổi tài khoản
```
1. Ở LoginActivity
2. Nhấn "Chọn tài khoản Google khác"
3. Hiển thị tất cả tài khoản Google (không filter)
4. Chọn tài khoản khác → Đăng nhập thành công
5. Session cũ bị replace bởi session mới
```

## Tính Năng CredentialManager
```java
setFilterByAuthorizedAccounts(boolean flag)
```

**true:** 
- Chỉ hiển thị tài khoản đã dùng trước đó
- Nhanh hơn (không cần hiển thị danh sách)
- Fallback sang account chooser nếu không có

**false:**
- Hiển thị tất cả tài khoản Google trên thiết bị
- Cho phép người dùng chọn bất kỳ tài khoản nào
- Account chooser được hiển thị

## So Sánh Cách Cũ vs Mới

### Cách Cũ (GoogleSignInClient - Deprecated)
```java
GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, options);
googleSignInClient.signOut(); // Logout
```
❌ Deprecated  
❌ Phức tạp khi multi-account  
❌ Không support account chooser tốt

### Cách Mới (CredentialManager - Recommended)
```java
CredentialManager credentialManager = CredentialManager.create(this);
credentialManager.getCredentialAsync(...);
setFilterByAuthorizedAccounts(true/false); // Linh hoạt
```
✅ Modern API  
✅ Hỗ trợ multi-account tốt  
✅ Account chooser tự động  
✅ Clear credential state dễ dàng

## Kiểm Thử

### Test Case 1: Chọn Tài Khoản Lần Đầu
1. Cài app mới
2. Mở LoginActivity
3. Nhấn "Tiếp tục với Google"
4. ✅ Hiển thị account chooser (nếu có tài khoản trên thiết bị)
5. ✅ Chọn tài khoản → Đăng nhập thành công

### Test Case 2: Quay Lại Tài Khoản Đã Authorize
1. App đã đăng nhập User A
2. Logout hoàn toàn
3. Mở lại app → LoginActivity
4. Nhấn "Tiếp tục với Google"
5. ✅ Hiển thị User A (không cần chọn lại)
6. ✅ Nhấn "Tiếp tục" → Đăng nhập nhanh

### Test Case 3: Đổi Sang Tài Khoản Khác
1. App đã đăng nhập User A
2. Logout
3. Ở LoginActivity
4. Nhấn "Chọn tài khoản Google khác"
5. ✅ Hiển thị danh sách tất cả tài khoản
6. ✅ Chọn User B → Đăng nhập thành công

### Test Case 4: Back từ Account Chooser
1. Ở LoginActivity
2. Nhấn "Chọn tài khoản Google khác"
3. Hiển thị account chooser
4. ✅ Nhấn back → Quay lại LoginActivity

## Ghi Chú

1. **CredentialManager là API mới**
   - Hỗ trợ từ API 34
   - Fallback tự động cho device cũ
   - Recommended bởi Google

2. **Account Chooser tự động**
   - CredentialManager tự quản lý account chooser
   - Không cần GoogleSignInClient
   - Trải nghiệm người dùng tốt hơn

3. **Clear Credential khi Logout**
   - Đã triển khai ở Step 6 (handleLogout)
   - Xóa stored credential trên device
   - Logout sạch và an toàn

4. **Multi-Account Support**
   - Người dùng có thể chuyển đổi giữa các tài khoản
   - Mỗi login là 1 session mới
   - Session cũ được replace

## Files Đã Sửa

1. ✅ `app/src/main/res/layout/activity_login.xml`
   - Thêm btn_choose_account

2. ✅ `app/src/main/res/values/strings.xml`
   - Thêm choose_another_account string

3. ✅ `app/src/main/java/.../LoginActivity.java`
   - Thêm chooseAnotherAccount() method
   - Thêm onclick listener

## Kết Quả
✅ Người dùng có thể chọn tài khoản Google khác dễ dàng  
✅ Hỗ trợ multi-account mượt mà  
✅ Sử dụng API modern (CredentialManager)  
✅ Trải nghiệm người dùng tốt hơn

