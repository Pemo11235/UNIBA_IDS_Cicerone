package com.winotech.cicerone.view;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Subscribed;


public class DeleteSubscribedGlobetrotterFragment extends Fragment {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller delle attività
    private static transient GeneralController generalController;

    // puntatore all'iscrizione
    private Subscribed subscription;

    // oggetti grafici
    private Button actionButton;


    /*
    Singleton della classe
     */
    public static DeleteSubscribedGlobetrotterFragment newInstance(DBManager db, Subscribed subscribed) {

        DeleteSubscribedGlobetrotterFragment fragment = new DeleteSubscribedGlobetrotterFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.KEY_SUBSCRIPTION_DATA, subscribed);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_delete_subscribed_globetrotter, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione del db
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        subscription = (Subscribed) getArguments().getSerializable(Constants.KEY_SUBSCRIPTION_DATA);

        // inizializzazione del controller
        if(generalController == null)
            GeneralController.initInstance(getActivity());
        generalController = GeneralController.getInstance();

        // inizializzazione delgi oggetti grafici
        actionButton = view.findViewById(R.id.action_button);

        // impostazione del listener sul click del bottone
        actionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // cancellazione dell'iscriizone
                boolean success = generalController.deleteSubscribed(
                            subscription.getGlobetrotter().getUsername(),
                            subscription.getEvent().getId());

                    // se tutto va bene cambia fragment
                    if(success) {

                        // inserisci una nuova notifica
                        generalController.saveNotification(
                                subscription.getGlobetrotter().getUsername(),
                                db.getMyAccount().getUsername(),
                                5,
                                subscription.getEvent().getId(),
                                "NULL"
                        );

                        // cambio del fragment
                        getFragmentManager().popBackStack();
                        getFragmentManager().popBackStack();
                    }

            }
        });


        return view;
    }


}
