package com.winotech.cicerone.view;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.AuthController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;

import java.io.Serializable;


public class SettingsFragment extends Fragment implements Serializable {

    // variabili per il layout del fragment
    private View view;
    private ViewGroup localContainer;

    private DBManager db;
    private AuthController authController;

    // puntatore all'activity principale
    MainActivity activity;

    // variabili per gli oggetti grafici
    private TextView textViewLogout;
    private TextView textViewPolicy;
    private TextView textViewEditAccount;
    private TextView getTextViewChangeLanguage;
    private TextView textViewDeleteAccount;



    /*
    Singleton del fragment
     */
    public static SettingsFragment newInstance(DBManager db, AuthController authController) {

        SettingsFragment fragment = new SettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.AUTH_CONTROLLER_KEY, authController);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inizializzazione del layout del fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        localContainer = container;

        // rimozione di tutti i  fragment dallo stack
        if(localContainer != null)
            localContainer.removeAllViews();


        // acquisizione del controller
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        authController = (AuthController) getArguments().getSerializable(Constants.AUTH_CONTROLLER_KEY);

        // inizializzazione al puntatore sull'activity di appartenenza
        activity = (MainActivity) getActivity();

        // ----- INIZIALIZZAZIONE OGGETTI GRAFICI -----
        textViewPolicy = view.findViewById(R.id.textViewReadContract);
        textViewLogout = view.findViewById(R.id.textViewLogout);
        textViewEditAccount = view.findViewById(R.id.textViewEditAccount);
        getTextViewChangeLanguage = view.findViewById(R.id.textViewChangeLanguage);
        textViewDeleteAccount = view.findViewById(R.id.textViewDeleteAccount);


        // ----- DEFINIZIONE DEI LISTENER -----

        // definizione del listener sul bottone di logout
        textViewLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // definizione dell'alert dialog
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

                // definizione del messaggio
                alertDialogBuilder
                        .setMessage(activity.getResources().getString(R.string.logout_message))
                        .setCancelable(false)
                        .setPositiveButton(activity.getResources().getString(R.string.yes),
                                new DialogInterface.OnClickListener() {

                            // se il bottone viene selezionato effettua il logout
                            public void onClick(DialogInterface dialog,int id) {

                                activity.getAuthController().logout();
                                Log.d("SettingsFragment", "Logout successful");
                            }
                        })
                        .setNegativeButton(activity.getResources().getString(R.string.no),
                                new DialogInterface.OnClickListener() {

                            // se il bottone non viene selezionato non effettua il logout
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                                Log.d("SettingsFragment", "Logout successfully blocked");
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
        });


        // definizione del listener sul bottone di modifica profilo
        textViewEditAccount.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = EditAccountFragment.newInstance(db, authController);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
            }
        });

        // definizione del listener sul bottone di modifica profilo
        getTextViewChangeLanguage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = SelectLanguageFragment.newInstance();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
            }
        });
        // definizione del listener sul bottone di visualizzazione termini d'uso

        textViewPolicy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = ViewPolicyFragment.newInstance();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
            }
        });

        // definizione del listener sul bottone di cancellazione dell'account
        textViewDeleteAccount.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = DeleteAccountFragment.newInstance(db, authController);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
            }
        });

        return view;
    }

}
