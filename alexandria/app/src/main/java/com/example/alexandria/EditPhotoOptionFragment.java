package com.example.alexandria;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

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

    protected static Uri newImage = null;
    private static final String TAG = "edit photo fragment";
    private static final int GET_FROM_GALLERY = 1 ;

    FirebaseStorage storage = FirebaseStorage.getInstance();


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_edit_photo_option, null);
        Button fromPhone = view.findViewById(R.id.from_phone);
        Button takePhoto = view.findViewById(R.id.take_photo);
        Button deletePhoto = view.findViewById(R.id.delete_photo);

        StorageReference storageRef = storage.getReference();

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

                // take photo and upload to database
            }
        });

        deletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Delete Photo button clicked");

                // delete photo from database, change photo to default image
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        return builder
                .setView(view)
                .setTitle("Edit Photo")
                .create();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            newImage = data.getData();

            new ConfirmPhotoFragment().show(getActivity().getSupportFragmentManager(), "confirm photo");
            Log.d(TAG,"Photo selected from gallery");
            dismiss();

        }
    }
}
