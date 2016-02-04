package com.example.sky.myapplication;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sky on 4/28/2015.
 */
public class Song implements Parcelable{
    public static long firstSongId;
    private long id;
    private String title , mimeType , artist , dateAdded;
    String album;


    public static void setFirstSongId(long id){
        firstSongId = id;
    }

    public static long getFirstSongId(){return firstSongId;}

    public void setMimeType(String s){mimeType = s;}

    public Song(long songID , String songTitle , String songArtist,String songAlbum,String dateAdd){
        id = songID;
        title = songTitle;
        artist = songArtist;
        album = songAlbum;
        dateAdded = dateAdd;

    }

    public Song(Parcel in){
        readFromParcel(in);
    }


    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getAlbum(){return album;}
    public String getMimeType(){return mimeType;}
    public String getDateAdded(){return dateAdded;}


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
        List<String> l  = new ArrayList<>();
        l.add(title);
        l.add(artist);
        l.add(album);

        dest.writeStringList(l);

    }


    public void readFromParcel(Parcel in){

        id = in.readLong();
        ArrayList<String> list = in.createStringArrayList();
        title = list.get(0);
        artist = list.get(1);
        album = list.get(2);
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public  Song createFromParcel(Parcel in) {
                    return new Song(in);
                }

                public  Song[] newArray(int size) {
                    return new Song[size];
                }
            };
}

