package org.example.voluntariadomadrid.views.offers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.voluntariadomadrid.MainApp;
import org.example.voluntariadomadrid.models.VolunteerOffer;
import org.example.voluntariadomadrid.services.OfferService;
import org.example.voluntariadomadrid.utils.NavigationManager;
import org.example.voluntariadomadrid.utils.SessionManager;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

// Controlador de la pantalla de lista de ofertas.
// Muestra todas las ofertas de la organizacion en una tabla
// y permite crear nuevas, editarlas o cambiar su estado.
public class OfferListController implements Initializable {

    @FXML private TableView<VolunteerOffer> tableOfertas;
    @FXML private TableColumn<VolunteerOffer, String> colTitulo;
    @FXML private TableColumn<VolunteerOffer, String> colCategoria;
    @FXML private TableColumn<VolunteerOffer, Integer> colPlazas;
    @FXML private TableColumn<VolunteerOffer, String> colEstado;
    @FXML private TableColumn<VolunteerOffer, Date> colFechaInicio;
    @FXML private TableColumn<VolunteerOffer, Date> colFechaFin;
    @FXML private TableColumn<VolunteerOffer, Void> colAcciones;
    @FXML private Label statusLabel;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Enlaza las columnas simples con los campos del modelo.
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        // Columna plazas: muestra "ocupadas / total".
        colPlazas.setCellValueFactory(new PropertyValueFactory<>("plazasTotal"));
        colPlazas.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    VolunteerOffer o = getTableView()
                            .getItems().get(getIndex());
                    setText(o.getPlazasOcupadas() + " / " + total);
                }
            }
        });

        // Columna estado con color segun valor.
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    switch (estado) {
                        case "activo" ->
                                setStyle("-fx-text-fill: #43a047; -fx-font-weight: bold;");
                        case "desactivado" ->
                                setStyle("-fx-text-fill: #e53935; -fx-font-weight: bold;");
                        case "completado" ->
                                setStyle("-fx-text-fill: #fb8c00; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });

        // Columnas de fecha.
        colFechaInicio.setCellValueFactory(
                new PropertyValueFactory<>("fechaInicio"));
        colFechaInicio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Date fecha, boolean empty) {
                super.updateItem(fecha, empty);
                setText((empty || fecha == null) ? null : sdf.format(fecha));
            }
        });

        colFechaFin.setCellValueFactory(
                new PropertyValueFactory<>("fechaFin"));
        colFechaFin.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Date fecha, boolean empty) {
                super.updateItem(fecha, empty);
                setText((empty || fecha == null) ? null : sdf.format(fecha));
            }
        });

        // Columna de acciones con boton editar y boton cambiar estado.
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEstado = new Button();
            private final javafx.scene.layout.HBox box =
                    new javafx.scene.layout.HBox(6, btnEditar, btnEstado);

            {
                // Estilo del boton editar.
                btnEditar.setStyle(
                        "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0;" +
                                "-fx-font-size: 12px; -fx-padding: 4 10;" +
                                "-fx-background-radius: 4; -fx-cursor: hand;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    VolunteerOffer oferta = getTableView()
                            .getItems().get(getIndex());

                    // Configura el boton de estado segun el estado actual.
                    if ("activo".equals(oferta.getEstado())) {
                        btnEstado.setText("Desactivar");
                        btnEstado.setStyle(
                                "-fx-background-color: #ffebee; -fx-text-fill: #e53935;" +
                                        "-fx-font-size: 12px; -fx-padding: 4 10;" +
                                        "-fx-background-radius: 4; -fx-cursor: hand;");
                    } else if ("desactivado".equals(oferta.getEstado())) {
                        btnEstado.setText("Activar");
                        btnEstado.setStyle(
                                "-fx-background-color: #e8f5e9; -fx-text-fill: #43a047;" +
                                        "-fx-font-size: 12px; -fx-padding: 4 10;" +
                                        "-fx-background-radius: 4; -fx-cursor: hand;");
                    } else {
                        // Las ofertas completadas no pueden cambiar estado.
                        btnEstado.setVisible(false);
                    }

                    btnEditar.setOnAction(e -> abrirFormulario(oferta));
                    btnEstado.setOnAction(e -> cambiarEstado(oferta));
                    setGraphic(box);
                }
            }
        });

        cargarOfertas();
    }

    // Carga las ofertas desde Firestore en un hilo secundario.
    private void cargarOfertas() {
        statusLabel.setText("Cargando ofertas...");
        statusLabel.setVisible(true);

        String orgId = SessionManager.getInstance()
                .getOrganizacionActual().getId();

        new Thread(() -> {
            OfferService.OfferResult result =
                    OfferService.listarOfertas(orgId);

            Platform.runLater(() -> {
                if (result.exito) {
                    tableOfertas.getItems().setAll(result.ofertas);
                    statusLabel.setVisible(
                            result.ofertas.isEmpty());
                    if (result.ofertas.isEmpty()) {
                        statusLabel.setText(
                                "No hay ofertas creadas todavia.");
                    }
                } else {
                    statusLabel.setText(result.mensajeError);
                }
            });
        }).start();
    }

    // Abre el formulario modal para crear una nueva oferta
    // o editar una existente.
    private void abrirFormulario(VolunteerOffer ofertaExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource(
                            "views/offers/NewOfferView.fxml"));
            Parent root = loader.load();

            NewOfferController ctrl = loader.getController();

            // Si se pasa una oferta existente es edicion, si es null es creacion.
            if (ofertaExistente != null) {
                ctrl.setOfertaParaEditar(ofertaExistente);
            }

            // Registrar un callback — es decir
            // una funcion que se ejecutara en el futuro cuando ocurra algo concreto.
            ctrl.setOnOfertaGuardada(() -> cargarOfertas());

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(NavigationManager.getPrimaryStage());
            modal.setTitle(ofertaExistente == null
                    ? "Nueva oferta" : "Editar oferta");
            modal.setScene(new Scene(root));
            modal.setResizable(false);
            modal.showAndWait();

        } catch (Exception e) {
            System.err.println("Error al abrir formulario de oferta: "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    // Boton nueva oferta — abre el formulario sin oferta existente.
    @FXML
    private void handleNuevaOferta() {
        abrirFormulario(null);
    }

    // Cambia el estado de una oferta entre activo y desactivado.
    private void cambiarEstado(VolunteerOffer oferta) {
        String nuevoEstado = "activo".equals(oferta.getEstado())
                ? "desactivado" : "activo";
        String accion = "activo".equals(oferta.getEstado())
                ? "desactivar" : "activar";

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar accion");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("Estas seguro de que quieres "
                + accion + " la oferta \"" + oferta.getTitulo() + "\"?");
        confirmacion.initOwner(NavigationManager.getPrimaryStage());

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                new Thread(() -> {
                    OfferService.OfferResult result =
                            OfferService.cambiarEstado(
                                    oferta.getId(), nuevoEstado);

                    Platform.runLater(() -> {
                        if (result.exito) {
                            cargarOfertas();
                        } else {
                            statusLabel.setText(result.mensajeError);
                            statusLabel.setVisible(true);
                        }
                    });
                }).start();
            }
        });
    }
}