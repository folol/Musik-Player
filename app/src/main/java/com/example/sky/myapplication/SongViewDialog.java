package com.example.sky.myapplication;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.ArrayList;


/**
 * Created by Devansh on 12/24/2015.
 */
public class SongViewDialog  extends DialogFragment {

    static int mSongViewMode = -1;
    QuerySongList qsl;
    String[] mProjection;
   // ArrayList<Song> songListToPlay;
    MainActivity ma;


    public SongViewDialog(){
//        mSongViewMode = ((MainActivity)getActivity()).getSongViewMode();
        mProjection = new String[1];
       // songListToPlay = new ArrayList<Song>();
    }

    @Override
        public void onActivityCreated(Bundle savedInstance){
            ma = (MainActivity)getActivity();
            mSongViewMode = ma.getSongViewMode();
            super.onActivityCreated(savedInstance);
        }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_mode)
                .setSingleChoiceItems(R.array.song_mode_array, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                      //  Log.i("Musik","Mode "+which);
                        if(which == mSongViewMode){
                            dialog.dismiss();
                        }
                        else {
                            switch (which) {
                                case 0:
                                    final Handler mHandler = ma.getUiHandler();
                                    new Thread(new Runnable() {

                                        // After call for background.start this run method call
                                        public void run() {
                                            qsl = new QuerySongList(getContext(),null, null, null, "SONG");
                                            qsl.findSongs();
                                            Message msg = mHandler.obtainMessage();
                                            Bundle b = new Bundle();
                                            b.putString("msg", "Song_View_Selected");
                                            b.putParcelableArrayList("SongList", qsl.getList());
                                            msg.setData(b);
                                            mHandler.sendMessage(msg);
                                            Log.i("Musik","Thread ran");
                                        }
                                    }).start();
                                    dialog.dismiss();

//                                            Bundle b = new Bundle();
//                                            b.putString("message","Song_View_Selected");
//                                            b.putParcelableArrayList("SongList", qsl.getList());
//                                      //      ma.runHandler(b);
                                     //       Log.i("Musik","Thread ran "+mHandler);

                                    break;
                                case 1:
                                    mProjection[0] = "DISTINCT " + android.provider.MediaStore.Audio.Media.ARTIST;
                                    qsl = new QuerySongList(getContext(), mProjection, null, null, "ARTIST");
                                    qsl.findSongs();
                                    ma.setSongViewMode(which);
                                    ma.songFragmentDataChanged(qsl.getList());
                                    dialog.dismiss();
                                    break;
                                case 2:
                                    mProjection[0] = "DISTINCT "+android.provider.MediaStore.Audio.Media.ALBUM;
                                    QuerySongList qsl = new QuerySongList(getContext(),mProjection,null,null,"ALBUM");
                                    qsl.findSongs();
                                    ma.setSongViewMode(which);
                                    ma.songFragmentDataChanged(qsl.getList());
                                    dialog.dismiss();
                                    break;
                                case 3:
                                    String[] proj1 = {MediaStore.Audio.Genres.NAME, MediaStore.Audio.Genres._ID};
                                    qsl = new QuerySongList(getContext(),proj1,null,null,"GENRE");
                                    ma.setSongViewMode(which);
                                    ma.songFragmentDataChanged(qsl.getGenreList());
                                    dialog.dismiss();
                                    break;
                                default:
                                    dialog.dismiss();
                                    break;


                            }
                        }
                    }
                });
        return builder.create();
    }
}
