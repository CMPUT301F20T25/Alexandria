package com.example.alexandria;
/**
 * Allows users to set the Geolocation of a request and writes the results to the database if the
 * location is set and confirmed.
 * @author Kyla Wong, ktwong@ualberta.ca
 */

import androidx.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private String bookId;
    private String borrowerId;

    private Intent intent;
    protected final static int RC_REQUEST_SUCCESS = 0;
    protected final static int RC_REQUEST_FAILURE = 1;
    protected final static int RC_REQUEST_NOTIFY_FAILURE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_geolocation);

        intent = getIntent();
        try {
            bookId = intent.getStringExtra("bookId");
            borrowerId = intent.getStringExtra("borrowerId");
            bookTitle = intent.getStringExtra("title");
            borrower = intent.getStringExtra("borrowerUsername");
        } catch (Exception e) {
            Log.e("SET GEO", "Missing book info");
        }

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

    /**
     * Updates the position of the marker upon user confirmation
     * @param p the GeoPoint to update the mark to
     */
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

    /**
     * Writes the confirmed book request to the database
     */
    private void writeRequest() {
        DocumentReference bookDocument = FirebaseFirestore.getInstance().collection("books").document(bookId);

        Map<String, String> status = new HashMap<>();
        //TODO: check status values
        status.put("borrower", "accepted");
        status.put("owner", "accepted");
        status.put("public", "unavailable");

        Map<String, Double> location = new HashMap<>();
        location.put("latitude", markerLocation.getLatitude());
        location.put("longitude", markerLocation.getLongitude());

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("location", location);
        DocumentReference borrowerRef = FirebaseFirestore.getInstance().collection("users").document(borrowerId);
        updates.put("borrower", borrowerRef);

        bookDocument.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        bookDocument.get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        ArrayList<DocumentReference> requests = (ArrayList<DocumentReference>) documentSnapshot.getData().get("requestedUsers");
                                        Log.e("requested Users", requests.toString());
                                        Map<String, Object> updateRequests = new HashMap<>();
                                        updateRequests.put("requestedUsers", null);
                                        bookDocument.update(updateRequests);

                                        notifyUsers(requests, documentSnapshot.getData().get("ownerReference"), documentSnapshot.getData().get("owner").toString());
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        setResult(RC_REQUEST_FAILURE);
                        finish();
                    }
                });
    }

    /**
     * Writes the notifications for the user requests accepted and denied to the database
     * @param requests ArrayList of document references of users who requested the book
     * @param ownerReference document reference of the owner of the book
     * @param ownerUsername String of the book's owner's username
     */
    private void notifyUsers(@NonNull ArrayList<DocumentReference> requests, Object ownerReference, String ownerUsername) {
        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("users");
        String TAG = "notify";
        usersRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot user : queryDocumentSnapshots) {
                            for (DocumentReference userRef : requests) {
                                Log.e(TAG, userRef.getId().toString());
                                Log.e(TAG, user.getId().toString());
                                Log.e(TAG, userRef.getId());
                                Log.e(TAG, borrowerId);
                                if (userRef.getId().equals(user.getId()) && userRef.getId().equals(borrowerId)) {
                                    Map<String,String> accepted = new HashMap<>();
                                    accepted.put("bookId", bookId);
                                    accepted.put("bookTitle", bookTitle);
                                    accepted.put("ownerId", ((DocumentReference) ownerReference).getId());
                                    accepted.put("ownerUsername", ownerUsername);

                                    Map<String,ArrayList<Map<String,String>>> notifications = (Map<String, ArrayList<Map<String, String>>>) user.getData().get("notifications");
                                    ((ArrayList<Map<String,String>>) notifications.get("accepted")).add(accepted);

                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("notifications", notifications);
                                    userRef.update(updates);
                                    break;
                                } else if (userRef.getId().equals(user.getId())) {
                                    Map<String,String> denied = new HashMap<>();
                                    denied.put("bookId", bookId);
                                    denied.put("bookTitle", bookTitle);
                                    denied.put("ownerId", ((DocumentReference) ownerReference).getId());
                                    denied.put("ownerUsername", ownerUsername);

                                    Map<String,ArrayList<Map<String,String>>> notifications = (Map<String, ArrayList<Map<String, String>>>) user.getData().get("notifications");
                                    ((ArrayList<Map<String,String>>) notifications.get("denied")).add(denied);

                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("notifications", notifications);
                                    userRef.update(updates);
                                    break;
                                }
                            }
                        }
                        setResult(RC_REQUEST_SUCCESS);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Set Geo", "Failed to notify users");
                        setResult(RC_REQUEST_NOTIFY_FAILURE);
                        finish();
                    }
                });
    }
}