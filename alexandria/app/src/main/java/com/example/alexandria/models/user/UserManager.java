package com.example.alexandria.models.user;

import com.google.firebase.auth.FirebaseUser;

public class UserManager {
    private User user;
    private static UserManager manager = new UserManager();

    private UserManager(){};

    public static UserManager getInstance(){
        return manager;
    }

    public void setUser(FirebaseUser firebaseUser, String phone, String username, String userBio){
        this.user = new User(firebaseUser, phone, username, userBio);
    }

    public User getUser(){
        return this.user;
    }
}
