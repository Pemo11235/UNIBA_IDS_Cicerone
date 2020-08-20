package com.winotech.cicerone.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import android.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.AuthController;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Cicerone;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Subscribed;
import com.winotech.cicerone.model.Tour;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class DeleteAccountFragment extends Fragment {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller delle attività
    private static transient AuthController authController;

    // oggetti grafici
    private Button submitButton;



    /*
    Singleton del fragment
     */
    public static DeleteAccountFragment newInstance(DBManager db, AuthController authController) {

        DeleteAccountFragment fragment = new DeleteAccountFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.AUTH_CONTROLLER_KEY, authController);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del layout del fragment
        view = inflater.inflate(R.layout.fragment_delete_account, container, false);
        localContainer = container;

        // rimozione di tutti i  fragment dallo stack
        if(localContainer != null)
            localContainer.removeAllViews();

        // acquisizione del controller
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        authController = (AuthController) getArguments().getSerializable(Constants.AUTH_CONTROLLER_KEY);

        // inizializzazione componente grafico
        submitButton = view.findViewById(R.id.action_button);


        // definizione del listener del bottone di invio
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {


                if(db.getMyAccount() instanceof Cicerone) {

                    if (verifyDeletionData()) {

                        showAreYouSurePopup();

                    } else {

                        Toast.makeText(view.getContext(), getResources().getString(R.string.account_have_tour_payment),
                                Toast.LENGTH_LONG).show();
                    }

                } else {

                    showAreYouSurePopup();
                }
            }
        });


        return view;
    }


    public boolean verifyDeletionData() {

        // acquisizione dell'istanza del tourcontroller e acquisizione dei tour dell'utente
        TourController tourController = TourController.getInstance();
        ArrayList<Tour> tours = tourController.getTours(db.getMyAccount().getUsername(), true);



        // se esistono dei tour in possesso del cicerone effettua il controllo
        if(tours != null && !tours.isEmpty()) {

            // scandendo ogni tour presente per l'utente
            for (Tour tour : tours) {

                // acquisizione dei dati del tour
                HashMap<String, Object> tourData = tourController.getTourFromID(tours.get(0).getId());

                // se il costo del tour è maggiore di 0
                if (tour.getCost() > 0) {

                    // acquisisci la lista di eventi del tour e scandiscili
                    ArrayList<Event> events = (ArrayList<Event>)tourData.get("events");
                    for (Event event : events) {

                        // se l'evento non è ancora passato
                        if(!event.getEndDate().before(Calendar.getInstance())) {

                            // acquisisci la lista di iscritti e scandiscili
                            ArrayList<Subscribed> subscribeds = event.getEventSubscribeds();
                            if (subscribeds != null && !subscribeds.isEmpty()) {
                                for (Subscribed sub : subscribeds) {

                                    // se l'iscritto ha gia pagato allora non puoi effettuare nulla
                                    if (sub.getHasPayed())
                                        return false;

                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }


    public void showAreYouSurePopup() {

        // definizione dell'alert dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());

        // definizione del messaggio
        alertDialogBuilder
                .setMessage(view.getContext().getResources().getString(R.string.delete_are_you_sure))
                .setCancelable(false)
                .setPositiveButton(view.getContext().getResources().getString(R.string.proceed),
                        new DialogInterface.OnClickListener() {

                            // se il bottone viene selezionato effettua il logout
                            public void onClick(DialogInterface dialog,int id) {



                                boolean success = authController.deleteMyAccount(db.getMyAccount().getUsername());

                                if(success) {

                                    authController.logout();
                                    Log.d("DeleteFragment", "Deletion successful");
                                }
                            }
                        })
                .setNegativeButton(view.getContext().getResources().getString(R.string.no),
                        new DialogInterface.OnClickListener() {

                            // se il bottone non viene selezionato non effettua il logout
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                                Log.d("DeleteFragment", "Deletion successfully blocked");
                            }
                        });

        // crea l'alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // mostra a video l'alert dialog
        alertDialog.show();

        Button b = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if(b != null)
            b.setTextColor(Color.BLACK);
        b = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if(b != null)
            b.setTextColor(Color.BLACK);
    }
}
