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
import com.zeeshan.material.multiselectionspinner.MultiSelectionSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoomsActivity extends AppCompatActivity
{
    public static final String TAG = "RoomsActivity";
    Toolbar toolbar;
    ImageButton btnCreate, btnFilter;
    RecyclerView rvRooms;
    RoomsAdapter adapter;
    List<Room> rooms;
    List<String> allTags, selectedTags;
    HashMap<String, List<Room>> tagsToRooms;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;
    MultiSelectionSpinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        allTags = new ArrayList<>();
        selectedTags = new ArrayList<>();
        tagsToRooms = new HashMap<>();
        rooms = new ArrayList<>();
        swipeContainer = findViewById(R.id.swipeContainer);
        btnCreate = findViewById(R.id.btnCreate);
        btnFilter = findViewById(R.id.btnFilter);
        spinner = findViewById(R.id.spinner);
        // set up recycler view --> create adapter --> setup layout manager --> connect rv
        rvRooms = findViewById(R.id.rvRooms);
        adapter = new RoomsAdapter(this, rooms);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvRooms.setAdapter(adapter);
        rvRooms.setLayoutManager(gridLayoutManager);
        queryRooms();

        spinner.setItems(allTags);
        spinner.setHint("apply a filter!");
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
        btnFilter.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i(TAG, "btnFilter on click");
                if (!selectedTags.isEmpty())
                    filterRooms();
                else
                {
                    rooms.clear();
                    queryRooms();
                }
            }
        });
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
                if (!selectedTags.isEmpty())
                    filterRooms();
                else
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
                {
                    Log.i(TAG, "MOREROOMS -- room name: " + room.getName() + "; host: " + room.getHost().getUsername());
                    if (room.getTags() != null)
                        allTags.addAll(room.getTags());
                }
                rooms.addAll(objects);
                adapter.notifyDataSetChanged();
                invertIndex();
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
                {
                    Log.i(TAG, "QUERYROOMS -- room name: " + room.getName() + "; host: " + room.getHost().getUsername());
                    if (room.getTags() != null)
                        allTags.addAll(room.getTags());
                }
                rooms.addAll(objects);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void filterRooms()
    {
        Log.i(TAG, "entered filterRooms()");
        rooms.clear();
        for (String tag : selectedTags)
        {
            if (tagsToRooms.get(tag) != null)
            {
                Log.i(TAG, tag + " has rooms: " + tagsToRooms.get(tag));
                rooms.addAll(tagsToRooms.get(tag));
            }
        }
        adapter.notifyDataSetChanged();
    }

    // mappify allTags: each tag has its own room(s)
    private void invertIndex()
    {
        Log.i(TAG, "entered invertIndex()");
        // map: KEY = tag; VALUE = rooms with tag
        for (String tag : allTags)
        {
            List<Room> tagRooms = new ArrayList<>();

            if (!(tagsToRooms.containsKey(tag))) // add new tag to list if it doesn't exist
                tagsToRooms.put(tag, tagRooms);

            for (Room room : rooms)
                if ((room.getTags()).contains(tag) && !tagRooms.contains(room))
                    tagRooms.add(room);
            tagsToRooms.put(tag, tagRooms);
        }
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