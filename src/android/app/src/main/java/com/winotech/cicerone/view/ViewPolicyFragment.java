package com.winotech.cicerone.view;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.winotech.cicerone.R;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;

import java.io.Serializable;
import java.util.ArrayList;

public class ViewPolicyFragment extends Fragment implements Serializable{

  // variabili per il layout del fragment
  private View view;
  private ViewGroup localContainer;
  private TextView policy;



  /*
  Singleton del fragment
   */
  public static android.app.Fragment newInstance() {

    ViewPolicyFragment fragment = new ViewPolicyFragment();
    Bundle bundle = new Bundle();

    fragment.setArguments(bundle);

    return fragment;
  }




  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_view_policy, container, false);
    localContainer = container;
    policy = view.findViewById(R.id.policyTerms);


    // pulitura dei vecchi fragment visitati
    if(localContainer != null)
      localContainer.removeAllViews();

    policy.setText(
            Html.fromHtml(getString(R.string.policy_terms))
    );



    return view;
  }

}