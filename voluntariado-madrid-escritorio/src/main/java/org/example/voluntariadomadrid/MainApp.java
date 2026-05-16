package org.example.voluntariadomadrid;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.voluntariadomadrid.utils.NavigationManager;

// Clase principal de la aplicacion JavaFX.
// Es el punto de entrada de toda la app de escritorio.
// Extiende Application, que es la clase base de JavaFX
// para cualquier aplicacion de escritorio.
public class MainApp extends Application {

    // Metodo que se ejecuta ANTES de que aparezca la ventana.
    // Es el lugar correcto para inicializar servicios externos
    // como Firebase, porque no bloquea el hilo de JavaFX.
    @Override
    public void init() throws Exception {
        // Inicializa la conexion con Firebase usando el archivo
        // de credenciales configurado en FirebaseConfig.
        FirebaseConfig.init();
        System.out.println("Firebase conectado correctamente");
    }

    // Metodo principal de JavaFX. Se ejecuta despues de init().
    // Aqui se configura y muestra la ventana principal.
    // El parametro stage es la ventana principal que JavaFX
    // crea automaticamente al arrancar la aplicacion.
    @Override
    public void start(Stage stage) {

        // Registra el Stage principal en NavigationManager.
        NavigationManager.setPrimaryStage(stage);

        // Configura el titulo y tamaño de la ventana principal.
        stage.setTitle("Voluntariado Madrid");
        stage.setWidth(900);
        stage.setHeight(600);
        stage.setMinWidth(800);
        stage.setMinHeight(550);

        // Navega directamente a la pantalla de login al arrancar.
        NavigationManager.navigateTo("views/LoginView.fxml");

        // Muestra la ventana en pantalla.
        stage.show();
    }

    // Punto de entrada del programa. Llama a launch() que
    // inicializa JavaFX y llama a init() y start() en orden.
    public static void main(String[] args) {
        launch(args);
    }
}