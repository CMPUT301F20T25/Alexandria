package com.example.alexandria;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.security.interfaces.DSAKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * allows user to edit or delete book
 * @author Xueying Luo
 */
public class EditBookActivity extends AppCompatActivity implements ConfirmPhotoFragment.ConfirmPhotoListener, EditPhotoOptionFragment.deleteImageListener {

    protected static final int RESULT_DELETE = 2;

    private final String[] returnBookID = new String[1];
    private boolean defaultPhoto = true;
    private String oldISBN;
    private DocumentReference userRef = MainActivity.currentUserRef;
    private String currentImage;
    private boolean photoChanged = false;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        Toolbar toolbar = (Toolbar) findViewById(R.id.editBook_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set auto fill texts
        ImageView imageView = findViewById(R.id.editImage);
        EditText titleView = findViewById(R.id.editTitle);
        EditText authorView = findViewById(R.id.editAuthor);
        EditText isbnView = findViewById(R.id.editISBN);
        EditText descrView = findViewById(R.id.editDescr);

        // get intent
        Intent intent = getIntent();
        String bookID = intent.getStringExtra("book");
        returnBookID[0] = bookID;

        db = FirebaseFirestore.getInstance();
        final DocumentReference bookRef = db.collection("books").document(bookID);

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
                        oldISBN = isbn;
                        String title = String.valueOf(document.getData().get("title"));
                        String descr = String.valueOf(document.getData().get("description"));

                        titleView.setText(title);
                        authorView.setText(author);
                        isbnView.setText(isbn);
                        descrView.setText(descr);

                        // retrieve image from storage
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        String imagePath = String.valueOf(document.getData().get("photo"));
                        currentImage = imagePath;
                        if (!imagePath.equals("default")) {
                            defaultPhoto = false;
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
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });

        Button editPhoto = findViewById(R.id.editPhotoButton2);
        editPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPhotoOptionFragment fragment = new EditPhotoOptionFragment();
                Bundle bundle = new Bundle();
                if (!defaultPhoto) {
                    bundle.putString("editing", currentImage);
                } else {
                    bundle.putString("editing", "default");
                }
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), "edit photo option");
                Log.d("edit book", "Edit Photo button clicked");
            }
        });

        // save edited info
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newTitle = titleView.getText().toString();
                String newISBN = isbnView.getText().toString();
                String newDescr = descrView.getText().toString();

                // split author text by '\n'
                String newAuthorList = authorView.getText().toString();
                List<String> authorList = Arrays.asList(newAuthorList.split("\n"));

                // validate isbn
                int validDigit = 0;
                for (char ch : newISBN.toCharArray()) {
                    if (Character.isDigit(ch)) {
                        validDigit++;
                    }
                }

                if (newAuthorList.equals("") || newTitle.equals("") || validDigit != 13) {
                    // invalid input - empty title/author or isbn less than 13 digits
                    Context context = getApplicationContext();
                    Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show();
                } else { // valid input

                    //replace current info with new one

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("authors", authorList);
                    updates.put("title", newTitle);
                    updates.put("description", newDescr);
                    updates.put("isbn", newISBN);

                    String refText = "images/" + bookRef.getId() + ".jpg";

                    if (defaultPhoto) {
                        // use default image
                        updates.put("photo", "default");
                    } else {
                        updates.put("photo", refText);
                    }

                    bookRef.update(updates)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("tag", "successfully updated");

                                    // copy the content, create a new doc if isbn is changed, delete the old one
                                    // reference: https://stackoverflow.com/questions/47885921/can-i-change-the-name-of-a-document-in-firestore

                                    if (!oldISBN.equals(newISBN)) {

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

                                                    Log.d("tag", "id list: " + allDocID.toString());

                                                    while (docFound[0]) {
                                                        if (allDocID.contains(newBookID[0])) { // if id exists
                                                            Log.d("tag", "document exists - counter = " + counter[0]);
                                                            counter[0] += 1;
                                                            newBookID[0] = newISBN + '-' + counter[0];
                                                        } else { // id does not exist
                                                            Log.d("tag", "No such document, keep this id : " + newBookID[0]);
                                                            docFound[0] = false;

                                                            Log.d("tag", "new bookID - " + newBookID[0]);


                                                            bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        DocumentSnapshot document = task.getResult();
                                                                        if (document.exists()) {

                                                                            Log.d("edit book", "default photo status = " + defaultPhoto);

                                                                            if (defaultPhoto) { // if photo is set to default
                                                                                bookRef.update("photo", "default");

                                                                                // delete old photo
                                                                                StorageReference oldRef = FirebaseStorage.getInstance().getReference().child(refText);
                                                                                oldRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Log.d("tag", "Photo deleted successfully");

                                                                                        // copy and create a new doc, delete the old one
                                                                                        Map content = document.getData();
                                                                                        bookRef.delete();
                                                                                        db.collection("books").document(newBookID[0]).set(content);
                                                                                        Log.d("tag", "document content copied");

                                                                                        // update the user's book list
                                                                                        userRef.update(
                                                                                                "books", FieldValue.arrayRemove(bookRef),
                                                                                                "books", FieldValue.arrayUnion(
                                                                                                        db.collection("books").document(newBookID[0])))
                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void aVoid) {
                                                                                                        Log.d("tag", "book list updated successfully");

                                                                                                        returnBookID[0] = newBookID[0];

                                                                                                        Intent returnIntent = new Intent();
                                                                                                        Log.d("return bookid", returnBookID[0]);
                                                                                                        returnIntent.putExtra("returnBookID", returnBookID[0]);
                                                                                                        setResult(RESULT_OK, returnIntent);
                                                                                                        finish();
                                                                                                    }
                                                                                                })
                                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                                    @Override
                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                        Log.d("tag", "book list update failed");

                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception exception) {
                                                                                        Log.d("tag", "Photo deletion failed");
                                                                                    }
                                                                                });


                                                                            } else { // if photo uploaded

                                                                                String newPath = "images/" + newBookID[0] + ".jpg";
                                                                                Log.d("edit book", "new image path = "+newPath);

                                                                                // upload photo to storage
                                                                                StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(newPath);
                                                                                imageView.setDrawingCacheEnabled(true);
                                                                                imageView.buildDrawingCache();
                                                                                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
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
                                                                                        Log.d("TAG", "image uploaded successfully");

                                                                                        // delete old photo
                                                                                        StorageReference oldRef = FirebaseStorage.getInstance().getReference().child(refText);
                                                                                        oldRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                Log.d("tag", "Photo deleted successfully");

                                                                                                // copy and create a new doc, delete the old one
                                                                                                Map content = document.getData();
                                                                                                bookRef.delete();
                                                                                                db.collection("books").document(newBookID[0])
                                                                                                        .set(content)
                                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Void aVoid) {
                                                                                                                // update photo path
                                                                                                                db.collection("books").document(newBookID[0]).update("photo", newPath);
                                                                                                            }
                                                                                                        });
                                                                                                Log.d("tag", "document content copied");

                                                                                                // update the user's book list
                                                                                                userRef.update(
                                                                                                        "books", FieldValue.arrayRemove(bookRef),
                                                                                                        "books", FieldValue.arrayUnion(
                                                                                                                db.collection("books").document(newBookID[0])))
                                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Void aVoid) {
                                                                                                                Log.d("tag", "book list updated successfully");

                                                                                                                returnBookID[0] = newBookID[0];

                                                                                                                Intent returnIntent = new Intent();
                                                                                                                Log.d("return bookid", returnBookID[0]);
                                                                                                                returnIntent.putExtra("returnBookID", returnBookID[0]);
                                                                                                                setResult(RESULT_OK, returnIntent);
                                                                                                                finish();
                                                                                                            }
                                                                                                        })
                                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                                            @Override
                                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                                Log.d("tag", "book list update failed");

                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception exception) {
                                                                                                Log.d("tag", "Photo deletion failed");
                                                                                            }
                                                                                        });


                                                                                    }
                                                                                });

                                                                            }

                                                                        } else {
                                                                            Log.d("TAG", "No such document");
                                                                        }
                                                                    } else {
                                                                        Log.d("TAG", "get failed with ", task.getException());
                                                                    }
                                                                }
                                                            });

                                                        }

                                                    }
                                                }

                                            }
                                        });

                                    } else { // isbn not edited

                                        if (photoChanged) {
                                            // upload image to storage

                                            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(refText);

                                            imageView.setDrawingCacheEnabled(true);
                                            imageView.buildDrawingCache();
                                            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
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
                                                    Log.d("TAG", "image uploaded successfully");

                                                    returnBookID[0] = bookRef.getId();

                                                    Intent returnIntent = new Intent();
                                                    Log.d("return bookid", returnBookID[0]);
                                                    returnIntent.putExtra("returnBookID", returnBookID[0]);
                                                    setResult(RESULT_OK, returnIntent);
                                                    finish();
                                                }
                                            });

                                        } else { // photo not changed

                                            returnBookID[0] = bookRef.getId();

                                            Intent returnIntent = new Intent();
                                            Log.d("return bookid", returnBookID[0]);
                                            returnIntent.putExtra("returnBookID", returnBookID[0]);
                                            setResult(RESULT_OK, returnIntent);
                                            finish();
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Context context = getApplicationContext();
                                    Toast.makeText(context, "update failed", Toast.LENGTH_SHORT).show();
                                }
                            });


                }
            }
        });


        Button deleteButton = findViewById(R.id.deleteBook);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DocumentReference currentBookRef = db.collection("books").document(returnBookID[0]);

                // delete photo from storage
                bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String imagePath = String.valueOf(document.getData().get("photo"));
                                Log.d("delete book", "imagePath = "+imagePath);

                                if (imagePath.equals("default")){
                                    // delete book from collection
                                    currentBookRef.delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("delete book", "book deleted from collection successfully");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d("delete book", "book deletion from collection failed");

                                                }
                                            });

                                    // delete book from user's book list
                                    userRef.update("books", FieldValue.arrayRemove(currentBookRef))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("tag", "book deleted from user's book list successfully");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d("tag", "book deletion from user's book list failed");

                                                }
                                            });

                                    Intent returnIntent = new Intent();
                                    Log.d("edit book", "delete book - " + returnBookID[0]);
                                    returnIntent.putExtra("returnBookID", returnBookID[0]);

                                    setResult(RESULT_DELETE, returnIntent);
                                    finish();

                                } else { // photo is not default
                                    FirebaseStorage.getInstance().getReference().child(imagePath)
                                            .delete()
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d("delete book", "photo deletion failed");
                                                }
                                            })
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("delete book", "photo deleted successfully");


                                                    // delete book from collection
                                                    currentBookRef.delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d("delete book", "book deleted from collection successfully");
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d("delete book", "book deletion from collection failed");

                                                                }
                                                            });

                                                    // delete book from user's book list
                                                    userRef.update("books", FieldValue.arrayRemove(currentBookRef))
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d("tag", "book deleted from user's book list successfully");
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d("tag", "book deletion from user's book list failed");

                                                                }
                                                            });

                                                    Intent returnIntent = new Intent();
                                                    Log.d("edit book", "delete book - " + returnBookID[0]);
                                                    returnIntent.putExtra("returnBookID", returnBookID[0]);

                                                    setResult(RESULT_DELETE, returnIntent);
                                                    finish();
                                                }
                                            });
                                }

                            }
                        }
                    }
                });
            }
        });
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

    /**
     * update image view with photo uploaded from gallery
     * @param uri photo uri to be uploaded
     */
    public void updateImage(Uri uri){
        ImageView image = findViewById(R.id.editImage);
        image.setImageURI(uri);
        defaultPhoto = false;
        photoChanged = true;
        Log.d("TAG", "image view updated");
    }

    /**
     * update image view with photo taken by camera
     * @param bitmap photo bitmap to be uploaded
     */
    public void updateImage(Bitmap bitmap){
        ImageView image = findViewById(R.id.editImage);
        image.setImageBitmap(bitmap);
        defaultPhoto = false;
        photoChanged = true;
        Log.d("TAG", "image view updated");
    }

    @Override
    public void deleteImage() {
        // not to be implemented
    }

    /**
     * set image to default and delete image in storage
     * @param imagePath image reference to delete
     */
    public void deleteImage(String imagePath){
        ImageView image = findViewById(R.id.editImage);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.default_book);

        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("tag", "Photo deleted successfully");

                image.setImageDrawable(drawable);
                defaultPhoto = true;
                photoChanged = false;
                Log.d("TAG", "image view set to default");

                // delete image path in book
                DocumentReference bookRef = db.collection("books").document(returnBookID[0]);
                bookRef.update("photo", "default")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("TAG", "image path set to default");
                                // delete photo in storage
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("TAG", "image path failed to set to default");
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("tag", "Photo deletion failed");
            }
        });




    }

}