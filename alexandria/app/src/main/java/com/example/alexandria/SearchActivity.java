package com.example.alexandria;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toolbar;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SearchActivity extends BaseActivity {
    //attributes
    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private CollectionReference booksRef;

    //layout elements
    private Toolbar toolbar;
    private SearchView searchBar;
    private RecyclerView resultsList;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set layout elements
        toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        searchBar = (SearchView) findViewById(R.id.search_searchView);
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setIconified(false);
            }
        });
        resultsList = (RecyclerView) findViewById(R.id.search_resultsList);
        //TODO: make layouts for the user and book results
        //TODO: figure out how to hook up search functionality; design and test queries
        //TODO: select result functionality --> redirect to profile/book pages (activities? have to make a public book activity? make a public user activity?)

        //set attribute defaults
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        booksRef = db.collection("books");

    }

    @Override
    int getContentViewId() {
        return R.layout.activity_search;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_search;
    }
}