package com.winotech.cicerone.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/*
Classe per la definizione di un evento associato
ad una attività
 */
public class Event implements Serializable {

    private int id; // id dell'evento
    private Tour tour; // tour associato all'evento
    private Calendar startDate; // data di inizio dell'evento
    private Calendar endDate; // data di fine dell'evento
    private String description; // descrizione dell'evento
    private int maxSubscribeds; // numero massimo di registrazioni

    private ArrayList<Language> eventLanguages; // lista di lingue parlate nell'evento
    private ArrayList<Subscribed> eventSubscribeds; // lista di iscritti all'evento
    private ArrayList<Request> eventRequests; // lista di richieste di iscrizione all'evento


    /*
    Costruttore della classe
     */
    public Event() {

        this.id = 0;
        this.tour = null;
        this.startDate = null;
        this.endDate = null;
        this.description = null;
        this.maxSubscribeds = -1;

        this.eventLanguages = new ArrayList<>();
        this.eventSubscribeds = new ArrayList<>();
        this.eventRequests = new ArrayList<>();
    }

    /*
    Costruttore della classe
     */
    public Event(int id, Tour tour, Calendar startDate, Calendar endDate,
                 String description, int maxSubscribeds) {

        this.id = id;
        this.tour = tour;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.maxSubscribeds = maxSubscribeds;

        this.eventLanguages = new ArrayList<>();
        this.eventSubscribeds = new ArrayList<>();
        this.eventRequests = new ArrayList<>();
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

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxSubscribeds() {
        return maxSubscribeds;
    }

    public void setMaxSubscribeds(int maxSubscribeds) {
        this.maxSubscribeds = maxSubscribeds;
    }

    public ArrayList<Language> getEventLanguages() {
        return eventLanguages;
    }

    public ArrayList<Subscribed> getEventSubscribeds() {
        return eventSubscribeds;
    }

    public ArrayList<Request> getEventRequests() {
        return eventRequests;
    }

    // ----- METODI DI AGGIUNTA -----

    /*
    Metodo di aggiunta di una lingua all'evento
     */
    public void addEventLanguages(Language language) {
        this.eventLanguages.add(language);
    }

    /*
    Metodo di aggiunta di un iscritto all'evento
     */
    public void addEventSubscribeds(Subscribed subscribed) {
        this.eventSubscribeds.add(subscribed);
    }


    /*
    Metodo di aggiunta di una richiesta di iscrizione all'evento
     */
    public void addEventRequests(Request request) {
        this.eventRequests.add(request);
    }

}
