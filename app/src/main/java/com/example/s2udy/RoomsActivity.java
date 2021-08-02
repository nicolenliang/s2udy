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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.s2udy.adapters.RoomsAdapter;
import com.example.s2udy.models.Room;
import com.example.s2udy.models.User;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.zeeshan.material.multiselectionspinner.MultiSelectionSpinner;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoomsActivity extends AppCompatActivity implements View.OnClickListener
{
    public static final String TAG = "RoomsActivity";
    Toolbar toolbar;
    ImageButton btnCreate, btnFilter, btnDropdown;
    RecyclerView rvRooms;
    RelativeLayout rlFilter;
    EditText etSearch;

    RoomsAdapter adapter;
    List<Room> rooms;
    public List<String> allTags;
    List<String> selectedTags;
    HashMap<String, List<Room>> tagsToRooms;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;
    MultiSelectionSpinner spinner;
    List<User> users;

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
        users = new ArrayList<>();
        swipeContainer = findViewById(R.id.swipeContainer);
        btnCreate = findViewById(R.id.btnCreate);
        btnFilter = findViewById(R.id.btnFilter);
        btnDropdown = findViewById(R.id.btnDropdown);
        rlFilter = findViewById(R.id.rlFilter);
        etSearch = findViewById(R.id.etSearch);
        spinner = findViewById(R.id.spinner);

        btnCreate.setOnClickListener(this);
        btnFilter.setOnClickListener(this);
        btnDropdown.setOnClickListener(this);

        // set up recycler view --> create adapter --> setup layout manager --> connect rv
        rvRooms = findViewById(R.id.rvRooms);
        adapter = new RoomsAdapter(this, rooms);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvRooms.setAdapter(adapter);
        rvRooms.setLayoutManager(gridLayoutManager);
        queryRooms();

        allTags.add(0, "chat enabled");
        allTags.add(1, "chat disabled");
        spinner.setItems(allTags);
        spinner.setHint("apply a filter!");
        spinner.setOnItemSelectedListener(new MultiSelectionSpinner.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(View view, boolean isSelected, int position)
            {
                String selectedTag = allTags.get(position);
                Log.i(TAG, "item selected: " + selectedTag + "; isSelected: " + isSelected);
                if (selectedTag.equals("chat enabled"))
                    filterChat(true);
                else if (selectedTag.equals("chat disabled"))
                    filterChat(false);
                else
                {
                    if (isSelected && !selectedTags.contains(selectedTag))
                        selectedTags.add(selectedTag);
                    else if (!isSelected && selectedTags.contains(selectedTag))
                        selectedTags.remove(selectedTag);
                }
            }

            @Override
            public void onSelectionCleared()
            {
                spinner.clear();
                selectedTags.clear();
                spinner.setItems(allTags);
            }
        });
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                adapter.clear();
                String search = etSearch.getText().toString();
                if (!selectedTags.isEmpty() || !search.isEmpty())
                {
                    if (!selectedTags.isEmpty())
                        filterTags();
                    if (!search.isEmpty())
                        filterSearch(search);
                }
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

    @Override
    public void onClick(View v)
    {
        if (v.getId() == btnFilter.getId())
        {
            Log.i(TAG, "btnFilter on click");
            String search = etSearch.getText().toString();
            if (!selectedTags.isEmpty())
                filterTags();
            else if (!search.isEmpty())
            {
                filterSearch(search);
            }
            else
            {
                rooms.clear();
                queryRooms();
            }
        }
        if (v.getId() == btnCreate.getId())
        {
            Intent i = new Intent(RoomsActivity.this, CreateActivity.class);
            i.putExtra("allTags", Parcels.wrap(allTags));
            startActivity(i);
        }
        if (v.getId() == btnDropdown.getId())
        {
            if (rlFilter.getVisibility() == View.GONE)
            {
                rlFilter.setVisibility(View.VISIBLE);
                btnDropdown.setImageResource(R.drawable.ic_round_keyboard_arrow_up);
            }
            else
            {
                rlFilter.setVisibility(View.GONE);
                btnDropdown.setImageResource(R.drawable.ic_round_keyboard_arrow_down);
            }
        }
    }

    private void filterSearch(String search)
    {
        Log.i(TAG, "entered filterSearch");
        List<Room> filtered = new ArrayList<>();
        for (Room room : rooms)
        {
            if (room.getName().contains(search))
                filtered.add(room);
            for (User user : room.getUsers())
            {
                try
                {
                    if (user.fetchIfNeeded().getUsername().equals(search))
                        filtered.add(room);
                }
                catch (ParseException e)
                { e.printStackTrace(); }
            }
        }
        adapter.clear();
        rooms.addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    private void filterChat(boolean enabled)
    {
        ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
        query.include(Room.KEY_HOST);
        query.whereEqualTo(Room.KEY_CHAT, enabled);
        query.findInBackground(new FindCallback<Room>()
        {
            @Override
            public void done(List<Room> objects, ParseException e)
            {
                if (e != null)
                {
                    Log.e(TAG, "filterChat error in querying rooms", e);
                    return;
                }
                adapter.clear();
                rooms.addAll(objects);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void filterTags()
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

                Set<String> distinctTags = new HashSet<>(allTags);
                allTags.clear();
                allTags.addAll(distinctTags);
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