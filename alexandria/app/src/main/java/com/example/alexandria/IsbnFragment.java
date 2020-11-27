package com.example.alexandria;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import android.provider.ContactsContract;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.inline.InlineContentView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

@androidx.camera.core.ExperimentalGetImage
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class IsbnFragment extends Fragment {
    private IsbnFragmentListener isbnCallback;
    private ImageView backButton;

    private static final String TAG = "IsbnFragment";
    private static final int RC_PERMISSIONS = 1;

    private BarcodeScanner barcodeScanner;
    private ProcessCameraProvider cameraProvider;
    private PreviewView cameraPreviewView;
    private Preview cameraPreview;
    private CameraSelector cameraSelector;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private ImageAnalysis analysisUseCase;

    private static final String ISBN_API_KEY = "AIzaSyB6bLjuktybRey2iJ0SxavVBtkiFNhPiug";

    public interface IsbnFragmentListener {
        public void onScanDone(Bundle resultBundle);
    }

    public static IsbnFragment newInstance() {
        IsbnFragment fragment = new IsbnFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View isbnLayout = inflater.inflate(R.layout.fragment_isbn, container, false);
        backButton = (ImageView) isbnLayout.findViewById(R.id.isbnFragment_backImage);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: delete later; for testing only
                testApiQuery();
                //isbnCallback.onScanDone(null);
            }
        });
        cameraPreviewView = (PreviewView) isbnLayout.findViewById(R.id.isbnFragment_cameraPreview);
        if(isCameraPermissionGranted()) {
            setupCamera();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, RC_PERMISSIONS);
        }
        return isbnLayout;
    }

    private boolean isCameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (requestCode == RC_PERMISSIONS) {
            if (isCameraPermissionGranted()) {
                setupCamera();
            } else {
                Log.e(TAG, "No camera permission granted");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) {
            return;
        }
        if (cameraPreview != null) {
            cameraProvider.unbind(cameraPreview);
        }
        cameraPreview = new Preview.Builder()
                .setTargetRotation(cameraPreviewView.getDisplay().getRotation())
                .build();
        cameraPreview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview);
        } catch (IllegalStateException | IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
        }
        bindAnalysisUseCase();
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        //TODO: problem with analyzer not firing
        analysisUseCase = new ImageAnalysis.Builder().build();
        analysisUseCase.setAnalyzer(ContextCompat.getMainExecutor(getContext()), imageProxy -> {
            processImageProxy(barcodeScanner, imageProxy);
        });
    }

    private class IsbnAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        public void analyze(@NonNull ImageProxy image) {
            processImageProxy(barcodeScanner, image);
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    private void processImageProxy(BarcodeScanner barcodeScanner, ImageProxy imageProxy) {
        Log.e(TAG, "process img proxy called");
        InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        barcodeScanner.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        if (barcodes.isEmpty()) {
                            Log.d(TAG, "No barcode detected");
                        }
                        for (int i = 0; i < barcodes.size(); ++i) {
                            Barcode barcode = barcodes.get(i);
                            //TODO: add graphic overlay
                            if (barcode != null && barcode.getRawValue() != null && !barcode.getRawValue().isEmpty() && barcode.getFormat() == Barcode.FORMAT_EAN_13) {
                                Bundle results = getBookInfo(barcode);
                                isbnCallback.onScanDone(results);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                })
                .addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Barcode>> task) {
                        imageProxy.close();
                    }
                });

    }

    private void setupCamera() {
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
        new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(this, provider -> {
                    cameraProvider = provider;
                    if (isCameraPermissionGranted()) {
                        bindCameraUseCases();
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, RC_PERMISSIONS);
                    }
                });

        barcodeScanner = BarcodeScanning.getClient();
        //TODO: only for testing; remove later
        //testApiQuery();
    }

    private void testApiQuery() {
        String bookSearchString = "https://www.googleapis.com/books/v1/volumes?" + "q=isbn:" + "9781443428453" + "&key=" + ISBN_API_KEY;
        try {
            String res = new GetBookInfo().execute(bookSearchString).get();
            isbnCallback.onScanDone(parseResults(res, "9781443428453"));
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static final class CameraXViewModel extends AndroidViewModel {

        private static final String TAG = "CameraXViewModel";
        private MutableLiveData<ProcessCameraProvider> cameraProviderLiveData;

        //Create an instance which interacts with the camera service via the given application context.

        public CameraXViewModel(@NonNull Application application) {
            super(application);
        }

        public LiveData<ProcessCameraProvider> getProcessCameraProvider() {
            if (cameraProviderLiveData == null) {
                cameraProviderLiveData = new MutableLiveData<>();

                ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                        ProcessCameraProvider.getInstance(getApplication());
                cameraProviderFuture.addListener(
                        () -> {
                            try {
                                cameraProviderLiveData.setValue(cameraProviderFuture.get());
                            } catch (ExecutionException | InterruptedException e) {
                                // Handle any errors (including cancellation) here.
                                Log.e(TAG, "Unhandled exception", e);
                            }
                        },
                        ContextCompat.getMainExecutor(getApplication()));
            }

            return cameraProviderLiveData;
        }
    }

    private Bundle getBookInfo(Barcode barcode) {
        Bundle results = new Bundle();
        String bookSearchString = "https://www.googleapis.com/books/v1/volumes?" + "q=isbn:" + barcode.getRawValue() + "&key=" + ISBN_API_KEY;
        try {
            String res = new GetBookInfo().execute(bookSearchString).get();
            return parseResults(res, barcode.getRawValue());
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    private Bundle parseResults(String result, String isbn) {
        Bundle bookBundle = new Bundle();
        try {
            JSONObject resultObject = new JSONObject(result);
            JSONArray bookArray = resultObject.getJSONArray("items");

            JSONObject bookObject = bookArray.getJSONObject(0);
            JSONObject volumeObject = bookObject.getJSONObject("volumeInfo");

            //get isbn
            try {
                bookBundle.putString("isbn", isbn);
            } catch (Exception e) {
                Log.d("PARSE REQUEST", e.getMessage());
                bookBundle.putString("isbn", "");
            }
            //get title
            try {
                bookBundle.putString("title", volumeObject.getString("title"));
            } catch (JSONException e) {
                Log.d("PARSE REQUEST", e.getMessage());
                bookBundle.putString("title", "");
            }
            //get authors
            try {
                StringBuilder authorBuilder = new StringBuilder("");
                JSONArray authorArray = volumeObject.getJSONArray("authors");
                for (int i = 0; i < authorArray.length(); ++i) {
                    if (i > 0) {
                        authorBuilder.append(", ");
                    }
                    authorBuilder.append(authorArray.getString(i));
                }
                bookBundle.putString("authors", authorBuilder.toString());
            } catch (JSONException e) {
                Log.d("PARSE REQUEST", e.getMessage());
                bookBundle.putString("authors", "");
            }
            //get description
            try {
                bookBundle.putString("description", volumeObject.getString("description"));
            } catch (JSONException e) {
                Log.d("PARSE REQUEST", e.getMessage());
                bookBundle.putString("description", "");
            }
        } catch (Exception e) {
            Log.d("PARSE REQUEST", e.getMessage());
            //TODO: handle when no/bad result returned
            //rescan?
        }
        return bookBundle;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            isbnCallback = (IsbnFragmentListener) context;
        } catch(ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement IsbnFragmentListener");
        }
    }

    private class GetBookInfo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            //get book info
            StringBuilder stringBuilder = new StringBuilder();
            String result = null;
            for (String bookSearchUrl : strings) {
                try {
                    URL url = new URL(bookSearchUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        result = readStream(in);
                        Log.e("HTTP REQUEST: ", String.valueOf(urlConnection.getResponseCode()));
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            return result;
        }

        private String readStream(InputStream is) throws IOException {
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(is),1000);
            for (String line = r.readLine(); line != null; line =r.readLine()){
                sb.append(line);
            }
            is.close();
            return sb.toString();
        }
    }
}