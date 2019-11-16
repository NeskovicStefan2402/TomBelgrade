package com.example.tombelgradeapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

public class MainActivity extends Activity {
    Button btn;
    public static int brojac=0;
    public static MediaPlayer player;
    private int lastClickTime=(int)System.currentTimeMillis();
    private int init = 0;
    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        btn=findViewById(R.id.Pretrazi);
        Controller.pustiPocetniSignal(this);
        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                prebaciIntent();
                return true;
            }
        });
    }
    public void pretraga(View view) {
        final MainActivity main=this;
        if (init == 0) {
            init++;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (init == 1) {
                        if (!Controller.player.isPlaying()) {
                            btn.setText(Controller.getTextButton(main));
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    govor();
                                }
                            }, 3000);

                        }

                    }else {
                            Intent i=new Intent(main,ListOfThings.class);
                            startActivity(i);

                        }
                    init = 0;
                }

            }, 500);
        } else {
            init++;
        }
    }

    public void govor(){
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
        try{
           startActivityForResult(intent,1000);
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case 1000:{
                if(resultCode==RESULT_OK && null!=data){
                    ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    LinkedList<String> lista=Controller.getLista();
                    Toast.makeText(this, ""+lista, Toast.LENGTH_SHORT).show();
                    if(Controller.daLiPostoji(lista,result.get(0))){
                           Controller.talkMetoda(this,"Your "+result.get(0)+" is located.");
                     }else{
                        Controller.talkMetoda(this,"Your "+result.get(0)+" is not located. Try again!");
                    }
                }
            }
            break;
        }

    }
    public void prebaciIntent(){
        Intent i=new Intent(this,DodajActivity.class);
        startActivity(i);
    }

}
