package org.example.voluntariadomadrid.views;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.example.voluntariadomadrid.utils.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

// Controlador de la pantalla de bienvenida del dashboard.
// Muestra el saludo personalizado y las tarjetas de resumen
// con estadisticas basicas de la organizacion.
public class HomeController implements Initializable {

    // Etiqueta del saludo con el nombre del usuario logueado.
    @FXML
    private Label welcomeLabel;

    // Etiqueta con el nombre de la organizacion.
    @FXML
    private Label orgLabel;

    // Etiqueta que muestra el numero de ofertas activas.
    @FXML
    private Label ofertasActivasLabel;

    // Etiqueta que muestra el total de voluntarios inscritos.
    @FXML
    private Label totalInscritosLabel;

    // Etiqueta que muestra el numero de ofertas completadas.
    @FXML
    private Label ofertasCompletadasLabel;

    // Metodo que se ejecuta automaticamente al cargar la vista.
    // Inicializa los datos de la pantalla de bienvenida.
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Obtiene los datos de la sesion activa.
        SessionManager session = SessionManager.getInstance();

        // Muestra el saludo con el nombre del usuario logueado.
        welcomeLabel.setText("Bienvenido, " + session.getUsuarioActual().getNombre());

        // Muestra el nombre de la organizacion del usuario.
        orgLabel.setText(session.getOrganizacionActual().getNombre());

        // Carga las estadisticas desde Firestore en un hilo secundario
        // para no bloquear la interfaz mientras se obtienen los datos.
        cargarEstadisticas();
    }

    // Consulta Firestore para obtener las estadisticas basicas
    // de la organizacion y las muestra en las tarjetas.
    private void cargarEstadisticas() {

        // Obtiene el ID de la organizacion del usuario logueado.
        String orgId = SessionManager.getInstance()
                .getOrganizacionActual().getId();

        new Thread(() -> {
            try {
                Firestore db = FirestoreClient.getFirestore();

                // Cuenta las ofertas activas de esta organizacion.
                QuerySnapshot activas = db.collection("volunteer_offers")
                        .whereEqualTo("organizacionId", orgId)
                        .whereEqualTo("estado", "activo")
                        .get().get();

                // Cuenta las ofertas completadas de esta organizacion.
                QuerySnapshot completadas = db.collection("volunteer_offers")
                        .whereEqualTo("organizacionId", orgId)
                        .whereEqualTo("estado", "completado")
                        .get().get();

                // Cuenta el total de inscripciones de esta organizacion.
                QuerySnapshot inscritos = db.collection("enrollments")
                        .whereEqualTo("organizacionId", orgId)
                        .get().get();

                // Actualiza las etiquetas en el hilo de JavaFX.
                Platform.runLater(() -> {
                    ofertasActivasLabel.setText(
                            String.valueOf(activas.size()));
                    ofertasCompletadasLabel.setText(
                            String.valueOf(completadas.size()));
                    totalInscritosLabel.setText(
                            String.valueOf(inscritos.size()));
                });

            } catch (Exception e) {
                System.err.println("Error al cargar estadisticas: "
                        + e.getMessage());
            }
        }).start();
    }
}