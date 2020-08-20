package com.winotech.cicerone.misc;

import java.io.Serializable;

public class Constants implements Serializable {

    public static final boolean ROLE_GLOBETROTTER = false;
    public static final boolean ROLE_CICERONE = true;


    public static final String AUTH_CONTROLLER_KEY = "user_controller";
    public static final String TOUR_CONTROLLER_KEY = "tour_controller";
    public static final String DB_KEY = "local_db";
    public static final String SESSION_KEY = "session";

    public static final String FRAGMENT_TO_LOAD_KEY = "fragment_to_load";
    public static final String MY_TOURS_FRAGMENT_VALUE = "myToursFragment";
    public static final String NEW_TOUR_FRAGMENT_VALUE = "newTourFragment";
    public static final String SHOW_TOUR_CICERONE_FRAGMENT_VALUE = "showTourCiceroneFragment";


    // directory del percorso all'immagine di default
    public static final String DEFAULT_IMAGE_PATH = "images/default.jpg";

    public static final int IMG_RESULT = 1;

    // nome del database
    public static final String DATABASE_NAME = "cicerone.db";

    // nome della tabella dell'utente
    public static final String TABLE_USER = "user";

    // nome della tabella delle lingue
    public static final String TABLE_LANGUAGE = "language";

    // nome della tabella delle lingue parlate dall'utente
    public static final String TABLE_USERSPOKENLANGUAGE = "user_spoken_language";

    // nomi delle colonne della tabella
    public static final String KEY_USER_USERNAME = "username";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_AUTH_CODE = "password";   // passovuord
    public static final String KEY_USER_FIRST_NAME = "first_name";
    public static final String KEY_USER_LAST_NAME = "last_name";
    public static final String KEY_USER_PHONE_NUMBER = "phone";
    public static final String KEY_USER_PHOTO = "photo";
    public static final String KEY_USER_BIOGRAPHY = "biography";
    public static final String KEY_USER_ROLE = "role";

    public static final String KEY_LANGUAGE_ID = "id";
    public static final String KEY_LANGUAGE_NAME = "name";

    public static final String KEY_USERSPOKENLANGUAGE_USERNAME = "username_id";
    public static final String KEY_USERSPOKENLANGUAGE_LANGUAGE = "language_id";

    public static final String KEY_TOUR_OBJ = "tour_object";
    public static final String KEY_TOUR_ID = "tour_id";
    public static final String KEY_TOUR_LOCATIONS = "tour_locations";
    public static final String KEY_TOUR_EVENTS = "tour_events";
    public static final String KEY_EVENT_ID = "event_id";
    public static final String KEY_EVENT_DATA = "event_data";
    public static final String KEY_REQUEST_DATA = "request_data";
    public static final String KEY_SUBSCRIPTION_DATA = "subscription_data";
    public static final String KEY_FEEDBACK_DATA = "feedback_data";

    // nomi delle lingue possibili acquisibili dal sistema
    public static final String LANGUAGE_IT = "it";
    public static final String LANGUAGE_EN = "en";
}
