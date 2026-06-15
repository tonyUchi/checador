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
/*
declaracion de los elementos del Scene Builder
 */
    @FXML private Label lblReloj;
    @FXML private TextField txtId;

    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    /*
    Método que se encarga de inicializar los componentes de la aplicación.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        iniciarReloj();
    }

    /*
    Método que se encarga de crear un reloj dinámico mostrando horas, minutos y segundos, se puede cambiar el modo de visualización de 12 o 24, yo prefiero usar el formato de 12 horas, siento que para los trabajadores es mas entendible .
     */
    private void iniciarReloj() {
        Timeline reloj = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalTime ahora = LocalTime.now();
            lblReloj.setText(ahora.format(DateTimeFormatter.ofPattern("hh:mm:ss")));
        }), new KeyFrame(Duration.seconds(1)));
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
    }

    /*
    Método que se encarga de manejar la lógica principal de la aplicación, es el que captura el id del usuario y esta a la escucha del el botón que sera pulsado, se encarga de verificar que el trabajador no tenga un descanso o vacaciones, de ser así no lo deja registrar, de lo contrario crea su registro y queda a la espera para recibir los estados de los demás botones.
     */
    @FXML
    private void manejarBotones(ActionEvent event) {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            mostrarAlerta("Atención", "Por favor, ingresa un ID de trabajador.", Alert.AlertType.WARNING);
            return;
        }
        if (!asistenciaDAO.existeTrabajador(id)) {
            mostrarAlerta("ID no reconocido", "El ID '" + id + "' no existe en el sistema.", Alert.AlertType.ERROR);
            txtId.clear();
            return;
        }
        String periodo = asistenciaDAO.verificarPeriodoLibre(id);
        if (periodo != null) {
            String mensaje = periodo.equals("VACACIONES") ?
                    "El trabajador está en su periodo de VACACIONES." :
                    "Hoy es día de DESCANSO para este trabajador.";
            mostrarAlerta("Acceso Denegado", mensaje, Alert.AlertType.INFORMATION);
            txtId.clear();
            return;
        }
        Button btnPresionado = (Button) event.getSource();
        String textoBoton = btnPresionado.getText().toLowerCase();
        int estado = asistenciaDAO.obtenerEstadoHoy(id);
        try {
            if (textoBoton.contains("entrada") && !textoBoton.contains("comida")) {
                if (estado == 0) {
                    asistenciaDAO.registrarMovimiento(id, 0);
                    mostrarAlerta("Éxito", "Entrada registrada correctamente.", Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error", "El trabajador ya tiene una entrada registrada hoy.", Alert.AlertType.ERROR);
                }
            }
            else if (textoBoton.contains("salida") && textoBoton.contains("comida")) {
                if (estado == 1) {
                    asistenciaDAO.registrarMovimiento(id, 1);
                    mostrarAlerta("Buen provecho", "Salida a comer registrada. Se calculó tu hora de regreso.", Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error", "Debes registrar entrada antes de salir a comer.", Alert.AlertType.ERROR);
                }
            }
            else if (textoBoton.contains("regreso") && textoBoton.contains("comida")) {
                if (estado == 2) {
                    asistenciaDAO.registrarMovimiento(id, 2);
                    mostrarAlerta("Bienvenido", "Regreso de comida registrado.", Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error", "No se encontró una salida a comer previa.", Alert.AlertType.ERROR);
                }
            }
            else if (textoBoton.contains("salida") && !textoBoton.contains("comida")) {
                if (estado == 3) {
                    asistenciaDAO.registrarMovimiento(id, 3);
                    mostrarAlerta("Hasta mañana", "Salida final registrada. ¡Buen descanso!", Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error", "No puedes registrar salida final sin haber completado los pasos previos.", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            mostrarAlerta("Error Crítico", "Ocurrió un error al procesar el registro.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
        txtId.clear();
    }

    /*
    Método que nos ayuda a mostrar las diferentes alertas que nuestra aplicación puede mandar, así como determinar si solo son información o es un error.
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /*
    Método que se encarga de abrir la ventana del administrador, para ello antes de dejarlo entrar se crea una ventana emergente que pide el usuario y la contraseña, si los datos son correctos se abre la ventana, de lo contrario se queda en la pantalla principal.
     */
    @FXML
    private void abrirPanelAdmin() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Control de Acceso");
        dialog.setHeaderText("Área de Administración - Inicie Sesión");
        ButtonType botonAceptarTipo = new ButtonType("Entrar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(botonAceptarTipo, ButtonType.CANCEL);
        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Nombre de usuario");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Contraseña");
        VBox contenedor = new VBox(10);
        contenedor.getChildren().addAll(
                new Label("Usuario:"), txtUsuario,
                new Label("Contraseña:"), txtPassword
        );
        dialog.getDialogPane().setContent(contenedor);
        dialog.setResultConverter(dialogBoton -> {
            if (dialogBoton == botonAceptarTipo) {
                return new Pair<>(txtUsuario.getText().trim(), txtPassword.getText().trim());
            }
            return null;
        });
        Optional<Pair<String, String>> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            String usuario = resultado.get().getKey();
            String password = resultado.get().getValue();
            boolean loginExitoso = asistenciaDAO.validarAdmin(usuario, password);
            if (loginExitoso) {
                try {
                    Stage ventanaActual = (Stage) txtId.getScene().getWindow();
                    ventanaActual.hide();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/checador/gui/VistaAdmin.fxml"));
                    Parent root = loader.load();
                    Stage stageAdmin = new Stage();
                    stageAdmin.setTitle("Panel de Administración");
                    stageAdmin.setScene(new Scene(root));
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