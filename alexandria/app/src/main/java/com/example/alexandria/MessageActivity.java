package com.example.alexandria;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {

    private ImageView backImage;
    private RecyclerView notifListRecyclerView;
    private NotificationAdapter notifListAdapter;
    private ArrayList<NotificationModel> notifData = new ArrayList<NotificationModel>();

    private DocumentReference currUserRef = MainActivity.currentUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //set layout items
        backImage = (ImageView) findViewById(R.id.message_backImage);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        notifListRecyclerView = (RecyclerView) findViewById(R.id.message_notifList);

        //setup recycler adapter
        notifListAdapter = new NotificationAdapter(notifData, this);
        LinearLayoutManager notifLayoutManager = new LinearLayoutManager(getApplicationContext());

        //setup recycler list
        DividerItemDecoration divider = new DividerItemDecoration(notifListRecyclerView.getContext(), notifLayoutManager.getOrientation());
        notifListRecyclerView.addItemDecoration(divider);
        notifListRecyclerView.setLayoutManager(notifLayoutManager);
        notifListRecyclerView.setAdapter(notifListAdapter);

        //set OnClickListener
        notifListAdapter.setOnItemClickListener(new NotificationAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v, String message, int messageType, String bookId) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MessageActivity.this);
                alertDialogBuilder.setTitle("Notification");
                alertDialogBuilder.setMessage(message);
                alertDialogBuilder
                        .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteNotification(messageType, bookId);
                            }
                        })
                        .setNegativeButton("Keep", null);
                alertDialogBuilder.show();
            }
        });
        loadNotifications();
    }

    private void deleteNotification(int type, String bookId) {
        currUserRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String,ArrayList<Map<String,String>>> notifications = (Map<String, ArrayList<Map<String, String>>>) documentSnapshot.getData().get("notifications");
                        if (notifications != null) {
                            try {
                                if (notifications.get("accepted") != null) {
                                    String notifArrayName = "";
                                    if (type == NotificationModel.MSG_ACCEPTED) {
                                        notifArrayName = "accepted";
                                    } else if (type == NotificationModel.MSG_DENIED) {
                                        notifArrayName = "denied";
                                    } else if (type == NotificationModel.MSG_RECEIVED) {
                                        notifArrayName = "received";
                                    }
                                    for (Map<String, String> notice : (ArrayList<Map<String, String>>) notifications.get(notifArrayName)) {
                                        Log.e("Del Notif", notice.toString());
                                        if (notice.get("bookId").equals(bookId)) {
                                            ArrayList<Map<String, String>> newNotifArray = (ArrayList<Map<String, String>>) notifications.get(notifArrayName);
                                            newNotifArray.remove(notice);
                                            notifications.put(notifArrayName, newNotifArray);
                                            currUserRef.update("notifications", notifications);
                                        }
                                    }
                                }
                            } catch (Exception ignore) {}
                        }
                        loadNotifications();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Delete Notif", "Error deleting the notification: " + bookId);
                    }
                });
    }

    private void loadNotifications() {
        ArrayList<NotificationModel> models = new ArrayList<NotificationModel>();
        currUserRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String,ArrayList<Map<String,String>>> notifications = (Map<String, ArrayList<Map<String, String>>>) documentSnapshot.getData().get("notifications");
                        if (notifications != null) {
                            //add accepted
                            try {
                                if (notifications.get("accepted") != null) {
                                    for (Map<String, String> notice : (ArrayList<Map<String, String>>) notifications.get("accepted")) {
                                        Log.e("ACC_NOTICE", notice.toString());
                                        models.add(new NotificationModel(NotificationModel.MSG_ACCEPTED, notice.get("bookId"), notice.get("bookTitle"), notice.get("ownerUsername")));
                                    }
                                }
                                //add denied
                                if (notifications.get("denied") != null) {
                                    for (Map<String, String> notice : (ArrayList<Map<String, String>>) notifications.get("denied")) {
                                        Log.e("DEN_NOTICE", notice.toString());
                                        models.add(new NotificationModel(NotificationModel.MSG_DENIED, notice.get("bookId"), notice.get("bookTitle"), notice.get("ownerUsername")));
                                    }
                                }
                                //add received
                                if (notifications.get("received") != null) {
                                    for (Map<String, String> notice : (ArrayList<Map<String, String>>) notifications.get("received")) {
                                        Log.e("REC_NOTICE", notice.toString());
                                        models.add(new NotificationModel(NotificationModel.MSG_RECEIVED, notice.get("bookId"), notice.get("bookTitle"), notice.get("borrowerUsername")));
                                    }
                                }
                                updateData(models);
                            } catch (Exception ignore) {}
                        } else {
                            //TODO: write message telling using that there are no notifications
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Message Activity", "Error retrieving user's notifications");
                    }
                });
    }

    private void updateData(ArrayList<NotificationModel> models) {
        this.notifData.clear();
        this.notifData.addAll(models);
        notifListAdapter.updateData(notifData);
    }

    public class NotificationModel {
        private String message;
        private String bookId;
        private int messageType;

        private static final int MSG_ACCEPTED = 0;
        private static final int MSG_DENIED = 1;
        private static final int MSG_RECEIVED = 2;

        public NotificationModel(@NonNull int messageType, @NonNull String bookId, @NonNull String bookTitle, @NonNull String personUsername) {
            this.messageType = messageType;
            if (messageType == MSG_ACCEPTED) {
                message = "@" + personUsername + " has accepted your request for " + bookTitle + "!";
            } else if (messageType == MSG_DENIED) {
                message = "@" + personUsername + " has denied your request for " + bookTitle + ".";
            } else if (messageType == MSG_RECEIVED) {
                message = "@" + personUsername + " has made a request to borrow your copy of " + bookTitle + ".";
            } else {
                throw new IllegalArgumentException("invalid messageType given");
            }
            this.bookId = bookId;
        }

        public String getMessage() {
            return this.message;
        }

        public String getBookId() {
            return this.bookId;
        }

        public int getMessageType() {
            return this.messageType;
        }
    }
}