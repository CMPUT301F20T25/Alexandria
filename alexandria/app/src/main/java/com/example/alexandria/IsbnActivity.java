package com.example.alexandria;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class IsbnActivity extends AppCompatActivity implements View.OnClickListener, IsbnFragment.IsbnFragmentListener {

    private IntentIntegrator scanIntegrator;
    private IntentResult scanResult;
    private String barcode;

    private Toolbar isbnToolbar;
    private TextView titleTextView;
    private TextView authorsTextView;
    private TextView barcodeTextView;
    private TextView descriptionTextView;
    private TextView actionTextView;
    private Button actionButton;
    private Button rescanButton;

    //isbn fragment stuff
    private Fragment isbnFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_isbn);

        //instantiate layout items
        isbnToolbar = (Toolbar) findViewById(R.id.isbn_toolbar);
        titleTextView = (TextView) findViewById(R.id.isbn_titleText);
        authorsTextView = (TextView) findViewById(R.id.isbn_authorsText);
        barcodeTextView = (TextView) findViewById(R.id.isbn_barcodeText);
        descriptionTextView = (TextView) findViewById(R.id.isbn_descriptionText);
        actionTextView = (TextView) findViewById(R.id.isbn_actionText);
        actionButton = (Button) findViewById(R.id.isbn_actionButton);
        rescanButton = (Button) findViewById(R.id.isbn_rescanButton);

        //set toolbar behavior
        setSupportActionBar(isbnToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //set button onClick listeners
        actionButton.setOnClickListener(new OnActionClickListener());
        rescanButton.setOnClickListener(this);

        //perform isbn scan
        scanIsbn();
    }

    private class OnActionClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            //TODO: handle appropriate action with results
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.isbn_rescanButton) {
            scanIsbn();
        }
    }

    public void scanIsbn() {
        //TODO: call isbn scanner fragment
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent); //TODO: why a super call?

        scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanResult != null) {
            String scanContent = scanResult.getContents();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Barcode could be scanned.", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    @Override
    public void onScanDone(Bundle resultBundle) {
        //TODO: terminate isbn scanner fragment
        //TODO: unpack results
    }
}