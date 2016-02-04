package com.example.sky.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Devansh on 11/8/2015.
 */
public class SetUpCounter extends AsyncTask<Void , Void , String> {

    HashMap<Long,Integer> songCounter;
    Activity mActivity;
    ArrayList<Song> sl;
    int flag;
    boolean otherMode;

    SetUpCounter(HashMap<Long,Integer> hm,ArrayList<Song> al,Activity a){
        mActivity = a;
        songCounter = hm;
        sl = al;
        otherMode = false;
    }

    @Override
    protected String doInBackground(Void... v) {

        if(sl == null){
            QuerySongList qsl = new QuerySongList(mActivity,null,null,null,"SONG");
            qsl.findSongs();
            sl = qsl.getList();
            otherMode =true;
        }

            if(songCounter == null){
                songCounter = new HashMap<>();
                for(int i=0;i<sl.size();i++){
                    songCounter.put(sl.get(i).getID(),0);
                }
            }
            else{
                Set<Long> s = songCounter.keySet();
                Iterator<Long> iter = s.iterator();
                while(iter.hasNext()){
                    flag = 0;
                    Long l = iter.next();
                    for(Song sg:sl){
                        if(sg.getID() == l){
                            flag = 1;
                            break;
                        }
                    }
                    if(flag == 0)
                        iter.remove();
                }

                for(int i=0;i<sl.size();i++){
                    if(!songCounter.containsKey(sl.get(i).getID())){
                        songCounter.put(sl.get(i).getID(),0);
                    }
                }
            }
            return "Success";
    }

    @Override
    protected void onPostExecute(String s) {
        MainActivity mac = (MainActivity) mActivity;
        mac.setSongCounter(songCounter);
        if(otherMode) {
            mac.setRealSongList(sl);
        }
            mac.getSavedSongListToPlay();
            mac.setUpBottomView();
        if(!sl.isEmpty())
            Song.setFirstSongId(sl.get(0).getID());

    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... ss) {}
}
