package org.example.voluntariadomadrid.views;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.example.voluntariadomadrid.models.VolunteerOffer;
import org.example.voluntariadomadrid.services.EnrollmentService;
import org.example.voluntariadomadrid.services.OfferService;
import org.example.voluntariadomadrid.utils.NavigationManager;
import org.example.voluntariadomadrid.utils.PdfGenerator;
import org.example.voluntariadomadrid.utils.SessionManager;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

// Controlador de la pantalla de voluntarios inscritos.
// Muestra un selector de oferta y una tabla con los voluntarios
// inscritos en la oferta seleccionada.
public class VolunteerListController implements Initializable {

    // Combo desplegable para seleccionar la oferta a consultar.
    @FXML
    private ComboBox<VolunteerOffer> ofertaCombo;

    // Tabla que muestra los voluntarios inscritos en la oferta seleccionada.
    @FXML
    private TableView<EnrollmentService.EnrollmentInfo> tableInscritos;

    // Columnas de la tabla.
    @FXML
    private TableColumn<EnrollmentService.EnrollmentInfo, String> colNombre;
    @FXML
    private TableColumn<EnrollmentService.EnrollmentInfo, String> colEmail;
    @FXML
    private TableColumn<EnrollmentService.EnrollmentInfo, String> colFecha;
    @FXML
    private TableColumn<EnrollmentService.EnrollmentInfo, String> colEstado;
    @FXML
    private TableColumn<EnrollmentService.EnrollmentInfo, Boolean> colCertificado;
    // Columna con el boton para emitir el certificado PDF.
    @FXML
    private TableColumn<EnrollmentService.EnrollmentInfo, Void> colAcciones;

    // Etiqueta que muestra cuantos voluntarios hay inscritos.
    @FXML
    private Label resumenLabel;

    // Etiqueta de estado para mensajes al usuario.
    @FXML
    private Label statusLabel;

    // Formato de fecha para mostrar en la tabla.
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Metodo que se ejecuta automaticamente al cargar la vista.
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Configura el combo para mostrar el titulo de cada oferta.
        // Sin esto el combo mostraria el toString() del objeto.
        ofertaCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(VolunteerOffer oferta, boolean empty) {
                super.updateItem(oferta, empty);
                if (empty || oferta == null) {
                    setText(null);
                } else {
                    // Muestra titulo y estado de la oferta en el combo.
                    setText(oferta.getTitulo()
                            + " (" + oferta.getEstado() + ")");
                }
            }
        });

        // Configura como se muestra el elemento seleccionado en el combo.
        ofertaCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(VolunteerOffer oferta, boolean empty) {
                super.updateItem(oferta, empty);
                if (empty || oferta == null) {
                    setText(null);
                } else {
                    setText(oferta.getTitulo()
                            + " (" + oferta.getEstado() + ")");
                }
            }
        });

        // Configura las columnas de la tabla.
        configurarColumnas();

        // Carga las ofertas de la organizacion en el combo.
        cargarOfertas();
    }

    // Configura las columnas de la tabla con su logica de visualizacion.
    private void configurarColumnas() {

        // Columna nombre completo del voluntario.
        // Usa getNombreCompleto() del EnrollmentInfo.
        colNombre.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getNombreCompleto()));

        // Columna email del voluntario.
        colEmail.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().emailVoluntario));

        colFecha.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().fechaInscripcion.toString()));


        colEstado.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().estado));

        colCertificado.setCellValueFactory(data ->
                new javafx.beans.property.SimpleBooleanProperty(data.getValue().certificadoEmitido));

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnCertificado = new Button("Emitir certificado");

            {
                btnCertificado.setStyle(
                        "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;" +
                                "-fx-font-size: 12px; -fx-padding: 4 10;" +
                                "-fx-background-radius: 4; -fx-cursor: hand;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                EnrollmentService.EnrollmentInfo info =
                        getTableView().getItems().get(getIndex());
                // El boton siempre esta visible para cualquier inscripcion.
                btnCertificado.setOnAction(e -> emitirCertificado(info));
                setGraphic(btnCertificado);
            }
        });
    }

    // Carga todas las ofertas de la organizacion en el combo desplegable.
    // Se ejecuta al inicializar la pantalla.
    private void cargarOfertas() {

        String orgId = SessionManager.getInstance()
                .getOrganizacionActual().getId();

        new Thread(() -> {
            OfferService.OfferResult result =
                    OfferService.listarOfertas(orgId);

            Platform.runLater(() -> {
                if (result.exito && !result.ofertas.isEmpty()) {
                    ofertaCombo.getItems().setAll(result.ofertas);
                    statusLabel.setText(
                            "Selecciona una oferta para ver los voluntarios inscritos.");
                } else if (result.exito) {
                    statusLabel.setText(
                            "No hay ofertas creadas todavia en tu organizacion.");
                } else {
                    statusLabel.setText(result.mensajeError);
                }
            });
        }).start();
    }

    // Metodo que se ejecuta cuando el usuario selecciona una oferta
    // en el combo desplegable. Carga los inscritos de esa oferta.
    @FXML
    private void handleOfertaSeleccionada() {

        VolunteerOffer ofertaSeleccionada = ofertaCombo.getValue();

        // Comprueba que hay una oferta seleccionada antes de consultar.
        if (ofertaSeleccionada == null) return;

        statusLabel.setText("Cargando voluntarios inscritos...");
        statusLabel.setVisible(true);
        resumenLabel.setVisible(false);
        tableInscritos.getItems().clear();

        new Thread(() -> {
            EnrollmentService.EnrollmentResult result =
                    EnrollmentService.listarInscritosPorOferta(
                            ofertaSeleccionada.getId());

            Platform.runLater(() -> {
                if (result.exito) {
                    tableInscritos.getItems()
                            .setAll(result.inscripciones);

                    int total = result.inscripciones.size();
                    int plazasTotal = ofertaSeleccionada.getPlazasTotal();

                    if (total == 0) {
                        statusLabel.setText(
                                "No hay voluntarios inscritos en esta oferta todavia.");
                        statusLabel.setVisible(true);
                        resumenLabel.setVisible(false);
                    } else {
                        // Muestra un resumen con el numero de inscritos
                        // y las plazas totales de la oferta.
                        statusLabel.setVisible(false);
                        resumenLabel.setText(total + " voluntario"
                                + (total != 1 ? "s" : "")
                                + " inscritos de " + plazasTotal
                                + " plazas disponibles");
                        resumenLabel.setVisible(true);
                    }
                } else {
                    statusLabel.setText(result.mensajeError);
                    statusLabel.setVisible(true);
                }
            });
        }).start();
    }

    // Genera el certificado PDF para un voluntario y lo guarda
    // en la ubicacion que el usuario elija mediante un dialogo.
    private void emitirCertificado(EnrollmentService.EnrollmentInfo info) {

        // Abre un dialogo para que el usuario elija donde guardar el PDF.
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Guardar certificado");
        fileChooser.setInitialFileName(
                "certificado_" + info.nombreVoluntario.replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));

        java.io.File archivo = fileChooser.showSaveDialog(
                NavigationManager.getPrimaryStage());

        // Si el usuario cancela el dialogo no hace nada.
        if (archivo == null) return;

        String nombreOrg = SessionManager.getInstance()
                .getOrganizacionActual().getNombre();
        String nombreVoluntario = info.getNombreCompleto();

        new Thread(() -> {
            boolean generado = PdfGenerator.generarCertificado(
                    nombreOrg, nombreVoluntario, archivo.getAbsolutePath());

            Platform.runLater(() -> {
                if (generado) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Certificado generado");
                    alert.setHeaderText(null);
                    alert.setContentText("Certificado guardado correctamente.");
                    alert.initOwner(NavigationManager.getPrimaryStage());
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText(
                            "No se pudo generar el certificado. Intentalo de nuevo.");
                    alert.initOwner(NavigationManager.getPrimaryStage());
                    alert.showAndWait();
                }
            });
        }).start();
    }
}