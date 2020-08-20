package com.winotech.cicerone.view;

import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Subscribed;
import com.winotech.cicerone.model.Tour;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;


public class DeleteEventFragment extends Fragment {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller delle attività
    private static transient TourController tourController;

    // puntatore all'evento
    private Event event;

    // oggetto grafico
    private Button actionButton;



    /*
    Singleton del fragment
     */
    public static DeleteEventFragment newInstance(DBManager db, Event event) {

        DeleteEventFragment fragment = new DeleteEventFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.KEY_EVENT_DATA, event);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_delete_event, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione dei parametri passati
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        event = (Event) getArguments().getSerializable(Constants.KEY_EVENT_DATA);

        // inizializzazione del controller
        if(tourController == null)
            TourController.initInstance(getActivity());
        tourController = TourController.getInstance();

        // inizializzazione del bottone
        actionButton = view.findViewById(R.id.action_button);

        // definizione del listener del click del bottone
        actionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // flag per trovare almeno un globetrotter che ha pagato
                boolean atLeastASubscribed = false;

                // definizione del valore del flag
                for(Subscribed sub : event.getEventSubscribeds()) {

                    if(sub.getHasPayed()){

                        atLeastASubscribed = true;
                        break;
                    }
                }

                // se l'evento è a pagamento e almeno una persona ha pagato allora mostra un messaggio di errore
                if(event.getTour().getCost() > 0 && atLeastASubscribed) {

                    Toast.makeText(view.getContext(), getResources().getString(R.string.there_is_a_buyer_in_event),
                            Toast.LENGTH_LONG).show();

                } else {

                    // tenta la cancellazione dell'evento e se va a buon fine cambia fragment
                    boolean success = tourController.deleteEvent(event.getId());

                    // se l'esecuzione va a buon fine allora torna alla pagina precedente
                    if (success) {

                        // puntatore al generalController
                        GeneralController generalController = GeneralController.getInstance();

                        // acquisizione degli eventi del tour
                        final HashMap<String, Integer> subscribeds = new HashMap<>();

                        for (Subscribed subscribed : event.getEventSubscribeds())
                           subscribeds.put(subscribed.getGlobetrotter().getUsername(), event.getId());

                        // invio delle notifiche
                        for(Map.Entry<String, Integer> sub : subscribeds.entrySet()) {

                            generalController.saveNotification(
                                    db.getMyAccount().getUsername(),
                                    sub.getKey(),
                                    6,
                                    sub.getValue(),
                                    "NULL");
                        }


                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        Fragment fragment = ShowTourCiceroneFragment.newInstance(db, event.getTour().getId());
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.fragment, fragment).commit();
                    }
                }
            }
        });





        return view;
    }


}
