package com.example.s2udy.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.s2udy.SpotifyClient;
import com.example.s2udy.VolleyCallBack;
import com.example.s2udy.adapters.SongSearchAdapter;
import com.example.s2udy.models.Room;
import com.example.s2udy.models.Song;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.Track;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import com.example.s2udy.R;

import java.util.ArrayList;
import java.util.List;

public class MusicFragment extends Fragment implements View.OnClickListener
{
    public static final String TAG = "MusicFragment";
    public static final String CLIENT_ID = "2c534ec838644149b8b738468a8e9dbf";
    public static final String REDIRECT_URI = "http://com.example.s2udy/callback";
    public static final int REQUEST_CODE = 9999;
    public static final String scopes =
            "user-read-playback-state,app-remote-control,user-modify-playback-state,user-read-currently-playing,playlist-read-private";
    private boolean started, playing;
    private SpotifyAppRemote spotifyAppRemote;
    private SpotifyClient spotifyClient;
    SharedPreferences sharedPreferences;
    RequestQueue requestQueue;
    Room room;
    CardView cvMusic;
    TextView tvTitle, tvSong, tvArtist;
    ImageView ivAlbum;
    ImageButton ibPlayPause, ibNext, ibPrevious, ibQueue;
    EditText etSong, etSearch;
    Button btnAdd, btnSearch;
    RecyclerView rvSearch;
    SongSearchAdapter adapter;
    List<String> queue;
    List<Song> recents, searches;
    Song recent, searched;

    public MusicFragment(Room room)
    {
        this.room = room;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_music, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        spotifyClient = new SpotifyClient(requireContext());

        cvMusic = view.findViewById(R.id.cvMusic);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvSong = view.findViewById(R.id.tvSong);
        tvArtist = view.findViewById(R.id.tvArtist);
        ivAlbum = view.findViewById(R.id.ivAlbum);
        ibPlayPause = view.findViewById(R.id.ibPlayPause);
        ibNext = view.findViewById(R.id.ibNext);
        ibPrevious = view.findViewById(R.id.ibPrevious);
        etSong = view.findViewById(R.id.etSong);
        etSearch = view.findViewById(R.id.etSearch);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnSearch = view.findViewById(R.id.btnSearch);
        ibQueue = view.findViewById(R.id.ibQueue);
        queue = new ArrayList<>();
        recents = new ArrayList<>();
        searches = new ArrayList<>();

        ibPlayPause.setOnClickListener(this);
        ibNext.setOnClickListener(this);
        ibPrevious.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        ibQueue.setOnClickListener(this);
        btnSearch.setOnClickListener(this);

        authenticateSpotify();
        sharedPreferences = requireActivity().getSharedPreferences("SPOTIFY", 0);
        requestQueue = Volley.newRequestQueue(requireContext());
        getRecents();

        rvSearch = view.findViewById(R.id.rvSearch);
        adapter = new SongSearchAdapter(getContext(), searches, this);
        rvSearch.setAdapter(adapter);
        rvSearch.setLayoutManager(new LinearLayoutManager(getContext()));
        setRvVisibility();
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.ibPlayPause)
            onClickPlayPause();
        if (v.getId() == R.id.ibNext)
            onClickNext();
        if (v.getId() == R.id.ibPrevious)
            onClickPrevious();
        if (v.getId() == R.id.btnAdd)
            onClickAdd(etSong.getText().toString());
        if (v.getId() == R.id.ibQueue)
            onClickQueue();
        if (v.getId() == R.id.btnSearch);
            getSearch();
    }

    private void getSearch()
    {
        String q = etSearch.getText().toString();
        String type = "track";
        spotifyClient.search(q, type, new VolleyCallBack()
        {
            @Override
            public void onSuccess()
            {
                searches = spotifyClient.getSearches();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void getRecents()
    {
        spotifyClient.getRecentlyPlayed(new VolleyCallBack()
        {
            @Override
            public void onSuccess()
            {
                recents = spotifyClient.getRecents();
                recent = recents.get(0);
            }
        });
    }

    private void onClickPlayPause()
    {
        if (!playing)
        {
            spotifyAppRemote.getPlayerApi().resume();
            ibPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_outline);
            playing = true;
        }
        else
        {
            spotifyAppRemote.getPlayerApi().pause();
            ibPlayPause.setImageResource(R.drawable.ic_baseline_play_circle_outline);
            playing = false;
        }
    }

    private void onClickNext()
    {
        spotifyAppRemote.getPlayerApi().skipNext();
        updateTrack();
    }

    private void onClickPrevious()
    {
        spotifyAppRemote.getPlayerApi().skipPrevious();
        updateTrack();
    }

    private void onClickAdd(String queueUrl)
    {
        String uri = parseUrl(queueUrl);
        Log.i(TAG, "uri: " + uri);
        spotifyAppRemote.getPlayerApi().queue(uri);
        Toast.makeText(getContext(), "added to queue!", Toast.LENGTH_SHORT).show();
        etSong.setText("");
    }

    private void onClickQueue()
    {
        //TODO: SHOW QUEUE WHEN CLICKED
    }

    public void queueSearch(Song song)
    {
        spotifyAppRemote.getPlayerApi().queue("spotify:track:" + song.getId());
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (started && playing)
            ibPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_outline);
        else
            ibPlayPause.setImageResource(R.drawable.ic_baseline_play_circle_outline);

        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true) // auto shows authorization view if user hasn't approved scope
                .build();
        
        if (!SpotifyAppRemote.isSpotifyInstalled(getContext()))
        {
            Toast.makeText(getContext(), "you must have Spotify installed!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        SpotifyAppRemote.connect(getContext(), connectionParams, new Connector.ConnectionListener()
        {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemoteConnected)
            {
                spotifyAppRemote = spotifyAppRemoteConnected;
                Log.i(TAG, "spotifyAppRemote connected!");
                connected(); // start interacting with App Remote
            }

            @Override
            public void onFailure(Throwable error)
            {
                Log.e(TAG, "spotifyAppRemote connection failure: ", error);
            }
        });
    }

    private void connected()
    {
        if (!started)
        {
            String userUri = parseUrl(room.getMusic());
            if (!userUri.isEmpty())
            {
                Log.i(TAG, "starting URI: " + userUri);
                spotifyAppRemote.getPlayerApi().play(userUri);
            }
            ibPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_outline);
            started = true;
        }
        updateTrack();
        playing = true;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        SpotifyAppRemote.disconnect(spotifyAppRemote);
    }

    private void updateTrack()
    {
        spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState ->
        {
            Track currTrack = playerState.track;
            if (currTrack != null)
            {
                Log.i(TAG, "song: " + currTrack.name + "; artist: " + currTrack.artist.name);
                tvSong.setText(currTrack.name);
                tvArtist.setText(currTrack.artist.name);
                CallResult<Bitmap> albumResult = spotifyAppRemote.getImagesApi().getImage(currTrack.imageUri);
                albumResult.setResultCallback(new CallResult.ResultCallback<Bitmap>()
                {
                    @Override
                    public void onResult(Bitmap data)
                    {
                        Glide.with(requireContext()).asBitmap().load(data).into(ivAlbum);
                    }
                });
            }
        });
    }

    private void authenticateSpotify()
    {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{scopes});
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(getActivity(), REQUEST_CODE, request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE)
        {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
            switch (response.getType())
            {
                case TOKEN: // worked!
                    SharedPreferences.Editor editor = requireContext().getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    Log.i(TAG, "got access token!");
                    editor.apply();
                    break;
                case ERROR: // error :(
                    Log.e(TAG, "authorization flow error: ");
                    break;
                default:
                    break;
            }
        }
    }

    private void setRvVisibility()
    {
        if (etSearch.isFocused())
            rvSearch.setVisibility(View.VISIBLE);
        else
            rvSearch.setVisibility(View.GONE);
    }

    private String parseUrl(String url)
    {
        String uri = "spotify:";
        if (!started) // album/playlist URL submissions in room creation
        {
            if (url.contains("track"))
                uri += "track:";
            else if (url.contains("album"))
                uri += "album:";
            else if (url.contains("playlist"))
                uri += "playlist:";
        }
        else
            if (url.contains("album") || url.contains("playlist"))
                Toast.makeText(getContext(), "link must be to a song!", Toast.LENGTH_SHORT).show();
        uri += url.substring(url.lastIndexOf("/") + 1, url.indexOf("?"));
        return uri;
    }
}