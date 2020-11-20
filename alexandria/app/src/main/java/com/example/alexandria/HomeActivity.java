package com.example.alexandria;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Map;

public class HomeActivity extends BaseActivity {

    ListView myBookList;
    ListView borrowedList;
    ListView requestedList;
    ArrayAdapter<Book> myBookAdapter;
    ArrayAdapter<Book> borrowedBookAdapter;
    ArrayAdapter<Book> requestBookAdapter;
    ArrayList<Book> myBookDataList;
    ArrayList<Book> borrowedBookDataList;
    ArrayList<Book> requestBookDataList;
    String userEmail;
    String ownerEmail;
    String borrowerEmail;
    String requestEmail;

    public static final String User_Data = "com.example.alexandria.USER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Button scanButton;
        Button myBookButton;
        Button borrowedButton;
        Button requestedButton;
        FirebaseFirestore db;

        Intent intent = getIntent();
        userEmail = intent.getStringExtra(MainActivity.User_Data);

        final String TAG = "Sample";

        myBookList = findViewById(R.id.myBook_list);
        borrowedList = findViewById(R.id.borrowed_list);
        requestedList = findViewById(R.id.requested_list);
        scanButton = findViewById(R.id.scan_button);
        myBookButton = findViewById(R.id.myBook_button);
        borrowedButton = findViewById(R.id.borrowed_button);
        requestedButton = findViewById(R.id.requested_button);

        myBookDataList = new ArrayList<>();
        borrowedBookDataList = new ArrayList<>();
        requestBookDataList = new ArrayList<>();
        myBookAdapter = new CustomList(this, myBookDataList);
        borrowedBookAdapter = new CustomList(this, borrowedBookDataList);
        requestBookAdapter = new CustomList(this, requestBookDataList);
        myBookList.setAdapter(myBookAdapter);
        borrowedList.setAdapter(borrowedBookAdapter);
        requestedList.setAdapter(requestBookAdapter);

        db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("books");
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable
                    FirebaseFirestoreException error) {
                // Clear the old list
                myBookDataList.clear();
                borrowedBookDataList.clear();
                requestBookDataList.clear();
                for(QueryDocumentSnapshot doc: queryDocumentSnapshots)
                {
                    ArrayList<String> authorList = (ArrayList<String>) doc.getData().get("authors");
                    String author = authorList.get(0);
                    //for (int counter = 1; counter < authorList.size(); counter++) {
                        //author = author+'\n' + authorList.get(counter);
                   // }

                    String id = doc.getId();
                    String isbn = (String) doc.getData().get("isbn");
                    String title = String.valueOf(doc.getData().get("title"));
                    String description = (String) doc.getData().get("description");

                    ownerEmail= (String) doc.getData().get("ownerEmail");
                    borrowerEmail = (String) doc.getData().get("borrowerEmail");

                    if(userEmail.equals(ownerEmail)){
                        myBookDataList.add(new Book(id, isbn, description, title, author)); // Adding the cities and provinces from FireStore
                    }

                    if(userEmail.equals(borrowerEmail)){
                        borrowedBookDataList.add(new Book(id, isbn, description, title, author)); // Adding the cities and provinces from FireStore
                    }

                    ArrayList<String> requestList = (ArrayList<String>) doc.getData().get("requestedUsers");
                    for (int counter = 1; counter < requestList.size(); counter = counter + 2) {
                        requestEmail = requestList.get(counter);
                        if(userEmail.equals(requestEmail)){
                            requestBookDataList.add(new Book(id, isbn, description, title, author)); // Adding the cities and provinces from FireStore
                        }
                    }
                }
                myBookAdapter.notifyDataSetChanged();
                borrowedBookAdapter.notifyDataSetChanged();
                requestBookAdapter.notifyDataSetChanged();
            }
        });

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

        // click to view my book info
        myBookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openBookInfoActivity(i, myBookDataList);
            }
        });

        // click to view borrowed books
        borrowedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBorrowedActivity();
            }
        });

        // click to view borrowed book info
        borrowedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openBorrowedBookInfoActivity(i, borrowedBookDataList);
            }
        });

        // click to view requested books
        requestedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRequestedActivity();
            }
        });

        // click to view requested book info
        requestedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openRequestBookInfoActivity(i, requestBookDataList);
            }
        });
    }

    private void openScanActivity() {
        Intent ISBNIntent = new Intent(this, ISBNActivity.class);
        startActivity(ISBNIntent);
    }

    private void openMyBookActivity() {
        Intent myBookIntent = new Intent(this, MyBookActivity.class);
        myBookIntent.putExtra(User_Data, userEmail);
        startActivity(myBookIntent);
    }


    private void openBorrowedActivity() {
        Intent borrowedIntent = new Intent(this, BorrowedActivity.class);
        borrowedIntent.putExtra(User_Data, userEmail);
        startActivity(borrowedIntent);
    }

    private void openRequestedActivity() {
        Intent requestedIntent = new Intent(this, RequestedActivity.class);
        requestedIntent.putExtra(User_Data, userEmail);
        startActivity(requestedIntent);
    }

    private void openBookInfoActivity(int position, ArrayList<Book> bookDataList) {
        Intent bookInfoIntent = new Intent(HomeActivity.this, BookInfoActivity.class);
        String bookID = bookDataList.get(position).getBookID();
        bookInfoIntent.putExtra("bookID",bookID);
        startActivity(bookInfoIntent);
    }

    private void openBorrowedBookInfoActivity(int position, ArrayList<Book> bookDataList) {
        Intent bookInfoIntent = new Intent(HomeActivity.this, BookInfoActivity.class);
        String bookID = bookDataList.get(position).getBookID();
        bookInfoIntent.putExtra("bookID", bookID);
        startActivity(bookInfoIntent);
    }

    private void openRequestBookInfoActivity(int position, ArrayList<Book> bookDataList) {
        Intent bookInfoIntent = new Intent(HomeActivity.this, BookInfoActivity.class);
        String bookID = bookDataList.get(position).getBookID();
        bookInfoIntent.putExtra("bookID", bookID);
        startActivity(bookInfoIntent);
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

