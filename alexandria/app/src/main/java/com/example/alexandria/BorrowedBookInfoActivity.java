package com.example.alexandria;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Map;

/**
 * display book information to its borrower
 * @author Xueying Luo
 */
public class BorrowedBookInfoActivity extends AppCompatActivity {

    private String bookID = null; // passed from previous page
    private DocumentReference bookRef;
    private String buttonUserId = null; // pass to userInfoActivity

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference userRef = MainActivity.currentUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrowed_book_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.borrowedBook_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        bookID = intent.getStringExtra("bookID");

        bookRef = db.collection("books").document(bookID);

        // make image clickable and zoom image
        ImageView imageView = findViewById(R.id.borrowedBookImage);
        imageView.setClickable(true);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ViewImageFragment fragment = new ViewImageFragment();

                Bundle bundle = new Bundle();

                // get image in bytes
                imageView.setDrawingCacheEnabled(true);
                imageView.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                bundle.putByteArray("image",data);
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), "enlarge image");
            }
        });

        Button ownerButton = findViewById(R.id.ownerButton);
        ownerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInfo();
            }
        });


        Button returnButton = findViewById(R.id.returnBookButton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // set borrower & status to null

                Log.d("accepted", "confirm button clicked");

                bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // set borrower status to 'borrowed'
                                Map<String, String> status = (Map<String, String>) document.getData().get("status");
                                status.put("borrower", null);

                                bookRef.update("status", status);

                                finish();
                            }
                        }
                    }
                });
            }
        });

        bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());

                        // get data from database

                        ArrayList<String> authorList = (ArrayList<String>) document.getData().get("authors");
                        String author = authorList.get(0);
                        for (int counter = 1; counter < authorList.size(); counter++) {
                            author = author + '\n' + authorList.get(counter);
                        }

                        String isbn = String.valueOf(document.getData().get("isbn"));
                        String title = String.valueOf(document.getData().get("title"));
                        String descr = String.valueOf(document.getData().get("description"));

                        DocumentReference ownerRef = (DocumentReference) document.getData().get("ownerReference");

                        // display book info

                        ImageView imageView = findViewById(R.id.borrowedBookImage);
                        TextView titleView = findViewById(R.id.borrowedBookTitle);
                        TextView authorView = findViewById(R.id.borrowedBookAuthor);
                        TextView isbnView = findViewById(R.id.borrowedBookISBN);
                        TextView descrView = findViewById(R.id.borrowedBookDescr);

                        titleView.setText(title);
                        authorView.setText(author);
                        isbnView.setText(isbn);
                        descrView.setText(descr);

                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        String imagePath = String.valueOf(document.getData().get("photo"));
                        if (!imagePath.equals("default")) {
                            storageRef.child(imagePath).getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // Use the bytes to display the image
                                    Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                    imageView.setImageDrawable(image);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Context context = getApplicationContext();
                                    Toast.makeText(context, "Retrieving photo failed ", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        ownerRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot document = task.getResult();
                                String username = String.valueOf(document.getData().get("username"));
                                ownerButton.setText(username);
                            }
                        });

                        buttonUserId = ownerRef.getId();

                    } else {
                        Log.d("TAG", "document not found ");
                    }


                }
            }
        });
    }

    /**
     * go to user info activity
     */
    public void userInfo() {
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra("userId", buttonUserId);
        Log.d("TAG", "passed userId = " +buttonUserId);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}