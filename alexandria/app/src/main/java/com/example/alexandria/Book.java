package com.example.alexandria;

public class Book {
    private String isbn;
    private String description;
    private String name;
    private String author;
    private String owner;

    Book(String isbn, String description,  String name, String author, String owner){
        this.isbn = isbn;
        this.description = description;
        this.name = name;
        this.author = author;
        this.owner = owner;
    }

    String getBookISBN(){ return this.isbn;}

    String getBookDescription(){ return this.description;}

    String getBookName(){ return this.name;}

    String getAuthorName(){ return this.author;}

    String getBookOwner(){ return this.owner;}

}
