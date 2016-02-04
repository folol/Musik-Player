package com.example.sky.myapplication;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ArtistSpecific extends AppCompatActivity {

    ListView songListView;
    ArtistSpecificAdapter artSpecAdt;
    ArrayList<Song> songArtistSpecificList, songListToPlay;
    String type;
    String mSelection;
    String[] typeName;
    private HorizontalScrollView mLimiterScroller;
    private ViewGroup mLimiterViews;
    private TextView mTitle,mArtist,mNext,mPlayPause;
    private ImageView mCover;
    View mView;
    private MusicService musicSrv;
    private long songId,genre_id = -10L;
    private int songIdPostion;
    public static final String BOTTOM_VIEW = "bottom_view",ACTION_DEF="default";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_specific);
        Toolbar toolbar = (Toolbar) findViewById(R.id.artist_specificToolbar);
        setSupportActionBar(toolbar);
        if(musicSrv == null) {
            musicSrv = MusicService.getInstance();
        }
        Intent data = getIntent();
        songId = data.getLongExtra("AS_SONG_PLAYED",Song.getFirstSongId());
        typeName = new String[1];
        songArtistSpecificList = new ArrayList<Song>();          //Artist specific song list
        songListToPlay = new ArrayList<>();               //Song list of all songs playing
        Bundle b = data.getBundleExtra("LIST_BUNDLE");
        if (b != null) {
            songListToPlay = b.getParcelableArrayList("SONG_LIST");
        }

        type = data.getCharSequenceExtra("TYPE").toString();
        typeName[0] = data.getCharSequenceExtra("TYPE_NAME").toString();
        genre_id = data.getLongExtra("GENRE_ID", -10L);
        songIdPostion = data.getIntExtra("SongIdPosition",0);


        if(type.equals("ARTIST"))
            mSelection = MediaStore.Audio.Media.ARTIST +" = ?";
        else if(type.equals("ALBUM"))
            mSelection = MediaStore.Audio.Media.ALBUM +" = ?";
        else    mSelection = null;

        getSupportActionBar().setTitle(typeName[0]);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        songListView = (ListView) findViewById(R.id.artist_specificSongList);
        getSongList();

        if(!songArtistSpecificList.isEmpty()){
            Collections.sort(songArtistSpecificList, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
            });


             artSpecAdt = new ArtistSpecificAdapter(this, songArtistSpecificList,type);
             songListView.setAdapter(artSpecAdt);
            registerForContextMenu(songListView);
        }

        mLimiterScroller = (HorizontalScrollView)findViewById(R.id.artist_specific_limiter_scroller);
        mLimiterViews = (ViewGroup)findViewById(R.id.artist_specific_limiter_layout);
        View bottomView = getLayoutInflater().inflate(R.layout.artist_specific_bottom_song_view,null);
        mTitle = (TextView)bottomView.findViewById(R.id.as_title);
        mArtist = (TextView)bottomView.findViewById(R.id.as_artist);
        mCover = (ImageView)bottomView.findViewById(R.id.as_cover);
        mNext = (TextView)bottomView.findViewById(R.id.as_next);
        mPlayPause = (TextView)bottomView.findViewById(R.id.as_play_pause);
        mLimiterViews.addView(bottomView);

        if(musicSrv.isPng())
            mPlayPause.setText("PAUSE");
        else
            mPlayPause.setText("PLAY");
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicSrv != null) {
                    if (MusicService.isHasPlayed())
                        musicSrv.playNext();
                    else {
                        musicSrv.setList(songListToPlay);
                        musicSrv.setSong(songIdPostion);
                        musicSrv.playNext();
                    }
                }
            }
        });
        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!MusicService.isHasPlayed()){
                    mPlayPause.setText("PAUSE");
                    musicSrv.setList(songListToPlay);
                    musicSrv.setSong(songIdPostion);
                    musicSrv.playSong();
                }
                else {
                    String textValue = mPlayPause.getText().toString();
                    if( textValue.equals("PLAY")) {
                        mPlayPause.setText("PAUSE");
                        musicSrv.go();
                    }
                    else{
                        mPlayPause.setText("PLAY");
                        musicSrv.pausePlayer();
                    }
                }
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("SONG_PLAYED"));
        setUpBottomView();

    }

    private int findSongPosition(){
        int count = 0;
        while(count < songListToPlay.size()){
            Song s = songListToPlay.get(count);
            if(songId == s.getID())
                break;
            count++;
        }
        if(count >= songListToPlay.size())
            count = 0;
        return count;

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override

        public void onReceive(Context context, Intent intent) {
            songId = intent.getLongExtra("SONG_PLAYED", Song.getFirstSongId());
            if(musicSrv == null)
                musicSrv = MusicService.getInstance();
            setUpBottomView();
        }
    };

    private void setUpBottomView(){
        /*
        if(songArtistSpecificList != null && MusicService.hasInstance()) {
            MediaMetadataRetriever mr = new MediaMetadataRetriever();

            Uri trackUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    songId);

            // Log.i("mc", "song - " + songPosition + songListToPlay);
            try {
                mr.setDataSource(this, trackUri);
                mTitle.setText(mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                mArtist.setText(mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                byte[] art = mr.getEmbeddedPicture();
                Bitmap songImage = BitmapFactory
                        .decodeByteArray(art, 0, art.length);
                mCover.setImageBitmap(songImage);
            }catch (Exception e){
                mCover.setImageResource(R.drawable.play);
            }
            mLimiterScroller.setVisibility(View.VISIBLE);
        }
        else{
            mLimiterScroller.setVisibility(View.GONE);
        }
        */
        if(songListToPlay != null && !songListToPlay.isEmpty() ) {
            if (!MusicService.isHasPlayed()) {// || musicSrv == null) {
                //musicSrv == null) {
                //  songId = settings.getLong("Song_last_playedd", Song.getFirstSongId());
                //   Log.i("Musik","null part music service object "+musicSrv);
                MediaMetadataRetriever mr = new MediaMetadataRetriever();
                try {
                    Uri trackUri = ContentUris.withAppendedId(
                            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            songId);
                    mr.setDataSource(this, trackUri);
                    byte[] art = mr.getEmbeddedPicture();
                    Bitmap songImage = BitmapFactory
                            .decodeByteArray(art, 0, art.length);
                    mTitle.setText(mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                    mArtist.setText(mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                    mCover.setImageBitmap(songImage);
                } catch (Exception e) {
                    mCover.setImageResource(R.drawable.default_albumart);
                }

            } else {
                //  Log.i("Musik","music service object "+musicSrv);// + " "+musicSrv.getCurrentTitle());
                mTitle.setText(musicSrv.getCurrentTitle());
                mArtist.setText(musicSrv.getCurrentArtist());
                mCover.setImageBitmap(musicSrv.getCurrentSongCover());
                // mLimiterScroller.setVisibility(View.VISIBLE);
            }
            if(mLimiterViews.getVisibility() == View.GONE)
                mLimiterViews.setVisibility(View.VISIBLE);
        }
        else{
        //    Log.i("Musik"," "+songListToPlay+" "+songListToPlay.size());
            mLimiterViews.setVisibility(View.GONE);
        }
    }

    public void clickFromBottomView(View view){
        Intent bottomIntent = new Intent(this,FullPlaybackActivity.class);
        bottomIntent.setAction(BOTTOM_VIEW);
       /*
        bottomIntent.putExtra("LAST_PLAYED", songId);
        if(!MusicService.isHasPlayed()){

            Bundle b = new Bundle();
            b.putParcelableArrayList("SONG_LIST", songListToPlay);
            bottomIntent.putExtra("LIST_BUNDLE", b);
        }
        */
        if(!MusicService.isHasPlayed()){
            musicSrv.setList(songListToPlay);
            musicSrv.setSong(songIdPostion);
        }
        startActivity(bottomIntent);
    }

    public void getSongList(){

        if(type.equals("GENRE")) {
            if (genre_id != -10) {


                Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", genre_id);

                String[] projection = new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media._ID, android.provider.MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM};

                Cursor musicCursor = getContentResolver().query(uri, projection, null, null, null);

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
                    int dateAdded = musicCursor.getColumnIndex
                            (MediaStore.Audio.Media.DATE_ADDED);
                    //add songs to list
                    do {
                        long thisId = musicCursor.getLong(idColumn);
                        String thisTitle = musicCursor.getString(titleColumn);
                        String thisArtist = musicCursor.getString(artistColumn);
                        String thisAlbum = musicCursor.getString(albumColumn);
                        songArtistSpecificList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum,null));
                    }
                    while (musicCursor.moveToNext());


                }
            }
        }
        else {
            QuerySongList qsl = new QuerySongList(this, null, mSelection, typeName, "SONG");
            qsl.findSongs();
            songArtistSpecificList = qsl.getList();
        }

    }

    public void clickFromArtistSpecific(View view){
        int songToPlay = Integer.parseInt(view.getTag().toString());
        setUpSongListToPlay(songToPlay, MusicService.getPlayMode());       //Start here
       // setUpBottomView();
        /*
        Intent data = new Intent(this,FullPlaybackActivity.class);
        data.setAction(ACTION_DEF);
        data.putExtra("SONG_TO_PLAY", songToPlay);
        Bundle b = new Bundle();
        b.putParcelableArrayList("SONG_LIST", songArtistSpecificList);
        data.putExtra("LIST_BUNDLE", b);
        startActivity(data);
        */
    }

    public void setUpSongListToPlay(int position,int i){
        //int i = MusicService.getPlayMode();
       // Log.i("Musik", "mode - " + i);
        if(i == 0) {         //Play mode
            if(songListToPlay == null)
                songListToPlay = new ArrayList<>();
            songListToPlay.clear();
            songListToPlay.add(songArtistSpecificList.get(position));
            musicSrv.setList(songListToPlay);
            musicSrv.setSong(0);
            musicSrv.playSong();
          //  Log.i("Musik","as play mode "+songListToPlay.size());

        }
        else{    //Enqueue mode
            if(songListToPlay == null)
                songListToPlay = new ArrayList<>();
            if(musicSrv.isPng()) {
                songListToPlay.add(songArtistSpecificList.get(position));
                musicSrv.setList(songListToPlay);
                //musicSrv.setSong(position);
                //musicSrv.playSong();
           //     Log.i("Musik","as enq mode playing "+songListToPlay.size());
            }
            else {
                songListToPlay.clear();
                songListToPlay.add(songArtistSpecificList.get(position));
                musicSrv.setList(songListToPlay);
                musicSrv.setSong(0);
                musicSrv.playSong();
            //    Log.i("Musik", "as enq mode not playing "+songListToPlay.size());
            }

            Toast.makeText(this, "One Song Enqueued", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
       // Log.i("Musik","play sf");
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_main_list_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        mView = info.targetView;
        int position = Integer.parseInt(mView.getTag().toString());
        switch (item.getItemId()) {
            case R.id.action_play:
              //  Log.i("Musik","play sf");
                setUpSongListToPlay(position,0);
                return true;
            case R.id.action_enqueue:
                if(!MusicService.isHasPlayed())
                    setUpSongListToPlay(position,0);
                else
                    setUpSongListToPlay(Integer.parseInt(mView.getTag().toString()), 1);
                return true;
            case R.id.action_ringtone:
                makeRingtone(position);
                return true;
            case R.id.action_share:

                return true;
            case R.id.action_delete:
                deleteSong(position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void makeRingtone(int position){
        Song s = songArtistSpecificList.get(position);
        long currSongId = s.getID();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSongId);
        String path = getRealPathFromURI(this,trackUri);
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

        getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + path + "\"", null);
        Uri newUri = getContentResolver().insert(uri, values);

        RingtoneManager.setActualDefaultRingtoneUri(this,
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
        2 - When a song is deleted that has been played from main activity in song mode
         */
        if(!MusicService.isHasPlayed() && musicSrv != null){
            if(musicSrv.getList() == null){
                setListToMusicSrv();
            }
        }
        if(musicSrv != null) {
            Song s = songArtistSpecificList.get(position);
            Uri trackUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    s.getID());
            String path = getRealPathFromURI(this, trackUri);
            if(musicSrv.removeSong(s,path)) {
                getContentResolver().delete(trackUri, null, null);
                deleteMediaFile(s);
            }

        }
    }

    public void deleteMediaFile(Song s){
        //songList.remove(s);
        songArtistSpecificList.remove(s);
        //songFragmentDataChanged(songList);
        songListToPlay = musicSrv.getList();
        artSpecAdt.dataChanged(songArtistSpecificList);
        artSpecAdt.notifyDataSetChanged();
        Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show();
    }

    public void setListToMusicSrv(){
        musicSrv.setList(songListToPlay);
        musicSrv.setSong(songIdPostion);
    }



    @Override
    protected void onDestroy(){
      //  Log.i("Musik","as ondestroy ");
        musicSrv = null;
        super.onDestroy();
    }
    @Override
    protected void onResume(){
        if(musicSrv != null) {
            if (musicSrv.isPng())
                mPlayPause.setText("PAUSE");
            else
                mPlayPause.setText("PLAY");
        }
        else{
            musicSrv = MusicService.getInstance();
        }
    //    Log.i("Musik", "as onresume " + musicSrv);

        if(MusicService.isHasPlayed())
            songListToPlay = musicSrv.getList();

        super.onResume();
    }

    @Override
    protected void onStart(){
        if(musicSrv == null)
            musicSrv = MusicService.getInstance();
    //    Log.i("Musik","as onstart "+musicSrv);
        super.onStart();
    }

    @Override
    protected void onRestart(){
        if(musicSrv == null)
            musicSrv = MusicService.getInstance();
     //   Log.i("Musik","as onrestart "+musicSrv);
        super.onRestart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}
