package com.example.nutriai;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import java.io.File;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        // Initialize views
        PhotoView photoView = findViewById(R.id.photo_view);
        ImageButton btnClose = findViewById(R.id.btn_close);

        // Set listener for the close button
        btnClose.setOnClickListener(v -> finish());

        // Get the image path from the intent
        String imagePath = getIntent().getStringExtra("image_path");

        // Load the image using Glide
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if(imageFile.exists()){
                Glide.with(this)
                     .load(imageFile)
                     .into(photoView);
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No image path provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
