package com.example.tepertochno;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    private List<Article> articles;
    public ArticleAdapter(List<Article> articles) {
        this.articles = articles;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.title.setText(article.title);
        holder.topic.setText(article.topic);
        holder.snippet.setText("Автор - "+ article.author);

        // Клик по статье открывает экран чтения/редактирования
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ReaderActivity.class);
            // Передаем данные выбранной статьи
            intent.putExtra("title", article.title);
            intent.putExtra("author", article.author);
            intent.putExtra("content", article.content);
            intent.putExtra("topic",article.topic);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return articles.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, snippet, topic;
        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            topic = itemView.findViewById(R.id.tvTopic);
            snippet = itemView.findViewById(R.id.tvSnippet);
        }
    }
}
