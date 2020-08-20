package com.winotech.cicerone.view;

import android.os.Bundle;

import android.app.FragmentTransaction;
import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Notification;
import com.winotech.cicerone.model.Request;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class ShowNotificationFragment extends Fragment {

    // costante della chiave dell'idi della notifica
    private static final String NOTIFICATION_ID_KEY = "notificationID";

    // componenti del contesto del Fragment
    private View view;
    private ViewGroup localContainer;

    // puntatore al db locale
    private DBManager db;

    // puntatore al controller delle attività
    private static transient GeneralController generalController;

    // notifica da mostrare a video
    private Notification notification;

    // oggetti grafici del fragment
    transient private CircleImageView profileImage;
    private TextView usernameTextView;
    private TextView createdOnTextView;
    private TextView tourNameTextView;
    private TextView notificationDateTextView;
    private TextView notificationObjectTextView;
    private TextView notificationDescriptionTextView;
    private TextView notificationNotesTextView;
    private Button actionButton;


    /*
    Singleton del fragment
     */
    public static ShowNotificationFragment newInstance(DBManager db, Notification notification) {

        ShowNotificationFragment fragment = new ShowNotificationFragment();

        if(db != null) {

            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.DB_KEY, db);
            bundle.putSerializable(NOTIFICATION_ID_KEY, notification);
            fragment.setArguments(bundle);
        }

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_show_notification, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione del db e della notifica
        if(getArguments() != null) {

            db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
            notification = (Notification) getArguments().getSerializable(NOTIFICATION_ID_KEY);

        } else {

            db = new DBManager(view.getContext());
            notification = new Notification();
        }

        // inizializzazione del controller
        if(generalController == null)
            GeneralController.initInstance(getActivity());
        generalController = GeneralController.getInstance();

        // inizializzazione componenti presenti nel fragment
        profileImage = view.findViewById(R.id.imageView4);
        usernameTextView = view.findViewById(R.id.username_textView);
        createdOnTextView = view.findViewById(R.id.created_on_textView);
        tourNameTextView = view.findViewById(R.id.tour_name_textView);
        notificationDateTextView = view.findViewById(R.id.notification_date_textView);
        notificationObjectTextView = view.findViewById(R.id.notification_object_textView);
        notificationDescriptionTextView = view.findViewById(R.id.notification_description_textView);
        notificationNotesTextView = view.findViewById(R.id.notification_notes_textView);
        actionButton = view.findViewById(R.id.redirect_button);

        // acquisizione dei nomi dei mesi
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();

        // definizione dello username
        usernameTextView.setText(notification.getSender().getUsername());

        // oggetto per la definizione delle date
        GregorianCalendar date = new GregorianCalendar();

        // definizione della data di creazioned ella notifica
        date.setTime(notification.getCreatedOn().getTime());
        String createdOn = date.get(GregorianCalendar.DAY_OF_MONTH) + " "
                + months[date.get(GregorianCalendar.MONTH)] + " " +
                date.get(GregorianCalendar.YEAR);
        createdOnTextView.setText(createdOn);

        // definizione del nome del tour di appartenenza
        tourNameTextView.setText(notification.getEvent().getTour().getName());

        // definizione della data di effettuazione dell'evento
        // se la data è si un solo giorno allora mostra solo la data di inizio
        // dato che inizio e fine sono uguali
        String dateDuration = "";
        if(notification.getEvent().getStartDate().equals(notification.getEvent().getEndDate())) {

            date = (GregorianCalendar) notification.getEvent().getStartDate();
            dateDuration = date.get(GregorianCalendar.DAY_OF_MONTH) + " "
                    + months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);

        } else {

            date = (GregorianCalendar) notification.getEvent().getStartDate();
            dateDuration = date.get(GregorianCalendar.DAY_OF_MONTH) + " "
                    + months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR) + " - ";
            date = (GregorianCalendar) notification.getEvent().getEndDate();
            dateDuration += date.get(GregorianCalendar.DAY_OF_MONTH) + " " +
                    months[date.get(GregorianCalendar.MONTH)] + " " +
                    date.get(GregorianCalendar.YEAR);
        }
        notificationDateTextView.setText(dateDuration);

        // definizione dell'oggetto della reagione di invio della notifica
        notificationObjectTextView.setText(generalController.getNotificationObjectFromID(
                notification.getReason().getName()));

        // definiizone della descrizione della notifica
        String description = generalController.getNotificationDescriptionFromID(notification.getReason().getName());
        description = description.replace("{SENDER}", notification.getSender().getUsername());
        notificationDescriptionTextView.setText(description);

        // definizione della descrizione aggiuntiva
        if (!notification.getDescription().equals("null")) {

            notificationNotesTextView.setText(notification.getDescription());

        } else {

            TextView textView = view.findViewById(R.id.textViewNN);
            textView.setHeight(0);
            textView.setVisibility(View.INVISIBLE);
            notificationNotesTextView.setVisibility(View.INVISIBLE);
            notificationNotesTextView.setHeight(0);
        }

        // definizione dell'immagine del profilo del mittente
        profileImage.setImageBitmap(notification.getSender().getPhoto());

        // se la notifica è da parte di un globetrotter che ha richiesto l'iscrizione
        // allora l'onClick porterà alla pagina di gestione delle richieste
        if (notification.getReason().getName().equals("GLOBETROTTER_MADE_REQUEST")) {

            // settaggio stampa
            actionButton.setText(getResources().getString(R.string.redirect_to_request_list));

            // settaggio listener
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // acquisizione delle richieste associate all'evento
                    ArrayList<Request> requests = new ArrayList<>();
                    Request foundRequest = null;

                    // acquisizione degli eventi del tour
                    TourController tourController = TourController.getInstance();
                    HashMap<String, Object> data = tourController.getTourFromID(notification.getEvent().getTour().getId());
                    ArrayList<Event> events = (ArrayList<Event>)data.get("events");

                    // acquisizione delle richieste
                    for(Event event : events){

                        if(event.getId() == notification.getEvent().getId()) {

                            requests = new ArrayList<>(event.getEventRequests());
                            break;
                        }
                    }

                    // acquisizione della richiesta particolare
                    for(Request request : requests) {

                        if(request.getGlobetrotter().getUsername().equals(notification.getSender().getUsername())) {

                            foundRequest = request;
                            break;
                        }
                    }

                    if(foundRequest != null) {

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        Fragment fragment = ManageRequestFragment.newInstance(db, foundRequest);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.fragment, fragment).commit();

                    } else {

                        // messaggio di errore
                        Toast.makeText (getActivity(), R.string.cant_manage_request_already_did,
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        // se la notifica è da parte di un cicerone che ha accettato la richiesta
        // allora l'onClick porterà alla pagina di effettuazione del pagamento
        else if (notification.getReason().getName().equals("CICERONE_ACCEPT_REQUEST")){

            // settaggio stampa
            actionButton.setText(getResources().getString(R.string.redirect_to_subscription_page));

            // settaggio listener
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Fragment fragment = ShowEventGlobetrotterFragment.newInstance(db, notification.getEvent().getId());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment, fragment).commit();
                }
            });
        }

        // se non è nessuno dei due allora non mostrare il bottone
        else {

            actionButton.setVisibility(View.GONE);
            actionButton.setHeight(0);
        }

        return view;
    }


}
