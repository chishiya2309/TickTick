# Phase 0 - Cấu hình nền tảng Auth/Cloud (Firebase + Google Sign-In)

## Mục tiêu

Thiết lập xong nền tảng để các phase sau có thể triển khai đăng nhập Google + đồng bộ cloud:
- Tạo Firebase project
- Add Android app vào Firebase
- Khai báo SHA-1/SHA-256 cho debug và release
- Thêm `google-services.json` vào app Android

---

## Thông tin project TickTick cần dùng

Từ codebase hiện tại:

- `applicationId`: `hcmute.edu.vn.lequanghung_23110110.ticktick`
- `namespace`: `hcmute.edu.vn.lequanghung_23110110.ticktick`
- `minSdk`: `24`
- `targetSdk`: `36`

Nguồn:
- `app/build.gradle.kts`

> Lưu ý: khi add app vào Firebase, **package name phải khớp chính xác** với `applicationId`.

---

## Checklist đầu vào trước khi bắt đầu

- [ ] Có tài khoản Google để truy cập Firebase Console
- [ ] Có quyền truy cập project Android local
- [ ] Máy Windows có Java (để chạy `keytool`)
- [ ] Có/đã chuẩn bị release keystore (nếu muốn cấu hình release ngay)

---

## Bước 1 - Tạo Firebase Project

1. Truy cập Firebase Console:
    - `https://console.firebase.google.com/`
2. Chọn **Create a project**
3. Đặt tên project (ví dụ: `TickTick-Prod`)
4. Bật/tắt Google Analytics theo nhu cầu team
5. Chờ project được tạo xong

---

## Bước 2 - Add Android App vào Firebase

1. Trong Firebase project, chọn **Add app** -> chọn icon Android.
2. Nhập:
    - **Android package name**: `hcmute.edu.vn.lequanghung_23110110.ticktick`
    - **App nickname**: `TickTick Android` (tuỳ chọn)
    - **Debug signing certificate SHA-1**: có thể để trống tạm, sẽ thêm sau
3. Bấm **Register app**

> Nếu lỡ nhập sai package name, nên xóa app trong Firebase và tạo lại app mới đúng package.

---

## Bước 3 - Lấy SHA-1/SHA-256 (Debug)

Có 2 cách, ưu tiên cách A.

## Cách A (khuyến nghị): dùng Gradle signingReport

Mở PowerShell tại root project `D:\Workspace\MOP_Project\TickTick`:

```powershell
cd D:\Workspace\MOP_Project\TickTick
.\gradlew.bat signingReport
