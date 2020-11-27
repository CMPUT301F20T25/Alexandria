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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * Ask user to confirm the photo
 * @author Xueying Luo
 */
public class ConfirmPhotoFragment extends DialogFragment {
    private static final String TAG = "Confirm Photo Fragment";
    private ConfirmPhotoListener listener;
    private Uri uri = EditPhotoOptionFragment.newImageUri;
    private Bitmap bitmap = EditPhotoOptionFragment.newImageBitmap;

    interface ConfirmPhotoListener{
        void updateImage(Uri uri);
        void updateImage(Bitmap bitmap);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_confirm_photo, null);

        ImageView image = view.findViewById(R.id.confirmPhoto);

        if (uri!=null) {
            image.setImageURI(uri);
            Log.d(TAG, "image uri set");
        } else if (bitmap!=null) {
            image.setImageBitmap(bitmap);
            Log.d(TAG, "image bitmap set");
        } else {
            Context context = getContext();
            Toast.makeText(context, "Error: Please try again", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "uri = null & bitmap = null");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        return builder
                .setView(view)
                .setTitle("Confirm Upload")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (uri != null) {
                            listener.updateImage(uri);
                        } else if (bitmap != null) {
                            listener.updateImage(bitmap);
                        }
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
