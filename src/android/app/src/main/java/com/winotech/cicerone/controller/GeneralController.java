package com.winotech.cicerone.controller;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.misc.AppConfiguration;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Cicerone;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Globetrotter;
import com.winotech.cicerone.model.Notification;
import com.winotech.cicerone.model.Reason;
import com.winotech.cicerone.model.Request;
import com.winotech.cicerone.model.Subscribed;
import com.winotech.cicerone.model.Tour;
import com.winotech.cicerone.model.User;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class GeneralController /*extends Thread*/ implements Serializable {

    // context dell'applicazione
    private transient Context context;

    // manager del cambio del fragment
    FragmentTransaction fragmentTransaction;

    // database locale
    private DBManager db;

    // dialogo di stampa a video
    private ProgressDialog dialog;

    // flag per definire se l'operazione effettuata è andata a buon fine
    public boolean success;

    // puntatore al controller generale
    private static GeneralController controller;

    // vettore contenente i dati temporanei che si
    // stanno scrivendo nell'inserimento di un nuova attività
    private ArrayList<Notification> loadedNotifications;



    // Costruttore del controller
    private GeneralController (Context context) {

        this.context = context;

        // controller della connessione con il database locale
        db = new DBManager(context);

        loadedNotifications = new ArrayList<>();
    }


    // Metodo per l'inizializzazione dell'istanza del controller
    public static void initInstance(Context context) {

        if(controller == null)
            controller = new GeneralController(context);
    }


    // Metodo per l'acqusiizione dell'istanza del controller
    public static GeneralController getInstance() {

        return controller;
    }


    // Metodo per l'acquisizione delle notifiche dell'utente
    public ArrayList<Notification> getNotifications(String username) {

        // lista delle attività trovati
        ArrayList<Notification> foundedNotifications = new ArrayList<>();

        try {

            // inizializzazione dell'AsyncTask
            GetNotificationData notificationDataAsyncTask = new GetNotificationData();

            // esecuzione della ricerca delle notifiche
            notificationDataAsyncTask.execute(username).get();

            // acquisizione dele notifiche cercate
            foundedNotifications = new ArrayList<>(notificationDataAsyncTask.getLoadedNotifications());

        } catch (ExecutionException | InterruptedException e) {

            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        // ritorno delle notifiche trovate
        return foundedNotifications;
    }


    // Metodo per l'aggiunta della richiesta di accettazione ad un evento
    public boolean saveRequest(String username, String cicerone, int event_id, int group_dimension) {

        boolean result = false;

        try {

            // inizializzazione dell'AsyncTask
            SetEventRequestData eventRequestDataAsyncTask = new SetEventRequestData();

            // esecuzione della ricerca delle notifiche
            String execResult = eventRequestDataAsyncTask.execute(
                    username,
                    String.valueOf(event_id),
                    String.valueOf(group_dimension)
            ).get();

            // se l'esecuzione va a buon fine allora ritorna true
            if(execResult.equals("success")) {

                // inizializzazione dell'AsyncTask
                SetNotificationData notificationDataAsyncTask = new SetNotificationData();

                // esecuzione della ricerca delle notifiche
                String secondExecResult = notificationDataAsyncTask.execute(
                        username,
                        cicerone,
                        "2",
                        String.valueOf(event_id),
                        "NULL"
                ).get();

                // se l'esecuzione va a buon fine allora ritorna true
                if(secondExecResult.equals("success"))
                    result = true;
            }

        } catch (ExecutionException  e) {

            e.printStackTrace();


        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        return result;
    }


    // Metodo per il salvataggio della risposta alla richiesta nel database
    public boolean saveRequestResponse(final Request request, final boolean isAccepted) {

        // definizione del flag di corretta esecuzione
        boolean result = false;

        try {

            String isAcceptedString = "FALSE";

            Toast.makeText(context, context.getResources().getText(R.string.acquiring_data),
                    Toast.LENGTH_SHORT).show();

            SetRequestResponseData setRequestResponseDataAsyncTask = new SetRequestResponseData();

            if(isAccepted)
                isAcceptedString = "TRUE";

            // esecuzione del salvataggio dei dati aggiornati
            String execResult = setRequestResponseDataAsyncTask.execute(
                    request.getGlobetrotter().getUsername(),
                    Integer.toString(request.getEvent().getId()),
                    Integer.toString(request.getGroupDimension()),
                    isAcceptedString,
                    db.getMyAccount().getUsername()
            ).get();

            // se l'esecuzione va a buon fine allora ritorna true
            if (execResult.equals("success"))
                result = true;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return result;
    }


    // Metodo per il salvataggio di una nuova notifica nel database
    public boolean saveNotification(final String sender, final String receiver, final int reason,
                                    final int eventID, final String description) {

        boolean result = false;

        try {
            // inizializzazione dell'AsyncTask
            SetNotificationData notificationDataAsyncTask = new SetNotificationData();

            // esecuzione del salvataggio della notifica
            String secondExecResult = notificationDataAsyncTask.execute(
                    sender,
                    receiver,
                    Integer.toString(reason),
                    Integer.toString(eventID),
                    description
            ).get();

            // se l'esecuzione va a buon fine allora ritorna true
            if (secondExecResult.equals("success"))
                result = true;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return result;
    }


    // Metodo per la cancellazione di una iscrizione dal database
    public boolean deleteSubscribed(String username, int eventID) {

        boolean result = false;

        try {
            // inizializzazione dell'AsyncTask
            DeleteSubscribedData deleteSubscribedDataAsyncTask = new DeleteSubscribedData();

            // esecuzione della cancellazione dell'iscrizione
            String execResult = deleteSubscribedDataAsyncTask.execute(
                    username,
                    Integer.toString(eventID)
            ).get();

            // se l'esecuzione va a buon fine allora ritorna true
            if (execResult.equals("success")) {

                result = true;

                // messaggio di corretto salvataggio dei dati
                Toast.makeText(context, context.getResources().getString(R.string.success_delete_subscription),
                        Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(context, context.getResources().getString(R.string.oops_request),
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return result;
    }


    // Metodo per l'aggiunta della recensione di un attività
    public boolean saveFeedback(String username, int tourID, int rate, String description) {

        boolean result = false;

        try {


            // inizializzazione dell'AsyncTask
            SetFeedbackData feedbackDataAsyncTask = new SetFeedbackData();

            // esecuzione della ricerca delle notifiche
            String execResult = feedbackDataAsyncTask.execute(
                    username,
                    Integer.toString(tourID),
                    Integer.toString(rate),
                    description
            ).get();

            // se l'esecuzione va a buon fine allora ritorna true
            if(execResult.equals("success")) {

                result = true;

                // messaggio di corretto salvataggio dei dati
                Toast.makeText(context, context.getResources().getString(R.string.success_manage_feedback),
                        Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(context, context.getResources().getString(R.string.oops_request),
                        Toast.LENGTH_LONG).show();
            }

        } catch (ExecutionException e) {

            e.printStackTrace();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        return result;
    }


    // Metodo per laggiornamento delle iscrizioni
    public boolean updateSubscriptions(String username, boolean isCicerone) {

        boolean result = false;

        try {

            String isCiceroneString = "1";

            // inizializzazione dell'AsyncTask
            UpdateSubscriptionsData updateSubscriptionsDataAsyncTask = new UpdateSubscriptionsData();

            if(!isCicerone)
                isCiceroneString = "0";

            // esecuzione della ricerca delle notifiche
            String execResult = updateSubscriptionsDataAsyncTask.execute(
                    username,
                    isCiceroneString
            ).get();

            // se l'esecuzione va a buon fine allora ritorna true
            if(execResult.equals("success")) {

                result = true;
            }

        } catch (ExecutionException e) {

            e.printStackTrace();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        return result;
    }


    // Metodo per l'aggiornamento delle richieste di iscrizione
    public boolean updateRequests(String username, boolean isCicerone) {

        boolean result = false;

        try {

            String isCiceroneString = "1";

            // inizializzazione dell'AsyncTask
            UpdateRequestsData updateRequestsDataAsyncTask = new UpdateRequestsData();

            if(!isCicerone)
                isCiceroneString = "0";

            // esecuzione della ricerca delle notifiche
            String execResult = updateRequestsDataAsyncTask.execute(
                    username,
                    isCiceroneString
            ).get();

            // se l'esecuzione va a buon fine allora ritorna true
            if(execResult.equals("success")) {

                result = true;
            }

        } catch (ExecutionException e) {

            e.printStackTrace();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        return result;
    }


    // Metodo per il salvataggio del pagamento effettuato
    public boolean savePayment(String username, String eventID) {

        boolean result = false;

        try {

            // inizializzazione dell'AsyncTask
            SavePaymentData savePaymentDataAsyncTask = new SavePaymentData();

            // esecuzione della ricerca delle notifiche
            String execResult = savePaymentDataAsyncTask.execute(
                    username,
                    eventID
            ).get();

            // se l'esecuzione va a buon fine allora ritorna true
            if(execResult.equals("success")) {

                result = true;
            }

        } catch (ExecutionException e) {

            e.printStackTrace();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        return result;
    }




    // ----- CLASSI LOCALI DI ASYNCTASK -----

    /*
    Classe locale per l'acquisizione dei dati delle notifiche
    di un determinato utente
    */
    public class GetNotificationData extends AsyncTask<String, Void, String> {

        // lista di notifiche
        ArrayList<Notification> loadedNotifications = new ArrayList<>();


        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            POSTdata.put("username", data[0]);

            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_GET_NOTIFICATIONS, POSTdata, "POST");    //POSTdata, "POST");

            try {

                Log.d("response REST notific: ", response);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("{");
                stringBuilder.append(StringUtils.substringBeforeLast(StringUtils.substringAfter(response, "{"), "}"));
                stringBuilder.append("}");

                // definizione di un oggetto json dalla risposta ricevuta
                JSONObject json = new JSONObject(stringBuilder.toString());
                //JSONObject json = new JSONObject (response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));

                // definizione dell'errore accaduto
                boolean error = json.getBoolean("error");

                // se l'errore è FALSE
                if (!error) {

                    // svuotamento delle notifiche precedentemente caricate
                    loadedNotifications.clear();

                    // definizione di un novo array JSON acquisendo, dalla risposta presa,
                    // l'array associativo di nome "notifications" (e trasformato in oggetto JSON)
                    JSONArray notificationsArray = json.getJSONArray("notifications");

                    for (int i = 0; i < notificationsArray.length(); i ++) {

                        // acquisizione dell'oggetto JSON da trasformare in oggetto Tour
                        JSONObject notificationData = notificationsArray.getJSONObject(i);

                        // definizione del formato della data
                        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);

                        // definizione della notifica
                        Notification notification = new Notification(

                                notificationData.getInt("id"),
                                null, // inutile da definire, sono io!
                                null,
                                null,
                                null,
                                notificationData.getString("description"),
                                null
                        );

                        // definizione del mittente
                        User sender;

                        // se il mittente è un cicerone allora imposta il mittente
                        // come oggetto Cicerone, altrimenti come Globetrotter
                        //if (notificationData.getInt("receiver_is_cicerone") == 1) {
                        if (db.getMyAccount() instanceof Globetrotter) {

                            sender = new Cicerone();
                            sender.setUsername(notificationData.getString("sender_username"));

                        } else {

                            sender  = new Globetrotter();
                            sender.setUsername(notificationData.getString("sender_username"));
                        }

                        // acquisizione della foto del mittente
                        sender.setPhoto(getBitmapFromURL(AppConfiguration.URL_PREFIX +
                                    notificationData.getString("image_path")));


                        // definizione dell'evento
                        Event event = new Event(
                                Integer.parseInt(notificationData.getString("event_id")),
                                new Tour(Integer.parseInt(notificationData.getString("tour_id")),
                                        notificationData.getString("tour_name"),
                                        null, null, null, null, 0),
                                null,
                                null,
                                notificationData.getString("event_description"),
                                0
                        );

                        // definizione della data di inizio dell'evento
                        Calendar startDate = Calendar.getInstance();
                        startDate.setTime(dataFormat.parse(notificationData.getString("event_start_date")));
                        event.setStartDate(startDate);

                        // definizione della data di fine dell'evento
                        Calendar endDate = Calendar.getInstance();
                        endDate.setTime(dataFormat.parse(notificationData.getString("event_end_date")));
                        event.setEndDate(endDate);

                        // definizione della ragione dell'invio della notifica
                        Reason reason = new Reason(0, notificationData.getString("reason_name"));

                        // definizione della data di creazione della notifica
                        Calendar createdOn = new GregorianCalendar();//Calendar.getInstance();
                        createdOn.setTime(dataFormat.parse(notificationData.getString("created_on")));

                        // definizione dei parametri delle classi create
                        notification.setSender(sender);
                        notification.setEvent(event);
                        notification.setReason(reason);
                        notification.setCreatedOn(createdOn);

                        // aggiunta del tour tra quelli caricati
                        loadedNotifications.add(notification);
                    }

                } else { // errore nel caricamento

                    Notification fakeNotification = new Notification();

                    User fakeUser  = new User();
                    fakeUser.setUsername(context.getResources().getString(R.string.no_notifications_find));

                    fakeNotification.setSender(fakeUser);
                    fakeNotification.setReason(new Reason(0, " "));

                    loadedNotifications.add(fakeNotification);
                }

            } catch (JSONException e) {

                e.printStackTrace();

            } catch (ParseException e) {

                e.printStackTrace();

            } catch (NullPointerException e) {

                //e.printStackTrace();
                Thread.currentThread().interrupt();
            }

            // ordinamento della lista
            orderingNotificationByCreatedOn(loadedNotifications);

            return response;
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


        // metodo per l'acquisizione delle notifiche ricercate
        public ArrayList<Notification> getLoadedNotifications() {

            return loadedNotifications;
        }

    }


    /*
    Classe locale per l'inserimento dei dati delle richieste
    per un evento di un determinato utente
    */
    public class SetEventRequestData extends AsyncTask<String, Void, String> {

        private String result = "";

        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            // acquisizione della data in cui è stato creata la notifica
            Calendar today = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);

            POSTdata.put("globetrotter", data[0]);
            POSTdata.put("event_id", data[1]);
            POSTdata.put("group_dimension", data[2]);
            POSTdata.put("created_on",  sdf.format(today.getTime()));

            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_SET_REQUEST, POSTdata, "POST");

            try {

                Log.d("response REST req.ev: ", response);

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
   Classe locale per l'inserimento dei dati delle richieste
   per un evento di un determinato utente
   */
    public class SetNotificationData extends AsyncTask<String, Void, String> {

        private String result = "";

        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            // acquisizione della data in cui è stato creata la notifica
            Calendar today = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);

            POSTdata.put("sender_id", data[0]);
            POSTdata.put("receiver_id", data[1]);
            POSTdata.put("reason_id", data[2]);
            POSTdata.put("created_on",  sdf.format(today.getTime()));
            POSTdata.put("event_id",  data[3]);
            POSTdata.put("description",  data[4]);

            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_SET_NOTIFICATION, POSTdata, "POST");

            try {

                Log.d("response REST not: ", response);

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
   Classe locale per l'inserimento dei dati della risposta ad
   un evento da parte del cicerone
   */
    public class SetRequestResponseData extends AsyncTask<String, Void, String> {

        private String result = "";

        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            // acquisizione della data in cui è stato creata la notifica
            Calendar today = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);

            POSTdata.put("globetrotter", data[0]);
            POSTdata.put("event_id", data[1]);
            POSTdata.put("group_dimension", data[2]);
            POSTdata.put("is_accepted", data[3]);
            POSTdata.put("cicerone", data[4]);
            POSTdata.put("created_on", sdf.format(today.getTime()));

            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_SET_REQUEST_RESPONSE, POSTdata, "POST");

            try {

                Log.d("response REST resp: ", response);

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
   Classe locale per la cancellazione dei dati dell'iscrizione ad
   un evento da parte del cicerone
   */
    public class DeleteSubscribedData extends AsyncTask<String, Void, String> {

        private String result = "";

        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            POSTdata.put("globetrotter", data[0]);
            POSTdata.put("event_id", data[1]);

            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_DELETE_SUBSCRIBED, POSTdata, "POST");

            try {

                Log.d("response REST delSub: ", response);

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
   Classe locale per l'inserimento dei dati della recensione
   per un'attività di un determinato utente
   */
    public class SetFeedbackData extends AsyncTask<String, Void, String> {

        private String result = "";

        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            // acquisizione della data in cui è stato creata la notifica
            Calendar today = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);

            POSTdata.put("username", data[0]);
            POSTdata.put("tour_id", data[1]);
            POSTdata.put("rate", data[2]);
            POSTdata.put("description",  data[3]);
            POSTdata.put("created_on",  sdf.format(today.getTime()));

            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_SET_FEEDBACK, POSTdata, "POST");

            try {

                Log.d("response REST feed: ", response);

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
    Classe locale per l'aggiornamento dei dati dell'iscrizione
     */
    public class UpdateSubscriptionsData extends AsyncTask<String, Void, String> {

        private String result = "";

        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            POSTdata.put("username", data[0]);
            POSTdata.put("is_cicerone", data[1]);

            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_UPDATE_SUBSCRIPTIONS, POSTdata, "POST");

            try {

                Log.d("response REST upSub: ", response);

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
    Classe locale per l'aggiornamento dei dati delle richieste d'iscrizione
     */
    public class UpdateRequestsData extends AsyncTask<String, Void, String> {

        private String result = "";

        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            POSTdata.put("username", data[0]);
            POSTdata.put("is_cicerone", data[1]);

            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_UPDATE_REQUESTS, POSTdata, "POST");

            try {

                Log.d("response REST upReq: ", response);

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
   Classe locale per l'aggiornamento del pagamento dell'iscrizione
    */
    public class SavePaymentData extends AsyncTask<String, Void, String> {

        private String result = "";

        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            POSTdata.put("username", data[0]);
            POSTdata.put("event_id", data[1]);

            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_SAVE_PAYMENT, POSTdata, "POST");

            try {

                Log.d("response REST set1pay: ", response);

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



    // ----- METODI DI SUPPORTO DEL CONTROLLER ----

    /*
   Metodo per l'acquisizione dell'immagine bitmap dall'url
    */
    public Bitmap getBitmapFromURL(String src) {

        // permette il download nello stesso thread principale
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


    /*
    Metodo per acquisire la posizione esatta sotto forma di indirizzo dalle coordinate
     */
    public String getLocationFromCoords(float latitude, float longitude) {

        try {

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if(!addresses.isEmpty())
                return addresses.get(0).getThoroughfare() + ", " + addresses.get(0).getLocality() ;


        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }


    /*
    Metodo per l'acquisizione delle notifiche dell'utente loggato
     */
    public ArrayList<Notification> getLoadedNotifications() {

        return loadedNotifications;
    }



    // ----- METODI PER L'ORDINAMENTO DELLA LISTA DI NOTIFICHE -----

    // Metodo per l'ordinamento delle notifiche in base alla data di creazione
    private void orderingNotificationByCreatedOn(ArrayList<Notification> notifications) {

        // creazione di un vettore temporaneo
        Vector<Notification> notificationVector = new Vector<>();

        // copia di ogni notifica all'interno del vettore temporaneo
        notificationVector.addAll(notifications);

        // ordinamento del vettore temporaneo delle notifiche
        sort(notificationVector, 0, notificationVector.size() - 1);

        // reinserimento secondo l'ordine definito dell'insieme delle notifiche
        notifications.clear();
        notifications.addAll(notificationVector);
    }


    // metodo per l'ordinamento delle notifiche
    private void sort(Vector<Notification> notifications, int indexStart, int indexEnd) {

        if(indexStart < indexEnd){

            int indexCenter = (indexStart + indexEnd) / 2;
            sort(notifications, indexStart, indexCenter);
            sort(notifications, indexCenter + 1, indexEnd);
            merging(notifications, indexStart, indexCenter, indexEnd);
        }
    }


    // Metodo di supporto al merge sort per le notifiche
    private void merging(Vector<Notification> notifications, int indexStart, int indexCenter, int indexEnd) {

        int i = indexStart;
        int j = indexCenter + 1;
        int k = 0;

        Vector<Notification> temp = new Vector<>();

        while(i <= indexCenter && j <= indexEnd) {

            if(notifications.get(i).getCreatedOn().after(notifications.get(j).getCreatedOn())){

                temp.add(k, notifications.get(i));
                i ++;

            } else {

                temp.add(k, notifications.get(j));
                j ++;
            }

            k ++;
        }

        while(i <= indexCenter) {

            temp.add(k, notifications.get(i));
            i ++;
            k ++;
        }

        while ((j <= indexEnd)){

            temp.add(k, notifications.get(j));
            j ++;
            k ++;
        }

        //notifications.clear();
        for (k = indexStart; k <= indexEnd; k ++){

            notifications.set(k, temp.get(k - indexStart));
        }
    }


    // ----- METODI GENERICI PER LE NOTIFICHE -----

    // Metodo per la conversione dell'id della ragione della creazione della notifica in stringa
    public String getNotificationObjectFromID (String id) {

        String notificationObject = "";

        switch(id) {

            case "GLOBETROTTER_MADE_PAYMENT":
                notificationObject += context.getResources().getString(R.string.GLOBETROTTER_MADE_PAYMENT);
                break;

            case "GLOBETROTTER_MADE_REQUEST":
                notificationObject += context.getResources().getString(R.string.GLOBETROTTER_MADE_REQUEST);
                break;

            case "CICERONE_ACCEPT_REQUEST":
                notificationObject += context.getResources().getString(R.string.CICERONE_ACCEPT_REQUEST);
                break;

            case "CICERONE_REFUSE_REQUEST":
                notificationObject += context.getResources().getString(R.string.CICERONE_REFUSE_REQUEST);
                break;

            case "CICERONE_REMOVED_GLOBETROTTER_FROM_EVENT":
                notificationObject += context.getResources().getString(R.string.CICERONE_REMOVED_GLOBETROTTER_FROM_EVENT);
                break;

            case "GLOBETROTTER_RESIGN_EVENT":
                notificationObject += context.getResources().getString(R.string.GLOBETROTTER_RESIGN_EVENT);
                break;

            case "CICERONE_DELETE_EVENT":
                notificationObject += context.getResources().getString(R.string.CICERONE_DELETE_EVENT);
                break;

            case "CICERONE_EDIT_EVENT_DATE":
                notificationObject += context.getResources().getString(R.string.CICERONE_EDIT_EVENT_DATE);
                break;

            case "CICERONE_REMOVE_LOCATION":
                notificationObject += context.getResources().getString(R.string.CICERONE_REMOVE_LOCATION);
                break;

            case "CICERONE_ADD_LOCATION":
                notificationObject += context.getResources().getString(R.string.CICERONE_ADD_LOCATION);
                break;

            case "GLOBETROTTER_MADE_FEEDBACK":
                notificationObject += context.getResources().getString(R.string.GLOBETROTTER_MADE_FEEDBACK);
                break;

            case "CICERONE_DELETE_TOUR":
                notificationObject += context.getResources().getString(R.string.CICERONE_DELETE_TOUR);
                break;

            default:
                notificationObject += "ND";
                break;
        }

        return notificationObject;
    }


    // Metodo per la conversione dell'id della descrizione della notifica in stringa
    public String getNotificationDescriptionFromID (String id) {

        String notificationObject = "";

        switch(id) {

            case "GLOBETROTTER_MADE_PAYMENT":
                notificationObject += context.getResources().getString(R.string.GLOBETROTTER_MADE_PAYMENT_DESCRIPTION);
                break;

            case "GLOBETROTTER_MADE_REQUEST":
                notificationObject += context.getResources().getString(R.string.GLOBETROTTER_MADE_REQUEST_DESCRIPTION);
                break;

            case "CICERONE_ACCEPT_REQUEST":
                notificationObject += context.getResources().getString(R.string.CICERONE_ACCEPT_REQUEST_DESCRIPTION);
                break;

            case "CICERONE_REFUSE_REQUEST":
                notificationObject += context.getResources().getString(R.string.CICERONE_REFUSE_REQUEST_DESCRIPTION);
                break;

            case "CICERONE_REMOVED_GLOBETROTTER_FROM_EVENT":
                notificationObject += context.getResources().getString(R.string.CICERONE_REMOVED_GLOBETROTTER_FROM_EVENT_DESCRIPTION);
                break;

            case "GLOBETROTTER_RESIGN_EVENT":
                notificationObject += context.getResources().getString(R.string.GLOBETROTTER_RESIGN_EVENT_DESCRIPTION);
                break;

            case "CICERONE_DELETE_EVENT":
                notificationObject += context.getResources().getString(R.string.CICERONE_DELETE_EVENT_DESCRIPTION);
                break;

            case "CICERONE_EDIT_EVENT_DATE":
                notificationObject += context.getResources().getString(R.string.CICERONE_EDIT_EVENT_DATE_DESCRIPTION);
                break;

            case "CICERONE_REMOVE_LOCATION":
                notificationObject += context.getResources().getString(R.string.CICERONE_REMOVE_LOCATION_DESCRIPTION);
                break;

            case "CICERONE_ADD_LOCATION":
                notificationObject += context.getResources().getString(R.string.CICERONE_ADD_LOCATION_DESCRIPTION);
                break;

            case "GLOBETROTTER_MADE_FEEDBACK":
                notificationObject += context.getResources().getString(R.string.GLOBETROTTER_MADE_FEEDBACK_DESCRIPTION);
                break;

            case "CICERONE_DELETE_TOUR":
                notificationObject += context.getResources().getString(R.string.CICERONE_DELETE_TOUR_DESCRIPTION);
                break;

            default:
                notificationObject += " ";
                break;
        }

        return notificationObject;
    }
}
