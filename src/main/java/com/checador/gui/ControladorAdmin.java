package com.checador.gui;

import com.checador.dao.AsistenciaDAO;
import com.checador.model.Trabajador;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ControladorAdmin {

    @FXML private TableView<Trabajador> tablaTrabajadores;
    @FXML private TableColumn<Trabajador, String> colId;
    @FXML private TableColumn<Trabajador, String> colNombre;
    @FXML private TableColumn<Trabajador, String> colApp;
    @FXML private TableColumn<Trabajador, String> colApm;
    @FXML private TableColumn<Trabajador, String> colPuesto;

    @FXML private TextField txtNuevoId;
    @FXML private TextField txtNuevoNombre;
    @FXML private TextField txtApp;
    @FXML private TextField txtApm;
    @FXML private TextField txtPuesto;

    @FXML private TextField txtConfigId;
    @FXML private TextField txtHoraEntrada;
    @FXML private Spinner<Integer> spinMinutosC;
    @FXML private Spinner<Integer> spinMinutosT;
    @FXML private TextField txtHoraSalida;
    @FXML private Button btnAtras;


    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();
    private ObservableList<Trabajador> listaObservable;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApp.setCellValueFactory(new PropertyValueFactory<>("app"));
        colApm.setCellValueFactory(new PropertyValueFactory<>("apm"));
        colPuesto.setCellValueFactory(new PropertyValueFactory<>("puesto"));

        spinMinutosC.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 120, 60));
        spinMinutosT.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 15, 5));

        actualizarTabla();
    }

    @FXML
    private void agregarTrabajador() {
        String id = txtNuevoId.getText().trim();
        String nombre = txtNuevoNombre.getText().trim();
        String app = txtApp.getText().trim();
        String apm = txtApm.getText().trim();
        String puesto = txtPuesto.getText().trim();

        if (id.isEmpty() || nombre.isEmpty() || app.isEmpty() || puesto.isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios.");
            return;
        }

        // Llamada al DAO (necesitarás crear este método en AsistenciaDAO)
        if (asistenciaDAO.registrarTrabajador(id, nombre, app, apm, puesto)) {
            actualizarTabla();
            txtNuevoId.clear();
            txtNuevoNombre.clear();
            txtApp.clear();
            txtApm.clear();
            txtPuesto.clear();
        }
    }

    @FXML
    private void cargarHorarioEmpleado() {
        String idBuscar = txtConfigId.getText().trim();

        if (idBuscar.isEmpty()) {
            mostrarAlerta("Atención", "Por favor, ingresa un ID de empleado para buscar.");
            return;
        }

        Map<String, String> horario = asistenciaDAO.obtenerHorario(idBuscar);

        if (!horario.isEmpty()) {
            txtHoraEntrada.setText(horario.get("entrada"));
            txtHoraSalida.setText(horario.get("salida"));

            // AJUSTE PARA SPINNERS: Convertimos el String de la BD a Entero y lo asignamos
            spinMinutosT.getValueFactory().setValue(Integer.parseInt(horario.get("tolerancia")));
            spinMinutosC.getValueFactory().setValue(Integer.parseInt(horario.get("comida")));

            mostrarAlerta("Éxito", "Horario cargado correctamente.");
        } else {
            mostrarAlerta("Información", "No se encontró un horario previo. Puede asignar uno nuevo.");
            txtHoraEntrada.clear();
            txtHoraSalida.clear();

            // Reseteamos los spinners a un valor por defecto (ej. 0 o 15)
            spinMinutosT.getValueFactory().setValue(0);
            spinMinutosC.getValueFactory().setValue(0);
        }
    }

    @FXML
    private void eliminarTrabajador() {
        // 1. Obtener el trabajador seleccionado en la tabla
        Trabajador seleccionado = tablaTrabajadores.getSelectionModel().getSelectedItem();

        if (seleccionado != null) {
            // Opcional pero recomendado: Una pequeña validación antes de borrar
            if (asistenciaDAO.eliminarTrabajador(seleccionado.getId())) {
                mostrarAlerta("Éxito", "Trabajador eliminado correctamente.");
                actualizarTabla(); // Refresca la lista visualmente
            } else {
                mostrarAlerta("Error", "No se pudo eliminar al trabajador de la base de datos.");
            }
        } else {
            // Si el usuario presionó el botón sin seleccionar a nadie
            mostrarAlerta("Atención", "Por favor, selecciona un trabajador de la tabla primero.");
        }
    }

    @FXML
    private void guardarConfiguracion() {
        String idTrabajador = txtConfigId.getText().trim();
        String entrada = txtHoraEntrada.getText().trim();
        String salida = txtHoraSalida.getText().trim();

        // AJUSTE PARA SPINNERS: Obtenemos el valor entero directamente sin parsear
        int tolerancia = spinMinutosT.getValue();
        int tiempoComida = spinMinutosT.getValue();

        // Validación de campos de texto
        if (idTrabajador.isEmpty() || entrada.isEmpty() || salida.isEmpty()) {
            mostrarAlerta("Campos vacíos", "Por favor, llena el ID y los horarios de entrada/salida.");
            return;
        }

        // Mandamos los datos al DAO (el método que usa INSERT OR REPLACE)
        boolean exito = asistenciaDAO.guardarOHorarioTrabajador(idTrabajador, entrada, salida, tolerancia, tiempoComida);

        if (exito) {
            mostrarAlerta("Éxito", "Horario guardado/actualizado correctamente para el ID: " + idTrabajador);

            // Opcional: Limpiar la pantalla tras guardar
            txtConfigId.clear();
            txtHoraEntrada.clear();
            txtHoraSalida.clear();
            spinMinutosT.getValueFactory().setValue(0);
            spinMinutosC.getValueFactory().setValue(0);
        } else {
            mostrarAlerta("Error", "No se pudo guardar el horario. Asegúrate de que el ID de empleado exista.");
        }
    }

    private void actualizarTabla() {
        // Aquí llamaremos al DAO para obtener la lista
        // Por ahora, simulamos una lista para que no te dé error
        List<Trabajador> lista = asistenciaDAO.listarTrabajadores();
        listaObservable = FXCollections.observableArrayList(lista);
        tablaTrabajadores.setItems(listaObservable);
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void regresarAlChecador() {
        try {
            // 1. Cerrar por completo la ventana de administración actual
            Stage etapaActual = (Stage) btnAtras.getScene().getWindow(); // Asegúrate de tener el fx:id de btnAtras
            etapaActual.close(); // .close() la destruye de la memoria, no solo la esconde

            // 2. Volver a cargar y mostrar la ventana del checador principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/checador/gui/VistaPrincipal.fxml"));
            Parent root = loader.load();

            Stage stageChecador = new Stage();
            stageChecador.setTitle("Checador de Asistencia Oficial");
            stageChecador.setScene(new Scene(root));

            // Al ser una ventana normal e independiente, Manjaro te va a habilitar los botones de minimizar
            stageChecador.show();

        } catch (IOException e) {
            System.err.println("Error al regresar al checador: " + e.getMessage());
        }
    }
}