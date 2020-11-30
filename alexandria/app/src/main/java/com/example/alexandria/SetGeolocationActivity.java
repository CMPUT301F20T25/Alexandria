package com.example.alexandria;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class SetGeolocationActivity extends AppCompatActivity {
    private TextView instructionTextView;
    private TextView toolbarTextView;
    private TextView questionTextView;
    private ImageView backImage;
    private MapView mapView;
    private Button confirmButton;
    private Marker marker;
    private GeoPoint markerLocation;
    private Boolean firstClick = true;

    private String borrower;
    private String bookTitle;

    private Intent intent;
    protected final static int RC_REQUEST_SUCCESS = 0;
    protected final static int RC_REQUEST_FAILURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_geolocation);

        intent = getIntent();
        try {
            bookTitle = intent.getStringExtra("title");
            borrower = intent.getStringExtra("borrowerUsername");
        } catch (Exception e) {
            Log.e("SET GEO", "Missing book info");
        }

        //TODO: delete later; only for testing
        bookTitle = "Harry Potter and the Philosopher's Stone";
        borrower = "testUser2";

        //set fields
        instructionTextView = (TextView) findViewById(R.id.setgeo_instructionText);
        instructionTextView.setText("Press and hold to select location.");
        toolbarTextView = (TextView) findViewById(R.id.setgeo_toolbarText);
        toolbarTextView.setText("Set Request Geolocation");
        questionTextView = (TextView) findViewById(R.id.setgeo_questionText);
        questionTextView.setText("Where should @" + borrower + " pick up " + bookTitle + "?");
        backImage = (ImageView) findViewById(R.id.setgeo_backImage);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RC_REQUEST_FAILURE);
                finish();
            }
        });
        confirmButton = (Button) findViewById(R.id.setgeo_confirmButton);
        confirmButton.setText("Set Location");
        confirmButton.setClickable(false);
        confirmButton.setAlpha(0.5f);
        mapView = (MapView) findViewById(R.id.setgeo_mapView);
        marker = new Marker(mapView);

        //setup map
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mapView.setMultiTouchControls(true);

        //copyright overlay
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(this);
        mapView.getOverlays().add(copyrightOverlay);

        //long press overlay
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                updateMarker(p);
                return false;
            }
        };
        mapView.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));
        mapView.invalidate();

        //setup button
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerLocation != null) {
                    writeRequest();
                } else {
                    Toast.makeText(getBaseContext(), "Please select a location!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateMarker(GeoPoint p) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle("Set Marker Location")
                .setMessage("Are you sure you would like to place your marker here? \nLatitude: " + p.getLatitude() + "\nLongitude: " + p.getLongitude())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (firstClick) {
                            firstClick = false;
                            confirmButton.setAlpha(1.0f);
                            confirmButton.setClickable(true);
                            marker = new Marker(mapView);
                            mapView.getOverlays().add(marker);
                        }
                        markerLocation = p;
                        marker.setPosition(p);
                        mapView.invalidate();
                    }
                })
                .setNegativeButton("Cancel", null);
        alertDialogBuilder.show();
    }

    private void writeRequest() {
        //TODO: set location and write accepted request to the database
        setResult(RC_REQUEST_SUCCESS);
        finish();
    }


}