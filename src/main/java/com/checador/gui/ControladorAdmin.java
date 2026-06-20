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

    /*
    Declaracion de los elementos utilizados en el escene buider.
     */
    @FXML private TableView<Trabajador> tablaTrabajadores;
    @FXML private TableColumn<Trabajador, String> colId;
    @FXML private TableColumn<Trabajador, String> colNombre;
    @FXML private TableColumn<Trabajador, String> colApp;
    @FXML private TableColumn<Trabajador, String> colApm;
    @FXML private TableColumn<Trabajador, String> colPuesto;
    @FXML private TextField txtNuevoId;
    @FXML private TextField txtNuevoNombre;
    @FXML private TextField txtIdPeriodoAdmin;
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private TextField txtApp;
    @FXML private TextField txtApm;
    @FXML private TextField txtPuesto;
    @FXML private TextField txtConfigId;
    @FXML private TextField txtHoraEntrada;
    @FXML private Spinner<Integer> spinMinutosC;
    @FXML private Spinner<Integer> spinMinutosT;
    @FXML private ComboBox<String> cmbTipoPeriodo;
    @FXML private TextField txtHoraSalida;
    @FXML private Button btnAtras;
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();
    private ObservableList<Trabajador> listaObservable;

    /*
    Se inicializan loa valores que por defecto se cargaran al iniciarse la aplicación
     */
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApp.setCellValueFactory(new PropertyValueFactory<>("app"));
        colApm.setCellValueFactory(new PropertyValueFactory<>("apm"));
        colPuesto.setCellValueFactory(new PropertyValueFactory<>("puesto"));
        spinMinutosC.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 120, 60));
        spinMinutosT.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 15, 5));
        cmbTipoPeriodo.getItems().addAll("VACACIONES", "DESCANSO");
        cmbTipoPeriodo.setValue("DESCANSO");
        actualizarTabla();
    }

    /*
    Método que recolecta los valores ingresados en las cajas de texto y manda esta información al DAO para realizar el registro de un trabajador en la base de datos.
     */
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

        if (asistenciaDAO.registrarTrabajador(id, nombre, app, apm, puesto)) {
            actualizarTabla();
            txtNuevoId.clear();
            txtNuevoNombre.clear();
            txtApp.clear();
            txtApm.clear();
            txtPuesto.clear();
        }
    }

    /*
    Método que busca por el Id del trabajador su horario asignado, si existe uno lo cargara en pantalla, de lo contrario se podrá establecer uno nuevo.
     */
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
            spinMinutosT.getValueFactory().setValue(Integer.parseInt(horario.get("tolerancia")));
            spinMinutosC.getValueFactory().setValue(Integer.parseInt(horario.get("comida")));
            mostrarAlerta("Éxito", "Horario cargado correctamente.");
        } else {
            mostrarAlerta("Información", "No se encontró un horario previo. Puede asignar uno nuevo.");
            txtHoraEntrada.clear();
            txtHoraSalida.clear();
            spinMinutosT.getValueFactory().setValue(0);
            spinMinutosC.getValueFactory().setValue(0);
        }
    }

    /*
    Método que permite seleccionar y eliminar a un trabajador de la tabla, trabajadores.
     */
    @FXML
    private void eliminarTrabajador() {
        Trabajador seleccionado = tablaTrabajadores.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            if (asistenciaDAO.eliminarTrabajador(seleccionado.getId())) {
                mostrarAlerta("Éxito", "Trabajador eliminado correctamente.");
                actualizarTabla();
            } else {
                mostrarAlerta("Error", "No se pudo eliminar al trabajador de la base de datos.");
            }
        } else {
            mostrarAlerta("Atención", "Por favor, selecciona un trabajador de la tabla primero.");
        }
    }

    /*
    Método que permite obtener los valores de las cajas de texto para los horarios de los trabajadores y después los enviá al DAO, para que se puedan guardar en la base de datos.
     */
    @FXML
    private void guardarConfiguracion() {
        String idTrabajador = txtConfigId.getText().trim();
        String entrada = txtHoraEntrada.getText().trim();
        String salida = txtHoraSalida.getText().trim();
        int tolerancia = spinMinutosT.getValue();
        int tiempoComida = spinMinutosT.getValue();
        if (idTrabajador.isEmpty() || entrada.isEmpty() || salida.isEmpty()) {
            mostrarAlerta("Campos vacíos", "Por favor, llena el ID y los horarios de entrada/salida.");
            return;
        }
        boolean exito = asistenciaDAO.guardarOHorarioTrabajador(idTrabajador, entrada, salida, tolerancia, tiempoComida);
        if (exito) {
            mostrarAlerta("Éxito", "Horario guardado/actualizado correctamente para el ID: " + idTrabajador);
            txtConfigId.clear();
            txtHoraEntrada.clear();
            txtHoraSalida.clear();
            spinMinutosT.getValueFactory().setValue(0);
            spinMinutosC.getValueFactory().setValue(0);
        } else {
            mostrarAlerta("Error", "No se pudo guardar el horario. Asegúrate de que el ID de empleado exista.");
        }
    }

    /*
    Método que permite obtener una lista de los trabajadores desde la base de datos y con ello rellenar la tabla que se le mostrara al administrador.
     */
    private void actualizarTabla() {
        List<Trabajador> lista = asistenciaDAO.listarTrabajadores();
        listaObservable = FXCollections.observableArrayList(lista);
        tablaTrabajadores.setItems(listaObservable);
    }

    /*
    Metodo que agrega el registro para dia de vacaciones o descanso
     */

    @FXML
    public void guardarNuevoPeriodo() {
        if (txtIdPeriodoAdmin.getText().trim().isEmpty() ||
                cmbTipoPeriodo.getValue() == null ||
                dpFechaInicio.getValue() == null ||
                dpFechaFin.getValue() == null) {

            mostrarAlerta("Campos Incompletos", "Por favor, llena todos los campos antes de guardar.");
            return;
        }

        String id = txtIdPeriodoAdmin.getText().trim();
        String tipo = cmbTipoPeriodo.getValue();
        String inicio = dpFechaInicio.getValue().toString();
        String fin = dpFechaFin.getValue().toString();

        if (dpFechaFin.getValue().isBefore(dpFechaInicio.getValue())) {
            mostrarAlerta("Error de Fechas", "La fecha de fin no puede ser anterior a la fecha de inicio.");
            return;
        }

        boolean existeEmpleado = asistenciaDAO.existeTrabajador(id);

        if (!existeEmpleado) {
            mostrarAlerta("Trabajador No Encontrado",
                    "El ID '" + id + "' no corresponde a ningún trabajador registrado.");
            return;
        }
        boolean exito = asistenciaDAO.registrarPeriodoLibre(id, tipo, inicio, fin);
        if (exito) {
            mostrarAlerta("Registro Exitoso", "El periodo de " + tipo + " fue asignado correctamente.");
            txtIdPeriodoAdmin.clear();
            dpFechaInicio.setValue(null);
            dpFechaFin.setValue(null);
        } else {
            mostrarAlerta("Error", "No se pudo registrar el periodo. Verifica si el ID del trabajador es correcto.");
        }
    }

    /*
    Método que nos ayuda a mostrar las diferentes alertas que el programa puede lanzar.
     */
    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /*
    Método que se encarga de cerrar la ventana del administrador y regresar a la pantalla principal del programa.
     */
    @FXML
    private void regresarAlChecador() {
        try {
            Stage etapaActual = (Stage) btnAtras.getScene().getWindow();
            etapaActual.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/checador/gui/VistaPrincipal.fxml"));
            Parent root = loader.load();
            Stage stageChecador = new Stage();
            stageChecador.setTitle("Checador de Asistencia Oficial");
            stageChecador.setScene(new Scene(root));
            stageChecador.show();
        } catch (IOException e) {
            System.err.println("Error al regresar al checador: " + e.getMessage());
        }
    }

}