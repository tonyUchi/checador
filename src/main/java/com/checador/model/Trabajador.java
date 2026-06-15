package com.checador.model;

public class Trabajador {
    private String id;
    private String nombre;
    private String app;
    private String apm;
    private String puesto;

    public Trabajador(String id, String nombre, String app, String apm, String puesto) {
        this.id = id;
        this.nombre = nombre;
        this.app = app;
        this.apm = apm;
        this.puesto = puesto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApp() { return app; }
    public void setApp(String app) { this.app = app; }

    public String getApm() { return apm; }
    public void setApm(String apm) { this.apm = apm; }

    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }

    public String getNombreCompleto() {
        return nombre + " " + app + " " + (apm != null ? apm : "");
    }
}