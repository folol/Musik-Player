package com.example.sky.myapplication;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This activity is launched only on first opening of app to let user enter his Mobile Number only
 */

public class UserRegistration extends ActionBarActivity  {

    EditText userMobNo_view;
    String userMobNo_value;
    Button nextButton;
    TextView status;
    Intent data;
    private final int REG_SUCC = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);
        userMobNo_view = (EditText) findViewById(R.id.user_registrationPhoneNo);
        nextButton = (Button) findViewById(R.id.profileButtonNext);
        status = (TextView) findViewById(R.id.textView_status);
        userMobNo_view.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH ||event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    userMobNo_value = userMobNo_view.getText().toString();

                    if(!searchDB(userMobNo_value) && validate_userMobNo(userMobNo_value)){
                        enterInDB(userMobNo_value);

                    }
                    else{
                        userMobNo_value = null;
                        retrieveUserInfo();
                    }
                }


                return false;
            }
        });

        userMobNo_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status.setText("");
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                status.setText("");
                userMobNo_value = userMobNo_view.getText().toString();
                if(!searchDB(userMobNo_value) && validate_userMobNo(userMobNo_value)){
                    enterInDB(userMobNo_value);
                }
                else{
                    userMobNo_value = null;
                    retrieveUserInfo();
                }
            }
        });
    }

    //To retireve user details if he is already registered.

    private void retrieveUserInfo() {
    }

    //To validate the Mobile Number format

    private boolean validate_userMobNo(String userMobNo_value) {
        if(userMobNo_value.matches("[0-9]+") && userMobNo_value.length() == 10) {

            //userMobNo_value = null;
            return true;
        }
        else {
            status.setText("Enter valid mobile number");
            return false;
        }

    }

    //Search Mobile number in database

    private boolean searchDB(String userMobNo_value){
        return false;

    }

    //Enter Mobile Number in database  and then launched second part of registration through Part2 activity

    private void enterInDB(String userMobNo_value){
                //run query to create entry in user table;
        data = new Intent(this,UserRegistrationPartTwo.class);
        startActivityForResult(data,REG_SUCC);


    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent result){
            if(resultCode == RESULT_OK){
                String userNameValue = result.getStringExtra("USERNAME");
                data = new Intent();
                data.putExtra("USERNAME",userNameValue);
                data.putExtra("USER_PHONE_NUMBER",userMobNo_value);
                setResult(RESULT_OK,data);
                finish();
            }
    }



    @Override
    public  void onResume(){
        super.onResume();
    }

    @Override
    public void onStop() {

        super.onStop();
    }

    @Override
    public void onPause(){
        super.onPause();
    }



}
