package com.example.tombelgradeapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DataController extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="Baza.db";
    public static final String TABLE_NAME="Uredjaji";
    public static final String COLUMN_1="Id";
    public static final String COLUMN_2="BleName";
    public static final String COLUMN_3="ObjectName";
    public static final String COLUMN_4="Connected";
    public DataController(Context context) {
        super(context,DATABASE_NAME,null,4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (Id INTEGER PRIMARY KEY AUTOINCREMENT, BleName TEXT, ObjectName TEXT, Connected INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public void kreirajBazu(){

    }
    public boolean insertData(String bleName, String objectName,int connected, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_2, bleName);
        contentValues.put(COLUMN_3, objectName);
        contentValues.put(COLUMN_4, connected);
        long result = db.insert(TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    private void clearTable(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE Id > 0");
    }

    public void initDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        this.clearTable(db);
        this.insertData("Buzz-01", "Majica", 0, db);
        this.insertData("Buzz-02", "Farmerke", 0, db);
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }

    public String daLiPostoji(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        if (res.getCount() == 0) {
            return "";
        }
        System.out.println(name);
        while (res.moveToNext()) {
            if(res.getString(2).toLowerCase().equals(name.toLowerCase())){
                return res.getString(1);
            }

        }
        return "";
    }

    public void ispisiBazu() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        if (res.getCount() == 0) {
            System.out.println("Prazna baza");
        }
        while (res.moveToNext()) {
            System.out.println(res.getString(5));
        }
    }

    public boolean updateData(String id,String bleName, String objectName, int connection) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_1,id);
        contentValues.put(COLUMN_2,bleName);
        contentValues.put(COLUMN_3,objectName);
        contentValues.put(COLUMN_4,connection);
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
