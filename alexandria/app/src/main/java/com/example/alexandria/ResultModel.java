package com.example.alexandria;

import android.os.Build;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public abstract class ResultModel {
    protected int viewType;

    public ResultModel() { //viewType = 0 means user, 1 means book
        if (viewType != 0 && viewType != 1) {
            throw new IllegalArgumentException("viewType must be 0 or 1");
        }
        this.viewType = getContentViewId();
    }

    public int getViewType() {
        return this.viewType;
    }
    abstract int getContentViewId();

    static class SearchUserItemModel extends ResultModel{
        private String username;
        private String bio;

        public SearchUserItemModel(@NonNull final String username) {
            super();
            this.username = username;
            this.bio = " ";
        }

        public SearchUserItemModel(@NonNull final String username, String bio) {
            super();
            this.username = username;
            this.bio = bio;
        }

        @NonNull
        public String getUsername() { return this.username; }

        public void setUsername(String username) { this.username = username; }

        public String getBio() { return this.bio; }

        public void setBio(String bio) { this.bio = bio; }

        @Override
        int getContentViewId() {
            return R.layout.activity_search_useritem;
        }
    }

    static class SearchBookItemModel extends ResultModel {
        private String title;
        private ArrayList<String> authors;
        private String owner;
        private String publicStatus;
        //private ImageView photo; //TODO: figure out how to handle images

        public SearchBookItemModel(@NonNull String title, @NonNull ArrayList<String> authors, @NonNull String owner, @NonNull String publicStatus) {
            super();
            this.title = title;
            this.authors = authors;
            this.owner = owner;
            this.publicStatus = publicStatus;
        }

        @NonNull
        public String getTitle() { return this.title; }

        public void setTitle(String title) { this.title = title; }

        @NonNull
        public ArrayList<String> getAuthors() { return this.authors; }

        public void setAuthors(ArrayList<String> authors) { this.authors = authors; }

        @NonNull
        public String getOwner() { return this.owner; }

        public void setOwner(String owner) { this.owner = owner; }

        @NonNull
        public String getPublicStatus() { return this.publicStatus; }

        public void setPublicStatus(String publicStatus) { this.publicStatus = publicStatus; }

        /*
        public ImageView getPhoto() { return this.photoView; }

        public void setPhoto(ImageView photo) { this.photo = photo; }
        */

        @Override
        int getContentViewId() {
            return R.layout.activity_search_bookitem;
        }
    }
}
