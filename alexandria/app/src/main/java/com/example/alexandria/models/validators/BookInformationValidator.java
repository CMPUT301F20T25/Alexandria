package com.example.alexandria.models.validators;

import java.util.ArrayList;

/**
 * Validate book information
 * @author han
 */
public class BookInformationValidator implements Validator{
    private final String title;
    private final String author;
    private final String description;
    private final String isbn;


    /**
     * Constructor of BookInformationValidator
     * @param title String
     * @param author String
     * @param description String
     * @param isbn String
     */
    BookInformationValidator(String title, String author, String description, String isbn){
        this.title = title;
        this.author = author;
        this.description = description;
        this.isbn = isbn;
    }


    /**
     * Check if book's isbn is valid
     * @return true if valid.
     */
    private Boolean isISBNValid(){
        Boolean valid = this.isbn != null && this.isbn.matches("(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]");
        if(!valid){
            ValidationError error = new ValidationError("isbn", "Invalid ISBN");
        }
        return valid;
    }


    @Override
    public Boolean isValid() {

        // validate ISBN
        Boolean valid = this.isISBNValid();
        return valid;
    }

    @Override
    public ArrayList<ValidationError> getError() {
        return this.errors;
    }
}
