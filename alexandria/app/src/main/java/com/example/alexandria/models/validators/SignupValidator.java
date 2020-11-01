package com.example.alexandria.models.validators;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.ArrayList;

/**
 * Validate the signup information
 * @author han
 */
public class SignupValidator implements Validator{
    private final String username;
    private final String userBio;
    private final String email;
    private final String phone;
    private final String password;


    /**
     * Constructor of the SignupValidator
     * @param username String
     * @param userBio String
     * @param email String
     * @param phone String
     * @param pass String
     */
    SignupValidator(String username, String userBio, String email, String phone, String pass){
        this.username = username;
        this.userBio = userBio;
        this.email = email;
        this.phone = phone;
        this.password = pass;
    }

    /**
     * Constructor of the SignupValidator
     * @param username String
     * @param email String
     * @param phone String
     * @param pass String
     */
    SignupValidator(String username, String email, String phone, String pass){
        this.username = username;
        this.userBio = new String("");
        this.email = email;
        this.phone = phone;
        this.password = pass;
    }


    /**
     * check if username only contains letters, numbers and underscores. And username has to be shorter
     * than 25 characters
     * @return True if it's valid
     */
    private Boolean isUsernameValid(){
        Boolean valid = this.username != null && this.username.matches("[A-Za-z0-9_]+") && this.username.length() < 25;
        if(!valid){
            this.errors.add(new ValidationError("username", "Username can only contain letters, numbers, and _. With less than 25 characters."));
        }
        return valid;
    }


    /**
     * check if userBio is less than 400 characters
     * @return True if it's valid
     */
    private Boolean isUserBioValid(){
        Boolean valid = this.userBio != null && this.userBio.length() < 400;
        if(!valid){
            this.errors.add(new ValidationError("userbio", "Bio has to be less than 400 characters"));
        }
        return valid;
    }


    /**
     * check if email is valid
     * @return True if it's valid
     */
    private Boolean isEmailValid(){
        Boolean valid = this.email != null && this.email.matches("[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}");
        if(!valid){
            this.errors.add(new ValidationError("email", "Email is invalid"));
        }
        return valid;
    }

    /**
     * check if phone is valid
     * @return True if it's valid
     */
    private Boolean isPhoneValid(){
        Boolean valid = this.phone != null && this.phone.matches("(\\d{3}[-]?){1,2}(\\d{4})");
        if(!valid){
            this.errors.add(new ValidationError("phone", "Phone number is invalid"));
        }
        return valid;
    }

    /**
     * check if the password is valid
     * @return True if it's valid
     */
    private Boolean isPasswordValid(){
        Boolean valid = this.password != null && this.password.matches("^[A-Za-z0-9#?!@$%^&*-]+$");
        if(!valid){
            this.errors.add(new ValidationError("password", "Password contains invalid characters!"));
        }
        return valid;
    }


    /**
     * Check if given information is valid
     * @return Boolean, true if all information is valid
     */
    @Override
    public Boolean isValid() {
        Boolean valid = this.isUsernameValid();
        valid &= this.isUserBioValid();
        valid &= this.isEmailValid();
        valid &= this.isPasswordValid();
        valid &= this.isPhoneValid();
        return valid;
    }

    /**
     * Return the error caused validation failed
     * @return ValidationError
     */
    @Override
    public ArrayList<ValidationError> getError() {
        return this.errors;
    }
}
