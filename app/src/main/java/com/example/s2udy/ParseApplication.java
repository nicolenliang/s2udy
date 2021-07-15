package com.example.s2udy;

import android.app.Application;

import com.example.s2udy.models.Room;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        ParseObject.registerSubclass(Room.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("Jz6QWqjeiMYLgrUYOux9cWbtU18ayvDJXlNUlBtL")
                .clientKey("Bi50hNVWFSxmKxA1xG4LS7f7laiLkpMmaU19k2Ke")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
