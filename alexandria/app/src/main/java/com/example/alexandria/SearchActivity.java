package com.example.alexandria;
/**
 * Allows a user to search for books and other users using keywords
 * @author Kyla Wong, ktwong@ualberta.ca
 */

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
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

import java.lang.reflect.Array;
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
    private String currentId;
    private String currentUsername;

    //layout elements
    private androidx.appcompat.widget.Toolbar toolbar;
    private SearchView searchBar;
    private RecyclerView resultsView;
    private SearchAdapter resultAdapter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: check later once MainActivity sets currentUserRef
        setCurrentUserInfo();

        //initialize database
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
                    resultData.clear();
                    loadResults(query);
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
        resultAdapter.setOnItemClickListener(new SearchAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v, String info) {
                if (resultAdapter.getItemViewType(position) == R.layout.activity_search_useritem) {
                    //TODO: connect to public user profile page
                    /*
                    Intent intent = new Intent(v.getContext(), UserProfileActivity.class); //change class name to match what gets made
                    intent.putExtra("username", info);
                    startActivity(intent);
                     */
                } else if (resultAdapter.getItemViewType(position) == R.layout.activity_search_bookitem) {
                    Intent intent = new Intent(v.getContext(), BookInfoActivity.class);
                    intent.putExtra("bookID", info);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Queries database for current user's information
     */
    private void setCurrentUserInfo() {
        DocumentReference currentUserRef = MainActivity.currentUserRef;
        currentUserRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            currentId = task.getResult().getId();
                            currentUsername = task.getResult().get("username").toString();
                        }
                    }
                });
    }

    /**
     * Runs queries on the database looking for matches to the keywords pertaining to a username, book title, or book author(s)
     * @param keywords the string to query the database with
     */
    private void loadResults(@NonNull String keywords) {
        String USER_TYPE = "user";
        String BOOK_TYPE = "book";
        if (!keywords.equals("")) {
            //search users
            Query userQuery = db.collection("users");
            runQuery(userQuery, USER_TYPE, keywords);
            //search books
            Query bookQuery = db.collection("books");
            runQuery(bookQuery, BOOK_TYPE, keywords);
        }
    }

    /**
     * Runs the given query on the database
     * @param query the query to be run
     * @param modelType the type of model that should be made with the query results ("users", "books")
     */
    private void runQuery(Query query,@NonNull String modelType, String keywords) {
        ArrayList<ResultModel> models = new ArrayList<ResultModel>();
        String TAG = "Running query";
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String,Object> r = document.getData();
                                r.put("id", document.getId());
                                r.put("resultType", modelType);
                                results.add(r);
                            }
                            results = filterResults(keywords, results);
                            for (Map<String,Object> r : results) {
                                if (r.get("resultType").equals("book")) {
                                    ArrayList<String> authors = (ArrayList<String>) r.get("authors");
                                    Map<String,Object> status = (Map<String,Object>) r.get("status");
                                    models.add(new ResultModel.SearchBookItemModel(r.get("id").toString(), r.get("title").toString(), authors, r.get("owner").toString(), status.get("public").toString()));
                                } else if (r.get("resultType").equals("user")) {
                                    models.add(new ResultModel.SearchUserItemModel(r.get("username").toString(), r.get("bio").toString()));
                                }
                            }
                            updateResults(models);
                        } else {
                            Log.w(TAG, "Error getting documents", task.getException());
                        }
                    }
                });
    }

    /**
     * Updates the recyclerView to display the results
     * @param models the ArrayList of models for the adapter
     */
    private void updateResults(ArrayList<ResultModel> models) {
        resultData.addAll(models);
        resultAdapter.updateData(resultData);
    }

    /**
     * Filters out non-matching results of the database query based on the given keywords
     * @param keywords String of keywords to match
     * @param initResults Array of results to be filtered
     * @return ArrayList<Map<String,Object>> of filtered results
     */
    private ArrayList<Map<String,Object>> filterResults(String keywords, ArrayList<Map<String,Object>> initResults) {
        ArrayList<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
        String keywordRegex = ".*" + keywords.toLowerCase() + ".*";
        //filter the results based on the given keywords
        for (Map<String,Object> r : initResults) {
            if (r.get("resultType").equals("user")) {
                String rId = r.get("id").toString();
                String rUsername = r.get("username").toString().toLowerCase();
                if (!rId.equals(currentId) && rUsername.matches(keywordRegex)) {
                    results.add(r);
                }
            } else if (r.get("resultType").equals("book")) {
                String rTitle = r.get("title").toString().toLowerCase();
                ArrayList<String> authors = (ArrayList<String>) r.get("authors");
                Map<String,Object> status = (Map<String,Object>) r.get("status");
                String rOwner = r.get("owner").toString().toLowerCase();
                String rBorrower = (r.get("borrower") == null) ? "" : r.get("borrower").toString().toLowerCase();
                if (!rBorrower.equals(currentUsername.toLowerCase()) && !rOwner.equals(currentUsername.toLowerCase()) && status.get("public").toString().toLowerCase().equals("available")) {
                    if (rTitle.matches(keywordRegex)) {
                        results.add(r);
                    } else {
                        for (String author : authors) {
                            if (author.toLowerCase().matches(keywordRegex)) {
                                results.add(r);
                            }
                        }
                    }
                }
            }
        }
        return results;
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