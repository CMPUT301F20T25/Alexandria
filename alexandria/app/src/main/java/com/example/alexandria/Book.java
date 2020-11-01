//package com.example.alexandria;
//
//import android.media.Image;
//
//import java.util.Dictionary;
//
//
//public class Book {
//
//    private int isbn; // 10 or 13 digits
//    private String title;
//    private String author;
//    private String description;
//    private User owner;
//    private String status; // owner's side status
//    private Dictionary requestStatus; //<user, status> borrower's side status
//    private Image image;
//
//    // constructor
//    public Book(int isbn, String title, String author, String description, User owner, String status, Image image) {
//        this.isbn = isbn;
//        this.title = title;
//        this.author = author;
//        this.description = description;
//        this.owner = owner;
//        this.status = status;
//        this.image = image;
//    }
//
//    // getters & setters
//
//    public int getIsbn() {
//        return isbn;
//    }
//
//    public void setIsbn(int isbn) {
//        this.isbn = isbn;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getAuthor() {
//        return author;
//    }
//
//    public void setAuthor(String author) {
//        this.author = author;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public User getOwner() {
//        return owner;
//    }
//
//    public void setOwner(User owner) {
//        this.owner = owner;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public Image getImage() {
//        return image;
//    }
//
//    public void setImage(Image image) {
//        this.image = image;
//    }
//
//
//    public String getRequestStatus(User borrower) {
//        return requestStatus.get(borrower);
//    }
//
//    public void setRequestStatus(User borrower, String status) {
//        // add <user, status> to dictionary when a request is made
//        requestStatus.put(borrower,status);
//    }
//}
