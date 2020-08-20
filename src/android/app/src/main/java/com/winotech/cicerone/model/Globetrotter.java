package com.winotech.cicerone.model;

import java.io.Serializable;
import java.util.ArrayList;

/*
Classe per la definizione e gestione di un utente globetrotter
 */
public class Globetrotter extends User implements Serializable {

    private ArrayList<Subscribed> eventSubscribed; // lista degli eventi a cui è iscritto il globetrotter
    private ArrayList<Feedback> tourFeedbacks; // lista dei feedback rilasciati dal globetrotter
    private ArrayList<Request> eventRequests; // lista dellerichieste di partecipazione all'evento

    /*
    Costruttore della classe
     */
    public Globetrotter() {

        this.eventSubscribed = null;
        this.tourFeedbacks = null;
        this.eventRequests = null;
    }


    // ----- METODI GET -----

    public ArrayList<Subscribed> getEventSubscribed() {
        return eventSubscribed;
    }

    public ArrayList<Feedback> getTourFeedbacks() {
        return tourFeedbacks;
    }

    public ArrayList<Request> getEventRequests() {
        return eventRequests;
    }

    // ----- METODI DI AGGIUNTA -----

    /*
    Metodo per l'aggiunta di una sottoscrizione all'evento
     */
    public void addEventSubscribed(Subscribed subscribed) {

        if (subscribed != null)
            this.eventSubscribed.add(subscribed);
    }

    /*
    Metodo per l'aggiunta di un feedback all'attività
     */
    public void addTourFeedback(Feedback feedback) {

        if (feedback != null)
            this.tourFeedbacks.add(feedback);
    }

    /*
    Metodo per l'aggiunta di una richiesta di iscrizione all'evento
     */
    public void addEventRequest(Request request) {

        if (request != null)
            this.eventRequests.add(request);
    }


}
