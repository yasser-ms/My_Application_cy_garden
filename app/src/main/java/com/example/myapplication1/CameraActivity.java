package com.example.myapplication1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.example.myapplication1.database.DatabaseHelper;
import com.example.myapplication1.models.Photo;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends BaseActivity {

    private static final String TAG = "CameraActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    private TextureView textureView;
    private Button btnCapture, btnSave, btnCancel;
    private EditText inputNotes;
    private ImageView imgPreview;
    private TextView txtParcelId;

    private String parcelId;
    private String userEmail;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewBuilder;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;

    private String capturedImagePath;
    private boolean isTextureAvailable = false;
    private boolean isCameraOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        parcelId = getIntent().getStringExtra("parcel_id");
        if (parcelId == null) {
            parcelId = "B4";
        }

        userEmail = getSharedPreferences("GardenApp", MODE_PRIVATE).getString("user_email", "");

        initializeViews();
        setupClickListeners();
        setupTextureListener();
        setupImageReader(); // Create ImageReader upfront otherwise error xD
    }

    private void initializeViews() {
        textureView = findViewById(R.id.textureView);
        btnCapture = findViewById(R.id.btnCapture);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        inputNotes = findViewById(R.id.inputNotes);
        imgPreview = findViewById(R.id.imgPreview);
        txtParcelId = findViewById(R.id.txtParcelId);

        txtParcelId.setText("Parcelle #" + parcelId);

        btnSave.setVisibility(android.view.View.GONE);
        imgPreview.setVisibility(android.view.View.GONE);
    }

    private void setupImageReader() {
        // Create ImageReader once during initialization
        imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 2);
        Log.d(TAG, "ImageReader created");
    }

    private void setupTextureListener() {
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "Surface texture available");
                isTextureAvailable = true;
                if (checkCameraPermission()) {
                    openCamera();
                } else {
                    requestCameraPermission();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "Surface texture size changed");
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.d(TAG, "Surface texture destroyed");
                isTextureAvailable = false;
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                // Called frequently so we're not gonna log anything here
            }
        });
    }

    private void setupClickListeners() {
        btnCapture.setOnClickListener(v -> {
            Log.d(TAG, "Capture button clicked");
            capturePhoto();
        });

        btnSave.setOnClickListener(v -> {
            String notes = inputNotes.getText().toString().trim();
            savePhoto(notes);
        });

        btnCancel.setOnClickListener(v -> {
            Log.d(TAG, "Cancel button clicked");
            finish();
        });
    }

    private boolean checkCameraPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        Log.d(TAG, "Requesting camera permission");
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted");
                if (isTextureAvailable) {
                    openCamera();
                }
            } else {
                Log.e(TAG, "Camera permission denied");
                Toast.makeText(this, "Permission caméra requise", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void openCamera() {
        if (!isTextureAvailable) {
            Log.w(TAG, "Texture not available yet");
            return;
        }

        if (isCameraOpen) {
            Log.w(TAG, "Camera already open");
            return;
        }

        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            Log.d(TAG, "Opening camera: " + cameraId);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "No camera permission");
                return;
            }

            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    Log.d(TAG, "Camera opened successfully");
                    cameraDevice = camera;
                    isCameraOpen = true;
                    createCameraPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.w(TAG, "Camera disconnected");
                    camera.close();
                    cameraDevice = null;
                    isCameraOpen = false;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e(TAG, "Camera error: " + error);
                    camera.close();
                    cameraDevice = null;
                    isCameraOpen = false;
                    runOnUiThread(() ->
                            Toast.makeText(CameraActivity.this,
                                    "Erreur caméra: " + error, Toast.LENGTH_SHORT).show());
                }
            }, backgroundHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access exception", e);
            Toast.makeText(this, "Erreur d'accès caméra", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error opening camera", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createCameraPreview() {
        if (cameraDevice == null) {
            Log.e(TAG, "Camera device is null");
            return;
        }

        if (!isTextureAvailable) {
            Log.e(TAG, "Texture not available");
            return;
        }

        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture == null) {
                Log.e(TAG, "SurfaceTexture is null");
                return;
            }

            texture.setDefaultBufferSize(1920, 1080);
            Surface previewSurface = new Surface(texture);
            Surface captureSurface = imageReader.getSurface();

            // Create preview request
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(previewSurface);

            Log.d(TAG, "Creating capture session with BOTH surfaces");
            cameraDevice.createCaptureSession(
                    Arrays.asList(previewSurface, captureSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            Log.d(TAG, "Capture session configured with both surfaces");
                            captureSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG, "Capture session configuration failed");
                            Toast.makeText(CameraActivity.this,
                                    "Configuration échouée", Toast.LENGTH_SHORT).show();
                        }
                    }, backgroundHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access exception in preview", e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in preview", e);
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) {
            Log.e(TAG, "Camera device is null in updatePreview");
            return;
        }

        try {
            previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            previewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);

            captureSession.setRepeatingRequest(previewBuilder.build(), null, backgroundHandler);
            Log.d(TAG, "Preview started");
        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access exception in updatePreview", e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in updatePreview", e);
        }
    }

    private void capturePhoto() {
        if (cameraDevice == null) {
            Log.e(TAG, "Camera device is null");
            Toast.makeText(this, "Caméra non disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        if (captureSession == null) {
            Log.e(TAG, "Capture session is null");
            Toast.makeText(this, "Session caméra non disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Log.d(TAG, "Starting photo capture");

            // Set image listener
            imageReader.setOnImageAvailableListener(reader -> {
                Log.d(TAG, "Image available");
                Image image = null;
                try {
                    image = reader.acquireLatestImage();
                    if (image != null) {
                        capturedImagePath = saveImageToFile(image);
                        Log.d(TAG, "Image saved to: " + capturedImagePath);

                        runOnUiThread(() -> {
                            textureView.setVisibility(android.view.View.GONE);
                            imgPreview.setVisibility(android.view.View.VISIBLE);
                            imgPreview.setImageURI(android.net.Uri.fromFile(new File(capturedImagePath)));

                            btnCapture.setVisibility(android.view.View.GONE);
                            btnSave.setVisibility(android.view.View.VISIBLE);

                            Toast.makeText(this, "Photo capturée!", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing image", e);
                    runOnUiThread(() ->
                            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } finally {
                    if (image != null) {
                        image.close();
                    }
                }
            }, backgroundHandler);

            // Create capture request using the ImageReader surface
            final CaptureRequest.Builder captureBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());

            // Set auto-focus and auto-exposure
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90); // Portrait orientation

            Log.d(TAG, "Triggering capture");
            captureSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    Log.d(TAG, "Capture completed");
                }

                @Override
                public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                            @NonNull CaptureRequest request,
                                            @NonNull CaptureFailure failure) {
                    Log.e(TAG, "Capture failed: " + failure.getReason());
                    runOnUiThread(() ->
                            Toast.makeText(CameraActivity.this,
                                    "Capture échouée", Toast.LENGTH_SHORT).show());
                }
            }, backgroundHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access exception in capturePhoto", e);
            Toast.makeText(this, "Erreur de capture", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in capturePhoto", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String saveImageToFile(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String fileName = "IMG_" + parcelId + "_" + timestamp + ".jpg";

        File file = new File(getExternalFilesDir(null), fileName);
        Log.d(TAG, "Saving image to: " + file.getAbsolutePath());

        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(bytes);
            output.flush();
            Log.d(TAG, "Image file written successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error writing image file", e);
        }

        return file.getAbsolutePath();
    }

    private void savePhoto(String notes) {
        if (capturedImagePath == null) {
            Toast.makeText(this, "Aucune photo capturée", Toast.LENGTH_SHORT).show();
            return;
        }

        Photo photo = new Photo(parcelId, userEmail, capturedImagePath, notes);

        // Save to local database
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        long id = db.insertPhoto(photo);

        if (id > 0) {
            Log.d(TAG, "Photo saved to database with ID: " + id);
            Toast.makeText(this, "Photo sauvegardée!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Log.e(TAG, "Failed to save photo to database");
            Toast.makeText(this, "Erreur de sauvegarde", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startBackgroundThread();

        if (textureView.isAvailable() && checkCameraPermission()) {
            isTextureAvailable = true;
            openCamera();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void closeCamera() {
        Log.d(TAG, "Closing camera");

        if (captureSession != null) {
            try {
                captureSession.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing capture session", e);
            }
            captureSession = null;
        }

        if (cameraDevice != null) {
            try {
                cameraDevice.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing camera device", e);
            }
            cameraDevice = null;
            isCameraOpen = false;
        }

        Log.d(TAG, "Camera closed");
    }

    private void startBackgroundThread() {
        if (backgroundThread == null) {
            backgroundThread = new HandlerThread("CameraBackground");
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());
            Log.d(TAG, "Background thread started");
        }
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
                Log.d(TAG, "Background thread stopped");
            } catch (InterruptedException e) {
                Log.e(TAG, "Error stopping background thread", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }
}