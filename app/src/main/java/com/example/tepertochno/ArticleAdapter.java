package com.example.tepertochno;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    // Переменная хранит список статей для отображения
    private final List<Article> articles;

    // Конструктор принимает исходный список данных из активности
    public ArticleAdapter(List<Article> articles) {
        this.articles = articles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Метод отвечает за создание новой графической ячейки на основе разметки item_article
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Метод отвечает за связывание текстовых данных конкретной статьи с элементами UI ячейки
        Article article = articles.get(position);

        holder.title.setText(article.title);
        holder.topic.setText(article.topic);
        holder.snippet.setText("Автор - " + article.author);

        // Блок кода отвечает за обработку клика по карточке статьи и передачу всех параметров на экран чтения
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ReaderActivity.class);

            intent.putExtra("title", article.title);
            intent.putExtra("author", article.author);
            intent.putExtra("content", article.content);
            intent.putExtra("topic", article.topic);

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        // Метод отвечает за возвращение общего количества элементов в списке для отрисовки
        return articles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Класс отвечает за кэширование и быстрый доступ к графическим элементам внутри одной ячейки
        TextView title, snippet, topic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            topic = itemView.findViewById(R.id.tvTopic);
            snippet = itemView.findViewById(R.id.tvSnippet);
        }
    }
}
