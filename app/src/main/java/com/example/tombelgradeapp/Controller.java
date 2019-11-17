package com.example.tombelgradeapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.sip.SipSession;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Controller extends AppCompatActivity {
    public static MediaPlayer player;
    public static TextToSpeech tts;
    public static String text;
    public static void pustiPocetniSignal(Context c){
        if(player==null) {
            player = MediaPlayer.create(c, R.raw.uvodni);
        }player.start();
    }
    public static void talkMetoda(final Context c, final String text){
        tts = new TextToSpeech(c, new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result= tts.setLanguage(Locale.US);
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        //Toast.makeText(c, "Nema jezika ", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                else
                    Toast.makeText(c, "Greska pri inicijalizaciji", Toast.LENGTH_SHORT).show();

            }
        });

    }
    public static void dajInstrukcije(Context c,String instrukcija){
        talkMetoda(c,instrukcija);
    }
    public static String getTextButton(Context c){
        String stavka="";
        String txt;
        if (MainActivity.brojac%2==0) {
            talkMetoda(c,"Can you tell us what you lost");

            txt="Proces pretrage "+stavka+" traje...";

        }else{
            txt="Pretraga";
        }
        MainActivity.brojac++;
        return txt;
    }
    public static String vratiElement(LinkedList<String> lista, String element) {
        for (int i = 0; i <lista.size(); i++) {
            if(lista.get(i).toLowerCase().equals(element.toLowerCase())){
                return element;
            }
        }
        return null;
    }
    public static boolean daLiPostoji(LinkedList<String > lista,String ele){
        if(vratiElement(lista,ele)==null){
            return false;
        }
        return true;
    }

    public static LinkedList<String> getLista(){
        LinkedList<String> stvari=new LinkedList<>();
        stvari.add("Majica");
        stvari.add("Papuce");
        stvari.add("Naocare");
        stvari.add("Lekovi");
        stvari.add("Stap");
        stvari.add("Patike");
        stvari.add("Cipele");
        stvari.add("Farmerke");
        return stvari;
    }



}
