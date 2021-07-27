package com.example.s2udy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.s2udy.models.Room;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

public class CreateActivity extends AppCompatActivity
{
    public static final String TAG = "CreateActivity";
    public static int capacity;
    EditText etName, etDescription, etCapacity, etMusic, etZoom;
    Switch switchChat;
    Button btnCreate;

    TextView tvRequired;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etCapacity = findViewById(R.id.etCapacity);
        etMusic = findViewById(R.id.etMusic);
        etZoom = findViewById(R.id.etZoom);
        switchChat = findViewById(R.id.switchChat);
        btnCreate = findViewById(R.id.btnCreate);
        tvRequired = findViewById(R.id.tvRequired);
        btnCreate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String name = etName.getText().toString();
                String description = etDescription.getText().toString();
                String spotify = etMusic.getText().toString();
                if (name.isEmpty() || description.isEmpty() || spotify.isEmpty())
                {
                    Toast.makeText(CreateActivity.this, "required fields cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Boolean chat = switchChat.isChecked();
                ParseUser currentUser = ParseUser.getCurrentUser();
                String capacityS = etCapacity.getText().toString();
                String musicLink = etMusic.getText().toString();
                String zoomLink = etZoom.getText().toString();;
                if (!capacityS.isEmpty())
                    capacity = Integer.parseInt(capacityS);

                Room room = createRoom(name, description, chat, currentUser, capacity, musicLink, zoomLink);
                Intent i = new Intent(CreateActivity.this, InRoomActivity.class);
                i.putExtra(Room.class.getSimpleName(), Parcels.wrap(room));
                startActivity(i);
            }
        });
    }

    private Room createRoom(String name, String description, Boolean chat, ParseUser currentUser, int capacity, String musicLink, String zoomLink)
    {
        Room room = new Room();
        room.setName(name);
        room.setDescription(description);
        room.setChatEnabled(chat);
        room.setHost(currentUser);
        room.setCapacity(capacity);
        room.setMusic(musicLink);
        room.setZoom(zoomLink);
        room.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e != null)
                {
                    Log.e(TAG, "issue in creating room!", e);
                    return;
                }
                Log.i(TAG, "room created successfully!");
            }
        });
        finish();
        return room;
    }
}