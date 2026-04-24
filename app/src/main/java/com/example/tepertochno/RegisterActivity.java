package com.example.tepertochno;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etName = findViewById(R.id.etRegName);
        EditText etEmail = findViewById(R.id.etRegEmail);
        EditText etPass = findViewById(R.id.etRegPass);

        findViewById(R.id.tvToLogin).setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
        });


        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPass.getText().toString();
            String name = etName.getText().toString();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Auth прошел успешно
                    String uid = mAuth.getCurrentUser().getUid();
                    User newUser = new User(uid, name, email);

                    String databaseUrl = "https://tepertochno-82a9f-default-rtdb.europe-west1.firebasedatabase.app/";
                    DatabaseReference db = FirebaseDatabase.getInstance(databaseUrl).getReference("users");

                    // Пытаемся записать в БД
                    db.child(uid).setValue(newUser)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("AUTH_DEBUG", "Данные пользователя сохранены в БД");
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                // Если ошибка тут — значит проблема в Rules (Правилах) или URL базы
                                Log.e("AUTH_DEBUG", "Ошибка записи в БД: " + e.getMessage());
                                Toast.makeText(this, "Ошибка базы: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

                } else {
                // Это покажет точную причину ошибки от сервера
                Log.e("AUTH_DEBUG", "Ошибка Auth", task.getException());
                Toast.makeText(this, "Ошибка: " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }

        });

        });
}}