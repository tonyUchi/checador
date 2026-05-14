package com.checador.gui;

import com.checador.dao.AsistenciaDAO;
import com.checador.model.Trabajador;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class ControladorAdmin {

    @FXML private TableView<Trabajador> tablaTrabajadores;
    @FXML private TableColumn<Trabajador, String> colId;
    @FXML private TableColumn<Trabajador, String> colNombre;

    @FXML private TextField txtNuevoId;
    @FXML private TextField txtNuevoNombre;
    @FXML private TextField txtHoraEntrada;
    @FXML private Spinner<Integer> spinMinutos;

    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();
    private ObservableList<Trabajador> listaObservable;

    @FXML
    public void initialize() {
        // 1. Vincular columnas con atributos del modelo Trabajador
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        // 2. Configurar el Spinner
        spinMinutos.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 120, 60));

        // 3. Cargar datos iniciales
        actualizarTabla();
    }

    @FXML
    private void agregarTrabajador() {
        String id = txtNuevoId.getText().trim();
        String nombre = txtNuevoNombre.getText().trim();

        if (id.isEmpty() || nombre.isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios.");
            return;
        }

        // Llamada al DAO (necesitarás crear este método en AsistenciaDAO)
        if (asistenciaDAO.registrarTrabajador(id, nombre)) {
            actualizarTabla();
            txtNuevoId.clear();
            txtNuevoNombre.clear();
        }
    }

    @FXML
    private void eliminarTrabajador() {
        Trabajador seleccionado = tablaTrabajadores.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            // asistenciaDAO.eliminarTrabajador(seleccionado.getId());
            actualizarTabla();
        } else {
            mostrarAlerta("Atención", "Selecciona un trabajador de la tabla.");
        }
    }

    @FXML
    private void guardarConfiguracion() {
        int minutos = spinMinutos.getValue();
        String entrada = txtHoraEntrada.getText().trim();
        // Lógica para guardar en la tabla configuracion
        System.out.println("Guardando: " + minutos + " min y entrada " + entrada);
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
}