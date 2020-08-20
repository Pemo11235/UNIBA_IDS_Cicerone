package com.winotech.cicerone.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Language;
import com.winotech.cicerone.model.Tour;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class NewEventFragment extends Fragment {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller delle attività
    private static transient TourController tourController;

    // puntatore al tour di appartenenza
    private Tour tour;

    // componenti grafici del fragment
    private TextView dateDescriptionTextView;
    private TextView startDateView;
    private TextView endDateView;
    private EditText descriptionEditText;
    private EditText maxSubscriptionsEditText;
    private Button addLocationButton;
    private Button submitButton;

    // lista di linguaggi scelti
    private HashMap<String, Boolean> chosenLanguages = new HashMap<>();


    /*
    Singleton del fragment
     */
    public static NewEventFragment newInstance(DBManager db, Tour tour) {

        NewEventFragment fragment = new NewEventFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.KEY_TOUR_ID, tour);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_new_event, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione dei parametri passati
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        tour = (Tour) getArguments().getSerializable(Constants.KEY_TOUR_ID);

        // inizializzazione del controller
        if(tourController == null)
            TourController.initInstance(getActivity());
        tourController = TourController.getInstance();

        // inizializzazione delle stringhe
        dateDescriptionTextView = view.findViewById(R.id.textView37);
        startDateView = view.findViewById(R.id.textView35);
        endDateView = view.findViewById(R.id.textView34);
        descriptionEditText = view.findViewById(R.id.editText3);
        maxSubscriptionsEditText = view.findViewById(R.id.editText4);
        addLocationButton = view.findViewById(R.id.button4);
        submitButton = view.findViewById(R.id.button5);

        // definizione della stringa del range
        String range = getResources().getString(R.string.event_start_and_end_date_description) + " ";
        GregorianCalendar dateInCalendar = new GregorianCalendar();
        dateInCalendar.setTime(tour.getStartDate().getTime());
        range += dateInCalendar.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                (dateInCalendar.get(GregorianCalendar.MONTH) + 1) + "/" +
                dateInCalendar.get(GregorianCalendar.YEAR);
        dateInCalendar.setTime(tour.getEndDate().getTime());
        range += " - " + dateInCalendar.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                (dateInCalendar.get(GregorianCalendar.MONTH) + 1) + "/" +
                dateInCalendar.get(GregorianCalendar.YEAR);
        dateDescriptionTextView.setText(range);

        final ArrayList<Language> languageList = db.getLanguageList();
        for(Language language : languageList){
            chosenLanguages.put(language.getName(), false);
        }

        // definizione dei popup della scelta delle date
        setDatePickerDialog();

        // definizione del popup della scelta delel lingue
        setLanguagePickerDialog();

        // definizione del listener al bottone di invio
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                try {

                    // definizione della lista di id dei linguaggi scelti
                    ArrayList<String> languageIDs = new ArrayList<>();

                    // controllo delle lingue scelte
                    for(Map.Entry<String, Boolean> language : chosenLanguages.entrySet()) {

                        // controllo per ogni lingua disponibile
                        for(int i = 0; i < languageList.size(); i ++){

                            // se il valore è true, ovvero è stata selezionata,
                            // allora si acquisisce l'id della lingua e la si inserisce
                            // nella lista degli id delle lingue
                            if(languageList.get(i).getName().equals(language.getKey()) && language.getValue()){

                                languageIDs.add(Integer.toString(languageList.get(i).getId()));
                                break;
                            }
                        }
                    }

                    // definizione del formato della data
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);

                    // definizione della data di inizio
                    Calendar eventStartDate = null;
                    if(!startDateView.getText().toString().equals(getString(R.string.no_date))) {

                        eventStartDate = Calendar.getInstance();
                        eventStartDate.setTime(dateFormat.parse(startDateView.getText().toString()));
                    }

                    // definizione della data di fine
                    Calendar eventEndDate = null;
                    if(!endDateView.getText().toString().equals(getString(R.string.no_date))) {

                        eventEndDate = Calendar.getInstance();
                        eventEndDate.setTime(dateFormat.parse(endDateView.getText().toString()));
                    }

                    // definizione della descrizione
                    String eventDescription = null;
                    if(descriptionEditText.getText() != null && !descriptionEditText.getText().toString().isEmpty())
                        eventDescription = descriptionEditText.getText().toString();

                    // definizione del numero massimo di iscritti
                    int eventMaxSubs = -1;
                    if(maxSubscriptionsEditText.getText() != null && !maxSubscriptionsEditText.getText().toString().isEmpty())
                        eventMaxSubs = Integer.valueOf(maxSubscriptionsEditText.getText().toString());

                    // se le caselle sono tutte occupate
                    if (eventMaxSubs >= 0 && eventStartDate != null && eventEndDate != null && !languageIDs.isEmpty()) {

                        // tenta il salvataggio del tour e se va a buon fine cambia fragment
                        boolean success = tourController.saveEvent(tour, eventDescription, eventMaxSubs,
                                eventStartDate, eventEndDate, languageIDs);

                        // se l'esecuzione va a buon fine allora torna alla pagina precedente
                        if(success) {

                            // aggiornamento dei dati sul tour in memoria nel controller
                            tourController.getTourFromID(tour.getId());

                            getFragmentManager().popBackStack();
                        }

                    } else {

                        // messaggio di errore
                        Toast.makeText (view.getContext(), R.string.empty_fields,
                                Toast.LENGTH_LONG).show();
                    }

                } catch (ParseException e) {

                    e.printStackTrace();
                }
            }
        });




        return view;
    }


    // Metodo per la stampa a video del popup di scelta della data
    private void setDatePickerDialog() {

        final View v = this.view;

        // acquisizione del bottone della data di inizio
        TextView startDatePickerButton = view.findViewById(R.id.textView35);
        startDatePickerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // definizione del listener del clicl del bottone
                DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

                        StringBuffer strBuf = new StringBuffer();

                        strBuf.append(dayOfMonth);
                        strBuf.append("/");
                        strBuf.append(month+1);
                        strBuf.append("/");
                        strBuf.append(year);

                        TextView startDatePickerText = v.findViewById(R.id.textView35);
                        startDatePickerText.setText(strBuf.toString());
                    }
                };



                // acquisisce la data odierna
                Calendar now = Calendar.getInstance();
                int year = now.get(java.util.Calendar.YEAR);
                int month = now.get(java.util.Calendar.MONTH);
                int day = now.get(java.util.Calendar.DAY_OF_MONTH);

                // creazione di una nuova istanza di DatePickerDialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(),
                        R.style.Theme_AppCompat_DayNight_Dialog,
                        onDateSetListener, year, month, day);


                // rappresentazione del popup
                datePickerDialog.show();
            }
        });

        // acquisizione del bottone della data di inizio
        TextView endDatePickerButton = view.findViewById(R.id.textView34);
        endDatePickerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // definizione del listener del clicl del bottone
                DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

                        StringBuffer strBuf = new StringBuffer();

                        strBuf.append(dayOfMonth);
                        strBuf.append("/");
                        strBuf.append(month+1);
                        strBuf.append("/");
                        strBuf.append(year);

                        TextView startDatePickerText = v.findViewById(R.id.textView34);
                        startDatePickerText.setText(strBuf.toString());
                    }
                };

                // acquisisce la data odierna
                Calendar now = Calendar.getInstance();
                int year = now.get(java.util.Calendar.YEAR);
                int month = now.get(java.util.Calendar.MONTH);
                int day = now.get(java.util.Calendar.DAY_OF_MONTH);

                // creazione di una nuova istanza di DatePickerDialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(),
                        R.style.Theme_AppCompat_DayNight_Dialog,
                        onDateSetListener, year, month, day);

                // rappresentazione del popup
                datePickerDialog.show();
            }
        });
    }


    // Metodo per la stampa a video del popup di scelta della lingua
    private void setLanguagePickerDialog() {

        final View v = this.view;
        final ArrayList<Language> languages = db.getLanguageList();

        // acquisizione del bottone della data di inizio
        Button addLanguageButton = view.findViewById(R.id.button4);
        addLanguageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                AlertDialog.Builder popUpBuilder = new AlertDialog.Builder(view.getContext());
                popUpBuilder.setTitle(getResources().getString(R.string.add_language));

                final String[] languageNames = new String[languages.size()];
                final boolean[] checkedItems = new boolean[languages.size()];
                int i = 0;

                for(Map.Entry<String, Boolean> language : chosenLanguages.entrySet()) {

                    languageNames[i] = language.getKey();
                    checkedItems[i] = language.getValue();
                    i++;
                }

                popUpBuilder.setMultiChoiceItems(languageNames, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        checkedItems[which] = isChecked;
                        chosenLanguages.put(languageNames[which], isChecked);
                    }
                });

                // Add OK and Cancel buttons
                popUpBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });
                popUpBuilder.setNegativeButton("Cancel", null);

                // Create and show the alert dialog
                AlertDialog dialog = popUpBuilder.create();
                dialog.show();
            }
        });


    }
}
