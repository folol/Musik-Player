package com.example.sky.myapplication;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Devansh on 10/16/2015.
 */
public class QuerySongList {
    ArrayList<Song> songList;
    ArrayList<Long> hidingList;
    String[] mProjection, mArguments;
    String mSelection;
    Context mActivity;
    final String source;


    QuerySongList(Context context, String[] projection, String selection, String[] arg, String s) {
        mActivity = context;
        mProjection = projection;
        mSelection = selection;
        mArguments = arg;
        source = s;
        songList = new ArrayList<Song>();
        hidingList = new ArrayList<>();
    }

    public ArrayList<Song> getList() {
        return songList;
    }

    public ArrayList<Song> getGenreList(){
        //String[] proj1 = {MediaStore.Audio.Genres.NAME, MediaStore.Audio.Genres._ID};
        Cursor genreCursor = mActivity.getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,  mProjection, null, null, null);
        if (genreCursor.moveToFirst()) {
            do {
                int value_index = genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);
                int id_index = genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID);
                long genreId = Long.parseLong(genreCursor.getString(id_index));
                songList.add(new Song(genreId,genreCursor.getString(value_index),null,null,null));
            }while(genreCursor.moveToNext());
        }
        genreCursor.close();
        return songList;
    }

    public void getGenreSpecificList(long genre_id) {
        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", genre_id);

        String[] projection = new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media._ID, android.provider.MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM};

        Cursor musicCursor = mActivity.getContentResolver().query(uri, projection, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ALBUM);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum,null));
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
    }

    public void findSongs() {
        ContentResolver musicResolver = mActivity.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, mProjection, mSelection, mArguments, null);
        switch (source) {
            case "SONG" :
                if (musicCursor != null && musicCursor.moveToFirst()) {
                    //get columns
                    int titleColumn = musicCursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Media.TITLE);
                    int idColumn = musicCursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Media._ID);
                    int artistColumn = musicCursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Media.ARTIST);
                    int albumColumn = musicCursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Media.ALBUM);
                    int mimeType = musicCursor.getColumnIndex
                            (MediaStore.Audio.Media.MIME_TYPE);
                    int dateAdded = musicCursor.getColumnIndex
                            (MediaStore.Audio.Media.DATE_ADDED);
                    int typeR = musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE);
                    int typeP = musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_PODCAST);
                    int typeA = musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_ALARM);
                    int typeN = musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_NOTIFICATION);
                    //add songs to list
                    do {
                        String types1 = musicCursor.getString(typeR)+musicCursor.getString(typeP)+musicCursor.getString(typeA)+musicCursor.getString(typeN);
                        if(types1.equals("0000")) {
                            long thisId = musicCursor.getLong(idColumn);
                            String thisTitle = musicCursor.getString(titleColumn);
                            String thisAlbum = musicCursor.getString(albumColumn);
                            String thisArtist = musicCursor.getString(artistColumn);
                            String thisDateAdded = musicCursor.getString(dateAdded);

                            if(thisTitle.equals("") || thisTitle.equals(null))
                                hidingList.add(thisId);
                            if(thisAlbum.equals("")||thisAlbum.equals(null))
                                thisAlbum = "<unknown>";
                            if(thisArtist.equals("")||thisArtist.equals(null))
                                thisArtist = "<unknown>";
                            if(thisAlbum.matches("(http:\\/\\/|https:\\/\\/)?(www.)?([a-zA-Z0-9 ()<>+\\-\\`\\~\\?\\\\*%$#@!?\\{\\}\\[\\]]+)[a-zA-Z0-9 ()<>+\\-\\`\\~\\?\\\\*%$#@!?\\{\\}\\[\\]]+]*\\.[a-zA-Z0-9]{2}.?([a-z]+)?"))
                                thisAlbum = "<unknown>";
                            if(thisArtist.matches("(http:\\/\\/|https:\\/\\/)?(www.)?([a-zA-Z0-9 ()<>+\\-\\`\\~\\?\\\\*%$#@!?\\{\\}\\[\\]]+)[a-zA-Z0-9 ()<>+\\-\\`\\~\\?\\\\*%$#@!?\\{\\}\\[\\]]+]*\\.[a-zA-Z0-9]{2}.?([a-z]+)?"))
                                thisArtist = "<unknown>";


                            songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum,thisDateAdded));
                        }
                    }
                    while (musicCursor.moveToNext());
                }
                break;
            case "ALBUM":

                if (musicCursor != null && musicCursor.moveToFirst()) {

                    int albumColumn = musicCursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Media.ALBUM);


                    do {

                        String thisAlbum = musicCursor.getString(albumColumn);
                        songList.add(new Song(-1,null,null,thisAlbum,null));
                    } while (musicCursor.moveToNext());
                }

                break;

            case "ARTIST":
                if (musicCursor != null && musicCursor.moveToFirst()) {

                    int artistColumn = musicCursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Media.ARTIST);


                    do {

                        String thisArtist = musicCursor.getString(artistColumn);

                        songList.add(new Song(-1,null,thisArtist,null,null));
                    } while (musicCursor.moveToNext());
                }
                break;

            case "GENRE":
                break;


            default:
                songList = null;
                break;

        }
        musicCursor.close();
    }
}
