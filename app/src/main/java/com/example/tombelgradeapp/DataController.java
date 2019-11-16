package com.example.tombelgradeapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataController extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="Baza.db";
    public static final String TABLE_NAME="Uredjaji";
    public static final String COLUMN_1="Id";
    public static final String COLUMN_2="UUID";
    public static final String COLUMN_3="Rx";
    public static final String COLUMN_4="Tx";
    public static final String COLUMN_5="Name";
    public static final String COLUMN_6="Connected";
    public DataController(Context context) {
        super(context,DATABASE_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (Id INTEGER PRIMARY KEY AUTOINCREMENT,UUID TEXT,Rx TEXT,Tx TEXT,Name TEXT,Connected INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public boolean insertData(Long id,String uuid,String rx,String tx,String name,int conn) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_1,id);
        contentValues.put(COLUMN_2,uuid);
        contentValues.put(COLUMN_3,rx);
        contentValues.put(COLUMN_4,tx);
        contentValues.put(COLUMN_5,name);
        contentValues.put(COLUMN_6,conn);
        long result = db.insert(TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }

    public boolean updateData(String id,String uuid,String rx,String tx,String name,int conn) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_1,id);
        contentValues.put(COLUMN_2,uuid);
        contentValues.put(COLUMN_3,rx);
        contentValues.put(COLUMN_4,tx);
        contentValues.put(COLUMN_5,name);
        contentValues.put(COLUMN_6,conn);
        db.update(TABLE_NAME, contentValues, "ID = ?",new String[] { id });
        return true;
    }

    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?",new String[] {id});
    }
    public void deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME);
    }


}
