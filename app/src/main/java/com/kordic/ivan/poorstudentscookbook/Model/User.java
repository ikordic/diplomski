package com.kordic.ivan.poorstudentscookbook.Model;

public class User
{

    private String userId;
    private String userEmail;
    private String userUsername;
    private String userProfileImage;

    public User()
    {
    }

    public User(String userId, String userEmail, String userUsername, String userProfileImage)
    {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userUsername = userUsername;
        this.userProfileImage = userProfileImage;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getUserEmail()
    {
        return userEmail;
    }

    public void setUserEmail(String userEmail)
    {
        this.userEmail = userEmail;
    }

    public String getUserUsername()
    {
        return userUsername;
    }

    public void setUserUsername(String userUsername)
    {
        this.userUsername = userUsername;
    }

    public String getUserProfileImage()
    {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage)
    {
        this.userProfileImage = userProfileImage;
    }
}