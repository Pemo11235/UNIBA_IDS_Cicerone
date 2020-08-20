package com.winotech.cicerone.view;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jakewharton.processphoenix.ProcessPhoenix;
import com.winotech.cicerone.R;
import com.winotech.cicerone.misc.Constants;

import java.io.Serializable;


public class SelectLanguageFragment extends Fragment implements Serializable {

    // variabili per il layout del fragment
    private View view;
    private ViewGroup localContainer;

    // puntatore all'activity principale
    MainActivity activity;

    // variabili per gli oggetti grafici
    private RadioGroup radioLanguage;
    private RadioButton ItalianLanguage;
    private RadioButton EnglishLanguage;

    //stringa per la lingua locale
    private String lang;

    /*
    Singleton del fragment
     */
    public static android.app.Fragment newInstance() {

        SelectLanguageFragment fragment = new SelectLanguageFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inizializzazione del layout del fragment
        view = inflater.inflate(R.layout.fragment_select_language, container, false);
        localContainer = container;

        // rimozione di tutti i  fragment dallo stack
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione al puntatore sull'activity di appartenenza
        activity = (MainActivity) getActivity();

        radioLanguage = view.findViewById(R.id.LanguageRadio);
        ItalianLanguage = view.findViewById(R.id.ItalianLanguage);
        EnglishLanguage = view.findViewById(R.id.EnglishLanguage);


        //acquisizione lingua
        //String lang = LanguageController.read(LanguageController.language, null);
        String lang = activity.getSessionManager().getLanguage();

        Log.d("ChangeLanguage", "Language is " + lang);

            //activity.getSessionManager().setLanguage(activity.getResources().getConfiguration().locale.toString());
            //LanguageController.write(LanguageController.language, getResources().getConfiguration().locale.toString());

        //check del linguaggio
        if (lang.equals(Constants.LANGUAGE_IT))
            ItalianLanguage.setChecked(true);
        else if (lang.equals(Constants.LANGUAGE_EN))
            EnglishLanguage.setChecked(true);


        // definizione listener del ragio group
        radioLanguage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                //String lang = LanguageController.read(LanguageController.language, null);
                String lang = activity.getSessionManager().getLanguage();
                lang = lang.trim();

                if (checkedId == R.id.ItalianLanguage && lang.equals(Constants.LANGUAGE_EN)) {

                    Log.d("ChangeLanguage", "Changing language to italian");

                    // definizione della nuova lingua
                    activity.getSessionManager().updateLanguage(activity, Constants.LANGUAGE_IT);

                    // definizione del messaggio da mostrare
                    String message = activity.getResources().getString(R.string.change_language_message) +
                            " " + activity.getResources().getString(R.string.it);

                    // ricarica del testo
                    ProcessPhoenix.triggerRebirth(getActivity());

                } else if (checkedId == R.id.EnglishLanguage && lang.equals(Constants.LANGUAGE_IT)) {

                    Log.d("ChangeLanguage", "Changing language to english");

                    // definizione della nuova lingua
                    activity.getSessionManager().updateLanguage(activity, Constants.LANGUAGE_EN);

                    // definizione del messaggio da mostrare
                    String message = activity.getResources().getString(R.string.change_language_message) +
                            " " + activity.getResources().getString(R.string.it);

                    // ricarica del testo
                    ProcessPhoenix.triggerRebirth(getActivity());

                } else {

                    Log.d("ChangeLanguage", "Ops");
                }

            }
        });


        return view;
    }


}
