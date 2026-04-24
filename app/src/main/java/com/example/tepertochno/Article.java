package com.example.tepertochno;

public class Article {
    public String id, title, author, content,topic;
    public Article() {} // Обязательно для Firebase
    public Article(String id, String title, String author, String content, String topic) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.content = content;
        this.topic = topic;
    }
}