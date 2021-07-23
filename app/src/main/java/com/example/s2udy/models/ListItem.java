package com.example.s2udy.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("ListItem")
public class ListItem extends ParseObject
{
    public static final String KEY_BODY = "body";
    public static final String KEY_DONE = "done";
    public static final String KEY_ROOM = "room";

    public String getBody() { return getString(KEY_BODY); }
    public void setBody(String body) { put(KEY_BODY, body); }

    public Boolean getDone() { return getBoolean(KEY_DONE); }
    public void setDone(boolean done) { put(KEY_DONE, done); }

    public ParseObject getRoom() { return getParseObject(KEY_ROOM); }
    public void setRoom(Room room) { put(KEY_ROOM, room); }
}
