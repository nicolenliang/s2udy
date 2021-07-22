package com.example.s2udy.models;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Message")
public class Message extends ParseObject
{
    public static final String KEY_USER = "user";
    public static final String KEY_BODY = "body";
    public static final String KEY_ROOM = "room";

    public ParseUser getUser() { return getParseUser(KEY_USER); }
    public void setUser(ParseUser user) { put(KEY_USER, user); }

    public String getBody() { return getString(KEY_BODY); }
    public void setBody(String body) { put(KEY_BODY, body); }

    public ParseObject getRoom() { return getParseObject(KEY_ROOM); }
    public void setRoom(Room room) { put(KEY_ROOM, room); }
}
