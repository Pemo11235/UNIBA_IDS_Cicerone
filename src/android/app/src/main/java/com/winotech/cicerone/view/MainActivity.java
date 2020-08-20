package com.winotech.cicerone.view;


import android.app.Fragment;
//import androidx.fragment.app.Fragment;
import android.app.FragmentTransaction;
import android.app.FragmentManager;
//import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.AuthController;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.misc.SessionManager;
import com.winotech.cicerone.model.Cicerone;
import com.winotech.cicerone.model.Location;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class MainActivity extends AppCompatActivity implements Serializable {

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment fragmentView;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private RadioButton notificationButton;
    private RadioButton searchButton;
    private RadioButton myToursButton;
    private RadioButton myAccountButton;
    private RadioButton settingsButton;



    private DBManager db;
    private SessionManager session;

    private AuthController auth;
    private TourController tourController = TourController.getInstance();
    private GeneralController generalController = GeneralController.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // inizializzazione degli oggetti grafici
        notificationButton = this.findViewById(R.id.radioButton);
        searchButton = this.findViewById(R.id.radioButton2);
        myToursButton = this.findViewById(R.id.radioButton3);
        myAccountButton = this.findViewById(R.id.radioButton4);
        settingsButton = this.findViewById(R.id.radioButton5);

        // definizione dei dati di configurazione
        Configuration config = getResources().getConfiguration();

        // handler del database locale
        db = new DBManager(getApplicationContext());

        // session manager dei dati
        session = new SessionManager(getApplicationContext());

        // definizione del controller per l'autenticazione
        auth = new AuthController(getApplicationContext(), session);

        // inizializzazione del controller delle attività
        if(tourController == null)
            TourController.initInstance(MainActivity.this);
        tourController = TourController.getInstance();

        // inizializzazione del controller generico
        if(generalController == null)
            GeneralController.initInstance(MainActivity.this);
        generalController = GeneralController.getInstance();

        // inizializzazione dell'intent del fragment
        String intentFragment = "";

        // aggiornamento delle iscrizioni ai tour associati
        generalController.updateSubscriptions(db.getMyAccount().getUsername(), db.getMyAccount() instanceof Cicerone);
        generalController.updateRequests(db.getMyAccount().getUsername(), db.getMyAccount() instanceof Cicerone);

        // definizione dell'intent
        if (getIntent() != null && getIntent().getExtras() != null &&
                getIntent().getExtras().getString(Constants.FRAGMENT_TO_LOAD_KEY) != null)//.getExtras().getString("FRAGMENT_TO_LOAD") != null)
            intentFragment = getIntent().getExtras().getString(Constants.FRAGMENT_TO_LOAD_KEY);

        // se l'intent dei fragment non è null allora essite una richiesta di reindirizzamento ad una certa pagina
        if (intentFragment != null) {

            // definizione del reindirizzamento al MyToursFragment
            if (intentFragment.equals(Constants.MY_TOURS_FRAGMENT_VALUE) || intentFragment.equals("")) {

                myToursButton.setBackgroundResource(R.drawable.navbar_selected_button);

                fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = MyToursFragment.newInstance(db);
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();

            }

            // definizione del reindirizzamento al NewTourFragment
            else if (intentFragment.equals(Constants.NEW_TOUR_FRAGMENT_VALUE)) {

                myToursButton.setBackgroundResource(R.drawable.navbar_selected_button);

                fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = NewTourFragment.newInstance(
                        (HashMap<String, Location>)getIntent().getExtras().getSerializable("SET_LOCATIONS"));
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
            }

            // definizione del reindirizzamento al ShowTourCiceroneFragment
            else if (intentFragment.equals(Constants.SHOW_TOUR_CICERONE_FRAGMENT_VALUE)) {

                myToursButton.setBackgroundResource(R.drawable.navbar_selected_button);

                fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = ShowTourCiceroneFragment.newInstance(
                        db,
                        (int)getIntent().getExtras().getSerializable("TOUR_ID"));
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
            }
        }

        // aggiornamento della lingua impostata
        session.updateLanguage(this, session.getLanguage());

        // definizione del bottone delle mie attività
        myToursButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myToursButton.setBackgroundResource(R.drawable.navbar_selected_button);
                searchButton.setBackgroundResource(R.drawable.navbar_button);
                myAccountButton.setBackgroundResource(R.drawable.navbar_button);
                settingsButton.setBackgroundResource(R.drawable.navbar_button);
                notificationButton.setBackgroundResource(R.drawable.navbar_button);

                fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = MyToursFragment.newInstance(db);
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
            }
        });

        // definizione del bottone della ricerca di attività
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myToursButton.setBackgroundResource(R.drawable.navbar_button);
                searchButton.setBackgroundResource(R.drawable.navbar_selected_button);
                myAccountButton.setBackgroundResource(R.drawable.navbar_button);
                settingsButton.setBackgroundResource(R.drawable.navbar_button);
                notificationButton.setBackgroundResource(R.drawable.navbar_button);

                fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = SearchFragment.newInstance(db);
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
            }
        });

        // definizione del bottone del mipo account
        myAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myToursButton.setBackgroundResource(R.drawable.navbar_button);
                searchButton.setBackgroundResource(R.drawable.navbar_button);
                myAccountButton.setBackgroundResource(R.drawable.navbar_selected_button);
                settingsButton.setBackgroundResource(R.drawable.navbar_button);
                notificationButton.setBackgroundResource(R.drawable.navbar_button);

                fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = MyAccountFragment.newInstance(db, auth);
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
            }
        });

        // definizione del bottone dei settaggi dell'applicazione
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myToursButton.setBackgroundResource(R.drawable.navbar_button);
                searchButton.setBackgroundResource(R.drawable.navbar_button);
                myAccountButton.setBackgroundResource(R.drawable.navbar_button);
                settingsButton.setBackgroundResource(R.drawable.navbar_selected_button);
                notificationButton.setBackgroundResource(R.drawable.navbar_button);

                fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = SettingsFragment.newInstance(db, auth);
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
            }
        });

        // definizione del bottone delle notifiche arrivate
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myToursButton.setBackgroundResource(R.drawable.navbar_button);
                searchButton.setBackgroundResource(R.drawable.navbar_button);
                myAccountButton.setBackgroundResource(R.drawable.navbar_button);
                settingsButton.setBackgroundResource(R.drawable.navbar_button);
                notificationButton.setBackgroundResource(R.drawable.navbar_selected_button);

                fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = NotificationsFragment.newInstance(db);
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
            }
        });
    }


    @Override
    public void onResume(){

        super.onResume();
    }


    // METODI PER L'ACQUISIZIONE DEI DATI DI GESTIONE
    public SessionManager getSessionManager(){

        return session;
    }

    public DBManager getDb() {

        return db;
    }

    public AuthController getAuthController() {

        return auth;
    }

}
