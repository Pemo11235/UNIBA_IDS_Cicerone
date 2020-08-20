package com.winotech.cicerone.view;

import android.app.FragmentTransaction;
import android.os.Bundle;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.Subscribed;

import de.hdodenhof.circleimageview.CircleImageView;


public class ManageSubscribedFragment extends Fragment {

    private View view;
    private ViewGroup localContainer;
    private DBManager db;

    // puntatore al controller generico
    private static transient GeneralController generalController;

    // oggetti grafici
    private CircleImageView imageProfile;
    private TextView usernameTextView;
    private TextView firstLastNameTextView;
    private TextView emailTextView;
    private TextView phoneNumberTextView;
    private TextView biographyTextView;
    private TextView groupDimensionTextView;
    private TextView hasPayedTextView;

    private TextView usernameDescriptionTextView;
    private TextView firstLastNameDescriptionTextView;
    private TextView emailDescriptionTextView;
    private TextView phoneNumberDescriptionTextView;
    private TextView biographyDescriptionTextView;
    private TextView groupDimensionDescriptionTextView;
    private TextView hasPayedDescriptionTextView;

    private TextView globetrotterInfoTitle;
    private ImageView globetrotterInfoArrow;
    private TextView subscriptionInfoTitle;
    private ImageView subscriptionInfoArrow;

    private LinearLayout globetrotterInfo;
    private LinearLayout globetrotterHeader;
    private LinearLayout subscriptionInfo;

    private Button actionButton;

    private boolean globetrotterInfoAreGone;
    private boolean subscriptionInfoAreGone;

    // puntatore alla richiesta dell'iscrizione all'evento
    private Subscribed subscription;



    /*
    Singleton del fragment
     */
    public static ManageSubscribedFragment newInstance(DBManager db, Subscribed subscription) {

        ManageSubscribedFragment fragment = new ManageSubscribedFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.KEY_SUBSCRIPTION_DATA, subscription);
        fragment.setArguments(bundle);

        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del fragment
        view = inflater.inflate(R.layout.fragment_manage_subscribed, container, false);
        localContainer = container;
        if(localContainer != null)
            localContainer.removeAllViews();

        // inizializzazione dei parametri passati
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);
        subscription = (Subscribed) getArguments().getSerializable(Constants.KEY_SUBSCRIPTION_DATA);

        // inizializzazione del controller
        if(generalController == null)
            GeneralController.initInstance(getActivity());
        generalController = GeneralController.getInstance();

        // inizializzazione degli oggetti grafici
        imageProfile = view.findViewById(R.id.imageView4);
        usernameTextView = view.findViewById(R.id.username_textView);
        firstLastNameTextView = view.findViewById(R.id.first_last_name_textView);
        emailTextView = view.findViewById(R.id.email_textView);
        phoneNumberTextView = view.findViewById(R.id.phone_number_textView);
        biographyTextView = view.findViewById(R.id.biography_textView);
        groupDimensionTextView = view.findViewById(R.id.group_dimension_textView);
        hasPayedTextView = view.findViewById(R.id.has_payed_textView);
        usernameDescriptionTextView = view.findViewById(R.id.textView13);
        firstLastNameDescriptionTextView = view.findViewById(R.id.textView9);
        emailDescriptionTextView = view.findViewById(R.id.textView50);
        phoneNumberDescriptionTextView = view.findViewById(R.id.textView51);
        biographyDescriptionTextView = view.findViewById(R.id.textView52);
        groupDimensionDescriptionTextView = view.findViewById(R.id.textView53);
        hasPayedDescriptionTextView = view.findViewById(R.id.textView54);
        globetrotterInfoTitle = view.findViewById(R.id.globetrotter_info_textView);
        globetrotterInfoArrow = view.findViewById(R.id.globetrotter_info_arrow);
        subscriptionInfoTitle = view.findViewById(R.id.subscription_info_textView);
        subscriptionInfoArrow = view.findViewById(R.id.subscription_info_arrow);
        globetrotterInfo = view.findViewById(R.id.globetrotter_info);
        globetrotterHeader = view.findViewById(R.id.globetrotter_header);
        subscriptionInfo = view.findViewById(R.id.subscription_info);
        actionButton = view.findViewById(R.id.action_button);

        // settaggio dell'invisibilità iniziale dei componenti
        globetrotterInfoAreGone = true;
        subscriptionInfoAreGone = false;
        groupDimensionTextView.setVisibility(View.GONE);
        groupDimensionDescriptionTextView.setVisibility(View.GONE);
        hasPayedTextView.setVisibility(View.GONE);
        hasPayedDescriptionTextView.setVisibility(View.GONE);

        // settaggio dell'invisibilità del bottone
        if(subscription.getEvent().getTour().getCost() > 0 && subscription.getHasPayed())
            actionButton.setVisibility(View.GONE);

        // impostazione della foto del profilo
        imageProfile.setImageBitmap(subscription.getGlobetrotter().getPhoto());

        // impostazione dello username del globetrotter
        usernameTextView.setText(subscription.getGlobetrotter().getUsername());

        // impostazione del nome e cognome del globetrotter
        String firstLastName = subscription.getGlobetrotter().getFirstName() + " " +
                subscription.getGlobetrotter().getLastName();
        firstLastNameTextView.setText(firstLastName);

        // impostazione della email del globetrotter
        emailTextView.setText(subscription.getGlobetrotter().getEmail());

        // impostazione del numero di cellulare del globetrotter
        phoneNumberTextView.setText(subscription.getGlobetrotter().getPhone());

        // impostazione della biografia del globetrotter
        if(subscription.getGlobetrotter().getBiography().equals("null") ||
                subscription.getGlobetrotter().getBiography().equals("") ||
                subscription.getGlobetrotter().getBiography().equals(" "))
            biographyTextView.setText(R.string.no_information);
        else
            biographyTextView.setText(subscription.getGlobetrotter().getBiography());


        // impostazione della dimensione del gruppo iscritto
        groupDimensionTextView.setText(Integer.toString(subscription.getGroupDimension()));

        // impostazione della stringa sul pagamento effettuato
        final String hasPayedText;
        if(subscription.getEvent().getTour().getCost() > 0) {

            if(subscription.getHasPayed())
                hasPayedText = getResources().getString(R.string.globetrotter_has_payed);
            else
                hasPayedText = getResources().getString(R.string.globetrotter_has_not_payed);

        } else {

            hasPayedText = getResources().getString(R.string.event_free_cost_description);
        }
        hasPayedTextView.setText(hasPayedText);


        // definizione del listener sul titolo del globetrotter
        globetrotterInfoTitle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(globetrotterInfoAreGone) {

                    globetrotterHeader.setVisibility(View.VISIBLE);
                    firstLastNameTextView.setVisibility(View.VISIBLE);
                    firstLastNameDescriptionTextView.setVisibility(View.VISIBLE);
                    emailTextView.setVisibility(View.VISIBLE);
                    emailDescriptionTextView.setVisibility(View.VISIBLE);
                    phoneNumberTextView.setVisibility(View.VISIBLE);
                    phoneNumberDescriptionTextView.setVisibility(View.VISIBLE);
                    biographyTextView.setVisibility(View.VISIBLE);
                    biographyDescriptionTextView.setVisibility(View.VISIBLE);

                    globetrotterInfoArrow.setRotation((float)-90);
                    globetrotterInfoAreGone = false;

                } else {

                    globetrotterHeader.setVisibility(View.GONE);
                    firstLastNameTextView.setVisibility(View.GONE);
                    firstLastNameDescriptionTextView.setVisibility(View.GONE);
                    emailTextView.setVisibility(View.GONE);
                    emailDescriptionTextView.setVisibility(View.GONE);
                    phoneNumberTextView.setVisibility(View.GONE);
                    phoneNumberDescriptionTextView.setVisibility(View.GONE);
                    biographyTextView.setVisibility(View.GONE);
                    biographyDescriptionTextView.setVisibility(View.GONE);

                    globetrotterInfoArrow.setRotation((float)0);
                    globetrotterInfoAreGone = true;
                }
            }
        });
        globetrotterInfoArrow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(globetrotterInfoAreGone) {

                    globetrotterHeader.setVisibility(View.VISIBLE);
                    firstLastNameTextView.setVisibility(View.VISIBLE);
                    firstLastNameDescriptionTextView.setVisibility(View.VISIBLE);
                    emailTextView.setVisibility(View.VISIBLE);
                    emailDescriptionTextView.setVisibility(View.VISIBLE);
                    phoneNumberTextView.setVisibility(View.VISIBLE);
                    phoneNumberDescriptionTextView.setVisibility(View.VISIBLE);
                    biographyTextView.setVisibility(View.VISIBLE);
                    biographyDescriptionTextView.setVisibility(View.VISIBLE);

                    globetrotterInfoArrow.setRotation((float)-90);
                    globetrotterInfoAreGone = false;

                } else {

                    globetrotterHeader.setVisibility(View.GONE);
                    firstLastNameTextView.setVisibility(View.GONE);
                    firstLastNameDescriptionTextView.setVisibility(View.GONE);
                    emailTextView.setVisibility(View.GONE);
                    emailDescriptionTextView.setVisibility(View.GONE);
                    phoneNumberTextView.setVisibility(View.GONE);
                    phoneNumberDescriptionTextView.setVisibility(View.GONE);
                    biographyTextView.setVisibility(View.GONE);
                    biographyDescriptionTextView.setVisibility(View.GONE);

                    globetrotterInfoArrow.setRotation((float)0);
                    globetrotterInfoAreGone = true;
                }
            }
        });


        // definizione del listener sul titolo dell'iscrizione
        subscriptionInfoTitle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(subscriptionInfoAreGone) {

                    groupDimensionTextView.setVisibility(View.VISIBLE);
                    groupDimensionDescriptionTextView.setVisibility(View.VISIBLE);
                    hasPayedTextView.setVisibility(View.VISIBLE);
                    hasPayedDescriptionTextView.setVisibility(View.VISIBLE);

                    subscriptionInfoArrow.setRotation((float)-90);
                    subscriptionInfoAreGone = false;

                } else {

                    groupDimensionTextView.setVisibility(View.GONE);
                    groupDimensionDescriptionTextView.setVisibility(View.GONE);
                    hasPayedTextView.setVisibility(View.GONE);
                    hasPayedDescriptionTextView.setVisibility(View.GONE);

                    subscriptionInfoArrow.setRotation((float)0);
                    subscriptionInfoAreGone = true;
                }
            }
        });
        subscriptionInfoArrow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(subscriptionInfoAreGone) {

                    groupDimensionTextView.setVisibility(View.VISIBLE);
                    groupDimensionDescriptionTextView.setVisibility(View.VISIBLE);
                    hasPayedTextView.setVisibility(View.VISIBLE);
                    hasPayedDescriptionTextView.setVisibility(View.VISIBLE);

                    subscriptionInfoArrow.setRotation((float)-90);
                    subscriptionInfoAreGone = false;

                } else {

                    groupDimensionTextView.setVisibility(View.GONE);
                    groupDimensionDescriptionTextView.setVisibility(View.GONE);
                    hasPayedTextView.setVisibility(View.GONE);
                    hasPayedDescriptionTextView.setVisibility(View.GONE);

                    subscriptionInfoArrow.setRotation((float)0);
                    subscriptionInfoAreGone = true;
                }
            }
        });


        // definizione del listener sulbottone di azione
        actionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragment = DeleteSubscribedCiceroneFragment.newInstance(db, subscription);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment, fragment).commit();
            }
        });

        return view;
    }


}
