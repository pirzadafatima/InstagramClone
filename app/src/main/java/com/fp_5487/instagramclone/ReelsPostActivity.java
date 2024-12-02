package com.fp_5487.instagramclone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ReelsPostActivity extends AppCompatActivity {

    private VideoView postVideoView;
    private EditText postDescriptionEditText;
    private Button postButton;

    private List<String> taggedUsers;
    private DatabaseReference databaseReference;
    private String currentUserId;
    private Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reels_post);

        postVideoView = findViewById(R.id.postVideoView);
        postDescriptionEditText = findViewById(R.id.postDescriptionVideoEditText);
        postButton = findViewById(R.id.postVideoButton);
        back = findViewById(R.id.back_btnn);
        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("reels");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Retrieve the video URI from the Intent
        Intent intent = getIntent();
        String videoUriString = intent.getStringExtra("selectedVideoUri");

        if (videoUriString != null) {
            Uri videoUri = Uri.parse(videoUriString);

            // Set the video URI to the VideoView
            postVideoView.setVideoURI(videoUri);
            postVideoView.start(); // Play the video
        } else {
            Toast.makeText(this, "No video found", Toast.LENGTH_SHORT).show();
        }

        postButton.setOnClickListener(v -> {
            String description = postDescriptionEditText.getText().toString().trim();

            if (description.isEmpty()) {
                Toast.makeText(ReelsPostActivity.this, "Please add a description", Toast.LENGTH_SHORT).show();
            } else if (videoUriString != null) {
                // Encode video file to Base64
                String base64Video = encodeVideoToBase64(videoUriString);

                if (base64Video != null) {
                    // Save the post to Firebase with Base64 video and description
                    savePostToDatabase(description, base64Video, currentUserId);
                } else {
                    Toast.makeText(ReelsPostActivity.this, "Failed to encode video", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ReelsPostActivity.this, "No video to post", Toast.LENGTH_SHORT).show();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void savePostToDatabase(String description, String base64Video, String userID) {
        if (base64Video == null) {
            Toast.makeText(this, "No video found to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique post ID and timestamp
        String postId = databaseReference.push().getKey();
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Create a Post object with Base64 video data and description
        Post post = new Post(postId, userID, description, base64Video, timestamp, "Example, Country", taggedUsers);

        // Save the post to Firebase Realtime Database
        if (postId != null) {
            databaseReference.child(postId).setValue(post)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                        // Navigate back to HomeActivity
                        Intent intent = new Intent(ReelsPostActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ReelsPostActivity.this, "Failed to create post", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String encodeVideoToBase64(String videoUriString) {
        try {
            Uri videoUri = Uri.parse(videoUriString);
            String videoPath = videoUri.getPath(); // Get the file path from URI

            if (videoPath == null) {
                Toast.makeText(this, "Invalid video file path", Toast.LENGTH_SHORT).show();
                return null;
            }

            FileInputStream fileInputStream = new FileInputStream(videoPath);
            byte[] videoBytes = new byte[fileInputStream.available()];
            fileInputStream.read(videoBytes);
            fileInputStream.close();

            return Base64.encodeToString(videoBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error encoding video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
