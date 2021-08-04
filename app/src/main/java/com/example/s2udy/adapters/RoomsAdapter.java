package com.example.s2udy.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s2udy.InRoomActivity;
import com.example.s2udy.R;
import com.example.s2udy.models.Room;

import org.parceler.Parcels;

import java.util.List;

public class RoomsAdapter extends RecyclerView.Adapter
{
    public static final String TAG = "RoomsAdapter";
    Context context;
    List<Room> rooms;
    onLongClickListener longClickListener;

    public interface onLongClickListener
    {
        void onItemLongClicked(int position);
    }

    public RoomsAdapter(Context context, List<Room> rooms, onLongClickListener longClickListener)
    {
        this.context = context;
        this.rooms = rooms;
        this.longClickListener = longClickListener;
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
        TextView tvRoomName, tvHost, tvChatEnable, tvTags, tvDescription;
        ImageView ivLocked;
        EditText etPasscode;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvHost = itemView.findViewById(R.id.tvHost);
            tvChatEnable = itemView.findViewById(R.id.tvChatEnable);
            tvTags = itemView.findViewById(R.id.tvTags);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivLocked = itemView.findViewById(R.id.ivLocked);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    longClickListener.onItemLongClicked(getAdapterPosition());
                    return false;
                }
            });
        }

        public void bind(Room room)
        {
            tvRoomName.setText(room.getName());
            tvHost.setText("host: " + room.getHost().getUsername());
            String chat = "disabled";
            if (room.getChatEnabled())
                chat = "enabled";
            tvChatEnable.setText("chat: " + chat);
            String tags = room.getTags().toString();
            tags = tags.substring(1, tags.length() - 1);
            tvTags.setText("tags: " + tags);
            tvDescription.setText(room.getDescription());
            if (!(room.getPasscode() == null))
                ivLocked.setVisibility(View.VISIBLE);
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

                if (room.getPasscode() == null)
                    context.startActivity(i);
                else
                {
                    View editDialog = LayoutInflater.from(context).inflate(R.layout.dialog_edit, null);
                    etPasscode = editDialog.findViewById(R.id.etDialog);

                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("enter passcode")
                            .setMessage("room is private! please enter a passcode")
                            .setView(editDialog)
                            .setPositiveButton("submit", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    String passcode = etPasscode.getText().toString();
                                    if (passcode.equals(room.getPasscode()))
                                        context.startActivity(i);
                                    else
                                        Toast.makeText(context, "passcode incorrect!", Toast.LENGTH_SHORT).show();
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
            }
        }
    }
}
