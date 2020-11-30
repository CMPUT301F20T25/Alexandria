package com.example.alexandria;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialogFragment;


// reference: https://codinginflow.com/tutorials/android/custom-dialog-interface
public class ChangePasswordDialog extends AppCompatDialogFragment {
    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private DialogListener dialogListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_change_password, null);

        builder.setView(view)
                .setTitle("Change Password")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // cancel
                    }
                })
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // get passwords
                        String currentPassword = currentPasswordEditText.getText().toString();
                        String newPassword = newPasswordEditText.getText().toString();
                        dialogListener.applyTexts(currentPassword, newPassword);
                    }
                });

        currentPasswordEditText = view.findViewById(R.id.currentPassword_EditText);
        newPasswordEditText = view.findViewById(R.id.newPassword_EditText);

        return builder.create();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try {
            dialogListener = (DialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement DialogListener");
        }
    }

    public interface DialogListener{
        void applyTexts(String currentPassword, String newPassword);
    }

}
