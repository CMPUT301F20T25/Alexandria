package com.example.alexandria;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Distribution;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;

public class SearchActivity extends BaseActivity {
    //attributes
    private FirebaseFirestore db;
    private ArrayList<ResultModel> resultData = new ArrayList<ResultModel>();

    //layout elements
    private androidx.appcompat.widget.Toolbar toolbar;
    private SearchView searchBar;
    private RecyclerView resultsView;
    private SearchAdapter resultAdapter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setup database
        db = FirebaseFirestore.getInstance();

        //set layout elements
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.search_toolbar);
        searchBar = (SearchView) findViewById(R.id.search_searchView);
        searchBar.setIconifiedByDefault(true);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String q = searchBar.getQuery().toString();
                if (!q.equals("")) {
                    Log.e("SEARCH MADE", q);
                    resultData.clear();
                    loadResults(q);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        searchBar.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                resultData.clear();
                updateResults(new ArrayList<ResultModel>());
                return true;
            }
        });

        //instantiate adapter and layout manager
        resultAdapter = new SearchAdapter(resultData);
        LinearLayoutManager resultsLayoutManager = new LinearLayoutManager(getApplicationContext());

        //instantiate recyclerView
        resultsView = (RecyclerView) findViewById(R.id.search_resultsList);
        //  set divider decoration
        DividerItemDecoration divider = new DividerItemDecoration(resultsView.getContext(), resultsLayoutManager.getOrientation());
        resultsView.addItemDecoration(divider);
        //  set layout manager and adapter
        resultsView.setLayoutManager(resultsLayoutManager);
        resultsView.setAdapter(resultAdapter);
    }

    private void loadResults(@NonNull String keywords) {
        String USER_TYPE = "users";
        String BOOK_TYPE = "books";
        Log.e("LOAD RESULTS", keywords);
        if (!keywords.equals("")) {
            //TODO: make search more robust
            //search on user's username is case sensitive and only matches partial prefix matches
            Query userUsernameQuery = db.collection("users").orderBy("username").startAt(keywords).endAt(keywords + "~");
            runQuery(userUsernameQuery, USER_TYPE);
            //search on book title is case sensitive and only matches partial prefix matches
            Query bookTitleQuery = db.collection("books").orderBy("title").startAt(keywords).endAt(keywords + "~");
            runQuery(bookTitleQuery, BOOK_TYPE);
            //search on author name only matches a full, case-sensitive match of one author
            Query bookAuthorsQuery = db.collection("books").whereArrayContains("authors", keywords);
            runQuery(bookAuthorsQuery, BOOK_TYPE);
        }
    }

    private void runQuery(Query query,@NonNull String modelType) {
        ArrayList<ResultModel> models = new ArrayList<ResultModel>();
        String TAG = "Running query";
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String,Object> data = document.getData();
                                if (modelType == "users" && data.get("bio") != "") {
                                    models.add(new ResultModel.SearchUserItemModel(data.get("username").toString()));
                                } else if (modelType == "users") {
                                    models.add(new ResultModel.SearchUserItemModel(data.get("username").toString(), data.get("bio").toString()));
                                } else if (modelType == "books") {
                                    //Log.e(TAG, data.toString());
                                    ArrayList<String> authors = (ArrayList<String>) data.get("authors");
                                    Map<String,Object> status = (Map<String,Object>) data.get("status");
                                    /*
                                    //TODO: delete if db gets the change I need
                                    final String[] owner = new String[1];
                                    DocumentReference ownerRef = (DocumentReference) data.get("owner");
                                    ownerRef.get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        owner[0] = task.getResult().getData().get("username").toString();
                                                    }
                                                }
                                            });
                                     //use: String.valueof(owner[0])
                                     */
                                    models.add(new ResultModel.SearchBookItemModel(data.get("title").toString(), authors, data.get("owner").toString(), status.get("public").toString()));
                                }
                            }
                            updateResults(models);
                        } else {
                            Log.w(TAG, "Error getting documents", task.getException());
                        }
                    }
                });
    }

    private void updateResults(ArrayList<ResultModel> models) {
        resultData.addAll(models);
        resultAdapter.updateData(resultData);
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