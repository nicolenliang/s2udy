package com.example.s2udy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.s2udy.adapters.RoomsAdapter;
import com.example.s2udy.models.Room;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class RoomsActivity extends AppCompatActivity
{
    public static final String TAG = "RoomsActivity";
    Toolbar toolbar;
    ImageButton btnCreate;
    RecyclerView rvRooms;
    RoomsAdapter adapter;
    List<Room> rooms;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        rooms = new ArrayList<>();
        swipeContainer = findViewById(R.id.swipeContainer);
        btnCreate = findViewById(R.id.btnCreate);
        // set up recycler view --> create adapter --> setup layout manager --> connect rv
        rvRooms = findViewById(R.id.rvRooms);
        adapter = new RoomsAdapter(this, rooms);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvRooms.setAdapter(adapter);
        rvRooms.setLayoutManager(gridLayoutManager);
        queryRooms();

        btnCreate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(RoomsActivity.this, CreateActivity.class);
                startActivity(i);
            }
        });
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                adapter.clear();
                queryRooms();
                swipeContainer.setRefreshing(false);
            }
        });
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager)
        {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view)
            {
                moreRooms();
            }
        };
        rvRooms.addOnScrollListener(scrollListener);
    }

    private void moreRooms()
    {
        ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
        query.include(Room.KEY_HOST);
        query.setSkip(rooms.size());
        query.setLimit(10);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Room>()
        {
            @Override
            public void done(List<Room> objects, ParseException e)
            {
                if (e != null)
                {
                    Log.e(TAG, "moreRooms issue in querying", e);
                    return;
                }
                for (Room room : objects)
                    Log.i(TAG, "room name: " + room.getName() + "; host: " + room.getHost().getUsername());
                rooms.addAll(objects);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void queryRooms()
    {
        ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
        query.include(Room.KEY_HOST);
        query.setLimit(10);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Room>()
        {
            @Override
            public void done(List<Room> objects, ParseException e)
            {
                if (e != null)
                {
                    Log.e(TAG, "queryRooms issue in querying", e);
                    return;
                }
                for (Room room : objects)
                    Log.i(TAG, "room name: " + room.getName() + "; host: " + room.getHost().getUsername());
                rooms.addAll(objects);
                adapter.notifyDataSetChanged();
            }
        });
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
                    Intent i = new Intent(RoomsActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                    Toast.makeText(RoomsActivity.this, "logout successful!",Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (item.getItemId() == R.id.action_profile)
        {
            Intent i = new Intent(RoomsActivity.this, ProfileActivity.class);
            startActivity(i);
        }
        return true;
    }
}