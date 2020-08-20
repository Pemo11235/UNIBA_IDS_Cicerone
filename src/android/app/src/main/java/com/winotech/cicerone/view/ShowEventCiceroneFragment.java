package com.winotech.cicerone.view;

import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import android.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Language;
import com.winotech.cicerone.model.Location;
import com.winotech.cicerone.model.Request;
import com.winotech.cicerone.model.Subscribed;
import com.winotech.cicerone.model.Tour;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowEventCiceroneFragment extends Fragment {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller delle attività
    private static transient TourController tourController;

    // puntatore all'evento
    private Event event;

    // puntatore al tour
    private Tour tour;

    // oggetti grafici
    private TextView eventInfoTitleTextView;
    private TextView tourInfoTitleTextView;
    private TextView requestsTitleTextView;
    private TextView subscriptionsTitleTextView;

    private TextView eventDateSubtitleTextView;
    private TextView eventDescriptionSubtitleTextView;
    private TextView eventMaxSubsSubtitleTextView;
    private TextView eventLanguagesSubtitleTextView;
    private TextView tourNameSubtitleTextView;
    private TextView tourDateSubtitleTextView;
    private TextView tourCostSubtitleTextView;
    private TextView tourDescriptionSubtitleTextView;
    private TextView tourLocationsSubtitleTextView;

    private TextView eventDateTextView;
    private TextView eventDescriptionTextView;
    private TextView eventMaxSubsTextView;
    private TextView eventLanguagesTextView;
    private TextView tourNameTextView;
    private TextView tourDateTextView;
    private TextView tourCostTextView;
    private TextView tourDescriptionTextView;
    private TextView tourLocationsTextView;

    private LinearLayout requestsList;
    private LinearLayout subscriptionsList;

    private ImageView eventArrow;
    private ImageView tourArrow;
    private ImageView requestsArrow;
    private ImageView subscriptionsArrow;

    private TextView editEventTextView;
    private Button deleteEventButton;

    private boolean eventInfoAreGone = false;
    private boolean tourInfoAreGone = true;
    private boolean requestInfoAreGone = true;
    private boolean subscriptionInfoAreGone = true;

    // layout delle liste di utenti
    LayoutParams params;

    // settaggio della prima lettura del fragment.
    // E' utile per ricaricare i dati quando si aggiunge un nuovo evento
    private boolean firstLoad = true;





    /*
    Singleton del fragment
     */
    public static ShowEventCiceroneFragment newInstance(DBManager db, Event event, Tour tour) {

        ShowEventCiceroneFragment fragment = new ShowEventCiceroneFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.KEY_EVENT_DATA, event);
        bundle.putSerializable(Constants.KEY_TOUR_ID, tour);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_show_event_cicerone, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione dei parametri passati
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        event = (Event) getArguments().getSerializable(Constants.KEY_EVENT_DATA);
        tour = (Tour) getArguments().getSerializable(Constants.KEY_TOUR_ID);

        // inizializzazione del controller
        if(tourController == null)
            TourController.initInstance(getActivity());
        tourController = TourController.getInstance();

        // inizializzazione degli oggetti grafici
        eventInfoTitleTextView = view.findViewById(R.id.event_general_info_textView);
        tourInfoTitleTextView = view.findViewById(R.id.tour_general_info_textView);
        requestsTitleTextView = view.findViewById(R.id.requests_info_textView);
        subscriptionsTitleTextView = view.findViewById(R.id.subscription_info_textView);
        eventDateSubtitleTextView = view.findViewById(R.id.textView9);
        eventDescriptionSubtitleTextView = view.findViewById(R.id.textView12);
        eventMaxSubsSubtitleTextView = view.findViewById(R.id.textView10);
        eventLanguagesSubtitleTextView = view.findViewById(R.id.textView41);
        tourNameSubtitleTextView = view.findViewById(R.id.textView39);
        tourDateSubtitleTextView = view.findViewById(R.id.textView09);
        tourCostSubtitleTextView = view.findViewById(R.id.textView010);
        tourDescriptionSubtitleTextView = view.findViewById(R.id.textView012);
        tourLocationsSubtitleTextView = view.findViewById(R.id.locations_info_textView);
        eventDateTextView = view.findViewById(R.id.event_date_textView);
        eventDescriptionTextView = view.findViewById(R.id.event_description_textView);
        eventMaxSubsTextView = view.findViewById(R.id.event_max_subscribeds_textView);
        eventLanguagesTextView = view.findViewById(R.id.event_languages_textView);
        tourNameTextView = view.findViewById(R.id.tour_name_textView);
        tourDateTextView = view.findViewById(R.id.tour_date_textView);
        tourCostTextView = view.findViewById(R.id.tour_cost_textView);
        tourDescriptionTextView = view.findViewById(R.id.tour_description_textView);
        tourLocationsTextView = view.findViewById(R.id.tour_locations_textView);
        eventArrow = view.findViewById(R.id.event_general_info_arrow);
        tourArrow = view.findViewById(R.id.tour_general_info_arrow);
        requestsArrow = view.findViewById(R.id.requests_info_arrow);
        subscriptionsArrow = view.findViewById(R.id.subscription_info_arrow);
        editEventTextView = view.findViewById(R.id.edit_event_textView);
        deleteEventButton = view.findViewById(R.id.action_button);
        requestsList = view.findViewById(R.id.mainView_request);
        subscriptionsList = view.findViewById(R.id.mainView_subscription);

        // inizializzazione del layout della lista di oggetti (richieste ed iscrizioni)
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 80, 30, 0);

        // settaggio degli oggetti grafici inizialmente nascosti
        tourNameSubtitleTextView.setVisibility(View.GONE);
        tourNameTextView.setVisibility(View.GONE);
        tourDateSubtitleTextView.setVisibility(View.GONE);
        tourDateTextView.setVisibility(View.GONE);
        tourDescriptionSubtitleTextView.setVisibility(View.GONE);
        tourDescriptionTextView.setVisibility(View.GONE);
        tourCostSubtitleTextView.setVisibility(View.GONE);
        tourCostTextView.setVisibility(View.GONE);
        tourLocationsSubtitleTextView.setVisibility(View.GONE);
        tourLocationsTextView.setVisibility(View.GONE);
        requestsList.setVisibility(View.GONE);
        subscriptionsList.setVisibility(View.GONE);

        // oggetto per la definizione delle date
        GregorianCalendar date = new GregorianCalendar();

        // acquisizione dei nomi dei mesi
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();

        // definizione della data di validità dell'evento
        // se avviene in un solo giorno allora si mostra solo la data di inizio
        // altrimenti mostra sia la data di inizio che quella di fine
        if(event.getStartDate().equals(event.getEndDate())) {

            date.setTime(event.getStartDate().getTime());
            String dateText = date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);

            eventDateTextView.setText(dateText);

        } else {

            date.setTime(event.getStartDate().getTime());
            String dateText = date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);
            date.setTime(event.getEndDate().getTime());
            dateText += " - " + date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);

            eventDateTextView.setText(dateText);
        }

        // definizione della descrizione dell'evento
        eventDescriptionTextView.setText(event.getDescription());

        // definizione del numero massimo di iscritti dell'evento
        eventMaxSubsTextView.setText(Integer.toString(event.getMaxSubscribeds()));

        // definizione delle lingue parlate dell'evento
        ArrayList<Language> languages = event.getEventLanguages();
        if(!languages.isEmpty()) {

            Vector<String> languageNames = new Vector<>();
            for (Language language : languages) {

                if (!languageNames.contains(language.getName()))
                    languageNames.add(language.getName());
            }
            String languageString = "";
            for (String language : languageNames)
                languageString += language + "\n";
            eventLanguagesTextView.setText(languageString);

        } else {

            eventLanguagesTextView.setText(getResources().getString(R.string.no_spoken_languages));
        }

        // definizione del nome del tour
        tourNameTextView.setText(tour.getName());

        // definizione della data di validità del tour
        // se avviene in un solo giorno allora si mostra solo la data di inizio
        // altrimenti mostra sia la data di inizio che quella di fine
        if(tour.getStartDate().equals(tour.getEndDate())) {

            date.setTime(tour.getStartDate().getTime());
            String dateText = date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);

            tourDateTextView.setText(dateText);

        } else {

            date.setTime(tour.getStartDate().getTime());
            String dateText = date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);
            date.setTime(tour.getEndDate().getTime());
            dateText += " - " + date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);

            tourDateTextView.setText(dateText);
        }

        // definizione del costo del tour
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        String cost = df.format(tour.getCost()) + "€";
        tourCostTextView.setText(cost);

        // definizione della descrizione del tour
        tourDescriptionTextView.setText(tour.getDescription());

        // definizione delle tappe del tour
        ArrayList<Location> locations = tour.getTourLocations();
        String locationString = getResources().getString(R.string.no_location);
        if(!locations.isEmpty())
            locationString = "";
        for (Location location : locations)
            locationString += getLocationFromCoords(location.getLatitude(), location.getLongitude()) + "\n";
        tourLocationsTextView.setText(locationString);

        // definiizone della lista cliccabile delle richieste
        ArrayList<Request> requests = event.getEventRequests();
        for (final Request request : requests) {

            // definizione di una bottone
            Button requestBtn = new Button(view.getContext());

            // conversione in stringa della data
            Calendar createdOn = Calendar.getInstance();
            createdOn.setTime(tour.getStartDate().getTime());
            String dateText = createdOn.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                    createdOn.get(GregorianCalendar.MONTH) + "/" +
                    createdOn.get(GregorianCalendar.YEAR);

            // acqusiizione della stringa da stampare e sostituzione dei placeholder
            String text = getResources().getString(R.string.request_group_dimension);
            text = text.replace("{G_USER}", request.getGlobetrotter().getUsername());
            text = text.replace("{DATE}", dateText);
            text = text.replace("{GROUP_DIM}", Integer.toString(request.getGroupDimension()));

            // impostazione del layout
            requestBtn.setLayoutParams(params);
            requestBtn.setBackgroundColor(getResources().getColor(R.color.background));
            requestBtn.setTextColor(getResources().getColor(R.color.text_on_background));
            requestBtn.setText(text);
            requestBtn.setTextSize(20);
            requestBtn.setFocusable(true);
            requestBtn.setClickable(true);
            requestBtn.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

            // impostazione del listener del bottone
            requestBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragment = ManageRequestFragment.newInstance(db, request);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment, fragment).commit();
                }
            });

            requestsList.addView(requestBtn);
        }

        // definiizone della lista cliccabile delle richieste
        ArrayList<Subscribed> subscribeds = event.getEventSubscribeds();
        for (final Subscribed subscribed : subscribeds) {

            // definizione delle componenti grafiche della riga
            TextView subscribedTextView = new TextView(view.getContext());
            CircleImageView profileImage = new CircleImageView(view.getContext());
            LinearLayout layout = new LinearLayout(view.getContext());

            // definizione dell'effetto del click sulla riga
            TypedValue clickEffect = new TypedValue();
            view.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, clickEffect, true);

            // impostazione del layout dell'immagine
            Bitmap image = Bitmap.createScaledBitmap(
                    subscribed.getGlobetrotter().getPhoto().copy(Bitmap.Config.ARGB_8888, true),
                    250,
                    250,
                    false
            );
            profileImage.setLayoutParams(params);
            profileImage.setImageBitmap(image);
            profileImage.setBorderWidth(10);
            profileImage.setBorderColor(Color.BLACK);
            profileImage.setBackgroundResource(clickEffect.resourceId);
            profileImage.setFocusable(true);
            profileImage.setClickable(true);

            // impostazione del layout del testo
            subscribedTextView.setLayoutParams(params);
            subscribedTextView.setBackgroundColor(getResources().getColor(R.color.background));
            subscribedTextView.setTextColor(getResources().getColor(R.color.text_on_background));
            subscribedTextView.setText(subscribed.getGlobetrotter().getUsername());
            subscribedTextView.setTextSize(30);
            subscribedTextView.setBackgroundResource(clickEffect.resourceId);
            subscribedTextView.setFocusable(true);
            subscribedTextView.setClickable(true);
            subscribedTextView.setPadding(0,50,0,0);
            subscribedTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            // impostazione del layout della riga
            layout.setLayoutParams(params);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setBackgroundResource(clickEffect.resourceId);
            layout.setClickable(true);
            layout.setFocusable(true);
            layout.addView(profileImage);
            layout.addView(subscribedTextView);

            // impostazione del listener dell'immagine del profilo
            profileImage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragment = ManageSubscribedFragment.newInstance(db, subscribed);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment, fragment).commit();
                }
            });

            // impostazione del listener del testo
            subscribedTextView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragment = ManageSubscribedFragment.newInstance(db, subscribed);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment, fragment).commit();
                }
            });

            // impostazione del listener della riga
            layout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragment = ManageSubscribedFragment.newInstance(db, subscribed);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment, fragment).commit();
                }
            });

            subscriptionsList.addView(layout);
        }

        // settaggio della data massima di cancellazione dell'evento
        Calendar maxDate = new GregorianCalendar();
        maxDate.setTime(event.getStartDate().getTime());
        maxDate.add(Calendar.DAY_OF_MONTH, -3);
        Calendar today = Calendar.getInstance();

        // se si è superato il termine massimo di cancellazione dell'evento
        // allora il bottone di cancellazione non viene mostrato a video
        if(!today.before(maxDate))
            deleteEventButton.setVisibility(View.GONE);

        // settaggio della data massima di modifica dell'evento
        maxDate.setTime(event.getStartDate().getTime());
        maxDate.add(Calendar.DAY_OF_MONTH, -6);

        // se si è superato il termine massimo di modifica dell'evento
        // (ovvero 7 giorni prima dell'inizio dell'evento)
        // allora il bottone di modifica non viene mostrato a video
        if(!today.before(maxDate))
            editEventTextView.setVisibility(View.GONE);

        // definizione del listener del click sul titolo delle info degli eventi
        eventInfoTitleTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(eventInfoAreGone) {

                    eventDateSubtitleTextView.setVisibility(View.VISIBLE);
                    eventDateTextView.setVisibility(View.VISIBLE);
                    eventDescriptionSubtitleTextView.setVisibility(View.VISIBLE);
                    eventDescriptionTextView.setVisibility(View.VISIBLE);
                    eventMaxSubsSubtitleTextView.setVisibility(View.VISIBLE);
                    eventMaxSubsTextView.setVisibility(View.VISIBLE);
                    eventLanguagesSubtitleTextView.setVisibility(View.VISIBLE);
                    eventLanguagesTextView.setVisibility(View.VISIBLE);
                    editEventTextView.setVisibility(View.VISIBLE);

                    eventArrow.setRotation((float)-90);
                    eventInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    eventDateSubtitleTextView.setVisibility(View.GONE);
                    eventDateTextView.setVisibility(View.GONE);
                    eventDescriptionSubtitleTextView.setVisibility(View.GONE);
                    eventDescriptionTextView.setVisibility(View.GONE);
                    eventMaxSubsSubtitleTextView.setVisibility(View.GONE);
                    eventMaxSubsTextView.setVisibility(View.GONE);
                    eventLanguagesSubtitleTextView.setVisibility(View.GONE);
                    eventLanguagesTextView.setVisibility(View.GONE);
                    editEventTextView.setVisibility(View.GONE);

                    eventArrow.setRotation((float)0);
                    eventInfoAreGone = true;
                }
            }
        });

        // definizione del listener del click sul titolo delle info del tour
        tourInfoTitleTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(tourInfoAreGone) {

                    tourNameSubtitleTextView.setVisibility(View.VISIBLE);
                    tourNameTextView.setVisibility(View.VISIBLE);
                    tourDateSubtitleTextView.setVisibility(View.VISIBLE);
                    tourDateTextView.setVisibility(View.VISIBLE);
                    tourDescriptionSubtitleTextView.setVisibility(View.VISIBLE);
                    tourDescriptionTextView.setVisibility(View.VISIBLE);
                    tourCostSubtitleTextView.setVisibility(View.VISIBLE);
                    tourCostTextView.setVisibility(View.VISIBLE);
                    tourLocationsSubtitleTextView.setVisibility(View.VISIBLE);
                    tourLocationsTextView.setVisibility(View.VISIBLE);

                    tourArrow.setRotation((float)-90);
                    tourInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    tourNameSubtitleTextView.setVisibility(View.GONE);
                    tourNameTextView.setVisibility(View.GONE);
                    tourDateSubtitleTextView.setVisibility(View.GONE);
                    tourDateTextView.setVisibility(View.GONE);
                    tourDescriptionSubtitleTextView.setVisibility(View.GONE);
                    tourDescriptionTextView.setVisibility(View.GONE);
                    tourCostSubtitleTextView.setVisibility(View.GONE);
                    tourCostTextView.setVisibility(View.GONE);
                    tourLocationsSubtitleTextView.setVisibility(View.GONE);
                    tourLocationsTextView.setVisibility(View.GONE);

                    tourArrow.setRotation((float)0);
                    tourInfoAreGone = true;
                }
            }
        });


        // definizione del listener del click sul titolo delle info degli eventi
        requestsTitleTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(requestInfoAreGone) {

                    requestsList.setVisibility(View.VISIBLE);

                    requestsArrow.setRotation((float)-90);
                    requestInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    requestsList.setVisibility(View.GONE);

                    requestsArrow.setRotation((float)0);
                    requestInfoAreGone = true;
                }
            }
        });

        // definizione del listener del click sul titolo delle info degli iscritti
        subscriptionsTitleTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(subscriptionInfoAreGone) {

                    subscriptionsList.setVisibility(View.VISIBLE);

                    subscriptionsArrow.setRotation((float)-90);
                    subscriptionInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    subscriptionsList.setVisibility(View.GONE);

                    subscriptionsArrow.setRotation((float)0);
                    subscriptionInfoAreGone = true;
                }
            }
        });

        // definizione del listener del click sul bottone di modifica dell'evento
        editEventTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragment = EditEventFragment.newInstance(db, event);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment, fragment).commit();
            }
        });

        // definizione del listener del click sul bottone di cancellazione dell'evento
        deleteEventButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragment = DeleteEventFragment.newInstance(db, event);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment, fragment).commit();
            }
        });




        return view;
    }


    @Override
    public void onResume(){

        super.onResume();

        // se è il primo accesso al fragment (ovvero è stato
        // appena creato) allora setta il flag a falso
        if(firstLoad)
            firstLoad = false;

        // altrimenti ricarica i dati dal server per aggiornarli
        // ed aggiorna i dati degli oggetti grafici
        else {

            // ricaricamento dei dati acquisendoli direttamente dal server
            HashMap<String, Object> reloadedData = tourController.getTourFromID(event.getTour().getId());
            ArrayList<Event> tempEventList = (ArrayList<Event>)reloadedData.get("events");
            for(Event temp : tempEventList){

                if(temp.getId() == event.getId()) {

                    event.setDescription(temp.getDescription());
                    event.setMaxSubscribeds(temp.getMaxSubscribeds());
                    event.setStartDate(temp.getStartDate());
                    event.setEndDate(temp.getEndDate());

                    event.getEventRequests().clear();
                    for(Request request : temp.getEventRequests())
                        event.addEventRequests(request);

                    event.getEventLanguages().clear();
                    for(Language language : temp.getEventLanguages())
                        event.addEventLanguages(language);

                    event.getEventSubscribeds().clear();
                    for(Subscribed subscribed : temp.getEventSubscribeds())
                        event.addEventSubscribeds(subscribed);

                    break;
                }
            }
            tempEventList.clear();
            reloadedData.clear();

            // oggetto per la definizione delle date
            GregorianCalendar date = new GregorianCalendar();

            // acquisizione dei nomi dei mesi
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] months = dfs.getMonths();

            // definizione della data di validità dell'evento
            // se avviene in un solo giorno allora si mostra solo la data di inizio
            // altrimenti mostra sia la data di inizio che quella di fine
            if(event.getStartDate().equals(event.getEndDate())) {

                date.setTime(event.getStartDate().getTime());
                String dateText = date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                        months[date.get(GregorianCalendar.MONTH)] + " " +
                        date.get(GregorianCalendar.YEAR);

                eventDateTextView.setText(dateText);

            } else {

                date.setTime(event.getStartDate().getTime());
                String dateText = date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                        months[date.get(GregorianCalendar.MONTH)] + " " +
                        date.get(GregorianCalendar.YEAR);
                date.setTime(event.getEndDate().getTime());
                dateText += " - " + date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                        months[date.get(GregorianCalendar.MONTH)] + " " +
                        date.get(GregorianCalendar.YEAR);

                eventDateTextView.setText(dateText);
            }

            // definizione della descrizione dell'evento
            eventDescriptionTextView.setText(event.getDescription());

            // definizione del numero massimo di iscritti dell'evento
            eventMaxSubsTextView.setText(Integer.toString(event.getMaxSubscribeds()));

            // ridefinizione delle liste di richieste
            requestsList.removeAllViews();
            for (final Request request : event.getEventRequests()) {

                // definizione di una bottone
                Button requestBtn = new Button(view.getContext());

                // conversione in stringa della data
                Calendar createdOn = Calendar.getInstance();
                createdOn.setTime(tour.getStartDate().getTime());
                String dateText = createdOn.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                        createdOn.get(GregorianCalendar.MONTH) + "/" +
                        createdOn.get(GregorianCalendar.YEAR);

                // acqusiizione della stringa da stampare e sostituzione dei placeholder
                String text = getResources().getString(R.string.request_group_dimension);
                text = text.replace("{G_USER}", request.getGlobetrotter().getUsername());
                text = text.replace("{DATE}", dateText);
                text = text.replace("{GROUP_DIM}", Integer.toString(request.getGroupDimension()));

                // impostazione del layout
                requestBtn.setLayoutParams(params);
                requestBtn.setBackgroundColor(getResources().getColor(R.color.background));
                requestBtn.setTextColor(getResources().getColor(R.color.text_on_background));
                requestBtn.setText(text);
                requestBtn.setTextSize(20);
                requestBtn.setFocusable(true);
                requestBtn.setClickable(true);
                requestBtn.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

                // impostazione del listener del bottone
                requestBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        Fragment fragment = ManageRequestFragment.newInstance(db, request);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.fragment, fragment).commit();
                    }
                });

                requestsList.addView(requestBtn);
            }

            // ridefinizione delle liste di iscritti
            subscriptionsList.removeAllViews();
            for (final Subscribed subscribed : event.getEventSubscribeds()) {

                // definizione delle componenti grafiche della riga
                TextView subscribedTextView = new TextView(view.getContext());
                CircleImageView profileImage = new CircleImageView(view.getContext());
                LinearLayout layout = new LinearLayout(view.getContext());

                // definizione dell'effetto del click sulla riga
                TypedValue clickEffect = new TypedValue();
                view.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, clickEffect, true);

                // impostazione del layout dell'immagine
                Bitmap image = Bitmap.createScaledBitmap(
                        subscribed.getGlobetrotter().getPhoto().copy(Bitmap.Config.ARGB_8888, true),
                        250,
                        250,
                        false
                );
                profileImage.setLayoutParams(params);
                profileImage.setImageBitmap(image);
                profileImage.setBorderWidth(10);
                profileImage.setBorderColor(Color.BLACK);
                profileImage.setBackgroundResource(clickEffect.resourceId);
                profileImage.setFocusable(true);
                profileImage.setClickable(true);

                // impostazione del layout del testo
                subscribedTextView.setLayoutParams(params);
                subscribedTextView.setBackgroundColor(getResources().getColor(R.color.background));
                subscribedTextView.setTextColor(getResources().getColor(R.color.text_on_background));
                subscribedTextView.setText(subscribed.getGlobetrotter().getUsername());
                subscribedTextView.setTextSize(30);
                subscribedTextView.setBackgroundResource(clickEffect.resourceId);
                subscribedTextView.setFocusable(true);
                subscribedTextView.setClickable(true);
                subscribedTextView.setPadding(0,50,0,0);
                subscribedTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                // impostazione del layout della riga
                layout.setLayoutParams(params);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setBackgroundResource(clickEffect.resourceId);
                layout.setClickable(true);
                layout.setFocusable(true);
                layout.addView(profileImage);
                layout.addView(subscribedTextView);

                // impostazione del listener dell'immagine del profilo
                profileImage.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        Fragment fragment = ManageSubscribedFragment.newInstance(db, subscribed);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.fragment, fragment).commit();
                    }
                });

                // impostazione del listener del testo
                subscribedTextView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        Fragment fragment = ManageSubscribedFragment.newInstance(db, subscribed);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.fragment, fragment).commit();
                    }
                });

                // impostazione del listener della riga
                layout.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        Fragment fragment = ManageSubscribedFragment.newInstance(db, subscribed);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.fragment, fragment).commit();
                    }
                });

                subscriptionsList.addView(layout);
            }

        }

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
