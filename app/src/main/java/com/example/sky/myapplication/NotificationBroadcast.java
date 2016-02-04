package com.example.sky.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by Devansh on 10/29/2015.
 */
public class NotificationBroadcast extends BroadcastReceiver {

    public static final String NOTIFY_DELETE = "com.example.sky.myapplication.delete";
    public static final String NOTIFY_PAUSE = "com.example.sky.myapplication.pause";
    public static final String NOTIFY_PLAY = "com.example.sky.myapplication.play";
    public static final String NOTIFY_NEXT = "com.example.sky.myapplication.next";
    MusicService musicSer;
    MainActivity ma;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(MusicService.hasInstance())
            musicSer = MusicService.getInstance();

        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if(musicSer.isPng()){
                        musicSer.pausePlayer();
                    }else{
                        musicSer.go();
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    musicSer.go();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    musicSer.pausePlayer();
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:

                    musicSer.playNext();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:

                    musicSer.playPrev();
                    break;
            }
        }
        else {
            if (intent.getAction().equals(NOTIFY_PLAY)) {
                if (musicSer != null) {
                    musicSer.go();
                    musicSer.updateNotificationView();
                }

            } else if (intent.getAction().equals(NOTIFY_PAUSE)) {
                if (musicSer != null) {
                    if(musicSer.isPng())
                        musicSer.pausePlayer();
                    else
                        musicSer.go();
                    Log.i("Musik","hum chale");
                    musicSer.updateNotificationView();
                }

            } else if (intent.getAction().equals(NOTIFY_NEXT)) {
                if (musicSer != null)
                    musicSer.playNext();
            } else if (intent.getAction().equals(NOTIFY_DELETE)) {
                musicSer.stopForeground(true);
                musicSer.stopSelf();
            }
        }
    }

    public String ComponentName() {
        return this.getClass().getName();
    }
}

