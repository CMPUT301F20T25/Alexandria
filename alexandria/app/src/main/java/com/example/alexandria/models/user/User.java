package com.example.alexandria.models.user;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

/**
 * User model
 * @author han
 */
public class User {
    private FirebaseUser firebaseUser;
    private String userBio;
    private String phone;
    private String username;

    /**
     * Constructor
     * @param firebaseUser the authorized firebaseUser
     * @param userBio Bio of user
     */
    public User(FirebaseUser firebaseUser, String phone, String username, String userBio){
        this.firebaseUser = firebaseUser;
        this.username = username;
        this.userBio = userBio;
        this.phone = phone;
    }


    /**
     * Getter of user's email
     * @return String, the user email
     */
    public String getEmail(){
        return this.firebaseUser.getEmail();
    }

    /**
     * Getter of user's phone
     * @return String, the user's phone
     */
    public String getPhone(){
        return this.phone;
    }


    /**
     * Getter of user's bio
     * @return String, the user's bio
     */
    public String getUserBio(){
        return this.userBio;
    }

    /**
     * Getter of user's username
     * @return String, the user's bio
     */
    public String getUsername(){
        return this.username;
    }

}
