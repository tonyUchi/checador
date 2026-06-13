package com.checador.dao;

import com.checador.db.Conexion;
import com.checador.model.Asistencia;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import com.checador.model.Trabajador;
import java.util.*;

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

    // Verifica si el ID ingresado existe en la tabla de trabajadores
    public boolean existeTrabajador(String id) {
        String sql = "SELECT 1 FROM trabajadores WHERE id = ?";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // Retorna true si encontró al menos una fila
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Verifica si el trabajador tiene vacaciones o descanso hoy
    public String verificarPeriodoLibre(String id) {
        String sql = "SELECT tipo FROM periodos_libres " +
                "WHERE id = ? AND date('now', 'localtime') BETWEEN fecha_inicio AND fecha_fin";

        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("tipo"); // Retornará 'VACACIONES' o 'DESCANSO'
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // No tiene periodos libres hoy
    }

    public List<Trabajador> listarTrabajadores() {
        List<Trabajador> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, app, apm, puesto FROM trabajadores ORDER BY app ASC";

        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Trabajador(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("app"),
                        rs.getString("apm"),
                        rs.getString("puesto")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar trabajadores: " + e.getMessage());
        }
        return lista;
    }

    public boolean registrarTrabajador(String id, String nombre, String app, String apm, String puesto) {
        String sql = "INSERT INTO trabajadores (id, nombre, app, apm, puesto) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, nombre);
            ps.setString(3, app);
            ps.setString(4, apm);
            ps.setString(5, puesto);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar trabajador: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarTrabajador(String id) {
        String sql = "DELETE FROM trabajadores WHERE id = ?";

        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, id);
            int filasAfectadas = ps.executeUpdate();

            // Retorna true si se eliminó al menos una fila
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar trabajador: " + e.getMessage());
            return false;
        }
    }

    public Map<String, String> obtenerHorario(String idTrabajador) {
        Map<String, String> datos = new HashMap<>();
        String sql = "SELECT hora_entrada, hora_salida, tolerancia, tiempo_comida FROM horarios WHERE id_trabajador = ?";

        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, idTrabajador);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    datos.put("entrada", rs.getString("hora_entrada"));
                    datos.put("salida", rs.getString("hora_salida"));
                    datos.put("tolerancia", String.valueOf(rs.getInt("tolerancia")));
                    datos.put("comida", String.valueOf(rs.getInt("tiempo_comida")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener horario: " + e.getMessage());
        }
        return datos; // Si está vacío, significa que el empleado no tiene horario asignado aún
    }

    public boolean guardarOHorarioTrabajador(String idTrabajador, String entrada, String salida, int tolerancia, int comida) {
        String sql = "INSERT OR REPLACE INTO horarios (id_trabajador, hora_entrada, hora_salida, tolerancia, tiempo_comida) VALUES (?, ?, ?, ?, ?)";

        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, idTrabajador);
            ps.setString(2, entrada);
            ps.setString(3, salida);
            ps.setInt(4, tolerancia);
            ps.setInt(5, comida);

            int filasAfectadas = ps.executeUpdate();

            // Retorna true si se insertó o reemplazó la fila con éxito
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error en el DAO al guardar/actualizar horario: " + e.getMessage());
            return false;
        }
    }
}