package com.winotech.cicerone.model;

import java.io.Serializable;

/*
Classe per la definizione della località dell'attività
 */
public class Location implements Serializable {

    private int id; // id della località
    private float latitude; // latitudine della località
    private float longitude; // longitudine della località


    /*
    Costruttore della classe Location
     */
    public Location() {

        this.id = 0;
        this.latitude = -1;
        this.longitude = -1;
    }

    /*
    Costruttore della classe Location
     */
    public Location(int id, float latitude, float longitude) {

        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    // ----- METODI SETTER -----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }
}
