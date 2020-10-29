package com.example.alexandria;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "tag";
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get UI components
        usernameEditText = (EditText) findViewById(R.id.editTextUsernameLogin);
        passwordEditText = (EditText) findViewById(R.id.editTextTextPassword);
        loginButton = (Button) findViewById(R.id.buttonLogin);
        registerButton = (Button) findViewById(R.id.buttonRegister);

        // setting up click listener on the loginButton
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // getting info from components
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                login(username, password);

            }
        });

        // Setting up click listener on the registerTextView
        registerButton.setOnClickListener(v -> {
            Log.d("LoginInfo", "User is asking to register");
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }


    public void login(String username, String password){
        //generate password hash
        //Reference: https://howtodoinjava.com/java/java-security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }


        // compare with password in database - not completed
        
        db = FirebaseFirestore.getInstance();
        String currentPassword = null;// read from database

        // temporary password - for test use
        currentPassword = "123";
        generatedPassword = password;

        if (currentPassword.equals(generatedPassword)){
            // go to home activity

            // to be modified later when the password check is finished
            Intent home = new Intent(this, HomeActivity.class);
            startActivity(home);

        } else if (password.isEmpty()) {
            // no action
            return;
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


    }


}
