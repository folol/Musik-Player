package com.example.sky.myapplication;

/**
 * Created by sky on 4/28/2015.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.BaseAdapter;


    public class SongAdapter extends BaseAdapter implements SectionIndexer,View.OnCreateContextMenuListener {
        private ArrayList<Song> songs;
        private LayoutInflater songInf;
        HashMap<String, Integer> mapIndex;
        HashMap<Long,Integer> counterMap;
        String[] sections;

        public SongAdapter(Context c, ArrayList<Song> theSongs,HashMap<Long,Integer> hm,int i){
            songs=theSongs;
            songInf=LayoutInflater.from(c);
            counterMap = hm;
            updateScrollBarSectionView(i);
        }

        public void updateScrollBarSectionView(int i){
            mapIndex = new LinkedHashMap<String, Integer>();
            switch(i) {
                case 0:
                    for (int x = 0; x < songs.size(); x++) {
                        String title = songs.get(x).getTitle();
                        if(title.isEmpty() == false) {
                            String ch = title.substring(0, 1);
                            ch = ch.toUpperCase(Locale.US);

                            // HashMap will prevent duplicates
                            mapIndex.put(ch, x);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < songs.size(); x++) {
                        String title = songs.get(x).getArtist();
                        if(title.isEmpty() == false) {
                            String ch = title.substring(0, 1);
                            ch = ch.toUpperCase(Locale.US);

                            // HashMap will prevent duplicates
                            mapIndex.put(ch, x);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < songs.size(); x++) {
                        String title = songs.get(x).getAlbum();
                        if(title.isEmpty() == false) {
                            String ch = title.substring(0, 1);
                            ch = ch.toUpperCase(Locale.US);

                            // HashMap will prevent duplicates
                            mapIndex.put(ch, x);
                        }
                    }
                    break;
                case 3:
                    for (int x = 0; x < songs.size(); x++) {
                        String title = songs.get(x).getTitle();
                        if(title.isEmpty() == false) {
                            String ch = title.substring(0, 1);
                            ch = ch.toUpperCase(Locale.US);

                            // HashMap will prevent duplicates
                            mapIndex.put(ch, x);
                        }
                    }
                    break;
            }
            Set<String> sectionLetters = mapIndex.keySet();

            // create a list from the set to sort
            ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);

            Collections.sort(sectionList);

            sections = new String[sectionList.size()];

            sectionList.toArray(sections);
        }

        public void dataChanged(ArrayList<Song> s){
            songs = s;
        }

        public int getPositionForSection(int section) {

            return  mapIndex.get(sections[section]);
        }

        public int getSectionForPosition(int position) {

            return 0;
        }

        public Object[] getSections() {
            return sections;
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //map to songfragment_adapter layout
            LinearLayout songLay = (LinearLayout)songInf.inflate
                    (R.layout.songfragment_adapter, parent, false);
            //get title and artist views
            TextView songView = (TextView)songLay.findViewById(R.id.song_title);
            TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
            TextView albumView = (TextView)songLay.findViewById(R.id.song_album);

            Song currSong = songs.get(position);

            songView.setText(currSong.getTitle());
            artistView.setText(currSong.getArtist());
            albumView.setText(currSong.getAlbum());
            //set position as tag
            songLay.setTag(position);
            songLay.setOnCreateContextMenuListener(this);        //So that songLay will become long clickable
            return songLay;
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            // empty implementation
        }

    }
