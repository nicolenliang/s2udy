package com.example.s2udy.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

@ParseClassName("Room")
public class Room extends ParseObject
{
    public static final String KEY_ID = "objectId";
    public static final String KEY_NAME = "roomname";
    public static final String KEY_HOST = "host";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_CAPACITY = "capacity";
    public static final String KEY_PASSCODE = "passcode";
    public static final String KEY_CHAT = "chatEnabled";
    public static final String KEY_MUSIC = "music";
    public static final String KEY_ZOOM = "zoom";
    public static final String KEY_TAGS = "tags";
    public static final String KEY_USERS = "users";

    public String getId() { return getString(KEY_ID); }

    public String getName() { return getString(KEY_NAME); }
    public void setName(String roomname) { put(KEY_NAME, roomname); }

    public ParseUser getHost() { return getParseUser(KEY_HOST); }
    public void setHost(ParseUser host) { put(KEY_HOST, host); }

    public String getDescription() { return getString(KEY_DESCRIPTION); }
    public void setDescription(String description) { put(KEY_DESCRIPTION, description); }

    public int getCapacity() { return getInt(KEY_CAPACITY); }
    public void setCapacity(int capacity) { put(KEY_CAPACITY, capacity); }

    public String getPasscode() { return getString(KEY_PASSCODE); }
    public void setPasscode(String passcode) { put(KEY_PASSCODE, passcode); }

    public Boolean getChatEnabled() { return getBoolean(KEY_CHAT); }
    public void setChatEnabled(Boolean chat) { put(KEY_CHAT, chat); }

    public String getMusic() { return getString(KEY_MUSIC); }
    public void setMusic(String music) { put(KEY_MUSIC, music); }

    public String getZoom() { return getString(KEY_ZOOM); }
    public void setZoom(String zoom) { put(KEY_ZOOM, zoom); }

    public List<String> getTags() { return getList(KEY_TAGS); }
    public void setTags(List<String> tags) { put(KEY_TAGS, tags); }

    public List<User> getUsers() { return getList(KEY_USERS); }
    public void setUsers(List<User> users) { put(KEY_USERS, users); }
}
