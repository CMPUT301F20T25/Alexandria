package com.example.alexandria;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.TextView;

import org.osmdroid.util.GeoPoint;

public class GeolocationActivity extends FragmentActivity implements GeolocationFragment.GeolocationFragmentListener {
    private TextView coordinateTextView;
    private Fragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geolocation);

        coordinateTextView = (TextView) findViewById(R.id.geolocation_coordinateText);
        mapFragment = GeolocationFragment.newInstance(true, 0.0, 0.0);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.geolocation_fragment, mapFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void locationSelected(GeoPoint location) {
        String coordinates = String.valueOf(location.getLatitude()) + ", " + String.valueOf(location.getLongitude());
        coordinateTextView.setText(coordinates);
    }
}