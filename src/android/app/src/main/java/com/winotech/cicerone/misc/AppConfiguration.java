package com.winotech.cicerone.misc;

import java.io.Serializable;

public class AppConfiguration implements Serializable {

    // prefisso della directory
    public final static String URL_PREFIX = "http://192.168.1.60/Cicerone/";

    // directory del file php per il login
    public final static String URL_FILE_LOGIN = URL_PREFIX + "Login.php";

    // directory del file php per la registrazione
    public final static String URL_FILE_REGISTER = URL_PREFIX + "Register.php";

    // directory del file php per il recupero
    public final static String URL_FILE_RECOVERY = URL_PREFIX + "Recovery.php";

    // directory del file php per l'acquisizione dei dati personali
    public final static String URL_FILE_GETMYDATA = URL_PREFIX + "GetMyData.php";

    // directory del file php per la modifica dei dati personali
    public final static String URL_FILE_EDIT_PERSONAL_INFOS = URL_PREFIX + "EditPersonalInfos.php";

    // directory del file php per la modifica della password
    public final static String URL_FILE_EDIT_PASSWORD = URL_PREFIX + "EditPassword.php";

    // directory del file php per la modifica delle lingue parlate
    public final static String URL_FILE_EDIT_SPOKEN_LANGUAGES = URL_PREFIX + "EditSpokenLanguages.php";

    // directory della cartella per il salvataggio delle immagini del profilo
    public final static String URL_IMAGE_STORAGE_PATH = URL_PREFIX + "images/";

    // directory della cartella per il salvataggio del nuovo tour
    public final static String URL_FILE_NEW_TOUR = URL_PREFIX + "NewTour.php";

    // directory della cartella per l'acquisizione del tour
    public final static String URL_FILE_GET_TOUR = URL_PREFIX + "GetTour.php";

    // directory della cartella per l'acquisizione del tour
    public final static String URL_FILE_GET_TOUR_BY_ID = URL_PREFIX + "GetTourByID.php";

    // directory della cartella per l'acquisizione del tour su ricerca
    public final static String URL_FILE_GET_SEARCHED_TOUR = URL_PREFIX + "GetSearchedTour.php";

    // directory della cartella per l'acquisizione delle notifiche
    public final static String URL_FILE_GET_NOTIFICATIONS = URL_PREFIX + "GetNotifications.php";

    // directory della cartella per l'acquisizione degli eventi
    public final static String URL_FILE_GET_EVENT_BY_ID = URL_PREFIX + "GetEventByID.php";

    // directory della cartella per il settaggio delle richieste
    public final static String URL_FILE_SET_REQUEST = URL_PREFIX + "NewRequest.php";

    // directory della cartella per il settaggio delle notifiche
    public final static String URL_FILE_SET_NOTIFICATION = URL_PREFIX + "NewNotification.php";

	// directory della cartella per l'acquisizione dei miei eventi
    public final static String URL_FILE_GET_EVENTS_SUBSCRIBED = URL_PREFIX + "GetEventsSubscribed.php";

    // directory della cartella per l'aggiornamento dell'attività
    public final static String URL_FILE_UPDATE_TOUR = URL_PREFIX + "UpdateTour.php";

    // directory della cartella per la cancellazione dell'attività
    public final static String URL_FILE_DELETE_TOUR = URL_PREFIX + "DeleteTour.php";

    // directory della cartella per l'aggiunta di un nuovo evento
    public final static String URL_FILE_NEW_EVENT = URL_PREFIX + "NewEvent.php";

    // directory della cartella per l'aggiornamento dell'evento
    public final static String URL_FILE_UPDATE_EVENT = URL_PREFIX + "UpdateEvent.php";

    // directory della cartella per la cancellazione dell'evento
    public final static String URL_FILE_DELETE_EVENT = URL_PREFIX + "DeleteEvent.php";

    // directory della cartella per l'aggiornamento delle tappe
    public final static String URL_FILE_UPDATE_LOCATIONS = URL_PREFIX + "UpdateLocations.php";

    // directory della cartella per la gestione della risposta alla richiesta
    public final static String URL_FILE_SET_REQUEST_RESPONSE = URL_PREFIX + "NewRequestResponse.php";

    // directory della cartella per la cancellazione dell'iscrizione
    public final static String URL_FILE_DELETE_SUBSCRIBED = URL_PREFIX + "DeleteSubscribed.php";

    // directory della cartella per l'aggiunta di una nuova recensione
    public final static String URL_FILE_SET_FEEDBACK = URL_PREFIX + "NewFeedback.php";

    // directory della cartella per la cancellazione dell'iscrizione
    public final static String URL_FILE_DELETE_MY_ACCOUNT = URL_PREFIX + "DeleteMyAccount.php";

    // directory della cartella per l'aggiornamento dello stato delle iscrizioni
    public final static String URL_FILE_UPDATE_SUBSCRIPTIONS = URL_PREFIX + "UpdateSubscriptions.php";

    // directory della cartella per l'aggiornamento dello stato delle richieste
    public final static String URL_FILE_UPDATE_REQUESTS = URL_PREFIX + "UpdateRequests.php";

    // directory della cartella per l'aggiornamento dello stato del pagamento
    public final static String URL_FILE_SAVE_PAYMENT = URL_PREFIX + "SavePayment.php";
}
