package com.example.alexandria.models.validators;

import java.util.ArrayList;

/**
 * Validate email information, only limited to formatting error
 * @author han
 *
 */
public class EmailValidator implements Validator{
    private final String email;

    /**
     * Constructor
     * @param email email address to validate
     */
    public EmailValidator(String email){
        this.email = email;
    }

    /**
     * Validate email address format
     * @return true if valid
     */
    @Override
    public Boolean isValid() {
        Boolean valid = this.email != null && this.email.matches("[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}");
        if(!valid){
            this.errors.add(new ValidationError("email", "Email is invalid"));
        }
        return valid;
    }

    /**
     * return the error
     * @return a list of ValidationError
     */
    @Override
    public ArrayList<ValidationError> getError() {
        return this.errors;
    }
}
