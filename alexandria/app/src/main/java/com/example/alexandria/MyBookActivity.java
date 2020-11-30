package com.example.alexandria;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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

public class MyBookActivity extends BaseActivity {
    String filterStatus = "all";
    ListView currentList;
    ArrayAdapter<Book> bookAdapter;
    ArrayList<Book> bookDataList;
    ArrayList<Book> bookShowDataList;
    FirebaseFirestore db;
    DocumentReference userRef = MainActivity.currentUserRef;

    private static final int ADD_BOOK_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up toolbar
        // reference: https://developer.android.com/training/appbar/setting-up
        // https://stackoverflow.com/questions/29448116/adding-backbutton-on-top-of-child-element-of-toolbar/29794680#29794680
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_book_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d("tag", "My Book Activity created");

        currentList = findViewById(R.id.current_list);

        bookDataList = new ArrayList<>();
        bookShowDataList = new ArrayList<>();
        bookAdapter = new CustomList(this, bookShowDataList);
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

                    DocumentReference ownerRef = (DocumentReference) doc.getData().get("ownerReference");

                    Map<String, String> statusMap = (Map) doc.getData().get("status");
                    String ownerStatus = statusMap.get("owner");
                    String publicStatus = statusMap.get("public");

                    String bookStatus = "borrowed";
                    if(publicStatus.equals("available")){
                        if (doc.getData().get("requestedUsers") instanceof ArrayList) {
                            bookStatus = "requested";
                        }
                        else{
                            bookStatus = "available";
                        }
                    }
                    else{
                        if(ownerStatus.equals("accepted")){
                            bookStatus = "accepted";
                        }
                    }

                    if (userRef.equals(ownerRef)) {
                        bookDataList.add(new Book(id, isbn, description, title, author, bookStatus)); // Adding the cities and provinces from FireStore
                    }
                }
                bookShowDataList.clear();
                for (int index = 0; index < bookDataList.size(); index++){
                    bookShowDataList.add(0,bookDataList.get(index));
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
        Intent bookInfoIntent = new Intent(MyBookActivity.this, BookInfoActivity.class);
        String bookID = bookShowDataList.get(position).getBookID();
        bookInfoIntent.putExtra("bookID", bookID);
        startActivity(bookInfoIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mybook, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                Log.d("toolbar item", "Back button selected");
                break;
            case R.id.addBook:
                Intent intent = new Intent(this, AddBookActivity.class);
                startActivityForResult(intent, ADD_BOOK_CODE);
                Log.d("toolbar item", "add button selected");
                break;
            case R.id.show_all:
                filterStatus = "all";
                bookShowDataList.clear();
                for (int index = 0; index < bookDataList.size(); index++){
                    bookShowDataList.add(0,bookDataList.get(index));
                }
                bookAdapter.notifyDataSetChanged();
                Log.d("toolbar item", "all button selected");
                break;
            case R.id.show_available:
                filterStatus = "available";
                bookShowDataList.clear();
                for (int index = 0; index < bookDataList.size(); index++){
                    if(bookDataList.get(index).getBookStatus().equals(filterStatus)){
                        bookShowDataList.add(0,bookDataList.get(index));
                    }
                }
                bookAdapter.notifyDataSetChanged();
                Log.d("toolbar item", "available button selected");
                break;
            case R.id.show_requested:
                filterStatus = "requested";
                bookShowDataList.clear();
                for (int index = 0; index < bookDataList.size(); index++){
                    if(bookDataList.get(index).getBookStatus().equals(filterStatus)){
                        bookShowDataList.add(0,bookDataList.get(index));
                    }
                }
                bookAdapter.notifyDataSetChanged();
                Log.d("toolbar item", "requested button selected");
                break;
            case R.id.show_accepted:
                filterStatus = "accepted";
                bookShowDataList.clear();
                for (int index = 0; index < bookDataList.size(); index++){
                    if(bookDataList.get(index).getBookStatus().equals(filterStatus)){
                        bookShowDataList.add(0,bookDataList.get(index));
                    }
                }
                bookAdapter.notifyDataSetChanged();
                Log.d("toolbar item", "accepted button selected");
                break;
            case R.id.show_borrowed:
                filterStatus = "borrowed";
                bookShowDataList.clear();
                for (int index = 0; index < bookDataList.size(); index++){
                    if(bookDataList.get(index).getBookStatus().equals(filterStatus)){
                        bookShowDataList.add(0,bookDataList.get(index));
                    }
                }
                bookAdapter.notifyDataSetChanged();
                Log.d("toolbar item", "borrowed button selected");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_my_book;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_home;
    }
}
