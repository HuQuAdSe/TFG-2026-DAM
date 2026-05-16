package org.example.voluntariadomadrid.views;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.voluntariadomadrid.services.AuthService;
import org.example.voluntariadomadrid.utils.NavigationManager;

// Controlador de la pantalla de login.
// Gestiona los eventos de la interfaz: el boton de login
// y el enlace para ir al registro de organizacion.
// Cada campo con @FXML esta conectado a un elemento del LoginView.fxml
// mediante el atributo fx:id.
public class LoginController {

    // Campo de texto donde el usuario escribe su correo.
    // El nombre debe coincidir exactamente con el fx:id del FXML.
    @FXML
    private TextField emailField;

    // Campo de contrasena donde el usuario escribe su contrasena.
    // PasswordField oculta el texto automaticamente con asteriscos.
    @FXML
    private PasswordField passwordField;

    // Etiqueta para mostrar mensajes de error al usuario.
    // Esta oculta por defecto y se hace visible si hay un error.
    @FXML
    private Label errorLabel;

    // Boton de login. Se desactiva mientras se procesa el login
    // para evitar que el usuario haga clic varias veces.
    @FXML
    private Button loginButton;

    // Metodo que se ejecuta cuando el usuario pulsa "Iniciar sesion".
    // El prefijo handle es una convencion para metodos de eventos en JavaFX.
    @FXML
    private void handleLogin() {

        // Lee los valores introducidos por el usuario.
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validacion basica: comprueba que los campos no esten vacios
        // antes de hacer ninguna peticion a Firebase.
        if (email.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor, rellena todos los campos.");
            return;
        }

        // Desactiva el boton y muestra texto de carga
        // para que el usuario sepa que la operacion esta en curso.
        loginButton.setDisable(true);
        loginButton.setText("Iniciando sesion...");
        ocultarError();

        // Ejecuta el login en un hilo secundario para no bloquear
        // la interfaz grafica mientras se hace la peticion a Firebase.
        // Si no hicieramos esto, la ventana se congelaria hasta recibir respuesta.
        new Thread(() -> {

            // Llama al servicio de autenticacion con las credenciales del usuario.
            AuthService.LoginResult result = AuthService.login(email, password);

            // Platform.runLater ejecuta el codigo de vuelta en el hilo
            // de JavaFX, que es el unico que puede modificar la interfaz.
            Platform.runLater(() -> {

                // Reactiva el boton independientemente del resultado.
                loginButton.setDisable(false);
                loginButton.setText("Iniciar sesion");

                if (result.exito) {
                    // Login correcto: navega al dashboard principal.
                    // Cambiar a "views/DashboardView.fxml"
                    System.out.println("Login correcto: " + result.usuario.getNombre());
                    NavigationManager.navigateTo("views/DashboardView.fxml");
                } else {
                    // Login fallido: muestra el mensaje de error al usuario.
                    mostrarError(result.mensajeError);
                }
            });

        }).start(); // Inicia el hilo secundario
    }

    // Metodo que se ejecuta cuando el usuario pulsa "Registra tu organizacion".
    // Navega a la pantalla de registro de nueva organizacion.
    @FXML
    private void handleGoToRegister() {

        NavigationManager.navigateTo("views/RegisterOrgView.fxml");
    }

    // Muestra un mensaje de error en la etiqueta de error
    // y hace visible dicha etiqueta.
    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
    }

    // Oculta la etiqueta de error y borra su contenido.
    private void ocultarError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
}