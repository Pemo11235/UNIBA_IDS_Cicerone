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
import android.widget.ListView;
import android.widget.TextView;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Notification;

import java.io.Serializable;
import java.util.ArrayList;


public class NotificationsFragment extends Fragment implements Serializable {

    // componenti del contesto del Fragment
    private View view;
    private ViewGroup localContainer;

    // puntatore al db locale
    private DBManager db;

    // puntatore al controller delle attività
    private static transient GeneralController generalController;

    // adattatore per la lista delle attività
    private ListViewAdapter adapter;

    // componenti grafiche
    private ListView listViewNotification;

    // lista dei tour da mostrare
    private ArrayList<Notification> notificationList;


    /*
    Singleton del fragment
     */
    public static NotificationsFragment newInstance(DBManager db) {

        NotificationsFragment fragment = new NotificationsFragment();

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
        view = inflater.inflate(R.layout.fragment_notifications, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione del db
        if(getArguments() != null)
            db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        else
            db = new DBManager(view.getContext());

        // inizializzazione del controller
        if(generalController == null)
            GeneralController.initInstance(getActivity());
        generalController = GeneralController.getInstance();

        // inizializzazioend dele componenti grafiche
        listViewNotification = view.findViewById(R.id.mainView3);

        // inizializzazione della lista di ricerca
        notificationList = new ArrayList<>();//generalController.getNotifications(db.getMyAccount().getUsername()));


        // acquisizione delle proprie attività
        if(db != null) {

            //if(generalController.getLoadedNotifications().isEmpty())
                notificationList = generalController.getNotifications(db.getMyAccount().getUsername());
            //else
            //    notificationList = generalController.getLoadedNotifications();
        }


        // definizione di un nuovo adapter
        adapter = new ListViewAdapter(notificationList);

        // settaggio dell'adapter sulla lista di notifiche
        listViewNotification.setAdapter(adapter);

        // settaggio del listener del contenuto dellariga
        listViewNotification.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long l) {

                // acquisizione dell'attività cliccata
                Notification clickedNotification = (Notification) adapter.getItemAtPosition(position);

                // se la notifica cliccata esiste e non è solo un segnaposto
                if(!clickedNotification.getReason().getName().equals(" ")) {

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragmentView = ShowNotificationFragment.newInstance(db, clickedNotification);
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
        ArrayList<Notification> items;


        @SuppressWarnings("unchecked")
        public ListViewAdapter(ArrayList<Notification> resource) {

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
            final TextView status = (TextView) convertView.findViewById(R.id.textViewMyTourListName);
            final TextView description = (TextView) convertView.findViewById(R.id.textViewMyTourListOther);

            String message = "";
            String subject = "";

            if(!items.get(position).getReason().getName().equals(" ")) {

                message = getResources().getString(R.string.notifications_from) + " "
                        + items.get(position).getSender().getUsername();

                subject = getResources().getString(R.string.notifications_object) + " " +
                        generalController.getNotificationObjectFromID(items.get(position).getReason().getName());

            } else {

                message = items.get(position).getSender().getUsername();

                subject = items.get(position).getReason().getName();
            }

            // settaggio del mittente della notifica
            status.setText(message);

            // settaggio della descrizione della notifica
            description.setText(subject);

            // ritorno del layout completo della riga
            return convertView;
        }
    }


}
