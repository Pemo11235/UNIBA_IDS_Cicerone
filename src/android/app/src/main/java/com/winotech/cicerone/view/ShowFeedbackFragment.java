package com.winotech.cicerone.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Feedback;
import com.winotech.cicerone.model.Request;
import com.winotech.cicerone.model.Subscribed;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class ShowFeedbackFragment extends Fragment {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller delle attività
    private static transient TourController tourController;

    // componenti grafiche
    private ListView listViewFeedback;

    // dati inerenti all'evento
    private ArrayList<Feedback> feedbacks = new ArrayList<>();


    /*
    Singleton del fragment
     */
    public static ShowFeedbackFragment newInstance(DBManager db, int tourID) {

        ShowFeedbackFragment fragment = new ShowFeedbackFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.KEY_TOUR_ID, tourID);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_show_feedback, container, false);
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

        // acquisizione della lista dei feedback
        HashMap<String, Object> tourData = new HashMap<>(tourController.getTourFromID(tourID));
        feedbacks = (ArrayList<Feedback>)tourData.get("feedbacks");
        tourData.clear();

        // definizione degli oggetti grafici
        listViewFeedback = view.findViewById(R.id.mainView);

        // definizione di un nuovo adapter
        FeedbackListViewAdapter adapter = new FeedbackListViewAdapter(feedbacks);

        // settaggio dell'adapter sulla lista di lingue
        listViewFeedback.setAdapter(adapter);

        return view;
    }


    /*
   Classe per la creazione di un adattatore per la listview
    */
    public class FeedbackListViewAdapter extends ArrayAdapter {

        Context context;
        ArrayList<Feedback> feedback;

        @SuppressWarnings("unchecked")
        public FeedbackListViewAdapter(ArrayList<Feedback> resource) {

            super(view.getContext(), R.layout.checkbox_listview_row, resource);

            this.context = view.getContext();
            this.feedback = resource;
        }

        @Override
        public View getView(final int position, View adapterView, ViewGroup parent) {

            // definizione del layout della linea
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            adapterView = inflater.inflate(R.layout.feedback_listview_row, parent, false);

            // inizializzazione del nome e della descrizione da mostrare nella linea
            final TextView globetrotter = adapterView.findViewById(R.id.feedback_globetrotter_textView);
            final TextView createdOn = adapterView.findViewById(R.id.feedback_created_on_textView);
            final RatingBar rate = adapterView.findViewById(R.id.feedback_rating_textView);
            final TextView description = adapterView.findViewById(R.id.feedback_textView);

            // acquisizione dei nomi dei mesi
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] months = dfs.getMonths();

            // definizione della data di effettuazione dell'evento
            // se la data è si un solo giorno allora mostra solo la data di inizio
            // dato che inizio e fine sono uguali
            String dateText = "";
            GregorianCalendar dateInCalendar = new GregorianCalendar();
            dateInCalendar.setTime(feedback.get(position).getCreatedOn().getTime());
            dateText = dateInCalendar.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[dateInCalendar.get(GregorianCalendar.MONTH)] + " " +
                    dateInCalendar.get(GregorianCalendar.YEAR);

            // settaggio del nome dell'attività
            createdOn.setText(dateText);

            // settaggio del globetrotter
            globetrotter.setText(feedback.get(position).getGlobetrotter().getUsername());

            // settaggio del voto
            rate.setRating((float)feedback.get(position).getRate());

            // settaggio della descrizione
            description.setText(feedback.get(position).getDescription());

            return adapterView;
        }
    }
}
