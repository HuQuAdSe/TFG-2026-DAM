package org.example.voluntariadomadrid.views.controllers;

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
import org.example.voluntariadomadrid.models.ControllerUser;
import org.example.voluntariadomadrid.services.ControllerService;
import org.example.voluntariadomadrid.utils.NavigationManager;
import org.example.voluntariadomadrid.utils.SessionManager;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

// Controlador de la pantalla de lista de controladores.
// Muestra todos los controladores de la organizacion en una tabla
// y permite crear nuevos o activar/desactivar los existentes.
public class ControllerListController implements Initializable {

    @FXML
    private TableView<ControllerUser> tableControladores;

    @FXML
    private TableColumn<ControllerUser, String> colNombre;

    @FXML
    private TableColumn<ControllerUser, String> colEmail;

    @FXML
    private TableColumn<ControllerUser, Boolean> colEstado;

    @FXML
    private TableColumn<ControllerUser, Date> colFecha;

    @FXML
    private TableColumn<ControllerUser, Void> colAcciones;

    @FXML
    private Label statusLabel;


    // Metodo que se ejecuta automaticamente al cargar la vista.
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Configura las columnas de la tabla enlazandolas con
        // los campos del modelo ControllerUser.
        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(
                new PropertyValueFactory<>("email"));

        // Columna de estado: muestra "Activo" o "Inactivo"
        // con color verde o rojo segun el valor del campo activo.
        colEstado.setCellValueFactory(
                new PropertyValueFactory<>("activo"));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean activo, boolean empty) {
                super.updateItem(activo, empty);
                if (empty || activo == null) {
                    setText(null);
                    setStyle("");
                } else if (activo) {
                    setText("Activo");
                    setStyle("-fx-text-fill: #43a047; -fx-font-weight: bold;");
                } else {
                    setText("Inactivo");
                    setStyle("-fx-text-fill: #e53935; -fx-font-weight: bold;");
                }
            }
        });

        // Columna de fecha: formatea el Date a texto legible.
        colFecha.setCellValueFactory(
                new PropertyValueFactory<>("fechaCreacion"));

        // Columna de acciones: boton para activar o desactivar.
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ControllerUser user = getTableView()
                            .getItems().get(getIndex());

                    // Cambia el texto y color del boton segun el estado.
                    if (user.isActivo()) {
                        btn.setText("Desactivar");
                        btn.setStyle(
                                "-fx-background-color: #ffebee;" +
                                        "-fx-text-fill: #e53935;" +
                                        "-fx-font-size: 12px;" +
                                        "-fx-padding: 4 10;" +
                                        "-fx-background-radius: 4;" +
                                        "-fx-cursor: hand;");
                    } else {
                        btn.setText("Activar");
                        btn.setStyle(
                                "-fx-background-color: #e8f5e9;" +
                                        "-fx-text-fill: #43a047;" +
                                        "-fx-font-size: 12px;" +
                                        "-fx-padding: 4 10;" +
                                        "-fx-background-radius: 4;" +
                                        "-fx-cursor: hand;");
                    }

                    // Al pulsar el boton cambia el estado del controlador.
                    btn.setOnAction(e -> cambiarEstado(user));
                    setGraphic(btn);
                }
            }
        });

        // Carga la lista de controladores desde Firestore.
        cargarControladores();
    }

    // Carga los controladores de la organizacion desde Firestore
    // en un hilo secundario para no bloquear la interfaz.
    private void cargarControladores() {
        statusLabel.setText("Cargando controladores...");
        statusLabel.setVisible(true);

        String orgId = SessionManager.getInstance()
                .getOrganizacionActual().getId();

        new Thread(() -> {
            ControllerService.ControllerResult result =
                    ControllerService.listarControladores(orgId);

            // Platform.runLater envía este bloque al hilo principal de JavaFX.
            // Es necesario porque cualquier modificacion de la interfaz grafica
            // (tablas, etiquetas, botones) debe hacerse en el hilo de la UI.
            Platform.runLater(() -> {

                // Verifica si la consulta a Firestore fue exitosa.
                if (result.exito) {

                    // Reemplaza todo el contenido actual de la tabla por la lista
                    // de controladores obtenida desde Firebase.
                    // tableControladores es un TableView<ControllerUser>.
                    tableControladores.getItems()
                            .setAll(result.controladores);

                    // Si no hay controladores para esta organizacion,
                    // muestra un mensaje informativo en la etiqueta de estado.
                    if (result.controladores.isEmpty()) {
                        statusLabel.setText(
                                "No hay controladores creados todavia.");
                    } else {
                        // Si hay controladores, oculta la etiqueta de estado
                        // porque ya no es necesaria (la tabla muestra los datos).
                        statusLabel.setVisible(false);
                    }

                } else {
                    // Si la consulta fallo (result.exito == false),
                    // muestra el mensaje de error en la etiqueta de estado.
                    // Ese mensaje viene desde ControllerService.
                    statusLabel.setText(result.mensajeError);
                }

            }); // Fin del bloque que se ejecuta en el hilo principal.
        }).start();
    }

    @FXML
    private void handleNuevoControlador() {
        try {
            //    Carga el archivo FXML que contiene la interfaz del formulario.
            //    MainApp.class.getResource busca dentro de src/main/resources/org/example/...
            //    La ruta es relativa a la carpeta resources.
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource(
                            "views/controllers/NewControllerView.fxml"));

            //    Carga el contenido del FXML (crea los objetos de la interfaz)
            //    y devuelve el nodo raiz (Parent) que representa la ventana.
            Parent root = loader.load();

            //    Obtiene el controlador asociado a ese FXML (NewControllerController)
            //    para poder comunicarnos con el formulario.
            NewControllerController newCtrl = loader.getController();

            // Se crea un objeto de tipo Runnable usando una clase anonima.
            newCtrl.setOnControladorCreado(new Runnable() {
                @Override
                public void run() {
                    // Cuando el formulario termine de crear el controlador,
                    // ejecutara este metodo run, que a su vez llama al metodo
                    // cargarControladores() del controlador actual (this).
                    // Esto provoca que la tabla de controladores se refresque
                    // automaticamente mostrando el nuevo registro.
                    cargarControladores();
                }
            });

            //    Crea un nuevo Stage (ventana) para el formulario.
            Stage modal = new Stage();

            //    Hace que la ventana sea modal y bloquee la ventana padre.
            //    Modality.APPLICATION_MODAL significa que el usuario no puede
            //    interactuar con otras ventanas de la aplicacion hasta que cierre esta.
            modal.initModality(Modality.APPLICATION_MODAL);

            //    Establece como dueño de esta ventana al Stage principal de la app.
            //    Asi la ventana se centrara sobre la principal y compartira icono, etc.
            modal.initOwner(NavigationManager.getPrimaryStage());

            //    Titulo de la ventana emergente.
            modal.setTitle("Nuevo controlador");

            //    Asigna la escena cargada (root) a la ventana.
            modal.setScene(new Scene(root));

            //    Impide que el usuario redimensione la ventana (tamano fijo).
            modal.setResizable(false);

            //    Muestra la ventana y se queda esperando hasta que sea cerrada.
            //     "showAndWait" detiene la ejecucion del codigo en este punto
            //     hasta que el usuario cierre el formulario (pulse "Guardar" o "Cancelar").
            modal.showAndWait();

        } catch (Exception e) {
            // Si falla la carga del FXML o cualquier otro error, se imprime en consola.
            System.err.println("Error al abrir formulario: "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    // Cambia el estado activo/inactivo de un controlador.
    // Pide confirmacion antes de realizar el cambio.
    private void cambiarEstado(ControllerUser user) {
        String accion = user.isActivo() ? "desactivar" : "activar";
        String mensaje = "Estas seguro de que quieres "
                + accion + " a " + user.getNombre() + "?";

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar accion");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(mensaje);
        confirmacion.initOwner(NavigationManager.getPrimaryStage());

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                new Thread(() -> {
                    ControllerService.ControllerResult result =
                            ControllerService.cambiarEstado(
                                    user.getUid(), !user.isActivo());

                    Platform.runLater(() -> {
                        if (result.exito) {
                            // Recarga la lista para reflejar el cambio.
                            cargarControladores();
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