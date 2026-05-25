package com.example.tepertochno;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String databaseUrl = "https://tepertochno-82a9f-default-rtdb.europe-west1.firebasedatabase.app/";
    private final DatabaseReference dbArticles = FirebaseDatabase.getInstance(databaseUrl).getReference("articles");
    private final DatabaseReference dbUsers = FirebaseDatabase.getInstance(databaseUrl).getReference("users");

    private ArticleAdapter adapter;
    private TextView tvWelcome;
    private ImageView ivUserAvatar, btnLogout;
    private final List<Article> articleList = new ArrayList<>();
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Блок кода отвечает за проверку авторизации; если пользователя нет, перенаправляет на экран логина
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ivUserAvatar.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        RecyclerView rv = findViewById(R.id.recyclerView);
        adapter = new ArticleAdapter(articleList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        loadUserAvatar();
        loadUserProfileName();
        loadAllArticlesFromFirebase();


        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }


    private void loadUserAvatar() {
        dbUsers.child(uid).child("avatarBase64").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                displayBase64Avatar(snapshot.getValue(String.class));
            }
        });
    }

    /**
     * Метод отвечает за декодирование строки Base64 и отображение круглой аватарки в шапке через библиотеку Glide.
     */
    private void displayBase64Avatar(String base64String) {
        byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
        Glide.with(this)
                .asBitmap()
                .load(imageBytes)
                .circleCrop()
                .into(ivUserAvatar);
    }

    /**
     * Метод отвечает за однократное чтение имени аккаунта из базы данных и установку приветственной надписи.
     */
    private void loadUserProfileName() {
        dbUsers.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("name").getValue(String.class);
                    tvWelcome.setText("Привет, " + userName + "!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DB_ERROR", "Не удалось получить имя: " + error.getMessage());
            }
        });
    }

    /**
     * Метод отвечает за постоянное отслеживание и вывод общего списка всех созданных статей в главное меню.
     */
    private void loadAllArticlesFromFirebase() {
        dbArticles.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                articleList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    articleList.add(ds.getValue(Article.class));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DB_ERROR", "Ошибка обновления списка статей: " + error.getMessage());
            }
        });
    }
}
