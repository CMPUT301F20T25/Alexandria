package com.example.alexandria;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // click to view my books
        Button myBookButton = (Button) findViewById(R.id.myBook_button);
        myBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openMyBookActivity();
                testBook();
            }
        });

        // click to view borrowed books
        Button borrowedButton = (Button) findViewById(R.id.borrowed_button);
        borrowedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openBorrowedActivity();
                testBook1();
            }
        });

        // click to view requested books
        Button requestedButton = (Button) findViewById(R.id.requested_button);
        requestedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openRequestedActivity();
                testBook2();
            }
        });
    }

    private void openMyBookActivity() {
        Intent myBookIntent = new Intent(this, MyBookActivity.class);
        startActivity(myBookIntent);
    }

    private void openBorrowedActivity() {
        Intent borrowedIntent = new Intent(this, BorrowedActivity.class);
        startActivity(borrowedIntent);
    }

    private void openRequestedActivity() {
        Intent requestedIntent = new Intent(this, RequestedActivity.class);
        startActivity(requestedIntent);
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_home;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_home;
    }


    private void testBook() {
        Intent intent = new Intent(this, BookInfoActivity.class);
        startActivity(intent);
    }

    private void testBook1() {
        Intent intent = new Intent(this, BorrowedBookInfoActivity.class);
        startActivity(intent);
    }

    private void testBook2() {
        Intent intent = new Intent(this, RequestedBookInfoActivity.class);
        startActivity(intent);
    }

}
