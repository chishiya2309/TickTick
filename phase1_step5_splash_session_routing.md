# Phase 1 - Buoc 5: Route Splash theo session

## 1) Muc tieu
Cap nhat `SplashActivity` de dieu huong theo trang thai session thay vi luon vao `MainActivity`.

Mapping can dat:
- `USER` -> `MainActivity`
- `GUEST` -> `MainActivity`
- `NONE` -> `LoginActivity`

---

## 2) File can sua
- `app/src/main/java/hcmute/edu/vn/lequanghung_23110110/ticktick/activity/SplashActivity.java`

Khong can sua them `AndroidManifest.xml` neu da co:
- `SplashActivity` la `LAUNCHER`
- `LoginActivity` va `MainActivity` da khai bao

---

## 3) Cac buoc trien khai chi tiet

### Buoc 3.1 - Them import cho session
Trong `SplashActivity.java`, them:

```java
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.SessionManager;
```

### Buoc 3.2 - Tao ham route theo session
Them ham rieng de xu ly dieu huong sau splash:

```java
private void routeAfterSplash() {
    SessionManager sessionManager = new SessionManager(this);
    SessionManager.SessionType sessionType = sessionManager.getSessionType();

    Intent intent;
    switch (sessionType) {
        case USER:
        case GUEST:
            intent = new Intent(SplashActivity.this, MainActivity.class);
            break;
        case NONE:
        default:
            intent = new Intent(SplashActivity.this, LoginActivity.class);
            break;
    }

    // Xoa back stack de user khong quay lai Splash/Login sai luong
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
```

### Buoc 3.3 - Thay logic postDelayed hien tai
Trong `onCreate`, thay doan dang luon mo `MainActivity` bang goi `routeAfterSplash()`:

```java
new Handler(Looper.getMainLooper()).postDelayed(this::routeAfterSplash, SPLASH_DURATION);
```

---

## 4) Mau code day du cho `SplashActivity.java`

```java
package hcmute.edu.vn.lequanghung_23110110.ticktick.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        animateSplashElements();
        new Handler(Looper.getMainLooper()).postDelayed(this::routeAfterSplash, SPLASH_DURATION);
    }

    private void routeAfterSplash() {
        SessionManager sessionManager = new SessionManager(this);
        SessionManager.SessionType sessionType = sessionManager.getSessionType();

        Intent intent;
        switch (sessionType) {
            case USER:
            case GUEST:
                intent = new Intent(SplashActivity.this, MainActivity.class);
                break;
            case NONE:
            default:
                intent = new Intent(SplashActivity.this, LoginActivity.class);
                break;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void animateSplashElements() {
        View icon = findViewById(R.id.splash_icon);
        View appName = findViewById(R.id.splash_app_name);
        View tagline = findViewById(R.id.splash_tagline);

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(800);
        fadeIn.setFillAfter(true);

        AlphaAnimation fadeInDelayed = new AlphaAnimation(0f, 1f);
        fadeInDelayed.setDuration(800);
        fadeInDelayed.setStartOffset(300);
        fadeInDelayed.setFillAfter(true);

        AlphaAnimation fadeInMore = new AlphaAnimation(0f, 1f);
        fadeInMore.setDuration(800);
        fadeInMore.setStartOffset(600);
        fadeInMore.setFillAfter(true);

        icon.startAnimation(fadeIn);
        appName.startAnimation(fadeInDelayed);
        tagline.startAnimation(fadeInMore);
    }
}
```

---

## 5) Checklist test nhanh

1. Fresh install, chua login:
   - Mo app -> Splash -> vao `LoginActivity`.
2. Dang nhap Google thanh cong (`setUserSession`):
   - Dong/mo lai app -> Splash -> vao `MainActivity`.
3. Chon Guest (`setGuestSession`):
   - Dong/mo lai app -> Splash -> vao `MainActivity`.
4. Logout (`clearSession`):
   - Dong/mo lai app -> Splash -> vao `LoginActivity`.
5. Bam back sau khi vao `MainActivity`:
   - Khong quay lai `SplashActivity` (vi da clear task).

---

## 6) Loi thuong gap va cach xu ly

- App van vao thang `MainActivity`:
  - Kiem tra ban da thay doan `postDelayed` cu bang `this::routeAfterSplash` chua.
- Khong tim thay `SessionManager`:
  - Kiem tra import dung package `hcmute.edu.vn.lequanghung_23110110.ticktick.utils.SessionManager`.
- Back stack sai (back quay ve Splash/Login):
  - Dam bao da them `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`.
- Luong login dung nhung splash route sai:
  - Kiem tra `LoginActivity` co goi dung `setUserSession(...)` hoac `setGuestSession()` truoc khi vao `MainActivity`.

