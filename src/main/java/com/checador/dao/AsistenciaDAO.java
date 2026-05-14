package com.checador.dao;

import com.checador.db.Conexion;
import com.checador.model.Asistencia;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AsistenciaDAO {

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // 1. Obtener cuántos minutos de comida tiene permitidos el trabajador
    public int obtenerMinutosComida() {
        String sql = "SELECT minutos_comida FROM configuracion WHERE id_config = 1";
        try (Connection cn = Conexion.conectar();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("minutos_comida");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 60; // Por defecto 1 hora si algo falla
    }

    // 2. Determinar en qué paso del día está el trabajador
    public int obtenerEstadoHoy(String id) {
        String sql = "SELECT hora_entrada, hora_comida_salida, hora_comida_regreso, hora_salida " +
                "FROM asistencias WHERE id = ? AND fecha = date('now', 'localtime')";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getString("hora_salida") != null) return 4;         // Ya terminó su día
                if (rs.getString("hora_comida_regreso") != null) return 3; // Ya regresó de comer
                if (rs.getString("hora_comida_salida") != null) return 2;  // Está en su hora de comida
                if (rs.getString("hora_entrada") != null) return 1;        // Ya checó entrada
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0; // No ha checado nada hoy
    }

    // 3. Registrar el movimiento y calcular hora esperada
    public void registrarMovimiento(String id, int estadoActual) {
        String sql = "";
        LocalTime ahora = LocalTime.now();

        try (Connection cn = Conexion.conectar()) {
            switch (estadoActual) {
                case 0: // ENTRADA
                    sql = "INSERT INTO asistencias (id, fecha, hora_entrada) VALUES (?, date('now', 'localtime'), ?)";
                    PreparedStatement ps0 = cn.prepareStatement(sql);
                    ps0.setString(1, id);
                    ps0.setString(2, ahora.format(timeFormatter));
                    ps0.executeUpdate();
                    break;

                case 1: // SALIDA A COMER
                    int minutosPermitidos = obtenerMinutosComida();
                    LocalTime horaEsperada = ahora.plusMinutes(minutosPermitidos);

                    sql = "UPDATE asistencias SET hora_comida_salida = ?, hora_comida_esperada = ? " +
                            "WHERE id = ? AND fecha = date('now', 'localtime')";
                    PreparedStatement ps1 = cn.prepareStatement(sql);
                    ps1.setString(1, ahora.format(timeFormatter));
                    ps1.setString(2, horaEsperada.format(timeFormatter));
                    ps1.setString(3, id);
                    ps1.executeUpdate();
                    break;

                case 2: // REGRESO DE COMER
                    sql = "UPDATE asistencias SET hora_comida_regreso = ? WHERE id = ? AND fecha = date('now', 'localtime')";
                    PreparedStatement ps2 = cn.prepareStatement(sql);
                    ps2.setString(1, ahora.format(timeFormatter));
                    ps2.setString(2, id);
                    ps2.executeUpdate();
                    break;

                case 3: // SALIDA FINAL
                    sql = "UPDATE asistencias SET hora_salida = ? WHERE id = ? AND fecha = date('now', 'localtime')";
                    PreparedStatement ps3 = cn.prepareStatement(sql);
                    ps3.setString(1, ahora.format(timeFormatter));
                    ps3.setString(2, id);
                    ps3.executeUpdate();
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}