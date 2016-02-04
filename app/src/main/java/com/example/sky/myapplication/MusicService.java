package com.example.sky.myapplication;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
//import android.media.RemoteControlClient;
import android.media.RemoteControlClient;
import android.os.Build;
import android.os.IBinder;

import java.io.File;
import java.util.ArrayList;
import android.content.ContentUris;
//import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Iterator;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

//import com.example.sky.myapplication.Song;

/**
 * Created by sky on 4/30/2015.
 */
public class MusicService extends Service implements        //Service class is used when app tries to performe a functionality when not interacting with user
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,        //these interface method are applied so that when MediaPlayer object changes the state ...appropriate method will be called .
        MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener{

    //media player
    private MediaPlayer player;                                 //Media Player class controls the the audio/video playback and it has several states.
    //songfragment_adapter list
    private ArrayList<Song> songs;
    //current position
    private int currSongPosition;
    private static int playMode , corruptMediaCounter;
    private static boolean hasPlayed = false;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle="";
    private static final int NOTIFY_ID=1;
    private boolean shuffle=false;
    private Random rand;
    public static final String BOTTOM_VIEW = "bottom_view";
    long currSongId;
    private ComponentName remoteComponentName;
    private RemoteControlClient remoteControlClient;
    AudioManager audioManager;
    Bitmap mDummyAlbumArt,songImage;
    Song playSong;
    Notification not;
    RemoteViews simpleView;

    public static MusicService sInstance;

    public static final String NOTIFY_DELETE = "com.example.sky.myapplication.delete";
    public static final String NOTIFY_PAUSE = "com.example.sky.myapplication.pause";
    public static final String NOTIFY_PLAY = "com.example.sky.myapplication.play";
    public static final String NOTIFY_NEXT = "com.example.sky.myapplication.next";

    @Override
    public IBinder onBind(Intent arg0) {            //this method is called when an activity tries to connect to this service ...like main try to connect to this at line 68 in mainactivity.java
        // TODO Auto-generated method stub
        corruptMediaCounter = 0;
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        corruptMediaCounter = 0;
        hasPlayed = false;
        sInstance = null;                                                            //this will be called when user exit the app , as connection will break.
        player.stop();
        player.release();
        return false;
    }

    public String getCurrentTitle(){
       // Log.i("Musik","title "+songTitle);
        if(MusicService.isHasPlayed())
            return songTitle;
        else if(songs != null)
            return songs.get(currSongPosition).getTitle();
        else
            return null;
    }

    public String getCurrentArtist(){
       // Log.i("Musik","title "+playSong.getArtist());
        return playSong.getArtist();
    }

    public ArrayList<Song> getList(){
        if(sInstance != null){
            return songs;
        }
        else
            return null;
    }


    public static MusicService getInstance(){
            return sInstance;

    }

    public Bitmap getCurrentSongCover(){
        MediaMetadataRetriever mr = new MediaMetadataRetriever();
        try {
            Uri trackUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    currSongId);
            mr.setDataSource(this, trackUri);
            byte[] art = mr.getEmbeddedPicture();
             songImage = BitmapFactory
                    .decodeByteArray(art, 0, art.length);
        }catch (Exception e){
            Drawable myDrawable = getResources().getDrawable(R.drawable.default_albumart);
            songImage      = ((BitmapDrawable) myDrawable).getBitmap();

        }
        return  songImage;
    }



    public int getCurrentSongPosition(){
        return currSongPosition;
    }

    public static boolean hasInstance(){
        if(sInstance == null)
            return false;
        else
            return true;
    }

    public static void setPlayMode(boolean b){
        if(b)
            playMode = 1;      //enqueue
        else
            playMode = 0;      //play
    }

    public void addListOfSongs(ArrayList<Song> sl){
        for(Song s:sl){
            songs.add(s);
        }
    }

    public boolean removeSong(Song s,String path){

        File fdelete = new File(path);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                //System.out.println("file Deleted :" + uri.getPath());
                if (songs != null && !songs.isEmpty()) {
                    if (playSong != null && playSong.getID() == s.getID()) {
                        if (isPng()) {   //If song playing has to be deleted
                            pausePlayer();
                            clearNotification();
                        }
                        songs.remove(currSongPosition);
                        if (!songs.isEmpty()) {
            //                Log.i("Musik","removeSong "+songs.size());
                            playNext();
                            pausePlayer();
                        } else {             //songs is empty so update bottom view of ma
                            Intent i = new Intent("SONG_PLAYED");
                            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                        }
                    } else {
                        int temp = isSongInSongList(s);
                        if (temp >=0 && temp < songs.size()) {    //if song in list to play has to be deleted
                            songs.remove(temp);
                        }
                    }
                    return true;       //File has been deleted and songs has been updated
                }
                return true;          //File has been deleted and songs is empty
            }
            else {
                Toast.makeText(this, "File cannot be deleted", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show();
        return false;


    }

    public int isSongInSongList(Song s){
        int i = 0;
        while(i < songs.size()){
            Song tempSong = songs.get(i);
            if(s.getID() == tempSong.getID())
                return i;
            else
                i++;
        }
        return i;
    }


    public static int getPlayMode(){
        return playMode;
    }

    public static boolean isHasPlayed(){
      //  Log.i("Musik",hasPlayed+" ");
        return hasPlayed;
    }

    public void onCreate(){
        //create the service
        //create the service
        super.onCreate();
        //initialize position
        currSongPosition =0;
        //create player
        player = new MediaPlayer();
        initMusicPlayer();
        rand=new Random();
        sInstance = this;
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        RegisterRemoteClient();
    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);                    //Set the low-level power management behavior for this MediaPlayer
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);       //Sets the audio stream type for this MediaPlayer
        player.setOnPreparedListener(this);                         // listener for (1) when the MediaPlayer instance is prepared
        player.setOnCompletionListener(this);                       //listner for when a songfragment_adapter has completed playback
        player.setOnErrorListener(this);                            //listner for when (3) an error is thrown:
    }

    public void setList(ArrayList<Song> theSongs){
        corruptMediaCounter = 0;
        songs=theSongs;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition()>0){
            if(currSongPosition == songs.size()-1)
                mp.pause();
            else{
                mp.reset();
                playNext();
            }

        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void playSong(){
        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        //play a songfragment_adapter
        player.reset();
        playSong = songs.get(currSongPosition);
        //get id
        currSongId = playSong.getID();       // currSongPosition;
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSongId);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
            player.prepareAsync();
            //FileNameMap fileNameMap = URLConnection.getFileNameMap();
            //String type = fileNameMap.getContentTypeFor(trackUri.toString());
            //Log.i("FUCK",type);
        }
        catch(Exception e){
            Toast.makeText(this, "Can not play this media", Toast.LENGTH_SHORT).show();
            if(songs.size() <= 1 && corruptMediaCounter >= songs.size()){
                playNext();
                corruptMediaCounter++;
            }
            else {
                player.reset();
                player.pause();
            }

        }
        songTitle=playSong.getTitle();
        hasPlayed = true;
        Intent i = new Intent("SONG_PLAYED");
        i.putExtra("SONG_PLAYED", currSongId);
        i.putExtra("SONG_POSITION", currSongPosition);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        UpdateMetadata(playSong);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();

        Intent notIntent = new Intent(this, FullPlaybackActivity.class);                //code from this line to end of method is implemented to add notification when user is out of our app and taking it back to app when he clicks on the notifctn.
        //TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack
        //stackBuilder.addParentStack(FullPlaybackActivity.class);
        // Adds the Intent to the top of the stack
        //stackBuilder.addNextIntent(notIntent);
        notIntent.setAction(BOTTOM_VIEW);

        simpleView = new RemoteViews(this.getPackageName(), R.layout.custom_notification);

        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.default_albumart)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);

        not = builder.build();
        not.contentView =simpleView;
        updateNotificationView();
        /*if(isPng()) {
            simpleView.setViewVisibility(R.id.btnPause_cn, View.VISIBLE);
            simpleView.setViewVisibility(R.id.btnPlay_cn, View.GONE);
        }
        else {
            simpleView.setViewVisibility(R.id.btnPlay_cn, View.VISIBLE);
            simpleView.setViewVisibility(R.id.btnPause_cn, View.GONE);
        }
        not.contentView.setTextViewText(R.id.textSongName_cn, songTitle);
        not.contentView.setTextViewText(R.id.textAlbumName_cn, playSong.getAlbum());
        not.contentView.setImageViewBitmap(R.id.imageView_cn, getCurrentSongCover());
        */
        setListeners(simpleView, this);
        startForeground(NOTIFY_ID, not);

    }

    public void updateNotificationView(){
        if(not != null) {
            if (isPng()) {
                simpleView.setViewVisibility(R.id.btnPause_cn, View.VISIBLE);
                simpleView.setViewVisibility(R.id.btnPlay_cn, View.GONE);
                Log.i("Musik", "hum chale true");
                //simpleView.setImageViewResource(R.id.btnPause_cn,R.drawable.play_pause);
            } else {
                simpleView.setViewVisibility(R.id.btnPause_cn, View.GONE);
                simpleView.setViewVisibility(R.id.btnPlay_cn, View.VISIBLE);
                Log.i("Musik", "hum chale false");
               // simpleView.setImageViewResource(R.id.btnPause_cn,R.drawable.play);
            }
            not.contentView.setTextViewText(R.id.textSongName_cn, songTitle);
            not.contentView.setTextViewText(R.id.textAlbumName_cn, playSong.getAlbum());
            not.contentView.setImageViewBitmap(R.id.imageView_cn, getCurrentSongCover());
           // Log.i("Musik","Notification updated "+isPng());
        }
    }

    public void clearNotification() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Log.i("Musik","Notif clear");
    }

    private void setListeners(RemoteViews view,Context context){
        Intent delete = new Intent(NOTIFY_DELETE);
        Intent pause = new Intent(NOTIFY_PAUSE);
        Intent next = new Intent(NOTIFY_NEXT);
        Intent play = new Intent(NOTIFY_PLAY);

        PendingIntent pDelete = PendingIntent.getBroadcast(context, 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnDelete_cn, pDelete);

        PendingIntent pPause = PendingIntent.getBroadcast(context, 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPause_cn, pPause);

        PendingIntent pNext = PendingIntent.getBroadcast(context, 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnNext_cn, pNext);

        PendingIntent pPlay = PendingIntent.getBroadcast(context, 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPlay_cn, pPlay);


    }



    public void setSong(int songIndex){
        currSongPosition =songIndex;
        if(playSong == null)
            playSong = songs.get(currSongPosition);
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
//        Log.i("Musik","isPng- "+player+" "+player.isPlaying());
        return player.isPlaying();
    }

    public void pausePlayer(){
        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public void playPrev(){
        currSongPosition--;
        if(currSongPosition <0) currSongPosition =songs.size()-1;
        playSong();
    }

    //skip to next
    public void playNext(){
        if(shuffle){
            int newSong = (int) currSongPosition;
            while(newSong== currSongPosition){
                newSong=rand.nextInt(songs.size());
            }
            currSongPosition =newSong;
        }
        else{
            currSongPosition++;
            if(currSongPosition >=songs.size()) currSongPosition =0;
        }
        playSong();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public void setShuffle(){
        if(shuffle) shuffle=false;
        else shuffle=true;
    }

    @SuppressLint("NewApi")
    private void RegisterRemoteClient(){
        remoteComponentName = new ComponentName(getApplicationContext(), new NotificationBroadcast().ComponentName());
        try {
            if(remoteControlClient == null) {

                audioManager.registerMediaButtonEventReceiver(remoteComponentName);
                Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                mediaButtonIntent.setComponent(remoteComponentName);

                PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
                remoteControlClient = new RemoteControlClient(mediaPendingIntent);

                audioManager.registerRemoteControlClient(remoteControlClient);
            }
            remoteControlClient.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_STOP |
                            RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
        }catch(Exception ex) {

        }

    }

    @SuppressLint("NewApi")
    private void UpdateMetadata(Song data){
        if (remoteControlClient == null)
            return;
        RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, data.getAlbum());
        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, data.getArtist());
        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, data.getTitle());

        if(mDummyAlbumArt == null){
            mDummyAlbumArt = getCurrentSongCover();
        }
        metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, mDummyAlbumArt);
        metadataEditor.apply();
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public void onAudioFocusChange(int focusChange){

    }

}
