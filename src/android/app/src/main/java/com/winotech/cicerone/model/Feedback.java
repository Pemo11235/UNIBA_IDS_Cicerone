package com.winotech.cicerone.model;

import java.io.Serializable;
import java.util.Calendar;

/*
Classe per la creazione e gestione del feedback
 */
public class Feedback implements Serializable {

    private int id; // ID del feedback
    private Tour tour; // attività associato al feedback (utile al globetrotter)
    private Globetrotter globetrotter; // globetrotter associato al feedback (utile al cicerone)
    private String description; // descrizione associata al feedback
    private int rate; // voto del feedback
    private Calendar createdOn; // data di creazione del feedback

    /*
    Costruttore della classe
     */
    public Feedback() {

        this.id = 0;
        this.tour = null;
        this.globetrotter = null;
        this.description = null;
        this.rate = -1;
        this.createdOn = null;
    }

    /*
    Costruttore della classe
     */
    public Feedback(int id, Tour tour, Globetrotter globetrotter, String description,
                    int rate, Calendar createdOn) {

        this.id = id;
        this.tour = tour;
        this.globetrotter = globetrotter;
        this.description = description;
        this.rate = rate;
        this.createdOn = createdOn;
    }


    // ----- METODI GETTER E SETTER -----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public Globetrotter getGlobetrotter() {
        return globetrotter;
    }

    public void setGlobetrotter(Globetrotter globetrotter) {
        this.globetrotter = globetrotter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public Calendar getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Calendar createdOn) {
        this.createdOn = createdOn;
    }
}
