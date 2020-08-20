package com.winotech.cicerone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.winotech.cicerone.R;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.misc.SessionManager;

import java.io.Serializable;

public class StartActivity extends AppCompatActivity implements Serializable {

    private Button buttonLogin;
    private Button buttonRegister;

    private SessionManager m_session; // manager della sessione


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        buttonLogin = this.findViewById(R.id.buttonLogin);
        buttonRegister = this.findViewById(R.id.buttonRegister);

        // definizione della sessione
        m_session = new SessionManager(getApplicationContext());

        // se l'utente è gia loggato
        if (m_session.isLoggedIn()) {

            // definisci un collegamento all'actvity della pagina principale
            Intent intent = new Intent (this, MainActivity.class);

            // avviamento dell'activity
            startActivity (intent);

            // chiusura di questa activity
            finish ();
        }

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                startActivity(intent);
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(StartActivity.this, RegisterActivity.class);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                startActivity(intent);
            }
        });
    }
}
