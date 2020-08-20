package com.winotech.cicerone.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

/*
Classe per la creazione di un utente
 */
public class User implements Serializable {

    private String username; // username dell'utente
    private String email; // email dell'utente
    private String password; // password dell'utente
    private String firstName; // nome dell'utente
    private String lastName; // cognome dell'utente
    private Bitmap photo; // foto dell'utente
    private String phone; // numero di telefono dell'utente
    private String biography; // biografia dell'utente

    private ArrayList<Language> spokenLanguages; // lista di lingue parlate dall'utente
    private ArrayList<Notification> notifications; // lista delle notifiche associate all'utente

    /*
    Costruttore della classe
     */
    public User() {

        this.username = null;
        this.email = null;
        this.password = null;
        this.firstName = null;
        this.lastName = null;
        this.photo = null;
        this.phone = null;
        this.biography = null;
        this.spokenLanguages = null;
        this.notifications = null;
    }


    /*
    Costruttore della classe
     */
    public User(String username, String email, String password, String first_name,
                String last_name, Bitmap image, String phone, String biography) {

        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = first_name;
        this.lastName = last_name;
        this.photo = image;
        this.phone = phone;
        this.biography = biography;
    }

    // ----- GETTER E SETTER -----

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String first_name) {
        this.firstName = first_name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String last_name) {
        this.lastName = last_name;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public ArrayList<Language> getSpokenLanguages() {
        return spokenLanguages;
    }

    public void setSpokenLanguages(ArrayList<Language> spokenLanguages) {
        this.spokenLanguages = spokenLanguages;
    }

    public ArrayList<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(ArrayList<Notification> notifications) {
        this.notifications = notifications;
    }

    // -----------------------------------
}
