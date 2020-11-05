package com.example.alexandria;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.firestore.FirebaseFirestore;

public class MyAccountActivity extends BaseActivity {
    private String currentUserName;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_my_account;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_user;
    }
}
