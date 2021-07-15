package com.example.s2udy.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.s2udy.R;

public class ListFragment extends Fragment
{
    public static final String TAG = "ListFragment";
    CardView cvList;
    TextView tvTitle;
    EditText etList;
    RecyclerView rvList;
    Button btnAdd;

    public ListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        cvList = view.findViewById(R.id.cvList);
        tvTitle = view.findViewById(R.id.tvTitle);
        etList = view.findViewById(R.id.etList);
        rvList = view.findViewById(R.id.rvList);
        btnAdd = view.findViewById(R.id.btnAdd);

        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
        cvList.startAnimation(bottomUp);
    }
}