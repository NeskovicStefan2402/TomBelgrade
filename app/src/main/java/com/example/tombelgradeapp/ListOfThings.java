package com.example.tombelgradeapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ListOfThings extends AppCompatActivity {
    DataController myDb;
    ListView lView;
    MyAdapter lAdapter;
    int i=0;
    List<Uredjaj> uredjaji=new ArrayList<Uredjaj>();
    LinkedList<String> indexes=new LinkedList<>();
    LinkedList<String> values=new LinkedList<>();
    LinkedList<String> connections=new LinkedList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_things);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        myDb= new DataController(this);
        getAll();
        popuniListe();
        lView=findViewById(R.id.lista);
        lAdapter = new MyAdapter(ListOfThings.this, indexes,values,connections);
        lView.setAdapter(lAdapter);

    }
    public  void AddData(List<String> lista) {
        for(int i=0;i<lista.size();i++) {
            boolean isInserted = myDb.insertData(Long.parseLong(i+""),"UUID","Rx","Tx",lista.get(i),0);
            System.out.println(isInserted+"");
        }
    }
    public void popuniListe(){
        for(int i=0;i<uredjaji.size();i++){
            indexes.add(uredjaji.get(i).getId()+"");
            values.add(uredjaji.get(i).getName());
            connections.add(uredjaji.get(i).getConn()+"");
        }
    }
    public void getAll() {
        Cursor res = myDb.getAllData();
        if(res.getCount() == 0) {
            Toast.makeText(this, "Nije nista nadjeno!", Toast.LENGTH_LONG).show();
            return;
        }
        while (res.moveToNext()) {
            Uredjaj u=new Uredjaj();
            u.setId(res.getLong(0));
            u.setUuid(res.getString(1));
            u.setRx(res.getString(2));
            u.setTx(res.getString(3));
            u.setName(res.getString(4));
            u.setConn(res.getInt(5));
            uredjaji.add(u);
        }

    }
    public void deleteData() {
        myDb.deleteAll();

    }
    public String napraviString(){
        String rec="";
        for(int i=0;i<values.size();i++){
            rec=rec+" "+values.get(i);
        }
        return rec;
    }

    public void konektujSe(View view){
        if(i<values.size()){
            Controller.dajInstrukcije(this,values.get(i));
            i++;
        }else{
            Controller.dajInstrukcije(this,values.get(0));
            i=1;
        }
    }

}
