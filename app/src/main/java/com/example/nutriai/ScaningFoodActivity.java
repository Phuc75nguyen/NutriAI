package com.example.nutriai;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private File currentPhotoFile;

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    // Xử lý khi chọn ảnh từ Gallery
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Log.d(TAG, "Gallery URI selected: " + uri);
                    Toast.makeText(this, "Processing image...", Toast.LENGTH_SHORT).show();
                    ioExecutor.execute(() -> {
                        try {
                            File file = getFileFromUri(uri);
                            if (file != null && file.exists()) {
                                Log.d(TAG, "File created from URI: " + file.getAbsolutePath());
                                // Chuyển sang màn hình Result ngay lập tức
                                runOnUiThread(() -> navigateToResult(file));
                            } else {
                                Log.e(TAG, "Failed to create file from URI");
                                runOnUiThread(() -> Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error in gallery processing", e);
                            runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
                } else {
                    Log.d(TAG, "Gallery URI is null (user cancelled)");
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

        previewLayout = findViewById(R.id.preview_layout);
        ivCapturedPreview = findViewById(R.id.iv_captured_preview);
        btnRetake = findViewById(R.id.btn_retake);
        btnAnalyze = findViewById(R.id.btn_analyze);

        cameraExecutor = Executors.newSingleThreadExecutor();
        ioExecutor = Executors.newSingleThreadExecutor();

        // Kiểm tra quyền Camera
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        // Sự kiện các nút bấm
        btnCapture.setOnClickListener(v -> takePhoto());
        btnGallery.setOnClickListener(v -> {
            Log.d(TAG, "Launching gallery picker");
            galleryLauncher.launch("image/*");
        });
        btnClose.setOnClickListener(v -> finish());

        btnRetake.setOnClickListener(v -> previewLayout.setVisibility(View.GONE));

        // --- ĐÃ SỬA LẠI ĐOẠN NÀY CHO ĐÚNG CÚ PHÁP ---
        btnAnalyze.setOnClickListener(v -> {
            if (currentPhotoFile != null && currentPhotoFile.exists()) {
                Log.d(TAG, "Analyzing captured photo: " + currentPhotoFile.getAbsolutePath());

                // Bỏ qua việc lưu gallery và gọi thread để test nhanh UI
                navigateToResult(currentPhotoFile);

            } else {
                Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
            }
        });
        // ---------------------------------------------
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

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
                        Log.d(TAG, "Photo captured: " + photoFile.getAbsolutePath());
                        currentPhotoFile = photoFile;
                        showPreview(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: " + exception.getMessage());
                        Toast.makeText(ScaningFoodActivity.this, "Capture failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showPreview(File file) {
        Glide.with(this).load(file).into(ivCapturedPreview);
        previewLayout.setVisibility(View.VISIBLE);
    }

    private void navigateToResult(File file) {
        Log.d(TAG, "Navigating to result with file: " + file.getAbsolutePath());
        Intent intent = new Intent(ScaningFoodActivity.this, FoodResultActivity.class);
        intent.putExtra("image_path", file.getAbsolutePath());
        startActivity(intent);
    }

    // Hàm này hiện tại chưa dùng trong luồng Mock, nhưng giữ lại để sau này dùng
    private void saveToGallery(File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Food_Capture_" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NutriAI");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
                 InputStream inputStream = new FileInputStream(file)) {
                if (outputStream != null) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    getContentResolver().update(uri, values, null, null);
                }
                runOnUiThread(() -> Toast.makeText(this, "Saved to Gallery", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                Log.e(TAG, "Error saving to gallery", e);
            }
        }
    }

    private File getFileFromUri(Uri uri) {
        File file = null;
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) return null;

            file = new File(getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
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
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        ioExecutor.shutdown();
    }
}