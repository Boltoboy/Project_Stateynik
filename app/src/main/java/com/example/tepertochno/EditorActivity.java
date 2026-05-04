package com.example.tepertochno;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        EditText etTitle = findViewById(R.id.etTitle);
        EditText etTopic = findViewById(R.id.etTopic);
        EditText etContent = findViewById(R.id.etContent);
        Button btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> {

            // 2. Лог для проверки в Logcat
            Log.d("MY_APP", "Начинаю сохранение...");
            String title = etTitle.getText().toString().trim();
            String topic = etTopic.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            if (title.isEmpty()) {
                etTitle.setError("Введите заголовок");
                return;
            }
            if (topic.isEmpty()) {
                etTopic.setError("Введите раздел");
                return;
            }
            if (content.isEmpty()){
                etContent.setError("Статья не может быть пустой");
            }

            String databaseUrl = "https://tepertochno-82a9f-default-rtdb.europe-west1.firebasedatabase.app/";
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance(databaseUrl).getReference("users").child(uid);

            userRef.get().addOnSuccessListener(snapshot -> {
                User user = snapshot.getValue(User.class);
                String authorName = (user != null) ? user.name : "Аноним";

                DatabaseReference dbArticles = FirebaseDatabase.getInstance(databaseUrl).getReference("articles");
                String articleId = dbArticles.push().getKey();

                Article article = new Article(articleId, title, authorName, content,topic);
                dbArticles.child(articleId).setValue(article).addOnCompleteListener(task -> finish());




                DatabaseReference db = FirebaseDatabase.getInstance(databaseUrl).getReference("articles");


                if (articleId != null) {
                    db.child(articleId).setValue(article)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("MY_APP", "Успех в Firebase!");
                                finish(); // Закрыть экран после сохранения
                            })
                            .addOnFailureListener(e -> {
                                Log.e("MY_APP", "Ошибка Firebase: " + e.getMessage());
                                Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                }
                // Путь: users -> UID -> myArticles -> ID статьи
                DatabaseReference userArticlesRef = FirebaseDatabase.getInstance(databaseUrl)
                        .getReference("users")
                        .child(uid)
                        .child("myArticles");
                Article newArticle = new Article(articleId, title, authorName, content, topic);

                if (articleId != null) {
                    userArticlesRef.child(articleId).setValue(newArticle)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Статья сохранена в ваш профиль", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                }
            });




        });
    }


}