package com.example.alexandria;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Allow users to delete/upload photos
 * @author Xueying Luo
 */

public class EditPhotoOptionFragment extends DialogFragment {

    protected static Uri newImageUri = null;
    protected static Bitmap newImageBitmap = null;
    private static final String TAG = "edit photo fragment";
    private static final int GET_FROM_GALLERY = 1 ;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private deleteImageListener listener;

    interface deleteImageListener{
        void deleteImage();
        void deleteImage(String imagePath);
    }

    FirebaseStorage storage = FirebaseStorage.getInstance();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_edit_photo_option, null);
        Button fromPhone = view.findViewById(R.id.from_phone);
        Button takePhoto = view.findViewById(R.id.take_photo);

        StorageReference storageRef = storage.getReference();

        String photo = "default";
        if (getArguments().getString("adding") != null) {
             photo = getArguments().getString("adding");  // new or default - from AddBookActivity
        } else if (getArguments().getString("editing") != null) {
            photo = getArguments().getString("editing");  // photo ref path or default- from EditBookActivity
        }

        Log.d(TAG,"data passed - "+photo);

        // clear previous data
        newImageBitmap = null;
        newImageUri = null;

        fromPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Upload From Phone button clicked");

                // select photos from phone
                // source: https://stackoverflow.com/questions/9107900/how-to-upload-image-from-gallery-in-android
                startActivityForResult(new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);

            }
        });

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Take Photo button clicked");

                // take photo
                // source: https://developer.android.com/training/camera/photobasics
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                    Context context = getContext();
                    Toast.makeText(context, "Error: Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

        String finalPhoto = photo;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
            .setView(view)
            .setTitle("Edit Photo")
            .setNeutralButton("Delete Photo", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "Delete Photo button clicked");
                    // delete photo, change photo to default

                    if (finalPhoto.equals("new")){
                        listener.deleteImage();
                    } else if (finalPhoto.length()>7) {
                        if(finalPhoto.startsWith("images/")){
                            listener.deleteImage(finalPhoto);
                        };
                    }
                }
            });

        AlertDialog dialog =  builder.create();
        dialog.show();

        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        LinearLayout.LayoutParams neutralButtonLayout = (LinearLayout.LayoutParams) neutralButton.getLayoutParams();
        neutralButtonLayout.gravity = Gravity.CENTER_HORIZONTAL;
        neutralButton.setLayoutParams(neutralButtonLayout);

        // hide delete button if photo is default
        if (photo.equals("default")){
            neutralButton.setVisibility(View.INVISIBLE);
        }

        return dialog;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            newImageUri = data.getData();

            new ConfirmPhotoFragment().show(getActivity().getSupportFragmentManager(), "confirm photo");
            Log.d(TAG,"Photo selected from gallery");
            dismiss();

        } else if (requestCode==REQUEST_IMAGE_CAPTURE && resultCode==Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            newImageBitmap = (Bitmap) extras.get("data");

            new ConfirmPhotoFragment().show(getActivity().getSupportFragmentManager(), "confirm photo");
            Log.d(TAG,"Photo taken from camera");
            dismiss();

            }
        }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (EditPhotoOptionFragment.deleteImageListener) context;
    }

}
