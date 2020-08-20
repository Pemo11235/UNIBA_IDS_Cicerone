package com.winotech.cicerone.view;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.AuthController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.misc.DBManager;
import com.winotech.cicerone.model.User;

import java.io.InputStream;
import java.io.Serializable;

import de.hdodenhof.circleimageview.CircleImageView;


public class EditAccountFragment extends Fragment implements Serializable {

    // variabili per il layout del fragment
    private View view;
    private ViewGroup localContainer;

    private DBManager db;
    private AuthController authController;

    // puntatore all'activity principale
    MainActivity activity;

    // variabili per gli oggetti grafici
    transient private CircleImageView profileImage;
    //private ImageView profileImage;
    private TextView changePhotoTextView;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText biographyEditText;
    private TextView editSpokenLanguagesTextView;
    private TextView editPasswordTextView;
    private Button submitButton;


    private Bitmap tempChangedPhoto;


    /*
    Singleton del fragment
     */
    public static EditAccountFragment newInstance(DBManager db, AuthController userController) {

        EditAccountFragment fragment = new EditAccountFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DB_KEY, db);
        bundle.putSerializable(Constants.AUTH_CONTROLLER_KEY, userController);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inizializzazione del layout del fragment
        view = inflater.inflate(R.layout.fragment_edit_account, container, false);
        localContainer = container;

        // rimozione di tutti i  fragment dallo stack
        if(localContainer != null)
            localContainer.removeAllViews();

        // acquisizione del controller
        authController = (AuthController) getArguments().getSerializable(Constants.AUTH_CONTROLLER_KEY);
        db = (DBManager) getArguments().getSerializable(Constants.DB_KEY);

        // inizializzazione al puntatore sull'activity di appartenenza
        activity = (MainActivity) getActivity();

        // ----- INIZIALIZZAZIONE OGGETTI GRAFICI -----
        profileImage = view.findViewById(R.id.imageView3);
        changePhotoTextView = view.findViewById(R.id.change_photo_textView);
        firstNameEditText = view.findViewById(R.id.first_name_editText);
        lastNameEditText = view.findViewById(R.id.last_name_editText);
        emailEditText = view.findViewById(R.id.old_password_editText);
        phoneEditText = view.findViewById(R.id.phone_editText);
        biographyEditText = view.findViewById(R.id.biography_editText);
        editSpokenLanguagesTextView = view.findViewById(R.id.edit_spoken_languages_textView);
        editPasswordTextView = view.findViewById(R.id.edit_password);
        submitButton = view.findViewById(R.id.edit_account_submit_button);

        // acquisizione dell'accuont dell'utente
        User myAccount = db.getMyAccount();

        // definizione del contenuto dell'immagine
        if(myAccount.getPhoto() != null) {

            profileImage.setImageBitmap(myAccount.getPhoto());
            tempChangedPhoto = myAccount.getPhoto();
        }

        // definizione della stampa del nome e del cognome
        firstNameEditText.setText(myAccount.getFirstName());
        lastNameEditText.setText(myAccount.getLastName());

        // definizione della stampa della email
        emailEditText.setText(myAccount.getEmail());

        // definizione della stampa del numero di telefono
        phoneEditText.setText(myAccount.getPhone());

        // definizione della stampa della biografia
        if(!myAccount.getBiography().equals("null") && !myAccount.getBiography().equals(""))
            biographyEditText.setText(myAccount.getBiography());
        else
            biographyEditText.setText(R.string.no_information);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        // ----- DEFINIZIONE DEI LISTENER -----
        // listener per il cambio della foto
        changePhotoTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent cameraIntent = new Intent(Intent.ACTION_PICK);
                cameraIntent.setType("image/*");
                startActivityForResult(cameraIntent, 1888);
            }
        });

        // listener per il cambio della foto
        profileImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent cameraIntent = new Intent(Intent.ACTION_PICK);
                cameraIntent.setType("image/*");
                startActivityForResult(cameraIntent, 1888);
            }
        });

        // listener per la modifica della lista delle lingue parlate
        editSpokenLanguagesTextView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = EditSpokenLanguagesFragment.newInstance(db, authController);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
            }
        });

        // listener per la modifica della password
        editPasswordTextView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentView = EditPasswordFragment.newInstance(db, authController);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment, fragmentView).commit();
            }
        });

        // listener per il bottone di invio
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String username = db.getMyAccount().getUsername();
                String previous_email = db.getMyAccount().getEmail();

                // valutazione della email
                String email;
                if(!emailEditText.getText().toString().contains("@") ||
                        !emailEditText.getText().toString().contains(".")) {

                    Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.empty_email),
                            Toast.LENGTH_SHORT).show();
                    return;

                } else
                    email = emailEditText.getText().toString();

                // valutaizone del nome e del cognome
                String first_name, last_name;
                if(firstNameEditText.getText().toString().equals("") ||
                        firstNameEditText.getText().toString().equals(" ") ||
                        lastNameEditText.getText().toString().equals("") ||
                        lastNameEditText.getText().toString().equals(" ")) {

                    Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.empty_name),
                            Toast.LENGTH_SHORT).show();
                    return;

                } else {

                    first_name = firstNameEditText.getText().toString();
                    last_name = lastNameEditText.getText().toString();
                }

                // valutazione del numero di telefono
                String phone_number;
                if (phoneEditText.getText().toString().contains(" ") ||
                        phoneEditText.getText().toString().length() < 8) {

                    Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.empty_phone),
                            Toast.LENGTH_SHORT).show();
                    return;

                } else {

                    phone_number = phoneEditText.getText().toString();
                }

                // definizione della biografia se non ha un valore consono
                String biography;
                if(!biographyEditText.getText().toString().equals(activity.getResources().getString(R.string.no_information)))
                    biography= biographyEditText.getText().toString();
                else
                    biography = "";

                (new SendData(username, previous_email, email, first_name, last_name,
                        phone_number, biography, tempChangedPhoto)).execute();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        Log.d("getPhotoFromGallery", "Analyzed requestCode");

        try {

            Uri returnUri = data.getData();

            InputStream imageStream = activity.getContentResolver().openInputStream(returnUri);
            Bitmap bitmapImage = BitmapFactory.decodeStream(imageStream);

            Log.d("getPhotoFromGallery", "data " + data.toString() + " imageStream " + imageStream.toString());

            profileImage.setImageBitmap(bitmapImage);
            tempChangedPhoto = bitmapImage;

        } catch (Exception e) {

            Log.d("getPhotoFromGallery", e.getClass() + " " + e.getMessage());
        }
    }


    /*
    Classe locale per l'invio dei dati in modo asincrono (necessario per la visualizzazione
    dei dialoghi)
     */
    class SendData extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;
        String username;
        String previous_email;
        String email;
        String first_name;
        String last_name;
        String phone_number;
        String biography;
        Bitmap photo;

        protected SendData(String username, String previous_email, String email,
                           String first_name, String last_name, String phone_number,
                           String biography, Bitmap photo) {

            this.username = username;
            this.previous_email = previous_email;
            this.email = email;
            this.first_name = first_name;
            this.last_name = last_name;
            this.phone_number = phone_number;
            this.biography = biography;
            this.photo = photo;

            dialog = new ProgressDialog(getActivity());
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.acquiring_data), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            activity.getAuthController().editPersonalInfo (username, previous_email, email, first_name,
                    last_name, phone_number, biography, photo);


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);

            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
        }
    }
}
