package com.checador.gui;

import com.checador.dao.AsistenciaDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class ControladorPrincipal implements Initializable {

    @FXML
    private Label lblReloj;

    @FXML
    private TextField txtId;

    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        iniciarReloj();
    }

    private void iniciarReloj() {
        Timeline reloj = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalTime ahora = LocalTime.now();
            lblReloj.setText(ahora.format(DateTimeFormatter.ofPattern("hh:mm:ss")));
        }), new KeyFrame(Duration.seconds(1)));
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
    }

    @FXML
    private void manejarBotones(ActionEvent event) {
        String id = txtId.getText().trim();

        // 1. Validación básica
        if (id.isEmpty()) {
            mostrarAlerta("Atención", "Por favor, ingresa un ID de trabajador.", Alert.AlertType.WARNING);
            return;
        }

        if (!asistenciaDAO.existeTrabajador(id)) {
            mostrarAlerta("ID no reconocido", "El ID '" + id + "' no existe en el sistema.", Alert.AlertType.ERROR);
            txtId.clear();
            return;
        }

        // 2. ¿Está de vacaciones o descanso?
        String periodo = asistenciaDAO.verificarPeriodoLibre(id);
        if (periodo != null) {
            String mensaje = periodo.equals("VACACIONES") ?
                    "El trabajador está en su periodo de VACACIONES." :
                    "Hoy es día de DESCANSO para este trabajador.";
            mostrarAlerta("Acceso Denegado", mensaje, Alert.AlertType.INFORMATION);
            txtId.clear();
            return;
        }

        // 2. Identificar qué botón se presionó
        Button btnPresionado = (Button) event.getSource();
        String textoBoton = btnPresionado.getText().toLowerCase();

        // 3. Obtener el estado actual del trabajador desde la BD
        int estado = asistenciaDAO.obtenerEstadoHoy(id);

        // 4. Lógica de flujo (La "Máquina de Estados")
        try {
            if (textoBoton.contains("entrada") && !textoBoton.contains("comida")) {
                if (estado == 0) {
                    asistenciaDAO.registrarMovimiento(id, 0);
                    mostrarAlerta("Éxito", "Entrada registrada correctamente.", Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error de flujo", "El trabajador ya tiene una entrada registrada hoy.", Alert.AlertType.ERROR);
                }
            }
            else if (textoBoton.contains("salida") && textoBoton.contains("comida")) {
                if (estado == 1) {
                    asistenciaDAO.registrarMovimiento(id, 1);
                    mostrarAlerta("Buen provecho", "Salida a comer registrada. Se calculó tu hora de regreso.", Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error de flujo", "Debes registrar entrada antes de salir a comer.", Alert.AlertType.ERROR);
                }
            }
            else if (textoBoton.contains("regreso") && textoBoton.contains("comida")) {
                if (estado == 2) {
                    asistenciaDAO.registrarMovimiento(id, 2);
                    mostrarAlerta("Bienvenido", "Regreso de comida registrado.", Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error de flujo", "No se encontró una salida a comer previa.", Alert.AlertType.ERROR);
                }
            }
            else if (textoBoton.contains("salida") && !textoBoton.contains("comida")) {
                if (estado == 3) {
                    asistenciaDAO.registrarMovimiento(id, 3);
                    mostrarAlerta("Hasta mañana", "Salida final registrada. ¡Buen descanso!", Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error de flujo", "No puedes registrar salida final sin haber completado los pasos previos.", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            mostrarAlerta("Error Crítico", "Ocurrió un error al procesar el registro.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }

        txtId.clear(); // Limpiamos para el siguiente trabajador
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    private void abrirPanelAdmin() {
        try {
            // 1. Cargar el archivo FXML de la vista administrador
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/checador/gui/VistaAdmin.fxml"));
            Parent root = loader.load();

            // 2. Crear un nuevo escenario (Ventana)
            Stage stageAdmin = new Stage();
            stageAdmin.setTitle("Panel de Administración - Sistema de Checado");

            // 3. Hacerla modal (opcional: bloquea la ventana de atrás hasta que se cierre esta)
            stageAdmin.initModality(Modality.APPLICATION_MODAL);

            // 4. Montar la escena y mostrarla
            stageAdmin.setScene(new Scene(root));
            stageAdmin.setResizable(false); // Para que no alteren tu diseño de 800x600
            stageAdmin.show();

        } catch (IOException e) {
            System.err.println("Error al abrir la ventana de administración: " + e.getMessage());
            e.printStackTrace();
        }
    }
}