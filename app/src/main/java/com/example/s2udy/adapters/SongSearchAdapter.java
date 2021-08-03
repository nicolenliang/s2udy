package com.example.s2udy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.s2udy.R;
import com.example.s2udy.fragments.MusicFragment;
import com.example.s2udy.models.Song;

import java.util.List;

public class SongSearchAdapter extends RecyclerView.Adapter
{
    MusicFragment musicFragment;
    Context context;
    List<Song> searches;

    public SongSearchAdapter(Context context, List<Song> searches, MusicFragment musicFragment)
    {
        this.context = context;
        this.searches = searches;
        this.musicFragment = musicFragment;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        Song song = searches.get(position);
        ((ViewHolder)holder).bind(song);
    }

    @Override
    public int getItemCount()
    {
        return searches.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView tvSong, tvArtist;
        ImageView ivAlbum;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvSong = itemView.findViewById(R.id.tvSong);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            ivAlbum = itemView.findViewById(R.id.ivAlbum);
        }

        public void bind(Song song)
        {
            tvSong.setText(song.getName());
            tvArtist.setText(song.getArtist());
            Glide.with(context).load(song.getAlbumUrl()).into(ivAlbum);
        }

        @Override
        public void onClick(View v)
        {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION)
            {
                Song song = searches.get(position);
                musicFragment.queueSearch(song);
            }
        }
    }
}
