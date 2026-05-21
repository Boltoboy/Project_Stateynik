package com.example.tepertochno;

import android.os.Bundle;
import android.webkit.WebView;
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

        // Получаем данные, которые передал Адаптер
        String title = getIntent().getStringExtra("title");
        String author = getIntent().getStringExtra("author");
        String topic = getIntent().getStringExtra("topic");

        // Отображаем их
        tvTitle.setText(title);
        tvAuthor.setText("Автор(ы) - " + author);
        tvTopic.setText("Раздел - "+topic);


        WebView webView = findViewById(R.id.tvReaderContent);
        String htmlContent = getIntent().getStringExtra("content");

        if (htmlContent != null) {
            // Внедряем CSS-стиль, который заставляет линию зачеркивания центрироваться строго по высоте каждого символа
            String cssStyle = "<style>" +
                    "strike, s, span[style*='line-through'] {" +
                    "    text-decoration: line-through !important;" +
                    "    display: inline-block;" +
                    "    vertical-align: middle;" +
                    "    line-height: normal;" +
                    "}" +
                    "</style>";

            String fullHtml = cssStyle + htmlContent;

            webView.loadDataWithBaseURL(null, fullHtml, "text/html", "utf-8", null);
        }




    }
}