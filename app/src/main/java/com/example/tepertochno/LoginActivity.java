package com.example.tepertochno;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //Метод инициализирует элементы интерфейса, настраивает переходы на экран регистрации и выполняет авторизацию в Firebase
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etEmail = findViewById(R.id.etLoginEmail);
        EditText etPass = findViewById(R.id.etLoginPass);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvToRegister = findViewById(R.id.tvToRegister);

        tvToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPass.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Ошибка: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
