package com.example.alexandria;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * allows user to add a book
 * @author Xueying Luo
 */
public class AddBookActivity extends AppCompatActivity implements ConfirmPhotoFragment.ConfirmPhotoListener, EditPhotoOptionFragment.deleteImageListener {

    private boolean defaultPhoto = true;
    private String TAG = "add book";
    private DocumentReference userRef = MainActivity.currentUserRef;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        Toolbar toolbar = (Toolbar) findViewById(R.id.addBook_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        ImageView image = findViewById(R.id.addBookImage);
        EditText title = findViewById(R.id.addBookTitle);
        EditText author = findViewById(R.id.addBookAuthor);
        EditText isbn = findViewById(R.id.addBookISBN);
        EditText descr = findViewById(R.id.addBookDescr);

        try {
            Intent intent = getIntent();
            title.setText(intent.getStringExtra("title"));
            author.setText(intent.getStringExtra("authors"));
            isbn.setText(intent.getStringExtra("isbn"));
            descr.setText(intent.getStringExtra("description"));
        } catch (Exception ignored) { }

        // default image - http://www.freepik.com - Designed by stockgiu / Freepik
        Button editPhoto = findViewById(R.id.editPhotoButton);
        editPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPhotoOptionFragment fragment = new EditPhotoOptionFragment();
                // pass data to fragment
                Bundle bundle = new Bundle();
                if (!defaultPhoto) {
                    bundle.putString("adding","new");
                } else {
                    bundle.putString("adding","default");
                }
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), "edit photo option");
                Log.d(TAG,"Edit Photo button clicked");
            }
        });

        Button addButton = findViewById(R.id.addBookButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "add button clicked");

                String newTitle = title.getText().toString();
                String newAuthor = author.getText().toString();
                String newISBN = isbn.getText().toString();
                String newDescr = descr.getText().toString();

                // split author text by '\n'
                List<String> authorList = Arrays.asList(newAuthor.split("\n"));

                // validate input TODO:validator is not working properly
//                BookInformationValidator validator =
//                        new BookInformationValidator(newTitle, newAuthor, newDescr, newISBN);
//                if(!validator.isValid()){ // invalid input
//                    Log.d(TAG, "invalid input");
//                    ArrayList<ValidationError> errors = validator.getError();
//                    for(ValidationError error : errors){
//
//                        if ("isbn".equals(error.getField())) {
//                            isbn.setError(error.getMessage());
//                            Log.d(TAG, "invalid isbn");
//                        } else {
//                            Log.d(TAG, "unknown error");
//
//                            Toast.makeText(AddBookActivity.this,
//                                    "Unknown Error, please try again", Toast.LENGTH_SHORT).show();
//                        }
//                    }

                int validDigit = 0;
                for (char ch : newISBN.toCharArray()){
                    if (Character.isDigit(ch)){
                        validDigit++;
                    }
                }

                if (newAuthor.equals("")  || newTitle.equals("") || validDigit!=13){
                    // invalid input - empty title/author/isbn
                    Context context = getApplicationContext();
                    Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show();
                } else { // valid input

                    Log.d(TAG, "valid input");

                    // generate bookID by checking existing docID

                    final int[] counter = {1};
                    final String[] newBookID = {newISBN + '-' + counter[0]}; // isbn-counter

                    final boolean[] docFound = {true};

                    // store all bookID in a list and check if the list contains the new generated id
                    ArrayList<String> allDocID = new ArrayList<>();
                    CollectionReference collRef = db.collection("books");
                    collRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String id = document.getId();
                                    allDocID.add(id);
                                }

                                Log.d(TAG, "id list: "+allDocID.toString());

                                while (docFound[0]) {
                                    if (allDocID.contains(newBookID[0])) { // if id exists
                                        Log.d(TAG, "document exists - counter = " + counter[0]);
                                        counter[0] += 1;
                                        newBookID[0] = newISBN + '-' + counter[0];
                                    } else { // id does not exist
                                        Log.d(TAG, "No such document, keep this id : " + newBookID[0]);
                                        docFound[0] = false;

                                        DocumentReference bookRef = db.collection("books").document(newBookID[0]);

                                        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot document = task.getResult();

                                                Map<String, String> status = new HashMap<>();
                                                status.put("borrower", null);
                                                status.put("owner", "available");
                                                status.put("public", "available");

                                                Map<String, Object> bookInfo = new HashMap<>();
                                                bookInfo.put("authors", authorList);
                                                bookInfo.put("title", newTitle);
                                                bookInfo.put("description", newDescr);
                                                bookInfo.put("isbn", newISBN);
                                                bookInfo.put("borrower", null);
                                                bookInfo.put("ownerReference", userRef);
                                                bookInfo.put("requestedUsers", null);
                                                bookInfo.put("status", status);

                                                if (defaultPhoto) {
                                                    // use default image
                                                    bookInfo.put("photo", "default");
                                                } else {
                                                    // upload image to storage
                                                    // source: https://firebase.google.com/docs/storage/android/upload-files
                                                    String refText = "images/"+newBookID[0]+".jpg";
                                                    bookInfo.put("photo", refText);

                                                    // Get the data from an ImageView as bytes
                                                    StorageReference imageRef = storageRef.child(refText);

                                                    image.setDrawingCacheEnabled(true);
                                                    image.buildDrawingCache();
                                                    Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                    byte[] data = baos.toByteArray();

                                                    UploadTask uploadTask = imageRef.putBytes(data);
                                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception exception) {
                                                            Context context = getApplicationContext();
                                                            Toast.makeText(context, "image upload failed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                                            Log.d(TAG,"image uploaded successfully");
                                                        }
                                                    });

                                                }
                                                // get username
                                                String username = String.valueOf(document.getData().get("username"));
                                                bookInfo.put("owner", username);

                                                // create a document in books collection
                                                bookRef.set(bookInfo)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "successfully added a book");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG, "adding book failed");
                                                            }
                                                        });

                                                // update the user's book list
                                                userRef.update("books", FieldValue.arrayUnion(bookRef))
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "user's book list updated successfully");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG, "user's book list update failed");

                                                            }
                                                        });
                                            }
                                        });
                                    }
                                }
                            } else {
                                Log.d(TAG, "task failed");
                            }
                        }
                    });
                    finish();

                }


            }

        });

    }

    /**
     * update image view with photo uploaded from gallery
     * @param uri photo uri to be uploaded
     */
    public void updateImage(Uri uri){
        ImageView image = findViewById(R.id.addBookImage);
        image.setImageURI(uri);
        defaultPhoto = false;
        Log.d(TAG, "image view updated");
    }

    /**
     * update image view with photo taken by camera
     * @param bitmap photo bitmap to be uploaded
     */
    public void updateImage(Bitmap bitmap){
        ImageView image = findViewById(R.id.addBookImage);
        image.setImageBitmap(bitmap);
        defaultPhoto = false;
        Log.d(TAG, "image view updated");
    }

    public void deleteImage(){
        ImageView image = findViewById(R.id.addBookImage);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.default_book);
        image.setImageDrawable(drawable);
        defaultPhoto = true;
        Log.d(TAG, "image view set to default");
    }

    @Override
    public void deleteImage(String imagePath) {
        // not to be implemented
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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