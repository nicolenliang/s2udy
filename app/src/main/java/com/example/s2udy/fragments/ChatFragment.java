package com.example.s2udy.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.s2udy.InRoomActivity;
import com.example.s2udy.R;
import com.example.s2udy.adapters.ChatAdapter;
import com.example.s2udy.models.Message;
import com.example.s2udy.models.Room;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements View.OnClickListener
{
    public static final String TAG = "ChatFragment";
    public static final String WEBSOCKET = "wss://s2udy.b4a.io";
    public Room room;
    CardView cvChat;
    TextView tvTitle, tvDisabled;
    EditText etMessage;
    ImageButton ibSend;
    List<Message> messages;
    RecyclerView rvChat;
    ChatAdapter adapter;

    public ChatFragment(Room room)
    {
        this.room = room;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (!room.getChatEnabled())
            return inflater.inflate(R.layout.fragment_chat_disabled, container, false);
        else
            return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        cvChat = view.findViewById(R.id.cvChat);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvDisabled = view.findViewById(R.id.tvDisabled);
        etMessage = view.findViewById(R.id.etMessage);
        ibSend = view.findViewById(R.id.ibSend);
        ibSend.setOnClickListener(this);

        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
        cvChat.startAnimation(bottomUp);

        messages = new ArrayList<>();
        rvChat = view.findViewById(R.id.rvChat);
        adapter = new ChatAdapter(getContext(), messages, ParseUser.getCurrentUser());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        rvChat.setLayoutManager(linearLayoutManager);
        rvChat.setAdapter(adapter);
        queryMessages();

        ParseLiveQueryClient parseLiveQueryClient = null;
        try
        { parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI(WEBSOCKET)); }
        catch (URISyntaxException e)
        { e.printStackTrace(); }

        ParseQuery<Message> query = ParseQuery.getQuery("Message");
        SubscriptionHandling<Message> subscriptionHandling = parseLiveQueryClient.subscribe(query);
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, (newQuery, object) ->
        {
            messages.add(0, object);
            Log.i(TAG, "subscriptionHandling thing");

            ((Activity)getContext()).runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    adapter.notifyItemInserted(0);
                    rvChat.smoothScrollToPosition(0);
                }
            });
        });
    }

    @Override
    // onClick send message button
    public void onClick(View v)
    {
        String body = etMessage.getText().toString();
        Message message = new Message();
        message.setUser(ParseUser.getCurrentUser());
        message.setBody(body);
        message.setRoom(room);
        message.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e != null)
                {
                    Log.e(TAG, "saveMessage() done error: ", e);
                    return;
                }
                Log.i(TAG, "saveMessage() done successful!");
            }
        });
        etMessage.setText("");
    }

    private void queryMessages()
    {
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
        query.setLimit(50);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<Message>()
        {
            @Override
            public void done(List<Message> objects, ParseException e)
            {
                if (e != null)
                {
                    Log.e(TAG, "issue in querying messages: ", e);
                    return;
                }
                Log.i(TAG, "queryMessages success");
                for (Message message : objects)
                {
                    if (!messages.contains(message) && message.getRoom().hasSameId(room))
                        messages.add(message);
                }
                adapter.notifyDataSetChanged();
                rvChat.smoothScrollToPosition(0);
            }
        });
    }
}