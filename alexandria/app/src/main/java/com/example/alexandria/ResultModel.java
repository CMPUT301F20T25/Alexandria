package com.example.alexandria;
/**
 * Abstract Model class used for formatting the SearchActivity search results
 * @author Kyla Wong, ktwong@ualberta.ca
 */

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public abstract class ResultModel {
    protected int viewType;

    /**
     * Constructor of ResultModel; sets the viewType field
     */
    public ResultModel() { //viewType = 0 means user, 1 means book
        if (viewType != 0 && viewType != 1) {
            throw new IllegalArgumentException("viewType must be 0 or 1");
        }
        this.viewType = getModelContentViewId();
    }

    /**
     * Returns the layout id being used
     * @return the layout id
     */
    public int getViewType() {
        return this.viewType;
    }

    /**
     * Gets the layout id to be used
     * @return the layout id
     */
    abstract int getModelContentViewId();

    /**
     * Subclass of ResultModel used for modelling user data
     * @author Kyla Wong, ktwong@ualberta.ca
     */
    static class SearchUserItemModel extends ResultModel{
        private String username;
        private String userId;
        private String bio;

        public SearchUserItemModel(@NonNull final String userId, String username, String bio) {
            super();
            this.userId = userId;
            this.username = username;
            this.bio = bio;
        }

        /**
         * Set user's database document Id
         * @return String of userId
         */
        @NonNull
        public String getUserId() { return this.userId; }

        /**
         * Return the userId
         * @param userId String of userId
         */
        public void setUserId(String userId) { this.userId = userId; }

        /**
         * Returns the user's username
         * @return String of username
         */
        public String getUsername() { return this.username; }

        /**
         * Sets the username
         * @param username String of username
         */
        public void setUsername(String username) { this.username = username; }

        /**
         * Returns the user's bio
         * @return String of bio
         */
        public String getBio() { return this.bio; }

        /**
         * Sets the bio
         * @param bio String of bio
         */
        public void setBio(String bio) { this.bio = bio; }

        /**
         * Returns the layout id
         * @return layout id
         */
        @Override
        int getModelContentViewId() {
            return R.layout.activity_search_useritem;
        }
    }

    /**
     * Subclass of ResultModel used for modelling book data
     * @author Kyla Wong, ktwong@ualberta.ca
     */
    static class SearchBookItemModel extends ResultModel {
        private String title;
        private ArrayList<String> authors;
        private String owner;
        private String publicStatus;
        private String bookId;

        public SearchBookItemModel(@NonNull String bookId, @NonNull String title, @NonNull ArrayList<String> authors, @NonNull String owner, @NonNull String publicStatus) {
            super();
            this.bookId = bookId;
            this.title = title;
            this.authors = authors;
            this.owner = owner;
            this.publicStatus = publicStatus;
        }

        /**
         * Returns book title
         * @return String of title
         */
        @NonNull
        public String getTitle() { return this.title; }

        /**
         * Sets book title
         * @param title String of title
         */
        public void setTitle(String title) { this.title = title; }

        /**
         * Returns ArrayList of book authors
         * @return ArrayList<String> of book authors
         */
        @NonNull
        public ArrayList<String> getAuthors() { return this.authors; }

        /**
         * Sets the ArrayList of book authors
         * @param authors ArrayList<String> of authors
         */
        public void setAuthors(ArrayList<String> authors) { this.authors = authors; }

        /**
         * Returns the book owner
         * @return String of book owner's username
         */
        @NonNull
        public String getOwner() { return this.owner; }

        /**
         * Sets the book owner's username
         * @param owner String of book owner's username
         */
        public void setOwner(String owner) { this.owner = owner; }

        /**
         * Returns the public status of the book
         * @return String of public book status
         */
        @NonNull
        public String getPublicStatus() { return this.publicStatus; }

        /**
         * Sets the public status of the book
         * @param publicStatus String of public book status
         */
        public void setPublicStatus(String publicStatus) { this.publicStatus = publicStatus; }

        /**
         * Returns the id of the book as listed in the database
         * @return String of book id
         */
        public String getBookId() { return this.bookId; }

        /**
         * Returns layout id
         * @return layout id
         */
        @Override
        int getModelContentViewId() {
            return R.layout.activity_search_bookitem;
        }
    }
}
