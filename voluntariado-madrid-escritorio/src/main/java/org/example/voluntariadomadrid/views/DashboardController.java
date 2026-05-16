package org.example.voluntariadomadrid.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.example.voluntariadomadrid.MainApp;
import org.example.voluntariadomadrid.utils.NavigationManager;
import org.example.voluntariadomadrid.utils.SessionManager;
import org.example.voluntariadomadrid.views.organization.OrgProfileController;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

// Controlador del dashboard principal.
// Gestiona el menu lateral y carga dinamicamente las vistas
// de cada seccion en el area de contenido derecho.
public class DashboardController implements Initializable {

    // Etiqueta que muestra el nombre de la organizacion en el menu lateral.
    @FXML
    private Label orgNameLabel;

    // Etiqueta que muestra el rol del usuario en el menu lateral.
    @FXML
    private Label userRoleLabel;

    // Area donde se cargan dinamicamente las vistas de cada seccion.
    @FXML
    private StackPane contentArea;

    // Botones del menu lateral para aplicar el estilo activo/inactivo.
    @FXML
    private Button btnInicio;
    @FXML
    private Button btnOfertas;
    @FXML
    private Button btnVoluntarios;
    @FXML
    private Button btnControladores;
    @FXML
    private Button btnAnaliticas;
    @FXML
    private Button btnOrganizacion;

    // Referencia al boton activo actualmente para poder
    // quitarle el estilo activo cuando se cambia de seccion.
    private Button botonActivo;

    // Metodo que se ejecuta automaticamente al cargar el dashboard.
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        SessionManager session = SessionManager.getInstance();

        // Muestra el nombre de la organizacion en el menu lateral.
        orgNameLabel.setText(
                session.getOrganizacionActual().getNombre());

        // Muestra el rol del usuario con formato legible.
        String rol = session.isAdmin() ? "Administrador" : "Controlador";
        userRoleLabel.setText(session.getUsuarioActual().getNombre()
                + " · " + rol);

        // Oculta el boton de Controladores si el usuario no es admin.
        // Los controladores no pueden crear ni ver otros controladores.
        if (!session.isAdmin()) {
            btnControladores.setVisible(false);
            btnControladores.setManaged(false);
        }

        // Carga la pantalla de inicio por defecto al entrar al dashboard.
        botonActivo = btnInicio;
        cargarVista("views/HomeView.fxml");
    }

    // Carga un archivo FXML en el area de contenido derecho.
    // Reemplaza cualquier vista que hubiera cargada anteriormente.
    private void cargarVista(String fxmlPath) {
        try {
            // Carga el FXML indicado como un nodo de JavaFX.
            Node vista = FXMLLoader.load(
                    MainApp.class.getResource(fxmlPath));

            // Reemplaza el contenido del area derecha con la nueva vista.
            contentArea.getChildren().setAll(vista);

        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // Cambia el estilo visual del boton activo en el menu lateral.
    // El boton seleccionado se pone azul, el anterior vuelve a gris.
    private void activarBoton(Button boton) {

        // Quita el estilo activo del boton que estaba seleccionado.
        if (botonActivo != null) {
            botonActivo.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #8892b0;" +
                            "-fx-font-size: 14px; -fx-padding: 10 16;" +
                            "-fx-background-radius: 6; -fx-cursor: hand;" +
                            "-fx-alignment: CENTER-LEFT;");
        }

        // Aplica el estilo activo al boton recien pulsado.
        boton.setStyle(
                "-fx-background-color: #3f51b5; -fx-text-fill: white;" +
                        "-fx-font-size: 14px; -fx-padding: 10 16;" +
                        "-fx-background-radius: 6; -fx-cursor: hand;" +
                        "-fx-alignment: CENTER-LEFT;");

        // Guarda la referencia al boton activo.
        botonActivo = boton;
    }

    // --- MANEJADORES DE LOS BOTONES DEL MENU ---

    @FXML
    private void handleInicio() {
        activarBoton(btnInicio);
        cargarVista("views/HomeView.fxml");
    }

    @FXML
    private void handleOfertas() {
        activarBoton(btnOfertas);
        cargarVista("views/offers/OfferListView.fxml");
    }

    @FXML
    private void handleVoluntarios() {
        activarBoton(btnVoluntarios);
        cargarVista("views/VolunteerListView.fxml");
    }

    @FXML
    private void handleControladores() {
        activarBoton(btnControladores);
        cargarVista("views/controllers/ControllerListView.fxml");
    }

    @FXML
    private void handleAnaliticas() {
        activarBoton(btnAnaliticas);
        cargarVista("views/analytics/AnalyticsView.fxml");
    }

    @FXML
    private void handleOrganizacion() {
        activarBoton(btnOrganizacion);
        cargarVista("views/organization/OrgProfileView.fxml");
    }

    // Cierra la sesion del usuario y vuelve a la pantalla de login.
    @FXML
    private void handleCerrarSesion() {
        // Borra los datos de sesion del SessionManager.
        SessionManager.getInstance().cerrarSesion();

        // Navega de vuelta a la pantalla de login.
        NavigationManager.navigateTo("views/LoginView.fxml");
    }
}