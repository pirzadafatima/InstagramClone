package com.fp_5487.instagramclone;

public class Notification {

    private String username;
    private String message;
    private String timestamp;
    private boolean isFollowRequest; // Add more fields as needed

    public Notification(String username, String message, String timestamp, boolean isFollowRequest) {
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
        this.isFollowRequest = isFollowRequest;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isFollowRequest() {
        return isFollowRequest;
    }
}
