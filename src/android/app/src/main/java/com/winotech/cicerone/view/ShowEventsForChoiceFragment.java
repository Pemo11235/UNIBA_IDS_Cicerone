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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Request;
import com.winotech.cicerone.model.Subscribed;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

@SuppressWarnings("unchecked")
public class ShowEventsForChoiceFragment extends Fragment implements Serializable {

    // componenti del contesto del Fragment
    private View view;
    private ViewGroup localContainer;

    // puntatore al db locale
    private DBManager db;

    // componenti grafiche
    private ListView listViewEvents;

    // dati inerenti all'evento
    private ArrayList<Event> eventData = new ArrayList<>();



    /*
    Singleton del fragment
    */
    public static ShowEventsForChoiceFragment newInstance(DBManager db, ArrayList<Event> eventData) {

        ShowEventsForChoiceFragment fragment = new ShowEventsForChoiceFragment();

        if(db != null) {

            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.DB_KEY, db);
            bundle.putSerializable(Constants.KEY_EVENT_DATA, eventData);
            fragment.setArguments(bundle);
        }

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_show_events_for_choice, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione del db
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        eventData = (ArrayList<Event>) getArguments().getSerializable(Constants.KEY_EVENT_DATA);

        // definizione degli oggetti grafici
        listViewEvents = view.findViewById(R.id.mainView);

        // definizione di un nuovo adapter
        SelectEventListViewAdapter adapter = new SelectEventListViewAdapter(eventData);

        // settaggio dell'adapter sulla lista di lingue
        listViewEvents.setAdapter(adapter);

        // settaggio del click su un elemento della lista
        listViewEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // acquisizione delle info sull'evento
                Event event = (Event)adapterView.getItemAtPosition(position);

                // settaggio della data massima di richiesta dell'evento
                Calendar maxDate = new GregorianCalendar();
                maxDate.setTime(event.getStartDate().getTime());
                maxDate.add(Calendar.DAY_OF_MONTH, -3);

                // settaggio della data odierna
                Calendar today = Calendar.getInstance();

                // se non si è superato il termine massimo di scadenza dell'evento
                if(today.before(maxDate)) {

                    // acqusiizione del numero di persone partecipanti all'evento
                    int peopleAlreadyInEvent = 0;
                    for (Subscribed sub : event.getEventSubscribeds())
                        peopleAlreadyInEvent += sub.getGroupDimension();

                    // settaggio se si è gia iscritti all'evento
                    boolean alreadySubscribed = false;
                    ArrayList<Subscribed> subscribeds = new ArrayList<>(event.getEventSubscribeds());
                    for (Subscribed sub : subscribeds) {

                        if (sub.getEvent().getId() == event.getId() &&
                                sub.getGlobetrotter().getUsername().equals(db.getMyAccount().getUsername())) {

                            alreadySubscribed = true;
                            break;
                        }
                    }
                    subscribeds.clear();

                    // settaggio se si ha gia inoltrato una richiesta all'evento
                    boolean alreadyRequested = false;
                    boolean deniedRequest = false;
                    ArrayList<Request> requests = new ArrayList<>(event.getEventRequests());
                    for (Request req : requests) {

                        if (req.getEvent().getId() == event.getId() &&
                                req.getGlobetrotter().getUsername().equals(db.getMyAccount().getUsername())) {

                            alreadyRequested = true;
                            if (req.getAccepted() != null && !req.getAccepted())
                                deniedRequest = true;
                            break;
                        }
                    }
                    requests.clear();


                    // se tutto va per il meglio effettua il cambio fragment
                    if (event.getMaxSubscribeds() > peopleAlreadyInEvent &&
                            !alreadyRequested && !alreadySubscribed) {

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        Fragment fragmentView = ShowSelectedEventFragment.newInstance(db, event);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.fragment, fragmentView).commit();

                    }

                    else if (event.getMaxSubscribeds() <= peopleAlreadyInEvent){

                        // messaggio di errore
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.not_available_event_description),
                                Toast.LENGTH_LONG).show();

                    }

                    else if (alreadySubscribed) {

                        // messaggio di errore
                        Toast.makeText (getActivity(),
                                getResources().getString(R.string.already_subscribed_event_description),
                                Toast.LENGTH_LONG).show();

                    }

                    // se si è gia effettuati una richiesta
                    else if (alreadyRequested) {

                        // se è stata rifiutata allora mostra l'avviso
                        if(deniedRequest){

                            // messaggio di errore
                            Toast.makeText (getActivity(),
                                    getResources().getString(R.string.already_requested_denied_event_description),
                                    Toast.LENGTH_LONG).show();

                        } else {

                            // messaggio di errore
                            Toast.makeText (getActivity(),
                                    getResources().getString(R.string.already_requested_event_description),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                } else {

                    // messaggio di errore
                    Toast.makeText (getActivity(),
                            getResources().getString(R.string.expired_event_description),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
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

            // settaggio del nome dell'attività
            date.setText(dateText);

            // settaggio della data massima di richiesta dell'evento
            Calendar maxDate = new GregorianCalendar();
            maxDate.setTime(events.get(position).getStartDate().getTime());
            maxDate.add(Calendar.DAY_OF_MONTH, -3);

            // settaggio della data odierna
            Calendar today = Calendar.getInstance();

            // settaggio del numero di gente gia iscritta
            int peopleAlreadyInEvent = 0;
            for (Subscribed sub : events.get(position).getEventSubscribeds())
                peopleAlreadyInEvent += sub.getGroupDimension();

            // settaggio se si è gia iscritti all'evento
            boolean alreadySubscribed = false;
            ArrayList<Subscribed> subscribeds = new ArrayList<>(events.get(position).getEventSubscribeds());
            if(!subscribeds.isEmpty()) {

                for (Subscribed sub : subscribeds) {

                    if (sub.getEvent().getId() == events.get(position).getId()) {

                        alreadySubscribed = true;
                        break;
                    }
                }
                subscribeds.clear();
            }

            // settaggio se si ha gia inoltrato una richiesta all'evento
            boolean alreadyRequested = false;
            ArrayList<Request> requests = new ArrayList<>(events.get(position).getEventRequests());
            if(!requests.isEmpty()){

                for (Request req : requests) {

                    if (req.getEvent().getId() == events.get(position).getId()) {

                        alreadyRequested = true;
                        break;
                    }
                }
                requests.clear();
            }

            // evento non sottoscrivibile perche scaduto il tempo massimo di iscrizione
            if (!today.before(maxDate)){

                status.setText(R.string.expired_event);

            } else if (events.get(position).getMaxSubscribeds() <= peopleAlreadyInEvent) {

                status.setText(R.string.not_available_event);

            } else if (alreadySubscribed) {

                status.setText(R.string.already_subscribed_event);

            } else if (alreadyRequested) {

                status.setText(R.string.already_requested_event);

            } else {

                int subsRemaining = events.get(position).getMaxSubscribeds() - peopleAlreadyInEvent;
                String text = subsRemaining + " " + getResources().getString(R.string.subs_remaining_event);
                status.setText(text);
            }

            return adapterView;
        }
    }
}
