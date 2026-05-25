package com.example.tepertochno;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Таймер задержки на 2 секунды (2000 миллисекунд)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // Переходим на экран входа (или MainActivity, если пользователь уже авторизован)
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);

            // Обязательно закрываем SplashActivity, чтобы пользователь не вернулся на него по кнопке "Назад"
            finish();

        }, 2000);
    }
}
