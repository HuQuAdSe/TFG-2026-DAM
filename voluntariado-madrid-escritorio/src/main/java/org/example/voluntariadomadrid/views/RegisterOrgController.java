package org.example.voluntariadomadrid.views;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.voluntariadomadrid.services.OrganizationService;
import org.example.voluntariadomadrid.utils.NavigationManager;
import org.example.voluntariadomadrid.utils.SessionManager;

// Controlador de la pantalla de registro de organizacion.
// Gestiona el formulario de registro y llama al servicio
// para crear el usuario y la organizacion en Firebase.
public class RegisterOrgController {

    // Campo para el nombre completo del administrador.
    @FXML
    private TextField nombreAdminField;

    // Campo para el correo electronico del administrador.
    @FXML
    private TextField emailField;

    // Campo para la contrasena del administrador.
    @FXML
    private PasswordField passwordField;

    // Campo para confirmar la contrasena.
    @FXML
    private PasswordField confirmPasswordField;

    // Campo para el nombre de la organizacion.
    @FXML
    private TextField nombreOrgField;

    // Campo para la descripcion de la organizacion.
    @FXML
    private TextArea descripcionOrgField;

    // Etiqueta para mostrar mensajes de error al usuario.
    @FXML
    private Label errorLabel;

    // Boton de registro. Se desactiva mientras se procesa
    // para evitar envios multiples.
    @FXML
    private Button registerButton;

    // Metodo que se ejecuta cuando el usuario pulsa "Crear organizacion".
    @FXML
    private void handleRegister() {

        // Lee todos los campos del formulario.
        String nombreAdmin = nombreAdminField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String nombreOrg = nombreOrgField.getText().trim();
        String descripcionOrg = descripcionOrgField.getText().trim();

        // Validacion: comprueba que ningun campo este vacio.
        if (nombreAdmin.isEmpty() || email.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty() || nombreOrg.isEmpty()) {
            mostrarError("Por favor, rellena todos los campos obligatorios.");
            return;
        }

        // Validacion: comprueba que las dos contrasenas coinciden.
        if (!password.equals(confirmPassword)) {
            mostrarError("Las contrasenas no coinciden.");
            return;
        }

        // Validacion: comprueba que la contrasena tiene al menos 6 caracteres.
        if (password.length() < 6) {
            mostrarError("La contrasena debe tener al menos 6 caracteres.");
            return;
        }

        // Desactiva el boton y muestra texto de carga.
        registerButton.setDisable(true);
        registerButton.setText("Creando organizacion...");
        ocultarError();

        // Ejecuta el registro en un hilo secundario para no
        // bloquear la interfaz mientras se procesa en Firebase.
        new Thread(() -> {

            OrganizationService.RegisterResult result =
                    OrganizationService.registrarOrganizacion(
                            nombreAdmin, email, password, nombreOrg, descripcionOrg);

            // Vuelve al hilo de JavaFX para actualizar la interfaz.
            Platform.runLater(() -> {

                registerButton.setDisable(false);
                registerButton.setText("Crear organizacion");

                if (result.exito) {
                    // Guarda la sesion automaticamente con los datos creados.
                    // El usuario queda logueado justo despues de registrarse.
                    SessionManager.getInstance().iniciarSesion(
                            result.usuario, result.organizacion);

                    // Navega directamente al dashboard.
                    NavigationManager.navigateTo("views/DashboardView.fxml");
                } else {
                    mostrarError(result.mensajeError);
                }
            });

        }).start();
    }

    // Metodo que se ejecuta cuando el usuario pulsa "Inicia sesion".
    // Vuelve a la pantalla de login.
    @FXML
    private void handleGoToLogin() {
        NavigationManager.navigateTo("views/LoginView.fxml");
    }

    // Muestra un mensaje de error en la etiqueta de error.
    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
    }

    // Oculta la etiqueta de error.
    private void ocultarError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
}