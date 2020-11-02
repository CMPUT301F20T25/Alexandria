package com.example.alexandria;

import android.media.Image;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class Book {

    private String isbn; // 10 or 13 digits
    private String title;
    private String author;
    private String description;

    //private User owner;
    private String owner;

    private String status; // owner's side status - Available/Borrowed
    private Map requestStatus; //<user, status> borrower's side status
    private Image image;

    // constructor
//    public Book(String isbn, String title, String author, String description, User owner, String status, Image image) {
//        this.isbn = isbn;
//        this.title = title;
//        this.author = author;
//        this.description = description;
//        this.owner = owner;
//        this.status = status;
//        this.image = image;
//        this.requestStatus = new HashMap<User, String>();
//    }

    public Book(String isbn, String title, String author, String description, String owner, String status) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.description = description;
        this.owner = owner;
        this.status = status;
        this.requestStatus = new HashMap<String, String>();
    }


    // getters & setters

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public User getOwner() {
//        return owner;
//    }
//
//    public void setOwner(User owner) {
//        this.owner = owner;
//    }


    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }





    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }


//    public String getRequestStatus(User borrower) {
//        return requestStatus.get(borrower);
//    }
//
//    public void setRequestStatus(User borrower, String status) {
//        // add <user, status> to dictionary when a request is made
//        requestStatus.put(borrower,status);
//    }
//
//    public User getBorrower(){
//
//        User borrower = null;
//
//        // hashmap iterator
//        // reference: https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
//        Iterator it = requestStatus.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Entry) it.next();
//            if (pair.getValue().toString().equals("Borrowed")){
//                borrower = pair.getKey().getClass(User);
//            }
//        }
//        return borrower;
//    }



    public void setRequestStatus(String borrower, String status) {
        // add <user, status> to dictionary when a request is made
        requestStatus.put(borrower,status);
    }

    public String getRequestStatus(String borrower) {
        return (String) requestStatus.get(borrower);
    }

    public String getBorrower(){

        String borrower = null;

        // hashmap iterator
        // reference: https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
        Iterator it = requestStatus.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Entry) it.next();
            if (pair.getValue().toString().equals("Borrowed")){
                borrower = pair.getKey().toString();
            }
        }
        return borrower;
    }




}
