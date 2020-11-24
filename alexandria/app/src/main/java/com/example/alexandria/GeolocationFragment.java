package com.example.alexandria;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GeolocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
//TODO: add view only restrictions
//TODO: make an interactive map using OSM-Android library
public class GeolocationFragment extends Fragment implements MapEventsReceiver {
    //map related fields
    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private Marker marker;
    private BoundingBox startBoundingBox;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    // the fragment initialization parameters
    private static final String ARG_VIEW_ONLY = "viewOnly";
    private static final String ARG_LAT = "latitude";
    private static final String ARG_LON = "longitude";

    private static Boolean viewOnly;
    private static GeoPoint markerLocation;

    //TODO: can this be private?
    private GeolocationFragmentListener geolocationCallable;

    //TODO: make single tap work
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        marker.setPosition(p);
        mapView.getOverlays().add(marker);
        geolocationCallable.locationSelected(p);
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }

    public interface GeolocationFragmentListener {
        public void locationSelected(GeoPoint location);
    }

    public GeolocationFragment() {
        // Required empty public constructor
    }

    public static GeolocationFragment newInstance(Boolean viewOnly, Double latitude, Double longitude) {
        GeolocationFragment fragment = new GeolocationFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_VIEW_ONLY, viewOnly);
        args.putDouble(ARG_LAT, latitude);
        args.putDouble(ARG_LON, longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            viewOnly = getArguments().getBoolean(ARG_VIEW_ONLY);
            markerLocation = new GeoPoint(getArguments().getDouble(ARG_LAT), getArguments().getDouble(ARG_LON));
        }
        Context context = getContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        requestPermissionsIfNecessary(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_geolocation, container, false);

        mapView = (MapView) layout.findViewById(R.id.geolocationFrag_mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);

        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        mapView.setMultiTouchControls(true);

        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(getContext());
        mapView.getOverlays().add(copyrightOverlay);

        //TODO: figure out how to simulate GPS location on emulator
        this.myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this.getActivity()), mapView);
        this.myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);

        Log.e("My Location", String.valueOf(myLocationOverlay.getMyLocationProvider().getLastKnownLocation()));

        //TODO: implement starting bounding box
        /*
        startBoundingBox = new BoundingBox(20.0, 20.0, 20.0, 20.0);
        startBoundingBox.bringToBoundingBox(myLocationOverlay.getMyLocation().getLatitude(), myLocationOverlay.getMyLocation().getLongitude());
        mapView.zoomToBoundingBox(startBoundingBox, true);
         */

        this.marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(53.525969, -113.523472));
        mapView.getOverlays().add(marker);
        mapView.invalidate();

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(this.getActivity(), permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String [] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this.getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(this.getActivity(), permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
}