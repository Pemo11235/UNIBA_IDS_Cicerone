package com.winotech.cicerone.view;

import android.app.Activity;
import android.app.FragmentTransaction;
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

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Cicerone;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Globetrotter;
import com.winotech.cicerone.model.Tour;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class MyToursFragment extends Fragment implements Serializable {

    // componenti del contesto del Fragment
    private View view;
    private ViewGroup localContainer;

    // puntatore al db locale
    private DBManager db;

    // puntatore al controller delle attività
    private static transient TourController tourController;

    // adattatore per la lista delle attività
    private ArrayAdapter adapter;

    // componenti grafiche
    private ListView listViewTours;
    private TextView textViewNoTour;
    private Button newTourButton;

    // lista dei tour da mostrare
    private ArrayList<Tour> showTours;
    private ArrayList<Event> showEvents;

    // settaggio della prima lettura del fragment.
    // E' utile per ricaricare i dati quando si aggiunge un nuovo tour
    private boolean firstLoad = true;


    /*
    Singleton del fragment
     */
    public static MyToursFragment newInstance(DBManager db) {

        MyToursFragment fragment = new MyToursFragment();

        if(db != null) {

            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.DB_KEY, db);
            fragment.setArguments(bundle);
        }

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflating del layout del fragment
        view = inflater.inflate(R.layout.fragment_my_tours, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione del db
        if(getArguments() != null)
            db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        else
            db = new DBManager(view.getContext());

        // inizializzazione del controller
        if(tourController == null)
            TourController.initInstance(getActivity());
        tourController = TourController.getInstance();

        // definizione degli oggetti grafici
        listViewTours = view.findViewById(R.id.mainView);
        textViewNoTour = view.findViewById(R.id.textViewNoTour);
        newTourButton = view.findViewById(R.id.button3);

        // acquisizione delle proprie attività
        if(db != null) {

            if(db.getMyAccount() instanceof Cicerone) {

                //if (tourController.getLoadedTours().isEmpty())
                    showTours = tourController.getTours(db.getMyAccount().getUsername(), db.getMyAccount() instanceof Cicerone);
                //else
                //    showTours = tourController.getLoadedTours();

            } else {

                //if (tourController.getLoadedEvents().isEmpty())
                    showEvents = tourController.getEventsSubscribed(db.getMyAccount().getUsername());
                //else
                //    showEvents = tourController.getLoadedEvents();
            }


            listViewTours.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                    if(db.getMyAccount() instanceof Cicerone) {
						
                        Tour t =(Tour) adapterView.getItemAtPosition(position);
                        int id = t.getId();
						
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        Fragment fragmentView = ShowTourCiceroneFragment.newInstance(db, id);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
						
                    } else {
						
                        Event event =(Event) adapterView.getItemAtPosition(position);
                        int id = event.getId();
						
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        Fragment fragmentView = ShowEventGlobetrotterFragment.newInstance(db, id);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.fragment, fragmentView).commit();

                    }
                }
            });
        }

        if((showEvents != null && !showEvents.isEmpty()) ||
                (showTours != null && !showTours.isEmpty()))
            textViewNoTour.setVisibility(View.GONE);
        else
            textViewNoTour.setVisibility(View.VISIBLE);

        // definizione di un nuovo adapter
        if(db != null) {

            if (db.getMyAccount() instanceof Cicerone)
                adapter = new TourListViewAdapter(showTours);

            else {

                adapter = new EventListViewAdapter(showEvents);
                newTourButton.setVisibility(View.GONE);
            }

            // settaggio dell'adapter sulla lista di lingue
            listViewTours.setAdapter(adapter);
        }


        // definiizone del listener del click del bottone di nuovo tour
        newTourButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = NewTourFragment.newInstance(null);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
            }
        });

        return view;
    }


    @Override
    public void onResume() {

        super.onResume();

        if(firstLoad)
            firstLoad = false;

        else {

            if (db.getMyAccount() instanceof Cicerone) {

                //if (tourController.getLoadedTours().isEmpty())
                showTours = tourController.getTours(db.getMyAccount().getUsername(), db.getMyAccount() instanceof Cicerone);
                //else
                //    showTours = tourController.getLoadedTours();

            } else {

                //if (tourController.getLoadedEvents().isEmpty())
                showEvents = tourController.getEventsSubscribed(db.getMyAccount().getUsername());
                //else
                //    showEvents = tourController.getLoadedEvents();
            }

            listViewTours.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                    if (db.getMyAccount() instanceof Cicerone) {

                        Tour t = (Tour) adapterView.getItemAtPosition(position);
                        int id = t.getId();

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        Fragment fragmentView = ShowTourCiceroneFragment.newInstance(db, id);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.fragment, fragmentView).commit();

                    } else {

                        Event event = (Event) adapterView.getItemAtPosition(position);
                        int id = event.getId();

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        Fragment fragmentView = ShowEventGlobetrotterFragment.newInstance(db, id);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.fragment, fragmentView).commit();

                    }
                }
            });

            if ((showEvents != null && !showEvents.isEmpty()) ||
                    (showTours != null && !showTours.isEmpty()))
                textViewNoTour.setVisibility(View.GONE);
            else
                textViewNoTour.setVisibility(View.VISIBLE);

            // definizione di un nuovo adapter
            if (db.getMyAccount() instanceof Cicerone)
                adapter = new TourListViewAdapter(showTours);

            else {

                adapter = new EventListViewAdapter(showEvents);
                newTourButton.setVisibility(View.GONE);
            }
            // settaggio dell'adapter sulla lista di lingue
            listViewTours.setAdapter(adapter);
        }
    }


    /*
    Classe per la creazione di un adattatore per la listview
     */
    public class TourListViewAdapter extends ArrayAdapter {

        Context context;
        ArrayList<Tour> items;

        @SuppressWarnings("unchecked")
        public TourListViewAdapter(ArrayList<Tour> resource) {

            super(view.getContext(), R.layout.checkbox_listview_row, resource);

            this.context = view.getContext();
            this.items = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // definizione del layout della linea
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.listview_row, parent, false);

            // inizializzazione del nome e della descrizione da mostrare nella linea
            final TextView name = (TextView) convertView.findViewById(R.id.textViewMyTourListName);
            final TextView status = (TextView) convertView.findViewById(R.id.textViewMyTourListOther);

            // settaggio del nome dell'attività
            name.setText(items.get(position).getName());

            // se non ci sono oggetti associati allora stampa
            if(items.get(position).getDescription().equals(getResources().getString(R.string.start_registering_tour)) ||
                    items.get(position).getDescription().equals(getResources().getString(R.string.start_inserting_tour))) {


                status.setText(items.get(position).getDescription());

            } else {


                if(db.getMyAccount() instanceof Cicerone) {

                    // settaggio dello stato dell'attività nel tempo
                    Calendar tourEndDate = Calendar.getInstance();

                    Date c = items.get(position).getEndDate().getTime();

                    if (items.get(position).getEndDate().after(Calendar.getInstance()))
                        status.setText(R.string.valid_tours);
                    else if (items.get(position).getEndDate().before(Calendar.getInstance()))
                        status.setText(R.string.past_tours);

                } else if(db.getMyAccount() instanceof Globetrotter) {

                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    String cost = df.format(items.get(position).getCost());

                    String costStatus = getResources().getText(R.string.tour_cost) + ": " + cost + "€";

                    status.setText(costStatus);
                }
            }

            return convertView;
        }
    }


    public class EventListViewAdapter extends ArrayAdapter {

        Context context;
        ArrayList<Event> items;

        @SuppressWarnings("unchecked")
        public EventListViewAdapter(ArrayList<Event> resource) {

            super(view.getContext(), R.layout.checkbox_listview_row, resource);

            this.context = view.getContext();
            this.items = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // definizione del layout della linea
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.listview_row, parent, false);

            // inizializzazione del nome e della descrizione da mostrare nella linea
            final TextView name = (TextView) convertView.findViewById(R.id.textViewMyTourListName);
            final TextView status = (TextView) convertView.findViewById(R.id.textViewMyTourListOther);

            // settaggio del nome dell'attività
            name.setText(items.get(position).getTour().getName());

            // se non ci sono oggetti associati allora stampa
            if(items.get(position).getDescription().equals(getResources().getString(R.string.start_registering_tour)) ||
                    items.get(position).getDescription().equals(getResources().getString(R.string.start_inserting_tour))) {


                status.setText(items.get(position).getDescription());

            } else {

                if(db.getMyAccount() instanceof Globetrotter) {

                    DateFormatSymbols dfs_event = new DateFormatSymbols();
                    String[] months_event = dfs_event.getMonths();
					
                    GregorianCalendar date_event = new GregorianCalendar();
                    String all_event_date = "";

                    if(items.get(position).getStartDate().equals(items.get(position).getEndDate())) {

                        date_event.setTime( items.get(position).getStartDate().getTime());
                        all_event_date = date_event.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                                months_event[date_event.get(GregorianCalendar.MONTH)] + " " +
                                date_event.get(GregorianCalendar.YEAR);
                        status.setText(all_event_date);

                    } else {

                        date_event.setTime( items.get(position).getStartDate().getTime());
                        all_event_date = date_event.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                                months_event[date_event.get(GregorianCalendar.MONTH)] + " " +
                                date_event.get(GregorianCalendar.YEAR);
                        date_event.setTime(items.get(position).getEndDate().getTime());
                        all_event_date += " - " + date_event.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                                months_event[date_event.get(GregorianCalendar.MONTH)] + " " +
                                date_event.get(GregorianCalendar.YEAR);
                        status.setText(all_event_date);
                    }
                }
            }

            return convertView;
        }
    }
}
