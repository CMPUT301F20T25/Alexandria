package com.example.alexandria;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class RequestedActivity extends BaseActivity {

    ListView currentList;
    ArrayAdapter<Book> bookAdapter;
    ArrayList<Book> bookDataList;
    DocumentReference userRef = MainActivity.currentUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseFirestore db;

        // set up toolbar
        // reference: https://developer.android.com/training/appbar/setting-up
        // https://stackoverflow.com/questions/29448116/adding-backbutton-on-top-of-child-element-of-toolbar/29794680#29794680
        Toolbar toolbar = (Toolbar) findViewById(R.id.requested_book_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d("tag", "Requested Book Activity created");
        currentList = findViewById(R.id.current_list);

        bookDataList = new ArrayList<>();
        bookAdapter = new CustomList(this, bookDataList);
        currentList.setAdapter(bookAdapter);

        db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("books");
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable
                    FirebaseFirestoreException error) {
                // Clear the old list
                bookDataList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    ArrayList<String> authorList = (ArrayList<String>) doc.getData().get("authors");
                    String author = authorList.get(0);

                    String id = doc.getId();
                    String isbn = String.valueOf(doc.getData().get("isbn"));
                    String title = String.valueOf(doc.getData().get("title"));
                    String description = String.valueOf(doc.getData().get("description"));

                    if (doc.getData().get("requestedUsers") instanceof ArrayList) {
                        ArrayList<DocumentReference> requestedList = (ArrayList<DocumentReference>) doc.getData().get("requestedUsers");
                        for (int counter = 0; counter < requestedList.size(); counter++) {
                            if (userRef.equals(requestedList.get(counter))) {
                                String bookStatus = "accepted";
                                bookDataList.add(0, new Book(id, isbn, description, title, author, bookStatus)); // Adding the cities and provinces from FireStore
                            }
                        }
                    }
                }
                bookAdapter.notifyDataSetChanged();
            }
        });

        currentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openBookInfoActivity(i);
            }
        });
    }

    private void openBookInfoActivity(int position) {
        Intent bookInfoIntent = new Intent(RequestedActivity.this, RequestedBookInfoActivity.class);
        String bookID = bookDataList.get(position).getBookID();
        bookInfoIntent.putExtra("bookID", bookID);
        startActivity(bookInfoIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                Log.d("toolbar item", "Back button selected");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_requested;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_home;
    }
}

