package com.winotech.cicerone.view;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Tour;


import java.io.Serializable;
import java.util.ArrayList;


public class SearchFragment extends Fragment implements Serializable {

    // componenti del contesto del Fragment
    private View view;
    private ViewGroup localContainer;

    // puntatore al db locale
    private DBManager db;

    // puntatore al controller delle attività
    private static transient TourController tourController;

    // adattatore per la lista delle attività
    private ListViewAdapter adapter;

    // componenti grafiche
    private ListView listViewSearch;
    private Button searchButton;
    private EditText searchView;

    // lista dei tour da mostrare
    private ArrayList<Tour> searchList;


    /*
    Singleton del fragment
     */
    public static SearchFragment newInstance(DBManager db) {

        SearchFragment fragment = new SearchFragment();

        if(db != null) {

            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.DB_KEY, db);
            fragment.setArguments(bundle);
        }

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_search, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione del db
        if(getArguments() != null)
            db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        else
            db = new DBManager(view.getContext());

        // inizializzazione del controller
        if(tourController == null)
            TourController.initInstance(getActivity());
        tourController = TourController.getInstance();

        // inizializzazioend dele componenti grafiche
        listViewSearch = view.findViewById(R.id.mainView2);
        searchButton = view.findViewById(R.id.button8);
        searchView = view.findViewById(R.id.editText2);

        // inizializzazione della lista di ricerca
        searchList = new ArrayList<>();

        // definizione di un nuovo adapter
        adapter = new ListViewAdapter(searchList);

        // settaggio dell'adapter sulla lista di lingue
        listViewSearch.setAdapter(adapter);

        // definizione del listener del bottone di ricerca
        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // se il SearchView contiene parole di senso compiuto allora effettua l'acqusizione
                if(!searchView.getText().toString().equals("") && !searchView.getText().toString().equals(" ")) {

                    searchList = new ArrayList<>(tourController.getTourFromSearch(searchView.getText().toString(),
                            db.getMyAccount().getUsername()));

                } else { // altrimenti crea un Tour fake per effettuare una stampa

                    searchList = new ArrayList<>();

                    Tour fakeTour = new Tour();

                    fakeTour.setName(getResources().getString(R.string.no_tour_find));
                    fakeTour.setDescription(getResources().getString(R.string.better_search_keywords));

                    searchList.add(fakeTour);
                }

                // definizione di un nuovo adapter
                adapter = new ListViewAdapter(searchList);

                // settaggio dell'adapter sulla lista di lingue
                listViewSearch.setAdapter(adapter);

            }
        });

        // settaggio del listener del contenuto dellariga
        listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long l) {

                // acquisizione dell'attività cliccata
                Tour clickedTour = (Tour)adapter.getItemAtPosition(position);

                // se il tour cliccato esiste e non è solo un segnaposto
                if(!clickedTour.getName().equals(view.getResources().getString(R.string.no_tour_find))) {

                    // visualizzazione del tour
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragmentView = ShowTourFromSearchFragment.newInstance(db, clickedTour.getId());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
                }
            }

        });

        return view;
    }


    /*
    Classe per la creazione di un adattatore per la listview
     */
    public class ListViewAdapter extends ArrayAdapter {

        Context context;
        ArrayList<Tour> items;


        @SuppressWarnings("unchecked")
        public ListViewAdapter(ArrayList<Tour> resource) {

            super(view.getContext(), R.layout.checkbox_listview_row, resource);

            this.context = view.getContext();
            this.items = resource;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // definizione del layout della linea
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.listview_row, parent, false);

            // inizializzazione del nome e della descrizione da mostrare nella linea
            final TextView name = (TextView) convertView.findViewById(R.id.textViewMyTourListName);
            final TextView description = (TextView) convertView.findViewById(R.id.textViewMyTourListOther);

            // settaggio del nome dell'attività
            name.setText(items.get(position).getName());

            // settaggio della descrizione dell'attività e troncamento se supera un tot di caratteri
            String formattedDescription = items.get(position).getDescription();
            if(formattedDescription.length() > 40) {

                formattedDescription = formattedDescription.substring(0, 40);
                formattedDescription = formattedDescription.concat("...");
            }

            // settaggio della descrizione dell'attività
            description.setText(formattedDescription);

            // ritorno del layout completo della riga
            return convertView;
        }
    }

}
