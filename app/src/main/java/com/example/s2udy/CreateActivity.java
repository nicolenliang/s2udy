package com.example.s2udy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.s2udy.models.Room;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.zeeshan.material.multiselectionspinner.MultiSelectionSpinner;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateActivity extends AppCompatActivity
{
    public static final String TAG = "CreateActivity";
    public List<String> tags, allTags;
    List<String> createdTags, selectedTags;
    EditText etName, etDescription, etPasscode, etTags, etMusic, etZoom;
    TextView tvTitle, tvTags, tvRequired;
    Switch switchChat;
    Button btnCreate;
    MultiSelectionSpinner spinner;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        allTags = Parcels.unwrap(getIntent().getParcelableExtra("allTags"));
        tags = new ArrayList<>();

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etPasscode = findViewById(R.id.etPasscode);
        etTags = findViewById(R.id.etTags);
        etMusic = findViewById(R.id.etMusic);
        etZoom = findViewById(R.id.etZoom);
        tvTitle = findViewById(R.id.tvTitle);
        tvTags = findViewById(R.id.tvTags);
        tvRequired = findViewById(R.id.tvRequired);
        switchChat = findViewById(R.id.switchChat);
        btnCreate = findViewById(R.id.btnCreate);
        spinner = findViewById(R.id.multiSelectSpinner);

        btnCreate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String name = etName.getText().toString();
                String description = etDescription.getText().toString();
                String passcode = etPasscode.getText().toString();
                String musicLink = etMusic.getText().toString();
                String zoomLink = etZoom.getText().toString();;

                if (name.isEmpty() || description.isEmpty() || musicLink.isEmpty())
                {
                    Toast.makeText(CreateActivity.this, "required fields cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Boolean chat = switchChat.isChecked();
                ParseUser currentUser = ParseUser.getCurrentUser();

                String tagsUnparsed = etTags.getText().toString(); // getting new created tags
                createdTags = new ArrayList<>(Arrays.asList(tagsUnparsed.toLowerCase().split("\\s*, \\s*")));
                for (String tag : allTags)
                    if (createdTags.contains(tag))
                        createdTags.remove(tag);
                Log.i(TAG, "createdTags: " + createdTags + "; selectedTags: " + selectedTags);
                tags.addAll(createdTags);
                tags.addAll(selectedTags);

                Room room = createRoom(name, description, passcode, chat, currentUser, tags, musicLink, zoomLink);
                Intent i = new Intent(CreateActivity.this, InRoomActivity.class);
                i.putExtra(Room.class.getSimpleName(), Parcels.wrap(room));
                startActivity(i);
            }
        });

        spinner.setItems(allTags);
        selectedTags = new ArrayList<>();
        spinner.setOnItemSelectedListener(new MultiSelectionSpinner.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(View view, boolean isSelected, int position)
            {
                String selectedTag = allTags.get(position);
                Log.i(TAG, "item selected: " + selectedTag + "; isSelected: " + isSelected);
                if (isSelected && !selectedTags.contains(selectedTag))
                    selectedTags.add(selectedTag);
                else if (!isSelected && selectedTags.contains(selectedTag))
                    selectedTags.remove(selectedTag);
            }

            @Override
            public void onSelectionCleared()
            {
                spinner.clear();
                selectedTags.clear();
                spinner.setItems(allTags);
            }
        });
    }

    private Room createRoom(String name, String description, String passcode, Boolean chat, ParseUser currentUser, List<String> tags, String musicLink, String zoomLink)
    {
        Room room = new Room();
        room.setName(name);
        room.setDescription(description);
        room.setPasscode(passcode);
        room.setChatEnabled(chat);
        room.setHost(currentUser);
        room.setTags(tags);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_logout:
                ParseUser.logOutInBackground(new LogOutCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        Intent i = new Intent(CreateActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                        Toast.makeText(CreateActivity.this, "logout successful!",Toast.LENGTH_SHORT).show();
                    }
                });
            case R.id.action_profile:
                Intent i = new Intent(CreateActivity.this, ProfileActivity.class);
                startActivity(i);
            case android.R.id.home:
                onBackPressed();
        }
        return true;
    }
}