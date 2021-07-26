package com.example.s2udy.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseUser;

@ParseClassName("_User")
public class User extends ParseUser
{
    public static final String KEY_NAME = "name";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PFP = "profilePic";

    public String getName() { return getString(KEY_NAME); }
    public void setName(String name) { put(KEY_NAME, name); }

    public String getUsername() { return getString(KEY_USERNAME); }
    public void setUsername(String username) { put(KEY_USERNAME, username); }

    public String getEmail() { return getString(KEY_EMAIL); }
    public void setEmail(String email) { put(KEY_EMAIL, email); }

    public String getPassword() { return getString(KEY_PASSWORD); }
    public void setPassword(String password) { put(KEY_PASSWORD, password); }

    public ParseFile getProfile() { return getParseFile(KEY_PFP); }
    public void setProfile(ParseFile profile) { put(KEY_PFP, profile); }
}
