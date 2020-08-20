package com.winotech.cicerone.controller;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.model.Language;
import com.winotech.cicerone.view.SettingsFragment;

import java.util.Locale;


public class LanguageController {

    private static SharedPreferences mSharedPref;
    public static final String language = "language";

    private LanguageController()
    {

    }

    //definizione dello shared preferences
    public static void init(Context context)
    {
        if(mSharedPref == null)
            mSharedPref = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
    }

    //definizione della funzione di lettura nello sharedpreferences
    public static String read(String key, String defValue) {

        return mSharedPref.getString(key, defValue);
    }

    //definizione della funzione di scrittura nello sharedpreferences
    public static void write(String key, String value) {

        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putString(key, value);
        prefsEditor.commit();
    }

    //definizione della funzione di rimozione nello sharedpreferences
    public static void remove(String key){

        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.remove(key);
        prefsEditor.commit();

    }

    public static boolean read(String key, boolean defValue) {

        return mSharedPref.getBoolean(key, defValue);
    }

    public static void write(String key, boolean value) {

        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.commit();
    }

    public static Integer read(String key, int defValue) {

        return mSharedPref.getInt(key, defValue);
    }

    public static void write(String key, Integer value) {

        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putInt(key, value).commit();
    }


    public static void updateResources(Context context, String language) {

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }


}


