package com.example.nutriai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
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

public class ScaningFoodActivity extends AppCompatActivity {

    private static final String TAG = "ScaningFoodActivity";

    private PreviewView cameraPreview;
    private ImageButton btnCapture, btnClose, btnGallery;
    private RelativeLayout previewLayout;
    private ImageView ivCapturedPreview;
    private Button btnRetake, btnAnalyze;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private ExecutorService ioExecutor;
    private ProcessCameraProvider cameraProvider;
    private File currentPhotoFile;

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

        cameraPreview = findViewById(R.id.camera_preview);
        btnCapture = findViewById(R.id.btn_capture);
        btnClose = findViewById(R.id.btn_close);
        btnGallery = findViewById(R.id.btn_gallery);
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

        // Logic nút Chụp lại: Chỉ cần ẩn ảnh preview đi, Camera thực tế vẫn đang chạy bên dưới
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
        // Không cần thiết phải unbind ở đây nếu dùng bindToLifecycle,
        // nhưng giữ lại cũng không sao.
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
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll();

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                            this,
                            cameraSelector,
                            preview,
                            imageCapture // Quan trọng: Phải bind cái này
                    );

                } catch (Exception exc) {
                    Log.e(TAG, "Use case binding failed", exc);
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera start failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        // Kiểm tra imageCapture có null không
        if (imageCapture == null) return;

        // --- SỬA LỖI Ở ĐÂY: TUYỆT ĐỐI KHÔNG GỌI unbindAll() ---
        // Nếu gọi unbindAll() lúc này, Camera sẽ bị ngắt kết nối ngay lập tức -> Lỗi Not Bound.

        // Tạo file lưu ảnh
        File photoFile = new File(getCacheDir(), "food_capture_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Chụp ảnh
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // Ảnh chụp thành công, lưu vào biến tạm
                        currentPhotoFile = photoFile;

                        // Hiển thị ảnh vừa chụp lên màn hình (che lấp Camera Preview)
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
        // Dùng Glide load ảnh vào ImageView và hiện layout Preview lên
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
}