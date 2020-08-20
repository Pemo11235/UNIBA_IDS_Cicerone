package com.winotech.cicerone.model;

import java.io.Serializable;
import java.util.ArrayList;

/*
Classe per la definizione e gestione di un utente cicerone
 */
public class Cicerone extends User implements Serializable {

    private ArrayList<Tour> createdTours; // lista di tour creati e gestiti dal cicerone

    /*
    Costruttore della classe
     */
    public Cicerone() {

        this.createdTours = null;
    }


    /*
    Metodo per l'acquisizione della lista dei tour
     */
    public ArrayList<Tour> getCreatedTours() {

        return createdTours;
    }

    /*
    Metodo per l'aggiunta di un tour alla lista
     */
    public void addCreatedTours(Tour createdTours) {

        if(createdTours != null)
            this.createdTours.add(createdTours);
    }
}
