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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    
    /**
    * onCreate method
    * @author han
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                login(email, password);

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
        //generate password hash
        //Reference: https://howtodoinjava.com/java/java-security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
        String hashedPassword = PassHash.hash(password);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d("Login", "signInWithEmailPassword:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent home = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(home);
                        }else{
                            Log.d("Login", "signInWithEmailPassword:failed", task.getException());
                            Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        /*
        // compare with password in database - not completed
        
        db = FirebaseFirestore.getInstance();
        String currentPassword = null;// read from database

        // temporary password - for test use
        currentPassword = "123";
        //generatedPassword = password;

        //if (currentPassword.equals(generatedPassword)){
            // go to home activity

            // to be modified later when the password check is finished
            Intent home = new Intent(this, HomeActivity.class);
            startActivity(home);

        } else if (password.isEmpty()) {
            // no action
        }
        else {
            // notify user with snackbar - incorrect password/username
            // reference: https://developer.android.com/training/snackbar/showing
            View coordinatorLayout = findViewById(R.id.coordinatorLayout);
            Snackbar snackbar = Snackbar.make(coordinatorLayout,
                    "Incorrect username/password", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }

        // logging user info
        Log.d("LoginInfo", username);
        Log.d("LoginInfo", password);

        */
    }


}
