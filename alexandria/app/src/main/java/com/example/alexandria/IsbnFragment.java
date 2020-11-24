package com.example.alexandria;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * A simple {@link Fragment} subclass.
 */
public class IsbnFragment extends Fragment {
    private IntentIntegrator scanIntegrator;
    private IntentResult scanResult;
    private static final String RES_BARCODE = "barcode";

    /*
    // TODO: Rename parameter arguments, choose names that match /// delete later
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters /// delete later
    private String mParam1;
    private String mParam2;
     */
    IsbnFragmentListener isbnCallback;

    public interface IsbnFragmentListener {
        public void onScanDone(Bundle resultBundle);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            isbnCallback = (IsbnFragmentListener) context;
        } catch(ClassCastException e) {
            throw new ClassCastException(context.toString() + " mus implement IsbnFragmentListener");
        }
    }

    public IsbnFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static IsbnFragment newInstance() { //String param1, String param2) {
        IsbnFragment fragment = new IsbnFragment();
        //TODO: delete later
        //Bundle args = new Bundle();
        //args.putString(ARG_BARCODE, barcode);
        //args.putString(ARG_PARAM2, param2);
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: delete later
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View isbnLayout = inflater.inflate(R.layout.fragment_isbn, container, false);

        scanIntegrator.initiateScan(); //TODO: figure out where this should properly go

        // Inflate the layout for this fragment
        return isbnLayout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String barcode = scanResult.getContents();
            //bundle results
            Bundle results = getBookInfo(barcode);
            //callback to activity
            isbnCallback.onScanDone(results);
        } else {
            Toast toast = Toast.makeText(getContext(), "Barcode could not be scanned.", Toast.LENGTH_SHORT);
            toast.show();
            //TODO: turn into dialog to retry scan or quit (pass null bundle)
        }
    }

    private Bundle getBookInfo(String barcode) {
        Bundle results = new Bundle();
        //TODO: call via ISBN API to retrieve book info
        return results;
    }

}