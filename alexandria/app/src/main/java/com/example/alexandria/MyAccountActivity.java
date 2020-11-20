package com.example.alexandria;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

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

        final String currentUserID;
        final String TAG = "Tag: Account";
        final FirebaseFirestore db;

        // current user email, for testing
        currentUserID = "testuser1@fake.com";

        // database setup
        db = FirebaseFirestore.getInstance();
        final CollectionReference collectionRef = db.collection("users");
        final DocumentReference userDocRef = db.collection("users").document(currentUserID);

        final EditText userNameEditText = findViewById(R.id.editTextUserName);
        final EditText phoneEditText = findViewById(R.id.editTextPhone);
        final EditText emailEditText = findViewById(R.id.editTextTextEmail);

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch acceptedSwitch = findViewById(R.id.accepted_switch);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch deniedSwitch = findViewById(R.id.denied_switch);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch receivedSwitch = findViewById(R.id.received_switch);

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

                    // retrieve notification settings
                    Map<String,Boolean> map = (Map<String, Boolean>) value.getData().get("notificationSettings");
                    Boolean acceptedReqNotification = map.get("acceptedRequests");
                    Boolean denyReqNotification = map.get("deniedRequests");
                    Boolean receiveReqNotification = map.get("receivedRequests");
                    // set switch
                    acceptedSwitch.setChecked(acceptedReqNotification);
                    deniedSwitch.setChecked(denyReqNotification);
                    receivedSwitch.setChecked(receiveReqNotification);

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        // save button
        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save changes
                String name = userNameEditText.getText().toString();
                userDocRef.update("username", name);

                String phone = phoneEditText.getText().toString();
                userDocRef.update("phone number", phone);

                String email = emailEditText.getText().toString();
                userDocRef.update("email", email);

                // display message
                Toast.makeText(MyAccountActivity.this, "Changes Saved",Toast.LENGTH_LONG).show();
            }
        });

        // accept notification setting
        acceptedSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDocRef.update("notificationSettings.acceptedRequests", acceptedSwitch.isChecked());

            }
        });

        // deny notification setting
        deniedSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDocRef.update("notificationSettings.deniedRequests", deniedSwitch.isChecked());
            }
        });

        // receive notification setting
        receivedSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDocRef.update("notificationSettings.receivedRequests", receivedSwitch.isChecked());
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
