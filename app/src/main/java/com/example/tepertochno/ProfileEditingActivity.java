package com.example.tepertochno;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileEditingActivity extends AppCompatActivity {

    private final String databaseUrl = "https://tepertochno-82a9f-default-rtdb.europe-west1.firebasedatabase.app/";
    private String uid;
    private EditText newName;
    private ImageView ivUserAvatar;
    private Button btnSave;

    //Поле инициализирует запуск системной галереи для выбора нового изображения профиля
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    String base64Image = encodeImage(imageUri);
                    if (base64Image != null) {
                        saveAvatarToDb(base64Image);
                    }
                }
            });

    //Метод инициализирует элементы интерфейса, обрабатывает нажатия кнопок и загружает данные аккаунта
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_editing);

        ivUserAvatar = findViewById(R.id.ivProfileAvatar);
        btnSave = findViewById(R.id.btnSave);
        newName = findViewById(R.id.etProfileName);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnSave.setOnClickListener(v -> {
            String name = newName.getText().toString().trim();
            if (name.isEmpty()) {
                newName.setError("Имя не может быть пустым");
                return;
            }
            updateNameEverywhere(name);
            Intent intent = new Intent(ProfileEditingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        ivUserAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        loadUserAvatar();

        DatabaseReference userRef = FirebaseDatabase.getInstance(databaseUrl).getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("name").getValue(String.class);
                    String userEmail = snapshot.child("email").getValue(String.class);

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

    //Метод кодирует выбранное изображение в качестве строки для Base 64
    private String encodeImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    //Метод сохраняет полученную текстовую строку изображения пользователя в базу данных Firebase
    private void saveAvatarToDb(String base64String) {
        FirebaseDatabase.getInstance(databaseUrl).getReference("users")
                .child(uid).child("avatarBase64").setValue(base64String)
                .addOnSuccessListener(aVoid -> displayBase64Avatar(base64String));
    }

    //Метод загружает текстовую строку аватара из базы данных текущего пользователя при старте
    private void loadUserAvatar() {
        FirebaseDatabase.getInstance(databaseUrl).getReference("users")
                .child(uid).child("avatarBase64").get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        displayBase64Avatar(snapshot.getValue(String.class));
                    }
                });
    }

    //Метод отображает декодированную из Base 64 картинку в круглом виде на экране
    private void displayBase64Avatar(String base64String) {
        byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
        Glide.with(this)
                .asBitmap()
                .load(imageBytes)
                .circleCrop()
                .into(ivUserAvatar);
    }

    //Метод обновляет имя пользователя одновременно в профиле и во всех созданных им статьях
    private void updateNameEverywhere(String newName) {
        DatabaseReference db = FirebaseDatabase.getInstance(databaseUrl).getReference();
        Map<String, Object> updates = new HashMap<>();

        updates.put("users/" + uid + "/name", newName);

        db.child("articles").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String authorIdInArticle = ds.child("authorId").getValue(String.class);
                    if (uid.equals(authorIdInArticle)) {
                        updates.put("articles/" + ds.getKey() + "/author", newName);
                    }
                }
            }

            db.child("users").child(uid).child("myArticles").get().addOnSuccessListener(mySnapshot -> {
                if (mySnapshot.exists()) {
                    for (DataSnapshot dsMy : mySnapshot.getChildren()) {
                        updates.put("users/" + uid + "/myArticles/" + dsMy.getKey() + "/author", newName);
                    }
                }

                db.updateChildren(updates).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Имя успешно обновлено!", Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> {
                    Log.e("DEBUG_AUTH", "Ошибка атомарного обновления: " + e.getMessage());
                });
            });
        });
    }
}
