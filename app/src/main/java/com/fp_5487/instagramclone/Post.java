package com.fp_5487.instagramclone;

import android.graphics.Bitmap;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Post {
    private String id;
    private String description;
    private String imageBase64;
    private String timestamp;
    private File videoFile;
    private Bitmap imageBitmap;  // Added Bitmap field for decoded image
    private String userId;
    private String location;
    private List<String> taggedUsers;
    private int likesCount;
    private Map<String, Boolean> likes;
    private List<Comment> comments;

    public Post() {
    }

    public Post(String id,  String userId, String description, String imageBase64, String timestamp, String location, List<String> taggedUsers) {
        this.description = description;
        this.imageBase64 = imageBase64;
        this.timestamp = timestamp;
        this.id = id;
        this.userId = userId;
        this.location = location;
        this.taggedUsers = taggedUsers;
        this.likesCount = 0;

    }

    public Post(String id, List<Comment> comments, String description, String imageBase64, String timestamp, String userId,
                String location, List<String> taggedUsers, int likesCount, Map<String, Boolean> likes) {
        this.id = id;
        this.comments = comments;
        this.description = description;
        this.imageBase64 = imageBase64;
        this.timestamp = timestamp;
        this.userId = userId;
        this.location = location;
        this.taggedUsers = taggedUsers;
        this.likesCount = likesCount;
        this.likes = likes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public File getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(File videoFile) {
        this.videoFile = videoFile;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getTaggedUsers() {
        return taggedUsers;
    }

    public void setTaggedUsers(List<String> taggedUsers) {
        this.taggedUsers = taggedUsers;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public Map<String, Boolean> getLikes() {
        return likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}