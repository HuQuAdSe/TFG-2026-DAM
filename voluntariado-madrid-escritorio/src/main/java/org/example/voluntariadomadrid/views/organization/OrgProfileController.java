package org.example.voluntariadomadrid.views.organization;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.voluntariadomadrid.models.Organization;
import org.example.voluntariadomadrid.services.OrganizationService;
import org.example.voluntariadomadrid.utils.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

// Controlador de la pantalla de perfil de la organizacion.
// Muestra los datos actuales de la organizacion y permite al admin
// editar el nombre y la descripcion. Los campos no editables como
// el ID y el administrador se muestran en modo solo lectura.
public class OrgProfileController implements Initializable {

    // Campo de solo lectura que muestra el ID de la organizacion.
    @FXML
    private TextField idField;

    // Campo de solo lectura que muestra el nombre del administrador.
    @FXML
    private TextField adminField;

    // Campo editable para el nombre de la organizacion.
    @FXML
    private TextField nombreField;

    // Campo editable para la descripcion de la organizacion.
    @FXML
    private TextArea descripcionField;

    // Etiqueta para mostrar mensajes de exito al guardar.
    @FXML
    private Label successLabel;

    // Etiqueta para mostrar mensajes de error al guardar.
    @FXML
    private Label errorLabel;

    // Boton de guardar. Se desactiva mientras se procesa la peticion.
    @FXML
    private Button saveButton;

    // Metodo que se ejecuta automaticamente al cargar la pantalla.
    // Rellena todos los campos con los datos de la sesion activa.
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        SessionManager session = SessionManager.getInstance();
        Organization org = session.getOrganizacionActual();

        // Rellena los campos de solo lectura con datos del sistema.
        idField.setText(org.getId());
        adminField.setText(session.getUsuarioActual().getNombre()
                + " (" + session.getUsuarioActual().getEmail() + ")");

        // Rellena los campos editables con los datos actuales.
        nombreField.setText(org.getNombre());
        descripcionField.setText(org.getDescripcion());

        // Si el usuario no es admin, desactiva los campos editables
        // para que los controladores no puedan modificar la organizacion.
        if (!session.isAdmin()) {
            nombreField.setEditable(false);
            descripcionField.setEditable(false);
            saveButton.setDisable(true);
            saveButton.setVisible(false);
        }
    }

    // Metodo que se ejecuta cuando el usuario pulsa "Guardar cambios".
    // Valida los campos y llama al servicio para actualizar Firestore.
    @FXML
    private void handleGuardar() {

        String nuevoNombre = nombreField.getText().trim();
        String nuevaDescripcion = descripcionField.getText().trim();

        // Validacion de campos obligatorios.
        if (nuevoNombre.isEmpty()) {
            mostrarError("El nombre de la organizacion no puede estar vacio.");
            return;
        }

        saveButton.setDisable(true);
        saveButton.setText("Guardando...");
        ocultarMensajes();

        String orgId = SessionManager.getInstance()
                .getOrganizacionActual().getId();

        new Thread(() -> {
            OrganizationService.RegisterResult result =
                    OrganizationService.actualizarOrganizacion(
                            orgId, nuevoNombre, nuevaDescripcion);

            Platform.runLater(() -> {
                saveButton.setDisable(false);
                saveButton.setText("Guardar cambios");

                if (result.exito) {
                    // Actualiza los datos de la organizacion en SessionManager
                    // para que el cambio se refleje en el menu lateral
                    // sin necesidad de cerrar sesion.
                    Organization org = SessionManager.getInstance()
                            .getOrganizacionActual();
                    org.setNombre(nuevoNombre);
                    org.setDescripcion(nuevaDescripcion);

                    mostrarExito("Datos actualizados correctamente.");
                } else {
                    mostrarError(result.mensajeError);
                }
            });
        }).start();
    }

    // Metodo que se ejecuta cuando el usuario pulsa "Descartar cambios".
    // Restaura los campos a los valores actuales de la organizacion
    // sin guardar nada en Firestore.
    @FXML
    private void handleDescartar() {
        Organization org = SessionManager.getInstance()
                .getOrganizacionActual();

        // Restaura los valores originales en los campos.
        nombreField.setText(org.getNombre());
        descripcionField.setText(org.getDescripcion());

        ocultarMensajes();
    }

    // Muestra un mensaje de exito en verde y oculta el de error.
    private void mostrarExito(String mensaje) {
        successLabel.setText(mensaje);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }

    // Muestra un mensaje de error en rojo y oculta el de exito.
    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    // Oculta ambas etiquetas de mensaje.
    private void ocultarMensajes() {
        successLabel.setVisible(false);
        errorLabel.setVisible(false);
    }
}