package com.example.alexandria;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.example.alexandria.models.user.User;
import com.example.alexandria.models.user.UserManager;
import com.example.alexandria.utils.PassHash;
import com.example.alexandria.models.validators.SignupValidator;
import com.example.alexandria.models.validators.ValidationError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class SignUpActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText phoneEditText;
    private Button registerButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

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
                SignupValidator validator = new SignupValidator(username, email, phone, password);
                if(!validator.isValid()){
                    ArrayList<ValidationError> errors = validator.getError();
                    for(ValidationError error : errors){
                        switch(error.getField()){
                            case "email":
                                emailEditText.setError(error.getMessage());
                                break;
                            case "phone":
                                phoneEditText.setError(error.getMessage());
                                break;
                            case "password":
                                passwordEditText.setError(error.getMessage());
                                break;
                            default:
                                Toast.makeText(SignUpActivity.this,"Unknown Error, please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return;
                }

                if(!password.equals(repeatedPass)){
                    passwordEditText.setError("Password doesn't match!");
                    confirmPasswordEditText.setError("Password doesn't match!");
                    return;
                }

                // Hash password
                // Referece: https://howtodoinjava.com/java/java-security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
                String hashedPassword = PassHash.hash(password);
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Log.d("Signup", "createUserWithEmail:success");
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();

                                    // Setting up user model
                                    UserManager.getInstance().setUser(firebaseUser, phone, username, "");
                                    User user = UserManager.getInstance().getUser();
                                    Log.d("Signup", UserManager.getInstance().getUser().getEmail());
                                    Log.d("Signup", UserManager.getInstance().getUser().getPhone());

                                    // Upload user's info to cloud
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                                    // Initializing payload
                                    Map<String, Object> userInfo = new HashMap<>();
                                    userInfo.put("email", UserManager.getInstance().getUser().getEmail());
                                    userInfo.put("phone", UserManager.getInstance().getUser().getPhone());
                                    userInfo.put("bio", UserManager.getInstance().getUser().getUserBio());
                                    userInfo.put("username", UserManager.getInstance().getUser().getUsername());

                                    // upload data
                                    db.collection("users").document(UserManager.getInstance().getUser().getEmail())
                                            .set(userInfo)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("Signup", "DocumentSnapshot successfully written!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w("Signup", "Error writing document", e);
                                                }
                                            });


                                    Intent home = new Intent(SignUpActivity.this, HomeActivity.class);
                                    startActivity(home);
                                }else{
                                    Log.d("Sign Up", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(SignUpActivity.this, "Unknown error",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }


}
