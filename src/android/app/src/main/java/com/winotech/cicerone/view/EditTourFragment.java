package com.winotech.cicerone.view;

import android.app.DatePickerDialog;
import android.content.Context;
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

import android.app.FragmentTransaction;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Tour;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;


public class EditTourFragment extends Fragment implements Serializable {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller delle attività
    private static transient TourController tourController;

    // oggetti grafici del fragment
    private EditText nameEditText;
    private EditText descriptionEditText;
    private EditText costEditText;
    private TextView costUneditable;
    private TextView startDateView;
    private TextView endDateView;
    private Button submitButton;

    /*
    Singleton del fragment
     */
    public static EditTourFragment newInstance(DBManager db, Tour tour) {

        EditTourFragment fragment = new EditTourFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.KEY_TOUR_OBJ, tour);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_edit_tour, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione dei parametri passati
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        Tour tour = (Tour) getArguments().getSerializable(Constants.KEY_TOUR_OBJ);

        // inizializzazione del controller
        if(tourController == null)
            TourController.initInstance(getActivity());
        tourController = TourController.getInstance();

        // inizializzazione delle stringhe
        nameEditText = view.findViewById(R.id.editText);
        descriptionEditText = view.findViewById(R.id.editText3);
        costEditText = view.findViewById(R.id.editText4);
        costUneditable = view.findViewById(R.id.textView38);
        startDateView = view.findViewById(R.id.textView35);
        endDateView = view.findViewById(R.id.textView34);
        submitButton = view.findViewById(R.id.button5);

        // acquisizione della lista degli eventi del tour
        final ArrayList<Event> events = new ArrayList<>(tour.getTourEvents());

        // acquisizione dell'id del tour
        final int tourID = tour.getId();

        // acquisizione del costo iniziale
        final float initialCost = tour.getCost();

        if(tour.getName() != null)
            nameEditText.setText(tour.getName());

        if(tour.getDescription() != null)
            descriptionEditText.setText(tour.getDescription());

        // definizione del costo del tour
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        String cost = df.format(tour.getCost());
        costEditText.setText(cost);

        // visibilità del messaggio di prezzo non modificabile
        if(!events.isEmpty()){

            costEditText.setFocusable(false);
            costUneditable.setVisibility(View.VISIBLE);
        }

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
        setDatePickerDialog(tour.getEndDate());

        // definizione del listener del bottone di invio dei dati
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                try {

                    // definizione del formato della data
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);

                    // acquisizione del nome del tour
                    String tourName = null;
                    if(nameEditText.getText() != null && !nameEditText.getText().toString().isEmpty())
                        tourName = nameEditText.getText().toString();

                    // acquisizione della descrizione del tour
                    String tourDescription = null;
                    if(descriptionEditText.getText() != null && !descriptionEditText.getText().toString().isEmpty())
                        tourDescription = descriptionEditText.getText().toString();

                    // acqusiizione del costo del tour
                    float tourCost = -1;
                    if(costEditText.getText() != null && !costEditText.getText().toString().isEmpty()) {

                        String cost = costEditText.getText().toString();
                        cost = cost.replace(",",".");
                        tourCost = Float.valueOf(cost);
                    }

                    // definizione della data di inizio del tour
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
                        boolean success = tourController.editTour(tourID, tourName, tourDescription, tourCost,
                                tourStartDate, tourEndDate, events);

                        // se l'esecuzione va a buon fine allora torna alla pagina precedente
                        if(success)
                            getFragmentManager().popBackStack();


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
    private void setDatePickerDialog(final Calendar endDate) {

        final View v = this.view;

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

                // acquisisce la data di fine del tour
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
