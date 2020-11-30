package com.example.alexandria;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class ViewGeolocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_geolocation);

        MapView mapView;
        TextView toolbarText;
        TextView instructionText;
        ImageView backImage;

        Intent intent;
        String bookTitle;

        GeoPoint markerLocation;

        intent = getIntent();
        try {
            bookTitle = intent.getStringExtra("title");
            Double lat = intent.getDoubleExtra("latitude", 0.0);
            Double lon = intent.getDoubleExtra("longitude", 0.0);
            markerLocation = new GeoPoint(lat, lon);
        } catch (Exception e) {
            Log.e("View Geo", "No title given");
        }

        //TODO: delete; for testing only
        bookTitle = "Harry Potter and the Goblet of Fire";
        markerLocation = new GeoPoint(0.0, 0.0);

        toolbarText = (TextView) findViewById(R.id.viewgeo_toolbarText);
        toolbarText.setText("View Request Location");
        instructionText = (TextView) findViewById(R.id.viewgeo_instructionText);
        instructionText.setText("Pickup " + bookTitle + " at:");
        backImage = (ImageView) findViewById(R.id.viewgeo_backImage);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mapView = (MapView) findViewById(R.id.viewgeo_mapView);

        //setup map
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mapView.setMultiTouchControls(true);

        //copyright overlay
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(this);
        mapView.getOverlays().add(copyrightOverlay);

        //location marker overlay
        Marker marker = new Marker(mapView);
        marker.setPosition(markerLocation);
        mapView.getOverlays().add(marker);
        mapView.invalidate();

    }
}