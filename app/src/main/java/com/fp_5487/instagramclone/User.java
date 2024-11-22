package com.fp_5487.instagramclone;

public class User {
    private String email;
    private String username;
    private String fullName;
    private String profilePic;
    private String bio;
    private String website;
    private String gender;
    private int followers;
    private int following;
    private int posts;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, int posts, String gender, String username, String fullName, String profilePic, String bio, String website, int followers, int following) {
        this.email = email;
        this.posts = posts;
        this.gender = gender;
        this.username = username;
        this.fullName = fullName;
        this.profilePic = profilePic;
        this.bio = bio;
        this.website = website;
        this.followers = followers;
        this.following = following;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public int getPosts() {
        return posts;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }
}

