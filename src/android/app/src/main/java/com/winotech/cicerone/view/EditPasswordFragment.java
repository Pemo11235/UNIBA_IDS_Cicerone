package com.winotech.cicerone.view;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.AuthController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;


public class EditPasswordFragment extends Fragment {


    // variabili per il layout del fragment
    private View view;
    private ViewGroup localContainer;

    private DBManager db;
    private AuthController authController;

    // puntatore all'activity principale
    MainActivity activity;

    // variabili per gli oggetti grafici
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText againNewPasswordEditText;
    private Button submitButton;


    /*
    Singleton del fragment
     */
    public static EditPasswordFragment newInstance(DBManager db, AuthController userController) {

        EditPasswordFragment fragment = new EditPasswordFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.AUTH_CONTROLLER_KEY, userController);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inizializzazione del layout del fragment
        view = inflater.inflate(R.layout.fragment_edit_password, container, false);
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
        oldPasswordEditText = view.findViewById(R.id.old_password_editText);
        newPasswordEditText = view.findViewById(R.id.new_password_editText);
        againNewPasswordEditText = view.findViewById(R.id.again_new_password_editText);
        submitButton = view.findViewById(R.id.submit_edit_password_button);


        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        // ----- DEFINIZIONE DEI LISTENER -----
        // listener per il bottone di invio
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // controllo del completamento dei campi
                if(oldPasswordEditText.getText().toString().equals("") ||
                        newPasswordEditText.getText().toString().equals("") ||
                        againNewPasswordEditText.getText().toString().equals("")) {

                    Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.empty_password),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // controllo della correttezza della password vecchia
                if(!oldPasswordEditText.getText().toString().equals(db.getMyAccount().getPassword())){

                    Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.wrong_password),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // controllo del corretto inserimento della password nuova e la sua omonima
                if(!newPasswordEditText.getText().toString().equals(againNewPasswordEditText.getText().toString())) {

                    Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.new_password_different),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // controllo della diversità tra la password vecchia e quella nuova
                if(newPasswordEditText.getText().toString().equals(oldPasswordEditText.getText().toString())) {

                    Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.new_password_no_change),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // settaggio della password
                String new_password = newPasswordEditText.getText().toString();

                // esecuzione dell'invio dei dati
                (new SendData(new_password)).execute();
            }
        });

    }

    /*
    Classe locale per l'invio dei dati in modo asincrono (necessario per la visualizzazione
    dei dialoghi)
     */
    class SendData extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;
        String username;
        String new_password;


        protected SendData(String new_password) {

            this.username = db.getMyAccount().getUsername();
            this.new_password = new_password;

            dialog = new ProgressDialog(getActivity());
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.acquiring_data),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            activity.getAuthController().editPassword (username, new_password);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);

            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
        }
    }
}
