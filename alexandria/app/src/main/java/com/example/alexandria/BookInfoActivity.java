package com.example.alexandria;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookInfoActivity extends AppCompatActivity {

    private int EDIT_BOOK_CODE = 1;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String bookID = "1234567890123-testUser1-1"; // passed from previous page
    private DocumentReference bookRef;

    DocumentReference userRef = MainActivity.currentUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);

        // set up toolbar
        // reference: https://developer.android.com/training/appbar/setting-up
        // https://stackoverflow.com/questions/29448116/adding-backbutton-on-top-of-child-element-of-toolbar/29794680#29794680
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        Intent intent = getIntent();
//        bookID = intent.getStringExtra("bookID");
        bookID = "9876543210999-testUser2";

        bookRef = db.collection("books").document(bookID);

        updateView();

    }

    public void updateView(){
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

                        String isbn = String.valueOf(document.getData().get("isbn"));
                        String title = String.valueOf(document.getData().get("title"));
                        String descr = String.valueOf(document.getData().get("description"));

                        DocumentReference ownerRef = (DocumentReference) document.getData().get("ownerReference");
                        DocumentReference borrowerRef = (DocumentReference) document.getData().get("borrower");

                        Map<String, String> statusMap = (Map) document.getData().get("status");
                        String ownerStatus = statusMap.get("owner");
                        String publicStatus = statusMap.get("public");

                        // display book info

                        ImageView imageView = findViewById(R.id.myBookImage);
                        TextView titleView = findViewById(R.id.myBookTitle);
                        TextView authorView = findViewById(R.id.myBookAuthor);
                        TextView isbnView = findViewById(R.id.myBookISBN);
                        TextView descrView = findViewById(R.id.myBookDescr);
                        TextView statusView = findViewById(R.id.myBookStatus);
                        Button borrowerOrOwnerButton = findViewById(R.id.bookBorrowerOrOwner);
                        TextView borrowerOrOwner_titleView = findViewById(R.id.borrowerOrOnwerTitle);

                        titleView.setText(title);
                        authorView.setText(author);
                        isbnView.setText(isbn);
                        descrView.setText(descr);


                        // set owner/borrower button visibility & status

                        if (userRef.equals(ownerRef)) {
                            // for owner -  hide borrower section when book is available

                            statusView.setText(ownerStatus);
                            if (ownerStatus.equals("borrowed") || ownerStatus.equals("accepted")) {

                                borrowerOrOwner_titleView.setVisibility(View.VISIBLE);
                                borrowerOrOwner_titleView.setText("Current Borrower:");
                                borrowerOrOwnerButton.setVisibility(View.VISIBLE);
                                borrowerOrOwnerButton.setText(borrowerRef.getId());

                            } else { // status = Available/Requested
                                borrowerOrOwner_titleView.setVisibility(View.INVISIBLE);
                                borrowerOrOwnerButton.setVisibility(View.INVISIBLE);
                            }

                        } else if (!userRef.equals(ownerRef) && !userRef.equals(borrowerRef)){

                            // for public user - hide borrower, show owner
                            borrowerOrOwnerButton.setVisibility(View.VISIBLE);
                            borrowerOrOwner_titleView.setVisibility(View.VISIBLE);
                            borrowerOrOwner_titleView.setText("Owner:");
                            borrowerOrOwnerButton.setText(ownerRef.getId());

                            statusView.setText(publicStatus);

                        }


                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {

                    DocumentReference ownerRef = (DocumentReference) document.getData().get("ownerReference");

                    // only show editing button when the user owns the book
                    if (userRef.equals(ownerRef)) {
                        getMenuInflater().inflate(R.menu.editbook, menu);
                    }
                } else {
                    Log.d("TAG", "document not found");
                }
            }
        });
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                Log.d("toolbar item", "Back button selected");
                break;
            case R.id.editBook:
                Intent intent = new Intent(this, EditBookActivity.class);
                intent.putExtra("book", bookID );
                startActivityForResult(intent, EDIT_BOOK_CODE);
                Log.d("toolbar item", "Edit button selected");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // when EditBookActivity finished, refresh info
        if (requestCode == EDIT_BOOK_CODE) {
            if (resultCode == RESULT_OK) {
                bookID = data.getStringExtra("returnBookID");
                bookRef = db.collection("books").document(bookID);

                Log.d("Tag", bookID+"..."+bookRef);

                updateView();
            }
        }
    }

}