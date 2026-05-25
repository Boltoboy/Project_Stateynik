package com.example.tepertochno;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //Метод инициализирует элементы интерфейса, обрабатывает переходы между окнами и регистрирует аккаунт в Firebase
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etName = findViewById(R.id.etRegName);
        EditText etEmail = findViewById(R.id.etRegEmail);
        EditText etPass = findViewById(R.id.etRegPass);

        findViewById(R.id.tvToLogin).setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });

        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPass.getText().toString().trim();
            String name = etName.getText().toString().trim();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = mAuth.getCurrentUser().getUid();
                    User newUser = new User(uid, name, email);

                    String databaseUrl = "https://tepertochno-82a9f-default-rtdb.europe-west1.firebasedatabase.app/";
                    DatabaseReference db = FirebaseDatabase.getInstance(databaseUrl).getReference("users");

                    db.child(uid).setValue(newUser)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("AUTH_DEBUG", "Данные пользователя сохранены в БД");
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("AUTH_DEBUG", "Ошибка записи в БД: " + e.getMessage());
                                Toast.makeText(this, "Ошибка базы: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                } else {
                    Log.e("AUTH_DEBUG", "Ошибка Auth", task.getException());
                    Toast.makeText(this, "Ошибка: " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
