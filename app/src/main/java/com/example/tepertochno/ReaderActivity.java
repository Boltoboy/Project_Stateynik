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
        String content = getIntent().getStringExtra("content");

        // Отображаем их
        tvTitle.setText(title);
        tvAuthor.setText("Автор(ы) - " + author);
        tvTopic.setText(topic);
        tvContent.setText(content);
    }
}