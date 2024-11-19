package com.fp_5487.instagramclone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.SharedPreferences;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class PostActivity extends AppCompatActivity {

    private ImageView postImageView;
    private EditText postDescriptionEditText;
    private Button postButton;

    private DatabaseReference databaseReference;

    private String base64Image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postImageView = findViewById(R.id.postImageView);
        postDescriptionEditText = findViewById(R.id.postDescriptionEditText);
        postButton = findViewById(R.id.postButton);

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");

        // Retrieve the Base64 encoded image string from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("image_prefs", MODE_PRIVATE);
        base64Image = sharedPreferences.getString("imageBase64", null);

        if (base64Image != null) {
            // Decode the Base64 string to a Bitmap
            Bitmap bitmap = decodeBase64ToBitmap(base64Image);
            postImageView.setImageBitmap(bitmap);
        }

        postButton.setOnClickListener(v -> {
            String description = postDescriptionEditText.getText().toString().trim();

            if (description.isEmpty()) {
                Toast.makeText(PostActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Save the post to Firebase Realtime Database with Base64 image
                savePostToDatabase(description, base64Image);
            }
        });
    }

    private void savePostToDatabase(String description, String base64Image) {
        if (base64Image == null) {
            Toast.makeText(this, "No image found to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique post ID and timestamp
        String postId = databaseReference.push().getKey();
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Create the Post object with Base64 image data and description
        Post post = new Post(description, base64Image, timestamp);

        // Save the post to Firebase Realtime Database
        if (postId != null) {
            databaseReference.child(postId).setValue(post)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(PostActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate back to HomeActivity
                        Intent intent = new Intent(PostActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish(); // Close the activity after saving
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PostActivity.this, "Failed to create post", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Helper method to decode Base64 string to Bitmap
    private Bitmap decodeBase64ToBitmap(String base64Image) {
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
