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
import android.widget.TextView;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Request;
import com.winotech.cicerone.model.Subscribed;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import de.hdodenhof.circleimageview.CircleImageView;


public class ManageRequestFragment extends Fragment {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller generico
    private static transient GeneralController generalController;

    // oggetti grafici
    private CircleImageView imageProfile;
    private TextView usernameTextView;
    private TextView groupDimensionTextView;
    private TextView createdOnTextView;
    private TextView stillPlacesTextView;
    private Button acceptButton;
    private Button rejectButton;

    // puntatore alla richiesta dell'iscrizione all'evento
    private Request request;

    // numero di posti ancora disponibili nel tour
    int stillPlacesNumber;


    /*
    Singleton del fragment
     */
    public static ManageRequestFragment newInstance(DBManager db, Request request) {

        ManageRequestFragment fragment = new ManageRequestFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.KEY_REQUEST_DATA, request);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_manage_request, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione dei parametri passati
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        request = (Request) getArguments().getSerializable(Constants.KEY_REQUEST_DATA);

        // inizializzazione del controller
        if(generalController == null)
            GeneralController.initInstance(getActivity());
        generalController = GeneralController.getInstance();

        // inizializzazione degli oggetti grafici
        imageProfile = view.findViewById(R.id.imageView4);
        usernameTextView = view.findViewById(R.id.username_textView);
        groupDimensionTextView = view.findViewById(R.id.request_group_dimension_textView);
        createdOnTextView = view.findViewById(R.id.request_created_on_textView);
        stillPlacesTextView = view.findViewById(R.id.still_places_textView);
        acceptButton = view.findViewById(R.id.accept_button);
        rejectButton = view.findViewById(R.id.reject_button);

        // definizione dell'immagine del profilo
        imageProfile.setImageBitmap(request.getGlobetrotter().getPhoto());

        // definizione dello username
        usernameTextView.setText(request.getGlobetrotter().getUsername());

        // definizione della dimensione del gruppo
        groupDimensionTextView.setText(Integer.toString(request.getGroupDimension()));

        // acquisizione dei nomi dei mesi
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();

        // oggetto per la definizione delle date
        GregorianCalendar date = new GregorianCalendar();

        // definizione della data di creazioned ella notifica
        date.setTime(request.getCreatedOn().getTime());
        String createdOn = date.get(GregorianCalendar.DAY_OF_MONTH) + " "
                + months[date.get(GregorianCalendar.MONTH)] + " " +
                date.get(GregorianCalendar.YEAR);
        createdOnTextView.setText(createdOn);

        // definizione del numero di posti disponibili
        stillPlacesNumber = request.getEvent().getMaxSubscribeds();
        ArrayList<Subscribed> totalSubscription = request.getEvent().getEventSubscribeds();
        for (Subscribed subscription : totalSubscription)
            stillPlacesNumber -= subscription.getGroupDimension();

        // definizione della stringa formattata
        String stillPlaces = getResources().getString(R.string.event_subscription_free_places);
        stillPlaces = stillPlaces.replace("{PLACES_REMAIN}", Integer.toString(stillPlacesNumber));

        // definizione del numero di posti disponibili
        stillPlacesTextView.setText(stillPlaces);

        // definizione del listener sul click del bottone di accettazione
        acceptButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // se non ci sono abbastanz aposti disponibili allora mostra un messaggio di errore
                if(stillPlacesNumber - request.getGroupDimension() < 0) {

                    // messaggio di errore
                    Toast.makeText (getActivity(), R.string.event_subscription_free_places_not_enough,
                            Toast.LENGTH_LONG).show();

                } else {

                    // salva la risposta negativa
                    if (generalController.saveRequestResponse(request, true)) {

                        // messaggio di errore
                        Toast.makeText(getActivity(), R.string.success_accept_request, Toast.LENGTH_LONG).show();

                        getFragmentManager().popBackStack();

                    } else {

                        // messaggio di errore
                        Toast.makeText(getActivity(), R.string.oops_request, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        // definizione del listener sul click del bottone di ridiuto
        rejectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // salva la risposta negativa
                if(generalController.saveRequestResponse(request, false)){

                    // messaggio di errore
                    Toast.makeText (getActivity(), R.string.success_reject_request, Toast.LENGTH_LONG).show();

                    getFragmentManager().popBackStack();

                } else {

                    // messaggio di errore
                    Toast.makeText (getActivity(), R.string.oops_request, Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }


}
