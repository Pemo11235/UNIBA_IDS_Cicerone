package com.winotech.cicerone.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Location;
import com.winotech.cicerone.model.Tour;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;


public class RenewTourFragment extends Fragment  implements Serializable {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller delle attività
    private static transient TourController tourController;

    // lista delle tappe selezionate
    HashMap<String, Location> locations = new HashMap<>();

    // oggetti grafici del fragment
    private EditText nameEditText;
    private EditText descriptionEditText;
    private EditText costEditText;
    private TextView startDateView;
    private TextView endDateView;
    private Button submitButton;


    /*
    Singleton del fragment
     */
    public static RenewTourFragment newInstance(DBManager db, Tour tour) {

        RenewTourFragment fragment = new RenewTourFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.KEY_TOUR_ID, tour);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_renew_tour, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione del controller
        if(tourController == null)
            TourController.initInstance(getActivity());
        tourController = TourController.getInstance();

        // inizializzazione dei parametri passati
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        Tour tour = (Tour) getArguments().getSerializable(Constants.KEY_TOUR_ID);

        // inizializzazione delle stringhe
        nameEditText = view.findViewById(R.id.editText);
        descriptionEditText = view.findViewById(R.id.editText3);
        costEditText = view.findViewById(R.id.editText4);
        startDateView = view.findViewById(R.id.textView35);
        endDateView = view.findViewById(R.id.textView34);


        // definizione dell'id del tour
        final int tourID = tour.getId();

        if (tour.getName() != null)
            nameEditText.setText(tour.getName());

        if (tour.getDescription() != null)
            descriptionEditText.setText(tour.getDescription());

        // definizione del costo del tour
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        String cost = df.format(tour.getCost());
        costEditText.setText(cost);

        // definizione della data di inizio dell'attività
        String startDateText = tour.getStartDate().get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                (tour.getStartDate().get(GregorianCalendar.MONTH) + 1) + "/" +
                tour.getStartDate().get(GregorianCalendar.YEAR);
        startDateView.setText(startDateText);

        // definizione della data di fine dell'attività
        String endDateText = tour.getEndDate().get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                (tour.getEndDate().get(GregorianCalendar.MONTH) + 1) + "/" +
                tour.getEndDate().get(GregorianCalendar.YEAR);
        endDateView.setText(endDateText);

        // definizione dei popup della scelta delle date
        setDatePickerDialog(tour.getStartDate(), tour.getEndDate());

        // inizializzazione del bottone di invio dei dati
        submitButton = view.findViewById(R.id.button5);

        // definizione del listener del bottone di invio dei dati
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                try {

                    // definizione del formato della data
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);

                    // definizione delle stringhe varie
                    String tourName = null;
                    if(nameEditText.getText() != null && !nameEditText.getText().toString().isEmpty())
                        tourName = nameEditText.getText().toString();

                    String tourDescription = null;
                    if(descriptionEditText.getText() != null && !descriptionEditText.getText().toString().isEmpty())
                        tourDescription = descriptionEditText.getText().toString();

                    float tourCost = -1;
                    if(costEditText.getText() != null && !costEditText.getText().toString().isEmpty()) {

                        String cost = costEditText.getText().toString();
                        cost = cost.replace(",", ".");
                        tourCost = Float.valueOf(cost);
                    }

                    // definizione della data di inizio
                    Calendar tourStartDate = null;
                    if(!startDateView.getText().toString().equals(getString(R.string.no_date))) {

                        tourStartDate = Calendar.getInstance();
                        tourStartDate.setTime(dateFormat.parse(startDateView.getText().toString()));
                    }

                    // definizione della data di fine
                    Calendar tourEndDate = null;
                    if(!endDateView.getText().toString().equals(getString(R.string.no_date))) {

                        tourEndDate = Calendar.getInstance();
                        tourEndDate.setTime(dateFormat.parse(endDateView.getText().toString()));
                    }

                    // se le caselle sono tutte occupate
                    if (tourName != null && tourDescription != null && tourCost >= 0
                            && tourStartDate != null && tourEndDate != null) {

                        // tenta il salvataggio del tour e se va a buon fine cambia fragment
                        boolean success = tourController.renewTour(tourID, tourName, tourDescription, tourCost,
                                tourStartDate, tourEndDate);

                        // se l'esecuzione va a buon fine allora torna alla pagina precedente
                        if(success)
                            getFragmentManager().popBackStack();

                    } else {

                        // messaggio di errore
                        Toast.makeText (view.getContext(), R.string.empty_fields, Toast.LENGTH_LONG).show();
                    }

                } catch (ParseException e) {

                    e.printStackTrace();
                }
            }
        });




        return view;
    }


    // Metodo per la stampa a video del popup di scelta della data
    private void setDatePickerDialog(final Calendar startDate, final Calendar endDate) {

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
                int year = startDate.get(Calendar.YEAR);
                int month = startDate.get(Calendar.MONTH);
                int day = startDate.get(Calendar.DAY_OF_MONTH);

                // creazione di una nuova istanza di DatePickerDialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(),
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
                int year = endDate.get(Calendar.YEAR);
                int month = endDate.get(Calendar.MONTH);
                int day = endDate.get(Calendar.DAY_OF_MONTH);

                // creazione di una nuova istanza di DatePickerDialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(),
                        R.style.Theme_AppCompat_DayNight_Dialog,
                        onDateSetListener, year, month, day);

                // rappresentazione del popup
                datePickerDialog.show();
            }
        });
    }

}
