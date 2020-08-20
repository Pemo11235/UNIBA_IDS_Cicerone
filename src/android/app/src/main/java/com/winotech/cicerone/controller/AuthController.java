package com.winotech.cicerone.controller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.winotech.cicerone.R;
import com.winotech.cicerone.misc.AppConfiguration;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.misc.SessionManager;
import com.winotech.cicerone.model.Cicerone;
import com.winotech.cicerone.model.Tour;
import com.winotech.cicerone.view.LoginActivity;
import com.winotech.cicerone.view.MainActivity;
import com.winotech.cicerone.view.RegisterActivity;
import com.winotech.cicerone.view.StartActivity;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class AuthController implements Serializable {

    // context dell'applicazione
    private Context context;
    private DBManager local_db;

    private ProgressDialog dialog;

    private SessionManager sessionManager;

    // flag per definire se l'operazione effettuata è andata a buon fine
    private boolean success;


    // Costruttore del controller
    public AuthController (Context context, SessionManager manager) {

        this.context = context;

        // controller della connessione con il database locale
        local_db = new DBManager(context);

        // settaggio del session manager
        sessionManager = manager;
    }


    /*
    Metodo per la registrazione di un nuovo utente
     */
    public boolean register(final String username, final String email, final String password,
                            final String first_name, final String last_name,
                            final String phone_number, final boolean role) {

        // finestra di dialogo
        dialog = new ProgressDialog (context);
        dialog.setCancelable (false);

        // definizione del messaggio di dialogo per la registrazione in corso
        dialog.setMessage(context.getResources().getString(R.string.signing_in));

        // visualizzazione del messaggio di dialogo
        show_dialog();

        // inizializzazione della variabile, se avviene qualche errore viene settata a false
        success = true;

        // definizione di una richiesta per la registrazione di un nuovo utente
        final StringRequest string_request = new StringRequest(Request.Method.POST,
                AppConfiguration.URL_FILE_REGISTER, new Response.Listener<String>() {

            // definizione del comportamento in risposta
            @Override
            public void onResponse(String response) {

                // messaggio di debug con conseguente camuffamento della pagina di dialogo
                Log.d(RegisterActivity.class.getSimpleName(), "Register Response: " + response);
                hide_dialog();

                try {

                    Log.d("response REST php API", response);

                    // definizione di un oggetto json dalla risposta ricevuta
                    JSONObject json = new JSONObject (response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));

                    Log.d("JSON to REST php API", json.toString());

                    // definizione dell'errore accaduto
                    boolean error = json.getBoolean("error");

                    // se l'errore è FALSE
                    if (!error) {

                        // definizione di un novo oggetto JSON acquisendo, dalla risposta presa,
                        // l'array associativo di nome "user" (e trasformato in oggetto JSON)
                        JSONObject user = json.getJSONObject("user");

                        Log.d("JSON to REST php API", user.toString());

                        // messaggio di corretta registrazione
                        Toast.makeText(context, context.getResources().getString(R.string.success_sign_in),
                                Toast.LENGTH_LONG).show();

                        // definizione di un collegamento tra questa activity e l'activity di login
                        Intent intent = new Intent (context, StartActivity.class);

                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        // inizio della nuova activity
                        context.startActivity(intent);

                        // conclusione di questa activity
                        //finish();

                        //ritorna l'esecuzione effettuata con successo
                        success = true;

                    } else { // errore nella registrazione

                        // visualizzazione del messaggio di errore nella registrazione
                        Toast.makeText(context, json.getString("error_msg"),
                                Toast.LENGTH_LONG).show();

                        success = false;
                    }

                } catch (JSONException e) {// gestione dell'eccezione

                    // errore nel file JSON passato
                    e.printStackTrace();
                    Toast.makeText(context, "JSON error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    success = false;
                }
            }

        }, new Response.ErrorListener () {

            // definizione del comportamento all'errore generico di login
            @Override
            public void onErrorResponse (VolleyError error) {

                // messaggio di debug
                Log.e (RegisterActivity.class.getSimpleName(), "Registration Error: " + error.getMessage());

                // visualizzazione del messaggio di errore
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();

                // camuffamento della finestra di dialogo
                hide_dialog();

                success = false;
            }

        }) { // definizione del comportamento dello string request

            @Override
            protected Map<String, String> getParams() {

                // Definizione della mappa dei parametri inseriti
                Map<String, String> parameters = new HashMap<String, String>();

                // inserimento nella mappa dei parametri inseriti
                parameters.put ("username", username);
                parameters.put ("email", email);
                parameters.put ("password", password);
                parameters.put ("first_name", first_name);
                parameters.put ("last_name", last_name);
                parameters.put ("phone_number", phone_number);
                parameters.put ("image_path", Constants.DEFAULT_IMAGE_PATH);
                if (role)
                    parameters.put ("role", "1");
                else
                    parameters.put ("role", "0");

                // restituzione della mappa dei parametri
                return parameters;
            }
        };

        // definizione del timeout per l'annullamento della richiesta
        int socket_timeout = 30000;

        // definizione della policy per il timeout dell'annullamento della richiesta
        RetryPolicy policy = new DefaultRetryPolicy(socket_timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        // definizione della policy sulla richiesta
        string_request.setRetryPolicy (policy);

        Log.d("tag", "sto analizzando");

        // Aggiunta della richiesta nelle code delle richieste
        AppController.get_instance().add_to_request_queue (string_request, "req_register");

        return success;
    }


    /*
    Metodo per l'autenticazione dell'utente
     */
    public boolean authentication (final String email, final String password) {

        // finestra di dialogo
        dialog = new ProgressDialog (context);
        dialog.setCancelable (false);

        // definizione del messaggio per il progress dialog e succesiva mostra
        dialog.setMessage (context.getResources().getString(R.string.logging_in));
        show_dialog();

        // inizializzazione della variabile, se avviene qualche errore viene settata a false
        success = true;

        // definizione della comunicazione con il database
        StringRequest string_request = new StringRequest(Request.Method.POST,
                AppConfiguration.URL_FILE_LOGIN, new Response.Listener<String>() {

            // definizione del comportamento di risposta alla richiesta
            @Override
            public void onResponse(String response) {

                // messaggio di debug
                Log.d(LoginActivity.class.getSimpleName(), "Login response: " + response);

                // camuffamento della finestra di dialogo
                hide_dialog();

                try {

                    // definizione del file JSON dalla risposta
                    JSONObject json = new JSONObject(response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));

                    // acquisizione dell'errore dal file JSON
                    boolean error = json.getBoolean("error");

                    // se l'errore è FALSE
                    if (!error) {

                        // definizione di un novo oggetto JSON acquisendo, dalla risposta presa,
                        // l'array associativo di nome "user" (e trasformato in oggetto JSON)
                        JSONObject user = json.getJSONObject("user");

                        // l'utente è stato correttamente loggato, dunque crea la sessione di login
                        sessionManager.setLogin(true);

                        // setta la lingua iniziale
                        //if(sessionManager.getLanguage() == null)
                        //sessionManager.setLanguage("en");//context.getResources().getConfiguration().locale.toString());
                        sessionManager.updateLanguage(context, context.getResources().getConfiguration().locale.toString().substring(0, 2));

                        Log.d("AppLanguage", "app language: " + sessionManager.getLanguage());

                        // aggiunta dell'utente al database locale
                        local_db.saveUser(
                                user.getString("username"),
                                user.getString("email"),
                                user.getString("password"),
                                user.getString("first_name"),
                                user.getString("last_name"),
                                user.getString("phone"),
                                getBitmapFromURL(AppConfiguration.URL_PREFIX + user.getString("image_path")),
                                user.getString("biography"),
                                user.getString("role")
                        );

                        //UserController userController = new UserController(local_db, context);
                        //userController.getMyAccountData();
                        getMyAccountData();

                        TourController.initInstance(context);
                        TourController tourController = TourController.getInstance();

                        tourController.getLoadedTours().clear();
                        tourController.getLoadedEvents().clear();
                        tourController.getLoadedLanguages().clear();


                        // definizione di un collegamento tra questa activity e l'activityprincipale
                        Intent intent = new Intent(context, MainActivity.class);

                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Constants.FRAGMENT_TO_LOAD_KEY, Constants.MY_TOURS_FRAGMENT_VALUE);

                        // inizio della nuova activity
                        context.startActivity(intent);

                        // conclusione di questa activity
                        //finish();

                        //ritorna l'esecuzione effettuata con successo
                        success = true;

                    } else {

                        // visualizzazione di un messaggio di errore nel login
                        Toast.makeText(context, json.getString("error_msg"),
                                Toast.LENGTH_LONG).show();

                        success = false;
                    }

                } catch (JSONException e) { // gestione dell'eccezione

                    // errore nel file JSON passato
                    e.printStackTrace();
                    Toast.makeText(context, "JSON error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    success = false;
                }
            }


        }, new Response.ErrorListener() {

            // definizione del comportamento all'errore generico di login
            @Override
            public void onErrorResponse(VolleyError error) {

                // messaggio di debug
                Log.e (LoginActivity.class.getSimpleName(), "Login Error: " + error.getMessage());

                // visualizzazione del messaggio di errore
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();

                // camuffamento della finestra di dialogo
                hide_dialog();

                success = false;
            }

        }) { // definizione del comportamento dello string request

            /*
            Metodo per il ritorno dei parametri inseriti
             */
            @Override
            protected Map<String, String> getParams() {

                // Definizione della mappa dei parametri inseriti
                Map<String, String> parameters = new HashMap<String, String>();

                // inserimento nella mappa dei parametri inseriti
                parameters.put ("email", email);
                parameters.put ("password", password);

                // restituzione della mappa dei parametri
                return parameters;
            }
        };

        // aggiunta di una richiesta alla coda delle richieste
        AppController.get_instance().add_to_request_queue (string_request, "login_request");

        return success;
    }


    /*
    Metodo per la ridefinizione della password
     */
    public boolean recovery(final String email) {

        // finestra di dialogo
        dialog = new ProgressDialog (context);
        dialog.setCancelable (false);

        // definizione del messaggio per il progress dialog e succesiva mostra
        dialog.setMessage (context.getResources().getString(R.string.sending_mail));
        show_dialog();

        // inizializzazione della variabile, se avviene qualche errore viene settata a false
        success = true;

        // definizione della comunicazione con il database
        StringRequest string_request = new StringRequest(Request.Method.POST,
                AppConfiguration.URL_FILE_RECOVERY, new Response.Listener<String>() {

            // definizione del comportamento di risposta alla richiesta
            @Override
            public void onResponse(String response) {

                // messaggio di debug
                Log.d(LoginActivity.class.getSimpleName(), "Recovery response: " + response);

                // camuffamento della finestra di dialogo
                hide_dialog();

                try {

                    // definizione del file JSON dalla risposta
                    JSONObject json = new JSONObject(response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));

                    // acquisizione dell'errore dal file JSON
                    boolean error = json.getBoolean("error");

                    // se l'errore è FALSE
                    if (!error) {

                        // definizione di un novo oggetto JSON acquisendo, dalla risposta presa,
                        // l'array associativo di nome "user" (e trasformato in oggetto JSON)
                        //JSONObject user = json.getJSONObject("user");

                        // definizione di un collegamento tra questa activity e l'activityprincipale
                        Intent intent = new Intent(context, StartActivity.class);

                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        // inizio della nuova activity
                        context.startActivity(intent);

                        // conclusione di questa activity
                        //finish();

                        //ritorna l'esecuzione effettuata con successo
                        success = true;

                    } else {

                        // visualizzazione di un messaggio di errore nel login
                        Toast.makeText(context, json.getString("error_msg"),
                                Toast.LENGTH_LONG).show();

                        success = false;
                    }

                } catch (JSONException e) { // gestione dell'eccezione

                    // errore nel file JSON passato
                    e.printStackTrace();
                    Toast.makeText(context, "JSON error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    success = false;
                }
            }


        }, new Response.ErrorListener() {

            // definizione del comportamento all'errore generico di login
            @Override
            public void onErrorResponse(VolleyError error) {

                // messaggio di debug
                Log.e (LoginActivity.class.getSimpleName(), "Recovery Error: " + error.getMessage());

                // visualizzazione del messaggio di errore
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();

                // camuffamento della finestra di dialogo
                hide_dialog();

                success = false;
            }

        }) { // definizione del comportamento dello string request

            /*
            Metodo per il ritorno dei parametri inseriti
             */
            @Override
            protected Map<String, String> getParams() {

                // Definizione della mappa dei parametri inseriti
                Map<String, String> parameters = new HashMap<String, String>();

                // inserimento nella mappa dei parametri inseriti
                parameters.put ("email", email);
                parameters.put ("email_object", context.getResources().getString(R.string.recovery_email_object));
                parameters.put ("email_body", context.getResources().getString(R.string.recovery_email_body));
                parameters.put ("email_password", context.getResources().getString(R.string.recovery_email_password));
                parameters.put ("email_conclusion", context.getResources().getString(R.string.recovery_email_conclusion));

                // restituzione della mappa dei parametri
                return parameters;
            }
        };

        // aggiunta di una richiesta alla coda delle richieste
        AppController.get_instance().add_to_request_queue (string_request, "recovery_request");

        return success;
    }

    /*
    Metodo per il logout dell'utente
     */
    public void logout() {

        try {

            sessionManager.setLogin(false);

            // cancellazione di tutte le tabelle del db
            local_db.removeAll();

            // ritorna all'activity iniziale
            Intent intent = new Intent(context, StartActivity.class);

            // svuota lo stack delle attività
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // inizio della nuova activity
            context.startActivity(intent);

        } catch (Exception e) {

            Log.d("Logout", e.getClass() + " " + e.getMessage());
        }

    }


    /*
    Metodo per la modifica dei dati personali
     */
    public boolean editPersonalInfo(final String username, final String previous_email,
                                    final String email, final String first_name,
                                    final String last_name, final String phone_number,
                                    final String biography, final Bitmap photo) {


        // inizializzazione della variabile, se avviene qualche errore viene settata a false
        success = true;

        // definizione di una richiesta per la registrazione di un nuovo utente
        final StringRequest string_request = new StringRequest(Request.Method.POST,
                AppConfiguration.URL_FILE_EDIT_PERSONAL_INFOS, new Response.Listener<String>() {

            // definizione del comportamento in risposta
            @Override
            public void onResponse(String response) {

                // messaggio di debug con conseguente camuffamento della pagina di dialogo
                Log.d(RegisterActivity.class.getSimpleName(), "Edit Response: " + response);

                try {

                    Log.d("response REST php API", response);

                    // definizione di un oggetto json dalla risposta ricevuta
                    JSONObject json = new JSONObject (response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));

                    Log.d("JSON to REST php API", json.toString());

                    // definizione dell'errore accaduto
                    boolean error = json.getBoolean("error");

                    // se l'errore è FALSE
                    if (!error) {

                        // definizione di un novo oggetto JSON acquisendo, dalla risposta presa,
                        // l'array associativo di nome "user" (e trasformato in oggetto JSON)
                        JSONObject user = json.getJSONObject("user");

                        Log.d("JSON to REST php API", user.toString());

                        // messaggio di corretta registrazione
                        Toast.makeText(context, context.getResources().getString(R.string.success_edit),
                                Toast.LENGTH_LONG).show();

                        // aggiunta dell'utente al database locale
                        local_db.updateUser(
                                user.getString("username"),
                                user.getString("email"),
                                null,
                                user.getString("first_name"),
                                user.getString("last_name"),
                                user.getString("phone_number"),
                                getBitmapFromURL(AppConfiguration.URL_PREFIX + user.getString("image_path")),
                                user.getString("biography")
                        );

                        //ritorna l'esecuzione effettuata con successo
                        success = true;

                    } else { // errore nella registrazione

                        // visualizzazione del messaggio di errore nella registrazione
                        Toast.makeText(context, json.getString("error_msg"),
                                Toast.LENGTH_LONG).show();

                        success = false;
                    }

                } catch (JSONException e) {// gestione dell'eccezione

                    // errore nel file JSON passato
                    e.printStackTrace();
                    Toast.makeText(context, "JSON error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    success = false;
                }
            }

        }, new Response.ErrorListener () {

            // definizione del comportamento all'errore generico di login
            @Override
            public void onErrorResponse (VolleyError error) {

                // messaggio di debug
                Log.e (RegisterActivity.class.getSimpleName(), "Edit Error: " + error.getMessage());

                // visualizzazione del messaggio di errore
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();

                success = false;
            }

        }) { // definizione del comportamento dello string request

            @Override
            protected Map<String, String> getParams() {

                // Definizione della mappa dei parametri inseriti
                Map<String, String> parameters = new HashMap<String, String>();

                // definizione dello stream della foto
                ByteArrayOutputStream bytePhoto = new ByteArrayOutputStream();

                Bitmap resizedPhoto;

                if(photo.getWidth() > 1500 || photo.getHeight() > 1500)
                     resizedPhoto = Bitmap.createScaledBitmap(photo, (int)(photo.getWidth()*0.5),
                             (int)(photo.getHeight()*0.5), true);
                else
                    resizedPhoto = photo;

                // compressione della foto
                resizedPhoto.compress(Bitmap.CompressFormat.PNG, 100, bytePhoto);

                // definizione dell'immagina in un BLOB
                byte[] imageBytes = bytePhoto.toByteArray();

                // decodifica finale della foto in Base64
                String encodedPhoto = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                // inserimento nella mappa dei parametri inseriti
                parameters.put ("username", username);
                parameters.put ("previous_email", previous_email);
                parameters.put ("email", email);
                parameters.put ("first_name", first_name);
                parameters.put ("last_name", last_name);
                parameters.put ("phone_number", phone_number);
                parameters.put ("biography", biography);
                parameters.put ("encoded_image", encodedPhoto);
                parameters.put ("image_dir_url", AppConfiguration.URL_IMAGE_STORAGE_PATH);

                // restituzione della mappa dei parametri
                return parameters;
            }
        };

        // definizione del timeout per l'annullamento della richiesta
        int socket_timeout = 30000;

        // definizione della policy per il timeout dell'annullamento della richiesta
        RetryPolicy policy = new DefaultRetryPolicy(socket_timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        // definizione della policy sulla richiesta
        string_request.setRetryPolicy (policy);

        Log.d("tag", "sto analizzando");

        // Aggiunta della richiesta nelle code delle richieste
        AppController.get_instance().add_to_request_queue (string_request, "req_register");

        return success;
    }


    /*
    Metodo per la modifica dei dati personali
     */
    public boolean editPassword(final String username, final String new_password) {


        // inizializzazione della variabile, se avviene qualche errore viene settata a false
        success = true;

        // definizione di una richiesta per la registrazione di un nuovo utente
        final StringRequest string_request = new StringRequest(Request.Method.POST,
                AppConfiguration.URL_FILE_EDIT_PASSWORD, new Response.Listener<String>() {

            // definizione del comportamento in risposta
            @Override
            public void onResponse(String response) {

                // messaggio di debug con conseguente camuffamento della pagina di dialogo
                Log.d(RegisterActivity.class.getSimpleName(), "Edit Response: " + response);

                try {

                    Log.d("response REST php API", response);

                    // definizione di un oggetto json dalla risposta ricevuta
                    JSONObject json = new JSONObject (response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));

                    Log.d("JSON to REST php API", json.toString());

                    // definizione dell'errore accaduto
                    boolean error = json.getBoolean("error");

                    // se l'errore è FALSE
                    if (!error) {

                        // definizione di un novo oggetto JSON acquisendo, dalla risposta presa,
                        // l'array associativo di nome "user" (e trasformato in oggetto JSON)
                        JSONObject user = json.getJSONObject("user");

                        Log.d("JSON to REST php API", user.toString());

                        // messaggio di corretta registrazione
                        Toast.makeText(context, context.getResources().getString(R.string.success_change_password),
                                Toast.LENGTH_LONG).show();

                        // aggiunta dell'utente al database locale
                        local_db.updateUser(
                                user.getString("username"),
                                null,
                                user.getString("password"),
                                null,
                                null,
                                null,
                                null,
                                null
                        );

                        //ritorna l'esecuzione effettuata con successo
                        success = true;

                    } else { // errore nella registrazione

                        // visualizzazione del messaggio di errore nella registrazione
                        Toast.makeText(context, json.getString("error_msg"),
                                Toast.LENGTH_LONG).show();

                        success = false;
                    }

                } catch (JSONException e) {// gestione dell'eccezione

                    // errore nel file JSON passato
                    e.printStackTrace();
                    Toast.makeText(context, "JSON error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    success = false;
                }
            }

        }, new Response.ErrorListener () {

            // definizione del comportamento all'errore generico di login
            @Override
            public void onErrorResponse (VolleyError error) {

                // messaggio di debug
                Log.e (RegisterActivity.class.getSimpleName(), "Edit Error: " + error.getMessage());

                // visualizzazione del messaggio di errore
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();

                success = false;
            }

        }) { // definizione del comportamento dello string request

            @Override
            protected Map<String, String> getParams() {

                // Definizione della mappa dei parametri inseriti
                Map<String, String> parameters = new HashMap<String, String>();

                // inserimento nella mappa dei parametri inseriti
                parameters.put ("username", username);
                parameters.put ("password", new_password);

                // restituzione della mappa dei parametri
                return parameters;
            }
        };

        // definizione del timeout per l'annullamento della richiesta
        int socket_timeout = 30000;

        // definizione della policy per il timeout dell'annullamento della richiesta
        RetryPolicy policy = new DefaultRetryPolicy(socket_timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        // definizione della policy sulla richiesta
        string_request.setRetryPolicy (policy);

        Log.d("tag", "sto analizzando");

        // Aggiunta della richiesta nelle code delle richieste
        AppController.get_instance().add_to_request_queue (string_request, "req_register");

        return success;
    }


    /*
   Metodo per la modifica delle lingue parlate
    */
    public boolean editSpokenLanguages(final String username, final HashMap<Integer, Boolean> spokenLanguages) {


        // inizializzazione della variabile, se avviene qualche errore viene settata a false
        success = true;

        // definizione di una richiesta per la registrazione di un nuovo utente
        final StringRequest string_request = new StringRequest(Request.Method.POST,
                AppConfiguration.URL_FILE_EDIT_SPOKEN_LANGUAGES, new Response.Listener<String>() {

            // definizione del comportamento in risposta
            @Override
            public void onResponse(String response) {

                // messaggio di debug con conseguente camuffamento della pagina di dialogo
                Log.d(RegisterActivity.class.getSimpleName(), "Edit Response: " + response.toString());

                try {

                    response = response.replace(response.substring(response.indexOf("{"), response.indexOf("}") + 1), "");

                    Log.d("response REST php API", response);

                    // definizione di un oggetto json dalla risposta ricevuta
                    JSONObject json = new JSONObject (response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));

                    //Log.d("JSON to REST php API", json.toString());

                    // definizione dell'errore accaduto
                    boolean error = json.getBoolean("error");

                    // se l'errore è FALSE
                    if (!error) {

                        // definizione di un novo oggetto JSON acquisendo, dalla risposta presa,
                        // l'array associativo di nome "user" (e trasformato in oggetto JSON)
                        //JSONArray user = json.getJSONObject("user");

                        //Log.d("JSON to REST php API", user.toString());

                        // messaggio di corretta registrazione
                        Toast.makeText(context, context.getResources().getString(R.string.success_change_spoken_languages),
                                Toast.LENGTH_LONG).show();

                        // aggiunta dell'utente al database locale
                        local_db.updateSpokenLanguages(username, spokenLanguages);

                        //ritorna l'esecuzione effettuata con successo
                        success = true;

                    } else { // errore nella registrazione

                        // visualizzazione del messaggio di errore nella registrazione
                        Toast.makeText(context, json.getString("error_msg"),
                                Toast.LENGTH_LONG).show();

                        success = false;
                    }

                } catch (JSONException e) {// gestione dell'eccezione

                    // errore nel file JSON passato
                    e.printStackTrace();
                    Toast.makeText(context, "JSON error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    success = false;
                }
            }

        }, new Response.ErrorListener () {

            // definizione del comportamento all'errore generico di login
            @Override
            public void onErrorResponse (VolleyError error) {

                // messaggio di debug
                Log.e (RegisterActivity.class.getSimpleName(), "Edit Error: " + error.getMessage());

                // visualizzazione del messaggio di errore
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();

                success = false;
            }

        }) { // definizione del comportamento dello string request

            @Override
            protected Map<String, String> getParams() {

                // Definizione della mappa dei parametri inseriti
                Map<String, String> parameters = new HashMap<String, String>();

                // inserimento nella mappa dei parametri inseriti
                parameters.put ("username", username);

                // definizione di ogni lingua se è parlata oppure no
                for (Map.Entry<Integer, Boolean> lang : spokenLanguages.entrySet()){

                    if(lang.getValue())
                        parameters.put(lang.getKey().toString(), "true");
                    else
                        parameters.put(lang.getKey().toString(), "false");
                }

                // restituzione della mappa dei parametri
                return parameters;
            }
        };

        // definizione del timeout per l'annullamento della richiesta
        int socket_timeout = 30000;

        // definizione della policy per il timeout dell'annullamento della richiesta
        RetryPolicy policy = new DefaultRetryPolicy(socket_timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        // definizione della policy sulla richiesta
        string_request.setRetryPolicy (policy);

        Log.d("tag", "sto analizzando");

        // Aggiunta della richiesta nelle code delle richieste
        AppController.get_instance().add_to_request_queue (string_request, "req_register");

        return success;
    }


    // Metodo per la cancellazione dei dati del mio account nel database
    public boolean deleteMyAccount(final String username) {

        // definizione del flag di corretta esecuzione
        boolean result = false;

        try {

            DeleteMyAccountData deleteAccountDataAsyncTask = new DeleteMyAccountData();

            // esecuzione della cancellazione dei dati
            String execResult = deleteAccountDataAsyncTask.execute(username).get();

            // se l'esecuzione va a buon fine allora ritorna true
            if (execResult.equals("success")) {

                result = true;

                // messaggio di corretto salvataggio dei dati
                Toast.makeText(context, context.getResources().getString(R.string.success_delete_account),
                        Toast.LENGTH_LONG).show();

            } else {

                // visualizzazione del messaggio di errore nel salvataggio
                Toast.makeText(context, context.getResources().getString(R.string.error_delete_account),
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return result;
    }



    public void getMyAccountData() {

        // finestra di dialogo
        dialog = new ProgressDialog(context);
        dialog.setCancelable (false);

        // definizione del messaggio per il progress dialog e succesiva mostra
        dialog.setMessage (context.getResources().getString(R.string.acquiring_data));
        show_dialog();

        // definizione della comunicazione con il database
        StringRequest string_request = new StringRequest(Request.Method.POST,
                AppConfiguration.URL_FILE_GETMYDATA, new Response.Listener<String>() {

            // definizione del comportamento di risposta alla richiesta
            @Override
            public void onResponse(String response) {

                // messaggio di debug
                Log.d(LoginActivity.class.getSimpleName(), "GetMyData response: " + response);

                // camuffamento della finestra di dialogo
                hide_dialog();

                try {

                    // definizione del file JSON dalla risposta
                    JSONObject json = new JSONObject(response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));

                    // acquisizione dell'errore dal file JSON
                    boolean error = json.getBoolean("error");

                    // se l'errore è FALSE
                    if (!error) {

                        // valutazione se esistono dati da raccogliere
                        if(!json.isNull("language")) {

                            // definizione di un novo oggetto JSON acquisendo, dalla risposta presa,
                            // l'array associativo di nome "language" (e trasformato in oggetto JSON)
                            JSONArray languageList = json.getJSONArray("language");

                            Log.d("Language", languageList.toString());

                            for (int i = 0; i < languageList.length(); i++) {

                                JSONObject language = languageList.getJSONObject(i);

                                local_db.saveLanguage(
                                        language.getInt("id"), // acquisizione dell'id della lingua
                                        language.getString("name") // acquisizione del nome della lingua
                                );
                            }
                        }

                        // valutazione se esistono dati da raccogliere
                        if(!json.isNull("user_spoken_language")) {

                            // definizione di un novo oggetto JSON acquisendo, dalla risposta presa,
                            // l'array associativo di nome "user_spoken_language" (e trasformato in oggetto JSON)
                            JSONArray userSpokenLanguageList = json.getJSONArray("user_spoken_language");

                            Log.d("UserSpokenLanguage", userSpokenLanguageList.toString());

                            for (int i = 0; i < userSpokenLanguageList.length(); i++) {

                                JSONObject userSpokenLanguage = userSpokenLanguageList.getJSONObject(i);

                                local_db.saveUserSpokenLanguage(
                                        userSpokenLanguage.getInt("language_id"), // acquisizione dell'id della lingua
                                        userSpokenLanguage.getString("username_id") // acquisizione dello username dell'utente
                                );
                            }
                        }

                        // TODO QUI PRENDERE GLI ALTRI DATI PERSONALI COME LE ISCRIZIONI E ALTRA ROBA



                        // definizione di un collegamento tra questa activity e l'activityprincipale
                        //Intent intent = new Intent(context, MainActivity.class);

                        //intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);

                        // inizio della nuova activity
                        //context.startActivity(intent);

                    } else {

                        // visualizzazione di un messaggio di errore nel login
                        Toast.makeText(context, json.getString("error_msg"),
                                Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) { // gestione dell'eccezione

                    // errore nel file JSON passato
                    e.printStackTrace();
                    Toast.makeText(context, "JSON error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }


        }, new Response.ErrorListener() {

            // definizione del comportamento all'errore generico di login
            @Override
            public void onErrorResponse(VolleyError error) {

                // messaggio di debug
                Log.e (LoginActivity.class.getSimpleName(), "GetMyData Error: " + error.getMessage());

                // visualizzazione del messaggio di errore
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();

                // camuffamento della finestra di dialogo
                hide_dialog();
            }

        }) { // definizione del comportamento dello string request

            /*
            Metodo per il ritorno dei parametri inseriti
             */
            @Override
            protected Map<String, String> getParams() {

                // Definizione della mappa dei parametri inseriti
                Map<String, String> parameters = new HashMap<String, String>();

                // inserimento nella mappa dei parametri inseriti
                parameters.put ("username", local_db.getMyAccount().getUsername());

                // restituzione della mappa dei parametri
                return parameters;
            }
        };

        // aggiunta di una richiesta alla coda delle richieste
        AppController.get_instance().add_to_request_queue (string_request, "getmydata_request");
    }


    /*
    Metodo per la visualizzazione della pagina di dialogo
     */
    private void show_dialog() {

        if (!dialog.isShowing())
            dialog.show();
    }


    /*
    Metodo per il camuffamento della pagina di dialogo
     */
    private void hide_dialog() {

        if (dialog.isShowing())
            dialog.dismiss();
    }



    /*
    Classe locale per la cancellazione dei dati
    del mio account
     */
    class DeleteMyAccountData extends AsyncTask<String, Void, String> {

        private String result = "";


        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            // acquisizione della data in cui è stato creata la notifica
            POSTdata.put("username", data[0]);


            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_DELETE_MY_ACCOUNT, POSTdata, "POST");

            try {

                Log.d("response REST myAcc: ", response);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("{");
                stringBuilder.append(StringUtils.substringBeforeLast(StringUtils.substringAfter(response, "{"), "}"));
                stringBuilder.append("}");

                // definizione di un oggetto json dalla risposta ricevuta
                JSONObject json = new JSONObject(stringBuilder.toString());

                // definizione dell'errore accaduto
                boolean error = json.getBoolean("error");

                // se l'errore è FALSE
                if (!error) {

                    // messaggio di corretto salvataggio dei dati
                    result = "success";

                } else { // errore nel salvataggio

                    result = "error";
                }

            } catch (JSONException e) {

                e.printStackTrace();
            }

            return result;
        }


        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
        }


        // metodo per l'esecuzione della chiamata del servizio REST
        // per l'acquisizione dei dati
        public String performCall(String requestURL, HashMap<String, String> data, String method) {

            //inizializzazione dell'URL e della risposta
            URL url;
            String response = "";

            try {

                // definizione dell'url
                url = new URL(requestURL);

                // impostazione dei parametri della connessione HTTP
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(150000);
                conn.setConnectTimeout(150000);
                conn.setRequestMethod(method);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // definizione dei parametri da passare
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(convertDataForPostSending(data));
                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();

                // se il codice di risposta definisce una corretta connessione
                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    // inserimento nella risposta da restituire tutto ciò
                    // che si è acquisito dal servizio REST
                    String line;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = reader.readLine()) != null)
                        response += line;

                } else {

                    // se la connessione non è andata a buon fine allora ritorna una risposta vuota
                    response="";
                }

            } catch (Exception e) {

                e.printStackTrace();
            }

            // ritorno della risposta acquisita dalla connessione
            return response;
        }


        // metodo che permette di convertire una HashMap di dati
        // in una stringa comprensibile al servizio REST in modo tale
        // da poterli passare nella richiesta in POST (o GET)
        private String convertDataForPostSending(HashMap<String, String> params) throws UnsupportedEncodingException {

            StringBuilder result = new StringBuilder();
            boolean first = true;

            // per ogni elemento della mappa
            for(Map.Entry<String, String> entry : params.entrySet()){

                // se non è il primo allora si aggiunge & per la concatenazione di piu oggetti
                if (first)
                    first = false;
                else
                    result.append("&");

                // decodifica della chiave del valore passato e successivo inserimento
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");

                // decodifica del valore passato e successivo inserimento
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            // ritorno della stringa di passaggio dei dati ben formattato
            return result.toString();
        }

    }




    /*
    Metodo per l'acquisizione dell'immagine bitmap dall'url
     */
    public Bitmap getBitmapFromURL(String src) {

        // permette il download nello stesso thread principale;
        // è sconsigliato ma sarebbe uno spreco inutile fare
        // una classe per l'asincronismo per una sola funzione
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {

            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;

        } catch (IOException e) {

            Log.d("getBitmapFromURL", e.getClass() + " " + e.getMessage());

            return null;
        }
    }
}
