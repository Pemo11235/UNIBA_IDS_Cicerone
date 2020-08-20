package com.winotech.cicerone.model;

import java.io.Serializable;

/*
Classe per la definizione delle
motivazioni della creazione della notifica
 */
public class Reason implements Serializable {

    private int id; // id della motivo della notifica
    private String name; // nome del motivo della notifica


    /*
    Costruttore della classe
     */
    public Reason() {

        this.id = 0;
        this.name = null;
    }

    /*
    Costruttore della classe
     */
    public Reason(int id, String name) {

        this.id = id;
        this.name = name;
    }


    //----- METODI GETTER E SETTER-----

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
}
