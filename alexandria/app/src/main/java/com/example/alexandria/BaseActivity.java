package com.example.alexandria;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SearchEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BasicActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    protected BottomNavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        navigationView = (BottomNavigationView) findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
        Log.d("exists", "NAVIGATION FRONT AND PRESENT");
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0,0);
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) { //remove @NotNull from params
        Log.d("nav_triggered", "I'VE BEEN SELECTED, WEEEEEEEEEEEEEEEEEEEE");
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_home) {
            Log.d("nav_home", "CLICKED HOME");
            //startActivity(new Intent(this, HomeActivity.class));
        } else if (itemId == R.id.navigation_search) {
            Log.d("nav_search", "CLICKED SEARCH");
        } else if (itemId == R.id.navigation_user) {
            Log.d("nav_user", "CLICKED USER ACCOUNT");
        }
        finish();
        return true;
    }

    private void updateNavigationBarState() {
        int actionId = getNavigationMenuItemId();
        selectNavigationBarItem(actionId);
    }

    private void selectNavigationBarItem(int itemId) {
        MenuItem item = navigationView.getMenu().findItem(itemId);
        item.setChecked(true);
    }

    abstract int getContentViewId();
    abstract int getNavigationMenuItemId();

}
