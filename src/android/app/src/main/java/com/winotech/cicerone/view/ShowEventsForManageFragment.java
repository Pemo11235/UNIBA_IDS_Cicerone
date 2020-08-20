package com.winotech.cicerone.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import android.app.FragmentTransaction;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Tour;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class ShowEventsForManageFragment extends Fragment {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller delle attività
    private static transient TourController tourController;

    // componenti grafiche
    private ListView listViewEvents;
    private Button newEventButton;
    private TextView messageTextView;

    // dati inerenti all'evento
    private ArrayList<Event> events;

    // id del tour di appartenenza
    private Tour tour;

    // settaggio della prima lettura del fragment.
    // E' utile per ricaricare i dati quando si aggiunge un nuovo evento
    private boolean firstLoad = true;


    /*
    Singleton del fragment
     */
    public static ShowEventsForManageFragment newInstance(DBManager db, ArrayList<Event> events, Tour tour) {

        ShowEventsForManageFragment fragment = new ShowEventsForManageFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.KEY_TOUR_EVENTS, events);
        bundle.putSerializable(Constants.KEY_TOUR_ID, tour);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_show_events_for_manage, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione dei parametri passati
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        events = new ArrayList<>((ArrayList<Event>) getArguments().getSerializable(Constants.KEY_TOUR_EVENTS));
        tour = (Tour) getArguments().getSerializable(Constants.KEY_TOUR_ID);

        // inizializzazione del controller
        if(tourController == null)
           TourController.initInstance(getActivity());
        tourController = TourController.getInstance();

        // definizione degli oggetti grafici
        listViewEvents = view.findViewById(R.id.mainView);
        newEventButton = view.findViewById(R.id.new_event_button);
        messageTextView = view.findViewById(R.id.message_textView);

        // definizione della visualizzazione dell'oggetto grafico
        if(events.isEmpty()) {

            listViewEvents.setVisibility(View.GONE);
            messageTextView.setVisibility(View.VISIBLE);

        } else {

            listViewEvents.setVisibility(View.VISIBLE);
            messageTextView.setVisibility(View.GONE);
        }

        if(listViewEvents.getVisibility() == View.VISIBLE) {

            // definizione di un nuovo adapter
            SelectEventListViewAdapter adapter = new SelectEventListViewAdapter(events);

            // settaggio dell'adapter sulla lista di lingue
            listViewEvents.setAdapter(adapter);

            // definizione del click di un evento nella lista
            listViewEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                    // acquisizione delle info sull'evento
                    Event event = (Event)adapterView.getItemAtPosition(position);

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragment = ShowEventCiceroneFragment.newInstance(db, event, tour);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment, fragment).commit();
                }
            });
        }

        // definizione del listener del bottone di aggiunta di un evento
        newEventButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragment = NewEventFragment.newInstance(db, tour);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment, fragment).commit();
            }
        });

        return view;
    }


    @Override
    public void onResume() {

        super.onResume();

        // se è il primo accesso al fragment (ovvero è stato
        // appena creato) allora setta il flag a falso
        if(firstLoad) {

            firstLoad = false;

        }

        // altrimenti ricarica i dati dal server per aggiornarli
        else {

            events = (ArrayList<Event>) tourController.getTourFromID(tour.getId()).get("events");

            // definizione di un nuovo adapter
            SelectEventListViewAdapter adapter = new SelectEventListViewAdapter(events);

            // settaggio dell'adapter sulla lista di lingue
            listViewEvents.setAdapter(adapter);
        }
    }


    /*
  Classe per la creazione di un adattatore per la listview
   */
    public class SelectEventListViewAdapter extends ArrayAdapter {

        Context context;
        ArrayList<Event> events;

        @SuppressWarnings("unchecked")
        public SelectEventListViewAdapter(ArrayList<Event> resource) {

            super(view.getContext(), R.layout.checkbox_listview_row, resource);

            this.context = view.getContext();
            this.events = resource;
        }

        @Override
        public View getView(final int position, View adapterView, ViewGroup parent) {

            // definizione del layout della linea
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            adapterView = inflater.inflate(R.layout.listview_row, parent, false);

            // inizializzazione del nome e della descrizione da mostrare nella linea
            final TextView date = (TextView) adapterView.findViewById(R.id.textViewMyTourListName);
            final TextView status = (TextView) adapterView.findViewById(R.id.textViewMyTourListOther);

            // acquisizione dei nomi dei mesi
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] months = dfs.getMonths();

            // definizione della data di effettuazione dell'evento
            // se la data è si un solo giorno allora mostra solo la data di inizio
            // dato che inizio e fine sono uguali
            String dateText = "";
            if(events.get(position).getStartDate().equals(events.get(position).getEndDate())) {

                GregorianCalendar dateInCalendar = new GregorianCalendar();
                dateInCalendar.setTime(events.get(position).getStartDate().getTime());
                dateText = dateInCalendar.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                        months[dateInCalendar.get(GregorianCalendar.MONTH)] + " " +
                        dateInCalendar.get(GregorianCalendar.YEAR);

            } else {

                GregorianCalendar dateInCalendar = new GregorianCalendar();
                dateInCalendar.setTime(events.get(position).getStartDate().getTime());
                dateText = dateInCalendar.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                        months[dateInCalendar.get(GregorianCalendar.MONTH)] + " " +
                        dateInCalendar.get(GregorianCalendar.YEAR);
                dateInCalendar.setTime(events.get(position).getEndDate().getTime());
                dateText += " - " + dateInCalendar.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                        months[dateInCalendar.get(GregorianCalendar.MONTH)] + " " +
                        dateInCalendar.get(GregorianCalendar.YEAR);
            }

            // settaggio della data dell'evento
            date.setText(dateText);

            // stringa da inserire per lo status
            String statusText = "";

            // settaggio della data massima di richiesta dell'evento
            Calendar maxDate = new GregorianCalendar();
            maxDate.setTime(events.get(position).getStartDate().getTime());
            maxDate.add(Calendar.DAY_OF_MONTH, -3);

            // settaggio della data odierna
            Calendar today = Calendar.getInstance();

            // se non si è superato il termine massimo di scadenza dell'evento
            if(today.before(maxDate)) {

                // definizione della stringa dello status
                int eventRequestNumber = events.get(position).getEventRequests().size();

                if (eventRequestNumber > 0)
                    statusText = getResources().getString(R.string.pending_requests) + ": " + eventRequestNumber;
                else
                    statusText = getResources().getString(R.string.no_pending_requests);
            }

            // se invece si è superato il termine massimo di scadenza dell'evento
            else {

                statusText = getResources().getString(R.string.expired_request_terms);
            }

            // settaggio dello stato dell'evento
            status.setText(statusText);

            return adapterView;
        }
    }
}
