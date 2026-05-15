package com.example.tepertochno;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

// Символ табуляции или фиксированное количество пробелов для красной строки
        final String indent = "\t";

        etContent.addTextChangedListener(new TextWatcher() {
            private boolean isIterating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isIterating) return;

                // Если пользователь нажал Enter (добавился перенос строки)
                if (count == 1 && s.charAt(start) == '\n') {
                    isIterating = true;

                    // Вставляем отступ сразу после символа переноса строки
                    etContent.getText().insert(start + 1, indent);

                    isIterating = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Проверяем самый первый абзац текста: если он пустой или не начинается с отступа
                if (!isIterating && s.length() > 0 && !s.toString().startsWith(indent)) {
                    isIterating = true;
                    s.insert(0, indent); // Добавляем отступ в самое начало текста
                    isIterating = false;
                }
            }
        });


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

                // БЕРЕМ UID НАПРЯМУЮ, это исключит ошибку с null/пустым id
                String authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference dbArticles = FirebaseDatabase.getInstance(databaseUrl).getReference("articles");
                String articleId = dbArticles.push().getKey();

                if (articleId != null) {
                    Article article = new Article(articleId, title, authorName, content, topic, authorId);

                    // 1. Сохраняем в общий список
                    dbArticles.child(articleId).setValue(article);

                    // 2. Сохраняем в личный список пользователя
                    DatabaseReference userArticlesRef = FirebaseDatabase.getInstance(databaseUrl)
                            .getReference("users")
                            .child(authorId)
                            .child("myArticles");

                    userArticlesRef.child(articleId).setValue(article)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Статья опубликована!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                }
            });





        });
    }


}