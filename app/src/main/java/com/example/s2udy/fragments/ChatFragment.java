package com.example.s2udy.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.s2udy.R;

public class ChatFragment extends Fragment
{
    public static final String TAG = "ChatFragment";
    CardView cvChat;
    TextView tvTitle;

    public ChatFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        cvChat = view.findViewById(R.id.cvChat);
        tvTitle = view.findViewById(R.id.tvTitle);

        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
        cvChat.startAnimation(bottomUp);
    }
}