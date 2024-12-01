package com.fp_5487.instagramclone;

import java.util.HashMap;

public class Comment {
    private String id;
    private String postId;
    private String userId;
    private String username;
    private String commentText;
    private int likes;
    private HashMap<String, Boolean> likedBy;

    public Comment() {
        // Required for Firebase
    }

    public Comment(String id, String postId, String userId, String username, String commentText, int likes) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.commentText = commentText;
        this.likes = likes;
        this.likedBy = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public HashMap<String, Boolean> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(HashMap<String, Boolean> likedBy) {
        this.likedBy = likedBy;
    }
}