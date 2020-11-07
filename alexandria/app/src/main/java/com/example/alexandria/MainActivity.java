package com.example.alexandria;

import com.example.alexandria.models.user.User;
import com.example.alexandria.models.user.UserManager;
import com.example.alexandria.models.validators.EmailValidator;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

                // validate input
                EmailValidator validator = new EmailValidator(email);
                if(!validator.isValid() || password.length() <= 0){
                    if(!validator.isValid()){
                        emailEditText.setError("Invalid format!");
                    }
                    if(password.length() <= 0){
                        passwordEditText.setError("Please enter your password");
                    }
                    return;
                }

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
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            // get user document relate with current user
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference docRef = db.collection("users").document(email);

                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            Log.d("Login", "DocumentSnapshot data: " + document.getData());
                                            UserManager.getInstance()
                                                    .setUser(firebaseUser,
                                                            (String) document.getData().get("phone"),
                                                            (String) document.getData().get("username"),
                                                            (String) document.getData().get("bio"));
                                            User user = UserManager.getInstance().getUser();

                                            Log.d("Login", user.getEmail());
                                            Log.d("Login", user.getPhone());
                                            Intent home = new Intent(MainActivity.this, HomeActivity.class);
                                            startActivity(home);
                                        } else {
                                            Log.d("Login", "No such document");
                                        }
                                    } else {
                                        Log.d("Login", "get failed with ", task.getException());
                                    }
                                }
                            });

                            // save user info to ram

                        }else{
                            Log.d("Login", "signInWithEmailPassword:failed", task.getException());
                            Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }


}
