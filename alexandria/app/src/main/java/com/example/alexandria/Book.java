package com.example.alexandria;

public class Book {
    private String isbn;
    private String description;
    private boolean isRent;
    private String name;
    private String author;
    private String owner;

    Book(String isbn, String description, boolean isRent, String name, String author, String owner){
        this.isbn = isbn;
        this.description = description;
        this.isRent = isRent;
        this.name = name;
        this.author = author;
        this.owner = owner;
    }

    String getBookISBN(){ return this.isbn;}

    String getBookDescription(){ return this.description;}

    Boolean getIsRent(){ return this.isRent;}

    String getBookName(){ return this.name;}

    String getAuthorName(){ return this.author;}

    String getBookOwner(){ return this.owner;}

}
