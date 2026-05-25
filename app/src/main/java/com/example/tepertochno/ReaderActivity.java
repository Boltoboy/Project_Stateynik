package com.example.tepertochno;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ReaderActivity extends AppCompatActivity {

    //Метод инициализирует элементы интерфейса, принимает текстовые и HTML данные выбранной статьи и отображает их на экране
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        TextView tvTitle = findViewById(R.id.tvReaderTitle);
        TextView tvAuthor = findViewById(R.id.tvReaderAuthor);
        TextView tvTopic = findViewById(R.id.tvReaderTopic);

        String title = getIntent().getStringExtra("title");
        String author = getIntent().getStringExtra("author");
        String topic = getIntent().getStringExtra("topic");

        tvTitle.setText(title);
        tvAuthor.setText("Автор(ы) - " + author);
        tvTopic.setText("Раздел - " + topic);

        WebView webView = findViewById(R.id.tvReaderContent);
        String htmlContent = getIntent().getStringExtra("content");

        if (htmlContent != null) {
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
