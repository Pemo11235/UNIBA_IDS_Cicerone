package com.winotech.cicerone.view;

import android.app.FragmentTransaction;
import android.os.Bundle;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Language;
import com.winotech.cicerone.model.Location;
import com.winotech.cicerone.model.Subscribed;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.GregorianCalendar;


public class ShowSelectedEventFragment extends Fragment implements Serializable {

    // componenti del contesto del Fragment
    private View view;
    private ViewGroup localContainer;

    // puntatore al db locale
    private DBManager db;

    // dati inerenti all'evento
    private Event event;

    // puntatore al controller generale
    private static transient GeneralController generalController;

    // oggetti grafici
    private TextView dateTextView;
    private TextView languagesTextView;
    private TextView tourDescriptionTextView;
    private TextView eventDescriptionTextView;
    private TextView locationsTextView;
    private TextView costTextView;
    private EditText groupDimensionEditText;
    private Button sendButton;


    /*
    Singleton del fragment
    */
    public static ShowSelectedEventFragment newInstance(DBManager db, Event event) {

        ShowSelectedEventFragment fragment = new ShowSelectedEventFragment();

        if(db != null) {

            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.DB_KEY, db);
            bundle.putSerializable(Constants.KEY_EVENT_DATA, event);
            fragment.setArguments(bundle);
        }

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_show_selected_event, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione del db
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        event = (Event) getArguments().getSerializable(Constants.KEY_EVENT_DATA);

        // inizializzazione del controller generico
        if(generalController == null)
            GeneralController.initInstance(getActivity());
        generalController = GeneralController.getInstance();

        // inizializzazione degli oggetti grafici
        dateTextView = view.findViewById(R.id.date_textView);
        languagesTextView = view.findViewById(R.id.languages_textView);
        tourDescriptionTextView = view.findViewById(R.id.tour_description_textView);
        eventDescriptionTextView = view.findViewById(R.id.event_description_textView);
        locationsTextView = view.findViewById(R.id.locations_textView);
        costTextView = view.findViewById(R.id.cost_textView);
        groupDimensionEditText = view.findViewById(R.id.group_dimension_editText);
        sendButton = view.findViewById(R.id.send_request_button);

        // acquisizione dei nomi dei mesi
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();

        // oggetto per la definizione delle date
        GregorianCalendar date = new GregorianCalendar();

        // stringa per la stampa della data
        String dateText;

        // se la data è inerente ad un solo giorno allora
        // mostra solo una data, altrimenti mostrane due
        if(event.getStartDate().equals(event.getEndDate())) {

            date.setTime(event.getStartDate().getTime());
            dateText = date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);

            dateTextView.setText(dateText);

        } else {

            date.setTime(event.getStartDate().getTime());
            dateText = date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR) + " - ";
            date.setTime(event.getEndDate().getTime());
            dateText += date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);

            dateTextView.setText(dateText);
        }

        // lista di lingue acqusiite dall'evento
        ArrayList<Language> languages = event.getEventLanguages();

        // stringa per la lista di lingue
        String languageText = "";

        // definizione e stampa della stringa delle lingue
        for(Language language : languages)
            languageText += language.getName() + "\n";
        languagesTextView.setText(languageText);

        // definizione delle descrizioni
        tourDescriptionTextView.setText(event.getTour().getDescription());
        eventDescriptionTextView.setText(event.getDescription());

        // lista di tappe acquisite dall'evento
        ArrayList<Location> locations = event.getTour().getTourLocations();

        // string aper la lista di tappe
        String locationText = "";

        // definizione e stampa della lista delle tappe
        for(Location location : locations)
            locationText += generalController.getLocationFromCoords(
                    location.getLatitude(), location.getLongitude()) + "\n";
        locationsTextView.setText(locationText);

        // definizione e stampa del costo dell'attività
        String costText = Float.toString(event.getTour().getCost());
        costTextView.setText(costText);

        // definizione del listener per il click del bottone
        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(!groupDimensionEditText.getText().toString().equals("") ||
                        !groupDimensionEditText.getText().toString().contains(" ")){

                    // acquisizione della dimensione del gruppo inserita
                    int groupDimension = Integer.parseInt(groupDimensionEditText.getText().toString());

                    // calcolo del numero dei globetrotter gia iscritti
                    int alreadySubscribed = 0;
                    ArrayList<Subscribed> subs = event.getEventSubscribeds();
                    for (Subscribed sub : subs)
                        alreadySubscribed += sub.getGroupDimension();

                    // se la dimensione del gruppo è negativa
                    if(groupDimension <= 0) {

                        // messaggio di errore
                        Toast.makeText (getActivity(), R.string.negative_group_dimension,
                                Toast.LENGTH_LONG).show();
                    }

                    // se la dimensione del gruppo è maggiore delle iscrizioni massime
                    else if (groupDimension > event.getMaxSubscribeds()){

                        // messaggio di errore
                        Toast.makeText (getActivity(), R.string.group_dimension_excedeed_max_subscribed,
                                Toast.LENGTH_LONG).show();
                    }

                    // se la dimensione del gruppo, sommata al numero di globetrotter
                    // gia presenti, è maggiore delle iscrizioni massime
                    else if (event.getMaxSubscribeds() < alreadySubscribed + groupDimension) {

                        // messaggio di errore
                        Toast.makeText (getActivity(), R.string.group_dimension_sum_excedeed_max_subscribed,
                                Toast.LENGTH_LONG).show();
                    }

                    // se è tutto ok
                    else {

                        // salva la richiesta
                        if(generalController.saveRequest(
                                db.getMyAccount().getUsername(),
                                event.getTour().getCicerone().getUsername(),
                                event.getId(),
                                groupDimension)){

                            // messaggio di errore
                            Toast.makeText (getActivity(), R.string.request_send_successfully_description,
                                    Toast.LENGTH_LONG).show();

                            getFragmentManager().popBackStack();
                            getFragmentManager().popBackStack();
                            getFragmentManager().popBackStack();
                            getFragmentManager().popBackStack();

                        } else {

                            // messaggio di errore
                            Toast.makeText (getActivity(), R.string.oops_request,
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                } else {

                    // messaggio di errore
                    Toast.makeText (getActivity(), R.string.empty_fields,
                            Toast.LENGTH_LONG).show();
                }



            }
        });

        return view;
    }


}
