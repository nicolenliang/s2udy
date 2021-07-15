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

public class MusicFragment extends Fragment
{
    public static final String TAG = "MusicFragment";
    CardView cvMusic;
    TextView tvTitle;

    public MusicFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_music, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        cvMusic = view.findViewById(R.id.cvMusic);
        tvTitle = view.findViewById(R.id.tvTitle);

        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
        cvMusic.startAnimation(bottomUp);
    }
}