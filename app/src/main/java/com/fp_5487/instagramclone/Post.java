package com.fp_5487.instagramclone;

import android.graphics.Bitmap;

import java.io.File;

public class Post {
    private String description;
    private String imageBase64;
    private String timestamp;
    private File videoFile;
    private Bitmap imageBitmap;  // Added Bitmap field for decoded image

    // Default constructor required for Firebase
    public Post() {}


    // Constructor for creating a new Post
    public Post(String description, String imageBase64, String timestamp) {
        this.description = description;
        this.imageBase64 = imageBase64;
        this.timestamp = timestamp;
    }

    // Getter and setter methods
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public void setVideoFile(File videoFile) {
        this.videoFile = videoFile;
    }

    public File getVideoFile() {
        return this.videoFile;
    }


}
