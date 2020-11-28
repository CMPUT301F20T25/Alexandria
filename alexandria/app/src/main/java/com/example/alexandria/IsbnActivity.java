package com.example.alexandria;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class IsbnActivity extends FragmentActivity implements View.OnClickListener, IsbnFragment.IsbnFragmentListener {

    private Toolbar isbnToolbar;
    private ImageView isbnBackImage;
    private TextView titleTextView;
    private TextView authorsTextView;
    private TextView barcodeTextView;
    private TextView descriptionTextView;
    private TextView actionTextView;
    private Button actionButton;
    private Button rescanButton;

    private Fragment isbnFragment;

    private int action;
    private static final int ACTION_UNSET = 0;
    private static final int ACTION_CONFIRM_BORROW = 1;
    private static final int ACTION_CONFIRM_RETURN = 2;
    private static final int ACTION_LOAN_BOOK = 3;
    private static final int ACTION_RETURN_BOOK = 4;
    private static final int ACTION_ADD_NEW = 5;
    private static final int ACTION_EDIT_BOOK = 6;

    private static final String TAG = "IsbnActivity";


    @androidx.camera.core.ExperimentalGetImage
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isbn);
        isbnFragment = IsbnFragment.newInstance();

        //instantiate layout items
        isbnToolbar = (Toolbar) findViewById(R.id.isbn_toolbar);
        isbnBackImage = (ImageView) findViewById(R.id.isbn_backImage);
        titleTextView = (TextView) findViewById(R.id.isbn_titleText);
        authorsTextView = (TextView) findViewById(R.id.isbn_authorsText);
        barcodeTextView = (TextView) findViewById(R.id.isbn_barcodeText);
        descriptionTextView = (TextView) findViewById(R.id.isbn_descriptionText);
        actionTextView = (TextView) findViewById(R.id.isbn_actionText);
        actionButton = (Button) findViewById(R.id.isbn_actionButton);
        rescanButton = (Button) findViewById(R.id.isbn_rescanButton);

        //set button onClick listeners
        action = ACTION_UNSET;
        actionButton.setOnClickListener(new OnActionClickListener());
        rescanButton.setOnClickListener(this);
        isbnBackImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //perform isbn scan
        scanIsbn();
    }

    private class OnActionClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            performAction();
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.isbn_rescanButton) {
            action = ACTION_UNSET;
            scanIsbn();
        }
    }

    public void scanIsbn() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.isbn_constraintLayout, isbnFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onScanDone(Bundle resultBundle) {
        if (resultBundle == null) {
            onBackPressed();
            return;
        }
        //set book info fields
        titleTextView.setText(resultBundle.getString("title"));
        authorsTextView.setText(resultBundle.getString("authors"));
        barcodeTextView.setText(resultBundle.getString("isbn"));
        descriptionTextView.setText(resultBundle.getString("description"));

        //make query to figure what action should be for the book
        setAction(resultBundle.getString("isbn"));

        //close fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(isbnFragment);
        fragmentTransaction.commit();
    }

    private void setAction(String isbn) {
        CollectionReference booksRef = FirebaseFirestore.getInstance().collection("books");

        booksRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                DocumentReference currentUser = MainActivity.currentUserRef;
                Boolean ownsBook = false;
                if (value == null) {
                    Log.e(TAG, "query snapshot null");
                    return;
                }
                for (QueryDocumentSnapshot doc : value) {
                    if (doc.getData().get("isbn").toString().equals(isbn)) {
                        //Log.e(TAG, "found book: " + isbn);
                        Map statusMap = (Map) doc.getData().get("status");
                        Object owner = doc.getData().get("ownerReference");
                        Object borrower = doc.getData().get("borrower");
                        //TODO: check the if-statement conditionals
                        if (currentUser.equals(borrower) && statusMap.get("owner") == "borrowed" && statusMap.get("borrower") == "accepted") {
                            //confirm you have received the book as borrower
                            actionButton.setText("Confirm Borrow");
                            actionTextView.setText("Looks like you received a book you requested! Would you like to confirm that you've borrowed it?");
                            action = ACTION_CONFIRM_BORROW;
                        } else if (currentUser.equals(borrower) && statusMap.get("owner") == "borrowed" && statusMap.get("borrower") == "borrowed") {
                            //return the book to the owner as borrower
                            actionButton.setText("Return Book");
                            actionTextView.setText("Would you like to mark this book as returned?");
                            action = ACTION_RETURN_BOOK;
                        } else if (currentUser.equals(owner) && statusMap.get("owner") == "accepted" && statusMap.get("borrower") == "accepted") {
                            //loan the book to borrower as owner
                            actionButton.setText("Loan Book");
                            actionTextView.setText("Would you like to mark this book as loaned?");
                            action = ACTION_LOAN_BOOK;
                        } else if (currentUser.equals(owner) && statusMap.get("owner") == "borrowed" && statusMap.get("borrower") == null) {
                            //confirm you have received the book as owner
                            actionButton.setText("Looks like you received the book you loaned! Would you like to confirm that you received it?");
                            action = ACTION_CONFIRM_RETURN;
                        } else if (currentUser.equals(owner)) {
                            titleTextView.setText(doc.getData().get("title").toString());
                            ArrayList authors = (ArrayList) doc.getData().get("authors");
                            authorsTextView.setText(TextUtils.join(", ", authors));
                            barcodeTextView.setText(doc.getData().get("isbn").toString());
                            descriptionTextView.setText(doc.getData().get("description").toString());
                            actionTextView.setText("Looks like you already own this book! Would you like to edit it's details?");
                            actionButton.setText("Edit Book");
                            action = ACTION_EDIT_BOOK;
                        }
                    }
                    if (action == ACTION_UNSET) {
                        actionTextView.setText("Looks like you've scanned a new book! Would you like to add it to your library?");
                        actionButton.setText("Add New Book");
                        action = ACTION_ADD_NEW;
                    }
                }
            }
        });
    }

    private void performAction() {
        String title = "";
        String question = "";
        switch (action) {
            case 0:
                title = "Action Unset";
                question = "n/a";
                Log.e(TAG, "Not a valid action");
                break;
            case 1:
                title = "Confirm Borrow";
                question = "Are you sure you would like to confirm your borrow?";
                break;
            case 2:
                title = "Confirm Return";
                question = "Are you sure you would like to confirm this book's return?";
                break;
            case 3:
                title = "Loan Book";
                question = "Are you sure you would like to loan this book?";
                break;
            case 4:
                title = "Return Book";
                question = "Are you sure you would like to return this book?";
                break;
            case 5:
                title = "Add New Book";
                question = "Are you sure you would like to add this book?";
                break;
            case 6:
                title = "Edit Book";
                question = "Are you sure you would like to edit this book?";
            default:
                break;
        }

        AlertDialog.Builder alertDialogueBuilder = new AlertDialog.Builder(this);
        alertDialogueBuilder.setTitle(title);
        alertDialogueBuilder.setMessage(question);
        alertDialogueBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: start the appropriate activity
            }
        })
                .setNegativeButton("Cancel", null);
        alertDialogueBuilder.show();
    }
}