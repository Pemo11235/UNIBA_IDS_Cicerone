package com.winotech.cicerone.view;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import android.app.FragmentTransaction;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Subscribed;
import com.winotech.cicerone.model.Tour;

import java.util.HashMap;


public class DeleteTourFragment extends Fragment {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller delle attività
    private static transient TourController tourController;

    private Button actionButton;

    /*
    Singleton del fragment
     */
    public static DeleteTourFragment newInstance(DBManager db, int tour) {

        DeleteTourFragment fragment = new DeleteTourFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.KEY_TOUR_ID, tour);
        fragment.setArguments(bundle);

        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_delete_tour, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione dei parametri passati
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        final int tourID = (int) getArguments().getSerializable(Constants.KEY_TOUR_ID);

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

                HashMap<String, Object> tourAssociatedData = tourController.getTourFromID(tourID);
                Tour tour = ((Tour)tourAssociatedData.get("tour"));
                boolean atLeastASubscribed = false;

                for(Event event : tour.getTourEvents()){

                    for(Subscribed sub : event.getEventSubscribeds()){

                        if(sub.getHasPayed()){

                            atLeastASubscribed = true;
                            break;
                        }
                    }
                }

                if(tour.getCost() > 0 && atLeastASubscribed) {

                    // messaggio di corretto salvataggio dei dati
                    Toast.makeText(view.getContext(), getResources().getString(R.string.there_is_a_buyer_in_tour),
                            Toast.LENGTH_LONG).show();

                } else {

                    // tenta il salvataggio del tour e se va a buon fine cambia fragment
                    boolean success = tourController.deleteTour(tourID);

                    // se l'esecuzione va a buon fine allora torna alla pagina precedente
                    if (success) {

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        Fragment fragment = MyToursFragment.newInstance(db);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.fragment, fragment).commit();
                    }
                }
            }
        });

        return view;
    }


}
