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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
        ImageButton addButton;
        Button myBookButton;
        Button borrowedButton;
        Button requestedButton;
        FirebaseFirestore db;

        final String TAG = "Sample";

        myBookList = findViewById(R.id.myBook_list);
        borrowedList = findViewById(R.id.borrowed_list);
        requestedList = findViewById(R.id.requested_list);
        addButton = findViewById(R.id.add_button);
        myBookButton = findViewById(R.id.myBook_button);
        borrowedButton = findViewById(R.id.borrowed_button);
        requestedButton = findViewById(R.id.requested_button);

        bookDataList = new ArrayList<>();
        bookAdapter = new CustomList(this, bookDataList);
        myBookList.setAdapter(bookAdapter);
        borrowedList.setAdapter(bookAdapter);
        requestedList.setAdapter(bookAdapter);
        db = FirebaseFirestore.getInstance();

        String name = "TestBook1";
        String isbn = "123456";
        String description = "description1";
        String author = "Test Author1";
        String owner = "owner1";
        bookDataList.add(new Book(isbn, description, name, author, owner)); // Adding the cities and provinces from FireStore

        name = "TestBook2";
        isbn = "123456";
        description = "description2";
        author = "Test Author2";
        owner = "owner2";
        bookDataList.add(new Book(isbn, description, name, author, owner)); // Adding the cities and provinces from FireStore

        name = "TestBook3";
        isbn = "123456";
        description = "description3";
        author = "Test Author3";
        owner = "owner3";
        bookDataList.add(new Book(isbn, description, name, author, owner)); // Adding the cities and provinces from FireStore

        name = "TestBook4";
        isbn = "123456";
        description = "description4";
        author = "Test Author4";
        owner = "owner4";
        bookDataList.add(new Book(isbn, description, name, author, owner)); // Adding the cities and provinces from FireStore

        name = "TestBook5";
        isbn = "123456";
        description = "description5";
        author = "Test Author5";
        owner = "owner5";
        bookDataList.add(new Book(isbn, description, name, author, owner)); // Adding the cities and provinces from FireStore


        //CollectionReference collectionReference = db.collection("Books");
        //collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            //@Override
            //public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable
                    //FirebaseFirestoreException error) {
                // Clear the old list
                //bookDataList.clear();
                //for(QueryDocumentSnapshot doc: queryDocumentSnapshots)
                //{
                    //String name = doc.getId();
                    //String isbn = (String) doc.getData().get("isbn");
                    //String description = (String) doc.getData().get("description");
                    //String author = (String) doc.getData().get("author");
                    //String owner = (String) doc.getData().get("owner");
                    //bookDataList.add(new Book(isbn, description, name, author, owner)); // Adding the cities and provinces from FireStore
                //}
                //bookAdapter.notifyDataSetChanged();
            //}
        //});

        // click to ISBN scan button
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openScanActivity();
                testBook4();
            }
        });

        // click to view my books
        myBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openMyBookActivity();
                testBook1();
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
                //openBorrowedActivity();
                testBook2();
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
                //openRequestedActivity();
                testBook3();
            }
        });

        requestedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }

    private void openScanActivity() {
        Intent ISBNIntent = new Intent(this, ISBNActivity.class);
        startActivity(ISBNIntent);
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


    private void testBook1(){
        Intent intent = new Intent(this, BookInfoActivity.class);
        String bookID = "9876543210777-2";
        intent.putExtra("bookID", bookID);
        startActivity(intent);
    }

    private void testBook2(){
        Intent intent = new Intent(this, BorrowedBookInfoActivity.class);
        String bookID = "9876543210111-1";
        intent.putExtra("bookID", bookID);
        startActivity(intent);
    }
    private void testBook3(){
        Intent intent = new Intent(this, RequestedBookInfoActivity.class);
        String bookID = "9876543210777-1";
        intent.putExtra("bookID", bookID);
        startActivity(intent);
    }

    private void testBook4(){
        Intent intent = new Intent(this, AddBookActivity.class);
        startActivity(intent);
    }

}
