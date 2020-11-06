package com.example.alexandria;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class RequestedBookInfoActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String bookID = null; // passed from previous page
    private DocumentReference bookRef;
    DocumentReference userRef = MainActivity.currentUserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requested_book_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        bookID = intent.getStringExtra("bookID");

        bookRef = db.collection("books").document(bookID);

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
                            author = author + '\n' + authorList.get(counter);
                        }

                        String isbn = String.valueOf(document.getData().get("isbn"));
                        String title = String.valueOf(document.getData().get("title"));
                        String descr = String.valueOf(document.getData().get("description"));

                        DocumentReference ownerRef = (DocumentReference) document.getData().get("ownerReference");
                        DocumentReference borrowerRef = (DocumentReference) document.getData().get("borrower");

                        // display book info

                        ImageView imageView = findViewById(R.id.requestedBookImage);
                        TextView titleView = findViewById(R.id.requestedBookTitle);
                        TextView authorView = findViewById(R.id.requestedBookAuthor);
                        TextView isbnView = findViewById(R.id.requestedBookISBN);
                        TextView descrView = findViewById(R.id.requestedBookDescr);
                        TextView statusView = findViewById(R.id.requestStatusContent);

                        titleView.setText(title);
                        authorView.setText(author);
                        isbnView.setText(isbn);
                        descrView.setText(descr);

                        String requestStatus = null;

                        // if user in requestedUser list, set status to requested
                        ArrayList<DocumentReference> requestedUsers = (ArrayList) document.getData().get("requestedUsers");
                        if (requestedUsers.contains(userRef)) {
                            requestStatus = "requested";
                            statusView.setText(requestStatus);
                        } else if (borrowerRef.equals(userRef)){
                            requestStatus = "accepted";
                            statusView.setText(requestStatus);

                        } else {
                            Log.d("TAG", "user does not request for this book");
                        }

                        Button ownerButton = findViewById(R.id.ownerButton);
                        ownerButton.setText(ownerRef.getId());


                        Button viewLocationButton = findViewById(R.id.viewLocationButton);
                        viewLocationButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });

                        Button confirmButton = findViewById(R.id.confirmButton);
                        confirmButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });


                        // hide button when status != accepted
                        if (!requestStatus.equals("accepted")){
                            viewLocationButton.setVisibility(View.INVISIBLE);
                            confirmButton.setVisibility(View.INVISIBLE);
                        }

                    }
                }
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