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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

        //setup database
        db = FirebaseFirestore.getInstance();
        //db settings
        FirebaseFirestoreSettings.Builder dbSettings = new FirebaseFirestoreSettings.Builder();
        dbSettings.setSslEnabled(false);
        usersRef = db.collection("users");
        booksRef = db.collection("books");

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
        ArrayList<ResultModel> resultData = generateTestList();
        SearchAdapter adapter = new SearchAdapter(resultData);
        LinearLayoutManager resultsLayoutManager = new LinearLayoutManager(getApplicationContext());

        //setup recyclerView
        resultsView = (RecyclerView) findViewById(R.id.search_resultsList);
        //  add decoration
        DividerItemDecoration divider = new DividerItemDecoration(resultsView.getContext(), resultsLayoutManager.getOrientation());
        resultsView.addItemDecoration(divider);
        //  set layout manager and adapter
        resultsView.setLayoutManager(resultsLayoutManager);
        resultsView.setAdapter(adapter);

        testDatabase();
    }

    private void testDatabase() {
        String TAG = "TESTING DATABASE";
        // Create a new user with a first and last name
        Map<String, Object> user1 = new HashMap<>();
        user1.put("first", "Ada");
        user1.put("last", "Lovelace");
        user1.put("born", 1815);

        // Add a new document with a generated ID
        db.collection("users")
                .add(user1)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        // Create a new user with a first, middle, and last name
        Map<String, Object> user2 = new HashMap<>();
        user2.put("first", "Alan");
        user2.put("middle", "Mathison");
        user2.put("last", "Turing");
        user2.put("born", 1912);

        // Add a new document with a generated ID
        db.collection("users")
                .add(user2)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        //READING!!!!!!!!!!!!!!!

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.e(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
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

    private ArrayList<ResultModel> getDatabaseResults(@NonNull String keywords) {
        ArrayList<ResultModel> models = new ArrayList<>();
        String TAG = "DB READ";
        Log.e("DB ACCESS", "HELLO I AM BEGINNING NOW");


        /*
        Map<String, Object> testData1 = new HashMap<>();
        testData1.put("name", "Betty");
        testData1.put("email", "betty@fake.com");
        db.collection("test").document("testUser4")
                .set(testData1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e("DB WRITE", "YAY WE ADDED STUFF");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("DB WRITE", "Data couldn't be added", e);
                    }
                });
         */

        if (keywords == "") {
            DocumentReference docRef = db.collection("users").document("testUser1");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }

                }
            });
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