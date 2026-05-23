package org.example.model;

public class NewsArticle {

    private String id;
    private String title;
    private String text;
    private String author;
    private String publishDate;
    private String url;

    public NewsArticle() {
    }

    public NewsArticle(String id,
                       String title,
                       String text,
                       String author,
                       String publishDate,
                       String url) {

        this.id = id;
        this.title = title;
        this.text = text;
        this.author = author;
        this.publishDate = publishDate;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public String getUrl() {
        return url;
    }
}