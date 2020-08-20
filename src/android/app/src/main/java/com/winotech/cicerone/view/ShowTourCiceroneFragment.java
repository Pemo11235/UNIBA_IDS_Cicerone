package com.winotech.cicerone.view;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Feedback;
import com.winotech.cicerone.model.Language;
import com.winotech.cicerone.model.Location;
import com.winotech.cicerone.model.Tour;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;


public class ShowTourCiceroneFragment extends Fragment implements Serializable {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller delle attività
    private static transient TourController tourController;

    // oggetti grafici del fragment
    private TextView tourGeneralInfoButton;
    private TextView usernameTextView;
    private TextView tourNameTextView;
    private TextView tourStartEndDateTextView;
    private TextView tourCostTextView;
    private TextView tourDescriptionTextView;
    private TextView tourLocationsTextView;
    private TextView tourEventsTextView;
    private TextView tourLanguagesTextView;
    private TextView tourFeedbackTextView;

    private TextView tourNameDescrTextView;
    private TextView tourStartEndDateDescrTextView;
    private TextView tourCostDescrTextView;
    private TextView tourDescriptionDescrTextView;
    private TextView tourLanguagesDescrTextView;
    private TextView tourLocationsDescrTextView;
    private TextView tourEventsDescrTextView;
    private TextView tourFeedbackDescrTextView;

    private TextView editTourTextView;
    private TextView editLocationTextView;
    private TextView editEventTextView;

    private ImageView generalInfoArrow;
    private ImageView locationsInfoArrow;
    private ImageView eventsInfoArrow;
    private ImageView feedbacksInfoArrow;

    private Button actionButton;

    private boolean generalInfoAreGone = false;
    private boolean locationInfoAreGone = true;
    private boolean eventInfoAreGone = true;
    private boolean feedbackInfoAreGone = true;

    /*
    Singleton del fragment
     */
    public static ShowTourCiceroneFragment newInstance(DBManager db, int tour_id) {

        ShowTourCiceroneFragment fragment = new ShowTourCiceroneFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.KEY_TOUR_ID, tour_id);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_show_tour_cicerone, container, false);
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

        // acquisizione dei dati completi dell'attività
        final HashMap<String, Object> tourData = new HashMap<>(tourController.getTourFromID(tourID));

        tourGeneralInfoButton = view.findViewById(R.id.tour_general_info_textView);
        tourNameTextView = view.findViewById(R.id.tour_name_textView);
        tourNameDescrTextView = view.findViewById(R.id.textView39);
        tourStartEndDateTextView = view.findViewById(R.id.tour_date_textView);
        tourStartEndDateDescrTextView = view.findViewById(R.id.textView9);
        tourCostTextView = view.findViewById(R.id.tour_cost_textView);
        tourCostDescrTextView = view.findViewById(R.id.textView10);
        tourDescriptionTextView = view.findViewById(R.id.tour_description_textView);
        tourDescriptionDescrTextView = view.findViewById(R.id.textView12);
        tourLanguagesTextView = view.findViewById(R.id.tour_languages_textView);
        tourLanguagesDescrTextView = view.findViewById(R.id.textView41);
        tourLocationsTextView = view.findViewById(R.id.tour_locations_textView);
        tourLocationsDescrTextView = view.findViewById(R.id.locations_info_textView);
        tourEventsTextView = view.findViewById(R.id.tour_events_TextView);
        tourEventsDescrTextView = view.findViewById(R.id.events_info_textView);
        tourFeedbackTextView = view.findViewById(R.id.tour_feedbacks_textView);
        tourFeedbackDescrTextView = view.findViewById(R.id.feedbacks_info_textView);
        editLocationTextView = view.findViewById(R.id.edit_location_textView);
        editEventTextView = view.findViewById(R.id.edit_events_textView);
        editTourTextView = view.findViewById(R.id.edit_tour_textView);
        generalInfoArrow = view.findViewById(R.id.general_info_arrow);
        locationsInfoArrow = view.findViewById(R.id.locations_info_arrow);
        eventsInfoArrow = view.findViewById(R.id.event_info_arrow);
        feedbacksInfoArrow = view.findViewById(R.id.feedbacks_info_arrow);
        actionButton = view.findViewById(R.id.action_button);

        // definizione della visibilità del contenuto
        if(!((Tour)tourData.get("tour")).getEndDate().before(Calendar.getInstance()))
            editTourTextView.setVisibility(View.VISIBLE);
        else
            editTourTextView.setVisibility(View.GONE);
        tourLocationsTextView.setVisibility(View.GONE);
        editLocationTextView.setVisibility(View.GONE);
        tourEventsTextView.setVisibility(View.GONE);
        editEventTextView.setVisibility(View.GONE);
        tourLanguagesDescrTextView.setVisibility(View.GONE);
        tourLanguagesTextView.setVisibility(View.GONE);
        tourFeedbackTextView.setVisibility(View.GONE);

        if(!((Tour)tourData.get("tour")).getEndDate().before(Calendar.getInstance()))
            actionButton.setText(getResources().getString(R.string.delete_tour));
        else
            actionButton.setText(getResources().getString(R.string.renew_tour));

        // acquisizione dei nomi dei mesi
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();

        // oggetto per la definizione delle date
        GregorianCalendar date = new GregorianCalendar();

        // definizione del nome del tour
        tourNameTextView.setText(((Tour) tourData.get("tour")).getName());

        // definizione della data di validità del tour
        // se avviene in un solo giorno allora si mostra solo la data di inizio
        // altrimenti mostra sia la data di inizio che quella di fine
        if(((Tour) tourData.get("tour")).getStartDate().equals(((Tour) tourData.get("tour")).getEndDate())) {

            date.setTime(((Tour) tourData.get("tour")).getStartDate().getTime());
            String dateText = date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);
            tourStartEndDateTextView.setText(dateText);

        } else {

            date.setTime(((Tour) tourData.get("tour")).getStartDate().getTime());
            String dateText = date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);
            date.setTime(((Tour) tourData.get("tour")).getEndDate().getTime());
            dateText += " - " + date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);
            tourStartEndDateTextView.setText(dateText);
        }

        // definizione del costo del tour
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        String cost = df.format(((Tour)tourData.get("tour")).getCost()) + "€";
        tourCostTextView.setText(cost);

        // definizione della descrizione del tour
        tourDescriptionTextView.setText(((Tour) tourData.get("tour")).getDescription());

        // definizione delle tappe del tour
        final ArrayList<Location> locations = (ArrayList<Location>) tourData.get("locations");
        String locationString = getResources().getString(R.string.no_location);

        // se la lista delle tappe non è vuota allora inizializza la stringa
        if(!locations.isEmpty())
           locationString = "";

        // per ogni tappa presente nella lista si acquisisce la
        // via della tappa reale
        for (Location location : locations)
            locationString += getLocationFromCoords(location.getLatitude(), location.getLongitude()) + "\n";

        // definizione della visualizzazione della lista delle tappe
        tourLocationsTextView.setText(locationString);

        // oggetto per la definizione delle date
        GregorianCalendar date_event = new GregorianCalendar();
        String all_event_date = getResources().getString(R.string.tour_no_events);

        //definizione eventi del tour
        ArrayList<Event> events = (ArrayList<Event>) tourData.get("events");

        // se la lista degli eventi non è vuota allora inizializza la stringa
        if(!events.isEmpty())
            all_event_date = "";

        // per ogni evento presente nella lista
        for (Event event : events) {

            // se l'evento si sviluppa in una sola data allora mostra solo
            // il giorno di inizio, altrimenti mostra sia l'inizio che la fine
            if(event.getStartDate().equals(event.getEndDate())) {

                date_event.setTime( event.getStartDate().getTime());
                all_event_date += date_event.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                        months[date_event.get(GregorianCalendar.MONTH)] + " " +
                        date.get(GregorianCalendar.YEAR);

            } else {

                date_event.setTime(event.getStartDate().getTime());
                all_event_date += date_event.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                        months[date_event.get(GregorianCalendar.MONTH)] + " " +
                        date.get(GregorianCalendar.YEAR);

                date_event.setTime(event.getEndDate().getTime());
                all_event_date += " - " + date_event.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                        months[date_event.get(GregorianCalendar.MONTH)] + " " +
                        date_event.get(GregorianCalendar.YEAR);
            }

            all_event_date += " \n";
        }

        // definizione della visualizzazione della lista degli eventi
        tourEventsTextView.setText(all_event_date);

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


        // definizione dello username del cicerone
        final ArrayList<Feedback> feedbacks = (ArrayList<Feedback>) tourData.get("feedbacks");
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

        // definizione del listener per il click della voce alle info generiche
        tourGeneralInfoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // se non sono visibili allora mostrale
                if(generalInfoAreGone) {

                    tourNameTextView.setVisibility(View.VISIBLE);
                    tourNameDescrTextView.setVisibility(View.VISIBLE);
                    tourStartEndDateTextView.setVisibility(View.VISIBLE);
                    tourStartEndDateDescrTextView.setVisibility(View.VISIBLE);
                    tourCostTextView.setVisibility(View.VISIBLE);
                    tourCostDescrTextView.setVisibility(View.VISIBLE);
                    tourDescriptionTextView.setVisibility(View.VISIBLE);
                    tourDescriptionDescrTextView.setVisibility(View.VISIBLE);

                    if(!((Tour)tourData.get("tour")).getEndDate().before(Calendar.getInstance()))
                        editTourTextView.setVisibility(View.VISIBLE);
                    else
                        editTourTextView.setVisibility(View.GONE);

                    generalInfoArrow.setRotation((float)-90);

                    generalInfoAreGone = false;
                }

                // se sono visibili allora nascondile
                else {

                    tourNameTextView.setVisibility(View.GONE);
                    tourNameDescrTextView.setVisibility(View.GONE);
                    tourStartEndDateTextView.setVisibility(View.GONE);
                    tourStartEndDateDescrTextView.setVisibility(View.GONE);
                    tourCostTextView.setVisibility(View.GONE);
                    tourCostDescrTextView.setVisibility(View.GONE);
                    tourDescriptionTextView.setVisibility(View.GONE);
                    tourDescriptionDescrTextView.setVisibility(View.GONE);
                    editTourTextView.setVisibility(View.GONE);

                    generalInfoArrow.setRotation((float)0);

                    generalInfoAreGone = true;
                }
            }
        });

        // definizione del listener per il click della voce alle info delle tappe
        tourLocationsDescrTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // se non sono visibili allora mostrale
                if(locationInfoAreGone) {

                    tourLocationsTextView.setVisibility(View.VISIBLE);

                    if(!((Tour)tourData.get("tour")).getEndDate().before(Calendar.getInstance()))
                        editLocationTextView.setVisibility(View.VISIBLE);
                    else
                        editLocationTextView.setVisibility(View.GONE);

                    locationsInfoArrow.setRotation((float)-90);

                    locationInfoAreGone = false;
                }

                // se sono visibili allora nascondile
                else {

                    tourLocationsTextView.setVisibility(View.GONE);
                    editLocationTextView.setVisibility(View.GONE);

                    locationsInfoArrow.setRotation((float)0);

                    locationInfoAreGone = true;
                }
            }
        });

        // definizione del listener per il click della voce alle info degli eventi
        tourEventsDescrTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // se non sono visibili allora mostrale
                if(eventInfoAreGone) {

                    tourEventsTextView.setVisibility(View.VISIBLE);
                    tourLanguagesTextView.setVisibility(View.VISIBLE);
                    tourLanguagesDescrTextView.setVisibility(View.VISIBLE);

                    if(!((Tour)tourData.get("tour")).getEndDate().before(Calendar.getInstance()))
                        editEventTextView.setVisibility(View.VISIBLE);
                    else
                        editEventTextView.setVisibility(View.GONE);

                    eventsInfoArrow.setRotation((float)-90);

                    eventInfoAreGone = false;
                }

                // se sono visibili allora nascondile
                else {

                    tourEventsTextView.setVisibility(View.GONE);
                    tourLanguagesTextView.setVisibility(View.GONE);
                    tourLanguagesDescrTextView.setVisibility(View.GONE);
                    editEventTextView.setVisibility(View.GONE);

                    eventsInfoArrow.setRotation((float)0);

                    eventInfoAreGone = true;
                }
            }
        });

        // definizione del listener per il click della voce alle info delle recensioni
        tourFeedbackDescrTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // se non sono visibili allora mostrale
                if(feedbackInfoAreGone) {

                    tourFeedbackTextView.setVisibility(View.VISIBLE);

                    feedbacksInfoArrow.setRotation((float)-90);

                    feedbackInfoAreGone = false;
                }

                // se sono visibili allora nascondile
                else {

                    tourFeedbackTextView.setVisibility(View.GONE);

                    feedbacksInfoArrow.setRotation((float)0);

                    feedbackInfoAreGone = true;
                }
            }
        });

        // definizione del listener per il click della voce alla modifica del tour
        editTourTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragment = EditTourFragment.newInstance(db, (Tour)tourData.get("tour"));
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment, fragment).commit();

            }
        });

        // definizione del listener per il click della voce alla modifica dell'evento
        editEventTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragment = ShowEventsForManageFragment.newInstance(db,
                        (ArrayList<Event>)tourData.get("events"),
                        (Tour)tourData.get("tour"));
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment, fragment).commit();
            }
        });

        // definizione del listener per il click della voce alla modifica delle tappe
        editLocationTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // definizione della struttura per contenere le tappe
                // gia impostate da far vedere sulla mappa di google
                HashMap<String, Location> alreadySetLocation = new HashMap<>();

                // acquisizione della tappa gia impostata e inserimento nella struttura
                for(Location location : locations) {

                    alreadySetLocation.put(
                            getLocationFromCoords(location.getLatitude(), location.getLongitude()),
                            location);
                }

                // inserimento dei dati temporanei nel controller
                tourController.setTempLocations(alreadySetLocation);

                // apertura dell'activity della modifica delle tappe
                Intent intent = new Intent(view.getContext(), EditLocationActivity.class);
                intent.putExtra("TOUR_ID", tourID);
                intent.putExtra("CICERONE", db.getMyAccount().getUsername());
                startActivity(intent);

                // eliminazione del fragment attuale dallo stack
                // (evita il bug della doppia finestra consecutiva su
                // showTourCiceroneFragment)
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getFragmentManager().popBackStack();
                    }
                }, 2000);

            }
        });

        // definizione del listener del bottone dell'azione
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // acquisizione dell'attività
                Tour tour = (Tour)tourData.get("tour");

                // se si seleziona il bottone di cancellazione
                if (actionButton.getText().equals(getResources().getString(R.string.delete_tour))) {

                    // cambio del fragment
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragment = DeleteTourFragment.newInstance(db, tour.getId());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment, fragment).commit();
                }

                // se si seleziona il bottone di rinnovo
                else if (actionButton.getText().equals(getResources().getString(R.string.renew_tour))) {

                    // cambio del fragment
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragment = RenewTourFragment.newInstance(db, tour);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment, fragment).commit();
                }
            }
        });


        return view;
    }


    /*
    Metodo per acquisire la posizione esatta sotto forma di indirizzo dalle coordinate
     */
    private String getLocationFromCoords(float latitude, float longitude) {

        try {

            Geocoder geocoder = new Geocoder(view.getContext(), Locale.getDefault());

            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if(!addresses.isEmpty()) {

                String subThoro = "";
                String thoro = "...";
                String local = "ND";

                if(addresses.get(0).getSubThoroughfare() != null)
                    subThoro = addresses.get(0).getSubThoroughfare();

                if(addresses.get(0).getThoroughfare() != null)
                    thoro = addresses.get(0).getThoroughfare();

                if(addresses.get(0).getLocality() != null)
                    local = addresses.get(0).getLocality();

                return thoro + " " + subThoro + ", " + local;
            }



        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }
}
