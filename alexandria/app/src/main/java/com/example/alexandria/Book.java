package com.example.alexandria;

import android.media.Image;


public class Book {

    private int isbn; // 10 or 13 digits
    private String title;
    private String author;
    private String description;
    private User owner;
    private Tuple<User, String> status; // <user, status>
    private Image image;

    public Book(int isbn, String title, String author, String description, User owner, Tuple<User, String> status, Image image) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.description = description;
        this.owner = owner;
        this.status = status;
        this.image = image;
    }

    public Book(int isbn, String title, String author, String description, User owner, Tuple<User, String> status) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.description = description;
        this.owner = owner;
        this.status = status;
    }

    // getters & setters


}
