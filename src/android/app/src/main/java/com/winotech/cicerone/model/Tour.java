package com.winotech.cicerone.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/*
Classe per la definizione di una certa attività
presente nel sistema
 */
public class Tour implements Serializable {

    private int id; // id dell'attività
    private String name; // nome dell'attività
    private Cicerone cicerone; // cicerone titolare dell'attività
    private Calendar startDate; // data di partenza dell'attività
    private Calendar endDate; // data di fine dell'attività
    private String description; // descrizione associata all'attività
    private float cost; // costo dell'attività

    private ArrayList<Location> tourLocations; // lista dei luoghi dell'attività
    private ArrayList<Event> tourEvents; // lista degli eventi associati
    private ArrayList<Feedback> tourFeedbacks; // lista dei feedback dell'attività


    /*
    Costruttore della classe Tour
     */
    public Tour() {

        this.id = 0;
        this.name = null;
        this.cicerone = null;
        this.startDate = null;
        this.endDate = null;
        this.description = null;
        this.cost = -1;

        this.tourLocations = new ArrayList<>();
        this.tourEvents = new ArrayList<>();
        this.tourFeedbacks = new ArrayList<>();
    }

    /*
    Costruttore della classe Tour
     */
    public Tour(int id, String name, Cicerone cicerone, Calendar startDate, Calendar endDate,
                String description, float cost) {

        this.id = id;
        this.name = name;
        this.cicerone = cicerone;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.cost = cost;

        this.tourLocations = new ArrayList<>();
        this.tourEvents = new ArrayList<>();
        this.tourFeedbacks = new ArrayList<>();
    }


    // ----- METODI GETTER E SETTER -----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Cicerone getCicerone() {
        return cicerone;
    }

    public void setCicerone(Cicerone cicerone) {
        this.cicerone = cicerone;
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

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public ArrayList<Location> getTourLocations() {
        return tourLocations;
    }

    public ArrayList<Event> getTourEvents() {
        return tourEvents;
    }

    public ArrayList<Feedback> getTourFeedbacks() {
        return tourFeedbacks;
    }



    // ----- METODI DI AGGIUNTA ---

    /*
    Metodo di aggiunta di una località alla lista
     */
    public void addTourLocation(Location location) {
        this.tourLocations.add(location);
    }

    /*
    Metodo di aggiunta di un evento alla lista
     */
    public void addTourEvent(Event event) {
        this.tourEvents.add(event);
    }

    /*
    Metodo di aggiunta di un feedback all'evento
     */
    public void addTourFeedbacks(Feedback feedback) {
        this.tourFeedbacks.add(feedback);
    }
}
