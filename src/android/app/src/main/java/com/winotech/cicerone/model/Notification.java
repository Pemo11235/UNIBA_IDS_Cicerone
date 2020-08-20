package com.winotech.cicerone.model;

import java.io.Serializable;
import java.util.Calendar;

/*
Classe per la creazione delle notifiche di cambiamento
degli eventi
 */
public class Notification implements Serializable {

    private int id; // ID della notifica
    private User sender; // mittente della notifica
    private User receiver; // riceventre della notifica
    private Event event; // evento associato alla notifica
    private Reason reason; // motivo della creazione della notifica
    private String description; // descrizione associata alla modifica
    private Calendar createdOn; // data di creazione della notifica


    /*
    Costruttore della classe
     */
    public Notification() {

        this.id = 0;
        this.sender = null;
        this.receiver = null;
        this.event = null;
        this.reason = null;
        this.description = null;
        this.createdOn = null;
    }

    /*
    Costruttore della classe
     */
    public Notification(int id, User sender, User receiver, Event event, Reason reason,
                        String description, Calendar createdOn) {

        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.event = event;
        this.reason = reason;
        this.description = description;
        this.createdOn = createdOn;
    }


    // ----- METODI GETTER E SETTER -----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Calendar createdOn) {
        this.createdOn = createdOn;
    }
}
