package com.example.collegescope;

public class UserProfile
{
    public String userSAT, userEmail, userName, userGPA;

    public UserProfile()
    {

    }

    public UserProfile(String userSAT, String userEmail, String userName, String userGPA)
    {
        this.userSAT = userSAT;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userGPA = userGPA;
    }

    public String getUserSAT()
    {
        return userSAT;
    }

    public void setUserSAT(String userAge)
    {
        this.userSAT = userAge;
    }

    public String getUserGPA()
    {
        return userGPA;
    }

    public void setUserGPA(String userGPA)
    {
        this.userGPA = userGPA;
    }

    public String getUserEmail()
    {
        return userEmail;
    }

    public void setUserEmail(String userEmail)
    {
        this.userEmail = userEmail;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }
}