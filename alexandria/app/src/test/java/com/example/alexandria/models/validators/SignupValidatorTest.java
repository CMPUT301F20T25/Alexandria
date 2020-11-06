package com.example.alexandria.models.validators;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class SignupValidatorTest {

    @Test
    public void isValid() {
        // Testing valid input
        SignupValidator validator = new SignupValidator("Han", "None", "han11@ualberta.ca", "780-123-1234", "wuhan111$");
        assertEquals("Should be valid!", true, validator.isValid());

        // Testing valid input
        validator = new SignupValidator("Han", "han11@ualberta.ca", "780-123-1234", "wuhan111$");
        assertEquals("Should be valid!", true, validator.isValid());

        // Testing invalid input
        validator = new SignupValidator("Han", "None", "han11@ualberta.ca", "7820-123-1234", "wuhan111$");
        assertEquals("Should be invalid!", false, validator.isValid());

        validator = new SignupValidator("Han", "None", "han11@erta@ca", "782-123-1234", "wuhan111$");
        assertEquals("Should be invalid!", false, validator.isValid());

        validator = new SignupValidator("Han", "None", "han11@erta.ca", "782-123-1224", "~wuhan111$");
        assertEquals("Should be invalid!", false, validator.isValid());

    }

    @Test
    public void getError() {
        // no error test
        SignupValidator validator = new SignupValidator("Han", "None", "han11@ualberta.ca", "780-123-1234", "wuhan111$");
        validator.isValid();
        assertEquals("Shouldn't have errors", new ArrayList<ValidationError>(), validator.getError());

        // with error test
        validator = new SignupValidator("Han", "None", "han11@ualberta@ca", "7803-123-1234", "~wuhan111$");
        validator.isValid();
        ArrayList<ValidationError> errors = validator.getError();
        assertEquals("Incorrect length", 3, errors.size());
        for(ValidationError error : errors){
            switch(error.getField()){
                case "email":
                    assertEquals("Email is invalid", error.getMessage());
                    break;
                case "phone":
                    assertEquals("Phone number is invalid", error.getMessage());
                    break;
                case "password":
                    assertEquals("Password contains invalid characters!", error.getMessage());
                    break;
                default:
                    throw new AssertionError("Unexpected error field");
            }
        }
    }

}