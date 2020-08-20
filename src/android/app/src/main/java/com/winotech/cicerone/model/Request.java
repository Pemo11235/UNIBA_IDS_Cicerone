package com.winotech.cicerone.model;

import java.io.Serializable;
import java.util.Calendar;

/*
Classe per la gestione e creazione della richiesta di iscrizione
ad un evento
 */
public class Request implements Serializable {

    private Globetrotter globetrotter; // globetrotter associato alla richiesta (utile al cicerone)
    private Event event; //  evento associato alla richiesta (utile al globetrotter)
    private int groupDimension = 0; // dimensione del gruppo
    private Calendar createdOn; // data di creazione della richiesta
    private Boolean accepted; // flag di accettazione della richiesta


    /*
    Costruttore della classe Request
     */
    public Request(Globetrotter globetrotter, Event event, int groupDimension,
                   Calendar createdOn, Boolean accepted) {

        this.globetrotter = globetrotter;
        this.event = event;
        this.groupDimension = groupDimension;
        this.createdOn = createdOn;
        this.accepted = accepted;
    }


    // ----- METODI GETTER E SETTER -----

    public Globetrotter getGlobetrotter() {
        return globetrotter;
    }

    public void setGlobetrotter(Globetrotter globetrotter) {
        this.globetrotter = globetrotter;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getGroupDimension() {
        return groupDimension;
    }

    public void setGroupDimension(int groupDimension) {
        this.groupDimension = groupDimension;
    }

    public Calendar getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Calendar createdOn) {
        this.createdOn = createdOn;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }
}
