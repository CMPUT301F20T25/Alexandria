package com.example.alexandria;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageButton addButton;
        ImageButton messageButton;
        TextView userName;
        Button myBookButton;
        Button borrowedButton;
        Button requestedButton;
        Button acceptedButton;
        FirebaseAuth mAuth;

        userName = findViewById(R.id.user_name_text);
        addButton = findViewById(R.id.add_button);
        messageButton = findViewById(R.id.message_button);
        myBookButton = findViewById(R.id.open_my_book_button);
        borrowedButton = findViewById(R.id.open_borrowed_book_button);
        requestedButton = findViewById(R.id.open_requested_book_button);
        acceptedButton = findViewById(R.id.open_accepted_book_button);

        mAuth = FirebaseAuth.getInstance ();
        FirebaseUser user = mAuth.getCurrentUser();
        String currentUserEmail = user.getEmail();
        String currentUserName = currentUserEmail.substring(0, currentUserEmail.indexOf("@"));
        userName.setText(currentUserName);

        // click to ISBN scan button
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openAddActivity();
                openScanActivity();
            }
        });

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMessageActivity();
            }
        });

        // click to view my books
        myBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMyBookActivity();
            }
        });


        // click to view borrowed books
        borrowedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBorrowedActivity();
            }
        });

        // click to view requested books
        requestedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRequestedActivity();
            }
        });

        acceptedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAcceptedActivity();
            }
        });

    }

    private void openAddActivity() {
        Intent intent = new Intent(this, AddBookActivity.class);
        startActivity(intent);
    }

    private void openScanActivity() {
        Intent ISBNIntent = new Intent(this, IsbnActivity.class);
        startActivity(ISBNIntent);
    }

    private void openMessageActivity() {
        //Intent intent = new Intent(this, Activity.class);
        //startActivity(intent);
    }

    private void openMyBookActivity() {
        Intent myBookIntent = new Intent(this, MyBookActivity.class);
        startActivity(myBookIntent);
    }

    private void openBorrowedActivity() {
        Intent borrowedIntent = new Intent(this, BorrowedActivity.class);
        startActivity(borrowedIntent);
    }

    private void openRequestedActivity() {
        Intent requestedIntent = new Intent(this, RequestedActivity.class);
        startActivity(requestedIntent);
    }

    private void openAcceptedActivity() {
        Intent requestedIntent = new Intent(this, AcceptedActivity.class);
        startActivity(requestedIntent);
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_home;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_home;
    }

}
