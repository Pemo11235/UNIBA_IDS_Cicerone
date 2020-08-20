package com.winotech.cicerone.model;

import java.io.Serializable;

/*
Classe per la definizione della lingua parlata
 */
public class Language implements Serializable {

  private int id = 0; // id della lingua
  private String name = null; // nome della lingua


  /*
  Costruttore della classe Language
   */
  public Language(int id, String name) {

    setId(id);
    setName(name);
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
}
