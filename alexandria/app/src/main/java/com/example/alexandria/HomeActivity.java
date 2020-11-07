package com.example.alexandria;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class HomeActivity extends BaseActivity {

    ListView myBookList;
    ListView borrowedList;
    ListView requestedList;
    ArrayAdapter<Book> bookAdapter;
    ArrayList<Book> bookDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button scanButton;
        Button myBookButton;
        Button borrowedButton;
        Button requestedButton;
        FirebaseFirestore db;

        myBookList = findViewById(R.id.myBook_list);
        borrowedList = findViewById(R.id.borrowed_list);
        requestedList = findViewById(R.id.requested_list);
        scanButton = findViewById(R.id.scan_button);
        myBookButton = findViewById(R.id.myBook_button);
        borrowedButton = findViewById(R.id.borrowed_button);
        requestedButton = findViewById(R.id.requested_button);

        bookDataList = new ArrayList<Book>();
        bookAdapter = new ArrayAdapter<Book>(this, R.layout.content, bookDataList);
        myBookList.setAdapter(bookAdapter);
        db = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = db.collection("Books");

        // click to ISBN scan button
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openScanActivity();
            }
        });

        // click to view my books
        myBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMyBookActivity();
            }
        });

        myBookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        // click to view borrowed books
        borrowedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBorrowedActivity();
            }
        });

        borrowedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        // click to view requested books
        requestedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRequestedActivity();
            }
        });

        requestedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }

    private void openScanActivity() {
        Intent scanIntent = new Intent(this, ScanActivity.class);
        startActivity(scanIntent);
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

    @Override
    int getContentViewId() {
        return R.layout.activity_home;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_home;
    }
    
}
