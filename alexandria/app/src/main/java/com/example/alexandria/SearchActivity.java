package com.example.alexandria;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toolbar;

import com.google.api.Distribution;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.transform.Result;

public class SearchActivity extends BaseActivity {
    //TODO: display a mixed list of books and users
    //TODO: figure out how to hook up search functionality; design and test queries
    //TODO: select result functionality --> redirect to profile/book pages (activities? have to make a public book activity? make a public user activity?)
    //attributes
    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private CollectionReference booksRef;

    //layout elements
    private androidx.appcompat.widget.Toolbar toolbar;
    private SearchView searchBar;
    private RecyclerView resultsView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set layout elements
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.search_toolbar);
        searchBar = (SearchView) findViewById(R.id.search_searchView);
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setIconified(false);
            }
        });

        //create adapter
        SearchAdapter adapter = new SearchAdapter(generateTestList());
        LinearLayoutManager resultsLayoutManager = new LinearLayoutManager(getApplicationContext());

        //setup recyclerView
        resultsView = (RecyclerView) findViewById(R.id.search_resultsList);
        //  add decoration
        DividerItemDecoration divider = new DividerItemDecoration(resultsView.getContext(), resultsLayoutManager.getOrientation());
        resultsView.addItemDecoration(divider);
        //  set layout manager and adapter
        resultsView.setLayoutManager(resultsLayoutManager);
        resultsView.setAdapter(adapter);

        //set attribute defaults
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        booksRef = db.collection("books");

    }

    private ArrayList<ResultModel> generateTestList() {
        ArrayList<ResultModel> models = new ArrayList<>();
        for (int i=0; i < 4; ++i) {
            ArrayList<String> a = new ArrayList<String>(Arrays.asList("author1", "author2"));
            ResultModel.SearchBookItemModel mod = new ResultModel.SearchBookItemModel("title"+i, a, "owner"+i, "available");
            models.add(mod);
        }
        for (int i=0; i < 4; ++i) {
            ResultModel.SearchUserItemModel mod = new ResultModel.SearchUserItemModel("user"+ i, "hello");
            models.add(mod);
        }
        return models;
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