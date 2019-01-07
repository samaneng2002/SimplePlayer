package com.example.android.simpleplayer;

public class Song {

    private long mId;
    private String mTitle;
    private String mArtist;

    public Song(long songID, String songTitle, String songArtist) {
        mId=songID;
        mTitle=songTitle;
        mArtist=songArtist;
    }

    public long getID(){return mId;}
    public String getTitle(){return mTitle;}
    public String getArtist(){return mArtist;}
}
