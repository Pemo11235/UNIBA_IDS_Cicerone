package com.winotech.cicerone.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.AuthController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.SessionManager;

import java.io.Serializable;

public class LoginActivity extends AppCompatActivity implements Serializable {


    private Button m_login_button; // bottone del login
    private EditText m_email_editText; // campo per l'inserimento della mail
    private EditText m_password_editText; // campo per l'inserimento della password
    private TextView m_need_help_textView; // bottone per la riconfigurazione della password
    private ProgressDialog m_dialog; // finestra di dialogo
    private SessionManager m_session; // manager della sessione

    /*
    Metodo di creazione dell'activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // collegamento degli oggetti grafici alle relative variabili
        m_login_button = findViewById (R.id.submit_button);
        m_email_editText = findViewById (R.id.old_password_editText);
        m_password_editText = findViewById (R.id.password_editText);
        m_need_help_textView = findViewById (R.id.need_help_textView);

        // definizione della sessione
        m_session = new SessionManager(getApplicationContext());

        // se l'utente è gia loggato
        if (m_session.isLoggedIn()) {

            // definisci un collegamento all'actvity della pagina principale
            Intent intent = new Intent (this, MainActivity.class);

            intent.putExtra(Constants.FRAGMENT_TO_LOAD_KEY, Constants.MY_TOURS_FRAGMENT_VALUE);

            // avviamento dell'activity
            startActivity (intent);

            // chiusura di questa activity
            finish ();
        }

        // listener per il bottone del login
        m_login_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AuthController auth = new AuthController(LoginActivity.this,
                        new SessionManager(getApplicationContext()));

                // definizione dei valori inseriti nelle caselle di testo
                String email = m_email_editText.getText().toString().trim();
                String password = m_password_editText.getText().toString().trim();

                // se l'email è vuota o è uno spazio vuoto
                if(email.length() == 0 || email.equals(" ")) {

                    Toast.makeText(getApplicationContext(), R.string.empty_email, Toast.LENGTH_SHORT).show();
                    return;
                }

                // se la password è vuoto o è uno spazio vuoto
                if(password.length() == 0 || password.equals(" ")) {

                    Toast.makeText(getApplicationContext(), R.string.empty_password, Toast.LENGTH_SHORT).show();
                    return;
                }

                // se la password e la email inserite sono plausibili
                if(password.length() > 0 && email.length() > 0) {

                    auth.authentication (email, password);
                }
            }
        });

        // listener per il bottone di reset della password
        m_need_help_textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // definizione del collegamento per giungere all'activity del reinserimento password
                Intent intent = new Intent (getApplicationContext(), RecoveryPasswordActivity.class);

                // apertura dell'activity del reinserimento password
                startActivity (intent);

                // chiusura di questa activity
                finish ();
            }
        });
    }

}
