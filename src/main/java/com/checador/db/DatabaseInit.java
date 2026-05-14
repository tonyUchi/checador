package com.checador.db;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInit {

    public static void crearTablas() {
        String sqlTrabajadores = "CREATE TABLE IF NOT EXISTS trabajadores (" +
                "id TEXT PRIMARY KEY," +
                "nombre TEXT NOT NULL," +
                "app TEXT NOT NULL," +
                "apm TEXT NOT NULL," +
                "puesto TEXT NOT NULL" +
                ");";

        String sqlAdmin = "CREATE TABLE IF NOT EXISTS administradores (" +
                "usuario TEXT PRIMARY KEY," +
                "password TEXT NOT NULL" +
                ");";

        String sqlConfig = "CREATE TABLE IF NOT EXISTS configuracion (" +
                "id_config INTEGER PRIMARY KEY AUTOINCREMENT," +
                "hora_entrada_oficial TEXT," +
                "minutos_comida INTEGER" + // Ejemplo: 60 para una hora
                ");";

        String sqlAsistencias = "CREATE TABLE IF NOT EXISTS asistencias (" +
                "id_registro INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id TEXT NOT NULL," +
                "fecha TEXT NOT NULL," +
                "hora_entrada TEXT," +
                "hora_comida_salida TEXT," +
                "hora_comida_esperada TEXT," +
                "hora_comida_regreso TEXT," +
                "hora_salida TEXT," +
                "FOREIGN KEY(id) REFERENCES trabajadores(id)" +
                ");";

        String sqlPeriodos = "CREATE TABLE IF NOT EXISTS periodos_libres (" +
                "id_periodo INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id TEXT NOT NULL," +
                "tipo TEXT CHECK(tipo IN ('VACACIONES', 'DESCANSO'))," +
                "fecha_inicio TEXT," +
                "fecha_fin TEXT," +
                "FOREIGN KEY(id) REFERENCES trabajadores(id)" +
                ");";

        try (Connection cn = Conexion.conectar();
             Statement st = cn.createStatement()) {

            st.execute(sqlTrabajadores);
            st.execute(sqlAdmin);
            st.execute(sqlConfig);
            st.execute(sqlAsistencias);
            st.execute(sqlPeriodos);

            // Datos iniciales para que el sistema no esté vacío
            st.execute("INSERT OR IGNORE INTO trabajadores (id, nombre, app, apm, puesto) VALUES ('123', 'Mayra', 'Cortes', 'Pereyra', 'Jefe')");
            st.execute("INSERT OR IGNORE INTO configuracion (id_config, hora_entrada_oficial, minutos_comida) VALUES (1, '09:00:00', 120)");

            System.out.println("Estructura completa de base de datos inicializada.");

        } catch (Exception e) {
            System.err.println("Error al inicializar tablas: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        crearTablas();
    }
}