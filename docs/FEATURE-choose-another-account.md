# Chọn Tài Khoản Google Khác - Hướng Dẫn Nhanh

## ✅ Hoàn Thành

Đã triển khai tính năng cho phép người dùng chọn tài khoản Google khác khi đăng nhập.

## 📱 Giao Diện LoginActivity

```
┌─────────────────────────────┐
│                             │
│        TickTick Logo        │
│                             │
│   Chào mừng đến TickTick    │
│                             │
│  Đăng nhập để đồng bộ...    │
│                             │
├─────────────────────────────┤
│ Tiếp tục với Google    [→]  │  ← Button xanh (mặc định)
├─────────────────────────────┤
│ Chọn tài khoản Google khác  │  ← Button outline (MỚI)
├─────────────────────────────┤
│ Tiếp tục như Guest         │
└─────────────────────────────┘
```

## Thay Đổi Chi Tiết

### 1️⃣ Layout - `activity_login.xml`
- ✅ Thêm nút `btn_choose_account`
- ✅ Sắp xếp constraint: `btn_google_sign_in` → `btn_choose_account` → `btn_guest_mode`

### 2️⃣ String Resource - `strings.xml`
- ✅ Thêm: `<string name="choose_another_account">Chọn tài khoản Google khác</string>`

### 3️⃣ Logic - `LoginActivity.java`
- ✅ Thêm: `findViewById(R.id.btn_choose_account).setOnClickListener(v -> chooseAnotherAccount());`
- ✅ Thêm phương thức: `chooseAnotherAccount()` gọi `requestGoogleCredential(false)`

## Cách Hoạt Động

### "Tiếp tục với Google"
```java
signInWithGoogle()
  ↓
requestGoogleCredential(true)
  ↓
setFilterByAuthorizedAccounts(true)
  ↓
- Nếu có tài khoản đã authorize → hiển thị nó
- Nếu không → fallback sang account chooser (tất cả tài khoản)
```

### "Chọn tài khoản Google khác" (MỚI)
```java
chooseAnotherAccount()
  ↓
requestGoogleCredential(false)
  ↓
setFilterByAuthorizedAccounts(false)
  ↓
Luôn hiển thị account chooser
  ↓
Chọn bất kỳ tài khoản Google nào
```

## Trường Hợp Sử Dụng

### ✅ Người dùng mới
1. Mở app → LoginActivity
2. Nhấn "Tiếp tục với Google"
3. Chọn tài khoản → Đăng nhập

### ✅ Người dùng có nhiều tài khoản Google
1. Ở LoginActivity
2. Nhấn "Chọn tài khoản Google khác"
3. Chọn tài khoản mong muốn
4. Đăng nhập thành công

### ✅ Người dùng muốn quay lại tài khoản cũ
1. Logout hoàn toàn (đã triển khai ở Step 6)
2. Ở LoginActivity
3. Nhấn "Tiếp tục với Google"
4. Hiển thị tài khoản cũ tự động → Tiếp tục

## 🔑 Điểm Khác Biệt

| Tính Năng | "Tiếp tục với Google" | "Chọn tài khoản khác" |
|-----------|---------------------|-----------------------|
| Filter | Authorized accounts | Tất cả tài khoản |
| Trải nghiệm | Nhanh (mặc định) | Linh hoạt (chọn) |
| Multi-account | Fallback | Ưu tiên |
| Use case | User thường xuyên | User đổi account |

## 🚀 Kiểm Thử

1. **Đăng nhập lần đầu**
   - Mở app
   - Nhấn "Tiếp tục với Google"
   - ✅ Hiển thị account chooser (nếu có tài khoản)
   - ✅ Chọn tài khoản → Đăng nhập

2. **Đổi Tài Khoản**
   - Ở LoginActivity
   - Nhấn "Chọn tài khoản Google khác"
   - ✅ Hiển thị danh sách tất cả tài khoản
   - ✅ Chọn tài khoản khác → Đăng nhập

3. **Quay Lại Tài Khoản Cũ**
   - Logout (Step 6)
   - Ở LoginActivity
   - Nhấn "Tiếp tục với Google"
   - ✅ Hiển thị tài khoản cũ tự động
   - ✅ Nhấn "Tiếp tục" → Đăng nhập nhanh

## 📚 Files Đã Sửa

| File | Thay Đổi |
|------|---------|
| `activity_login.xml` | Thêm btn_choose_account |
| `strings.xml` | Thêm choose_another_account |
| `LoginActivity.java` | Thêm chooseAnotherAccount() + listener |

## 🎯 Lợi Ích

✅ Multi-account support mượt mà  
✅ UX/UI rõ ràng - có nút riêng để đổi account  
✅ Sử dụng API modern (CredentialManager)  
✅ Fallback tự động cho device cũ  
✅ Kết hợp với Step 6 (Logout) để quản lý tài khoản toàn diện

## 📝 Ghi Chú

- **CredentialManager** là API Google khuyến khích từ API 34+
- **setFilterByAuthorizedAccounts(boolean)**:
  - `true`: Chỉ authorized accounts (nhanh)
  - `false`: Tất cả tài khoản (linh hoạt)
- **Fallback tự động**: Nếu không có authorized account, tự chuyển sang false
- **Tương thích**: Hoạt động trên Android 6.0+ (có fallback)

