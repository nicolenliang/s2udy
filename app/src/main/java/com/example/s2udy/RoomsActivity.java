package com.example.s2udy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.parse.GetCallback;
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
    ImageButton btnCreate, btnFilter, btnSearch;
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
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        allTags = new ArrayList<>();
        selectedTags = new ArrayList<>();
        tagsToRooms = new HashMap<>();
        rooms = new ArrayList<>();
        users = new ArrayList<>();
        swipeContainer = findViewById(R.id.swipeContainer);
        btnCreate = findViewById(R.id.btnCreate);
        btnFilter = findViewById(R.id.btnFilter);
        btnSearch = findViewById(R.id.btnSearch);
        rlFilter = findViewById(R.id.rlFilter);
        etSearch = findViewById(R.id.etSearch);
        spinner = findViewById(R.id.spinner);

        btnCreate.setOnClickListener(this);
        btnFilter.setOnClickListener(this);
        btnSearch.setOnClickListener(this);

        // long click item to delete
        RoomsAdapter.onLongClickListener longClickListener = new RoomsAdapter.onLongClickListener()
        {
            @Override
            public void onItemLongClicked(int position)
            {
                User current = (User) ParseUser.getCurrentUser();
                AlertDialog dialog = new AlertDialog.Builder(RoomsActivity.this)
                        .setTitle("delete room?")
                        .setPositiveButton("delete", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if (current.getUsername().equals((rooms.get(position)).getHost().getUsername()))
                                    deleteRoom(rooms.get(position));
                                else
                                    Toast.makeText(RoomsActivity.this, "only the host can delete a room!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
            }
        };

        // set up recycler view --> create adapter --> setup layout manager --> connect rv
        rvRooms = findViewById(R.id.rvRooms);
        adapter = new RoomsAdapter(this, rooms, longClickListener);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvRooms.setAdapter(adapter);
        rvRooms.setLayoutManager(gridLayoutManager);
        queryRooms();

        allTags.add(0, "chat enabled");
        allTags.add(1, "chat disabled");
        allTags.add(2, "private");
        spinner.setItems(allTags);
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
                else if (selectedTag.equals("private"))
                    filterPasscode();
                else // selected tags
                {
                    if (isSelected && !selectedTags.contains(selectedTag))
                        selectedTags.add(selectedTag);
                    else if (!isSelected && selectedTags.contains(selectedTag))
                        selectedTags.remove(selectedTag);
                    filterTags();
                }
            }

            @Override
            public void onSelectionCleared()
            {
                spinner.clear();
                selectedTags.clear();
                rooms.clear();
                spinner.setItems(allTags);
                queryRooms();
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
                        if (selectedTags.contains("chat enabled"))
                            filterChat(true);
                        else if (selectedTags.contains("chat disabled"))
                            filterChat(false);
                        else if (selectedTags.contains("private"))
                            filterPasscode();
                        else
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

    private void filterPasscode()
    {
        ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
        query.include(Room.KEY_HOST);
        query.whereNotEqualTo(Room.KEY_PASSCODE, "");
        query.findInBackground(new FindCallback<Room>()
        {
            @Override
            public void done(List<Room> objects, ParseException e)
            {
                if (e != null)
                    return;
                adapter.clear();
                rooms.addAll(objects);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == btnSearch.getId())
        {
            Log.i(TAG, "btnSearch on click");
            String search = etSearch.getText().toString();

            if (!search.isEmpty())
                filterSearch(search);
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
        if (v.getId() == btnFilter.getId())
        {
            if (rlFilter.getVisibility() == View.GONE)
                rlFilter.setVisibility(View.VISIBLE);
            else
                rlFilter.setVisibility(View.GONE);
        }
    }

    private void deleteRoom(Room room)
    {
        ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
        query.getInBackground(room.getObjectId(), new GetCallback<Room>()
        {
            @Override
            public void done(Room object, ParseException e)
            {
                if (e != null)
                    return;
                object.deleteInBackground();
                for (String tag : room.getTags())
                    allTags.remove(tag);
                Log.i(TAG, "deleteRoom() successful!");
            }
        });
        Toast.makeText(RoomsActivity.this, "room \"" + room.getName() + "\" deleted!", Toast.LENGTH_SHORT).show();
        rooms.remove(room);
        queryRooms();
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
            for (String tag : room.getTags())
                if (tag.contains(search))
                    filtered.add(room);
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
        switch(item.getItemId())
        {
            case R.id.action_logout:
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
                break;
            case R.id.action_profile:
                Intent i = new Intent(RoomsActivity.this, ProfileActivity.class);
                startActivity(i);
                break;
        }
        return true;
    }
}