package com.winotech.cicerone.controller;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.winotech.cicerone.R;
import com.winotech.cicerone.misc.AppConfiguration;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Cicerone;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Feedback;
import com.winotech.cicerone.model.Globetrotter;
import com.winotech.cicerone.model.Language;
import com.winotech.cicerone.model.Location;
import com.winotech.cicerone.model.Subscribed;
import com.winotech.cicerone.model.Tour;
import com.winotech.cicerone.view.MainActivity;
import com.winotech.cicerone.view.MyToursFragment;
import com.winotech.cicerone.view.RegisterActivity;

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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class TourController /*extends Thread */ implements Serializable {
    
    // context dell'applicazione
    private transient Context context;

    // manager del cambio del fragment
    FragmentTransaction fragmentTransaction;

    // database locale
    private DBManager local_db;

    // dialogo di stampa a video
    private ProgressDialog dialog;

    // flag per definire se l'operazione effettuata è andata a buon fine
    private boolean success;

    // puntatore al controller dei tour
    private static TourController controller;


    // ----- DATI TEMPORANEI IN MODIFICA -----

    // vettore contenente i dati temporanei che si
    // stanno scrivendo nell'inserimento di un nuova attività
    private Vector<String> tempNewTourData;

    // mappa contenente i dati temporanei che si
    // stanno scrivendo nell'inserimento di una o più tappe
    private HashMap<String, Location> tempLocations;

    // lista contenente le attività caricate temporaneamente
    private ArrayList<Tour> loadedTours = new ArrayList<>();
    private ArrayList<Event> loadedEvents = new ArrayList<>();
    private ArrayList<Language> loadedLanguages = new ArrayList<>();





    // Costruttore del controller
    private TourController (Context context) {

        this.context = context;

        // controller della connessione con il database locale
        local_db = new DBManager(context);
    }


    public static void initInstance(Context context) {

        if(controller == null)
            controller = new TourController(context);
    }


    public static TourController getInstance() {

        return controller;
    }


    // Metodo per il salvataggio di un nuovo tour nel database
    public boolean saveTour(final String tourName, final String tourDescription, final float tourCost,
                            final Calendar tourStartDate, final Calendar tourEndDate,
                            HashMap<String, Location> locations, MainActivity activity) {


        // definizione della data da sette giorni al giorno odierno
        // (sei giorni perché il settimo giorno è possibile
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, +6);

        // messaggio di errore per data di inizio o di fine precedente alla data odierna
        if(tourStartDate.before(maxDate) || tourEndDate.before(maxDate)) {

            // definizione della data da mostrare (realmente 7 giorni dopo)
            Calendar editedDate = Calendar.getInstance();
            editedDate.add(Calendar.DAY_OF_MONTH, +7);
            String dateBeforeSevenDays = editedDate.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                    (editedDate.get(GregorianCalendar.MONTH) + 1) + "/" +
                    editedDate.get(GregorianCalendar.YEAR);

            String text = context.getResources().getString(R.string.wrong_date_before_n_days) +
                    " " + dateBeforeSevenDays;

            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            success = false;
        }

        // messaggio di errore per data di inizio successiva alla data di fine
        else if(tourStartDate.after(tourEndDate)) {

            Toast.makeText(context, R.string.wrong_date_after, Toast.LENGTH_LONG).show();
            success = false;
        }

        // se non ci sono errori sulla data
        else {

            // esecuzione dell'invio dei dati
            (new SaveTourData(tourName, tourDescription, tourCost, tourStartDate, tourEndDate, locations, success)).execute();

            fragmentTransaction = activity.getFragmentManager().beginTransaction();
            Fragment fragmentView = MyToursFragment.newInstance(activity.getDb());
            fragmentTransaction.replace(R.id.fragment, fragmentView).commit();

        }

        return success;
    }


    // Metodo per l'aggiornamento dei dati del tour nel database
    public boolean editTour(final int id, final String tourName, final String tourDescription,
                            final float tourCost, final Calendar tourStartDate,
                            final Calendar tourEndDate, ArrayList<Event> events){

        boolean result = false;

        // messaggio di errore per data di inizio o di fine precedente alla data odierna
        if(tourEndDate.before(Calendar.getInstance())) {

            Toast.makeText(context, R.string.wrong_end_date_before_now, Toast.LENGTH_LONG).show();
            success = false;
        }

        // messaggio di errore per data di inizio successiva alla data di fine
        else if(tourStartDate.after(tourEndDate)) {

            Toast.makeText(context, R.string.wrong_date_after, Toast.LENGTH_LONG).show();
            success = false;
        }

        // se non ci sono errori sulla data
        else {

            try {

                // definizione della variabile per il controllo se le date sono corrette
                boolean correctDate = true;

                // per ogni evento controlla se la data inserita non viene
                // dopo quella dello stesso evento: se cosi fosse
                // allora definisci il flag come false
                for (Event event : events) {

                    if (event.getEndDate().after(tourEndDate))
                        correctDate = false;
                }

                // se dopo la computazione si nota che la data
                // passata inserita è corretta allora effettua il salvataggio
                if (correctDate) {

                    Toast.makeText(context, context.getResources().getText(R.string.acquiring_data),
                            Toast.LENGTH_SHORT).show();

                    UpdateTourData updateTourDataAsyncTask = new UpdateTourData();

                    String startDateOnString = tourStartDate.get(GregorianCalendar.YEAR) + "-" +
                            (tourStartDate.get(GregorianCalendar.MONTH) + 1) + "-" +
                            tourStartDate.get(GregorianCalendar.DAY_OF_MONTH);

                    String endDateOnString = tourEndDate.get(GregorianCalendar.YEAR) + "-" +
                            (tourEndDate.get(GregorianCalendar.MONTH) + 1) + "-" +
                            tourEndDate.get(GregorianCalendar.DAY_OF_MONTH);

                    // esecuzione del salvataggio dei dati aggiornati
                    String execResult = updateTourDataAsyncTask.execute(
                            Integer.toString(id),
                            tourName,
                            tourDescription,
                            Float.toString(tourCost),
                            startDateOnString,
                            endDateOnString
                    ).get();

                    // se l'esecuzione va a buon fine allora ritorna true
                    if (execResult.equals("success")) {

                        result = true;

                        // messaggio di corretto salvataggio dei dati
                        Toast.makeText(context, context.getResources().getString(R.string.success_save_data),
                                Toast.LENGTH_LONG).show();

                    } else {

                        // visualizzazione del messaggio di errore nel salvataggio
                        Toast.makeText(context, context.getResources().getString(R.string.error_save_data),
                                Toast.LENGTH_LONG).show();
                    }

                } else {

                    // visualizzazione del messaggio di errore nel salvataggio
                    Toast.makeText(context, context.getResources().getString(R.string.tour_data_before_event_data),
                            Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return result;
    }


    // Metodo per il rinnovo dei dati del tour nel database
    public boolean renewTour(final int id, final String tourName, final String tourDescription,
                             final float tourCost, final Calendar tourStartDate, final Calendar tourEndDate) {

        // definizione del flag di corretta esecuzione
        boolean result = false;

        // definizione della data da sette giorni al giorno odierno
        // (sei giorni perché il settimo giorno è possibile
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, +6);

        // messaggio di errore per data di inizio o di fine precedente alla data odierna
        if(tourStartDate.before(maxDate) || tourEndDate.before(maxDate)) {

            // definizione della data da mostrare (realmente 7 giorni dopo)
            Calendar editedDate = Calendar.getInstance();
            editedDate.add(Calendar.DAY_OF_MONTH, +7);
            String dateBeforeSevenDays = editedDate.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                    (editedDate.get(GregorianCalendar.MONTH) + 1) + "/" +
                    editedDate.get(GregorianCalendar.YEAR);

            String text = context.getResources().getString(R.string.wrong_date_before_n_days) +
                    " " + dateBeforeSevenDays;

            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            success = false;
        }

        // messaggio di errore per data di inizio successiva alla data di fine
        else if(tourStartDate.after(tourEndDate)) {

            Toast.makeText(context, R.string.wrong_date_after, Toast.LENGTH_LONG).show();
            success = false;
        }

        // se non ci sono errori sulla data
        else {

            try {

                Toast.makeText(context, context.getResources().getText(R.string.acquiring_data),
                        Toast.LENGTH_SHORT).show();

                UpdateTourData updateTourDataAsyncTask = new UpdateTourData();

                String startDateOnString = tourStartDate.get(GregorianCalendar.YEAR) + "-" +
                        (tourStartDate.get(GregorianCalendar.MONTH) + 1) + "-" +
                        tourStartDate.get(GregorianCalendar.DAY_OF_MONTH);

                String endDateOnString = tourEndDate.get(GregorianCalendar.YEAR) + "-" +
                        (tourEndDate.get(GregorianCalendar.MONTH) + 1) + "-" +
                        tourEndDate.get(GregorianCalendar.DAY_OF_MONTH);

                // esecuzione del salvataggio dei dati aggiornati
                String execResult = updateTourDataAsyncTask.execute(
                        Integer.toString(id),
                        tourName,
                        tourDescription,
                        Float.toString(tourCost),
                        startDateOnString,
                        endDateOnString
                ).get();

                // se l'esecuzione va a buon fine allora ritorna true
                if (execResult.equals("success")) {

                    result = true;

                    // messaggio di corretto salvataggio dei dati
                    Toast.makeText(context, context.getResources().getString(R.string.success_save_data),
                            Toast.LENGTH_LONG).show();

                } else {

                    // visualizzazione del messaggio di errore nel salvataggio
                    Toast.makeText(context, context.getResources().getString(R.string.error_save_data),
                            Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return result;
    }


    // Metodo per la cancellazione dei dati del tour nel database
    public boolean deleteTour(final int id) {

        // definizione del flag di corretta esecuzione
        boolean result = false;

        try {

            DeleteTourData deleteTourDataAsyncTask = new DeleteTourData();

            // esecuzione del salvataggio dei dati aggiornati
            String execResult = deleteTourDataAsyncTask.execute(
                    Integer.toString(id),
                    local_db.getMyAccount().getUsername(),
                    "12",
                    "NULL"
            ).get();

            // se l'esecuzione va a buon fine allora ritorna true
            if (execResult.equals("success")) {

                result = true;

                // messaggio di corretto salvataggio dei dati
                Toast.makeText(context, context.getResources().getString(R.string.success_delete_tour),
                        Toast.LENGTH_LONG).show();

            } else {

                // visualizzazione del messaggio di errore nel salvataggio
                Toast.makeText(context, context.getResources().getString(R.string.error_delete_tour),
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return result;
    }


    // Metodo per il salvataggio dei dati di un nuovo evento nel database
    public boolean saveEvent(final Tour tour, final String eventDescription, final int eventMaxSubs,
                             final Calendar eventStartDate, final Calendar eventEndDate, ArrayList<String> languageIDs){

        // definizione del flag di corretta esecuzione
        boolean result = false;

        // definizione del costo del tour
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);

        // definizione della data da sette giorni alla data di inizio
        // (sei giorni perché il settimo giorno è possibile prenderlo)
        Calendar maxDate = tour.getStartDate();
        //maxDate.add(Calendar.DAY_OF_MONTH, +6);

        // messaggio di errore per data di inizio o di fine precedente alla data massima impostata
        if(eventStartDate.before(maxDate) || eventEndDate.before(maxDate)) {

            // definizione della data da mostrare (realmente 7 giorni dopo)
            Calendar editedDate = tour.getStartDate();
            //editedDate.add(Calendar.DAY_OF_MONTH, +7);
            String dateBeforeSevenDays = editedDate.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                    (editedDate.get(GregorianCalendar.MONTH) + 1) + "/" +
                    editedDate.get(GregorianCalendar.YEAR);

            String text = context.getResources().getString(R.string.wrong_date_before_n_days) +
                    " " + dateBeforeSevenDays;

            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            success = false;
        }

        // definizione della data da sette giorni alla data odierna
        // (sei giorni perché il settimo giorno è possibile prenderlo)
        maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, +6);

        // messaggio di errore per data di inizio o di fine precedente alla data odierna
        if(eventStartDate.before(maxDate) || eventEndDate.before(maxDate)) {

            // definizione della data da mostrare (realmente 7 giorni dopo)
            Calendar editedDate = Calendar.getInstance();
            editedDate.add(Calendar.DAY_OF_MONTH, +7);
            String dateBeforeSevenDays = editedDate.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                    (editedDate.get(GregorianCalendar.MONTH) + 1) + "/" +
                    editedDate.get(GregorianCalendar.YEAR);

            String text = context.getResources().getString(R.string.wrong_date_before_n_days) +
                    " " + dateBeforeSevenDays;

            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            success = false;
        }

        // messaggio di errore per data di inizio successiva alla data di fine
        else if(eventStartDate.after(eventEndDate)) {

            Toast.makeText(context, R.string.wrong_date_after, Toast.LENGTH_LONG).show();
            success = false;
        }

        // messaggio di errore per data di inizio successiva alla data di fine del tour
        else if(eventStartDate.after(tour.getEndDate())){

            Toast.makeText(context, R.string.wrong_date_after_tour, Toast.LENGTH_LONG).show();
            success = false;
        }

        // se non ci sono errori sulla data
        else {

            try {

                Toast.makeText(context, context.getResources().getText(R.string.acquiring_data),
                        Toast.LENGTH_SHORT).show();

                SaveEventData saveEventDataAsyncTask = new SaveEventData();


                String startDateOnString = eventStartDate.get(GregorianCalendar.YEAR) + "-" +
                        (eventStartDate.get(GregorianCalendar.MONTH) + 1) + "-" +
                        String.format("%02d", eventStartDate.get(GregorianCalendar.DAY_OF_MONTH));

                String endDateOnString = eventEndDate.get(GregorianCalendar.YEAR) + "-" +
                        (eventEndDate.get(GregorianCalendar.MONTH) + 1) + "-" +
                        String.format("%02d", eventEndDate.get(GregorianCalendar.DAY_OF_MONTH));

                // passaggio della lista degli id delle lingue da salvare
                saveEventDataAsyncTask.setLanguageIDs(languageIDs);

                // esecuzione del salvataggio dei dati aggiornati
                String execResult = saveEventDataAsyncTask.execute(
                        Integer.toString(tour.getId()),
                        eventDescription,
                        Integer.toString(eventMaxSubs),
                        startDateOnString,
                        endDateOnString
                ).get();

                // se l'esecuzione va a buon fine allora ritorna true
                if (execResult.equals("success")) {

                    result = true;

                    // messaggio di corretto salvataggio dei dati
                    Toast.makeText(context, context.getResources().getString(R.string.success_save_data),
                            Toast.LENGTH_LONG).show();

                } else {

                    // visualizzazione del messaggio di errore nel salvataggio
                    Toast.makeText(context, context.getResources().getString(R.string.error_save_data),
                            Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return result;
    }


    // Metodo per l'aggiornamento dei dati dell'evento nel database
    public boolean editEvent(final Tour tour, final int eventID, final String eventDescription, final int eventMaxSubs,
                             final Calendar eventStartDate, final Calendar eventEndDate, ArrayList<String> languageIDs) {

        // definizione del flag di corretta esecuzione
        boolean result = false;

        // definizione del costo del tour
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);

        // definizione della data da sette giorni alla data di inizio
        // (sei giorni perché il settimo giorno è possibile prenderlo)
        Calendar maxDate = tour.getStartDate();

        // messaggio di errore per data di inizio o di fine precedente alla data massima impostata
        if(eventStartDate.before(maxDate) || eventEndDate.before(maxDate)) {

            // definizione della data da mostrare
            Calendar editedDate = tour.getStartDate();
            String dateBeforeSevenDays = editedDate.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                    (editedDate.get(GregorianCalendar.MONTH) + 1) + "/" +
                    editedDate.get(GregorianCalendar.YEAR);

            String text = context.getResources().getString(R.string.wrong_date_before_n_days) +
                    " " + dateBeforeSevenDays;

            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            success = false;
        }

        // definizione della data da sette giorni alla data odierna
        // (sei giorni perché il settimo giorno è possibile prenderlo)
        maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, +6);

        // messaggio di errore per data di inizio o di fine precedente alla data odierna
        if(eventStartDate.before(maxDate) || eventEndDate.before(maxDate)) {

            // definizione della data da mostrare (realmente 7 giorni dopo)
            Calendar editedDate = Calendar.getInstance();
            editedDate.add(Calendar.DAY_OF_MONTH, +7);
            String dateBeforeSevenDays = editedDate.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                    (editedDate.get(GregorianCalendar.MONTH) + 1) + "/" +
                    editedDate.get(GregorianCalendar.YEAR);

            String text = context.getResources().getString(R.string.wrong_date_before_n_days) +
                    " " + dateBeforeSevenDays;

            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            success = false;
        }

        // messaggio di errore per data di inizio successiva alla data di fine
        else if(eventStartDate.after(eventEndDate)) {

            Toast.makeText(context, R.string.wrong_date_after, Toast.LENGTH_LONG).show();
            success = false;
        }

        // messaggio di errore per data di inizio successiva alla data di fine del tour
        else if(eventStartDate.after(tour.getEndDate())){

            Toast.makeText(context, R.string.wrong_date_after_tour, Toast.LENGTH_LONG).show();
            success = false;
        }

        // se non ci sono errori sulla data
        else {

            try {

                Toast.makeText(context, context.getResources().getText(R.string.acquiring_data),
                        Toast.LENGTH_SHORT).show();

                UpdateEventData updateEventDataAsyncTask = new UpdateEventData();

                String startDateOnString = eventStartDate.get(GregorianCalendar.YEAR) + "-" +
                        (eventStartDate.get(GregorianCalendar.MONTH) + 1) + "-" +
                        String.format("%02d", eventStartDate.get(GregorianCalendar.DAY_OF_MONTH));

                String endDateOnString = eventEndDate.get(GregorianCalendar.YEAR) + "-" +
                        (eventEndDate.get(GregorianCalendar.MONTH) + 1) + "-" +
                        String.format("%02d", eventEndDate.get(GregorianCalendar.DAY_OF_MONTH));

                // passaggio della lista degli id delle lingue da salvare
                updateEventDataAsyncTask.setLanguageIDs(languageIDs);

                // esecuzione del salvataggio dei dati aggiornati
                String execResult = updateEventDataAsyncTask.execute(
                        Integer.toString(eventID),
                        eventDescription,
                        Integer.toString(eventMaxSubs),
                        startDateOnString,
                        endDateOnString
                ).get();

                // se l'esecuzione va a buon fine allora ritorna true
                if (execResult.equals("success")) {

                    result = true;

                    // messaggio di corretto salvataggio dei dati
                    Toast.makeText(context, context.getResources().getString(R.string.success_save_data),
                            Toast.LENGTH_LONG).show();

                } else {

                    // visualizzazione del messaggio di errore nel salvataggio
                    Toast.makeText(context, context.getResources().getString(R.string.error_save_data),
                            Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return result;
    }


    // Metodo per l'aggiornamento dei dati delle tappe nel database
    public boolean editLocation(final int id, final HashMap<String, Location> locations){

        // definizione del flag di corretta esecuzione
        boolean result = false;

        // messaggio di errore per lista di tappe vuota
        if(locations.isEmpty()){

            Toast.makeText(context, R.string.empty_location_list, Toast.LENGTH_LONG).show();
            success = false;
        }

        // se non ci sono errori
        else {

            try {

                Toast.makeText(context, context.getResources().getText(R.string.acquiring_data),
                        Toast.LENGTH_SHORT).show();

                UpdateLocationsData updateLocationsDataAsyncTask = new UpdateLocationsData();

                // passaggio della lista delle tappe da salvare
                updateLocationsDataAsyncTask.setLocations(locations);

                // esecuzione del salvataggio dei dati aggiornati
                String execResult = updateLocationsDataAsyncTask.execute(Integer.toString(id)).get();

                // se l'esecuzione va a buon fine allora ritorna true
                if (execResult.equals("success")) {

                    result = true;

                    // messaggio di corretto salvataggio dei dati
                    Toast.makeText(context, context.getResources().getString(R.string.success_save_data),
                            Toast.LENGTH_LONG).show();

                } else {

                    // visualizzazione del messaggio di errore nel salvataggio
                    Toast.makeText(context, context.getResources().getString(R.string.error_save_data),
                            Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return result;
    }


    // Metodo per l'acquisizione dei tour ricercati tramite barra di ricerca
    public ArrayList<Tour> getTours(final String username, boolean isCicerone) {

        // lista delle attività trovati
        //ArrayList<Tour> findTours = new ArrayList<>();
        loadedTours.clear();

        String ciceroneFlag = "";

        try {

            // conversione del carattere booleano in un carattere per evtare confusione
            if(isCicerone)
                ciceroneFlag = "1";
            else
                ciceroneFlag = "0";

            // inizializzazione dell'AsyncTask
            GetTourListData getTourListDataAsyncTask = new GetTourListData();

            // esecuzione della ricerca dei tour
            getTourListDataAsyncTask.execute(username, ciceroneFlag).get();

            // acquisizione dei tour cercati
            //findTours = new ArrayList<>(getTourListDataAsyncTask.getLoadedTours());
            loadedTours = new ArrayList<>(getTourListDataAsyncTask.getLoadedTours());

        } catch (ExecutionException e) {

            e.printStackTrace();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        // restituzione dei tour trovati
        return loadedTours;  //findTours;
    }


    // Metodo per la cancellazione dei dati dell'evento nel database
    public boolean deleteEvent(final int id) {

        // definizione del flag di corretta esecuzione
        boolean result = false;

        try {

            DeleteEventData deleteEventDataAsyncTask = new DeleteEventData();

            // esecuzione della cancellazione dei dati
            String execResult = deleteEventDataAsyncTask.execute(Integer.toString(id)).get();

            // se l'esecuzione va a buon fine allora ritorna true
            if (execResult.equals("success")) {

                result = true;

                // messaggio di corretto salvataggio dei dati
                Toast.makeText(context, context.getResources().getString(R.string.success_delete_event),
                        Toast.LENGTH_LONG).show();

            } else {

                // visualizzazione del messaggio di errore nel salvataggio
                Toast.makeText(context, context.getResources().getString(R.string.error_delete_event),
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return result;
    }


    // Metodo per l'acquisizione degli eventi in base al globetrotter
    public ArrayList<Event> getEventsSubscribed(final String username) {

        // lista degli eventi trovati
        loadedEvents.clear();

        try {

            // inizializzazione dell'AsyncTask
            GetEventsSubscribedData getMyEventAsyncTask = new GetEventsSubscribedData();

            // esecuzione della ricerca degli eventi
            getMyEventAsyncTask.execute(username).get();

            // acquisizione degli eventi cercati
            loadedEvents = new ArrayList<>(getMyEventAsyncTask.getSubscribeds());

            // aggiornamento degli eventi
            orderingEventByStartDate(loadedEvents);

        } catch (ExecutionException e) {

            e.printStackTrace();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        // restituzione dei tour trovati
        return loadedEvents;  //findTours;
    }


    // Metodo per l'acquisizione dell'evento in base al suo id
    public Event getEventFromID(final int eventID) {

        // evento trovato
        Event loadedEvent = new Event();

        String ciceroneFlag = "";

        try {

            // inizializzazione dell'AsyncTask
            GetEventFromIDData getEventAsyncTask = new GetEventFromIDData();

            // esecuzione della ricerca dei tour
            getEventAsyncTask.execute(Integer.toString(eventID), ciceroneFlag).get();

            // acquisizione dei tour cercati
            loadedEvent = getEventAsyncTask.getLoadedEvent();

        } catch (ExecutionException e) {

            e.printStackTrace();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        // restituzione dell'evento trovato
        return loadedEvent;
    }


    // Metodo per l'acquisizione dei tour ricercati tramite barra di ricerca
    public ArrayList<Tour> getTourFromSearch(String keywords, String username) {

        // lista delle attività trovati
        ArrayList<Tour> findTours = new ArrayList<>();

        try {

            // inizializzazione dell'AsyncTask
            GetSearchTourData searchTourDataAsyncTask = new GetSearchTourData();

            // esecuzione della ricerca dei tour
            searchTourDataAsyncTask.execute(keywords, username).get();

            // acquisizione dei tour cercati
            findTours = new ArrayList<>(searchTourDataAsyncTask.getLoadedTours());

        } catch (ExecutionException e) {

            e.printStackTrace();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        // restituzione dei tour trovati
        return findTours;
    }


    // Metodo per l'acquisizione dei dati completi del tour ricercato tramite ID
    public HashMap<String, Object> getTourFromID(int tourID) {

        // lista delle attività trovati
        HashMap<String, Object> tourData = new HashMap<>();

        try {

            // inizializzazione dell'AsyncTask
            GetTourInfoFromIdData tourDataFromIDAsyncTask = new GetTourInfoFromIdData();

            // esecuzione della ricerca dei tour
            tourDataFromIDAsyncTask.execute(Integer.toString(tourID)).get();

            // acquisizione dei tour cercati
            tourData = tourDataFromIDAsyncTask.getLoadedTour();

        } catch (ExecutionException e) {

            e.printStackTrace();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        // restituzione dei tour trovati
        return tourData;
    }





    // ----- METODI DI SERVIZIO DEL CONTROLLER -----

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


    public void setTempNewTourData(Vector<String> tempNewTourData) {

        this.tempNewTourData = new Vector<>(tempNewTourData);
    }


    public boolean hasTempNewTourData(){

        return tempNewTourData != null;
    }


    public Vector<String> popTempNewTourData(){

        Vector<String> data = new Vector<>(tempNewTourData);

        tempNewTourData.clear();
        tempNewTourData = null;

        return data;
    }


    public void setTempLocations(HashMap<String, Location> tempLocations) {

        this.tempLocations = new HashMap<>(tempLocations);
    }


    public HashMap<String, Location> popTempLocation() {

        HashMap<String, Location> data = new HashMap<>(tempLocations);

        tempLocations.clear();
        tempLocations = null;

        return data;
    }


    public boolean hasTempLocations(){

        return tempLocations != null ;
    }


    public ArrayList<Tour> getLoadedTours(){

        return loadedTours;
    }


    public ArrayList<Event> getLoadedEvents(){

        return loadedEvents;
    }


    public ArrayList<Language> getLoadedLanguages(){

        return loadedLanguages;
    }



    // ----- CLASSI DERIVATE DA ASYNCTASK -----

    /*
    Classe locale per l'invio dei dati di un nuovo tour in modo asincrono
     */
    class SaveTourData extends AsyncTask<Void, Void, Void> {

        // variabili da salvare
        String name;
        String description;
        float cost;
        Calendar startDate;
        Calendar endDate;
        HashMap<String, Location> locations;
        boolean localSuccess;

        ProgressDialog dialog;

        protected SaveTourData(String tourName, String tourDescription, float tourCost,
                               Calendar tourStartDate, Calendar tourEndDate,
                               HashMap<String, Location> locations, boolean success) {

            dialog = new ProgressDialog(context);
            dialog.setCancelable (false);

            name = tourName;
            description = tourDescription;
            cost = tourCost;
            startDate = tourStartDate;
            endDate = tourEndDate;
            localSuccess = success;
            this.locations = new HashMap<>(locations);
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            Toast.makeText(context, context.getResources().getText(R.string.acquiring_data),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            // definizione del messaggio di dialogo per il salvataggio dei dati
            dialog.setMessage(context.getResources().getString(R.string.saving_data));

            // definizione di una richiesta per il salvataggio di una nuova attività
            final StringRequest string_request = new StringRequest(Request.Method.POST,
                    AppConfiguration.URL_FILE_NEW_TOUR, new Response.Listener<String>() {

                // definizione del comportamento in risposta
                @Override
                public void onResponse(String response) {

                    // messaggio di debug con conseguente camuffamento della pagina di dialogo
                    Log.d(context.getClass().getSimpleName(), "Saving tour Response: " + response);

                    try {

                        Log.d("response REST php API", response);

                        // definizione di un oggetto json dalla risposta ricevuta
                        JSONObject json = new JSONObject (response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));

                        Log.d("JSON to REST php API", json.toString());

                        // definizione dell'errore accaduto
                        boolean error = json.getBoolean("error");

                        // se l'errore è FALSE
                        if (!error) {

                            // messaggio di corretto salvataggio dei dati
                            Toast.makeText(context, context.getResources().getString(R.string.success_save_data),
                                    Toast.LENGTH_LONG).show();

                            localSuccess = true;

                        } else { // errore nel salvataggio

                            // visualizzazione del messaggio di errore nel salvataggio
                            Toast.makeText(context, json.getString("error_msg"),
                                    Toast.LENGTH_LONG).show();

                            localSuccess = false;
                        }

                    } catch (JSONException e) {// gestione dell'eccezione

                        // errore nel file JSON passato
                        e.printStackTrace();
                        Toast.makeText(context, "JSON error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();

                        localSuccess = false;
                    }
                }

            }, new Response.ErrorListener () {

                // definizione del comportamento all'errore generico di login
                @Override
                public void onErrorResponse (VolleyError error) {

                    // messaggio di debug
                    Log.e (RegisterActivity.class.getSimpleName(), "Saving data Error: " + error.getMessage());

                    // visualizzazione del messaggio di errore
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();

                    localSuccess = false;
                }

            }) { // definizione del comportamento dello string request

                @Override
                protected Map<String, String> getParams() {

                    // Definizione della mappa dei parametri inseriti
                    Map<String, String> parameters = new HashMap<String, String>();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    // inserimento nella mappa dei parametri inseriti
                    parameters.put ("name", name);
                    parameters.put ("description", description);
                    parameters.put ("cost", String.valueOf(cost));
                    parameters.put ("start_date", String.valueOf(dateFormat.format(startDate.getTime())));
                    parameters.put ("end_date", String.valueOf(dateFormat.format(endDate.getTime())));
                    parameters.put ("cicerone", local_db.getMyAccount().getUsername());

                    for(Map.Entry<String, Location> pair : locations.entrySet())
                        parameters.put("loc-" + pair.getKey(),
                                pair.getValue().getLatitude() + "," + pair.getValue().getLongitude());

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
            AppController.get_instance().add_to_request_queue (string_request, "req_save_data");

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);

            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
        }
    }


    /*
    Classe locale per l'acquisizione della lista di tour associati
    ad un utente
     */
    class GetTourListData extends AsyncTask<String, Void, String> {

        // lista dei tour caricati
        private ArrayList<Tour> loadedTours = new ArrayList<>();


        @Override
        protected String doInBackground(String... data) {

            // dati da inviare al servizio REST
            HashMap<String, String> POSTdata = new HashMap<>();

            // inizializzazione dei dati da inviare
            POSTdata.put("username", data[0]);
            POSTdata.put("is_a_cicerone", data[1]);
            POSTdata.put("tour_id", "0");


            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_GET_TOUR, POSTdata, "POST");

            try {

                Log.d("response REST php API", response);

                // definizione di un oggetto json dalla risposta ricevuta
                //JSONObject json = new JSONObject (response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("{");
                stringBuilder.append(StringUtils.substringBeforeLast(StringUtils.substringAfter(response, "{"), "}"));
                stringBuilder.append("}");

                // definizione di un oggetto json dalla risposta ricevuta
                //
                JSONObject json = new JSONObject(stringBuilder.toString());


                Log.d("JSON to REST php API", json.toString());

                // definizione dell'errore accaduto
                boolean error = json.getBoolean("error");

                // se l'errore è FALSE
                if (!error) {

                    loadedTours.clear();

                    // definizione di un novo oggetto JSON acquisendo, dalla risposta presa,
                    // l'array associativo di nome "tours" (e trasformato in oggetto JSON)
                    //JSONObject tours = json.getJSONObject("tours");
                    JSONArray toursArray = json.getJSONArray("tours");

                    for(int i = 0; i < toursArray.length(); i ++) {

                        JSONObject tours = toursArray.getJSONObject(i);

                        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);

                        Tour tour = new Tour(
                                tours.getInt("id"),
                                tours.getString("name"),
                                null,
                                null,
                                null,
                                tours.getString("description"),
                                Float.valueOf(tours.getString("cost")));

                        Calendar c = Calendar.getInstance();
                        c.setTime(dataFormat.parse(tours.getString("start_date")));
                        tour.setStartDate(c);

                        c.setTime(dataFormat.parse(tours.getString("end_date")));
                        tour.setEndDate(c);

                        loadedTours.add(tour);
                    }

                }

            } catch (JSONException e) {

                e.printStackTrace();

            } catch (ParseException e) {

                e.printStackTrace();
            }


            return response;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
        }


        // metodo per l'esecuzione della chiamata del servizio REST
        // per l'acquisizione dei dati
        public String performCall(String requestURL, HashMap<String, String> keywords, String method) {

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
                writer.write(convertDataForPostSending(keywords));
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


        // metodo per l'acquisizione dei tour ricercati
        public ArrayList<Tour> getLoadedTours() {

            return loadedTours;
        }

    }


    /*
    Classe locale per l'acquisizione della lista di eventi sottoscritti
    da un utente
    */
    class GetEventsSubscribedData extends AsyncTask<String, Void, String> {

        // lista dei tour caricati
        private ArrayList<Event> subscribeds = new ArrayList<>();


        @Override
        protected String doInBackground(String... data) {

            // dati da inviare al servizio REST
            HashMap<String, String> POSTdata = new HashMap<>();

            // inizializzazione dei dati da inviare
            POSTdata.put("globetrotter", data[0]);


            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_GET_EVENTS_SUBSCRIBED, POSTdata, "POST");


            try {

                Log.d("response REST php API", response);

                // definizione di un oggetto json dalla risposta ricevuta
                //JSONObject json = new JSONObject (response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("{");
                stringBuilder.append(StringUtils.substringBeforeLast(StringUtils.substringAfter(response, "{"), "}"));
                stringBuilder.append("}");

                // definizione di un oggetto json dalla risposta ricevuta
                //JSONObject json = new JSONObject (response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));
                JSONObject json = new JSONObject(stringBuilder.toString());

                // definizione dell'errore accaduto
                boolean error = json.getBoolean("error");

                // se l'errore è FALSE
                if (!error) {

                    subscribeds.clear();

                    // definizione di un novo oggetto JSON acquisendo, dalla risposta presa,
                    // l'array associativo di nome "tours" (e trasformato in oggetto JSON)
                    //JSONObject tours = json.getJSONObject("tours");
                    JSONArray eventsArray = json.getJSONArray("events");


                    for (int i = 0; i < eventsArray.length(); i++) {

                        // lista dei tour caricati
                        JSONObject tours = eventsArray.getJSONObject(i);
                        Tour tourEvent;

                        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);

                        // definizione dell'attivita come oggetto
                        Tour tour = new Tour(
                                tours.getInt("tour_id"),
                                tours.getString("tour_name"),
                                null,
                                null,
                                null,
                                null,
                                Float.valueOf(tours.getString("tour_cost"))
                        );

                        Event myevent = new Event(
                                tours.getInt("event_id"),
                                null,
                                null,
                                null,
                                tours.getString("description"),
                                tours.getInt("max_subscribed")
                        );

                        Calendar c = Calendar.getInstance();
                        c.setTime(dataFormat.parse(tours.getString("start_date")));
                        myevent.setStartDate(c);

                        Calendar calendarEndDate = Calendar.getInstance();
                        calendarEndDate.setTime(dataFormat.parse(tours.getString("end_date")));
                        myevent.setEndDate(calendarEndDate);

                        myevent.setTour(tour);


                        subscribeds.add(myevent);
                    }
                }

            } catch (JSONException e) {

                e.printStackTrace();

            } catch (ParseException e) {

                e.printStackTrace();
            }


            return response;
        }


        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
        }


        // metodo per l'esecuzione della chiamata del servizio REST
        // per l'acquisizione dei dati
        public String performCall(String requestURL, HashMap<String, String> keywords, String method) {

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
                writer.write(convertDataForPostSending(keywords));
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


        // metodo per l'acquisizione dei tour ricercati
        public ArrayList<Event> getSubscribeds() {

            return subscribeds;
        }

    }


	/*
    Classe locale per l'acquisizione dei dati degli eventi
    tramite inserimento dell'id del tour
    */
    class GetEventFromIDData extends AsyncTask<String, Void, String> {

        // lista dei tour caricati
        private Event loadedEvent = new Event();


        @Override
        protected String doInBackground(String... data) {

            // dati da inviare al servizio REST
            HashMap<String, String> POSTdata = new HashMap<>();

            // inizializzazione dei dati da inviare
            POSTdata.put("id", data[0]);


            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_GET_EVENT_BY_ID, POSTdata, "POST");


            try {

                Log.d("response REST php API", response);

                // definizione di un oggetto json dalla risposta ricevuta
                //JSONObject json = new JSONObject (response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("{");
                stringBuilder.append(StringUtils.substringBeforeLast(StringUtils.substringAfter(response, "{"), "}"));
                stringBuilder.append("}");

                // definizione di un oggetto json dalla risposta ricevuta
                //JSONObject json = new JSONObject (response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));
                JSONObject json = new JSONObject(stringBuilder.toString());

                // definizione dell'errore accaduto
                boolean error = json.getBoolean("error");

                // se l'errore è FALSE
                if (!error) {

                    // definizione di un novo oggetto JSON acquisendo, dalla risposta presa,
                    // l'array associativo di nome "tours" (e trasformato in oggetto JSON)
                    //JSONObject tours = json.getJSONObject("tours");
                    JSONObject eventData = json.getJSONObject("event");

                    SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);

                    Event event = new Event(
                            eventData.getInt("id"),
                            null,
                            null,
                            null,
                            eventData.getString("description"),
                            eventData.getInt("max_subscribed")
                    );

                    Tour tour = new Tour();
                    tour.setId(Integer.parseInt(eventData.getString("tour_id")));
                    event.setTour(tour);

                    Calendar c = Calendar.getInstance();
                    c.setTime(dataFormat.parse(eventData.getString("start_date")));
                    event.setStartDate(c);

                    c.setTime(dataFormat.parse(eventData.getString("end_date")));
                    event.setEndDate(c);

                    loadedEvent = event;
                }

            } catch (JSONException e) {

                e.printStackTrace();

            } catch (ParseException e) {

                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
        }


        // metodo per l'esecuzione della chiamata del servizio REST
        // per l'acquisizione dei dati
        public String performCall(String requestURL, HashMap<String, String> keywords, String method) {

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
                writer.write(convertDataForPostSending(keywords));
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


        // metodo per l'acquisizione dei tour ricercati
        public Event getLoadedEvent() {

            return loadedEvent;
        }

    }


    /*
    Classe locale per l'acquisizione dei dati del tour
    tramite ricerca di parole chiave
    */
    public class GetSearchTourData extends AsyncTask<String, Void, String> {

        ArrayList<Tour> loadedTours = new ArrayList<>();

        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            POSTdata.put("keywords", data[0]);
            POSTdata.put("username", data[1]);


            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_GET_SEARCHED_TOUR, POSTdata, "POST");

            try {

                Log.d("response REST php API", response);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("{");
                stringBuilder.append(StringUtils.substringBeforeLast(StringUtils.substringAfter(response, "{"), "}"));
                stringBuilder.append("}");

                // definizione di un oggetto json dalla risposta ricevuta
                //JSONObject json = new JSONObject (response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));
                JSONObject json = new JSONObject(stringBuilder.toString());

                // definizione dell'errore accaduto
                boolean error = json.getBoolean("error");

                // se l'errore è FALSE
                if (!error) {

                    // svuotamento dei tour precedentemente caricati
                    loadedTours.clear();

                    // definizione di un novo array JSON acquisendo, dalla risposta presa,
                    // l'array associativo di nome "tours" (e trasformato in oggetto JSON)
                    JSONArray toursArray = json.getJSONArray("tours");

                    for(int i = 0; i < toursArray.length(); i ++) {

                        // acquisizione dell'oggetto JSON da trasformare in oggetto Tour
                        JSONObject tours = toursArray.getJSONObject(i);

                        // definizione del formato della data
                        SimpleDateFormat dataFormat = new SimpleDateFormat("dd-MM", Locale.ITALY);

                        // definizione del nuovo oggetto Tour
                        Tour tour = new Tour(
                                tours.getInt("id"),
                                tours.getString("name"),
                                null,
                                null,
                                null,
                                tours.getString("description"),
                                Float.valueOf(tours.getString("cost")));

                        // definizione della data di inizio dell'attività
                        Calendar c = Calendar.getInstance();
                        c.setTime(dataFormat.parse(tours.getString("start_date")));
                        tour.setStartDate(c);

                        // definizione della data di fine dell'attività
                        c.setTime(dataFormat.parse(tours.getString("end_date")));
                        tour.setEndDate(c);

                        // aggiunta del tour tra quelli caricati
                        loadedTours.add(tour);
                    }

                } else { // errore nel salvataggio

                    Tour fakeTour = new Tour();

                    fakeTour.setName(context.getResources().getString(R.string.no_tour_find));
                    fakeTour.setDescription(context.getResources().getString(R.string.better_search_keywords));

                    loadedTours.add(fakeTour);
                }

            } catch (JSONException e) {

                e.printStackTrace();

            } catch (ParseException e) {

                e.printStackTrace();
            }


            return response;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
        }


        // metodo per l'esecuzione della chiamata del servizio REST
        // per l'acquisizione dei dati
        public String performCall(String requestURL, HashMap<String, String> keywords, String method) {

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
                writer.write(convertDataForPostSending(keywords));
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


        // metodo per l'acquisizione dei tour ricercati
        public ArrayList<Tour> getLoadedTours() {

            return loadedTours;
        }

    }


    /*
    Classe locale per l'acquisizione dei dati completi
    del tour tramite passaggio del suo id
    */
    class GetTourInfoFromIdData extends AsyncTask<String, Void, String> {

        // lista dei tour caricati
        private HashMap<String, Object> loadedTourData = new HashMap<>();


        @Override
        protected String doInBackground(String... data) {

            // dati da inviare al servizio REST
            HashMap<String, String> POSTdata = new HashMap<>();

            // inizializzazione dei dati da inviare
            POSTdata.put("tour_id", data[0]);

            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_GET_TOUR_BY_ID, POSTdata, "POST");

            try {

                // definizione di un oggetto json dalla risposta ricevuta
                //JSONObject json = new JSONObject (response.substring (response.indexOf("{"), response.lastIndexOf("}") + 1));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("{");
                stringBuilder.append(StringUtils.substringBeforeLast(StringUtils.substringAfter(response, "{"), "}"));
                stringBuilder.append("}");

                // definizione di un oggetto json dalla risposta ricevuta
                //
                JSONObject json = new JSONObject(stringBuilder.toString());


                Log.d("JSON to REST php API", json.toString());

                // definizione dell'errore accaduto
                boolean error = json.getBoolean("error");

                // se l'errore è FALSE
                if (!error) {

                    // definizione del formato della data
                    SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);

                    // inizializzazione di una lista di tappe
                    ArrayList<Location> locationList = new ArrayList<>();

                    // inizializzazione di una lista di eventi
                    ArrayList<Event> eventList = new ArrayList<>();

                    // inizializzazione di una lista di recensioni
                    ArrayList<Feedback> feedbackList = new ArrayList<>();

                    // inizializzazione di una lista di lingue
                    ArrayList<Language> languageList = new ArrayList<>();

                    // inizializzazione di una lista di iscrizioni
                    ArrayList<Subscribed> subscribedList = new ArrayList<>();

                    // inizializzazione di una lista di richieste
                    ArrayList<com.winotech.cicerone.model.Request> requestList = new ArrayList<>();


                    // definizione di un novo oggetto JSON acquisendo, dalla risposta presa,
                    // l'array associativo di nome "data" (e trasformato in oggetto JSON)
                    JSONObject jsonData = json.getJSONObject("data");


                    // ----- ACQUISIZIONE DEI DATI SUL CICERONE -----
                    // acquisizione dei dati sul cicerone
                    JSONObject ciceroneData = jsonData.getJSONObject("cicerone");

                    // definizione del cicerone come oggetto
                    Cicerone cicerone = new Cicerone();
                    cicerone.setUsername(ciceroneData.getString("username"));
                    cicerone.setEmail(ciceroneData.getString("email"));
                    cicerone.setFirstName(ciceroneData.getString("first_name"));
                    cicerone.setLastName(ciceroneData.getString("last_name"));
                    cicerone.setPhone(ciceroneData.getString("phone"));

                    // definizione dei parametri che possono essere null
                    if(ciceroneData.getString("image_path") != null)
                        cicerone.setPhoto(getBitmapFromURL(AppConfiguration.URL_PREFIX + ciceroneData.getString("image_path")));
                    if(ciceroneData.getString("biography") != null)
                        cicerone.setBiography(ciceroneData.getString("biography"));


                    // ----- ACQUISIZIONE DEI DATI SULL'ATTIVITA' -----
                    // acquisizione dei dati sull'attività
                    JSONObject tourData = jsonData.getJSONObject("tour");

                    // definizione dell'attivita come oggetto
                    Tour tour = new Tour(
                            tourData.getInt("id"),
                            tourData.getString("name"),
                            null,
                            null,
                            null,
                            tourData.getString("description"),
                            Float.valueOf(tourData.getString("cost"))
                    );

                    // definizione della data di inizio di validità dell'attività
                    Calendar startDate = Calendar.getInstance();
                    startDate.setTime(dataFormat.parse(tourData.getString("start_date")));
                    tour.setStartDate(startDate);

                    // definizione della data di fine di validità dell'attività
                    Calendar endDate = Calendar.getInstance();
                    endDate.setTime(dataFormat.parse(tourData.getString("end_date")));
                    tour.setEndDate(endDate);

                    //Associazione del ciceorne al tour
                    tour.setCicerone(cicerone);


                    // ----- ACQUISIZIONE DEI DATI SULLE TAPPE -----
                    // acquisizione dei dati sulle tappe
                    JSONArray locationData = jsonData.getJSONArray("locations");

                    // acqusiizione e definizione di un insieme di tappe
                    for(int i = 0; i < locationData.length(); i ++){

                        // acquisizione della tappa
                        JSONObject jsonLocation = locationData.getJSONObject(i);

                        // definizione della tappa come oggetto
                        Location location = new Location(
                                0,
                                Float.valueOf(jsonLocation.getString("latitude")),
                                Float.valueOf(jsonLocation.getString("longitude"))
                        );

                        // aggiunta della tappa al l'attività corrispondente
                        tour.addTourLocation(location);

                        // aggiunta della tappa alla lista
                        locationList.add(location);
                    }


                    // ----- ACQUISIZIONE DEI DATI SULLE RECENSIONI -----
                    if(!jsonData.isNull("feedbacks")) {

                        // acquisizione dei dati sulle recensioni
                        JSONArray feedbackData = jsonData.getJSONArray("feedbacks");

                        // acqusiizione e definizione di un insieme di recensioni
                        for (int i = 0; i < feedbackData.length(); i++) {

                            // acquisizione del'evento
                            JSONObject jsonFeedback = feedbackData.getJSONObject(i);

                            // definizione della recensione come oggetto
                            Feedback feedback = new Feedback(
                                    jsonFeedback.getInt("id"),
                                    null,
                                    null,
                                    jsonFeedback.getString("description"),
                                    jsonFeedback.getInt("rate"),
                                    null
                            );

                            // settaggio dell'attività associata alla recensione
                            feedback.setTour(tour);

                            // settaggio del globetrotter che ha definito la recensione
                            Globetrotter globetrotter = new Globetrotter();
                            globetrotter.setUsername(jsonFeedback.getString("username"));
                            globetrotter.setEmail(jsonFeedback.getString("email"));
                            globetrotter.setFirstName(jsonFeedback.getString("first_name"));
                            globetrotter.setLastName(jsonFeedback.getString("last_name"));
                            globetrotter.setPhoto(getBitmapFromURL(AppConfiguration.URL_PREFIX +
                                    jsonFeedback.getString("image_path")));
                            globetrotter.setPhone(jsonFeedback.getString("phone"));
                            globetrotter.setBiography(jsonFeedback.getString("biography"));
                            feedback.setGlobetrotter(globetrotter);

                            // definizione della data di creazione della recensione
                            Calendar feedbackCreatedOn = Calendar.getInstance();
                            feedbackCreatedOn.setTime(dataFormat.parse(jsonFeedback.getString("created_on")));
                            feedback.setCreatedOn(feedbackCreatedOn);

                            // aggiunta della recensione all'attività
                            tour.addTourFeedbacks(feedback);

                            // aggiunta della recensione alla lista
                            feedbackList.add(feedback);
                        }
                    }

                    // se nn ci sono informazioni sugli eventi, allora è inutile
                    // tentare di acquisire le altre informazioni come gli iscritti o le richieste
                    // poiche all'interno del db sul rerver non viene acquisito nulla
                    // (essendo event_id una chiave esterna per molte tabelle)
                    if(!jsonData.isNull("events")) {

                        // ----- ACQUISIZIONE DEI DATI SULLE TAPPE -----
                        // acquisizione dei dati sugli eventi
                        JSONArray eventData = jsonData.getJSONArray("events");

                        // acqusiizione e definizione di un insieme di eventi
                        for (int i = 0; i < eventData.length(); i++) {

                            // acquisizione del'evento
                            JSONObject jsonEvent = eventData.getJSONObject(i);

                            // definizione dell'evento come oggetto
                            Event event = new Event(
                                    jsonEvent.getInt("id"),
                                    null,
                                    null,
                                    null,
                                    jsonEvent.getString("description"),
                                    jsonEvent.getInt("max_subscribed")
                            );

                            // settaggio dell'attività associata all'evento
                            event.setTour(tour);

                            // definizione della data di inizio di validità dell'evento
                            Calendar eventStartDate = Calendar.getInstance();
                            eventStartDate.setTime(dataFormat.parse(jsonEvent.getString("start_date")));
                            event.setStartDate(eventStartDate);

                            // definizione della data di fine di validità dell'evento
                            Calendar eventEndDate = Calendar.getInstance();
                            eventEndDate.setTime(dataFormat.parse(jsonEvent.getString("end_date")));
                            event.setEndDate(eventEndDate);

                            // aggiunta dell'evento all'attività corrispondente
                            tour.addTourEvent(event);

                            // aggiunta dell'evento alla lista
                            eventList.add(event);
                        }

                        // ordinamento eventi per data di inizio
                        orderingEventByStartDate(eventList);


                        // ----- ACQUISIZIONE DEI DATI SULLE LINGUE PARLATE NEL TOUR -----
                        if(!jsonData.isNull("spoken_languages")) {

                            // acquisizione dei dati sulle lingue parlate
                            JSONArray spokenLanguageData = jsonData.getJSONArray("spoken_languages");

                            // acqusiizione e definizione di un insieme di lingue
                            for (int i = 0; i < spokenLanguageData.length(); i++) {

                                // acquisizione della lingua
                                JSONObject jsonLanguage = spokenLanguageData.getJSONObject(i);

                                // definizione della lingua come oggetto
                                Language language = new Language(
                                        jsonLanguage.getInt("language_id"),
                                        jsonLanguage.getString("name")
                                );

                                // ricerca dell'evento a cui si associa la lingua
                                for (Event event : eventList) {

                                    // inserimento della stessa lingua per l'evento
                                    if (event.getId() == jsonLanguage.getInt("event_id"))
                                        event.addEventLanguages(language);
                                }

                                // aggiunta della lingua alla lista
                                languageList.add(language);
                            }
                        }


                        // ----- ACQUISIZIONE DEI DATI SULLE ISCRIZIONI AGLI EVENTI -----
                        if(!jsonData.isNull("subscribeds")) {

                            // acquisizione dei dati sulle iscrizioni
                            JSONArray subscribedData = jsonData.getJSONArray("subscribeds");

                            // acqusiizione e definizione di un insieme di iscrizioni
                            for (int i = 0; i < subscribedData.length(); i++) {

                                // acquisizione dell'iscrizione
                                JSONObject jsonSubscribed = subscribedData.getJSONObject(i);

                                // definizione dell'iscrizione come oggetto
                                Subscribed subscribed = new Subscribed(
                                        null,
                                        null,
                                        jsonSubscribed.getInt("group_dimension"),
                                        false
                                );

                                // ricerca dell'evento a cui si associa l'iscrizione
                                for (Event event : eventList) {

                                    // inserimento della stessa iscrizione per l'evento
                                    if (event.getId() == jsonSubscribed.getInt("event_id")) {

                                        event.addEventSubscribeds(subscribed);
                                        subscribed.setEvent(event);
                                    }
                                }

                                // settaggio del globetrotter che ha definito l'iscrizione
                                Globetrotter globetrotter = new Globetrotter();
                                globetrotter.setUsername(jsonSubscribed.getString("username"));
                                globetrotter.setEmail(jsonSubscribed.getString("email"));
                                globetrotter.setFirstName(jsonSubscribed.getString("first_name"));
                                globetrotter.setLastName(jsonSubscribed.getString("last_name"));
                                globetrotter.setPhoto(getBitmapFromURL(AppConfiguration.URL_PREFIX +
                                        jsonSubscribed.getString("image_path")));
                                globetrotter.setPhone(jsonSubscribed.getString("phone"));
                                globetrotter.setBiography(jsonSubscribed.getString("biography"));
                                subscribed.setGlobetrotter(globetrotter);

                                // definizione del flag di pagamento dell'iscrizione
                                if(jsonSubscribed.getString("has_payed").equals("1"))
                                    subscribed.setHasPayed(true);

                                // aggiunta della iscrizione alla lista
                                subscribedList.add(subscribed);
                            }
                        }


                        // ----- ACQUISIZIONE DEI DATI SULLE RICHIESTE DI ISCRIZIONE AGLI EVENTI -----
                        if(!jsonData.isNull("requests")) {

                            // acquisizione dei dati sulle richieste
                            JSONArray requestData = jsonData.getJSONArray("requests");

                            // acqusiizione e definizione di un insieme di richieste
                            for (int i = 0; i < requestData.length(); i++) {

                                // acquisizione della richiesta
                                JSONObject jsonRequest = requestData.getJSONObject(i);

                                // definizione della richiesta come oggetto
                                com.winotech.cicerone.model.Request request = new com.winotech.cicerone.model.Request(
                                        null,
                                        null,
                                        jsonRequest.getInt("group_dimension"),
                                        null,
                                        null
                                );

                                // settaggio del globetrotter che ha definito la richiesta
                                Globetrotter globetrotter = new Globetrotter();
                                globetrotter.setUsername(jsonRequest.getString("username"));
                                globetrotter.setEmail(jsonRequest.getString("email"));
                                globetrotter.setFirstName(jsonRequest.getString("first_name"));
                                globetrotter.setLastName(jsonRequest.getString("last_name"));
                                globetrotter.setPhoto(getBitmapFromURL(AppConfiguration.URL_PREFIX +
                                        jsonRequest.getString("image_path")));
                                globetrotter.setPhone(jsonRequest.getString("phone"));
                                globetrotter.setBiography(jsonRequest.getString("biography"));
                                request.setGlobetrotter(globetrotter);

                                // ricerca dell'evento a cui si associa la richiesta
                                for (Event event : eventList) {

                                    // inserimento della stessa richiesta per l'evento
                                    if (event.getId() == jsonRequest.getInt("event_id")) {

                                        event.addEventRequests(request);
                                        request.setEvent(event);
                                    }
                                }

                                // definizione della data di creazione della richiesta
                                Calendar requestCreatedOn = Calendar.getInstance();
                                requestCreatedOn.setTime(dataFormat.parse(jsonRequest.getString("created_on")));
                                request.setCreatedOn(requestCreatedOn);

                                // definizione del flag di accettazione della richiesta
                                if (jsonRequest.getString("accepted").equals("0"))
                                    request.setAccepted(false);
                                else if (jsonRequest.getString("accepted").equals("1"))
                                    request.setAccepted(true);

                                // aggiunta della richiesta alla lista
                                requestList.add(request);
                            }
                        }
                    }


                    // inserimento dei dati caricati nella mappa
                    loadedTourData.put("tour", tour);
                    loadedTourData.put("cicerone", cicerone);
                    loadedTourData.put("locations", locationList);
                    loadedTourData.put("events", eventList);
                    loadedTourData.put("feedbacks", feedbackList);
                    loadedTourData.put("spoken_languages", languageList);
                    loadedTourData.put("subscribeds", subscribedList);
                    loadedTourData.put("requests", requestList);
                }

            } catch (JSONException e) {

                e.printStackTrace();

            } catch (ParseException e) {

                e.printStackTrace();
            }


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


        // metodo per l'acquisizione dei dati del tour ricercato
        public HashMap<String, Object> getLoadedTour() {

            return loadedTourData;
        }

    }


    /*
    Classe locale per l'aggiornamento dei dati
    dell'attività
     */
    class UpdateTourData extends AsyncTask<String, Void, String> {

        private String result = "";


        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            // acquisizione della data in cui è stato creata la notifica
            POSTdata.put("tour_id", data[0]);
            POSTdata.put("name", data[1]);
            POSTdata.put("description", data[2]);
            POSTdata.put("cost",  data[3]);
            POSTdata.put("start_date",  data[4]);
            POSTdata.put("end_date",  data[5]);

            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_UPDATE_TOUR, POSTdata, "POST");

            try {

                Log.d("response REST tour: ", response);

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
    Classe locale per la cancellazione dei dati
    dell'attività
     */
    class DeleteTourData extends AsyncTask<String, Void, String> {

        private String result = "";


        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            // acquisizione della data in cui è stato creata la notifica
            Calendar today = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);

            // acquisizione della data in cui è stato creata la notifica
            POSTdata.put("tour_id", data[0]);
            POSTdata.put("cicerone_id", data[1]);
            POSTdata.put("reason_id", data[2]);
            POSTdata.put("created_on", sdf.format(today.getTime()));
            POSTdata.put("description", data[3]);


            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_DELETE_TOUR, POSTdata, "POST");

            try {

                Log.d("response REST tour: ", response);

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
    Classe locale per il salvataggio dei dati
    dell'evento
     */
    class SaveEventData extends AsyncTask<String, Void, String> {

        // lista di lingue da salvare nel db
        private ArrayList<String> languageIDs = new ArrayList<>();

        private String result = "";

        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            // conversione della lista di linguaggi in una stringa formattata
            String array = "";
            int i = 1;
            for(String id : languageIDs) {

                array += id;
                if(i < languageIDs.size())
                    array += ",";
                i ++;
            }

            // acquisizione della data in cui è stato creata la notifica
            POSTdata.put("tour_id", data[0]);
            if(data[1] == null)
                POSTdata.put("description", "NULL");
            else
                POSTdata.put("description", data[1]);
            POSTdata.put("max_subs", data[2]);
            POSTdata.put("start_date",  data[3]);
            POSTdata.put("end_date",  data[4]);
            POSTdata.put("languages",  array);

            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_NEW_EVENT, POSTdata, "POST");

            try {

                Log.d("response REST tour: ", response);

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


        // Metodo per il settaggio della lista di id dei linguaggi
        public void setLanguageIDs(ArrayList<String> languageIDs) {

            this.languageIDs = new ArrayList<>(languageIDs);
        }
    }


    /*
   Classe locale per l'aggiornamento dei dati
   dell'evento
    */
    class UpdateEventData extends AsyncTask<String, Void, String> {

        // lista di lingue da salvare nel db
        private ArrayList<String> languageIDs = new ArrayList<>();

        private String result = "";

        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            // conversione della lista di linguaggi in una stringa formattata
            String array = "";
            int i = 1;
            for(String id : languageIDs) {

                array += id;
                if(i < languageIDs.size())
                    array += ",";
                i ++;
            }

            // acquisizione della data in cui è stato creata la notifica
            POSTdata.put("event_id",  data[0]);
            if(data[1] == null)
                POSTdata.put("description", "NULL");
            else
                POSTdata.put("description", data[1]);
            POSTdata.put("max_subs", data[2]);
            POSTdata.put("start_date",  data[3]);
            POSTdata.put("end_date",  data[4]);
            POSTdata.put("languages",  array);

            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_UPDATE_EVENT, POSTdata, "POST");

            try {

                Log.d("response REST tour: ", response);

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


        // Metodo per il settaggio della lista di id dei linguaggi
        public void setLanguageIDs(ArrayList<String> languageIDs) {

            this.languageIDs = new ArrayList<>(languageIDs);
        }
    }


    /*
    Classe locale per la cancellazione dei dati
    dell'evento
     */
    class DeleteEventData extends AsyncTask<String, Void, String> {

        private String result = "";


        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            // acquisizione della data in cui è stato creata la notifica
            POSTdata.put("event_id", data[0]);


            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_DELETE_EVENT, POSTdata, "POST");

            try {

                Log.d("response REST tour: ", response);

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
    Classe locale per l'aggiornamento dei dati delle tappe
    */
    class UpdateLocationsData extends AsyncTask<String, Void, String> {

        // lista di lingue da salvare nel db
        private ArrayList<String> locations = new ArrayList<>();

        private String result = "";

        @Override
        protected String doInBackground(String... data) {

            HashMap<String, String> POSTdata = new HashMap<>();

            // conversione della lista di linguaggi in una stringa formattata
            String array = "";
            int i = 1;
            for(String location : locations) {

                array += location;
                if(i < locations.size())
                    array += ",";
                i ++;
            }

            // acquisizione della data in cui è stato creata la notifica
            POSTdata.put("tour_id",  data[0]);
            POSTdata.put("locations",  array);

            // chiamata della funzione per l'esecuzione della
            // richiesta dei dati da effettuare al servizio REST
            // del server
            String response = performCall(AppConfiguration.URL_FILE_UPDATE_LOCATIONS, POSTdata, "POST");

            try {

                Log.d("response REST tour: ", response);

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


        // Metodo per il settaggio della lista di tappe
        public void setLocations(HashMap<String, Location> locations) {

            for(Map.Entry<String, Location> location : locations.entrySet())
                this.locations.add(location.getValue().getLatitude() + "/" + location.getValue().getLongitude());

        }
    }



    // ----- METODI PER L'ORDINAMENTO DELLA LISTA DI NOTIFICHE -----

    // Metodo per l'ordinamento degli eventi in base alla data di inizio
    private void orderingEventByStartDate(ArrayList<Event> events) {

        // creazione di un vettore temporaneo
        Vector<Event> eventVector = new Vector<>();

        // copia di ogni notifica all'interno del vettore temporaneo
        eventVector.addAll(events);

        // ordinamento del vettore temporaneo delle notifiche
        sort(eventVector, 0, eventVector.size() - 1);

        // reinserimento secondo l'ordine definito dell'insieme delle notifiche
        events.clear();
        events.addAll(eventVector);
    }


    // metodo per l'ordinamento degli eventi
    private void sort(Vector<Event> events, int indexStart, int indexEnd) {

        if(indexStart < indexEnd){

            int indexCenter = (indexStart + indexEnd) / 2;
            sort(events, indexStart, indexCenter);
            sort(events, indexCenter + 1, indexEnd);
            merging(events, indexStart, indexCenter, indexEnd);
        }
    }


    // Metodo di supporto al merge sort per le notifiche
    private void merging(Vector<Event> events, int indexStart, int indexCenter, int indexEnd) {

        int i = indexStart;
        int j = indexCenter + 1;
        int k = 0;

        Vector<Event> temp = new Vector<>();

        while(i <= indexCenter && j <= indexEnd) {

            if(events.get(i).getStartDate().after(events.get(j).getStartDate())){

                temp.add(k, events.get(i));
                i ++;

            } else {

                temp.add(k, events.get(j));
                j ++;
            }

            k ++;
        }

        while(i <= indexCenter) {

            temp.add(k, events.get(i));
            i ++;
            k ++;
        }

        while ((j <= indexEnd)){

            temp.add(k, events.get(j));
            j ++;
            k ++;
        }

        //notifications.clear();
        for (k = indexStart; k <= indexEnd; k ++){

            events.set(k, temp.get(k - indexStart));
        }
    }

}