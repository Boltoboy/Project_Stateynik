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
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    Button btnLogout, btnRedaction;

    String databaseUrl = "https://tepertochno-82a9f-default-rtdb.europe-west1.firebasedatabase.app/";
    DatabaseReference db = FirebaseDatabase.getInstance(databaseUrl).getReference("articles");
    ArticleAdapter adapter;

    List<Article> list = new ArrayList<>();
    String uid;

    ImageView ivUserAvatar;

    // Лаунчер для выбора картинки из галереи
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
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnLogout = findViewById(R.id.btnLogout);
        btnRedaction = findViewById(R.id.btnRedaction);

        btnRedaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileEditingActivity.class);
                // Очищаем стек активностей, чтобы нельзя было вернуться назад кнопкой "Back"
                startActivity(intent);
                finish();
            }
        });

        btnLogout.setOnClickListener(v -> {
            // 1. Выходим из системы в Firebase Auth
            FirebaseAuth.getInstance().signOut();

            // 2. Переходим на экран регистрации
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            // Очищаем стек активностей, чтобы нельзя было вернуться назад кнопкой "Back"
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        ivUserAvatar = findViewById(R.id.ivProfileAvatar);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        loadUserAvatar();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        String databaseUrl = "https://tepertochno-82a9f-default-rtdb.europe-west1.firebasedatabase.app/";
        DatabaseReference userRef = FirebaseDatabase.getInstance(databaseUrl).getReference("users").child(uid);
        DatabaseReference userArticlesRef = FirebaseDatabase.getInstance(databaseUrl)
                .getReference("users")
                .child(uid)
                .child("myArticles");
        RecyclerView rv = findViewById(R.id.recyclerView2);
        adapter = new ArticleAdapter(list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 3. Слушаем изменения в этом узле
        userArticlesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear(); // Очищаем старый список перед обновлением

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Article article = postSnapshot.getValue(Article.class);
                    if (article != null) {
                        list.add(article);
                    }
                }

                // 4. Инициализируем адаптер и привязываем к RecyclerView
                // Используем ваш ArticleAdapter(List<Article> articles)

                // Если адаптер уже был установлен ранее, можно просто вызвать:
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Ошибка загрузки: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });




        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Достаем поле "name", которое мы сохраняли при регистрации
                    String userName = snapshot.child("name").getValue(String.class);
                    String userEmail = snapshot.child("email").getValue(String.class);

                    // Устанавливаем в TextView
                    TextView tvWelcome = findViewById(R.id.tvProfileName);
                    TextView tvProfileemail = findViewById(R.id.tvProfileEmail);
                    tvProfileemail.setText(userEmail);
                    tvWelcome.setText(userName);
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


}