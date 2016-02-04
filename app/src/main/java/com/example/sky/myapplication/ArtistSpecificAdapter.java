package com.example.sky.myapplication;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Devansh on 10/15/2015.
 */
public class ArtistSpecificAdapter extends BaseAdapter implements View.OnCreateContextMenuListener {

    private ArrayList<Song> songs;
    private LayoutInflater songInf;
    private String type;

    public ArtistSpecificAdapter(Context c, ArrayList<Song> theSongs,String t){
        songs=theSongs;
        songInf= LayoutInflater.from(c);
        type = t;
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void dataChanged(ArrayList<Song> s){
        songs = s;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to songfragment_adapter layout
        LinearLayout songLay = (LinearLayout)songInf.inflate
                (R.layout.artistspecific_adapter, parent, false);
        //get title and artist views
        TextView songView = (TextView)songLay.findViewById(R.id.artist_specificSongTitle);
        TextView secondView = (TextView)songLay.findViewById(R.id.artist_specificSongDetail);
        //get songfragment_adapter using position
        Song currSong = songs.get(position);
        //get title and artist strings
        songView.setText(currSong.getTitle());
        if(type.equals("ARTIST"))
            secondView.setText(currSong.getAlbum());
        else
            secondView.setText(currSong.getArtist());
        //set position as tag
        songLay.setOnCreateContextMenuListener(this);
        songLay.setTag(position);
        return songLay;
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        // empty implementation
    }
}
