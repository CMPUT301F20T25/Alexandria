package com.example.alexandria;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class MyAccountActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String TAG = "Tag: Account";
        final FirebaseFirestore db;

        // retrieve information of current user
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance ();
        FirebaseUser user = mAuth.getCurrentUser();
        String currentUserEmail = user.getEmail();
        String currentUserName = currentUserEmail.substring(0, currentUserEmail.indexOf("@"));

        // database setup
        db = FirebaseFirestore.getInstance();
        final CollectionReference collectionRef = db.collection("users");
        final DocumentReference userDocRef = db.collection("users").document(currentUserEmail);

        final TextView userNameEditText = findViewById(R.id.textUserName);
        final TextView phoneEditText = findViewById(R.id.textPhone);
        final TextView emailEditText = findViewById(R.id.textTextEmail);

        // get realtime updates with firebase
        userDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                if (value != null && value.exists()) {
                    Log.d(TAG, "Current data: " + value.getData());
                    // display user name
                    String userName = (String) value.getData().get("username");
                    userNameEditText.setText(userName);

                    // display phone number
                    String phone = (String) value.getData().get("phone number");
                    phoneEditText.setText(phone);

                    // display email
                    String email = (String) value.getData().get("email");
                    emailEditText.setText(email);

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        // open edit profile activity
        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditProfileActivity();
            }
        });

        // open setting activity
        Button settingButton = (Button) findViewById(R.id.setting_button);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingActivity();
            }
        });

        // open myBook activity
        Button myBookButton = (Button) findViewById(R.id.myBook_button);
        myBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMyBookActivity();
            }
        });

        // open message activity
        Button messageButton = (Button) findViewById(R.id.message_button);
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMessageActivity();
            }
        });

        // log out and open main activity
        Button logOutButton = (Button) findViewById(R.id.logOut_button);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                openMainActivity();
            }
        });
    }

    private void openEditProfileActivity(){
        Intent profileIntent = new Intent(this, EditProfileActivity.class);
        startActivity(profileIntent);
    }

    private void openSettingActivity(){
        Intent settingIntent = new Intent(this, SettingActivity.class);
        startActivity(settingIntent);
    }

    private void openMyBookActivity(){
        Intent myBookIntent = new Intent(this, MyBookRequestedActivity.class);
        startActivity(myBookIntent);
    }

    private void openMessageActivity(){
        Intent messageIntent = new Intent(this, MessageActivity.class);
        startActivity(messageIntent);
    }

    private void openMainActivity(){
        Intent logOutIntent = new Intent(this, MainActivity.class);
        startActivity(logOutIntent);
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
