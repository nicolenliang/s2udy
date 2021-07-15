package com.example.s2udy;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s2udy.models.Room;

import org.parceler.Parcels;

import java.util.List;

public class RoomsAdapter extends RecyclerView.Adapter
{
    public static final String TAG = "RoomsAdapter";
    Context context;
    List<Room> rooms;

    public RoomsAdapter(Context context, List<Room> rooms)
    {
        this.context = context;
        this.rooms = rooms;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        Room room = rooms.get(position);
        ((ViewHolder)holder).bind(room);
    }

    @Override
    public int getItemCount()
    {
        return rooms.size();
    }

    public void clear()
    {
        rooms.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView tvRoomName, tvDescription;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvDescription = itemView.findViewById(R.id.tvDescription);

            itemView.setOnClickListener(this);
        }

        public void bind(Room room)
        {
            tvRoomName.setText(room.getName());
            tvDescription.setText(room.getDescription());
        }

        // click to navigate to in-room screen
        @Override
        public void onClick(View v)
        {
            // grab position; null check; pass in as Parcel wrapped extra
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) // position exists :)
            {
                Room room = rooms.get(position);
                Log.i(TAG,"onClick room: " + room.getName());
                Intent i = new Intent(context, InRoomActivity.class);
                i.putExtra(Room.class.getSimpleName(), Parcels.wrap(room));
                context.startActivity(i);
            }
        }
    }
}
