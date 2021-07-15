package com.example.s2udy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginActivity extends AppCompatActivity
{
    public static final String TAG = "LoginActivity";
    EditText etUsername, etPassword;
    Button btnLogin, btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // check if user is already logged in; if so, skip this screen and go to main screen
        if (ParseUser.getCurrentUser() != null)
            goMainActivity();

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i(TAG, "onClick: LOG IN button");
                loginUser(etUsername.getText().toString(), etPassword.getText().toString());
            }
        });
        btnSignup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void loginUser(String username, String password)
    {
        Log.i(TAG, "attempting to log in user: " + username);
        // navigate to main screen once user logs in successfully
        ParseUser.logInInBackground(username, password, new LogInCallback()
        {
            @Override
            public void done(ParseUser user, ParseException e)
            {
                if (e != null) // nonnull = exception exists
                {
                    Log.e(TAG, "loginUser issue with login: ", e);
                    Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(LoginActivity.this, "login successful!", Toast.LENGTH_SHORT).show();
                goMainActivity();
            }
        });
    }

    private void goMainActivity()
    {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish(); // so user cannot swipe back to login screen
    }
}