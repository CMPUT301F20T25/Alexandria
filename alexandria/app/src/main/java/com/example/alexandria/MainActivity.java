package com.example.alexandria;

import com.example.alexandria.utils.PassHash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


/**
 * MainActivity. Responsible for logging in and redirect to sign up page
 * @author han
 */
public class MainActivity extends AppCompatActivity{

    private static final String TAG = "tag";
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    static protected DocumentReference currentUserRef = null;

    public static final String User_Data = "com.example.alexandria.USER";

    /**
     * onCreate method
     * @author han
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        // Initializing mAuth
        mAuth = FirebaseAuth.getInstance();
        Log.d("Login", mAuth.getApp().toString());

        // Get UI component
        emailEditText = (EditText) findViewById(R.id.editTextEmailLogin);
        passwordEditText = (EditText) findViewById(R.id.editTextTextPassword);
        loginButton = (Button) findViewById(R.id.buttonLogin);
        registerButton = (Button) findViewById(R.id.buttonRegister);

        // setting up click listener on the loginButton
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // getting info from components
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // check input
                if (email.equals("") || password.equals("")){
                    Toast.makeText(MainActivity.this, "Please enter Email Address and Password", Toast.LENGTH_SHORT).show();
                } else {
                    login(email, password);
                }
            }
        });

        // Setting up click listener on the registerTextView
        registerButton.setOnClickListener(v -> {
            Log.d("LoginInfo", "User is asking to register");
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

    }


    /**
     * Get user's input and try to login throught Firebse Authentication Module
     * @author han
     */
    public void login(String email, String password){

        String hashedPassword = PassHash.hash(password);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d("Login", "signInWithEmailPassword:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            currentUserRef = db.collection("users").document(user.getEmail());
                            String userEmail = mAuth.getCurrentUser().getEmail();
                            Intent home = new Intent(MainActivity.this, HomeActivity.class);
                            home.putExtra(User_Data, userEmail);
                            startActivity(home);
                        }else{
                            Log.d("Login", "signInWithEmailPassword:failed", task.getException());
                            Toast.makeText(MainActivity.this, "Email address and Password NOT match", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
