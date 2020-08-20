package com.winotech.cicerone.view;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import android.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Feedback;
import com.winotech.cicerone.model.Tour;

import java.util.ArrayList;


public class ManageFeedbackFragment extends Fragment {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller delle attività
    private static transient GeneralController generalController;

    // deifnizione degli oggetti grafici
    private TextView descriptionTextView;
    private RatingBar feedbackRateTextView;
    private EditText feedbackDescriptionEditText;
    private Button submitButton;

    // puntatore al feedback creato
    private Feedback feedback = null;



    /*
    Singleton del fragment
     */
    public static ManageFeedbackFragment newInstance(DBManager db, Tour tour,
                                                     ArrayList<Feedback> feedbacks, int eventID) {

        ManageFeedbackFragment fragment = new ManageFeedbackFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.KEY_TOUR_ID, tour);
        bundle.putSerializable(Constants.KEY_FEEDBACK_DATA, feedbacks);
        bundle.putSerializable(Constants.KEY_EVENT_ID, eventID);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_manage_feedback, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione del db
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        ArrayList<Feedback> feedbacks = (ArrayList<Feedback>) getArguments().getSerializable(Constants.KEY_FEEDBACK_DATA);
        final Tour tour = (Tour) getArguments().getSerializable(Constants.KEY_TOUR_ID);
        final int eventID = (int) getArguments().getSerializable(Constants.KEY_EVENT_ID);

        // inizializzazione del controller
        if(generalController == null)
            GeneralController.initInstance(getActivity());
        generalController = GeneralController.getInstance();

        // inizializzazione degli oggetti grafici
        descriptionTextView = view.findViewById(R.id.feedback_description_textView);
        feedbackRateTextView = view.findViewById(R.id.my_feedback_rating_textView);
        feedbackDescriptionEditText = view.findViewById(R.id.feedback_editText);
        submitButton = view.findViewById(R.id.submit_button);

        // definizione del mio feedback
        for(Feedback feed : feedbacks) {

            if (feed.getGlobetrotter().getUsername().equals(db.getMyAccount().getUsername())) {

                feedback = feed;
                break;
            }
        }

        // in base alla presenza o meno del feedback si cambia la vista degli oggetti
        if(feedback != null) {

            descriptionTextView.setText(getResources().getString(R.string.edit_feedback));
            feedbackRateTextView.setRating((float)feedback.getRate());
            feedbackDescriptionEditText.setText(feedback.getDescription());

        } else {

            descriptionTextView.setText(getResources().getString(R.string.insert_new_feedback));
        }

        // definizione del listener sul cambio del voto del feedback
        feedbackRateTextView.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {

                feedbackRateTextView.setRating(ratingBar.getRating());
            }
        });

        // definizione del listener dell'invio dei dati
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // salvataggio del feedback
                boolean success = generalController.saveFeedback(
                        db.getMyAccount().getUsername(),
                        tour.getId(),
                        (int)feedbackRateTextView.getRating(),
                        feedbackDescriptionEditText.getText().toString()
                );

                if(success) {

                    // inserisci una nuova notifica
                    generalController.saveNotification(
                            db.getMyAccount().getUsername(),
                            tour.getCicerone().getUsername(),
                            10,
                            eventID,
                            "NULL"
                    );

                    // cambio del fragment
                    getFragmentManager().popBackStack();
                }
            }
        });

        return view;
    }


}
