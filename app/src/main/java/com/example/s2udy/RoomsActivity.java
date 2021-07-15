package com.example.s2udy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.s2udy.models.Room;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class RoomsActivity extends AppCompatActivity
{
    public static final String TAG = "RoomsActivity";
    ImageButton btnCreate;
    RecyclerView rvRooms;
    RoomsAdapter adapter;
    List<Room> rooms;
    SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

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
        btnCreate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(RoomsActivity.this, CreateActivity.class);
                startActivity(i);
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
}