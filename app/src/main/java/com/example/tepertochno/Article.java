package com.example.tepertochno;

public class Article {
    public String id, title, author, content,topic, authorId;
    public Article() {} // Обязательно для Firebase
    public Article(String id, String title, String author, String content, String topic, String authorId) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.content = content;
        this.topic = topic;
        this.authorId = authorId;
    }
}