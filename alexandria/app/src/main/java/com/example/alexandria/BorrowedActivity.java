package com.example.alexandria;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class BorrowedActivity extends BaseActivity {

    ListView currentList;
    ArrayAdapter<Book> bookAdapter;
    ArrayList<Book> bookDataList;
    String userEmail;
    String borrowerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrowed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.borrowed_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseFirestore db;

        Intent intent = getIntent();
        userEmail = intent.getStringExtra(HomeActivity.User_Data);

        final String TAG = "Sample";

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
                for(QueryDocumentSnapshot doc: queryDocumentSnapshots)
                {
                    ArrayList<String> authorList = (ArrayList<String>) doc.getData().get("authors");
                    String author = authorList.get(0);

                    String id = doc.getId();
                    String isbn = (String) doc.getData().get("isbn");
                    String title = String.valueOf(doc.getData().get("title"));
                    String description = (String) doc.getData().get("description");

                    borrowerEmail= (String) doc.getData().get("borrowerEmail");

                    if(userEmail.equals(borrowerEmail)){
                        bookDataList.add(new Book(id, isbn, description, title, author)); // Adding the cities and provinces from FireStore
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
        Intent bookInfoIntent = new Intent(BorrowedActivity.this, BorrowedBookInfoActivity.class);
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
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
}