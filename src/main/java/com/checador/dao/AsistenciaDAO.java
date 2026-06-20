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

    /*
    El siguiente método nos permite obtener el tiempo que tiene disponible el trabajador para su horario de comida, por defecto son 2 horas, pero a veces la jefa lo cambia.
     */
    public int obtenerMinutosComida() {
        String sql = "SELECT minutos_comida FROM configuracion WHERE id_config = 1";
        try (Connection cn = Conexion.conectar();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("minutos_comida");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 120;
    }

    /*
    El siguiente método nos permite ver las acciones que el trabajador ya realizo, esto con la finalidad de comprobar que se lleva un orden, por ejemplo; un usuario no puede salir a comer si todavía no ha checado se entrada.
     */
    public int obtenerEstadoHoy(String id) {
        String sql = "SELECT hora_entrada, hora_comida_salida, hora_comida_regreso, hora_salida " +
                "FROM asistencias WHERE id = ? AND fecha = date('now', 'localtime')";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getString("hora_salida") != null) return 4;
                if (rs.getString("hora_comida_regreso") != null) return 3;
                if (rs.getString("hora_comida_salida") != null) return 2;
                if (rs.getString("hora_entrada") != null) return 1;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /*
        El siguiente método registra los movimientos en la base de datos y gracias al método anterior permite saber si la acción que el trabajador va a realizar esta permitida, todo esto se hace para que los datos sean congruentes en la base de datos, adicionalmente calculamos la hora de regreso esperada después de la comida, ya que seria buena idea decirle al trabajador a que hora tiene que llegar para que lo tome en consideración.
     */
    public void registrarMovimiento(String id, int estadoActual) {
        String sql = "";
        LocalTime ahora = LocalTime.now();

        try (Connection cn = Conexion.conectar()) {
            switch (estadoActual) {
                case 0:
                    sql = "INSERT INTO asistencias (id, fecha, hora_entrada) VALUES (?, date('now', 'localtime'), ?)";
                    PreparedStatement ps0 = cn.prepareStatement(sql);
                    ps0.setString(1, id);
                    ps0.setString(2, ahora.format(timeFormatter));
                    ps0.executeUpdate();
                    break;

                case 1:
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

                case 2:
                    sql = "UPDATE asistencias SET hora_comida_regreso = ? WHERE id = ? AND fecha = date('now', 'localtime')";
                    PreparedStatement ps2 = cn.prepareStatement(sql);
                    ps2.setString(1, ahora.format(timeFormatter));
                    ps2.setString(2, id);
                    ps2.executeUpdate();
                    break;

                case 3:
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

    /*
    El siguiente método verifica que el trabajador exista en la base de datos
     */
    public boolean existeTrabajador(String id) {
        String sql = "SELECT 1 FROM trabajadores WHERE id = ?";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
    El siguiente método verifica que el trabajador cuenta con su día de descanso o vacaciones y regresa el dato correspondiente, si no existe entonces devuelve null.
     */
    public String verificarPeriodoLibre(String id) {
        String sql = "SELECT tipo FROM periodos_libres " +
                "WHERE id = ? AND date('now', 'localtime') BETWEEN fecha_inicio AND fecha_fin";

        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("tipo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    El siguiente método regresa una lista de todos los trabajadores registrados ordenados por apellido paterno.
     */
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

    /*
    El siguiente método permite borrar a un trabajador de la base de datos.
     */
    public boolean eliminarTrabajador(String id) {
        String sql = "DELETE FROM trabajadores WHERE id = ?";

        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, id);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar trabajador: " + e.getMessage());
            return false;
        }
    }

    /*
    El siguiente método permite obtener los datos de configuración de los horarios definidos para un trabajador.
     */
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
        return datos;
    }

    /*
    El siguiente método permite guardar los datos de configuración de los horarios definidos para un trabajador.
     */
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

            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error en el DAO al guardar/actualizar horario: " + e.getMessage());
            return false;
        }
    }

    /*
    El siguiente método permite validar que el usuario administrador que se ingreso existe en la base de datos.
     */
    public boolean validarAdmin(String usuario, String contrasenia) {
        String sql = "SELECT usuario FROM administradores WHERE usuario = ? AND password = ?";

        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, contrasenia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al validar credenciales de admin: " + e.getMessage());
        }
        return false;
    }

    public boolean registrarPeriodoLibre(String idTrabajador, String tipo, String fechaInicio, String fechaFin) {
        String sql = "INSERT INTO periodos_libres (id, tipo, fecha_inicio, fecha_fin) VALUES (?, ?, ?, ?)";

        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, idTrabajador);
            ps.setString(2, tipo);
            ps.setString(3, fechaInicio);
            ps.setString(4, fechaFin);

            int filasAfectadas = ps.executeUpdate();
            if(filasAfectadas > 0)
            {
                return true;
            }
            else
            {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}