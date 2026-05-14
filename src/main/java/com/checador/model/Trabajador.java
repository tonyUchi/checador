package com.checador.model;
public class Trabajador {
    private String id;
    private String nombre;

    public Trabajador(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Getters: JavaFX los usa internamente para PropertyValueFactory
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    // Setters: Útiles para ediciones futuras
    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
