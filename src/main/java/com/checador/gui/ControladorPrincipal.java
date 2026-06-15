package com.checador.gui;

import com.checador.dao.AsistenciaDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

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
        // 1. Crear el Diálogo personalizado
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Control de Acceso");
        dialog.setHeaderText("Área de Administración - Inicie Sesión");

        // Configurar los botones del diálogo (Aceptar y Cancelar)
        ButtonType botonAceptarTipo = new ButtonType("Entrar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(botonAceptarTipo, ButtonType.CANCEL);

        // 2. Crear los campos de texto para la interfaz del Login
        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Nombre de usuario");

        PasswordField txtPassword = new PasswordField(); // Oculta los caracteres de la contraseña con puntitos
        txtPassword.setPromptText("Contraseña");

        // Acomodar los campos en un contenedor vertical con separación de 10 pixeles
        VBox contenedor = new VBox(10);
        contenedor.getChildren().addAll(
                new Label("Usuario:"), txtUsuario,
                new Label("Contraseña:"), txtPassword
        );
        dialog.getDialogPane().setContent(contenedor);

        // 3. Convertir el resultado de los campos a un par de datos (Usuario, Password) cuando den clic en Entrar
        dialog.setResultConverter(dialogBoton -> {
            if (dialogBoton == botonAceptarTipo) {
                return new Pair<>(txtUsuario.getText().trim(), txtPassword.getText().trim());
            }
            return null;
        });

        // 4. Mostrar el diálogo y capturar la respuesta
        Optional<Pair<String, String>> resultado = dialog.showAndWait();

        if (resultado.isPresent()) {
            String usuario = resultado.get().getKey();
            String password = resultado.get().getValue();

            // 5. Validar ambas credenciales en la Base de Datos a través del DAO
            boolean loginExitoso = asistenciaDAO.validarAdmin(usuario, password);

            if (loginExitoso) {
                try {
                    // 1. Conseguir la ventana actual (la del checador público) a través de cualquier componente
                    Stage ventanaActual = (Stage) txtId.getScene().getWindow();

// 2. Ocultarla para que no se quede atrás estorbando
                    ventanaActual.hide();

// 3. Cargar la nueva ventana de administración (como ya lo hacías)
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/checador/gui/VistaAdmin.fxml"));
                    Parent root = loader.load();

                    Stage stageAdmin = new Stage();
                    stageAdmin.setTitle("Panel de Administración");
                    stageAdmin.setScene(new Scene(root));

// ¡MUY IMPORTANTE!: Asegúrate de NO usar stageAdmin.initModality(...)
// Si usas Modality, a veces Linux (Manjaro) deshabilita el botón de minimizar por seguridad.

                    stageAdmin.show();

                } catch (IOException e) {
                    System.err.println("Error al abrir la ventana de administración: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                mostrarAlerta("Acceso Denegado", "El usuario o la contraseña son incorrectos.", Alert.AlertType.ERROR);
            }
        }
    }
}