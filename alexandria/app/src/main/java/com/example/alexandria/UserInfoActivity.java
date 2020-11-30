package com.example.alexandria;
/**
 * Displays the public profile information of the given user.
 * @author Kyla Wong, ktwong@ualberta.ca
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

public class UserInfoActivity extends AppCompatActivity {
    private TextView username;
    private TextView email;
    private TextView phone;
    private TextView bio;
    private TextView emailDesc;
    private TextView phoneDesc;
    private TextView bioDesc;
    private ImageView backButton;

    private String userId;

    private static final String TAG = "User Info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        //unpack intent
        try {
            Intent intent = getIntent();
            userId = intent.getStringExtra("userId");
        } catch (Exception e) {
            Log.e(TAG, "No userId given");
            //finish();
        }

        username = (TextView) findViewById(R.id.userInfo_username);
        email = (TextView) findViewById(R.id.userinfo_emailText);
        phone = (TextView) findViewById(R.id.userinfo_phoneText);
        bio = (TextView) findViewById(R.id.userinfo_bioText);

        emailDesc = (TextView) findViewById(R.id.userinfo_emailDesc);
        emailDesc.setTypeface(emailDesc.getTypeface(), Typeface.ITALIC);
        emailDesc.setText("Email:");
        phoneDesc = (TextView) findViewById(R.id.userinfo_phoneDesc);
        phoneDesc.setTypeface(phoneDesc.getTypeface(), Typeface.ITALIC);
        phoneDesc.setText("Phone Number:");
        bioDesc = (TextView) findViewById(R.id.userinfo_bioDesc);
        bioDesc.setTypeface(bioDesc.getTypeface(), Typeface.ITALIC);

        backButton = (ImageView) findViewById(R.id.userInfo_backImage);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setInfo();
    }

    /**
     * Queries the database for the user's info and sets it to display in the UI
     */
    private void setInfo() {
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        username.setText("@" + documentSnapshot.getData().get("username").toString());
                        email.setText(documentSnapshot.getData().get("email").toString());
                        String phoneNumber = documentSnapshot.getData().get("phone number").toString();
                        phoneNumber = "(" + phoneNumber.substring(0,3) + ") " + phoneNumber.substring(4,7) + "-" + phoneNumber.substring(8);
                        phone.setText(phoneNumber);
                        bio.setText(documentSnapshot.getData().get("bio").toString());
                        bioDesc.setText("A bit about @" + documentSnapshot.getData().get("username").toString() + " ...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error getting user info from database.");
                        finish();
                    }
                });
    }
}