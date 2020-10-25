package com.example.alexandria;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText phoneEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Get UI components
        usernameEditText = (EditText) findViewById(R.id.editTextUsernameRegister);
        emailEditText = (EditText) findViewById(R.id.editTextEmail);
        passwordEditText = (EditText) findViewById(R.id.editTextTextPassword);
        confirmPasswordEditText = (EditText) findViewById(R.id.editTextTextPasswordConfirm);
        phoneEditText = (EditText) findViewById(R.id.editTextPhone);
        registerButton = (Button) findViewById(R.id.buttonRegister);

        // Set onClick Listener on the register button
        registerButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // Get inputs
                String username = usernameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String repeatedPass = confirmPasswordEditText.getText().toString();
                String phone = phoneEditText.getText().toString();

                // Validate inputs
                if(!isValidEmail(email)){
                    emailEditText.setError("Your input is invalid!");
                    return;
                }
                if(!password.equals(repeatedPass)){
                    passwordEditText.setError("");
                    confirmPasswordEditText.setError("Password doesn't match!");
                    return;
                }

                // Hash password
                // Referece: https://howtodoinjava.com/java/java-security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
                String hashedPassword = null;
                try{
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    md.update(password.getBytes());
                    byte[] bytes = md.digest();
                    StringBuilder sb = new StringBuilder();
                    for (byte aByte : bytes) {
                        sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
                    }
                    hashedPassword = sb.toString();
                }catch (NoSuchAlgorithmException e){
                    Log.d("RegisterPage", "No Such Algorithm");
                    return;
                }
                Log.d("RegisterPage", "Data is valid!");
                Log.d("RegisterPage", hashedPassword);


                // Store on local

                // Send request to the database

                // redirect to home activity
            }
        });
    }

    // Validate email address
    private boolean isValidEmail(String email){
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }
}
