package com.winotech.cicerone.misc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.winotech.cicerone.model.Cicerone;
import com.winotech.cicerone.model.Globetrotter;
import com.winotech.cicerone.model.Language;
import com.winotech.cicerone.model.User;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DBManager implements Serializable {

    private DBHelper dbHelper = null;


    /*
    Costruttore della classe
     */
    public DBManager(Context context) {

        dbHelper = new DBHelper(context);

        // apertura in lettura del database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
    }


    /*
    Metodo per il savlvataggio dell'utente nel db
     */
    public void saveUser (String username, String email, String password, String first_name,
                          String last_name, String phone_number, Bitmap photo, String biography, String role) {

        // acquisizione del puntatore al db
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // creazione dell'oggetto per il salvataggio dei dati e riempimento dei campi
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.KEY_USER_USERNAME, username);
        contentValues.put(Constants.KEY_USER_EMAIL, email);
        contentValues.put(Constants.KEY_USER_AUTH_CODE, password);
        contentValues.put(Constants.KEY_USER_FIRST_NAME, first_name);
        contentValues.put(Constants.KEY_USER_LAST_NAME, last_name);
        contentValues.put(Constants.KEY_USER_PHONE_NUMBER, phone_number);
        contentValues.put(Constants.KEY_USER_PHOTO, bitmapToBlob(photo));
        contentValues.put(Constants.KEY_USER_BIOGRAPHY, biography);
        contentValues.put(Constants.KEY_USER_ROLE, role);

        try {

            // inserimento dei dati
            db.insert(Constants.TABLE_USER, null, contentValues);

            Log.d("Save User", "User correct stored" + username);

            db.close();

        } catch (SQLiteException e) {

            Log.d("Save User Exception", e.getClass() + e.getMessage());
        }
    }


    /*
    Metodo per il savlvataggio delle lingue parlate dagli utenti nel db
     */
    public void saveLanguage (int id, String name) {

        // acquisizione del puntatore al db
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // creazione dell'oggetto per il salvataggio dei dati e riempimento dei campi
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.KEY_LANGUAGE_ID, id);
        contentValues.put(Constants.KEY_LANGUAGE_NAME, name);


        try {

            // inserimento dei dati
            db.insert(Constants.TABLE_LANGUAGE, null, contentValues);

            Log.d("Save Language", "Language correct stored");

            db.close();

        } catch (SQLiteException e) {

            Log.d("Save Language Exception", e.getClass() + e.getMessage());
        }
    }


    /*
    Metodo per il savlvataggio delle lingue parlate dagli utenti nel db
     */
    public void saveUserSpokenLanguage (int language_id, String username_id) {

        // acquisizione del puntatore al db
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // creazione dell'oggetto per il salvataggio dei dati e riempimento dei campi
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.KEY_USERSPOKENLANGUAGE_USERNAME, username_id);
        contentValues.put(Constants.KEY_USERSPOKENLANGUAGE_LANGUAGE, language_id);

        try {

            // inserimento dei dati
            db.insert(Constants.TABLE_USERSPOKENLANGUAGE, null, contentValues);

            Log.d("Save UserSpokenLanguage", "UserSpokenLanguage correct stored");

            db.close();

        } catch (SQLiteException e) {

            Log.d("Save UserSpok. Except.", e.getClass() + e.getMessage());
        }
    }


    /*
    Metodo per la cancellazione della lingua parlata dall'utente nel db
     */
    public void deleteUserSpokenLanguage (int language_id, String username_id) {

        // acquisizione del puntatore al db
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {

            String whereClause = Constants.KEY_USERSPOKENLANGUAGE_USERNAME + "=? AND " +
                    Constants.KEY_USERSPOKENLANGUAGE_LANGUAGE + "=?";

            String language = String.valueOf(language_id);

            // inserimento dei dati
            db.delete(Constants.TABLE_USERSPOKENLANGUAGE, whereClause, new String[]{username_id, language});

            Log.d("Del UserSpokenLanguage", "UserSpokenLanguage correct deleted");

            db.close();

        } catch (SQLiteException e) {

            Log.d("Delete UserSpoken Exc", e.getClass() + e.getMessage());
        }
    }




    /*
    Metodo per l'acquisizione delle lingue dal database locale
     */
    public ArrayList<Language> getLanguageList () {

        // acquisizione del puntatore al db
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {

            // query di acquisizione delle info dell'utente
            String query = "SELECT * FROM " + Constants.TABLE_LANGUAGE;

            // esecuzione della query di acquisizione dati
            Cursor cursor = db.rawQuery(query, null);

            ArrayList<Language> languages = new ArrayList<>();

            // se si è acquisito qualcosa
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {

                do{
                    // creazione della lingua
                    Language lang = new Language(
                            cursor.getInt(0),
                            cursor.getString(1));

                    // aggiunta lingua alla lista
                    languages.add(lang);

                } while(cursor.moveToNext());
            }

            // chiusura del database
            db.close();

            // restituzione dell'utetne
            return languages;

        } catch (SQLiteException e) {

            Log.d("Read Languages", "Error in getting languages from local db");
            return null;
        }
    }




    /*
    Metodo per l'acquisizione dell'utente
     */
    public User getUser(String username) {

        // acquisizione del puntatore al db
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {

            String query = "SELECT * FROM " + Constants.TABLE_USER +
            " WHERE " + Constants.KEY_USER_USERNAME + "=?";

            // esecuzione della query di acquisizione dati
            Cursor cursor = db.rawQuery(query, new String[]{username});

            User user = null;

            // se si è acquisito qualcosa
            if (cursor.getCount() > 0) {

                cursor.moveToFirst();


                // inizializzazione dell'utente
                if (cursor.getString(8).equals("1"))
                    user = new Cicerone();
                else //if (cursor.getString(8).equals("0"))
                    user = new Globetrotter();

                // definizione dei campi di un oggetto user
                user.setUsername(cursor.getString(0));
                user.setEmail(cursor.getString(1));
                user.setPassword(cursor.getString(2));
                user.setFirstName(cursor.getString(3));
                user.setLastName(cursor.getString(4));
                user.setPhone(cursor.getString(5));
                user.setPhoto(blobToBitmap(cursor.getBlob(6)));
                user.setBiography(cursor.getString(7));

                // todo set_language
            }

            db.close();

            // restituzione dell'utetne
            return user;

        } catch (SQLiteException e) {

            Log.d("Save User", "Error in getting user from local db");
            return null;

        } catch (NullPointerException e) {

            Log.d("Save User", "Error in getting user from local db. Get null pointer");
            return null;
        }
    }

    /*
    Metodo per il salvataggio dell'utente nel db
     */
    public void updateUser (String username, String email, String password, String first_name,
                          String last_name, String phone_number, Bitmap photo, String biography) {

        // acquisizione del puntatore al db
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // creazione dell'oggetto per il salvataggio dei dati e riempimento dei campi
        ContentValues contentValues = new ContentValues();

        if(email != null)
            contentValues.put(Constants.KEY_USER_EMAIL, email);

        if(password != null)
            contentValues.put(Constants.KEY_USER_AUTH_CODE, password);

        if(first_name!= null)
            contentValues.put(Constants.KEY_USER_FIRST_NAME, first_name);

        if(last_name != null)
            contentValues.put(Constants.KEY_USER_LAST_NAME, last_name);

        if(phone_number != null)
            contentValues.put(Constants.KEY_USER_PHONE_NUMBER, phone_number);

        if(photo != null)
            contentValues.put(Constants.KEY_USER_PHOTO, bitmapToBlob(photo));

        if(biography != null)
            contentValues.put(Constants.KEY_USER_BIOGRAPHY, biography);

        try {

            String whereClause =  Constants.KEY_USER_USERNAME + "=?";

            // inserimento dei dati
            db.update(Constants.TABLE_USER, contentValues, whereClause, new String[]{username});

            Log.d("Update User", "User correct updated" + username);

            db.close();

        } catch (SQLiteException e) {

            Log.d("Update User Exception", e.getClass() + e.getMessage());
        }
    }


    /*
    Metodo per il salvataggio delle lingue parlate dall'utente
     */
    public void updateSpokenLanguages (String username, HashMap<Integer, Boolean> spoken_languages) {

        // acquisizione del puntatore al db
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {

            // query di acquisizione delle lingue parlate dall'utente
            String query = "SELECT * FROM " + Constants.TABLE_USERSPOKENLANGUAGE + " WHERE " +
                    Constants.KEY_USERSPOKENLANGUAGE_USERNAME + "=?";

            // esecuzione della query di acquisizione dati
            Cursor cursor = db.rawQuery(query, new String[]{username});

            // definizione della lista degli indici delle lingue
            ArrayList<Integer> languages = new ArrayList<>();

            // se ci sono lingue parlate
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {

                // le si aggiungono alla lista delle lingue
                do{
                    languages.add(cursor.getInt(1));

                } while(cursor.moveToNext());
            }

            // analizzando ogni elemento delle lingue parlate
            for (Map.Entry<Integer, Boolean> lang : spoken_languages.entrySet()){

                // se la lingua è stata selezionata come parlata e non esiste nella
                // lista delle lingue acquisite dal db vuol dire che è stata appena settata
                if(lang.getValue() && !languages.contains(lang.getKey())) {

                    // definizione dell'id della lingua
                    int language_id = lang.getKey();

                    // salvataggio della lingua parlata nel database locale
                    saveUserSpokenLanguage(language_id, username);

                } else if (!lang.getValue() && languages.contains(lang.getKey())) {
                    // se la lingua non è stata selezionata come parlata ed esiste nella
                    // lista delle lingue acquisite dal db vuol dire che è stata appena cancellata

                    // definizione dell'id della lingua
                    int language_id = lang.getKey();

                    // salvataggio della lingua parlata nel database locale
                    deleteUserSpokenLanguage(language_id, username);
                }
            }

            db.close();

        } catch (SQLiteException e) {

            Log.d("Update SpokenLang Exc", e.getClass() + e.getMessage());
        }


    }


    /*
    Metodo per l'acquisizione del proprio account dal db
     */
    public User getMyAccount () {

        // acquisizione del puntatore al db
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {

            // query di acquisizione delle info dell'utente
            String query = "SELECT * FROM " + Constants.TABLE_USER +
                    " LIMIT 1 ";

            // esecuzione della query di acquisizione dati
            Cursor cursor = db.rawQuery(query, null);

            User user = null;

            // se si è acquisito qualcosa
            if (cursor.getCount() > 0) {

                cursor.moveToFirst();

                // inizializzazione dell'utente
                if (cursor.getString(8).equals("1"))
                    user = new Cicerone();
                else //if (cursor.getString(8).equals("0"))
                    user = new Globetrotter();

                // definizione dei campi di un oggetto user
                user.setUsername(cursor.getString(0));
                user.setEmail(cursor.getString(1));
                user.setPassword(cursor.getString(2));
                user.setFirstName(cursor.getString(3));
                user.setLastName(cursor.getString(4));
                user.setPhone(cursor.getString(5));
                user.setPhoto(blobToBitmap(cursor.getBlob(6)));
                user.setBiography(cursor.getString(7));
            }


            // query per l'acquisizione delle info sulle lingue parlate
            query = "SELECT "+ Constants.KEY_LANGUAGE_ID + "," + Constants.KEY_LANGUAGE_NAME +
                    " FROM " + Constants.TABLE_USERSPOKENLANGUAGE + "," + Constants.TABLE_LANGUAGE +
                    " WHERE " + Constants.KEY_USERSPOKENLANGUAGE_LANGUAGE + "=" + Constants.KEY_LANGUAGE_ID +
                    " AND " + Constants.KEY_USERSPOKENLANGUAGE_USERNAME + "=?";

            // esecuzione della query di acquisizione dati
            if (user != null) {
                cursor = db.rawQuery(query, new String[]{user.getUsername()});
            }

            // definizione di una nuova lista di lingue
            ArrayList<Language> languages = new ArrayList<>();

            // se si è acquisito qualcosa
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {

                do {

                    // definizione di un nuovo linguaggio
                    Language foundLanguage = new Language(
                            cursor.getInt(0),
                            cursor.getString(1)
                    );

                    // aggiunta del linguaggio alla lista delle lingue parlate
                    languages.add(foundLanguage);

                } while(cursor.moveToNext());
            }

            // settaggio delle lingue parlate all'utente
            if (user != null) {
                user.setSpokenLanguages(languages);
            }


            // chiusura del database
            db.close();

            // restituzione dell'utetne
            return user;

        } catch (SQLiteException e) {

            Log.d("Save User", "Error in getting my account from local db");
            return null;

        } catch (NullPointerException e) {

            Log.d("Save User", "Error in getting my account from local db. Get null pointer");
            return null;
        }
    }








    /*
    Metodo per eliminare tutti i dati dal db locale
     */
    public void removeAll () {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // cancella tutte le tabelle
        db.delete(Constants.TABLE_USER, null, null);
        db.delete(Constants.TABLE_LANGUAGE, null, null);
        db.delete(Constants.TABLE_USERSPOKENLANGUAGE, null, null);

        // chiudi la connessione al db
        db.close();
    }



    /*
    Metodo per la conversione da BLOB a Bitmap
     */
    public Bitmap blobToBitmap(byte[] blob) {

        try {

            Bitmap bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
            return bitmap;

        } catch (Exception e) {

            Log.d("blobToBitmap", e.getClass() + " " + e.getMessage());
            return null;
        }
    }


    /*
    Metodo per la conversione da Bitmap a BLOB
     */
    public byte[] bitmapToBlob(Bitmap bitmap) {

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            byte[] blob = bos.toByteArray();

            return blob;

        } catch (Exception e) {

            Log.d("bitmapToBlob", e.getClass() + " " + e.getMessage());

            return null;
        }
    }

}
