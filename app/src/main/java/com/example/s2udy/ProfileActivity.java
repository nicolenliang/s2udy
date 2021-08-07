package com.example.s2udy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.s2udy.models.User;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity
{
    public static final String TAG = "ProfileActivity";
    public static final int TAKE_PICTURE = 11;
    private static final int UPLOAD_PICTURE = 22;
    public final User user = (User) User.getCurrentUser();
    private File photoFile;
    public String photoFileName = user.getUsername() + ".jpg";
    TextView tvTitle, tvName, tvUsername, tvEmail, tvPassword;
    EditText etName, etUsername, etEmail, etPassword;
    Button btnProfile, btnSave, btnEdit;
    ImageView ivProfile;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvTitle = findViewById(R.id.tvTitle);
        tvName = findViewById(R.id.tvName);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvPassword = findViewById(R.id.tvPassword);

        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnProfile = findViewById(R.id.btnProfile);
        btnSave = findViewById(R.id.btnSave);
        btnEdit = findViewById(R.id.btnEdit);
        ivProfile = findViewById(R.id.ivProfile);

        etName.setText(user.getName());
        etUsername.setText(user.getUsername());
        etEmail.setText(user.getEmail());
        etPassword.setText(user.getPassword());
        getCurrentProfile();

        btnEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                etName.setEnabled(true);
                etUsername.setEnabled(true);
                etEmail.setEnabled(true);
                etPassword.setEnabled(true);
            }
        });
        btnProfile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog dialog = new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("edit profile picture")
                        .setPositiveButton("upload image", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                                startActivityForResult(i, UPLOAD_PICTURE);
                                getCurrentProfile();
                            }
                        })
                        .setNeutralButton("take picture", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                launchCamera();
                                getCurrentProfile();
                            }
                        })
                        .create();
                dialog.show();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String name = etName.getText().toString();
                String username = etUsername.getText().toString();
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                Bitmap bitmap = ((BitmapDrawable)ivProfile.getDrawable()).getBitmap();
                ParseFile profile = bitmapToParsefile(bitmap);

                saveInfo(name, username, email, password, profile);
            }
        });
    }

    private void saveInfo(String name, String username, String email, String password, ParseFile profile)
    {
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        if (!password.isEmpty())
            user.setPassword(password);
        user.setProfile(profile);
        user.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e != null)
                {
                    Log.e(TAG, "saveInfo() error in saving: ", e);
                    return;
                }
                Toast.makeText(ProfileActivity.this, "user information saved!", Toast.LENGTH_SHORT).show();
                etName.setEnabled(false);
                etUsername.setEnabled(false);
                etEmail.setEnabled(false);
                etPassword.setEnabled(false);
            }
        });
    }

    private void getCurrentProfile()
    {
        String userId = user.getObjectId();
        ParseQuery<User> query = ParseQuery.getQuery(User.class);
        query.getInBackground(userId, new GetCallback<User>()
        {
            @Override
            public void done(User object, ParseException e)
            {
                if (e != null)
                {
                    Log.e(TAG, "error in retrieving user: ", e);
                    return;
                }
                ParseFile profile = object.getProfile();
                Glide.with(ProfileActivity.this)
                        .load(profile.getUrl())
                        .circleCrop()
                        .into(ivProfile);
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_logout:
                ParseUser.logOutInBackground(new LogOutCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                        Toast.makeText(ProfileActivity.this, "logout successful!",Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.action_profile:
                Intent i = new Intent(ProfileActivity.this, ProfileActivity.class);
                startActivity(i);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}