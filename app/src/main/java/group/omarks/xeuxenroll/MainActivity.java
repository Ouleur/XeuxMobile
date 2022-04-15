package group.omarks.xeuxenroll;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Size;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.Result;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {


        Context context;

        private ImageButton qrCodeFoundButton;
        private String qrCode;
        private Boolean condition;

        private static final int PERMISSION_REQUEST_CAMERA = 0;


        private PreviewView previewView;
        private CodeScannerView scannerView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;


        private CodeScanner mCodeScanner;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            context = this;

//            Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));

            checkInternetConenction();
            condition = true;



            qrCodeFoundButton = findViewById(R.id.activity_main_qrCodeFoundButton);
            qrCodeFoundButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(checkInternetConenction()) {
                        mCodeScanner.startPreview();
                        scannerView.setVisibility(View.VISIBLE);
                        qrCodeFoundButton.setVisibility(View.INVISIBLE);
                    }
                }
            });

//            previewView = findViewById(R.id.activity_main_previewView);
//            previewView.setVisibility(View.INVISIBLE);
//            cameraProviderFuture = ProcessCameraProvider.getInstance(this);
//            requestCamera();

            scannerView = findViewById(R.id.scanner_view);
            scannerView.setVisibility(View.INVISIBLE);
            mCodeScanner = new CodeScanner(this, scannerView);
            // Parameters (default values)

            mCodeScanner.setCamera(CodeScanner.CAMERA_BACK); // or CAMERA_FRONT or specific camera id
            mCodeScanner.setFormats(CodeScanner.ALL_FORMATS); // list of type BarcodeFormat,
            // ex. listOf(BarcodeFormat.QR_CODE)
            mCodeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
            // or CONTINUOUS
            mCodeScanner.setScanMode(ScanMode.SINGLE); // or CONTINUOUS or PREVIEW
            mCodeScanner.setAutoFocusEnabled(true);// Whether to enable auto focus or not
            mCodeScanner.setFlashEnabled(false); // Whether to enable flash or not

            mCodeScanner.setDecodeCallback(new DecodeCallback() {
                @Override
                public void onDecoded(@NonNull final Result result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            qrCode = result.getText();
                            if (condition) {
                                Toast.makeText(getApplicationContext(), qrCode, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, Student_info.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                                intent.putExtra("code", qrCode);
                                startActivity(intent);
                                //finish();
                                condition = false;
                            }
                        }
                    });
                }
            });



        }
        @Override
        protected void onResume() {
            super.onResume();
            mCodeScanner.startPreview();
        }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
            // do something here
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkInternetConenction() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec
                =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() ==
                android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
//            Toast.makeText(this, " Connected ", Toast.LENGTH_LONG).show();
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() ==
                                android.net.NetworkInfo.State.DISCONNECTED  ) {
            Toast.makeText(this, " Not Connected ", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }
}




