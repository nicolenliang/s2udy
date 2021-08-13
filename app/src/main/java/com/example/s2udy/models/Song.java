package com.example.s2udy.models;

public class Song
{
    String id, name, artist, albumUrl;

    public Song(String id, String name, String artist, String albumUrl)
    {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.albumUrl = albumUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbumUrl() { return albumUrl; }
    public void setAlbumUrl(String albumUrl) { this.albumUrl = albumUrl; }
}
