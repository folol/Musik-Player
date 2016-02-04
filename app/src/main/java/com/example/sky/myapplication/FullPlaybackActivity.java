package com.example.sky.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
//import android.widget.MediaController;

//import com.MusicController;

import java.util.ArrayList;

public class FullPlaybackActivity extends AppCompatActivity implements VideoControllerView.MediaPlayerControl,MediaPlayer.OnPreparedListener  {

    ArrayList<Song> songListToPlay;
    public MusicService musicSrv;
    private boolean musicBound=true;
    //private MusicController controller;
    VideoControllerView controller;
    private boolean paused=false, playbackPaused=false;
    int songToPlayPosition;
    long id , song_count_curr;
    ImageView songCover;
    ListView currentList;
    public static final String BOTTOM_VIEW = "bottom_view",ACTION_DEF="default";
    private String MODE = "LAUNCHED";

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
    }

    @Override
    protected void onNewIntent(Intent i ){

        //This is called when user touch notification
        if(MusicService.hasInstance()) {
            getServiceInstance();
            songListToPlay = musicSrv.getList();
            songToPlayPosition = musicSrv.getCurrentSongPosition();
            getSupportActionBar().setTitle(songListToPlay.get(songToPlayPosition).getTitle());
            setCover();
            if(songCover.getVisibility() == View.GONE)
                songCover.setVisibility(View.VISIBLE);
            songCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                    if(controller.isShowing())
                        controller.hide();
                    else
                        controller.show(0);
                        */
                }
            });
            setController();
            setListView();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_playback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.full_playbackToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        songCover = (ImageView) findViewById(R.id.full_playbackSongCover);
        Intent i = getIntent();
        currentList = (ListView)findViewById(R.id.fpa_list);
        controller = new VideoControllerView(this);
        getServiceInstance();
        songListToPlay = musicSrv.getList();
        songToPlayPosition = musicSrv.getCurrentSongPosition();
        getSupportActionBar().setTitle(musicSrv.getCurrentTitle());             //songListToPlay.get(songToPlayPosition).getTitle());
        setCover();
        songCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                if(controller.isShowing())
                    controller.hide();
                else
                */  if(!controller.isShowing())
                        controller.show(0);

            }
        });
        setController();
        setListView();
        MODE = "CONTINUOS";
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("SONG_PLAYED"));
        // song_count_curr = i.getLongExtra("Song_Count",0L);

        //    if(BOTTOM_VIEW.equals(i.getAction())){

        //This is called when user touch bottom view and music has been played
        //        if(MusicService.isHasPlayed()) {
        //    }
            //This is called when user touch bottom view and music has not been played
        /*
            else{
                if(MusicService.hasInstance()){
                    getServiceInstance();
                    Bundle b = i.getBundleExtra("LIST_BUNDLE");

                    if (b != null) {
                        songListToPlay = new ArrayList<Song>();
                        songListToPlay = b.getParcelableArrayList("SONG_LIST");
                    }
                    musicSrv.setList(songListToPlay);
                    id = i.getLongExtra("LAST_PLAYED",Song.getFirstSongId());
                    songToPlayPosition = findCurrentSongToPlay();
                 //   Log.i("FUCK", songToPlay + " " + id + " " + songListToPlay.size() + songListToPlay.get(songToPlay).getID());
                    musicSrv.setSong(songToPlayPosition);
                    musicSrv.playSong();
                    getSupportActionBar().setTitle(songListToPlay.get(songToPlayPosition).getTitle());

                    setCover();
                    songCover.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            controller.show(0);
                        }
                    });

                    setController();
                }
            }
        */

        //This is called when user touch a song from mainactivity
        /*
        else {
            songToPlayPosition = i.getIntExtra("SONG_TO_PLAY", 0);
            Bundle b = i.getBundleExtra("LIST_BUNDLE");

            if (b != null) {
                songListToPlay = new ArrayList<Song>();
                songListToPlay = b.getParcelableArrayList("SONG_LIST");
            }

            followUp();
        }
        */



    }

    public void getServiceInstance(){
        if(musicSrv == null)
            musicSrv = MusicService.getInstance();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override

        public void onReceive(Context context, Intent intent) {
            if(MODE.equals("CONTINUOS")) {
                int songPosn = intent.getIntExtra("SONG_POSITION", 0);
                //there is an error here , this broadcast is recieving here before the songListToPlay can update so it is
                //giving index out of bound error
         //       Log.i("FUCK", "second "+MODE+" "+musicSrv);
                getServiceInstance();
                getSupportActionBar().setTitle(musicSrv.getCurrentTitle());    //songListToPlay.get(songPosn).getTitle());
                songToPlayPosition = songPosn;
                setCover();
            }
        }
    };

    private void setListView(){
        FpaListAdapter fpaAdapter = new FpaListAdapter(this, songListToPlay);
        currentList.setAdapter(fpaAdapter);
        currentList.setVisibility(View.GONE);
    }

    public void followUp(){
        if(MusicService.hasInstance()) {
            getServiceInstance();
        }
        else{
            //start Service in this case and set musicSrv variable
        }
        musicSrv.setList(songListToPlay);
        musicSrv.setSong(songToPlayPosition);
        musicSrv.playSong();
        getSupportActionBar().setTitle(musicSrv.getCurrentTitle());      //songListToPlay.get(songToPlay).getTitle());
        setCover();
        songCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                if(controller.isShowing())
                    controller.hide();
                else
                    controller.show(0);
                    */
            }
        });
        setController();
    }

    public void setCover(){
        /*
        MediaMetadataRetriever mr = new MediaMetadataRetriever();
        id = songListToPlay.get(songToPlay).getID();
            Uri trackUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id);

        try {
            mr.setDataSource(this, trackUri);
            byte[] art = mr.getEmbeddedPicture();
            Bitmap songImage = BitmapFactory
                    .decodeByteArray(art, 0, art.length);
            songCover.setImageBitmap(songImage);
        }catch (Exception e){
            songCover.setImageResource(R.drawable.default_albumart);
        }
        */

        songCover.setImageBitmap(musicSrv.getCurrentSongCover());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fpa, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            onBackPressed();
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_show_queue:
                if(songCover.getVisibility() == View.VISIBLE) {
                    songCover.setVisibility(View.GONE);
                    currentList.setVisibility(View.VISIBLE);
                    currentList.setSelection(songToPlayPosition);
                    item.setTitle("Show Cover");
                }
                else {
                    setCover();
                    songCover.setVisibility(View.VISIBLE);
                    currentList.setVisibility(View.GONE);
                    item.setTitle("Show Queue");
                }
                break;
        }
        return true;
    }

    public void clickFromFpaAdapter(View view){
        songToPlayPosition = Integer.parseInt(view.getTag().toString());
       // id = songListToPlay.get(songToPlayPosition).getID();
        musicSrv.setSong(songToPlayPosition);
        musicSrv.playSong();
        getSupportActionBar().setTitle(musicSrv.getCurrentTitle());    //songListToPlay.get(songToPlay).getTitle());
    }

    private int findCurrentSongToPlay(){
        int count = 0;
        while(count < songListToPlay.size()){
            Song s = songListToPlay.get(count);
            if(id == s.getID())
                break;
            count++;
        }
        if(count >= songListToPlay.size())
            count = 0;
        return count;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //if(controller.isShowing())
          //  controller.hide();
        finish();
    }


    @Override
    protected void onDestroy() {
     //   Log.i("FUCK","FPA onDestroy");
        musicSrv = null;
        super.onDestroy();
    }


    private void setController() {
        //set the controller up
        //controller = new MusicController(this);
        controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        /*
        controller.setMediaPlayer(this);
        controller.setAnchorView(songCover);         //Set the view that acts as the anchor for the control view
        controller.setEnabled(true);
        */
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
    //    Log.i("Musik","FPA onprepared");
        controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
    }

    //play next
    private void playNext() {
        songToPlayPosition++;
        if(songToPlayPosition>= songListToPlay.size()) songToPlayPosition=0;
        musicSrv.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
    }

    //play previous
    private void playPrev(){
        songToPlayPosition--;
        if(songToPlayPosition<0) songToPlayPosition= songListToPlay.size()-1;
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
    //    if(!controller.isShowing())
    //        controller.show(0);
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else
       return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
           return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

//    @Override
//    public int getAudioSessionId() {
//        return 0;
//    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    @Override
    protected void onPause(){
        paused=true;
        super.onPause();
    }
    @Override
    protected void onResume(){
          if(paused){
            setController();
            paused=false;
        }
        super.onResume();
    }
    @Override
    protected void onStop() {
      //  if(controller.isShowing())
        //    controller.hide();
        super.onStop();
    }



}
