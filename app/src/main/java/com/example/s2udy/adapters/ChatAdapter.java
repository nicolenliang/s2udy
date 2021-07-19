package com.example.s2udy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.s2udy.R;
import com.example.s2udy.models.Message;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter
{
    public static final int INCOMING = 1;
    public static final int OUTGOING = 0;
    Context context;
    List<Message> messages;
    ParseUser user;

    public ChatAdapter(Context context, List<Message> messages, ParseUser user)
    {
        this.context = context;
        this.messages = messages;
        this.user = user;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if (viewType == INCOMING)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.message_incoming, parent, false);
            return new ViewHolderIncoming(view);
        }
        if (viewType == OUTGOING)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.message_outgoing, parent, false);
            return new ViewHolderOutgoing(view);
        }
        else
            throw new IllegalArgumentException("unknown view type!");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        Message message = messages.get(position);
        if (holder instanceof ViewHolderIncoming)
            ((ViewHolderIncoming)holder).bind(message);
        if (holder instanceof ViewHolderOutgoing)
            ((ViewHolderOutgoing)holder).bind(message);
    }

    @Override
    public int getItemCount()
    {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        Message message = messages.get(position);
        boolean me = message.getUser() != null && (message.getUser().getObjectId()).equals(ParseUser.getCurrentUser().getObjectId());
        if (me)
            return OUTGOING;
        else
            return INCOMING;
    }

    public class ViewHolderIncoming extends RecyclerView.ViewHolder
    {
        TextView tvUser, tvMessage;
        ImageView ivProfileOther;

        public ViewHolderIncoming(@NonNull View itemView)
        {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            ivProfileOther = itemView.findViewById(R.id.ivProfileOther);
        }

        public void bind(Message message)
        {
            try
            { tvUser.setText(message.getUser().fetchIfNeeded().getUsername()); }
            catch (ParseException e)
            { e.printStackTrace(); }
            tvMessage.setText(message.getBody());
            ParseFile profilepic = null;
            try
            { profilepic = message.getUser().fetchIfNeeded().getParseFile("profilePic"); }
            catch (ParseException e)
            { e.printStackTrace(); }
            if (profilepic != null)
                Glide.with(context).load(profilepic.getUrl())
                        .circleCrop().into(ivProfileOther);
        }
    }

    public class ViewHolderOutgoing extends RecyclerView.ViewHolder
    {
        TextView tvMessage;
        ImageView ivProfileMe;

        public ViewHolderOutgoing(@NonNull View itemView)
        {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            ivProfileMe = itemView.findViewById(R.id.ivProfileMe);
        }

        public void bind(Message message)
        {
            tvMessage.setText(message.getBody());
            ParseFile profilepic = null;
            try
            { profilepic = message.getUser().fetchIfNeeded().getParseFile("profilePic"); }
            catch (ParseException e)
            { e.printStackTrace(); }
            if (profilepic != null)
                Glide.with(context).load(profilepic.getUrl())
                        .circleCrop().into(ivProfileMe);
        }
    }
}
