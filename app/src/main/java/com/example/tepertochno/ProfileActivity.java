package com.example.tepertochno;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class ProfileActivity extends AppCompatActivity {

    private ImageView btnLogout;
    private ImageView btnRedaction;
    private ImageView btnToMain;
    private ImageView ivUserAvatar;
    private Button add;

    private final String databaseUrl = "https://tepertochno-82a9f-default-rtdb.europe-west1.firebasedatabase.app/";
    private final List<Article> list = new ArrayList<>();
    private ArticleAdapter adapter;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        btnLogout = findViewById(R.id.btnLogout);
        btnRedaction = findViewById(R.id.btnRedaction);
        btnToMain = findViewById(R.id.toMain);
        add = findViewById(R.id.fabAdd);
        ivUserAvatar = findViewById(R.id.ivProfileAvatar);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnToMain.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnRedaction.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ProfileEditingActivity.class);
            startActivity(intent);
            finish();
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        add.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditorActivity.class);
            startActivity(intent);
        });

        loadUserAvatar();

        DatabaseReference userRef = FirebaseDatabase.getInstance(databaseUrl).getReference("users").child(uid);
        DatabaseReference userArticlesRef = FirebaseDatabase.getInstance(databaseUrl)
                .getReference("users")
                .child(uid)
                .child("myArticles");

        RecyclerView rv = findViewById(R.id.recyclerView2);
        adapter = new ArticleAdapter(list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        userArticlesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Article article = postSnapshot.getValue(Article.class);
                    if (article != null) {
                        list.add(article);
                    }
                }
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
                    String userName = snapshot.child("name").getValue(String.class);
                    String userEmail = snapshot.child("email").getValue(String.class);

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


    //Метод загружает из базы данных текстовый формат аватара
    private void loadUserAvatar() {
        FirebaseDatabase.getInstance(databaseUrl).getReference("users")
                .child(uid).child("avatarBase64").get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        displayBase64Avatar(snapshot.getValue(String.class));
                    }
                });
    }

    //Метод переводит строчное представление аватара в изображение
    private void displayBase64Avatar(String base64String) {
        byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
        Glide.with(this)
                .asBitmap()
                .load(imageBytes)
                .circleCrop()
                .into(ivUserAvatar);
    }
}
