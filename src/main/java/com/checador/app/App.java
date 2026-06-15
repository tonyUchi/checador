package com.checador.app;

import com.checador.db.DatabaseInit;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/checador/gui/VistaPrincipal.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);

        stage.setTitle("Checador");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        // Inicializamos la base de datos antes de lanzar la GUI
        DatabaseInit.crearTablas();

        // Lanzamos la aplicación JavaFX
        launch();
    }
}