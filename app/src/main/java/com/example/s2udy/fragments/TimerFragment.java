package com.example.s2udy.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.s2udy.R;

import java.util.Locale;

public class TimerFragment extends Fragment
{
    public static final String TAG = "TimerFragment";
    private long startTimeInMillis;
    private long timeLeftInMillis;
    private boolean timerRunning, timerStarted;

    CountDownTimer cdTimer;

    CardView cvTimer;
    EditText etTimer;
    TextView tvTitle, tvTimer;
    Button btnStartPause, btnReset;

    public TimerFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        cvTimer = view.findViewById(R.id.cvTimer);
        etTimer = view.findViewById(R.id.etTimer);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvTimer = view.findViewById(R.id.tvTimer);
        btnStartPause = view.findViewById(R.id.btnStartPause);
        btnReset = view.findViewById(R.id.btnReset);

        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
        cvTimer.startAnimation(bottomUp);

        btnStartPause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // check if running: yes = pause, no = start
                if (timerRunning)
                {
                    cdTimer.cancel();
                    timerRunning = false;
                    btnStartPause.setText("start");
                    btnReset.setVisibility(View.VISIBLE);
                }
                else
                {
                    String input = etTimer.getText().toString();
                    if (input.isEmpty())
                    {
                        Toast.makeText(getContext(), "timer cannot be empty!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    parseInput(input);
                    startTimer();
                }
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                resetTimer();
                timerStarted = false;
            }
        });
        updateCountdownText();
    }

    private void resetTimer()
    {
        timeLeftInMillis = startTimeInMillis;
        updateCountdownText();
        btnReset.setVisibility(View.INVISIBLE);
        btnStartPause.setVisibility(View.VISIBLE);
    }

    private void updateCountdownText()
    {
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted;
        if (hours > 0)
            timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds); // formats into 00:00:00 with default TZ
        else
            timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds); // formats into 00:00 if no hours
        tvTimer.setText(timeFormatted);
    }

    private void startTimer()
    {
        if (!timerStarted)
        {
            resetTimer();
            timerStarted = true;
        }
        cdTimer = new CountDownTimer(timeLeftInMillis, 1000) // interval every second/1000 millis
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                timeLeftInMillis = millisUntilFinished;
                updateCountdownText();
            }
            @Override
            public void onFinish()
            {
                timerRunning = false;
                btnStartPause.setText("start");
                btnStartPause.setVisibility(View.INVISIBLE); // cant start timer when its at 00:00
                btnReset.setVisibility(View.VISIBLE);
            }
        }.start(); // immediately starts timer after calling method
        timerRunning = true;
        btnStartPause.setText("pause"); // if timer is running, button gives option to pause
        btnReset.setVisibility(View.INVISIBLE);
    }

    private long parseInput(String input)
    {
        if (input.length() == 8 || input.length() == 7) // hh:mm:ss OR h:mm:ss
        {
            int hours = Integer.parseInt(input.substring(0, input.indexOf(":")));
            int minutes = Integer.parseInt(input.substring(input.indexOf(":") + 1, input.indexOf(":", 3)));
            int seconds = Integer.parseInt(input.substring(input.indexOf(":", 3) + 1));
            startTimeInMillis = (hours * 3600000) + (minutes * 60000) + (seconds * 1000);
        }
        else if (input.length() == 5 || input.length() == 4) // mm:ss OR m:ss
        {
            int minutes = Integer.parseInt(input.substring(0, input.indexOf(":")));
            int seconds = Integer.parseInt(input.substring(input.indexOf(":") + 1));
            startTimeInMillis = (minutes * 60000) + (seconds * 1000);
        }
        else if (input.length() == 2 || input.length() == 1)
        {
            int seconds = Integer.parseInt(input);
            startTimeInMillis = seconds * 1000;
        }
        return startTimeInMillis;
    }
}