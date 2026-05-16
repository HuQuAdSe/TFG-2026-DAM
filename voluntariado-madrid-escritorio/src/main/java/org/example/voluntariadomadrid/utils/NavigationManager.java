package org.example.voluntariadomadrid.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.voluntariadomadrid.MainApp;

import java.io.IOException;

// Esta clase gestiona la navegacion entre pantallas de la aplicacion.
// Centraliza los cambios de pantalla para que cualquier controlador
// pueda cambiar de vista con una sola linea de codigo, sin repetir
// la logica de carga de FXML en cada sitio.
public class NavigationManager {

    // Referencia al Stage principal de la aplicacion (la ventana principal).
    // Es estatica para que sea accesible desde cualquier clase sin
    // necesidad de pasar el Stage de pantalla en pantalla.
    private static Stage primaryStage;

    // Guarda la referencia al Stage principal de la aplicacion.
    // Se llama una sola vez desde MainApp al arrancar la aplicacion.
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    // Cambia la pantalla actual cargando un archivo FXML.
    // El parametro fxmlPath es la ruta relativa al archivo FXML
    // dentro de la carpeta resources. Ejemplo:
    // navigateTo("views/LoginView.fxml")
    public static void navigateTo(String fxmlPath) {
        try {
            // Crea el cargador de FXML apuntando al archivo indicado.
            // MainApp.class.getResource busca el archivo dentro de
            // src/main/resources/org/example/voluntariadomadrid/
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource(fxmlPath)
            );

            // Carga el contenido del FXML y lo convierte en un nodo
            // de JavaFX listo para mostrar en pantalla.
            Parent root = loader.load();

            // Crea una nueva escena con el contenido cargado.
            Scene scene = new Scene(root);

            // Reemplaza la escena actual del Stage por la nueva.
            // Esto es lo que produce el cambio de pantalla visible.
            primaryStage.setScene(scene);

            // Centra la ventana en la pantalla tras el cambio.
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            // Si el archivo FXML no existe o tiene errores,
            // muestra el mensaje de error en la consola.
            System.err.println("Error al navegar a: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // Devuelve el Stage principal. Se usa cuando alguna pantalla
    // necesita cambiar el titulo de la ventana o su tamanio.
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}