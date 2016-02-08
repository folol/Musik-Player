package com.example.sky.myapplication;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContextWrapper;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.net.Uri;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;


public class MainActivity extends AppCompatActivity implements  SongsFragment.OnFragmentInteractionListener   {

    private ArrayList<Song> songList;               //List of Songs to be played
    private ArrayList<Song> realSongList;
    private ArrayList<Song> songListToPlay;
    private MusicService musicSrv;                  //Music is controlled from this class but it is displayed from here main , so main is needed to be binded with music service
    private Intent playIntent;                      //Intent is a messaging object , it is passed when user needs to start a service/activity from some different place .
    static Handler mUiHandler;
    ViewPagerAdapter adapter;
    ViewPager viewPager;
    ContextWrapper cw;
    FloatingActionButton actionButton;
    private boolean  isLaunched=false ,firstTime;
    final String APP_PREFS = "APP_PREFS";
    String sortingType,sortingOrder;
    static  final int REG_SUCCESS = 1;
    static int SongViewMode ;
    int position,songIdPosition;
    HashMap<Long, Integer> songCounter,songListSaved;
    long songId ;
    SharedPreferences settings;
    private HorizontalScrollView mLimiterScroller;
    private ViewGroup mLimiterViews ;
    private TextView mTitle,mArtist,mNext,mPlayPause;
    private ImageView mCover ,mFab;
    Bundle b;
    public static final String BOTTOM_VIEW = "bottom_view",ACTION_DEF="default";

    @Override
    protected void onStart() {
        super.onStart();


        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);         //musicConnection will be called here so that songfragment_adapter list can be passed to service class.
            startService(playIntent);
        }


    }
    @Override
    protected void onRestart(){
        super.onRestart();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** This is to check if app is launched first time**/
        settings = getSharedPreferences(APP_PREFS, 0);
        firstTime = settings.getBoolean("first_time", true);
        SongViewMode = settings.getInt("Song_View_Mode", 0);
        sortingType = settings.getString("Sorting_Type","ALPHA");
        sortingOrder = settings.getString("Sorting_Order","ASC");
        isLaunched = true;
//        Looper.prepare();
//        mUiHandler = new Handler(Looper.getMainLooper());
        if(firstTime){

            Intent mRegistrationIntent = new Intent(this,UserRegistration.class);
            startActivityForResult(mRegistrationIntent,REG_SUCCESS);
            settings.edit().putBoolean("first_time",false).apply();

        }

       Toolbar toolbar = (Toolbar) findViewById(R.id.maintoolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        viewPager = (ViewPager) findViewById(R.id.mainviewpager);


        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.maintabs);

        tabLayout.setupWithViewPager(viewPager);

        mLimiterScroller = (HorizontalScrollView)findViewById(R.id.limiter_scroller);
        mLimiterViews = (ViewGroup)findViewById(R.id.limiter_layout);
        //mFabView = (ViewGroup)findViewById(R.id.fab_layout);

        mFab =  new ImageView(this);
        mFab.setImageResource(R.drawable.play);//(ImageView)mFabView.findViewById(R.id.fab_image);

        // Adding Floating Action Button
        int fabButtonSize = getResources().getDimensionPixelSize(R.dimen.fab_button_size);
        int fabButtonRightMargin = getResources().getDimensionPixelOffset(R.dimen.fab_right_margin);
        int fabButtonBottomMargin = getResources().getDimensionPixelOffset(R.dimen.fab_bottom_margin);

        FloatingActionButton.LayoutParams starParams = new FloatingActionButton.LayoutParams(fabButtonSize, fabButtonSize);
        starParams.setMargins(0,
                0,
                fabButtonRightMargin,
                fabButtonBottomMargin);

        actionButton = new FloatingActionButton.Builder(this)
                .setContentView(mFab)
                .build();
        actionButton.setPosition(FloatingActionButton.POSITION_BOTTOM_RIGHT,starParams);

        // Adding Menu Items to FAB
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);

        int fabMenuSize = getResources().getDimensionPixelSize(R.dimen.fab_menu_size);
        SubActionButton.LayoutParams menuParam = new SubActionButton.LayoutParams(fabMenuSize,fabMenuSize);

        ImageView itemIcon = new ImageView(this);
        itemIcon.setImageResource(R.drawable.album_symbol);
        SubActionButton button1 = itemBuilder.setContentView(itemIcon).build();

        ImageView itemIcon2 = new ImageView(this);
        itemIcon2.setImageResource(R.drawable.artist_symbol);
        SubActionButton button2 = itemBuilder.setContentView(itemIcon2).build();

        button1.setLayoutParams(menuParam);
        button2.setLayoutParams(menuParam);

        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(button1)
                .addSubActionView(button2)
                .attachTo(actionButton)
                .build();
        actionButton.setVisibility(View.GONE);
        /** Bottom View Layout **/
        View bottomView = getLayoutInflater().inflate(R.layout.bottom_song_view,null);
        mTitle = (TextView)bottomView.findViewById(R.id.title);
        mArtist = (TextView)bottomView.findViewById(R.id.artist);
        mCover = (ImageView)bottomView.findViewById(R.id.cover);
        mNext = (TextView)bottomView.findViewById(R.id.next);
        mPlayPause = (TextView)bottomView.findViewById(R.id.play_pause);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicSrv != null){
                    if(MusicService.isHasPlayed())
                        musicSrv.playNext();
                    else {
                        musicSrv.setList(songListToPlay);
                        musicSrv.setSong(songIdPosition);
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
                        if(musicSrv!=null) {
                            musicSrv.setList(songListToPlay);
                            musicSrv.setSong(songIdPosition);
                            musicSrv.playSong();
                        }
                    }
                else {
                        String textValue = mPlayPause.getText().toString();
                        if(textValue.equals("PLAY")){
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
        mLimiterViews.addView(bottomView);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("SONG_PLAYED"));

        cw = new ContextWrapper(getApplicationContext());


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 0:
                        //if(actionButton.getVisibility() == View.GONE)
                        actionButton.setVisibility(View.GONE);
                        break;

                    default:
                        if(actionButton.getVisibility() == View.VISIBLE)
                        actionButton.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mUiHandler = new Handler(){
            @Override
            public void handleMessage(Message msg)  {
                Log.i("Musik","handler ran");
                String msgString = msg.getData().getString("msg");
                //Log.i("Musik","handler ran");
                switch(msgString){
                    case "Song_View_Selected":
                        ArrayList<Song> sl = msg.getData().getParcelableArrayList("SongList");
                        setSongViewMode(0);
                        songFragmentDataChanged(sl);
                        break;
                    case "Ghanta":
                        Toast.makeText(getBaseContext(),"Handler ran",Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;

                }
            }
        };

    }




    public Handler getUiHandler(){
        return mUiHandler;
    }

    public void runHandler(Bundle b){
        Message msg = mUiHandler.obtainMessage();
        msg.setData(b);
        mUiHandler.sendMessage(msg);
    }

    public void setSongViewMode(int i){
        SongViewMode = i;
    }

    public int getSongViewMode(){
        return SongViewMode;
    }

    public void setListToMusicSrv(){
        musicSrv.setList(songListToPlay);
        musicSrv.setSong(songIdPosition);
    }

    public void setSortingType(String s){
        sortingType = s;
    }

    public void setSortingOrder(String s){
        sortingOrder = s;
    }

    public void reSortSongs(){
        SongsFragment sf = (SongsFragment)adapter.getItem(0);
        sf.reSort();
    }

    public String getSortingType(){return sortingType;}

    public String getSortingOrder(){return sortingOrder;}

    public void songFragmentDataChanged(ArrayList<Song> s){
        songList = s;
        SongsFragment sf = (SongsFragment)adapter.getItem(0);
        sf.dataChanged(songList);
    }

    public void deleteMediaFile(Song s){
        songList.remove(s);
        songListToPlay = musicSrv.getList();
        songFragmentDataChanged(songList);
        Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show();
    }

    public void setSongCounter(HashMap<Long,Integer> hm){
        songCounter = hm;
    }

    public HashMap<Long,Integer> getSongCounter(){
        return songCounter;
    }

    private int findCurrentSongPosition(){
        int count = 0;
        while(count < songList.size()){
            Song s = songList.get(count);
            if(songId == s.getID())
                break;
            count++;
        }
        if(count >= songList.size())
            count = 0;
        return count;

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            songId = intent.getLongExtra("SONG_PLAYED",Song.firstSongId);
         //   Log.i("Musik","Broadcast reciever of main");
            int value = songCounter.get(songId);
            value++;
            songCounter.put(songId,value);
            setUpBottomView();
        }

    };

    public void setUpBottomView(){

        if((songListToPlay != null && !songListToPlay.isEmpty())  ) {
            if (!MusicService.isHasPlayed() || musicSrv == null) {
                //   Log.i("Musik", "ma subv null");
                songId = settings.getLong("Song_last_playedd", Song.getFirstSongId());
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
     //              Log.i("Musik","ma subv not null "+musicSrv);
                mTitle.setText(musicSrv.getCurrentTitle());
                mArtist.setText(musicSrv.getCurrentArtist());
                mCover.setImageBitmap(musicSrv.getCurrentSongCover());
                mLimiterScroller.setVisibility(View.VISIBLE);
            }
            if(mLimiterViews.getVisibility() == View.GONE)
                mLimiterViews.setVisibility(View.VISIBLE);
        //    setPlayPauseTextView();
        }
        else{
//            Log.i("Musik","subv - "+songListToPlay+" "+songListToPlay.size());
            mLimiterViews.setVisibility(View.GONE);
        }

    }

    private void setPlayPauseTextView(){
        /*There is a bug here
        *musicSrv.isPng() is returning false even when it is playing
        which is causing playPause TextView at bottom to print worng text
        */

        if(musicSrv != null) {
            if (musicSrv.isPng()) {
                mPlayPause.setText("PAUSE");
             //      Log.i("Musik",mPlayPause.getText().toString()+" "+musicSrv.isPng());
            } else {
                mPlayPause.setText("PLAY");
            //       Log.i("Musik", mPlayPause.getText().toString()+" "+musicSrv.isPng());
            }
            boolean isPn = musicSrv.isPng();
//            Log.i("Musik","ma isPng - "+isPn);
        }

    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SongsFragment(), "Song");

        viewPager.setAdapter(adapter);
    }

    public void onFragmentInteraction(ArrayList<Song> list){
        songList = list;
      //  Log.i("Musik","is launched "+isLaunched);
        /*
            There is an error here if app is
            launched in other mode than song mode as
            song list will not have actual songs but artist or albums names
            so song counter gets initialized wrong ......
            remove this error by running parralely a query to get actual song list
            and then initliaze song counter by that list.
        */
        if(isLaunched) {
          //  Log.i("Musik","is launched");
            getSavedMapCounter();
//            getSavedSongListToPlay();
            if(SongViewMode == 0)
                new SetUpCounter(songCounter,songList,this).execute();
            else
                new SetUpCounter(songCounter,null,this).execute();

//             setUpBottomView();
            isLaunched = false;
        }
    }

    public void setRealSongList(ArrayList<Song> sl){
        realSongList = new ArrayList<>();
        realSongList = sl;
    }

    /*Yeh kaam setUpCounter se bhi ho skta hai*/

    public void getSavedSongListToPlay(){
        int i = 0;
        songId = settings.getLong("Song_last_playedd", Song.getFirstSongId());
        File Listdirectory = cw.getDir("songListToPlayDir", Context.MODE_PRIVATE);
        File ListPath = new File(Listdirectory,"songListToPlay");
        try {
            FileInputStream fList = new FileInputStream(ListPath);
            ObjectInputStream s = new ObjectInputStream(fList);
            Object o = s.readObject();
            songListSaved = (HashMap<Long, Integer>)o;
            s.close();
            if (songListSaved == null) {      //In case of first app launched there will be no file saved
                songListSaved = new HashMap<>();
            //    Log.i("Musik","gsls null  "+songListToPlay.size());
            }
            else {
              //  Log.i("Musik","getting savedlist");
                songListToPlay = new ArrayList<>();
                Set<Long> sl = songListSaved.keySet();
                Iterator<Long> iter = sl.iterator();
                while (iter.hasNext()) {
                    //flag = 0;
                    Long l = iter.next();
                    if(l == songId) {
                        songIdPosition = i;
                //        Log.i("Musik"," songlistsaved- "+songListSaved.size()+" "+i);
                    }
                    i++;
                    if(SongViewMode == 0) {
                        for (Song sg : songList) {      //The problem is here that in other than song mode this songList wil not contain actual songs so no id will be there
                            if (sg.getID() == l) {
                                songListToPlay.add(new Song(l, sg.getTitle(), sg.getArtist(), sg.getAlbum(), null));
                                break;
                            }
                        }
                    }
                    else{
                        for (Song sg : realSongList) {      //The problem is here that in other than song mode this songList wil not contain actual songs so no id will be there
                            if (sg.getID() == l) {
                                songListToPlay.add(new Song(l, sg.getTitle(), sg.getArtist(), sg.getAlbum(), null));
                                break;
                            }
                        }
                    }
                }

            }
     //       Log.i("Musik","slvo - "+songListSaved+" "+songListSaved.size()+" sltpo- "+songListToPlay+" "+songListToPlay.size());

        }
        catch (Exception e){
         //   Log.i("Musik","gsls exception "+e);
            songListSaved = new HashMap<>();
        }

    }

    public void getSavedMapCounter(){
        File Mapdirectory = cw.getDir("songCounterDir", Context.MODE_PRIVATE);
        File mapPath = new File(Mapdirectory,"songCounterMap");
        try {
            FileInputStream fMap = new FileInputStream(mapPath);
            ObjectInputStream s = new ObjectInputStream(fMap);
            songCounter = (HashMap<Long, Integer>) s.readObject();
            s.close();
          //  Log.i("Musik", "getting songCounter 2");
        }
        catch (Exception e){
          //  Log.i("Musik","getting songCounter 3 "+e);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            Context context = getApplicationContext();
            CharSequence text = data.getStringExtra("USERNAME");
            CharSequence mobNo = data.getStringExtra("USER_PHONE_NUMBER");
            int duration = Toast.LENGTH_LONG;
            settings.edit().putString("USER_NAME",text.toString()).apply();
            settings.edit().putString("USER_PHONE_NUMBER",mobNo.toString()).apply();
            Toast toast = Toast.makeText(context,"Hello "+text, duration);
            toast.show();
            firstTime = false;
        }

    }

    //connect to the service

    private ServiceConnection musicConnection = new ServiceConnection(){        //this function is connecting to service class

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;        //whenever an activity try to connect to a sevice , it's onBind function will return a Ibinder to let that calling activity to ineract with it .
            //get service
            musicSrv = binder.getService();
        //    setPlayPauseTextView();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem mi = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) mi.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.search) {
            onSearchRequested();
            return true;
        }

        switch (item.getItemId()) {

            case R.id.action_end:
               // Intent sn_intent = new Intent(this,SocialNetwork.class);
               // startActivity(sn_intent);
                SongViewDialog svd = new SongViewDialog();
                svd.show(getSupportFragmentManager(), "SongModeDialog");
                break;

            case R.id.profile:
                Intent profileIntent = new Intent(this,Profile.class);
                startActivity(profileIntent);
                break;

            case R.id.sort:
                    SortingViewDialog srvd = new SortingViewDialog();
                    srvd.show(getSupportFragmentManager(),"SortingModeDialog");

                break;

        }

        return super.onOptionsItemSelected(item);
    }

    public void showToast(){
        new Thread(new Runnable() {

            // After call for background.start this run method call
            public void run() {
                //Handler mUiHandler = new Handler(Looper.getMainLooper());
                Message msg = mUiHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("msg","Ghanta");
                msg.setData(b);
                mUiHandler.sendMessage(msg);
                Log.i("Musik","Thread ran");
            }
        }).start();
    }

    @Override
    public boolean onSearchRequested() {
        Bundle appData = new Bundle();
        appData.putString("hello", "world");
        startSearch(null, false, appData, false);
        return true;
    }

    public void setUpSongListToPlay(int pos,int i){
      //  int i = MusicService.getPlayMode();
      //  Log.i("Musik","mode - "+i);
        //i is playMode --- Play Or Enqueue
        if(i == 0) {
            if(songListToPlay == null)
                songListToPlay = new ArrayList<>();
            songListToPlay.clear();
            songListToPlay.add(songList.get(pos));
            musicSrv.setList(songListToPlay);
            musicSrv.setSong(0);
            musicSrv.playSong();
        }
        else{
            if(songListToPlay == null)
                songListToPlay = new ArrayList<>();
            if(musicSrv.isPng()) {

                songListToPlay.add(songList.get(pos));
                musicSrv.setList(songListToPlay);
                //musicSrv.setSong(position);
                //musicSrv.playSong();
            }
            else {
                songListToPlay.clear();
                songListToPlay.add(songList.get(pos));
                musicSrv.setList(songListToPlay);
                musicSrv.setSong(0);
                musicSrv.playSong();
            }

            Toast.makeText(this, "One Song Enqueued", Toast.LENGTH_SHORT).show();
        }



    }


    public void songClicked(View view){
        switch (SongViewMode){
            case 0:
                position = Integer.parseInt(view.getTag().toString());
                setUpSongListToPlay(position, MusicService.getPlayMode());
                setPlayPauseTextView();
                /*
                Intent data = new Intent(this,FullPlaybackActivity.class);
                data.setAction(ACTION_DEF);
                data.putExtra("SONG_TO_PLAY", position);
                // data.putExtra("Song_Count",songCounter.get(songId));
                //Log.i("mc","song count - "+songCounter.get(songId));
                b = new Bundle();
                b.putParcelableArrayList("SONG_LIST", songListToPlay);
                data.putExtra("LIST_BUNDLE", b);
                startActivity(data);
                */
                break;
            case 1:
                position = Integer.parseInt(view.getTag().toString());
                String artist = songList.get(position).getArtist();
                Intent artistIntent = new Intent(this,ArtistSpecific.class);
                artistIntent.setAction(ACTION_DEF);
                artistIntent.putExtra("TYPE", "ARTIST");
                artistIntent.putExtra("TYPE_NAME", artist);
                artistIntent.putExtra("AS_SONG_PLAYED", songId);
                artistIntent.putExtra("SongIdPosition",songIdPosition);
              //  Log.i("Musik"," "+MusicService.isHasPlayed());
                //if(!MusicService.isHasPlayed()){
                    b = new Bundle();
                    b.putParcelableArrayList("SONG_LIST", songListToPlay);
                    artistIntent.putExtra("LIST_BUNDLE", b);
               //     Log.i("Musik",songListToPlay+" ma "+songListToPlay.size());
                //}
                startActivity(artistIntent);
                break;
            case 2:
                position = Integer.parseInt(view.getTag().toString());
                String album = songList.get(position).getAlbum();
                Intent albumIntent = new Intent(this,ArtistSpecific.class);
                albumIntent.setAction(ACTION_DEF);
                albumIntent.putExtra("TYPE_NAME", album);
                albumIntent.putExtra("TYPE","ALBUM");
                albumIntent.putExtra("AS_SONG_PLAYED", songId);
                albumIntent.putExtra("SongIdPosition",songIdPosition);
               // if(!MusicService.isHasPlayed()){
                    b = new Bundle();
                    b.putParcelableArrayList("SONG_LIST", songListToPlay);
                    albumIntent.putExtra("LIST_BUNDLE", b);
               // }
                startActivity(albumIntent);
                break;
            case 3:
                position = Integer.parseInt(view.getTag().toString());
                String genreName = songList.get(position).getTitle();
                long genre_id = songList.get(position).getID();
                Intent genreIntent = new Intent(this,ArtistSpecific.class);
                genreIntent.setAction(ACTION_DEF);
                genreIntent.putExtra("TYPE_NAME", genreName);
                genreIntent.putExtra("GENRE_ID",genre_id);
                genreIntent.putExtra("TYPE", "GENRE");
                genreIntent.putExtra("AS_SONG_PLAYED", songId);
                genreIntent.putExtra("SongIdPosition",songIdPosition);
              //  if(!MusicService.isHasPlayed()){
                    b = new Bundle();
                    b.putParcelableArrayList("SONG_LIST", songListToPlay);
                    genreIntent.putExtra("LIST_BUNDLE", b);
              //  }
                startActivity(genreIntent);
                break;
            default:
                break;
        }



    }

    public void clickFromBottomView(View view){

        /*
        bottomIntent.putExtra("LAST_PLAYED", songId);
        if(!MusicService.isHasPlayed()){
            Bundle b = new Bundle();
            b.putParcelableArrayList("SONG_LIST", songList);
            bottomIntent.putExtra("LIST_BUNDLE", b);
        }
        */
        if(!songListToPlay.isEmpty()) {
            Intent bottomIntent = new Intent(this,FullPlaybackActivity.class);
            bottomIntent.setAction(BOTTOM_VIEW);
            if (!MusicService.isHasPlayed()) {
                musicSrv.setList(songListToPlay);
                musicSrv.setSong(songIdPosition);
            }
            startActivity(bottomIntent);
        }

    }
    /*
    public void clickFromArtistFragment(View view){

        int position = Integer.parseInt(view.getTag().toString());
         ArtistFragment fragment = (ArtistFragment)adapter.getItem(2);
            String artist =   fragment.artistName(position);
            Intent artistIntent = new Intent(this,ArtistSpecific.class);
            artistIntent.setAction(ACTION_DEF);
            artistIntent.putExtra("TYPE", "ARTIST");
            artistIntent.putExtra("TYPE_NAME", artist);
            artistIntent.putExtra("AS_SONG_PLAYED", songId);
        if(!MusicService.isHasPlayed()){
            Bundle b = new Bundle();
            b.putParcelableArrayList("SONG_LIST", songListToPlay);
            artistIntent.putExtra("LIST_BUNDLE", b);
        }
        startActivity(artistIntent);

    }

    public void clickFromAlbumFragment(View view){

        int position = Integer.parseInt(view.getTag().toString());
        AlbumFragment fragment = (AlbumFragment)adapter.getItem(1);
        String album =   fragment.getAlbumName(position);
        Intent albumIntent = new Intent(this,ArtistSpecific.class);
        albumIntent.setAction(ACTION_DEF);
        albumIntent.putExtra("TYPE_NAME",album);
        albumIntent.putExtra("TYPE","ALBUM");
        albumIntent.putExtra("AS_SONG_PLAYED", songId);
        if(!MusicService.isHasPlayed()){
            Bundle b = new Bundle();
            b.putParcelableArrayList("SONG_LIST", songListToPlay);
            albumIntent.putExtra("LIST_BUNDLE", b);
        }
        startActivity(albumIntent);

    }

    public void clickFromGenreFragment(View view){

        int position = Integer.parseInt(view.getTag().toString());
        GenreFragment fragment = (GenreFragment)adapter.getItem(3);
        String genreName = fragment.getGenreName(position);
        long genre_id = fragment.getGenreId(position);
        Intent genreIntent = new Intent(this,ArtistSpecific.class);
        genreIntent.setAction(ACTION_DEF);
        genreIntent.putExtra("TYPE_NAME", genreName);
        genreIntent.putExtra("GENRE_ID",genre_id);
        genreIntent.putExtra("TYPE", "GENRE");
        genreIntent.putExtra("AS_SONG_PLAYED", songId);
        if(!MusicService.isHasPlayed()){
            Bundle b = new Bundle();
            b.putParcelableArrayList("SONG_LIST", songListToPlay);
            genreIntent.putExtra("LIST_BUNDLE", b);
        }
        startActivity(genreIntent);

    }

    */

    @Override
    protected void onDestroy() {

        saveMapCounter();
        saveSongListToPlay();
        unbindService(musicConnection);
        stopService(playIntent);
        musicSrv=null;
        settings.edit().putLong("Song_last_playedd", songId).apply();
        settings.edit().putInt("Song_View_Mode", getSongViewMode()).apply();
        settings.edit().putString("Sorting_Type", sortingType).apply();
        settings.edit().putString("Sorting_Order",sortingOrder).apply();
        super.onDestroy();

    }

    private void saveMapCounter(){
        File Mapdirectory = cw.getDir("songCounterDir", Context.MODE_PRIVATE);
        File mapPath = new File(Mapdirectory,"songCounterMap");
        try {
            FileOutputStream fMap = new FileOutputStream(mapPath);
            ObjectOutputStream s = new ObjectOutputStream(fMap);
            s.writeObject(songCounter);
            s.close();
        }
        catch (Exception e){

        }
    }

    private void saveSongListToPlay(){
        if(songListSaved != null)
            songListSaved.clear();
        int i=0;
        for(Song s:songListToPlay){
     //       Log.i("Musik","ssltp - "+s+" "+songListSaved);
            songListSaved.put(s.getID(),i);
            i++;
        }
      //  Log.i("Musik","sltp - "+songListToPlay+" "+songListToPlay.size()+" sls- "+songListSaved+" "+songListSaved.size());
    //    Log.i("Musik","saved list size "+songListSaved.size());
        File Listdirectory = cw.getDir("songListToPlayDir", Context.MODE_PRIVATE);
        File ListPath = new File(Listdirectory,"songListToPlay");
        try {
            FileOutputStream fMap = new FileOutputStream(ListPath);
            ObjectOutputStream s = new ObjectOutputStream(fMap);
            s.writeObject(songListSaved);
            s.close();
        }
        catch (Exception e){

        }
      //  Log.i("Musik","ondestory- "+songListToPlay.size()+" "+songListSaved.size());
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {

//        if(mLimiterScroller.getVisibility() == View.GONE)
//            mLimiterScroller.setVisibility(View.VISIBLE);
        /*
        if(musicSrv != null) {
            if (musicSrv.isPng())
                mPlayPause.setText("PAUSE");
            else
                mPlayPause.setText("PLAY");
        }
        */
        setPlayPauseTextView();
        if(MusicService.isHasPlayed()) {
            songListToPlay = musicSrv.getList();
    //        Log.i("Musik","onresume ma - "+songListToPlay.size());
        }
        super.onResume();

    }
    @Override
    protected void onStop() {
        super.onStop();
    }

}
