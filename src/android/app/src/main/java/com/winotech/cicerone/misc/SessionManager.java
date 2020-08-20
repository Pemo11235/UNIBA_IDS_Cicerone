package com.winotech.cicerone.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import java.io.Serializable;
import java.util.Locale;

/*
Classe per la definizione del manager dei dati di sessione all'interno dell'app.
 */
public class SessionManager implements Serializable {

    private static String TAG = SessionManager.class.getSimpleName();

    transient SharedPreferences preferences;

    transient Editor editor;
    transient Context context;

    int PRIVATE_MODE = 0;

    private static final String PREFERENCES_NAME = "ciceronePreferences";

    // chiavi di accesso per il preferences
    private static final String IS_LOGGED_IN_KEY = "isLoggedIn";
    private static final String LANGUAGE_KEY = "language";
    private static final String IS_CICERONE_KEY = "isCicerone";

    /*
    Costruttore della classe
     */
    public SessionManager (Context context) {

        this.context = context;
        preferences = context.getSharedPreferences(PREFERENCES_NAME, PRIVATE_MODE);
        editor = preferences.edit();
    }


    /*
     Metodo per il settaggio della persistenza del login
     */
    public void setLogin(boolean is_logged_in) {

        editor.putBoolean(IS_LOGGED_IN_KEY, is_logged_in);
        editor.commit();
        Log.d(TAG, "User login session modified");
    }


    /*
    Metodo per l'acquisizione del flag di persistenza di login
     */
    public boolean isLoggedIn() {

        return preferences.getBoolean(IS_LOGGED_IN_KEY, false);
    }


    /*
    Metodo per l'acquisizione del linguaggio corrente
     */
    public String getLanguage() {

        return preferences.getString(LANGUAGE_KEY, null);
    }


    /*
    Metodo per il settaggio della lingua
     */
    public void setLanguage(String language) {

        editor.putString(LANGUAGE_KEY, language);
        editor.commit();
        Log.d(TAG, "App language modified");

        //updateLanguage(context, language);
    }


    public void updateLanguage(Context context, String language) {

        setLanguage(language);

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();


        Configuration configuration = resources.getConfiguration();

        Log.d("UpdateLanguage", "Actual locale: " + configuration.locale.toString() + "future locale: " + locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            configuration.setLocale(locale);
        else
            configuration.locale = locale;


        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

}
