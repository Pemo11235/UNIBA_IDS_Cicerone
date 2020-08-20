package com.winotech.cicerone.model;

import java.io.Serializable;

/*
Classe per la definizione dell'iscrizione di un globetrotter
ad un evento
 */
public class Subscribed implements Serializable {

    private Event event; // evento collegato all'iscrizione (utile per globetrotter)
    private Globetrotter globetrotter; // globetrotter iscritto all'evento (utile per cicerone)
    private int groupDimension; // dimensione del gruppo associato al globetrotter
    private boolean hasPayed; // flag per il pagamento avvenuto


    /*
    Costruttore della classe
     */
    public Subscribed() {

        this.globetrotter = null;
        this.event = null;
        this.groupDimension = -1;
    }

    /*
    Costruttore della classe
     */
    public Subscribed(Event event, Globetrotter globetrotter, int groupDimension, boolean hasPayed) {

        this.event = event;
        this.globetrotter = globetrotter;
        this.groupDimension = groupDimension;
        this.hasPayed = hasPayed;
    }


    // ----- METODI GETTER E SETTER -----

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Globetrotter getGlobetrotter() {
        return globetrotter;
    }

    public void setGlobetrotter(Globetrotter globetrotter) {
        this.globetrotter = globetrotter;
    }

    public int getGroupDimension() {
        return groupDimension;
    }

    public void setGroupDimension(int groupDimension) {
        this.groupDimension = groupDimension;
    }

    public boolean getHasPayed() {
        return hasPayed;
    }

    public void setHasPayed(boolean hasPayed) {
        this.hasPayed = hasPayed;
    }
}
