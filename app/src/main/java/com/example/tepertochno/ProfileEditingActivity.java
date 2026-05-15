package com.example.tepertochno;

import static android.opengl.ETC1.encodeImage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileEditingActivity extends AppCompatActivity {

    Button btnLogout;

    String databaseUrl = "https://tepertochno-82a9f-default-rtdb.europe-west1.firebasedatabase.app/";
    DatabaseReference db = FirebaseDatabase.getInstance(databaseUrl).getReference("articles");
    ArticleAdapter adapter;

    List<Article> list = new ArrayList<>();
    String uid;
    EditText newName;

    ImageView ivUserAvatar;
    Button btnSave;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    String base64Image = encodeImage(imageUri); // Конвертируем в текст
                    if (base64Image != null) {
                        saveAvatarToDb(base64Image); // Сохраняем в базу
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_editing);
        ivUserAvatar = findViewById(R.id.ivProfileAvatar);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        btnSave = findViewById(R.id.btnSave);
        newName = findViewById(R.id.etProfileName);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = newName.getText().toString().trim();
                updateNameEverywhere(name);
                Intent intent = new Intent(ProfileEditingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

// Клик по аватарке — открыть галерею
        ivUserAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

// Загрузка аватарки при входе
        loadUserAvatar();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        String databaseUrl = "https://tepertochno-82a9f-default-rtdb.europe-west1.firebasedatabase.app/";
        DatabaseReference userRef = FirebaseDatabase.getInstance(databaseUrl).getReference("users").child(uid);


        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Достаем поле "name", которое мы сохраняли при регистрации
                    String userName = snapshot.child("name").getValue(String.class);
                    String userEmail = snapshot.child("email").getValue(String.class);

                    // Устанавливаем в TextView
                    TextView tvProfileemail = findViewById(R.id.tvProfileEmail);
                    tvProfileemail.setText(userEmail);
                    newName.setText(userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DB_ERROR", "Не удалось получить имя: " + error.getMessage());
            }
        });

    }

    // МЕТОД 1: Конвертация картинки в маленькую строку текста
    private String encodeImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            // Сжимаем, чтобы не перегружать бесплатную базу
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }
    // МЕТОД 2: Сохранение строки в Firebase
    private void saveAvatarToDb(String base64String) {
        FirebaseDatabase.getInstance(databaseUrl).getReference("users")
                .child(uid).child("avatarBase64").setValue(base64String)
                .addOnSuccessListener(aVoid -> displayBase64Avatar(base64String));
    }

    // МЕТОД 3: Загрузка строки из Firebase при старте
    private void loadUserAvatar() {
        FirebaseDatabase.getInstance(databaseUrl).getReference("users")
                .child(uid).child("avatarBase64").get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        displayBase64Avatar(snapshot.getValue(String.class));
                    }
                });
    }

    // МЕТОД 4: Отображение строки как круглой картинки через Glide
    private void displayBase64Avatar(String base64String) {
        byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
        Glide.with(this)
                .asBitmap()
                .load(imageBytes)
                .circleCrop()
                .into(ivUserAvatar);
    }

    private void updateUserName(String newName) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference db = FirebaseDatabase.getInstance(databaseUrl).getReference();

        // 1. Подготавливаем карту обновлений
        Map<String, Object> updates = new HashMap<>();

        // Меняем имя в профиле пользователя
        updates.put("users/" + uid + "/name", newName);

        // 2. Идем в общую папку статей (те, что на главном экране)
        db.child("Articles").get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot ds : snapshot.getChildren()) {
                // ВАЖНО: Мы проверяем authorId.
                // Если вы его не сохраняли, код ниже не поймет, где ваши статьи.
                String authorId = ds.child("authorId").getValue(String.class);

                if (uid.equals(authorId)) {
                    // Если статья принадлежит нам, добавляем путь на обновление имени автора
                    updates.put("Articles/" + ds.getKey() + "/author", newName);
                }
            }

            // 3. Также обновляем имя во вложенном списке в профиле
            db.child("users").child(uid).child("myArticles").get().addOnSuccessListener(mySnapshot -> {
                for (DataSnapshot dsMy : mySnapshot.getChildren()) {
                    updates.put("users/" + uid + "/myArticles/" + dsMy.getKey() + "/author", newName);
                }

                // 4. Выполняем ОДНО ОБНОВЛЕНИЕ для всех путей сразу
                db.updateChildren(updates).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Имя обновлено везде!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }).addOnFailureListener(e -> {
            Log.e("DB_ERROR", "Ошибка: " + e.getMessage());
        });
    }

    private void updateNameEverywhere(String newName) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference db = FirebaseDatabase.getInstance(databaseUrl).getReference();

        Map<String, Object> updates = new HashMap<>();

        // 1. Обновляем имя в профиле
        updates.put("users/" + uid + "/name", newName);

        // 2. Ищем статьи в общем узле "articles" (проверьте регистр в консоли!)
        db.child("articles").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Извлекаем authorId (именно так, как вы его назвали в конструкторе Article)
                    String authorIdInArticle = ds.child("authorId").getValue(String.class);

                    if (uid.equals(authorIdInArticle)) {
                        updates.put("articles/" + ds.getKey() + "/author", newName);
                    }
                }
            }

            // 3. Обновляем статьи в личном списке пользователя
            db.child("users").child(uid).child("myArticles").get().addOnSuccessListener(mySnapshot -> {
                if (mySnapshot.exists()) {
                    for (DataSnapshot dsMy : mySnapshot.getChildren()) {
                        updates.put("users/" + uid + "/myArticles/" + dsMy.getKey() + "/author", newName);
                    }
                }

                // 4. ПРИМЕНЯЕМ ОБНОВЛЕНИЯ
                db.updateChildren(updates).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Имя успешно обновлено!", Toast.LENGTH_SHORT).show();
                    finish(); // Возвращаемся в профиль
                }).addOnFailureListener(e -> {
                    Log.e("DEBUG_AUTH", "Ошибка: " + e.getMessage());
                });
            });
        });
    }



}