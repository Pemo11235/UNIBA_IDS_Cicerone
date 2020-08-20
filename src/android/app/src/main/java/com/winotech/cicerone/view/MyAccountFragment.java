package com.winotech.cicerone.view;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.AuthController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Cicerone;
import com.winotech.cicerone.model.Language;
import com.winotech.cicerone.model.User;

import java.io.Serializable;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class MyAccountFragment extends Fragment implements Serializable {

    private View view;
    private ViewGroup localContainer;

    private DBManager db;
    private AuthController authController;

    transient private CircleImageView profileImage;
    private TextView usernameTextView;
    private TextView nameTextView;
    private TextView roleTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private TextView biographyTextView;
    private TextView spokenLanguagesTextView;

    /*
    Singleton del fragment
     */
    public static MyAccountFragment newInstance(DBManager db, AuthController authController) {

        MyAccountFragment fragment = new MyAccountFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.AUTH_CONTROLLER_KEY, authController);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_my_account, container, false);
        localContainer = container;

        // pulitura dei vecchi fragment visitati
        if(localContainer != null)
            localContainer.removeAllViews();

        // acquisizione del controller
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        authController = (AuthController) getArguments().getSerializable(Constants.AUTH_CONTROLLER_KEY);

        // inizializzazione componenti presenti nel fragment
        profileImage = view.findViewById(R.id.imageView3);
        usernameTextView = view.findViewById(R.id.username_textView);
        roleTextView = view.findViewById(R.id.role_textView);
        nameTextView = view.findViewById(R.id.name_textView);
        emailTextView = view.findViewById(R.id.email_textView);
        phoneTextView = view.findViewById(R.id.phone_textView);
        biographyTextView = view.findViewById(R.id.biography_textView);
        spokenLanguagesTextView = view.findViewById(R.id.spoken_languages_textView);


        // acquisizione dell'accuont dell'utente
        User myAccount = db.getMyAccount();

        // definizione del contenuto dell'immagine
        if(myAccount.getPhoto() != null)
            profileImage.setImageBitmap(myAccount.getPhoto());


        // definizione della stampa del ruolo
        if(db.getMyAccount() instanceof Cicerone)
            roleTextView.setText("Cicerone");
        else
            roleTextView.setText("Globetrotter");

        // definizione della stampa dello username
        usernameTextView.setText(myAccount.getUsername());

        // definizione della stampa del nome e del cognome
        nameTextView.setText(myAccount.getFirstName() + " " + myAccount.getLastName());

        // definizione della stampa della email
        emailTextView.setText(myAccount.getEmail());

        // definizione della stampa del numero di telefono
        phoneTextView.setText(myAccount.getPhone());

        // definizione della stampa della biografia
        if(!myAccount.getBiography().equals("null") &&
                !myAccount.getBiography().equals("") &&
                !myAccount.getBiography().equals(" "))
            biographyTextView.setText(myAccount.getBiography());
        else
            biographyTextView.setText(R.string.no_information);

        // definizione della stampa della biografia
        if(!myAccount.getSpokenLanguages().isEmpty()) {

            String spokenLanguages = "";

            ArrayList<Language> languages = myAccount.getSpokenLanguages();

            for(Language lang : languages){

                Log.d("MyAccountSpokenLanguage", lang.getName());
                spokenLanguages = spokenLanguages.concat(lang.getName()) + " \n";
            }

            spokenLanguagesTextView.setText(spokenLanguages);
        } else {

            spokenLanguagesTextView.setText(R.string.no_information);
        }




        return view;
    }

}
