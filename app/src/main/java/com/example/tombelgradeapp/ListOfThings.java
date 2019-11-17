package com.example.tombelgradeapp;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tombelgradeapp.rest.BuggyControl;

import java.sql.SQLData;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ListOfThings extends AppCompatActivity {
    DataController myDb;
    SQLiteDatabase database;
    ListView lView;
    MyAdapter lAdapter;
    int i = 0;
    List<Uredjaj> uredjaji = new ArrayList<Uredjaj>();
    LinkedList<String> indexes = new LinkedList<>();
    LinkedList<String> values = new LinkedList<>();
    LinkedList<String> connections = new LinkedList<>();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_things);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        myDb = MainActivity.myDb;
        getAll();
        popuniListe();
        System.out.println(myDb.getAllData());
        lView=findViewById(R.id.lista);
        lAdapter = new MyAdapter(ListOfThings.this, indexes,values,connections);
        lView.setAdapter(lAdapter);

        /////////////////////////////////////////////////////////////////////////////////////////////



    }

    public void popuniListe() {
        for (int i = 0; i < uredjaji.size(); i++) {
            indexes.add(uredjaji.get(i).getId() + "");
            values.add(uredjaji.get(i).getObjectName());
            connections.add(uredjaji.get(i).getConnected() + "");
        }
    }

    public void getAll() {
        Cursor res = myDb.getAllData();
        if (res.getCount() == 0) {
            Toast.makeText(this, "Nije nista nadjeno!", Toast.LENGTH_LONG).show();
            return;
        }
        while (res.moveToNext()) {
            Uredjaj u = new Uredjaj();
            u.setId(res.getLong(0));
            u.setBleName(res.getString(1));
            u.setObjectName(res.getString(2));
            u.setConnected(res.getInt(3));
            uredjaji.add(u);
        }
    }

    public void deleteData() {
        myDb.deleteAll();

    }

    public String napraviString() {
        String rec = "";
        for (int i = 0; i < values.size(); i++) {
            rec = rec + " " + values.get(i);
        }
        return rec;
    }

    public void konektujSe(View view) {
        if (i < values.size()) {
            Controller.dajInstrukcije(this, values.get(i));
            i++;
        } else {
            Controller.dajInstrukcije(this, values.get(0));
            i = 1;
        }
    }

}