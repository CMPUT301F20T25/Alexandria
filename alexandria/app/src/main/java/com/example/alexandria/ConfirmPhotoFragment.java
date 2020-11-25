package com.example.alexandria;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * Ask user to confirm the photo
 * @author Xueying Luo
 */
public class ConfirmPhotoFragment extends DialogFragment {
    private ConfirmPhotoListener listener;
    private Uri uri = EditPhotoOptionFragment.newImage;

    interface ConfirmPhotoListener{
        void updateImage(Uri uri);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_confirm_photo, null);

        ImageView image = view.findViewById(R.id.confirmPhoto);
        image.setImageURI(uri);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        return builder
                .setView(view)
                .setTitle("Confirm Upload")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.updateImage(uri);
                    }
                })
                .create();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (ConfirmPhotoListener) context;
    }

}
