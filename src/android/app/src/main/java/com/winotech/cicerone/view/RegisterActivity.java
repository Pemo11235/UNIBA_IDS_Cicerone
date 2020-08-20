package com.winotech.cicerone.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.AuthController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.SessionManager;

import java.io.Serializable;

public class RegisterActivity extends Activity implements Serializable {

    private Button m_register_button;
    private EditText m_username_editText;
    private EditText m_email_editText;
    private EditText m_password_editText;
    private EditText m_first_name_editText;
    private EditText m_last_name_editText;
    private EditText m_phone_number_editText;
    private RadioButton m_cicerone_radioButton;
    private RadioButton m_globetrotter_radioButton;
    private SessionManager m_session;



    @Override
    public void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // collegamento delgi oggetti grafici in variabili da associare
        m_register_button = findViewById (R.id.register_button);
        m_username_editText = findViewById (R.id.username_editText);
        m_email_editText = findViewById (R.id.old_password_editText);
        m_password_editText = findViewById (R.id.password_editText);
        m_first_name_editText = findViewById (R.id.first_name_editText);
        m_last_name_editText = findViewById (R.id.last_name_editText);
        m_phone_number_editText = findViewById (R.id.phone_number_editText);
        m_cicerone_radioButton = findViewById (R.id.cicerone_radioButton);
        m_globetrotter_radioButton = findViewById (R.id.globetrotter_radioButton);

        // manager della sessione di connessione
        m_session = new SessionManager (getApplicationContext());

        // se l'utente è gia registrato
        if (m_session.isLoggedIn()) {

            // definizione di un collegamento tra tale activity e quella principale
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);

            // si reindirizza l'utente all'activity principale
            startActivity(intent);

            // chiusura di questa activity
            finish();
        }

        m_cicerone_radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                m_globetrotter_radioButton.setChecked(false);
                m_cicerone_radioButton.setChecked(true);
            }
        });

        m_globetrotter_radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                m_globetrotter_radioButton.setChecked(true);
                m_cicerone_radioButton.setChecked(false);
            }
        });

        // listener degli eventi sul bottone di registrazione
        m_register_button.setOnClickListener(new View.OnClickListener() {

            // definizione dell'evento di click
            public void onClick(View view) {

                AuthController auth = new AuthController(RegisterActivity.this, null);

                // acquisizione del contenuto dalle caselle di testo
                String username = m_username_editText.getText().toString().trim();
                String email = m_email_editText.getText().toString().trim();
                String password = m_password_editText.getText().toString().trim();
                String first_name = m_first_name_editText.getText().toString().trim();
                String last_name = m_last_name_editText.getText().toString().trim();
                String phone_number = m_phone_number_editText.getText().toString().trim();
                boolean role;
                if (m_cicerone_radioButton.isChecked())
                    role = Constants.ROLE_CICERONE;
                else
                    role = Constants.ROLE_GLOBETROTTER;

                // se le caselle sono tutte occupate
                if (!username.isEmpty () && !email.isEmpty () && !password.isEmpty ()
                        && !first_name.isEmpty () && !last_name.isEmpty () && !phone_number.isEmpty ()) {

                    //registra il nuovo utente
                    boolean is_registered = auth.register(username, email, password, first_name, last_name, phone_number, role);

                } else {

                    // messaggio di errore
                    Toast.makeText (getApplicationContext(), R.string.empty_fields,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }







}
