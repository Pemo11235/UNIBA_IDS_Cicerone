package com.winotech.cicerone.view;

import android.app.FragmentTransaction;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import android.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Cicerone;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Feedback;
import com.winotech.cicerone.model.Globetrotter;
import com.winotech.cicerone.model.Language;
import com.winotech.cicerone.model.Location;
import com.winotech.cicerone.model.Tour;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import de.hdodenhof.circleimageview.CircleImageView;


public class ShowTourFromSearchFragment extends Fragment implements Serializable {

    // componenti del contesto del Fragment
    private View view;
    private ViewGroup localContainer;

    // puntatore al db locale
    private DBManager db;

    // puntatore al controller delle attività
    private static transient TourController tourController;

    // puntatore al controller generale
    private static transient GeneralController generalController;

    // oggetti grafici del fragment
    private TextView tourNameTextView;
    private TextView tourStartEndDateTextView;
    private TextView tourCostTextView;
    private TextView tourDescriptionTextView;
    private TextView tourLocationsTextView;
    private TextView tourLanguagesTextView;
    transient private CircleImageView profileImage;
    private TextView usernameTextView;
    private TextView tourFeedbackTextView;
    private Button subscribeButton;


    /*
    Singleton del fragment
    */
    public static ShowTourFromSearchFragment newInstance(DBManager db, int tourID) {

        ShowTourFromSearchFragment fragment = new ShowTourFromSearchFragment();

        if(db != null) {

            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.DB_KEY, db);
            bundle.putSerializable(Constants.KEY_TOUR_ID, tourID);
            fragment.setArguments(bundle);
        }

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_show_tour_from_search, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione del db
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        final int tourID = (int) getArguments().getSerializable(Constants.KEY_TOUR_ID);

        // inizializzazione del controller dei tour
        if(tourController == null)
            TourController.initInstance(getActivity());
        tourController = TourController.getInstance();

        // inizializzazione del controller generico
        if(generalController == null)
            GeneralController.initInstance(getActivity());
        generalController = GeneralController.getInstance();

        // acquisizione dei dati completi dell'attività
        final HashMap<String, Object> tourData = new HashMap<>(tourController.getTourFromID(tourID));

        tourNameTextView = view.findViewById(R.id.tour_name_textView);
        tourStartEndDateTextView = view.findViewById(R.id.tour_date_textView);
        tourCostTextView = view.findViewById(R.id.tour_cost_textView);
        tourDescriptionTextView = view.findViewById(R.id.tour_description_textView);
        tourLocationsTextView = view.findViewById(R.id.tour_locations_textView);
        tourLanguagesTextView = view.findViewById(R.id.tour_languages_textView);
        profileImage = view.findViewById(R.id.imageView4);
        usernameTextView = view.findViewById(R.id.username_textView);
        tourFeedbackTextView = view.findViewById(R.id.tour_feedbacks_textView);
        subscribeButton = view.findViewById(R.id.subscribe_button);

        // acquisizione dei nomi dei mesi
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();

        // oggetto per la definizione delle date
        GregorianCalendar date = new GregorianCalendar();

        // definizione del nome del tour
        tourNameTextView.setText(((Tour) tourData.get("tour")).getName());

        // definizione della data di validità del tour
        date.setTime(((Tour) tourData.get("tour")).getStartDate().getTime());
        String dateText = date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                months[date.get(GregorianCalendar.MONTH)] + " " +
                date.get(GregorianCalendar.YEAR);
        date.setTime(((Tour) tourData.get("tour")).getEndDate().getTime());
        dateText += " - " + date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                months[date.get(GregorianCalendar.MONTH)] + " " +
                date.get(GregorianCalendar.YEAR);
        tourStartEndDateTextView.setText(dateText);

        // definizione del costo del tour
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        String cost = df.format(((Tour)tourData.get("tour")).getCost()) + "€";
        tourCostTextView.setText(cost);

        // definizione della descrizione del tour
        tourDescriptionTextView.setText(((Tour) tourData.get("tour")).getDescription());

        // definizione delle tappe del tour
        ArrayList<Location> locations = (ArrayList<Location>) tourData.get("locations");
        String locationString = "";
        for (Location location : locations)
            locationString += generalController.getLocationFromCoords(
                    location.getLatitude(), location.getLongitude()) + "\n";
        tourLocationsTextView.setText(locationString);

        // definizione delle lingue parlate del tour
        ArrayList<Language> languages = (ArrayList<Language>) tourData.get("spoken_languages");
        if(!languages.isEmpty()) {

            Vector<String> languageNames = new Vector<>();
            for (Language language : languages) {

                if (!languageNames.contains(language.getName()))
                    languageNames.add(language.getName());
            }
            String languageString = "";
            for (String language : languageNames)
                languageString += language + "\n";
            tourLanguagesTextView.setText(languageString);

        } else {

            tourLanguagesTextView.setText(getResources().getString(R.string.no_spoken_languages));
        }

        // definizione dell'immagine del profilo del cicerone
        profileImage.setImageBitmap(((Cicerone)tourData.get("cicerone")).getPhoto());

        // definizione dello username del cicerone
        usernameTextView.setText(((Cicerone) tourData.get("cicerone")).getUsername());

        // definizione dello username del cicerone
        ArrayList<Feedback> feedbacks = (ArrayList<Feedback>) tourData.get("feedbacks");
        if(!feedbacks.isEmpty()) {

            String feedbackString = "";
            for (Feedback goodFB : feedbacks) {

                if (goodFB.getRate() == 5) {
                    feedbackString += getResources().getString(R.string.best_feedback)
                            + goodFB.getGlobetrotter().getUsername() + " " + getResources().getString(R.string.from_feedback)
                            + goodFB.getDescription();
                    break;
                }
            }
            for (Feedback badFB : feedbacks) {

                if (badFB.getRate() == 1) {

                    feedbackString += getResources().getString(R.string.worst_feedback)
                            + badFB.getGlobetrotter().getUsername() + " " + getResources().getString(R.string.from_feedback)
                            + badFB.getDescription();
                    break;
                }
            }
            feedbackString += getResources().getString(R.string.read_all_feedbacks);
            tourFeedbackTextView.setText(Html.fromHtml(feedbackString));

        } else {

            String feedbackString = getResources().getString(R.string.no_feedback);
            tourFeedbackTextView.setText(Html.fromHtml(feedbackString));
        }

        // definizione del listener per il click
        tourFeedbackTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(!tourFeedbackTextView.getText().toString().equals(getResources().getString(R.string.no_feedback))) {

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragment = ShowFeedbackFragment.newInstance(db, tourID);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment, fragment).commit();
                }
            }
        });

        // visualizzazione del bottone di iscrizione all'evento
        if(db.getMyAccount() instanceof Globetrotter) {

            subscribeButton.setVisibility(View.VISIBLE);

            // definizione del listener per il click sul bottone
            subscribeButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragmentView = ShowEventsForChoiceFragment.newInstance(db,
                            (ArrayList<Event>)tourData.get("events"));
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
                }
            });

        } else {

            subscribeButton.setVisibility(View.INVISIBLE);
        }

        return view;
    }



}
