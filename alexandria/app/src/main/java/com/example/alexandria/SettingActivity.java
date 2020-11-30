package com.example.alexandria;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Map;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // final String currentUserID;
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

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch acceptedSwitch = findViewById(R.id.accept_switch);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch deniedSwitch = findViewById(R.id.deny_switch);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch receivedSwitch = findViewById(R.id.receive_switch);

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
}