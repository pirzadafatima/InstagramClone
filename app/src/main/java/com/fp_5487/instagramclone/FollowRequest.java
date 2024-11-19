package com.fp_5487.instagramclone;

public class FollowRequest {
    private String username;
    private String fullName;
    private String profileImageUrl;

    // Empty constructor required for Firebase
    public FollowRequest() {
    }

    public FollowRequest(String username, String fullName, String profileImageUrl) {
        this.username = username;
        this.fullName = fullName;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
