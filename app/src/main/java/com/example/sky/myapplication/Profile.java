package com.example.sky.myapplication;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;


/**
 * This activity is launched when user click on Profile item on action bar from main activity
 */

public class Profile extends ActionBarActivity {

    ImageView profilePic ;
    TextView userName ;
    TextView userPhoneNo ;
    SharedPreferences settings;
    final String APP_PREFS = "APP_PREFS";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profilePic = (ImageView) findViewById(R.id.profilePic);
        userName = (TextView) findViewById(R.id.profileUserName);
        userPhoneNo = (TextView) findViewById(R.id.profileUserPhoneNo);
        settings = getSharedPreferences(APP_PREFS,0);
        userName.setText(settings.getString("USER_NAME",""));
        userPhoneNo.setText(settings.getString("USER_PHONE_NUMBER","GUEST"));
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File myPath = new File(directory,"profile.jpg");
        Bitmap myBitmap = BitmapFactory.decodeFile(myPath.getAbsolutePath());
        if(myBitmap != null)
        profilePic.setImageBitmap(myBitmap);
        else
            profilePic.setImageResource(R.drawable.defalut_profile_pic);
    }

}
