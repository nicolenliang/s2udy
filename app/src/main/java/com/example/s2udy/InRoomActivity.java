package com.example.s2udy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.s2udy.fragments.ChatFragment;
import com.example.s2udy.fragments.ListFragment;
import com.example.s2udy.fragments.MusicFragment;
import com.example.s2udy.fragments.TimerFragment;
import com.example.s2udy.models.Room;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class InRoomActivity extends AppCompatActivity implements View.OnClickListener
{
    public static final String TAG = "InRoomActivity";
    public Room room;
    Toolbar toolbar;
    RelativeLayout rlContainer;
    TextView tvTitle, tvHost, tvDescription, tvLink;
    CardView cvTimer, cvList, cvChat, cvMusic;
    final FragmentManager fragmentManager = getSupportFragmentManager();
    public BottomNavigationView bottomNavigation;
    Fragment fragment, timerFragment, listFragment, chatFragment, musicFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_room);

        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        rlContainer = findViewById(R.id.rlContainer);
        tvTitle = findViewById(R.id.tvTitle);
        tvHost = findViewById(R.id.tvHost);
        tvDescription = findViewById(R.id.tvDescription);
        tvLink = findViewById(R.id.tvLink);
        cvTimer = findViewById(R.id.cvTimer);
        cvList = findViewById(R.id.cvList);
        cvChat = findViewById(R.id.cvChat);
        cvMusic = findViewById(R.id.cvMusic);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        room = Parcels.unwrap(getIntent().getParcelableExtra(Room.class.getSimpleName()));
        tvTitle.setText(room.getName());
        tvHost.setText(room.getHost().getUsername());
        tvDescription.setText(room.getDescription());
        tvLink.setText(room.getZoom());

        timerFragment = new TimerFragment(room);
        listFragment = new ListFragment(room);
        chatFragment = new ChatFragment(room);
        musicFragment = new MusicFragment(room);

        cvTimer.setOnClickListener(this);
        cvList.setOnClickListener(this);
        cvChat.setOnClickListener(this);
        cvMusic.setOnClickListener(this);

        rlContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (fragment != null)
                    fragmentManager.beginTransaction().remove(fragment).commit();
                // TODO: set animation for when we close fragment
            }
        });
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch(item.getItemId())
                {
                    case R.id.action_timer:
                        fragment = timerFragment;
                        break;
                    case R.id.action_list:
                        fragment = listFragment;
                        break;
                    case R.id.action_chat:
                        fragment = chatFragment;
                        break;
                    case R.id.action_music:
                        fragment = musicFragment;
                        break;
                }
                if (fragment.isVisible())
                    fragmentManager.beginTransaction().remove(fragment).commit();
                else
                    fragmentManager.beginTransaction().replace(R.id.rlContainer, fragment).commit();
                return true;
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.cvTimer:
                Log.i(TAG, "onClick cvTimer");
                fragment = timerFragment;
                bottomNavigation.setSelectedItemId(R.id.action_timer);
                break;
            case R.id.cvList:
                Log.i(TAG, "onClick cvList");
                fragment = listFragment;
                bottomNavigation.setSelectedItemId(R.id.action_list);
                break;
            case R.id.cvChat:
                Log.i(TAG, "onClick cvChat");
                fragment = chatFragment;
                bottomNavigation.setSelectedItemId(R.id.action_chat);
                break;
            case R.id.cvMusic:
                Log.i(TAG, "onClick cvMusic");
                fragment = musicFragment;
                bottomNavigation.setSelectedItemId(R.id.action_music);
                break;
        }
        fragmentManager.beginTransaction().replace(R.id.rlContainer, fragment).commit();
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
        if (item.getItemId() == R.id.action_logout)
        {
            ParseUser.logOutInBackground(new LogOutCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    Intent i = new Intent(InRoomActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                    Toast.makeText(InRoomActivity.this, "logout successful!",Toast.LENGTH_SHORT).show();
                }
            });
        }
        return true;
    }
}