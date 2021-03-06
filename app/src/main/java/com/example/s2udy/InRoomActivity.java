package com.example.s2udy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.davidmiguel.dragtoclose.DragToClose;
import com.example.s2udy.adapters.PageAdapter;
import com.example.s2udy.models.Room;
import com.example.s2udy.models.User;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class InRoomActivity extends AppCompatActivity implements View.OnClickListener
{
    public static final String TAG = "InRoomActivity";
    public Room room;
    public ViewPager2 viewPager;
    Toolbar toolbar;
    TextView tvTitle, tvHost, tvUsers, tvDescription, tvLink;
    CardView cvTimer, cvList, cvChat, cvMusic;
    CircleIndicator3 indicator;
    TabLayout tabLayout;
    DragToClose dragToClose;
    List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_room);

        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvTitle = findViewById(R.id.tvTitle);
        tvHost = findViewById(R.id.tvHost);
        tvUsers = findViewById(R.id.tvUsers);
        tvDescription = findViewById(R.id.tvDescription);
        tvLink = findViewById(R.id.tvLink);
        cvTimer = findViewById(R.id.cvTimer);
        cvList = findViewById(R.id.cvList);
        cvChat = findViewById(R.id.cvChat);
        cvMusic = findViewById(R.id.cvMusic);
        viewPager = findViewById(R.id.viewPager);
        dragToClose = findViewById(R.id.dragToClose);

        room = Parcels.unwrap(getIntent().getParcelableExtra(Room.class.getSimpleName()));

        users = new ArrayList<>();
        users.add((User) User.getCurrentUser());
        updateUsers();

        tvTitle.setText(room.getName());
        tvHost.setText("host: " + room.getHost().getUsername());
        HashSet<String> usernames = new HashSet<>();
        for (User user : room.getUsers())
        {
            try
            { usernames.add(user.fetchIfNeeded().getUsername()); }
            catch (ParseException e)
            { e.printStackTrace(); }
        }
        tvUsers.setText("in room: " + usernames.toString());
        tvDescription.setText(room.getDescription());
        tvLink.setText(room.getZoom());

        PageAdapter pAdapter = new PageAdapter(InRoomActivity.this, room);
        viewPager.setAdapter(pAdapter);
        indicator = findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager, (TabLayoutMediator.TabConfigurationStrategy) (tab, position) ->
        {
            switch (position)
            {
                case 0:
                    tab.setIcon(R.drawable.ic_baseline_chat_bubble);
                    break;
                case 1:
                    tab.setIcon(R.drawable.ic_baseline_timer);
                    break;
                case 2:
                    tab.setIcon(R.drawable.ic_baseline_format_list_bulleted);
                    break;
                case 3:
                    tab.setIcon(R.drawable.ic_baseline_music_note);
                    break;
            }
        }).attach();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                dragToClose.openDraggableContainer();
                dragToClose.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
                if (dragToClose.getVisibility() == View.VISIBLE)
                {
                    dragToClose.closeDraggableContainer();
                    dragToClose.setVisibility(View.GONE);
                }
                else
                {
                    dragToClose.openDraggableContainer();
                    dragToClose.setVisibility(View.VISIBLE);
                }
            }
        });

        cvTimer.setOnClickListener(this);
        cvList.setOnClickListener(this);
        cvChat.setOnClickListener(this);
        cvMusic.setOnClickListener(this);
    }

    private void updateUsers()
    {
        room.setUsers(users);
        room.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e != null)
                {
                    Log.e(TAG, "saveUsers() error in saving", e);
                    return;
                }
                Log.i(TAG, "saveUsers() successful");
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        users.remove((User) User.getCurrentUser());
        updateUsers();
    }

    @Override
    public void onClick(View v)
    {
        dragToClose.setVisibility(View.VISIBLE);

        switch(v.getId())
        {
            case R.id.cvChat:
                Log.i(TAG, "onClick cvChat");
                viewPager.setCurrentItem(0, true);
                break;
            case R.id.cvTimer:
                Log.i(TAG, "onClick cvTimer");
                viewPager.setCurrentItem(1, true);
                break;
            case R.id.cvList:
                Log.i(TAG, "onClick cvList");
                viewPager.setCurrentItem(2, true);
                break;
            case R.id.cvMusic:
                Log.i(TAG, "onClick cvMusic");
                viewPager.setCurrentItem(3, true);
                break;
            default:
                break;
        }
        dragToClose.openDraggableContainer();
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
                        Intent i = new Intent(InRoomActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                        Toast.makeText(InRoomActivity.this, "logout successful!",Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.action_profile:
                Intent i = new Intent(InRoomActivity.this, ProfileActivity.class);
                startActivity(i);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}