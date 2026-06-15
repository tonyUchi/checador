package com.checador.model;

public class Asistencia {
    private String id;
    private String fecha;
    private String horaEntrada;
    private String horaComidaSalida;
    private String horaComidaEsperada;
    private String horaComidaRegreso;
    private String horaSalida;

    public Asistencia(String id) {
        this.id = id;
    }

    public Asistencia(String id, String fecha, String horaEntrada, String horaComidaSalida,
                      String horaComidaEsperada, String horaComidaRegreso, String horaSalida) {
        this.id = id;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaComidaSalida = horaComidaSalida;
        this.horaComidaEsperada = horaComidaEsperada;
        this.horaComidaRegreso = horaComidaRegreso;
        this.horaSalida = horaSalida;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(String horaEntrada) { this.horaEntrada = horaEntrada; }

    public String getHoraComidaSalida() { return horaComidaSalida; }
    public void setHoraComidaSalida(String horaComidaSalida) { this.horaComidaSalida = horaComidaSalida; }

    public String getHoraComidaEsperada() { return horaComidaEsperada; }
    public void setHoraComidaEsperada(String horaComidaEsperada) { this.horaComidaEsperada = horaComidaEsperada; }

    public String getHoraComidaRegreso() { return horaComidaRegreso; }
    public void setHoraComidaRegreso(String horaComidaRegreso) { this.horaComidaRegreso = horaComidaRegreso; }

    public String getHoraSalida() { return horaSalida; }
    public void setHoraSalida(String horaSalida) { this.horaSalida = horaSalida; }
}
