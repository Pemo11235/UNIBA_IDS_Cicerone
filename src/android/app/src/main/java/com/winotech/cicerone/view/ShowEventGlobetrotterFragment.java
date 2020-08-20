package com.winotech.cicerone.view;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Cicerone;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Feedback;
import com.winotech.cicerone.model.Language;
import com.winotech.cicerone.model.Location;
import com.winotech.cicerone.model.Subscribed;
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

import de.hdodenhof.circleimageview.CircleImageView;


public class ShowEventGlobetrotterFragment extends Fragment implements Serializable {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller delle attività
    private static transient TourController tourController;

    // oggetti grafici del fragment
    private TextView groupDimensionTextView;
    private TextView hasPayedTextView;
    private TextView eventDateTextView;
    private TextView eventDescriptionTextView;
    private TextView eventMaxSubscribedTextView;
    private TextView eventSpokenLanguagesTextView;
    private TextView tourNameTextView;
    private TextView tourStartEndDateTextView;
    private TextView tourCostTextView;
    private TextView tourDescriptionTextView;
    private TextView tourLocationsTextView;
    transient private CircleImageView profileCiceroneImage;
    private TextView usernameCiceroneTextView;
    private TextView firstLastNameCiceroneTextView;
    private TextView emailCiceroneTextView;
    private TextView phoneNumberCiceroneTextView;
    private TextView biographyCiceroneTextView;
    private TextView tourFeedbackTextView;
    private TextView myFeedbackTextView;
    private TextView myFeedbackCreatedOnTextView;
    private RatingBar myFeedbackRatingTextView;

    private TextView groupDimensionDescrTextView;
    private TextView hasPayedDescrTextView;
    private TextView eventDateDescrTextView;
    private TextView eventDescriptionDescrTextView;
    private TextView eventMaxSubscribedDescrTextView;
    private TextView eventSpokenLanguagesDescrTextView;
    private TextView tourNameDescrTextView;
    private TextView tourStartEndDateDescrTextView;
    private TextView tourCostDescrTextView;
    private TextView tourDescriptionDescrTextView;
    private TextView tourLocationsDescrTextView;
    private LinearLayout ciceroneHeader;
    private TextView firstLastNameCiceroneDescrTextView;
    private TextView emailCiceroneDescrTextView;
    private TextView phoneNumberCiceroneDescrTextView;
    private TextView biographyCiceroneDescrTextView;

    private TextView subscriptionInfoTitleTextView;
    private TextView eventsInfoTitleTextView;
    private TextView tourInfoTitleTextView;
    private TextView ciceroneInfoTitleTextView;
    private TextView feedbackInfoTitleTextView;
    private TextView myFeedbackInfoTitleTextView;

    private ImageView subscriptionInfoArrow;
    private ImageView eventsInfoArrow;
    private ImageView tourInfoArrow;
    private ImageView ciceroneInfoArrow;
    private ImageView feedbackInfoArrow;
    private ImageView myFeedbackInfoArrow;

    private Button makePaymentButton;
    private Button actionOnSubscriptionButton;

    private boolean subscriptionInfoAreGone = false;
    private boolean eventInfoAreGone = true;
    private boolean tourInfoAreGone = true;
    private boolean ciceroneInfoAreGone = true;
    private boolean feedbackInfoAreGone = true;
    private boolean myFeedbackInfoAreGone = true;

    // puntatore alla iscrizione
    Subscribed subscribed;

    // puntatore all'evento
    Event event;

    // puntatore al tour di appartenenza
    Tour tour;

    // definizione del mio feedback
    Feedback myFeedback = null;

    // id dell'evento
    int eventID;

    // settaggio della prima lettura del fragment.
    // E' utile per ricaricare i dati quando si aggiunge un nuovo evento
    private boolean firstLoad = true;

    private boolean selectedButton = false;


    /*
    Singleton del fragment
     */
    public static ShowEventGlobetrotterFragment newInstance(DBManager db, int id) {

        ShowEventGlobetrotterFragment fragment = new ShowEventGlobetrotterFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable("id", id);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_show_event_globetrotter, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione del db
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        eventID = (int) getArguments().getSerializable("id");

        // inizializzazione del controller
        if(tourController == null)
            TourController.initInstance(getActivity());
        tourController = TourController.getInstance();

        // acquisizione dei dati completi dell'attività
        Event tempEvent = tourController.getEventFromID(eventID);
        HashMap<String, Object> tourData = new HashMap<>(tourController.getTourFromID(tempEvent.getTour().getId()));

        // acquisizione della richiesta
        final ArrayList<Subscribed> subscribeds = (ArrayList<Subscribed>)tourData.get("subscribeds");
        for(Subscribed sub : subscribeds) {

            if(sub.getGlobetrotter().getUsername().equals(db.getMyAccount().getUsername()) &&
                    sub.getEvent().getId() == eventID) {

                subscribed = sub;
                break;
            }
        }
        subscribeds.clear();

        // acquisizione dell'evento
        ArrayList<Event> events = (ArrayList<Event>)tourData.get("events");
        for(Event ev : events) {

            if(ev.getId() == eventID) {

                event = ev;
                break;
            }
        }
        events.clear();

        // acquisizione dell'attività
        tour = (Tour)tourData.get("tour");

        // acquisizione del cicerone
        Cicerone cicerone = (Cicerone)tourData.get("cicerone");


        // inizializzazione degli oggetti grafici
        groupDimensionTextView = view.findViewById(R.id.subscribed_group_dimension_textView);
        hasPayedTextView = view.findViewById(R.id.subscribed_has_payed_textView);
        eventDateTextView = view.findViewById(R.id.event_date_textView);
        eventDescriptionTextView = view.findViewById(R.id.event_description_textView);
        eventMaxSubscribedTextView = view.findViewById(R.id.event_max_subscribeds_textView);
        eventSpokenLanguagesTextView = view.findViewById(R.id.event_languages_textView);
        tourNameTextView = view.findViewById(R.id.tour_name_textView);
        tourStartEndDateTextView = view.findViewById(R.id.tour_date_textView);
        tourCostTextView = view.findViewById(R.id.tour_cost_textView);
        tourDescriptionTextView = view.findViewById(R.id.tour_description_textView);
        tourLocationsTextView = view.findViewById(R.id.tour_locations_textView);
        profileCiceroneImage = view.findViewById(R.id.imageView4);
        usernameCiceroneTextView = view.findViewById(R.id.username_textView);
        firstLastNameCiceroneTextView = view.findViewById(R.id.first_last_name_textView);
        emailCiceroneTextView = view.findViewById(R.id.email_textView);
        phoneNumberCiceroneTextView = view.findViewById(R.id.phone_number_textView);
        biographyCiceroneTextView = view.findViewById(R.id.biography_textView);
        tourFeedbackTextView = view.findViewById(R.id.tour_feedbacks_textView);
        myFeedbackTextView = view.findViewById(R.id.my_feedback_textView);
        myFeedbackCreatedOnTextView = view.findViewById(R.id.my_feedback_created_on_textView);
        myFeedbackRatingTextView = view.findViewById(R.id.my_feedback_rating_textView);
        groupDimensionDescrTextView = view.findViewById(R.id.textView50);
        hasPayedDescrTextView = view.findViewById(R.id.textView51);
        eventDateDescrTextView = view.findViewById(R.id.textView9);
        eventDescriptionDescrTextView = view.findViewById(R.id.textView12);
        eventMaxSubscribedDescrTextView = view.findViewById(R.id.textView10);
        eventSpokenLanguagesDescrTextView = view.findViewById(R.id.textView41);
        tourNameDescrTextView = view.findViewById(R.id.textView39);
        tourStartEndDateDescrTextView = view.findViewById(R.id.textView09);
        tourCostDescrTextView = view.findViewById(R.id.textView010);
        tourDescriptionDescrTextView = view.findViewById(R.id.textView012);
        tourLocationsDescrTextView = view.findViewById(R.id.locations_info_textView);
        ciceroneHeader = view.findViewById(R.id.globetrotter_header);
        firstLastNameCiceroneDescrTextView = view.findViewById(R.id.textView55);
        emailCiceroneDescrTextView = view.findViewById(R.id.textView56);
        phoneNumberCiceroneDescrTextView = view.findViewById(R.id.textView57);
        biographyCiceroneDescrTextView = view.findViewById(R.id.textView52);
        subscriptionInfoTitleTextView = view.findViewById(R.id.subscription_info_textView);
        eventsInfoTitleTextView = view.findViewById(R.id.event_general_info_textView);
        tourInfoTitleTextView = view.findViewById(R.id.tour_general_info_textView);
        ciceroneInfoTitleTextView = view.findViewById(R.id.cicerone_info_textView);
        feedbackInfoTitleTextView = view.findViewById(R.id.feedbacks_info_textView);
        myFeedbackInfoTitleTextView = view.findViewById(R.id.my_feedback_info_textView);
        subscriptionInfoArrow = view.findViewById(R.id.subscription_info_arrow);
        eventsInfoArrow = view.findViewById(R.id.event_general_info_arrow);
        tourInfoArrow = view.findViewById(R.id.tour_general_info_arrow);
        ciceroneInfoArrow = view.findViewById(R.id.cicerone_info_arrow);
        feedbackInfoArrow = view.findViewById(R.id.feedbacks_info_arrow);
        myFeedbackInfoArrow = view.findViewById(R.id.my_feedback_info_arrow);
        makePaymentButton = view.findViewById(R.id.make_payment_button);
        actionOnSubscriptionButton = view.findViewById(R.id.action_on_subscription_button);

        // impostazione della visibilità iniziale
        eventDateTextView.setVisibility(View.GONE);
        eventDescriptionTextView.setVisibility(View.GONE);
        eventMaxSubscribedTextView.setVisibility(View.GONE);
        eventSpokenLanguagesTextView.setVisibility(View.GONE);
        tourNameTextView.setVisibility(View.GONE);
        tourStartEndDateTextView.setVisibility(View.GONE);
        tourCostTextView.setVisibility(View.GONE);
        tourDescriptionTextView.setVisibility(View.GONE);
        tourLocationsTextView.setVisibility(View.GONE);
        firstLastNameCiceroneTextView.setVisibility(View.GONE);
        emailCiceroneTextView.setVisibility(View.GONE);
        phoneNumberCiceroneTextView.setVisibility(View.GONE);
        biographyCiceroneTextView.setVisibility(View.GONE);
        tourFeedbackTextView.setVisibility(View.GONE);
        eventDateDescrTextView.setVisibility(View.GONE);
        eventDescriptionDescrTextView.setVisibility(View.GONE);
        eventMaxSubscribedDescrTextView.setVisibility(View.GONE);
        eventSpokenLanguagesDescrTextView.setVisibility(View.GONE);
        tourNameDescrTextView.setVisibility(View.GONE);
        tourStartEndDateDescrTextView.setVisibility(View.GONE);
        tourCostDescrTextView.setVisibility(View.GONE);
        tourDescriptionDescrTextView.setVisibility(View.GONE);
        tourLocationsDescrTextView.setVisibility(View.GONE);
        ciceroneHeader.setVisibility(View.GONE);
        firstLastNameCiceroneDescrTextView.setVisibility(View.GONE);
        emailCiceroneDescrTextView.setVisibility(View.GONE);
        phoneNumberCiceroneDescrTextView.setVisibility(View.GONE);
        biographyCiceroneDescrTextView.setVisibility(View.GONE);
        myFeedbackTextView.setVisibility(View.GONE);
        myFeedbackCreatedOnTextView.setVisibility(View.GONE);
        myFeedbackRatingTextView.setVisibility(View.GONE);

        // se l'utente ha pagato o il tour è gratis non mostrare il bottone
        if(tour.getCost() > 0 && subscribed.getHasPayed())
            makePaymentButton.setVisibility(View.GONE);
        else if (tour.getCost() <= 0)
            makePaymentButton.setVisibility(View.GONE);

        // se l'evento è gia passato allora mostra il messaggio di gestione della recensione
        if(subscribed.getEvent().getEndDate().before(Calendar.getInstance()))
            actionOnSubscriptionButton.setText(getResources().getString(R.string.my_feedback_title));

        // acquisizione dei nomi dei mesi
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();

        // oggetto per la definizione delle date
        GregorianCalendar date = new GregorianCalendar();

        // definizione della dimensione del gruppo prenotato
        groupDimensionTextView.setText(Integer.toString(subscribed.getGroupDimension()));

        // definizione della stringa sul pagamento effettuato
        String hasPayedText;
        if(subscribed.getEvent().getTour().getCost() > 0) {

            if(subscribed.getHasPayed())
                hasPayedText = getResources().getString(R.string.you_have_payed);
            else
                hasPayedText = getResources().getString(R.string.you_have_not_payed);

            // definizione della data di scadenza del pagamento
            Calendar ultimatumDate = Calendar.getInstance();
            ultimatumDate.setTime(event.getStartDate().getTime());
            ultimatumDate.add(GregorianCalendar.DAY_OF_MONTH, -4);
            String dateText = ultimatumDate.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                    (ultimatumDate.get(GregorianCalendar.MONTH) + 1) + "/" +
                    ultimatumDate.get(GregorianCalendar.YEAR);
            hasPayedText = hasPayedText.replace("{DATE}", dateText);

        } else {

            hasPayedText = getResources().getString(R.string.event_free_cost_description);
        }
        hasPayedTextView.setText(hasPayedText);

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

        // definizione degli iscritti massimi all'evento
        eventMaxSubscribedTextView.setText(Integer.toString(event.getMaxSubscribeds()));

        // definizione delle lingue parlate nell'evento
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
            eventSpokenLanguagesTextView.setText(languageString);

        } else {

            eventSpokenLanguagesTextView.setText(getResources().getString(R.string.no_spoken_languages));
        }

        // definizione del nome del tour
        tourNameTextView.setText(((Tour) tourData.get("tour")).getName());

        // definizione della data di validità del tour
        // se avviene in un solo giorno allora si mostra solo la data di inizio
        // altrimenti mostra sia la data di inizio che quella di fine
        if(tour.getStartDate().equals(tour.getEndDate())) {

            date.setTime(tour.getStartDate().getTime());
            String dateText = date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);
            tourStartEndDateTextView.setText(dateText);

        } else {

            date.setTime(tour.getStartDate().getTime());
            String dateText = date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);
            date.setTime(tour.getEndDate().getTime());
            dateText += " - " + date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);
            tourStartEndDateTextView.setText(dateText);
        }

        // definizione del costo del tour
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        String cost = df.format(((Tour)tourData.get("tour")).getCost()) + "€";
        tourCostTextView.setText(cost);

        // definizione della descrizione del tour
        tourDescriptionTextView.setText(((Tour) tourData.get("tour")).getDescription());

        // definizione della descrizione del tour
        eventDescriptionTextView.setText(event.getDescription());

        // definizione delle tappe del tour
        ArrayList<Location> locations = (ArrayList<Location>) tourData.get("locations");
        String locationString = "";
        for (Location location : locations)
            locationString += getLocationFromCoords(location.getLatitude(), location.getLongitude()) + "\n";
        tourLocationsTextView.setText(locationString);

        // definizione dell'immagine del profilo del cicerone
        profileCiceroneImage.setImageBitmap(cicerone.getPhoto());

        // definizione dello username del cicerone
        usernameCiceroneTextView.setText(cicerone.getUsername());

        // definizione del nome e del congome del cicerone
        final String firstLastName = cicerone.getFirstName() + " " + cicerone.getLastName();
        firstLastNameCiceroneTextView.setText(firstLastName);

        // definizione della email del cicerone
        emailCiceroneTextView.setText(cicerone.getEmail());

        // definizione del numeor di telefono del cicerone
        phoneNumberCiceroneTextView.setText(cicerone.getPhone());

        // definizione della biografia del cicerone
        biographyCiceroneTextView.setText(cicerone.getBiography());

        // definizione delle recensioni sul tour
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

            // definizione del mio feedback
            for(Feedback feed : feedbacks) {

                if (feed.getGlobetrotter().getUsername().equals(db.getMyAccount().getUsername())) {

                    myFeedback = feed;
                    break;
                }
            }

        } else {

            String feedbackString = getResources().getString(R.string.no_feedback);
            tourFeedbackTextView.setText(Html.fromHtml(feedbackString));
        }

        // se non ci sono feedback nonfar visualizzare la voce del menu
        if(myFeedback == null){

            myFeedbackInfoTitleTextView.setVisibility(View.GONE);
            myFeedbackInfoArrow.setVisibility(View.GONE);
            myFeedbackTextView.setVisibility(View.GONE);
            myFeedbackRatingTextView.setVisibility(View.GONE);
            myFeedbackCreatedOnTextView.setVisibility(View.GONE);

        } else {

            // definiizone del contenuto
            myFeedbackTextView.setText(myFeedback.getDescription());

            // definizione del punteggio
            myFeedbackRatingTextView.setRating((float)myFeedback.getRate());

            // definizione della data
            Calendar createdOn = Calendar.getInstance();
            createdOn.setTime(myFeedback.getCreatedOn().getTime());
            String dateText = createdOn.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[createdOn.get(GregorianCalendar.MONTH)] + " " +
                    createdOn.get(GregorianCalendar.YEAR);
            myFeedbackCreatedOnTextView.setText(dateText);
        }

        // definizione del listener del titolo dell'iscrizione
        subscriptionInfoTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(subscriptionInfoAreGone) {

                    groupDimensionTextView.setVisibility(View.VISIBLE);
                    groupDimensionDescrTextView.setVisibility(View.VISIBLE);
                    hasPayedTextView.setVisibility(View.VISIBLE);
                    hasPayedDescrTextView.setVisibility(View.VISIBLE);

                    subscriptionInfoArrow.setRotation((float)-90);
                    subscriptionInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    groupDimensionTextView.setVisibility(View.GONE);
                    groupDimensionDescrTextView.setVisibility(View.GONE);
                    hasPayedTextView.setVisibility(View.GONE);
                    hasPayedDescrTextView.setVisibility(View.GONE);

                    subscriptionInfoArrow.setRotation((float)0);
                    subscriptionInfoAreGone = true;
                }
            }
        });
        subscriptionInfoArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(subscriptionInfoAreGone) {

                    groupDimensionTextView.setVisibility(View.VISIBLE);
                    groupDimensionDescrTextView.setVisibility(View.VISIBLE);
                    hasPayedTextView.setVisibility(View.VISIBLE);
                    hasPayedDescrTextView.setVisibility(View.VISIBLE);

                    subscriptionInfoArrow.setRotation((float)-90);
                    subscriptionInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    groupDimensionTextView.setVisibility(View.GONE);
                    groupDimensionDescrTextView.setVisibility(View.GONE);
                    hasPayedTextView.setVisibility(View.GONE);
                    hasPayedDescrTextView.setVisibility(View.GONE);

                    subscriptionInfoArrow.setRotation((float)0);
                    subscriptionInfoAreGone = true;
                }
            }
        });

        // definizione del listener del titolo degli eventi
        eventsInfoTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(eventInfoAreGone) {

                    eventDateTextView.setVisibility(View.VISIBLE);
                    eventDateDescrTextView.setVisibility(View.VISIBLE);
                    eventDescriptionTextView.setVisibility(View.VISIBLE);
                    eventDescriptionDescrTextView.setVisibility(View.VISIBLE);
                    eventMaxSubscribedTextView.setVisibility(View.VISIBLE);
                    eventMaxSubscribedDescrTextView.setVisibility(View.VISIBLE);
                    eventSpokenLanguagesTextView.setVisibility(View.VISIBLE);
                    eventSpokenLanguagesDescrTextView.setVisibility(View.VISIBLE);

                    eventsInfoArrow.setRotation((float)-90);
                    eventInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    eventDateTextView.setVisibility(View.GONE);
                    eventDateDescrTextView.setVisibility(View.GONE);
                    eventDescriptionTextView.setVisibility(View.GONE);
                    eventDescriptionDescrTextView.setVisibility(View.GONE);
                    eventMaxSubscribedTextView.setVisibility(View.GONE);
                    eventMaxSubscribedDescrTextView.setVisibility(View.GONE);
                    eventSpokenLanguagesTextView.setVisibility(View.GONE);
                    eventSpokenLanguagesDescrTextView.setVisibility(View.GONE);

                    eventsInfoArrow.setRotation((float)0);
                    eventInfoAreGone = true;
                }
            }
        });
        eventsInfoArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(eventInfoAreGone) {

                    eventDateTextView.setVisibility(View.VISIBLE);
                    eventDateDescrTextView.setVisibility(View.VISIBLE);
                    eventDescriptionTextView.setVisibility(View.VISIBLE);
                    eventDescriptionDescrTextView.setVisibility(View.VISIBLE);
                    eventMaxSubscribedTextView.setVisibility(View.VISIBLE);
                    eventMaxSubscribedDescrTextView.setVisibility(View.VISIBLE);
                    eventSpokenLanguagesTextView.setVisibility(View.VISIBLE);
                    eventSpokenLanguagesDescrTextView.setVisibility(View.VISIBLE);

                    eventsInfoArrow.setRotation((float)-90);
                    eventInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    eventDateTextView.setVisibility(View.GONE);
                    eventDateDescrTextView.setVisibility(View.GONE);
                    eventDescriptionTextView.setVisibility(View.GONE);
                    eventDescriptionDescrTextView.setVisibility(View.GONE);
                    eventMaxSubscribedTextView.setVisibility(View.GONE);
                    eventMaxSubscribedDescrTextView.setVisibility(View.GONE);
                    eventSpokenLanguagesTextView.setVisibility(View.GONE);
                    eventSpokenLanguagesDescrTextView.setVisibility(View.GONE);

                    eventsInfoArrow.setRotation((float)0);
                    eventInfoAreGone = true;
                }
            }
        });

        // definizione del listener del titolo degli eventi
        tourInfoTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(tourInfoAreGone) {

                    tourNameTextView.setVisibility(View.VISIBLE);
                    tourNameDescrTextView.setVisibility(View.VISIBLE);
                    tourStartEndDateTextView.setVisibility(View.VISIBLE);
                    tourStartEndDateDescrTextView.setVisibility(View.VISIBLE);
                    tourDescriptionTextView.setVisibility(View.VISIBLE);
                    tourDescriptionDescrTextView.setVisibility(View.VISIBLE);
                    tourCostTextView.setVisibility(View.VISIBLE);
                    tourCostDescrTextView.setVisibility(View.VISIBLE);
                    tourLocationsTextView.setVisibility(View.VISIBLE);
                    tourLocationsDescrTextView.setVisibility(View.VISIBLE);

                    tourInfoArrow.setRotation((float)-90);
                    tourInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    tourNameTextView.setVisibility(View.GONE);
                    tourNameDescrTextView.setVisibility(View.GONE);
                    tourStartEndDateTextView.setVisibility(View.GONE);
                    tourStartEndDateDescrTextView.setVisibility(View.GONE);
                    tourDescriptionTextView.setVisibility(View.GONE);
                    tourDescriptionDescrTextView.setVisibility(View.GONE);
                    tourCostTextView.setVisibility(View.GONE);
                    tourCostDescrTextView.setVisibility(View.GONE);
                    tourLocationsTextView.setVisibility(View.GONE);
                    tourLocationsDescrTextView.setVisibility(View.GONE);

                    tourInfoArrow.setRotation((float)0);
                    tourInfoAreGone = true;
                }
            }
        });
        tourInfoArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(tourInfoAreGone) {

                    tourNameTextView.setVisibility(View.VISIBLE);
                    tourNameDescrTextView.setVisibility(View.VISIBLE);
                    tourStartEndDateTextView.setVisibility(View.VISIBLE);
                    tourStartEndDateDescrTextView.setVisibility(View.VISIBLE);
                    tourDescriptionTextView.setVisibility(View.VISIBLE);
                    tourDescriptionDescrTextView.setVisibility(View.VISIBLE);
                    tourCostTextView.setVisibility(View.VISIBLE);
                    tourCostDescrTextView.setVisibility(View.VISIBLE);
                    tourLocationsTextView.setVisibility(View.VISIBLE);
                    tourLocationsDescrTextView.setVisibility(View.VISIBLE);

                    tourInfoArrow.setRotation((float)-90);
                    tourInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    tourNameTextView.setVisibility(View.GONE);
                    tourNameDescrTextView.setVisibility(View.GONE);
                    tourStartEndDateTextView.setVisibility(View.GONE);
                    tourStartEndDateDescrTextView.setVisibility(View.GONE);
                    tourDescriptionTextView.setVisibility(View.GONE);
                    tourDescriptionDescrTextView.setVisibility(View.GONE);
                    tourCostTextView.setVisibility(View.GONE);
                    tourCostDescrTextView.setVisibility(View.GONE);
                    tourLocationsTextView.setVisibility(View.GONE);
                    tourLocationsDescrTextView.setVisibility(View.GONE);

                    tourInfoArrow.setRotation((float)0);
                    tourInfoAreGone = true;
                }
            }
        });

        // definizione del listener del titolo del cicerone
        ciceroneInfoTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(ciceroneInfoAreGone) {

                    ciceroneHeader.setVisibility(View.VISIBLE);
                    firstLastNameCiceroneTextView.setVisibility(View.VISIBLE);
                    firstLastNameCiceroneDescrTextView.setVisibility(View.VISIBLE);
                    emailCiceroneTextView.setVisibility(View.VISIBLE);
                    emailCiceroneDescrTextView.setVisibility(View.VISIBLE);
                    phoneNumberCiceroneTextView.setVisibility(View.VISIBLE);
                    phoneNumberCiceroneDescrTextView.setVisibility(View.VISIBLE);
                    biographyCiceroneTextView.setVisibility(View.VISIBLE);
                    biographyCiceroneDescrTextView.setVisibility(View.VISIBLE);

                    ciceroneInfoArrow.setRotation((float)-90);
                    ciceroneInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    ciceroneHeader.setVisibility(View.GONE);
                    firstLastNameCiceroneTextView.setVisibility(View.GONE);
                    firstLastNameCiceroneDescrTextView.setVisibility(View.GONE);
                    emailCiceroneTextView.setVisibility(View.GONE);
                    emailCiceroneDescrTextView.setVisibility(View.GONE);
                    phoneNumberCiceroneTextView.setVisibility(View.GONE);
                    phoneNumberCiceroneDescrTextView.setVisibility(View.GONE);
                    biographyCiceroneTextView.setVisibility(View.GONE);
                    biographyCiceroneDescrTextView.setVisibility(View.GONE);

                    ciceroneInfoArrow.setRotation((float)0);
                    ciceroneInfoAreGone = true;
                }
            }
        });
        ciceroneInfoArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(ciceroneInfoAreGone) {

                    ciceroneHeader.setVisibility(View.VISIBLE);
                    firstLastNameCiceroneTextView.setVisibility(View.VISIBLE);
                    firstLastNameCiceroneDescrTextView.setVisibility(View.VISIBLE);
                    emailCiceroneTextView.setVisibility(View.VISIBLE);
                    emailCiceroneDescrTextView.setVisibility(View.VISIBLE);
                    phoneNumberCiceroneTextView.setVisibility(View.VISIBLE);
                    phoneNumberCiceroneDescrTextView.setVisibility(View.VISIBLE);
                    biographyCiceroneTextView.setVisibility(View.VISIBLE);
                    biographyCiceroneDescrTextView.setVisibility(View.VISIBLE);

                    ciceroneInfoArrow.setRotation((float)-90);
                    ciceroneInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    ciceroneHeader.setVisibility(View.GONE);
                    firstLastNameCiceroneTextView.setVisibility(View.GONE);
                    firstLastNameCiceroneDescrTextView.setVisibility(View.GONE);
                    emailCiceroneTextView.setVisibility(View.GONE);
                    emailCiceroneDescrTextView.setVisibility(View.GONE);
                    phoneNumberCiceroneTextView.setVisibility(View.GONE);
                    phoneNumberCiceroneDescrTextView.setVisibility(View.GONE);
                    biographyCiceroneTextView.setVisibility(View.GONE);
                    biographyCiceroneDescrTextView.setVisibility(View.GONE);

                    ciceroneInfoArrow.setRotation((float)0);
                    ciceroneInfoAreGone = true;
                }
            }
        });

        // definizione del listener del titolo delle recensioni
        feedbackInfoTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(feedbackInfoAreGone) {

                    tourFeedbackTextView.setVisibility(View.VISIBLE);

                    feedbackInfoArrow.setRotation((float)-90);
                    feedbackInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    tourFeedbackTextView.setVisibility(View.GONE);

                    feedbackInfoArrow.setRotation((float)0);
                    feedbackInfoAreGone = true;
                }
            }
        });
        feedbackInfoArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(feedbackInfoAreGone) {

                    tourFeedbackTextView.setVisibility(View.VISIBLE);

                    feedbackInfoArrow.setRotation((float)-90);
                    feedbackInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    tourFeedbackTextView.setVisibility(View.GONE);

                    feedbackInfoArrow.setRotation((float)0);
                    feedbackInfoAreGone = true;
                }
            }
        });

        // definizione del listener del titolo della mia recensione
        myFeedbackInfoTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(myFeedbackInfoAreGone) {

                    myFeedbackTextView.setVisibility(View.VISIBLE);
                    myFeedbackRatingTextView.setVisibility(View.VISIBLE);
                    myFeedbackCreatedOnTextView.setVisibility(View.VISIBLE);

                    myFeedbackInfoArrow.setRotation((float)-90);
                    myFeedbackInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    myFeedbackTextView.setVisibility(View.GONE);
                    myFeedbackRatingTextView.setVisibility(View.GONE);
                    myFeedbackCreatedOnTextView.setVisibility(View.GONE);

                    myFeedbackInfoArrow.setRotation((float)0);
                    myFeedbackInfoAreGone = true;
                }
            }
        });
        feedbackInfoArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // se le componenti non sono visibili allora mostrale
                if(myFeedbackInfoAreGone) {

                    myFeedbackTextView.setVisibility(View.VISIBLE);
                    myFeedbackRatingTextView.setVisibility(View.VISIBLE);
                    myFeedbackCreatedOnTextView.setVisibility(View.VISIBLE);

                    myFeedbackInfoArrow.setRotation((float)-90);
                    myFeedbackInfoAreGone = false;
                }

                // se le componenti sono visibili allora nascondile
                else {

                    myFeedbackTextView.setVisibility(View.GONE);
                    myFeedbackRatingTextView.setVisibility(View.GONE);
                    myFeedbackCreatedOnTextView.setVisibility(View.GONE);

                    myFeedbackInfoArrow.setRotation((float)0);
                    myFeedbackInfoAreGone = true;
                }
            }
        });

        // definizione del listener per il click
        tourFeedbackTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(!tourFeedbackTextView.getText().toString().equals(getResources().getString(R.string.no_feedback))) {

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragment = ShowFeedbackFragment.newInstance(db, tour.getId());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment, fragment).commit();
                }
            }
        });

        // definizione del listener per il click del bottone di pagamento
        makePaymentButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // definizione del collegamento per giungere all'activity del reinserimento password
                Intent intent = new Intent (view.getContext(), CheckoutActivity.class);

                // passaggio dell'iscrizione
                intent.putExtra("GLOBETROTTER_USER", subscribed.getGlobetrotter().getUsername());
                intent.putExtra("EVENT_ID", Integer.toString(subscribed.getEvent().getId()));
                intent.putExtra("COST", Float.toString(subscribed.getEvent().getTour().getCost()));
                intent.putExtra("GROUP_DIMENSION", Integer.toString(subscribed.getGroupDimension()));
                intent.putExtra("FIRST_NAME", subscribed.getGlobetrotter().getFirstName());
                intent.putExtra("LAST_NAME", subscribed.getGlobetrotter().getLastName());

                // apertura dell'activity del reinserimento password
                startActivity (intent);

                selectedButton = true;
                firstLoad = false;
            }
        });

        // definizione del listener per il click del bottone di cancellazione dell'iscrizione
        actionOnSubscriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // se il bottone funge da cancellazione allora effettua
                // la cancellazione stessa
                if(actionOnSubscriptionButton.getText().toString().equals(
                        getResources().getString(R.string.delete_subscription))){

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragment = DeleteSubscribedGlobetrotterFragment.newInstance(db, subscribed);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment, fragment).commit();
                }

                // altrimenti effettua l'inserimento del feedback
                else {

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragment = ManageFeedbackFragment.newInstance(db, tour, feedbacks, eventID);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment, fragment).commit();
                }

            }
        });

        if(selectedButton)
            makePaymentButton.setVisibility(View.GONE);

        return view;
    }


    @Override
    public void onResume() {

        super.onResume();

        if(selectedButton) {

            makePaymentButton.setVisibility(View.GONE);
            String hasPayedText;
            if(subscribed.getEvent().getTour().getCost() > 0) {

                if(subscribed.getHasPayed())
                    hasPayedText = getResources().getString(R.string.you_have_payed);
                else
                    hasPayedText = getResources().getString(R.string.you_have_not_payed);

                // definizione della data di scadenza del pagamento
                Calendar ultimatumDate = Calendar.getInstance();
                ultimatumDate.setTime(event.getStartDate().getTime());
                ultimatumDate.add(GregorianCalendar.DAY_OF_MONTH, -4);
                String dateText = ultimatumDate.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                        (ultimatumDate.get(GregorianCalendar.MONTH) + 1) + "/" +
                        ultimatumDate.get(GregorianCalendar.YEAR);
                hasPayedText = hasPayedText.replace("{DATE}", dateText);

            } else {

                hasPayedText = getResources().getString(R.string.event_free_cost_description);
            }
            hasPayedTextView.setText(hasPayedText);
        }

        // se è il primo accesso al fragment (ovvero è stato
        // appena creato) allora setta il flag a falso
        if(firstLoad) {

            firstLoad = false;
        }

        // altrimenti ricarica i dati dal server per aggiornarli
            // ed aggiorna i dati degli oggetti grafici
        else {

            // acquisizione dell'insieme dei dati
            Event tempEvent = tourController.getEventFromID(eventID);
            HashMap<String, Object> tourData = new HashMap<>(tourController.getTourFromID(tempEvent.getTour().getId()));

            // acquisizione dell'attività
            tour = (Tour)tourData.get("tour");

            // se l'utente ha pagato o il tour è gratis non mostrare il bottone
            if(tour.getCost() > 0 && subscribed.getHasPayed())
                makePaymentButton.setVisibility(View.GONE);
            else if (tour.getCost() <= 0)
                makePaymentButton.setVisibility(View.GONE);

            String hasPayedText;
            if(subscribed.getEvent().getTour().getCost() > 0) {

                if(subscribed.getHasPayed())
                    hasPayedText = getResources().getString(R.string.you_have_payed);
                else
                    hasPayedText = getResources().getString(R.string.you_have_not_payed);

                // definizione della data di scadenza del pagamento
                Calendar ultimatumDate = Calendar.getInstance();
                ultimatumDate.setTime(event.getStartDate().getTime());
                ultimatumDate.add(GregorianCalendar.DAY_OF_MONTH, -4);
                String dText = ultimatumDate.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                        (ultimatumDate.get(GregorianCalendar.MONTH) + 1) + "/" +
                        ultimatumDate.get(GregorianCalendar.YEAR);
                hasPayedText = hasPayedText.replace("{DATE}", dText);

            } else {

                hasPayedText = getResources().getString(R.string.event_free_cost_description);
            }
            hasPayedTextView.setText(hasPayedText);

            // se non ci sono feedback nonfar visualizzare la voce del menu
            if (myFeedback == null) {

                myFeedbackInfoTitleTextView.setVisibility(View.GONE);
                myFeedbackInfoArrow.setVisibility(View.GONE);
                myFeedbackTextView.setVisibility(View.GONE);
                myFeedbackRatingTextView.setVisibility(View.GONE);
                myFeedbackCreatedOnTextView.setVisibility(View.GONE);

            } else {

                // acquisizione dei nomi dei mesi
                DateFormatSymbols dfs = new DateFormatSymbols();
                String[] months = dfs.getMonths();

                // definiizone del contenuto
                myFeedbackTextView.setText(myFeedback.getDescription());

                // definizione del punteggio
                myFeedbackRatingTextView.setRating((float) myFeedback.getRate());

                // definizione della data
                Calendar createdOn = Calendar.getInstance();
                createdOn.setTime(myFeedback.getCreatedOn().getTime());
                String dateText = createdOn.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                        months[createdOn.get(GregorianCalendar.MONTH)] + " " +
                        createdOn.get(GregorianCalendar.YEAR);
                myFeedbackCreatedOnTextView.setText(dateText);
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

            if(!addresses.isEmpty())
                return addresses.get(0).getThoroughfare() + ", " + addresses.get(0).getLocality() ;


        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }
}
