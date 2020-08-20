package com.winotech.cicerone.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.AuthController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Language;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EditSpokenLanguagesFragment extends Fragment implements Serializable {

    // variabili per il layout del fragment
    private View view;
    private ViewGroup localContainer;

    private DBManager db;
    private AuthController authController;

    // puntatore all'activity principale
    MainActivity activity;

    // adapter per l'itemlist
    private ArrayAdapter<Object> adapter;

    // lista delle lingue parlate
    private ArrayList<Language> languageList;
    private ArrayList<Language> spokenLanguageList;

    // lista delle lingue da salvare
    private HashMap<String, Boolean> updatedSpokenLanguageList;

    // componenti grafici
    private ListView languageListView;
    private Button submitButton;


    /*
    Singleton del fragment
     */
    public static EditSpokenLanguagesFragment newInstance(DBManager db, AuthController userController) {

        EditSpokenLanguagesFragment fragment = new EditSpokenLanguagesFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.AUTH_CONTROLLER_KEY, userController);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_edit_spoken_languages, container, false);
        localContainer = container;

        // rimozione di tutti i  fragment dallo stack
        if(localContainer != null)
            localContainer.removeAllViews();

        // acquisizione del controller
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        authController = (AuthController) getArguments().getSerializable(Constants.AUTH_CONTROLLER_KEY);

        // inizializzazione al puntatore sull'activity di appartenenza
        activity = (MainActivity) getActivity();

        // DEFINIZIONE DEGLI OGGETTI GRAFICI
        languageListView = (ListView)view.findViewById(R.id.language_mainView);
        submitButton = (Button)view.findViewById(R.id.submit_edit_spoken_languages_button);


        // acquisizione di tutte le lingue dal database locale
        languageList = activity.getDb().getLanguageList();

        // acquisizione di tutte le lingue parlate dal database locale
        spokenLanguageList = db.getMyAccount().getSpokenLanguages();

        // lista delle lingue da aggiornare
        updatedSpokenLanguageList = new HashMap<>();

        // inizializzazione della mappa di lingue
        for (Language lang : languageList)
            updatedSpokenLanguageList.put(lang.getName(), false);

        // definizione dei campi delle mappe delle lingue
        for (Language lang : spokenLanguageList)
            updatedSpokenLanguageList.put(lang.getName(), true);

        // definizione di un nuovo adapter
        ListViewAdapter adapter= new ListViewAdapter(languageList);

        // settaggio dell'adapter sulla lista di lingue
        languageListView.setAdapter(adapter);

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){

        super.onActivityCreated(savedInstanceState);

        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // iteratore di scorrimento della mappa delle lingue parlate
                Iterator it = updatedSpokenLanguageList.entrySet().iterator();

                // stampa di debug delle lingue parlate aggiornate
                while (it.hasNext()){

                    // acquisizione dell'elemento puntato dall'iteratore
                    HashMap.Entry lang = (HashMap.Entry) it.next();
                    if((boolean)lang.getValue())
                        Log.d("EditSpokenLanguage", "Updated Spoken Languages: " + lang.getKey());
                }

                // esecuzione dell'invio dei dati
                (new SendSpokenLanguagesData(updatedSpokenLanguageList)).execute();

            }
        });
    }


    /*
    Classe per la creazione di un adattatore per la listview
     */
    public class ListViewAdapter extends ArrayAdapter {

        Context context;
        ArrayList<Language> items;

        @SuppressWarnings("unchecked")
        public ListViewAdapter(ArrayList<Language> resource) {

            super(activity, R.layout.checkbox_listview_row, resource);

            this.context = activity;
            this.items = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.checkbox_listview_row, parent, false);

            final TextView name = (TextView) convertView.findViewById(R.id.tv);
            final CheckBox box = (CheckBox) convertView.findViewById(R.id.cb);
            name.setText(items.get(position).getName());

            // settaggio dei checkbox sulle lingue gia parlate
            if (updatedSpokenLanguageList.get(items.get(position).getName()))
                box.setChecked(true);

            // listener associato alla stringa
            name.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if(box.isChecked())
                        box.setChecked(false);
                    else
                        box.setChecked(true);
                }
            });

            // listener associato al checkbox
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if(box.isChecked())
                        updatedSpokenLanguageList.put(items.get(position).getName(), true);
                    else
                        updatedSpokenLanguageList.put(items.get(position).getName(), false);

                    Log.d("CheckboxSelection", "Selected checkbox with name "
                            + items.get(position).getName());
                }
            });

            return convertView;
        }


    }


    /*
    Classe locale per l'invio dei dati in modo asincrono (necessario per la visualizzazione
    dei dialoghi)
     */
    class SendSpokenLanguagesData extends AsyncTask<Void, Void, Void> {

        // lista delle lingue da salvare
        private HashMap<Integer, Boolean> spokenLanguages;

        ProgressDialog dialog;

        protected SendSpokenLanguagesData(HashMap<String, Boolean> spokenLanguageList) {

            dialog = new ProgressDialog(getActivity());

            spokenLanguages = new HashMap<>();

            // definizione dell'insieme delle lingue parlabili
            ArrayList<Language> languages = activity.getDb().getLanguageList();

            // per ogni lingua passata nella lista delle lingue parlate dall'utente
            for (Map.Entry<String, Boolean> lang : spokenLanguageList.entrySet()){

                // si effettua un confronto con le lingue totali per acquisire l'id
                for (Language language : languages){

                    if(language.getName().equals(lang.getKey()))
                        spokenLanguages.put(language.getId(), lang.getValue());
                }
            }

        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.acquiring_data),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            // salvataggio delle lingue
            activity.getAuthController().editSpokenLanguages (db.getMyAccount().getUsername(),
                    spokenLanguages);

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
