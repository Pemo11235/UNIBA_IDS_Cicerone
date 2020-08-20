package com.winotech.cicerone.misc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.Serializable;

public class DBHelper extends SQLiteOpenHelper implements Serializable {

    // tag univoco per il debug della classe
    private static final String TAG = DBManager.class.getSimpleName();

    // versione del database
    private static final int DATABASE_VERSION = 1;


    public DBHelper (Context context){

        super(context, Constants.DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
    Metodo per la creazione delle tabelle
     */
    @Override
    public void onCreate (SQLiteDatabase db) {

        // definizione della query da eseguire
        String query = "CREATE TABLE " + Constants.TABLE_USER + " (" +
                Constants.KEY_USER_USERNAME + " TEXT PRIMARY KEY, " +
                Constants.KEY_USER_EMAIL + " TEXT, " +
                Constants.KEY_USER_AUTH_CODE + " TEXT, " +
                Constants.KEY_USER_FIRST_NAME + " TEXT, " +
                Constants.KEY_USER_LAST_NAME + " TEXT, " +
                Constants.KEY_USER_PHONE_NUMBER + " TEXT, " +
                Constants.KEY_USER_PHOTO + " BLOB, " +
                Constants.KEY_USER_BIOGRAPHY + " TEXT, " +
                Constants.KEY_USER_ROLE + " TEXT)";

        // esecuzione della query
        db.execSQL (query);

        // definizione della query da eseguire
        query = "CREATE TABLE " + Constants.TABLE_LANGUAGE + " (" +
                Constants.KEY_LANGUAGE_ID + " INT PRIMARY KEY, " +
                Constants.KEY_LANGUAGE_NAME + " TEXT)";

        // esecuzione della query
        db.execSQL (query);

        // definizione della query da eseguire
        query = "CREATE TABLE " + Constants.TABLE_USERSPOKENLANGUAGE + " (" +
                Constants.KEY_USERSPOKENLANGUAGE_USERNAME + " TEXT, " +
                Constants.KEY_USERSPOKENLANGUAGE_LANGUAGE + " INTEGER, " +
                "PRIMARY KEY (" +
                Constants.KEY_USERSPOKENLANGUAGE_USERNAME + "," +
                Constants.KEY_USERSPOKENLANGUAGE_LANGUAGE + "))";

        // esecuzione della query
        db.execSQL (query);

        // messaggio di debug
        Log.d (TAG, "Database tables created");
    }

    /*
    Metodo per l'aggiornamento delle tabelle
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // cancellazione delle tebelle se esistono
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_LANGUAGE);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_USERSPOKENLANGUAGE);

        // re-creazione delle tabelle
        onCreate(db);
    }

}
