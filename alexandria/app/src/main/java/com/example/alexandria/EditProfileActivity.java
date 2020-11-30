package com.example.alexandria;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class EditProfileActivity extends AppCompatActivity implements ChangePasswordDialog.DialogListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // final String currentUserID;
        final String TAG = "Tag: Account";
        final FirebaseFirestore db;

        // retrieve information of current user
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance ();
        FirebaseUser user = mAuth.getCurrentUser();
        String currentUserEmail = user.getEmail();
        String currentUserName = currentUserEmail.substring(0, currentUserEmail.indexOf("@"));

        final EditText userNameEditText = findViewById(R.id.EditTextUserName);
        final EditText phoneEditText = findViewById(R.id.EditTextPhone);
        final EditText emailEditText = findViewById(R.id.EditTextTextEmail);

        // database setup
        db = FirebaseFirestore.getInstance();
        final CollectionReference collectionRef = db.collection("users");
        final DocumentReference userDocRef = db.collection("users").document(currentUserEmail);

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
                Toast.makeText(EditProfileActivity.this, "Changes Saved",Toast.LENGTH_LONG).show();

            }
        });

        // change password button
        Button changePasswordButton = (Button) findViewById(R.id.changePassword_button);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });

    }

    private void showChangePasswordDialog(){
        ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
        changePasswordDialog.show(getSupportFragmentManager(), "Change Password Dialog");
    }

    @Override
    public void applyTexts(String currentPassword, String newPassword){
        // validate current password
        // change password
        if(passwordValidator(currentPassword, newPassword)){
            // update password
            // authenticate current password before changing password
            // reference: https://www.youtube.com/watch?v=IyBSlDUCJOA
            FirebaseAuth mAuth = FirebaseAuth.getInstance ();
            FirebaseUser user = mAuth.getCurrentUser();
            String currentUserEmail = user.getEmail();
            AuthCredential authCredential = EmailAuthProvider.getCredential(currentUserEmail, currentPassword);
            user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // authenticate success
                    // update password
                    user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EditProfileActivity.this, "Password Changed",Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, "Failed "+e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // authenticate failure
                    Toast.makeText(EditProfileActivity.this, "WRONG Current Password: "+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });


        }
    }

    private boolean passwordValidator(String currentPassword, String newPassword){
        if (currentPassword.equals("")){
            Toast.makeText(EditProfileActivity.this, "Empty Current Password",Toast.LENGTH_LONG).show();
            return false;
        }
        if (newPassword.equals("")){
            Toast.makeText(EditProfileActivity.this, "Empty New Password",Toast.LENGTH_LONG).show();
            return false;
        }
        if (newPassword.length()<6){
            Toast.makeText(EditProfileActivity.this, "Password must be more than 6 characters",Toast.LENGTH_LONG).show();
            return false;
        }
        if (newPassword.length()>15){
            Toast.makeText(EditProfileActivity.this, "Password must be fewer than 15 characters",Toast.LENGTH_LONG).show();
            return false;
        }
        if (newPassword.length()>=6 & newPassword.length()<=15){
            return true;
        }
        return false;
    }


}