package com.example.nutriai;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector; // Import mới cho Zoom
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScaningFoodActivity extends AppCompatActivity {

    private static final String TAG = "ScaningFoodActivity";

    private PreviewView cameraPreview;
    private ImageButton btnCapture, btnClose, btnGallery, btnFlash;
    private RelativeLayout previewLayout;
    private ImageView ivCapturedPreview;
    private Button btnRetake, btnAnalyze;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private ExecutorService ioExecutor;
    private ProcessCameraProvider cameraProvider;
    private File currentPhotoFile;

    // Biến quan trọng để điều khiển Camera
    private Camera camera;
    private boolean isFlashOn = false;

    // [MỚI] Biến phát hiện cử chỉ 2 ngón tay để Zoom
    private ScaleGestureDetector scaleGestureDetector;

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ioExecutor.execute(() -> {
                        File file = getFileFromUri(uri);
                        if (file != null && file.exists()) {
                            runOnUiThread(() -> navigateToResult(file));
                        }
                    });
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaning_food);

        // Ánh xạ View
        cameraPreview = findViewById(R.id.camera_preview);
        btnCapture = findViewById(R.id.btn_capture);
        btnClose = findViewById(R.id.btn_close);
        btnGallery = findViewById(R.id.btn_gallery);
        btnFlash = findViewById(R.id.btn_flash);
        previewLayout = findViewById(R.id.preview_layout);
        ivCapturedPreview = findViewById(R.id.iv_captured_preview);
        btnRetake = findViewById(R.id.btn_retake);
        btnAnalyze = findViewById(R.id.btn_analyze);

        cameraExecutor = Executors.newSingleThreadExecutor();
        ioExecutor = Executors.newSingleThreadExecutor();

        // 1. Setup Listeners
        btnCapture.setOnClickListener(v -> takePhoto());
        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnClose.setOnClickListener(v -> finish());

        // Cài đặt chức năng Flash
        setupFlashButton();

        // [CẬP NHẬT] Cài đặt cảm ứng (Bao gồm cả Focus và Zoom)
        setupTouchListener();

        // Logic nút Chụp lại
        btnRetake.setOnClickListener(v -> {
            previewLayout.setVisibility(View.GONE);
            currentPhotoFile = null;
        });

        btnAnalyze.setOnClickListener(v -> {
            if (currentPhotoFile != null && currentPhotoFile.exists()) {
                navigateToResult(currentPhotoFile);
            } else {
                Toast.makeText(this, "Chưa có ảnh nào được chụp.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        previewLayout.setVisibility(View.GONE);
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        ioExecutor.shutdown();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                // Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                // ImageCapture
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Select Back Camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                try {
                    cameraProvider.unbindAll();

                    // --- Gán đối tượng Camera ---
                    camera = cameraProvider.bindToLifecycle(
                            this,
                            cameraSelector,
                            preview,
                            imageCapture
                    );

                    // --- [MỚI] 1. CÀI ĐẶT ZOOM MẶC ĐỊNH 2.5x ---
                    // Giúp ảnh không bị xa, vật thể to rõ ngay khi mở camera
                    if (camera != null) {
                        camera.getCameraControl().setZoomRatio(2.0f);
                    }

                } catch (Exception exc) {
                    Log.e(TAG, "Use case binding failed", exc);
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera start failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(getCacheDir(), "food_capture_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        currentPhotoFile = photoFile;
                        showPreview(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                        Toast.makeText(ScaningFoodActivity.this, "Lỗi chụp ảnh: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showPreview(File file) {
        Glide.with(this).load(file).into(ivCapturedPreview);
        previewLayout.setVisibility(View.VISIBLE);
    }

    private void navigateToResult(File file) {
        Intent intent = new Intent(ScaningFoodActivity.this, FoodResultActivity.class);
        intent.putExtra("image_path", file.getAbsolutePath());
        startActivity(intent);
    }

    private File getFileFromUri(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) return null;
            File file = new File(getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
            return file;
        } catch (Exception e) {
            Log.e(TAG, "Error getting file from URI", e);
            return null;
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "Cần quyền Camera để sử dụng tính năng này.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // --- CẤU HÌNH FLASH (TORCH) ---
    private void setupFlashButton() {
        btnFlash.setOnClickListener(v -> {
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                isFlashOn = !isFlashOn;
                camera.getCameraControl().enableTorch(isFlashOn);
                if (isFlashOn) {
                    btnFlash.setImageResource(R.drawable.ic_flash_on);
                } else {
                    btnFlash.setImageResource(R.drawable.ic_flash_off);
                }
            } else {
                Toast.makeText(this, "Thiết bị không có đèn Flash", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- [MỚI] CẤU HÌNH CẢM ỨNG: ZOOM 2 NGÓN + TAP TO FOCUS ---
    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListener() {
        // 1. Khởi tạo bộ nhận diện cử chỉ Zoom (Pinch-to-zoom)
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (camera == null) return false;

                // Lấy mức Zoom hiện tại
                float currentZoomRatio = camera.getCameraInfo().getZoomState().getValue().getZoomRatio();

                // Tính mức Zoom mới dựa trên độ mở của 2 ngón tay
                float delta = detector.getScaleFactor();
                camera.getCameraControl().setZoomRatio(currentZoomRatio * delta);
                return true;
            }
        });

        // 2. Gán sự kiện chạm vào màn hình
        cameraPreview.setOnTouchListener((view, event) -> {
            // A. Gửi sự kiện cho bộ xử lý Zoom trước
            scaleGestureDetector.onTouchEvent(event);

            // B. Xử lý Lấy nét (Focus) khi nhấc tay lên (ACTION_UP)
            if (event.getAction() == MotionEvent.ACTION_UP) {
                MeteringPointFactory factory = cameraPreview.getMeteringPointFactory();
                MeteringPoint point = factory.createPoint(event.getX(), event.getY());

                FocusMeteringAction action = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                        .setAutoCancelDuration(3, TimeUnit.SECONDS)
                        .build();

                if (camera != null) {
                    camera.getCameraControl().startFocusAndMetering(action);
                }
                view.performClick();
                return true;
            }

            // Cần return true để tiếp tục nhận diện các cử chỉ kéo/thả tiếp theo
            return true;
        });
    }
}