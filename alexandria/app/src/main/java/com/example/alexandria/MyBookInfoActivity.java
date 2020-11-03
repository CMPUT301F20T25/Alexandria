package com.example.alexandria;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MyBookInfoActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_book_info);

        // set up toolbar
        // reference: https://developer.android.com/training/appbar/setting-up
        // https://stackoverflow.com/questions/29448116/adding-backbutton-on-top-of-child-element-of-toolbar/29794680#29794680
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get intent and book/user passed from previous page



        // book for test
        String user = "user1";
        Book book = new Book("1234567890","book2","jack", "some text",
                "user1", "Available");
        book.setRequestStatus("user2","Available");

        // display book info

        //ImageView image = findViewById(R.id.myBookImage);
        TextView title = findViewById(R.id.myBookTitle);
        TextView author = findViewById(R.id.myBookAuthor);
        TextView isbn = findViewById(R.id.myBookISBN);
        TextView descr = findViewById(R.id.myBookDescr);
        TextView status = findViewById(R.id.myBookStatus);
        TextView borrower = findViewById(R.id.myBookBorrower);
        TextView borrower_title = findViewById(R.id.myBorrower);

        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        isbn.setText(book.getIsbn());
        descr.setText(book.getDescription());
        status.setText(book.getStatus());

        // hide borrower section when book is available
        if (book.getStatus().equals("Borrowed")){
            borrower_title.setVisibility(View.VISIBLE);
            borrower.setVisibility(View.VISIBLE);
            borrower.setText(book.getBorrower());

        } else {
            borrower_title.setVisibility(View.INVISIBLE);
            borrower.setVisibility(View.INVISIBLE);
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editbook,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                Log.d("toolbar item", "Back button selected");
                break;
            case R.id.editBook:
                int EDIT_BOOK_ACTIVITY = 1;
                Intent intent = new Intent(this, EditBookActivity.class);
                startActivityForResult(intent, EDIT_BOOK_ACTIVITY);
                Log.d("toolbar item", "Edit button selected");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}