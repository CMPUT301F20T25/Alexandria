package com.example.alexandria;

public class Book {

    private final String id;
    private final String isbn;
    private final String description;
    private final String title;
    private final String author;
    private final String bookStatus;

    Book(String id, String isbn, String description, String title, String author, String bookStatus){
        this.id = id;
        this.isbn = isbn;
        this.description = description;
        this.title = title;
        this.author = author;
        this.bookStatus = bookStatus;
    }

    String getBookID(){ return this.id;}

    String getBookISBN(){ return this.isbn;}

    String getBookDescription(){ return this.description;}

    String getBookTitle(){ return this.title;}

    String getAuthorName(){ return this.author;}

    String getBookStatus(){ return this.bookStatus;}

}

