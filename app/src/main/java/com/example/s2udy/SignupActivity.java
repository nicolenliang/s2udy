package com.example.s2udy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.s2udy.models.User;
import com.parse.ParseException;
import com.parse.SignUpCallback;

public class SignupActivity extends AppCompatActivity
{
    public static final String TAG = "SignupActivity";
    EditText etName, etUsername, etEmail, etPassword;
    Button btnSignup, btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i(TAG, "onClick: SIGN UP BUTTON");
                String name = etName.getText().toString();
                String username = etUsername.getText().toString();
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty())
                {
                    Toast.makeText(SignupActivity.this, "must fill out all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }
                signupUser(name, username, email, password);
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void signupUser(String name, String username, String email, String password)
    {
        // create ParseUser obj
        User user = new User();
        // set properties
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e != null) // check for exception :)
                {
                    Log.e(TAG, "signupUser issue with signup", e);
                    Toast.makeText(SignupActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(SignupActivity.this, "sign up successful!", Toast.LENGTH_SHORT).show();
                goMainActivity();
            }
        });
    }

    private void goMainActivity()
    {
        Intent i = new Intent(SignupActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }


}