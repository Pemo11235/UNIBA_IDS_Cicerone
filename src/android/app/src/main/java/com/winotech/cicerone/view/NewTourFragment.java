package com.winotech.cicerone.view;

import android.app.DatePickerDialog;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
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
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.model.Location;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;


public class NewTourFragment extends Fragment implements Serializable {

    // variabili per il layout del fragment
    private View view;
    private ViewGroup localContainer;


    // controller del tour
    private TourController tourController;

    // puntatore all'activity principale
    MainActivity activity;

    // lista delle tappe selezionate
    HashMap<String, Location> locations = new HashMap<>();

    // oggetti grafici del fragment
    private EditText nameEditText;
    private EditText descriptionEditText;
    private EditText costEditText;
    private TextView startDateView;
    private TextView endDateView;
    private Button addLocationButton;
    private Button submitButton;

    // dati che si stanno modificando e salvati temporaneamente
    Vector<String> tempData;


    /*
    Singleton del fragment
     */
    public static Fragment newInstance(HashMap<String, Location> locations) {

        NewTourFragment fragment = new NewTourFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("SET_LOCATIONS", locations);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del layout del fragment
        view = inflater.inflate(R.layout.fragment_new_tour, container, false);
        localContainer = container;

        // rimozione di tutti i  fragment dallo stack
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione al puntatore sull'activity di appartenenza
        activity = (MainActivity) getActivity();

        // acquisizione del controller dei tour
        tourController = TourController.getInstance();

        // acquisizione delle tappe inserite
        if(getArguments().getSerializable("SET_LOCATIONS") != null) {

            locations = (HashMap<String, Location>) getArguments().getSerializable("SET_LOCATIONS");
            tourController.setTempLocations(locations);
        }

        // inizializzazione delle stringhe
        nameEditText = view.findViewById(R.id.editText);
        descriptionEditText = view.findViewById(R.id.editText3);
        costEditText = view.findViewById(R.id.editText4);
        startDateView = view.findViewById(R.id.textView35);
        endDateView = view.findViewById(R.id.textView34);

        // inizializzazione dei dati in modifica
        tempData = new Vector<>();

        // definizione dei dati qualora ci fossero informazioni temporanee
        if(tourController.hasTempNewTourData()) {

            tempData = tourController.popTempNewTourData();

            if (tempData.get(0) != null)
                nameEditText.setText(tempData.get(0));

            if (tempData.get(1) != null)
                descriptionEditText.setText(tempData.get(1));

            if (tempData.get(2) != null)
                costEditText.setText(tempData.get(2));

            if (tempData.get(3) != null)
                startDateView.setText(tempData.get(3));

            if (tempData.get(4) != null)
                endDateView.setText(tempData.get(4));
        }

        // inizializzazione della lista dei textview
        LinearLayout locationNameList = view.findViewById(R.id.locationNameListNewTour);

        // definizione dei popup della scelta delle date
        setDatePickerDialog();

        // inizializzazione del bottone di aggiunta della tappa
        addLocationButton = view.findViewById(R.id.button4);

        // definizione del listener del bottone di aggiunta della tappa
        addLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                tempData = new Vector<>();

                // aggiunta dei dati in modifica
                tempData.add(nameEditText.getText().toString());
                tempData.add(descriptionEditText.getText().toString());
                tempData.add(costEditText.getText().toString());
                tempData.add(startDateView.getText().toString());
                tempData.add(endDateView.getText().toString());

                tourController.setTempNewTourData(tempData);

                Intent intent = new Intent(activity, GetLocationActivity.class);
                startActivity(intent);
            }
        });

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
                    if(costEditText.getText() != null && !costEditText.getText().toString().isEmpty())
                        tourCost = Float.valueOf(costEditText.getText().toString());

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
                            && tourStartDate != null && tourEndDate != null && !locations.isEmpty()) {

                        // tenta il salvataggio del tour e se va a buon fine cambia fragment
                        boolean success = tourController.saveTour(tourName, tourDescription, tourCost,
                                tourStartDate, tourEndDate, locations, activity);

                        // se l'esecuzione va a buon fine allora torna alla pagina precedente
                        if(success)
                            getFragmentManager().popBackStack();

                    } else {

                        // messaggio di errore
                        Toast.makeText (activity, R.string.empty_fields,
                                Toast.LENGTH_LONG).show();
                    }



                } catch (ParseException e) {

                    e.printStackTrace();
                }
            }
        });

        // inizializzazione del nome delle tappe
        ArrayList<String> locationNames = new ArrayList<>();

        // acquisizione del nome delle tappe
        for(String locationName : locations.keySet())
            locationNames.add(locationName);

        // per ogni nome di una tappa si aggiunge una textView
        for(String name : locationNames) {

            // definizione del layout della stringa
            LinearLayout.LayoutParams textViewListParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textViewListParams.setMargins(40, 10, 40, 0);

            // inizializzazione e definizione del layout del textview
            TextView textView = new TextView(view.getContext());
            textView.setLayoutParams(textViewListParams);
            textView.setText(name);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(18);

            // aggiunta del textview alla lista
            locationNameList.addView(textView);
        }

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
                DatePickerDialog datePickerDialog = new DatePickerDialog(activity,
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(activity,
                        R.style.Theme_AppCompat_DayNight_Dialog,
                        onDateSetListener, year, month, day);

                // rappresentazione del popup
                datePickerDialog.show();
            }
        });
    }
}
