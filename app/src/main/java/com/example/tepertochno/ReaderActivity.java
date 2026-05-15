package com.example.tepertochno;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ReaderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        TextView tvTitle = findViewById(R.id.tvReaderTitle);
        TextView tvAuthor = findViewById(R.id.tvReaderAuthor);
        TextView tvTopic = findViewById(R.id.tvReaderTopic);
        TextView tvContent = findViewById(R.id.tvReaderContent);

        // Получаем данные, которые передал Адаптер
        String title = getIntent().getStringExtra("title");
        String author = getIntent().getStringExtra("author");
        String topic = getIntent().getStringExtra("topic");

        // Отображаем их
        tvTitle.setText(title);
        tvAuthor.setText("Автор(ы) - " + author);
        tvTopic.setText("Раздел - "+topic);
        String contentText = getIntent().getStringExtra("content");

        if (contentText != null) {
            // 1. Очищаем текст от старых символов табуляции, чтобы они не ломали верстку
            String cleanText = contentText.replace("\t", "");

            // 2. Разделяем текст на отдельные абзацы по символу переноса строки
            String[] paragraphs = cleanText.split("\n");
            android.text.SpannableStringBuilder builder = new android.text.SpannableStringBuilder();

            for (int i = 0; i < paragraphs.length; i++) {
                String paragraph = paragraphs[i];

                // Пропускаем пустые строки между абзацами, если пользователь нажимал Enter дважды
                if (paragraph.trim().isEmpty()) {
                    builder.append("\n");
                    continue;
                }

                int start = builder.length();
                builder.append(paragraph);
                int end = builder.length();

                // 3. Создаем отступ: 40 пикселей для ПЕРВОЙ строки абзаца, 0 пикселей для всех остальных
                android.text.style.LeadingMarginSpan marginSpan =
                        new android.text.style.LeadingMarginSpan.Standard(40, 0);

                // 4. Применяем этот отступ СТРОГО к текущему абзацу
                builder.setSpan(marginSpan, start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Добавляем перенос строки для следующего абзаца
                if (i < paragraphs.length - 1) {
                    builder.append("\n");
                }
            }

            // 5. Выводим текст на экран
            tvContent.setText(builder);
        }

    }
}