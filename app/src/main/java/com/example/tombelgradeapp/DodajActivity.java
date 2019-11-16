package com.example.tombelgradeapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

public class DodajActivity extends AppCompatActivity {
    TextView txtUUID,txtNaziv;
    Controller controller=new Controller();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //Controller.dajInstrukcije(this,"Move the device closer and click the button!");
        akcijaNaDobijeniUUID();
    }
    public void akcijaNaDobijeniUUID(){
        Controller.dajInstrukcije(this,"Enter the name of your device!");
        final DodajActivity dodaj=this;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                govor(dodaj);
            }}, 2000);
    }
    public void govor(Context c){
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try{
            startActivityForResult(intent,1000);
        }catch (Exception e){
            Toast.makeText(c, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case 1000:{
                if(resultCode==RESULT_OK && null!=data){
                    ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show();
                    Controller.talkMetoda(this,"Name of your device is "+result.get(0));
                }
            }
            break;
        }

    }

}
