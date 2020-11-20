package com.example.alexandria;
/**
 * allows user to add a book
 * @author Xueying Luo
 */

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alexandria.models.validators.BookInformationValidator;
import com.example.alexandria.models.validators.SignupValidator;
import com.example.alexandria.models.validators.ValidationError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddBookActivity extends AppCompatActivity {

    private String TAG = "add book";
    private DocumentReference userRef = MainActivity.currentUserRef;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();

        EditText title = findViewById(R.id.addBookTitle);
        EditText author = findViewById(R.id.addBookAuthor);
        EditText isbn = findViewById(R.id.addBookISBN);
        EditText descr = findViewById(R.id.addBookDescr);

        Button addButton = findViewById(R.id.addBookButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "add button clicked");

                String newTitle = title.getText().toString();
                String newAuthor = author.getText().toString();
                String newISBN = isbn.getText().toString();
                String newDescr = descr.getText().toString();

                // split author text by '\n'
                List<String> authorList = Arrays.asList(newAuthor.split("\n"));

                // validate input TODO:validator is not working properly
//                BookInformationValidator validator =
//                        new BookInformationValidator(newTitle, newAuthor, newDescr, newISBN);
//                if(!validator.isValid()){ // invalid input
//                    Log.d(TAG, "invalid input");
//                    ArrayList<ValidationError> errors = validator.getError();
//                    for(ValidationError error : errors){
//
//                        if ("isbn".equals(error.getField())) {
//                            isbn.setError(error.getMessage());
//                            Log.d(TAG, "invalid isbn");
//                        } else {
//                            Log.d(TAG, "unknown error");
//
//                            Toast.makeText(AddBookActivity.this,
//                                    "Unknown Error, please try again", Toast.LENGTH_SHORT).show();
//                        }
//                    }
                if (false){ // invalid input
                } else { // valid input

                    Log.d(TAG, "valid input");

                    // generate bookID by checking existing docID

                    final int[] counter = {1};
                    final String[] newBookID = {newISBN + '-' + counter[0]}; // isbn-counter

                    final boolean[] docFound = {true};

                    // store all bookID in a list and check if the list contains the new generated id
                    ArrayList<String> allDocID = new ArrayList<>();
                    CollectionReference collRef = db.collection("books");
                    collRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String id = document.getId();
                                    allDocID.add(id);
                                }

                                Log.d(TAG, "id list: "+allDocID.toString());

                                while (docFound[0]) {
                                    if (allDocID.contains(newBookID[0])) { // if id exists
                                        Log.d(TAG, "document exists - counter = " + counter[0]);
                                        counter[0] += 1;
                                        newBookID[0] = newISBN + '-' + counter[0];
                                    } else { // id does not exist
                                        Log.d(TAG, "No such document, keep this id : " + newBookID[0]);
                                        docFound[0] = false;

                                        DocumentReference bookRef = db.collection("books").document(newBookID[0]);

                                        Map<String, String> status = new HashMap<>();
                                        status.put("borrower", null);
                                        status.put("owner", "available");
                                        status.put("public", "available");

                                        Map<String, Object> bookInfo = new HashMap<>();
                                        bookInfo.put("authors", authorList);
                                        bookInfo.put("title", newTitle);
                                        bookInfo.put("description", newDescr);
                                        bookInfo.put("isbn", newISBN);
                                        bookInfo.put("borrower", null);
                                        bookInfo.put("ownerReference", userRef);
                                        bookInfo.put("photo", null);
                                        bookInfo.put("requestedUsers", null);
                                        bookInfo.put("status", status);

                                        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot document = task.getResult();

                                                // get username
                                                String username = String.valueOf(document.getData().get("username"));
                                                bookInfo.put("owner", username);

                                                // create a document in books collection
                                                bookRef.set(bookInfo)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "successfully added a book");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG, "adding book failed");
                                                            }
                                                        });

                                                // update the user's book list
                                                userRef.update("books", FieldValue.arrayUnion(bookRef))
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "user's book list updated successfully");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG, "user's book list update failed");

                                                            }
                                                        });
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    });

                }

                finish();

            }

        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}