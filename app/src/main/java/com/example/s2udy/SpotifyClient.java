package com.example.s2udy;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.s2udy.models.Song;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpotifyClient
{
    public static final String TAG = "SpotifyClient";
    public static final String TOKEN = "BQAUREjQDMLbMYIUWq-msJsEfkfiv-YX1H6QhkG0EHKQCBQRMJqPIiE4UmjJWVnSTqQX4TEITFHB5tfyszil6hH9OpqqkTRJ_xBLeE9426W-Jd_MRL0z19bZbkcK4JzeEDkirln_0StP1_puzfdS0_WLVfGzSA";
    List<Song> recents =  new ArrayList<>();
    List<Song> queue = new ArrayList<>();
    List<Song> searches = new ArrayList<>();
    SharedPreferences sharedPreferences;
    RequestQueue requestQueue;

    public SpotifyClient(Context context)
    {
        sharedPreferences = context.getSharedPreferences("SPOTIFY", 0);
        requestQueue = Volley.newRequestQueue(context);
    }

    public List<Song> getRecents() { return recents; }
    public List<Song> getQueue() { return queue; }
    public List<Song> getSearches() { return searches; }

    public List<Song> getRecentlyPlayed(final VolleyCallBack volleyCallBack)
    {
        String endpoint = "https://api.spotify.com/v1/me/player/recently-played";
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, endpoint, null, response ->
        {
            // Gson converts JSON obj to equivalent Java obj, and vice versa
            Gson gson = new Gson();
            JSONArray jsonArray = response.optJSONArray("items"); // grabbing items array from api call
            for (int i = 0; i < jsonArray.length(); i++)
            {
                try
                {
                    JSONObject object = jsonArray.getJSONObject(i).optJSONObject("track"); // grabbing individual track from items
                    Song song = gson.fromJson(object.toString(), Song.class);
                    recents.add(song);
                }
                catch (JSONException e)
                { e.printStackTrace(); }
            }
            volleyCallBack.onSuccess();
        },
        error ->
        {
            Log.e(TAG, "error in retrieving recently played tracks: ", error);
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", TOKEN);
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        requestQueue.add(objectRequest);
        return recents;
    }

    public List<Song> search(String q, String type, final VolleyCallBack volleyCallBack)
    {
        String endpoint = "https://api.spotify.com/v1/search";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, endpoint, null, response ->
        {
            Gson gson = new Gson();
            JSONArray jsonArray = response.optJSONArray("items");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                try
                {
                    JSONObject object = jsonArray.getJSONObject(i).optJSONObject("album");
                    Song song = gson.fromJson(object.toString(), Song.class);
                    searches.add(song);
                }
                catch (JSONException e)
                { e.printStackTrace(); }
            }
            volleyCallBack.onSuccess();
        },
        error ->
        {
            Log.e(TAG, "error in searching", error);
        })
        {
            @Override
            public Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("q", q);
                params.put("type", type);
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", TOKEN);
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
        return searches;
    }
}
