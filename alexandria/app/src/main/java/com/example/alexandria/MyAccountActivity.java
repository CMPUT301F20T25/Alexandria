package com.example.alexandria;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class MyAccountActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String currentUserName;
        final String TAG = "Tag: Account";
        final Button saveButton;
        final FirebaseFirestore db;

        // current user name, for testing
        currentUserName = "testUser1";

        // database setup
        db = FirebaseFirestore.getInstance();
        final CollectionReference collectionRef = db.collection("users");
        final DocumentReference userDocRef = db.collection("users").document(currentUserName);

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
                    String userName = value.getId();
                    EditText userNameEditText = findViewById(R.id.editTextUserName);
                    userNameEditText.setText(userName);

                    // display phone number
                    String phone = (String) value.getData().get("phone number");
                    EditText phoneEditText = findViewById(R.id.editTextPhone);
                    phoneEditText.setText(phone);

                    // display email
                    String email = (String) value.getData().get("email");
                    EditText emailEditText = findViewById(R.id.editTextTextEmail);
                    emailEditText.setText(email);

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
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
