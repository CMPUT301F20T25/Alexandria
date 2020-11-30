package com.example.alexandria;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class IsbnActivity extends FragmentActivity implements IsbnFragment.IsbnFragmentListener {

    private Toolbar isbnToolbar;
    private ImageView isbnBackImage;
    private ImageView coverImage;
    private TextView titleTextView;
    private TextView authorsTextView;
    private TextView barcodeTextView;
    private TextView descriptionTextView;
    private TextView actionTextView;
    private Button actionButton;
    private Button rescanButton;

    private Fragment isbnFragment;
    private Bundle results;

    private int action;
    private static final int ACTION_UNSET = 0;
    private static final int ACTION_CONFIRM_BORROW = 1;
    private static final int ACTION_CONFIRM_RETURN = 2;
    private static final int ACTION_LOAN_BOOK = 3;
    private static final int ACTION_RETURN_BOOK = 4;
    private static final int ACTION_ADD_NEW = 5;
    private static final int ACTION_EDIT_BOOK = 6;

    private static final int RC_EDIT_BOOK = 0;
    private static final int RC_ADD_BOOK = 1;

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
        coverImage = (ImageView) findViewById(R.id.isbn_coverImage);
        titleTextView = (TextView) findViewById(R.id.isbn_titleText);
        authorsTextView = (TextView) findViewById(R.id.isbn_authorsText);
        barcodeTextView = (TextView) findViewById(R.id.isbn_barcodeText);
        descriptionTextView = (TextView) findViewById(R.id.isbn_descriptionText);
        actionTextView = (TextView) findViewById(R.id.isbn_actionText);
        actionButton = (Button) findViewById(R.id.isbn_actionButton);
        rescanButton = (Button) findViewById(R.id.isbn_rescanButton);

        //set button onClick listeners
        action = ACTION_UNSET;
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAction();
            }
        });
        rescanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action = ACTION_UNSET;
                scanIsbn();
            }
        });
        isbnBackImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //perform isbn scan
        scanIsbn();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_EDIT_BOOK | requestCode == RC_ADD_BOOK) {
            finish();
        }
    }

    /**
     * Starts the isbn scanner fragment
     */
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
        this.results = resultBundle;

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
                Boolean setImage = false;
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
                            setImage = true;
                            results.putString("bookId", doc.getId());
                            actionButton.setText("Confirm Borrow");
                            actionTextView.setText("Looks like you received a book you requested! Would you like to confirm that you've borrowed it?");
                            action = ACTION_CONFIRM_BORROW;
                        } else if (currentUser.equals(borrower) && statusMap.get("owner") == "borrowed" && statusMap.get("borrower") == "borrowed") {
                            //return the book to the owner as borrower
                            setImage = true;
                            results.putString("bookId", doc.getId());
                            actionButton.setText("Return Book");
                            actionTextView.setText("Would you like to mark this book as returned?");
                            action = ACTION_RETURN_BOOK;
                        } else if (currentUser.equals(owner) && statusMap.get("owner") == "accepted" && statusMap.get("borrower") == "accepted") {
                            //loan the book to borrower as owner
                            setImage = true;
                            results.putString("bookId", doc.getId());
                            actionButton.setText("Loan Book");
                            actionTextView.setText("Would you like to mark this book as loaned?");
                            action = ACTION_LOAN_BOOK;
                        } else if (currentUser.equals(owner) && statusMap.get("owner") == "borrowed" && statusMap.get("borrower") == null) {
                            //confirm you have received the book as owner
                            setImage = true;
                            results.putString("bookId", doc.getId());
                            actionButton.setText("Looks like you received the book you loaned! Would you like to confirm that you received it?");
                            action = ACTION_CONFIRM_RETURN;
                        } else if (currentUser.equals(owner)) {
                            setImage = true;
                            results.putString("bookId", doc.getId());
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
                    if (setImage) {
                        String imagePath = String.valueOf(doc.getData().get("photo"));
                        if (!imagePath.isEmpty()) {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                            storageReference.child(imagePath).getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // Use the bytes to display the image
                                    Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                    coverImage.setImageDrawable(image);
                                }
                            });
                        }
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
                switch(action) {
                    case ACTION_CONFIRM_BORROW | ACTION_CONFIRM_RETURN | ACTION_LOAN_BOOK | ACTION_RETURN_BOOK:
                        changeStatusQuery();
                        break;
                    case ACTION_ADD_NEW:
                        List<String> authorList = Arrays.asList(results.getString("authors").split(", "));

                        Intent addBookIntent = new Intent(getBaseContext(), AddBookActivity.class);
                        addBookIntent.putExtra("title", results.getString("title"));
                        addBookIntent.putExtra("authors", TextUtils.join("\n", authorList));
                        addBookIntent.putExtra("isbn", results.getString("isbn"));
                        addBookIntent.putExtra("description", results.getString("description"));
                        startActivityForResult(addBookIntent, RC_ADD_BOOK);
                        break;
                    case ACTION_EDIT_BOOK:
                        Intent editBookIntent = new Intent(getBaseContext(), EditBookActivity.class);
                        editBookIntent.putExtra("book", results.getString("bookId"));
                        startActivityForResult(editBookIntent, RC_EDIT_BOOK);
                        break;
                    default:
                        break;
                }
            }
        })
                .setNegativeButton("Cancel", null);
        alertDialogueBuilder.show();
    }

    private void changeStatusQuery() {
        //TODO: change status based on action
        DocumentReference bookRef = FirebaseFirestore.getInstance().collection("books").document(results.getString("bookId"));

        String ownerStatus = null;
        String borrowerStatus = null;
        String publicStatus = null;

        switch (action) {
            case ACTION_CONFIRM_BORROW:
                ownerStatus = "borrowed";
                borrowerStatus = "borrowed";
                publicStatus = "unavailable";
                break;
            case ACTION_CONFIRM_RETURN:
                ownerStatus = "available";
                borrowerStatus = null;
                publicStatus = "available";
                break;
            case ACTION_LOAN_BOOK:
                ownerStatus = "borrowed";
                borrowerStatus = "accepted";
                publicStatus = "unavailable";
                break;
            case ACTION_RETURN_BOOK:
                ownerStatus = "borrowed";
                borrowerStatus = null;
                publicStatus = "unavailable";
                break;
            default:
                break;
        }

        //replace current info with new one
        Map<String, String> status = new HashMap<>();
        status.put("owner", ownerStatus);
        status.put("borrower", borrowerStatus);
        status.put("publicStatus", publicStatus);

        Map<String,Object> updates = new HashMap<>();
        updates.put("status", status);
        if (action == ACTION_CONFIRM_RETURN) {
            updates.put("borrower", null);
        }

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
                });
    }
}