package com.checador.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    // El archivo se creará en la carpeta principal del proyecto
    private static final String URL = "jdbc:sqlite:checador.db";

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            return null;
        }
    }
}