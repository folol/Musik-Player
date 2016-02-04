package com.example.sky.myapplication;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This activity is to let user enter profile picture and name
 */

public class UserRegistrationPartTwo extends ActionBarActivity {
    EditText userName;
    String userNameValue = "";
    Button next;
    Intent data;
    ImageView profilePic;
    private final int RESULT_LOAD_IMG = 1 , SELECT_FILE = 2 , CROP_FROM_CAMERA = 3;
    Bitmap thumbnail;
    File myPath;
    FileOutputStream fo;
    Uri profilePicUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration_part_two);

        userName = (EditText) findViewById(R.id.user_registration_part2UserName);
        next = (Button) findViewById(R.id.user_registration_part2Next);
        profilePic = (ImageView)findViewById(R.id.user_registration_part2ProfilePic);


        userName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH || event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    userNameValue = userName.getText().toString();
                    if (userNameValue.isEmpty()) {
                        userName.setText("Enter Name");
                    } else {
                        startFinishing();
                    }
                }
                return false;
            }
        });
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userNameValue = userName.getText().toString();
                if (userNameValue.equals("Enter Name")) {
                    userNameValue = null;
                    userName.setText("");
                }
            }
        });


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userNameValue = userName.getText().toString();
                if(userNameValue.isEmpty()) {

                    userName.setText("Enter Name");
                }
                else{

                    startFinishing();
                }

            }
        });

        //A dialog box is opened to let user enter pic from various sources like camera or an app.

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence items[] = {"Take Photo","Choose from Gallery","Cancel"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(UserRegistrationPartTwo.this);
                dialog.setTitle("Profile Picture");
                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (items[which].equals("Take Photo")) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            profilePicUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                                    "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));

                            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, profilePicUri);

                            try {
                                intent.putExtra("return-data", true);

                                startActivityForResult(intent, RESULT_LOAD_IMG);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else if (items[which].equals("Choose from Gallery")) {
                            Intent intent = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(
                                    Intent.createChooser(intent, "Select File"),
                                    SELECT_FILE);
                        } else {
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();



            }
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            // When an Image is picked
            if(resultCode == RESULT_OK){
                if (requestCode == RESULT_LOAD_IMG) {

                        doCrop();
                }
                //When gallery is picked
                else if(requestCode == SELECT_FILE ) {
                    profilePicUri = data.getData();
                    doCrop();


                }

                //Image has been picked and croped
                else if(requestCode == CROP_FROM_CAMERA){
                    Bundle extras = data.getExtras();

                    if (extras != null){
                        thumbnail = extras.getParcelable("data");
                        savePic();
                    }
                }
        }


    }

    //Save image in app directory in internal memory
    private void savePic(){

        try {

            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            myPath = new File(directory,"profile.jpg");
            if(myPath.exists()){
                myPath.delete();
                myPath = new File(directory,"profile.jpg");
            }

            fo = new FileOutputStream(myPath);
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, fo);
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        profilePic.setImageBitmap(thumbnail);

    }

    //Do the crop operation on the image picked
    private void doCrop() {
        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

        //Intent to launch an app that can do crop
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        //List of apps that can do crop
        List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );

        int size = list.size();


        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();

            return;
        } else {
            intent.setData(profilePicUri);

            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale",  true);
            intent.putExtra("return-data", true);

            if (size == 1) {
                Intent i        = new Intent(intent);
                ResolveInfo res = list.get(0);

                i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));


                startActivityForResult(i, CROP_FROM_CAMERA);
            } else {
                for (ResolveInfo res : list) {
                    final CropOption co = new CropOption();

                    co.title    = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon     = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                    co.appIntent= new Intent(intent);

                    co.appIntent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                    cropOptions.add(co);
                }

                CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);

                //Build a dialog to let user choose an app to crop the image
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose Crop App");
                builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int item ) {
                        startActivityForResult( cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
                    }
                });

                builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel( DialogInterface dialog ) {

                        if (profilePicUri != null ) {
                            getContentResolver().delete(profilePicUri, null, null );
                            profilePicUri = null;
                        }
                    }
                } );

                AlertDialog alert = builder.create();

                alert.show();
            }
        }
    }


    @Override
    protected void onStart(){
        super.onStart();
    }

    //Operations done to finish this activity and return to previous activity(UserRegistration)
    protected void startFinishing(){

        data = new Intent();
        data.putExtra("USERNAME",userNameValue);
        setResult(RESULT_OK, data);
        finish();
    }

}
