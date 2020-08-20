package com.winotech.cicerone.view;

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


public class DeleteSubscribedCiceroneFragment extends Fragment {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller generico
    private static transient GeneralController generalController;

    // puntatore alla iscrizione all'evento
    private Subscribed subscription;

    // stringa per la motivazione dela cancellazione dell'iscrizione
    private String reason;

    // oggetti grafici
    private EditText reasonEditText;
    private Button actionButton;


    /*
    Singleton del fragment
     */
    public static DeleteSubscribedCiceroneFragment newInstance(DBManager db, Subscribed subscription) {

        DeleteSubscribedCiceroneFragment fragment = new DeleteSubscribedCiceroneFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.KEY_SUBSCRIPTION_DATA, subscription);
        fragment.setArguments(bundle);


        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_delete_subscribed_cicerone, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione dei parametri passati
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        subscription = (Subscribed) getArguments().getSerializable(Constants.KEY_SUBSCRIPTION_DATA);

        // inizializzazione del controller
        if(generalController == null)
            GeneralController.initInstance(getActivity());
        generalController = GeneralController.getInstance();

        // inizializzazione del bottone
        reasonEditText = view.findViewById(R.id.reason_editText);
        actionButton = view.findViewById(R.id.action_button);

        // impostazione del listener sul click del bottone
        actionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String reasonOnString = reasonEditText.getText().toString();

                // se non si inserisce una motivazione si notifica di un errore
                if(reasonOnString.equals("") || reasonOnString.equals(" ") || reasonOnString.startsWith("  ")) {

                    Toast.makeText(view.getContext(), getResources().getString(R.string.must_insert_a_reason),
                            Toast.LENGTH_LONG).show();
                }

                // altrimenti esegui la cancellazione
                else {

                    // cancellazione dell'iscriizone
                    boolean success = generalController.deleteSubscribed(
                            subscription.getGlobetrotter().getUsername(),
                            subscription.getEvent().getId());

                    // se tutto va bene cambia fragment
                    if(success) {

                        // inserisci una nuova notifica
                        generalController.saveNotification(
                                db.getMyAccount().getUsername(),
                                subscription.getGlobetrotter().getUsername(),
                                4,
                                subscription.getEvent().getId(),
                                reasonOnString
                        );

                        // cambio del fragment
                        getFragmentManager().popBackStack();
                        getFragmentManager().popBackStack();
                    }
                }
            }
        });


        return view;
    }

}
