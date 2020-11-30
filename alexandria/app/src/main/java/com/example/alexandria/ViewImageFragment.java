package com.example.alexandria;

import android.app.Dialog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

/**
 * enlarge image when an image it clicked
 * @author Xueying Luo
 */
public class ViewImageFragment extends DialogFragment {
    private static final String TAG = "View Photo";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_view_image, null);

        Log.d(TAG, "image enlarged");

        ImageView image = view.findViewById(R.id.zoomImage);

        Drawable drawable = ContextCompat.getDrawable(getContext(),R.drawable.default_book);
        image.setImageDrawable(drawable);

        // convert byte array to bitmap and set to image view
        byte[] data = getArguments().getByteArray("image");
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        image.setImageBitmap(Bitmap.createBitmap(bmp));

        image.setClickable(true);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog = builder.setView(view).create();

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;

    }


}