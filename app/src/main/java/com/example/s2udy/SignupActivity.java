package com.example.s2udy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.s2udy.models.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SignupActivity extends AppCompatActivity
{
    public static final String TAG = "SignupActivity";
    public static final int TAKE_PICTURE = 11;
    public static final int UPLOAD_PICTURE = 22;
    private File photoFile;
    private static final String photoFileName = "photo.jpg";
    public User user = new User();
    TextView tvInformation;
    EditText etName, etUsername, etEmail, etPassword;
    ImageView ivProfile;
    Button btnSignup, btnLogin, btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        tvInformation = findViewById(R.id.tvInformation);
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivProfile = findViewById(R.id.ivProfile);
        btnSignup = findViewById(R.id.btnSignup);
        btnLogin = findViewById(R.id.btnLogin);
        btnProfile = findViewById(R.id.btnProfile);
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
                Bitmap bitmap = ((BitmapDrawable)ivProfile.getDrawable()).getBitmap();
                ParseFile profile = bitmapToParsefile(bitmap);
                if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty())
                {
                    Toast.makeText(SignupActivity.this, "must fill out all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }
                profile.saveInBackground(new SaveCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        if (e != null)
                        {
                            Log.e(TAG, "error in saving profile photo", e);
                            return;
                        }
                        signupUser(name, username, email, password, profile);
                    }
                });
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
        btnProfile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog dialog = new AlertDialog.Builder(SignupActivity.this)
                        .setTitle("edit profile picture")
                        .setPositiveButton("upload image", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                                startActivityForResult(i, UPLOAD_PICTURE);
                            }
                        })
                        .setNeutralButton("take picture", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                launchCamera();
                            }
                        })
                        .create();
                dialog.show();
            }
        });
    }

    private void signupUser(String name, String username, String email, String password, ParseFile profile)
    {
        // set properties
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setProfile(profile);
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

    private ParseFile bitmapToParsefile(Bitmap bitmap)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return new ParseFile(photoFileName, imageBytes);
    }

    private void launchCamera()
    {
        // intent launches camera app and then returns to original application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);

        // wrap photoFile obj into a content provider
        Uri fileProvider = FileProvider.getUriForFile(this, "com.example.s2udy.fileprovider", photoFile);
        // places output from camera (wrapped fileProvider obj) into intent
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // check for nullity (if device has a camera)
        if (intent.resolveActivity(getPackageManager()) != null)
        {
            // start the image capture intent to take photo
            startActivityForResult(intent, TAKE_PICTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (!(resultCode == RESULT_OK))
        {
            Toast.makeText(this, "error in uploading image!", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) // activity matches camera activity; everything work gud
        {
            // by this point we have the camera photo on disk; rotate and then store image
            Bitmap takenImage = rotateBitmapOrientation(photoFile.getPath());
            Bitmap resizedImage = BitmapScaler.scaleToFitWidth(takenImage, 100);
            Glide.with(this).load(resizedImage).circleCrop().into(ivProfile);
        }
        if (requestCode == UPLOAD_PICTURE && resultCode == RESULT_OK) // activity matches gallery activity
        {
            Uri selectedImageUri = data.getData();
            Bitmap selectedImage = null;
            try
            { selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri); }
            catch (IOException e)
            { e.printStackTrace(); }
            Bitmap resizedImage = BitmapScaler.scaleToFitWidth(selectedImage, 100);
            Glide.with(this).load(resizedImage).circleCrop().into(ivProfile);
        }
    }

    // returns photo stored on disk given photoFileName
    private File getPhotoFileUri(String fileName)
    {
        // get storage directory for photos; getExternalFilesDir gets package-specific directories
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // if storage dir doesn't exist: create it
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs())
        {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath)
    {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
    }
}