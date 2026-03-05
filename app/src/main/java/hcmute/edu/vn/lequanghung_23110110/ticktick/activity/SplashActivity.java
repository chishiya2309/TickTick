package hcmute.edu.vn.lequanghung_23110110.ticktick.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Fade-in animation cho icon và text
        animateSplashElements();

        // Chuyển sang MainActivity sau 2 giây
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Đóng SplashActivity để user không back lại được
        }, SPLASH_DURATION);
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
