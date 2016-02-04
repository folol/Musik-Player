package com.example.sky.myapplication;



import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    ListView songView;
    public ArrayList<Song> songList;
    HashMap<Long,Integer> hm;
    SongAdapter songAdt;
    static int mSongViewMode;
    QuerySongList qsl;
    String[] mProjection;
    String sortingType,sortingOrder;
    SwitchCompat switchMode ;
    MainActivity ma;
    View mView;
    MusicService musicSrv;

    public SongsFragment() {
        // Required empty public constructor
        mProjection = new String[1];
    }


    @Override
    public void onActivityCreated(Bundle savedInstance){
        musicSrv = MusicService.getInstance();
        super.onActivityCreated(savedInstance);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.songfragment, container, false);
        songView = (ListView)rootView.findViewById(R.id.songfragmentSongList);

        switchMode = (SwitchCompat)rootView.findViewById(R.id.switch_mode);
        ma = (MainActivity)getActivity();
        hm = ma.getSongCounter();
        switchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        MusicService.setPlayMode(true);
                      //  Log.i("Musik", "switched button clicked enqueue");
                    }
                    else {
                        MusicService.setPlayMode(false);
                     //   Log.i("Musik", "switched button clicked play");
                    }
            }
        });
        songList = new ArrayList<Song>();
        getSongList();

        mListener.onFragmentInteraction(songList);
        songAdt = new SongAdapter(getActivity(), songList,hm,ma.getSongViewMode());              //SongAdapter is to bridge the link between ListView(songView) and data behind it
        songView.setAdapter(songAdt);
        registerForContextMenu(songView);
        return  rootView;

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
      //  Log.i("Musik","play sf "+menuInfo);
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = (getActivity()).getMenuInflater();
        //Log.i("Musik","cm "+mSongViewMode);
        mSongViewMode = ma.getSongViewMode();
        switch(mSongViewMode){
            case 0:            //Song mode
                inflater.inflate(R.menu.context_main_list_menu, menu);
                break;
            default:        //Album , artist , genre , playlist
                inflater.inflate(R.menu.context_main_list_menu_othermode, menu);
                break;
            //File browser mode should have its own menu
        }
        //mView = v;

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        mView = info.targetView;
        int position = Integer.parseInt(mView.getTag().toString());
        switch (item.getItemId()) {
            case R.id.action_play:
               // Log.i("Musik","play sf");
             //   Log.i("Musik"," "+ma+" "+mView.getId());
                playFromContextMenu(position);
                return true;
            case R.id.action_enqueue:
                enqueueFromContextMenu(position);
                return true;
            case R.id.action_ringtone:
                makeRingtone(position);
                return true;
            case R.id.action_share:
                //This will be app specific ....
                // will require networking part also...
                return true;
            case R.id.action_delete:
                /* There is a bug here that if songListToPlay
                becomes null then what happens ??
                 */
                deleteSong(position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }




    public  void getSongList(){
        mSongViewMode = ma.getSongViewMode();
        switch(mSongViewMode){
            case 0:
                qsl = new QuerySongList(getContext(),null,null,null,"SONG");
                qsl.findSongs();
                songList = qsl.getList();
                sortSongs();
                break;
            case 1:
                mProjection[0] = "DISTINCT " + android.provider.MediaStore.Audio.Media.ARTIST;
                qsl = new QuerySongList(getContext(), mProjection, null, null, "ARTIST");
                qsl.findSongs();
                songList = qsl.getList();
                sortArtist();
                break;
            case 2:
                mProjection[0] = "DISTINCT "+android.provider.MediaStore.Audio.Media.ALBUM;
                QuerySongList qsl = new QuerySongList(getContext(),mProjection,null,null,"ALBUM");
                qsl.findSongs();
                songList = qsl.getList();
                sortAlbum();
                break;
            case 3:
                String[] proj1 = {MediaStore.Audio.Genres.NAME, MediaStore.Audio.Genres._ID};
                qsl = new QuerySongList(getContext(),proj1,null,null,"GENRE");
                songList = qsl.getGenreList();
                sortGenre();
                break;
            default:
                break;
        }



    }

    void sortSongs(){
        sortingType = ma.getSortingType();
        sortingOrder = ma.getSortingOrder();
        switch(sortingType){
            case "ALPHA":
                if(sortingOrder.equals("ASC")){
                    Collections.sort(songList, new Comparator<Song>() {
                        public int compare(Song a, Song b) {
                            return a.getTitle().compareTo(b.getTitle());
                        }
                    });
                }
                else{
                    Collections.sort(songList, new Comparator<Song>() {
                        public int compare(Song a, Song b) {
                            return b.getTitle().compareTo(a.getTitle());
                        }
                    });
                }
                break;
            case "DATE":
                if(sortingOrder.equals("ASC")){
                    Collections.sort(songList, new Comparator<Song>() {
                        public int compare(Song a, Song b) {
                            return a.getDateAdded().compareTo(b.getDateAdded());
                        }
                    });
                }
                else{
                    Collections.sort(songList, new Comparator<Song>() {
                        public int compare(Song a, Song b) {
                            return b.getDateAdded().compareTo(a.getDateAdded());
                        }
                    });
                }
                break;
            case "COUNT":
                //This feature has not been added yet
                break;
        }
    }

    void sortArtist(){
        sortingOrder = ma.getSortingOrder();
        if(sortingOrder.equals("ASC")){
            Collections.sort(songList, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                    return a.getArtist().compareTo(b.getArtist());
                }
            });
        }
        else{
            Collections.sort(songList, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                    return b.getArtist().compareTo(a.getArtist());
                }
            });
        }
    }

    void sortAlbum(){
        sortingOrder = ma.getSortingOrder();
        if(sortingOrder.equals("ASC")){
            Collections.sort(songList, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                    return a.getAlbum().compareTo(b.getAlbum());
                }
            });
        }
        else{
            Collections.sort(songList, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                    return b.getAlbum().compareTo(a.getAlbum());
                }
            });
        }
    }

    void sortGenre(){
        sortingOrder = ma.getSortingOrder();
        if(sortingOrder.equals("ASC")){
            Collections.sort(songList, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                    return a.getTitle().compareTo(b.getTitle());
                }
            });
        }
        else{
            Collections.sort(songList, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                    return b.getTitle().compareTo(a.getTitle());
                }
            });
        }
    }

    public void reSort(){
        mSongViewMode = ma.getSongViewMode();
        switch ((mSongViewMode)) {
            case 0:
                sortSongs();
                break;
            case 1:
                sortArtist();
                break;
            case 2:
                sortAlbum();
                break;
            case 3:
                sortGenre();
                break;
        }
        dataChanged(null);
    }

    void playFromContextMenu(int position){
        mSongViewMode = ma.getSongViewMode();
        if(mSongViewMode == 0)
            ma.setUpSongListToPlay(position, 0);
        else if(mSongViewMode == 1){
            String artist[] = new String[1];
            artist[0] = songList.get(position).getArtist();
            QuerySongList qsl = new QuerySongList(ma, null,MediaStore.Audio.Media.ARTIST +" = ?", artist, "SONG");
            qsl.findSongs();
            musicSrv.setList(qsl.getList());
            musicSrv.setSong(0);
            musicSrv.playSong();
        }
        else if(mSongViewMode == 2){
            String album[] = new String[1];
            album[0] = songList.get(position).getAlbum();
            QuerySongList qsl = new QuerySongList(ma, null,MediaStore.Audio.Media.ALBUM +" = ?", album, "SONG");
            qsl.findSongs();
            musicSrv.setList(qsl.getList());
            musicSrv.setSong(0);
            musicSrv.playSong();
        }
        else if(mSongViewMode == 3){
            String genreName = songList.get(position).getTitle();
            long genre_id = songList.get(position).getID();
            QuerySongList qsl = new QuerySongList(ma,null,null,null,null);
            qsl.getGenreSpecificList(genre_id);
            musicSrv.setList(qsl.getList());
            musicSrv.setSong(0);
            musicSrv.playSong();
        }
    }

    void enqueueFromContextMenu(int position){
        if(!MusicService.isHasPlayed()){
            playFromContextMenu(position);
        }
        else {
            mSongViewMode = ma.getSongViewMode();
            if (mSongViewMode == 0)
                ma.setUpSongListToPlay(Integer.parseInt(mView.getTag().toString()), 1);
            else if (mSongViewMode == 1) {
                String artist[] = new String[1];
                artist[0] = songList.get(position).getArtist();
                QuerySongList qsl = new QuerySongList(ma, null,MediaStore.Audio.Media.ARTIST +" = ?", artist, "SONG");
                qsl.findSongs();
              //  qsl.getList();
                musicSrv.addListOfSongs(qsl.getList());
            }
            else if(mSongViewMode == 2){
                String album[] = new String[1];
                album[0] = songList.get(position).getAlbum();
                QuerySongList qsl = new QuerySongList(ma, null,MediaStore.Audio.Media.ALBUM +" = ?", album, "SONG");
                qsl.findSongs();
              //  qsl.getList();
                musicSrv.addListOfSongs(qsl.getList());
            }
            else if(mSongViewMode == 3){
                String genreName = songList.get(position).getTitle();
                long genre_id = songList.get(position).getID();
                QuerySongList qsl = new QuerySongList(ma,null,null,null,null);
                qsl.getGenreSpecificList(genre_id);
                musicSrv.addListOfSongs(qsl.getList());
            }
        }
        Toast.makeText(ma, "Songs Enqueued", Toast.LENGTH_SHORT).show();
    }

    public void makeRingtone(int position){
        Song s = songList.get(position);
        long currSongId = s.getID();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSongId);
        String path = getRealPathFromURI(ma,trackUri);
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
       // Log.i("Musik","uri - "+uri+" path- "+path);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, path);
        values.put(MediaStore.MediaColumns.TITLE, s.getTitle());
        values.put(MediaStore.MediaColumns.MIME_TYPE, s.getMimeType());
        values.put(MediaStore.Audio.Media.ARTIST, s.getArtist());
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        ma.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + path + "\"", null);
        Uri newUri = ma.getContentResolver().insert(uri, values);

        RingtoneManager.setActualDefaultRingtoneUri(ma,
                RingtoneManager.TYPE_RINGTONE, newUri);
    }



    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }



    void deleteSong(int position){
        /* There are two bug here
        1 - When app is lauched and we delete a song that is in song list saved to play
        2 - When a album/artist/genre is deleted in other than song mode and one of its song
         has been played from main activity in song mode
         */
        if(!MusicService.isHasPlayed() && musicSrv != null){
            if(musicSrv.getList() == null){
                ma.setListToMusicSrv();
            }
        }
        if(mSongViewMode == 0 && musicSrv != null) {
            Song s = songList.get(position);
            Uri trackUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    s.getID());
            String path = getRealPathFromURI(ma, trackUri);
            if(musicSrv.removeSong(s,path)) {
                ma.getContentResolver().delete(trackUri, null, null);
                ma.deleteMediaFile(s);
            }
        }
        else if(mSongViewMode == 1 && musicSrv != null){
            String artist[] = new String[1];
            artist[0] = songList.get(position).getArtist();
            QuerySongList qsl = new QuerySongList(ma, null,MediaStore.Audio.Media.ARTIST +" = ?", artist, "SONG");
            qsl.findSongs();
            ArrayList<Song> songListToDelete = new ArrayList<>();
            songListToDelete = qsl.getList();
            if(!songListToDelete.isEmpty()){
                for(Song s:songListToDelete){
                    //Song song = songList.get(position);
                    Uri trackUri = ContentUris.withAppendedId(
                            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            s.getID());
                    String path = getRealPathFromURI(ma, trackUri);
                    if(musicSrv.removeSong(s,path)) {
                        ma.getContentResolver().delete(trackUri, null, null);
                    }
                }
                ma.deleteMediaFile(songList.get(position));
            }
            else{
                Toast.makeText(ma, "No files to  delete", Toast.LENGTH_SHORT).show();
            }
        }
    }




    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(ArrayList<Song> songList);
    }

    public void dataChanged(ArrayList<Song> s){
        if(s != null)
            songList = s;
      //  reSort();                  //The changed list should be sorted acc to sorting pref.
        songAdt.dataChanged(songList);
        songAdt.updateScrollBarSectionView(ma.getSongViewMode());
        songAdt.notifyDataSetChanged();
    }




}
