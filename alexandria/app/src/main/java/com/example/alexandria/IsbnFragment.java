package com.example.alexandria;
/**
 * This class uses the BarcodeScanner libraries from the Google ML Vision API and the CameraX API to read barcodes
 * from a live camera feed. It also uses the Google Books API to get info about a book based on its isbn.
 * @author Kyla Wong, ktwong@ualberta.ca
 */

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
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
import androidx.camera.core.impl.ImageInfoProcessor;
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
import android.util.Size;
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

import com.google.android.gms.common.util.SharedPreferencesUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
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
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;

import javax.net.ssl.HttpsURLConnection;

@androidx.camera.core.ExperimentalGetImage
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class IsbnFragment extends Fragment {
    private IsbnFragmentListener isbnCallback;
    private ImageView backButton;

    private static final String TAG = "IsbnFragment";
    private static final int RC_PERMISSIONS = 1;

    //private BarcodeScanner barcodeScanner;
    private ProcessCameraProvider cameraProvider;
    private PreviewView cameraPreviewView;
    private Preview cameraPreview;
    private CameraSelector cameraSelector;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private ImageAnalysis analysisUseCase;
    private BarcodeScannerProcessor imageProcessor;

    private static final String ISBN_API_KEY = "AIzaSyB6bLjuktybRey2iJ0SxavVBtkiFNhPiug";

    /**
     * Interface that must be implemented by the activity that calls this fragment
     * in order to receive information from the scanner.
     */
    public interface IsbnFragmentListener {
        void onScanDone(Bundle resultBundle);
    }

    /**
     * Creates and returns a new instance of this fragment.
     * @return The instance of this fragment created.
     */
    public static IsbnFragment newInstance() {
        IsbnFragment fragment = new IsbnFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Creates the fragment's view and sets up the camera used to read barcodes
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View isbnLayout = inflater.inflate(R.layout.fragment_isbn, container, false);
        backButton = (ImageView) isbnLayout.findViewById(R.id.isbnFragment_backImage);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isbnCallback.onScanDone(null);
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

    /**
     * Checks whether the user has granted permission for the camera to be used
     * @return Boolean indicating whether permission has been granted
     */
    private boolean isCameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Sets up the camera if the permissions needed to run the scanner are granted by the user
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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

    /**
     * Binds the live camera view to the lifecycle of the camera provider
     */
    private void bindCameraUseCases() {
        if (cameraProvider == null) {
            return;
        }
        if (cameraPreview != null) {
            cameraProvider.unbind(cameraPreview);
        }
        //Preview.Builder previewBuilder = new Preview.Builder();
        //previewBuilder.setTargetRotation(cameraPreviewView.getDisplay().getRotation());
        cameraPreview = new Preview.Builder().build();
        cameraPreview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview);
        } catch (IllegalStateException | IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
        }
        bindAnalysisUseCase();
    }

    /**
     * Sets up and binds the barcode analyzer to the camera provider lifecycle
     */
    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
        imageProcessor = new BarcodeScannerProcessor(getContext());
        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
        analysisUseCase = builder.build();

        analysisUseCase.setAnalyzer(ContextCompat.getMainExecutor(getContext()), image -> {
            try {
                imageProcessor.processImageProxy(image);
            } catch (Exception e) {
                Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
        cameraProvider.bindToLifecycle(this, cameraSelector, analysisUseCase);
    }

    /**
     * Sets up the camera with a live preview
     */
    private void setupCamera() {
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
        new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(this, provider -> {
                    cameraProvider = provider;
                    if (!isCameraPermissionGranted()) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, RC_PERMISSIONS);
                    }
                    bindCameraUseCases();
                });

        //barcodeScanner = BarcodeScanning.getClient();
    }

    /**
     * The CameraXViewModel used by the camer
     */
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

    /**
     * Makes a call to the the Google Books API to get the info of a book based on the given isbn
     * @param barcode The barcode of the book to lookup
     * @return The results of the API call
     */
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

    /**
     * Parses the results from the API call into a returnable Bundle
     * @param result the results of the API call
     * @param isbn the isbn of the book that the API call was made on
     * @return a Bundle of the parsed results
     */
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

    /**
     * Checks that activity calling the fragment implements ths IsbnFragmentListener interface
     * @param context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            isbnCallback = (IsbnFragmentListener) context;
        } catch(ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement IsbnFragmentListener");
        }
    }

    /**
     * The class used to make the asynchronus call to the Google Books APi
     */
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

    /**
     * The BarcodeScannerProcessor used to process the images provided by the live camera feed
     */
    public class BarcodeScannerProcessor {
        private static final String TAG = "BarcodeProcessor";

        private final BarcodeScanner barcodeScanner;
        Context context;

        private boolean isShutDown;
        private ScopedExecutor executor;

        public BarcodeScannerProcessor(Context context) {
            //TODO: fill out
            this.context = context;
            executor = new ScopedExecutor(TaskExecutors.MAIN_THREAD);
            barcodeScanner = BarcodeScanning.getClient();
        }

        //public void ProcessBitmap();

        protected Task<List<Barcode>> detectInImage(InputImage image) {
            return barcodeScanner.process(image);
        }

        protected void onSuccess(@NonNull List<Barcode> barcodes) { //TODO: add graphic overlay
            if (barcodes.isEmpty()) {
                Log.d(TAG, "No barcodes detected");
            }
            for (int i=0; i < barcodes.size(); ++i) {
                Log.e(TAG, "barcode detected!");
                Barcode barcode = barcodes.get(i);
                Log.e(TAG, barcode.getRawValue());
                Bundle results = getBookInfo(barcode);
                isbnCallback.onScanDone(results);
                //graphicOverlay.add(new BarcodeGraphic(graphicOverlay, barcode));
            }
        }

        protected void onFailure(Exception e) {
            Log.e(TAG, "Barcode detection failed " + e);
        }

        public void stop() {
            //TODO: check #2
            executor.shutdown();
            isShutDown = true;
            barcodeScanner.close();
        }

        @androidx.camera.core.ExperimentalGetImage
        public void processImageProxy(ImageProxy imageProxy) { //TODO: add graphic overlay
            if (isShutDown) {
                imageProxy.close();
                return;
            }

            //TODO: bitmap stuff for graphics (VisionProcessorBase 168)
            if (imageProxy.getImage() != null) {
                requestDetectInImage(InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees()), null, false)
                        .addOnCompleteListener(results -> imageProxy.close());
            } else {
                imageProxy.close();
            }
        }

        private Task<List<Barcode>> requestDetectInImage(InputImage inputImage, @Nullable final Bitmap originalCameraImage, boolean shouldShowFps) { //TODO: add graphic overlay
            return detectInImage(inputImage)
                    .addOnSuccessListener(executor, results -> {
                        //TODO: overlay graphics
                        onSuccess(results);
                    })
                    .addOnFailureListener(executor, e -> {
                        //TODO: clear graphic overlay
                        String error = "Failed to process. Error: " + e.getLocalizedMessage();
                        Toast.makeText(context, error + "\nCause: " + e.getCause(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, error);
                        e.printStackTrace();
                        onFailure(e);
                    });

        }
        private class ScopedExecutor implements Executor {

            private final Executor executor;
            private final AtomicBoolean shutdown = new AtomicBoolean();

            public ScopedExecutor(@NonNull Executor executor) {
                this.executor = executor;
            }

            @Override
            public void execute(@NonNull Runnable command) {
                // Return early if this object has been shut down.
                if (shutdown.get()) {
                    return;
                }
                executor.execute(
                        () -> {
                            // Check again in case it has been shut down in the mean time.
                            if (shutdown.get()) {
                                return;
                            }
                            command.run();
                        });
            }

            public void shutdown() {
                shutdown.set(true);
            }
        }
    }

}