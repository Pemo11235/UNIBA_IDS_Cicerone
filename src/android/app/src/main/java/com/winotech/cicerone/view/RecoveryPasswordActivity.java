package com.winotech.cicerone.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.AuthController;
import com.winotech.cicerone.misc.SessionManager;

import java.io.Serializable;

public class RecoveryPasswordActivity extends AppCompatActivity implements Serializable {

    private Button send_mail_button; // bottone dell'invio della mail
    private EditText email_editText; // campo per l'inserimento della mail

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_password);

        // collegamento degli oggetti grafici alle relative variabili
        send_mail_button = findViewById (R.id.submit_button);
        email_editText = findViewById (R.id.old_password_editText);

        // listener per il bottone del login
        send_mail_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AuthController auth = new AuthController(RecoveryPasswordActivity.this,
                        new SessionManager(getApplicationContext()));

                // definizione dei valori inseriti nelle caselle di testo
                String email = email_editText.getText().toString().trim();

                // se l'email è vuota o è uno spazio vuoto
                if(email.length() == 0 || email.equals(" ")) {

                    Toast.makeText(getApplicationContext(), R.string.empty_email, Toast.LENGTH_SHORT).show();
                    return;
                }

                // se la password e la email inserite sono plausibili
                if(email.length() > 0) {

                    auth.recovery (email);
                }

            }
        });
    }
}
