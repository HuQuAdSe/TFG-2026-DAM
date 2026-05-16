package org.example.voluntariadomadrid.views.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.voluntariadomadrid.services.ControllerService;
import org.example.voluntariadomadrid.utils.SessionManager;

// Controlador del formulario de creacion de nuevo controlador.
// Se abre como ventana emergente desde la lista de controladores.
public class NewControllerController {

    @FXML
    private TextField nombreField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button createButton;

    // Callback que se ejecuta cuando el controlador se crea correctamente.
    // Permite notificar a la lista para que se recargue automaticamente.
    private Runnable onControladorCreado;

    // El DashboardController llama a este metodo para registrar
    // la funcion que se ejecutara al crear el controlador.
    public void setOnControladorCreado(Runnable callback) {
        this.onControladorCreado = callback;
    }

    // Metodo que se ejecuta cuando el usuario pulsa "Crear controlador".
    @FXML
    private void handleCrear() {

        String nombre = nombreField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validacion de campos vacios.
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor, rellena todos los campos.");
            return;
        }

        // Validacion de longitud de contrasena.
        if (password.length() < 6) {
            mostrarError("La contrasena debe tener al menos 6 caracteres.");
            return;
        }

        createButton.setDisable(true);
        createButton.setText("Creando...");
        ocultarError();

        String orgId = SessionManager.getInstance()
                .getOrganizacionActual().getId();

        new Thread(() -> {
            ControllerService.ControllerResult result =
                    ControllerService.crearControlador(
                            nombre, email, password, orgId);

            Platform.runLater(() -> {
                createButton.setDisable(false);
                createButton.setText("Crear controlador");

                if (result.exito) {
                    // Notifica a la lista que se recargue.
                    if (onControladorCreado != null) {
                        onControladorCreado.run();
                    }
                    // Cierra la ventana emergente.
                    cerrarVentana();
                } else {
                    mostrarError(result.mensajeError);
                }
            });
        }).start();
    }

    // Cierra la ventana emergente sin hacer nada.
    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    // Cierra la ventana emergente obteniendo el Stage actual.
    private void cerrarVentana() {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
    }

    private void ocultarError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
}