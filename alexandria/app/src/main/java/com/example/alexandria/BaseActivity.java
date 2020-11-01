package com.example.alexandria;
/** Gives all subclasses a bottom navigation bar with three buttons: "Home", "Search", "My Account".
 * @author Kyla Wong, ktwong@ualberta.ca
 */

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SearchEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
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
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0,0);
    }

    /**
     * Handles the behavior when a button on the bottom navigation bar is selected.
     * @param item The button that was selected.
     * @return A boolean indicating whether the selection was successful (true) or not (false).
     */
    public boolean onNavigationItemSelected(@NonNull MenuItem item) { //remove @NotNull from params
        navigationView.postDelayed(() -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(this, HomeActivity.class));
            } else if (itemId == R.id.navigation_search) {
                startActivity(new Intent(this, SearchActivity.class));
            } else if (itemId == R.id.navigation_user) {
                startActivity(new Intent(this, MyAccountActivity.class));
            }
            finish();
        }, 300);
        return true;
    }

    /**
     * Updates the status of the navigation bar (ie. which button is currently selected).
     */
    private void updateNavigationBarState() {
        int actionId = getNavigationMenuItemId();
        selectNavigationBarItem(actionId);
    }

    /**
     * Sets the given navigation bar button to checked.
     * @param itemId The id of the button selected on the navigation bar
     */
    private void selectNavigationBarItem(int itemId) {
        MenuItem item = navigationView.getMenu().findItem(itemId);
        item.setChecked(true);
    }

    /**
     * Gets the content/layout file id for the activity (ex. R.layout.activity_home)
     * @return the id of the layout file to be used.
     */
    abstract int getContentViewId();

    /**
     * Gets the id of the bottom navigation bar for the current layout (ex. R.id.navigation)
     * @return The id of the bottom navigation bar of the layout.
     */
    abstract int getNavigationMenuItemId();

}
