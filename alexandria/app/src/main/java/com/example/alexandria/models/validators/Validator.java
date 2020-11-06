package com.example.alexandria.models.validators;

import java.util.ArrayList;

/**
 * @author han
 */
public interface Validator {
    ArrayList<ValidationError> errors = new ArrayList<ValidationError>();
    /**
     * Return if implemented model is valid or not
     * @author han
     * @return Boolean
     */
    public Boolean isValid();

    /**
     * Return the error that caused the validation failed
     * @author han
     * @return A list of ValidationError
     * @see ValidationError
     */
    public ArrayList<ValidationError> getError();

}
