package com.example.alexandria;
/**
 * allows user to edit book information
 * @author Xueying Luo
 */

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.security.interfaces.DSAKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditBookActivity extends AppCompatActivity {

    FirebaseFirestore db;

    private String oldISBN;
    private DocumentReference userRef = MainActivity.currentUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set auto fill texts
        ImageView imageView = findViewById(R.id.editImage);
        EditText titleView = findViewById(R.id.editTitle);
        EditText authorView = findViewById(R.id.editAuthor);
        EditText isbnView = findViewById(R.id.editISBN);
        EditText descrView = findViewById(R.id.editDescr);

        // get intent
        Intent intent = getIntent();
        String bookID = intent.getStringExtra("book");

        db = FirebaseFirestore.getInstance();
        final DocumentReference bookRef = db.collection("books").document(bookID);

        bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());

                        // get data from database

                        ArrayList<String> authorList = (ArrayList<String>) document.getData().get("authors");
                        String author = authorList.get(0);
                        for (int counter = 1; counter < authorList.size(); counter++) {
                            author = author+'\n' + authorList.get(counter);
                        }

                        //TODO: validate isbn

                        String isbn = String.valueOf(document.getData().get("isbn"));
                        oldISBN = isbn;
                        String title = String.valueOf(document.getData().get("title"));
                        String descr = String.valueOf(document.getData().get("description"));

                        titleView.setText(title);
                        authorView.setText(author);
                        isbnView.setText(isbn);
                        descrView.setText(descr);

                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });

        // save edited info
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newTitle = titleView.getText().toString();
                String newISBN = isbnView.getText().toString();
                String newDescr = descrView.getText().toString();

                // split author text by '\n'
                String newAuthorList = authorView.getText().toString();
                List<String> authorList = Arrays.asList(newAuthorList.split("\n"));

                //replace current info with new one
                Map<String,Object> updates = new HashMap<>();
                updates.put("authors", authorList);
                updates.put("title", newTitle);
                updates.put("description", newDescr);
                updates.put("isbn", newISBN);

                bookRef.update(updates)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("tag", "successfully updated");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("tag", "update failed");
                            }
                        })

                ;



                // copy the content, create a new doc if isbn is changed, delete old one
                // reference: https://stackoverflow.com/questions/47885921/can-i-change-the-name-of-a-document-in-firestore

                final String[] returnBookID = new String[1];
                if (!oldISBN.equals(newISBN)){

                    final boolean[] docFound = {true};
                    final int[] counter = {1};

                    // TODO: generate book id
                    // generate new bookID by checking existing docID
//                    while (docFound[0]) {
//                        String tempID = newBookID + String.valueOf(counter[0]);
//                        DocumentReference checkRef = db.collection("books").document(tempID);
//                        checkRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                if (task.isSuccessful()) {
//                                    DocumentSnapshot document = task.getResult();
//                                    if (document.exists()) {
//                                        Log.d("tag", "document found, try next one");
//                                        counter[0] +=1;
//
//                                    } else {
//                                        Log.d("TAG", "No such document, keep this id");
//                                        docFound[0] = false;
//                                    }
//                                } else {
//                                    Log.d("TAG", "get failed with ", task.getException());
//                                }
//                            }
//                        });
//                    }

                    String newBookID = newISBN + '-' + counter[0];
                    Log.d("tag", "new bookID - "+newBookID);

                    String finalNewBookID = newBookID;


                    bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d("tag","document content copied");

                                    // copy and create new doc, delete the old one
                                    Map content = document.getData();
                                    bookRef.delete();
                                    db.collection("books").document(finalNewBookID).set(content);

                                } else {
                                    Log.d("TAG", "No such document");
                                }
                            } else {
                                Log.d("TAG", "get failed with ", task.getException());
                            }
                        }
                    });


                    // update the user's book list
                    String finalNewBookID1 = newBookID;
                    userRef.update(
                            "books", FieldValue.arrayRemove(bookRef),
                            "books", FieldValue.arrayUnion(
                                    db.collection("books").document(finalNewBookID1)))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("tag","book list updated successfully");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("tag","book list update failed");

                                }
                            });

                    returnBookID[0] = newBookID;

                } else {

                    returnBookID[0] = bookRef.getId();
                }


                Intent returnIntent = new Intent();
                Log.d("return bookid", returnBookID[0]);
                returnIntent.putExtra("returnBookID", returnBookID[0]);
                setResult(RESULT_OK, returnIntent);
                finish();

            }


        });


        Button deleteButton = findViewById(R.id.deleteBook);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: delete book from books collection & the reference in user's books array

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