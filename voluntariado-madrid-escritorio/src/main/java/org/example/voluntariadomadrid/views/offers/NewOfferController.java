package org.example.voluntariadomadrid.views.offers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.voluntariadomadrid.models.VolunteerOffer;
import org.example.voluntariadomadrid.services.GeocodingService;
import org.example.voluntariadomadrid.services.OfferService;
import org.example.voluntariadomadrid.utils.SessionManager;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

// Controlador del formulario de creacion y edicion de ofertas.
// Se usa tanto para crear nuevas ofertas como para editar las existentes.
// Se abre como ventana emergente modal desde la lista de ofertas.
// Si se recibe una oferta existente mediante setOfertaParaEditar(),
// el formulario entra en modo edicion y rellena los campos automaticamente.
public class NewOfferController implements Initializable {

    // Campo de texto para el titulo de la oferta.
    @FXML private Label tituloFormulario;

    // Campo de texto para el titulo de la oferta.
    @FXML private TextField tituloField;

    // Area de texto para la descripcion detallada de la oferta.
    @FXML private TextArea descripcionField;

    // Combo desplegable para seleccionar la categoria de la oferta.
    @FXML private ComboBox<String> categoriaCombo;

    // Campo de texto para el numero de plazas disponibles.
    @FXML private TextField plazasField;

    // Campo de texto donde el usuario escribe la direccion de la actividad.
    @FXML private TextField direccionField;

    // Boton que llama a la API de geocodificacion para verificar la direccion.
    @FXML private Button btnVerificar;

    // Etiqueta que muestra las coordenadas obtenidas tras verificar la direccion.
    @FXML private Label coordsLabel;

    // Campo de texto para la fecha de inicio en formato dd/MM/yyyy.
    @FXML private TextField fechaInicioField;

    // Campo de texto para la fecha de fin en formato dd/MM/yyyy.
    @FXML private TextField fechaFinField;

    // Etiqueta para mostrar mensajes de error al usuario.
    @FXML private Label errorLabel;

    // Boton principal del formulario. Cambia su texto segun el modo.
    @FXML private Button saveButton;

    // Formato de fecha que el usuario debe respetar al introducir fechas.
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    // Si no es null, el formulario esta en modo edicion con esta oferta.
    // Si es null, el formulario esta en modo creacion de nueva oferta.
    private VolunteerOffer ofertaParaEditar;

    // Coordenadas geograficas obtenidas de la API de geocodificacion.
    // Se inicializan a 0.0 y se actualizan al verificar la direccion.
    private double latObtenida = 0.0;
    private double lngObtenida = 0.0;

    // Indica si el usuario ha verificado la direccion con la API.
    // Si es false, no se permite guardar para garantizar coordenadas reales.
    private boolean direccionVerificada = false;

    // Callback que se ejecuta cuando la oferta se guarda correctamente.
    // Permite notificar a la lista de ofertas para que se recargue.
    private Runnable onOfertaGuardada;

    // Metodo que se ejecuta automaticamente al cargar el formulario.
    // Inicializa el combo de categorias con las opciones disponibles.
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Rellena el combo desplegable con las categorias disponibles.
        categoriaCombo.getItems().addAll(
                "alimentacion",
                "educacion",
                "medio ambiente",
                "salud",
                "mayores",
                "infancia",
                "discapacidad",
                "emergencias",
                "cultura",
                "deporte",
                "otros"
        );

        // Selecciona la primera categoria por defecto para
        // que el combo nunca este vacio al abrir el formulario.
        categoriaCombo.getSelectionModel().selectFirst();
    }

    // Recibe la oferta a editar desde la lista de ofertas.
    // Al llamar a este metodo el formulario entra en modo edicion:
    // cambia el titulo, el texto del boton y rellena todos los campos
    // con los datos actuales de la oferta recibida.
    public void setOfertaParaEditar(VolunteerOffer oferta) {
        this.ofertaParaEditar = oferta;

        // Cambia los textos del formulario a modo edicion.
        tituloFormulario.setText("Editar oferta");
        saveButton.setText("Guardar cambios");

        // Rellena los campos con los datos actuales de la oferta.
        tituloField.setText(oferta.getTitulo());
        descripcionField.setText(oferta.getDescripcion());
        categoriaCombo.setValue(oferta.getCategoria());
        plazasField.setText(String.valueOf(oferta.getPlazasTotal()));

        // Extrae la direccion del mapa de ubicacion si existe.
        if (oferta.getUbicacion() != null
                && oferta.getUbicacion().get("direccion") != null) {
            direccionField.setText(
                    oferta.getUbicacion().get("direccion").toString());
        }

        // Formatea las fechas del objeto Date al formato dd/MM/yyyy
        // para mostrarlas en los campos de texto.
        if (oferta.getFechaInicio() != null) {
            fechaInicioField.setText(sdf.format(oferta.getFechaInicio()));
        }
        if (oferta.getFechaFin() != null) {
            fechaFinField.setText(sdf.format(oferta.getFechaFin()));
        }

        // Carga las coordenadas existentes de la oferta para no perderlas
        // si el usuario edita otros campos sin volver a verificar la direccion.
        if (oferta.getUbicacion() != null) {
            Object latObj = oferta.getUbicacion().get("lat");
            Object lngObj = oferta.getUbicacion().get("lng");
            if (latObj != null && lngObj != null) {
                latObtenida = Double.parseDouble(latObj.toString());
                lngObtenida = Double.parseDouble(lngObj.toString());

                // Marca como verificada para no bloquear el guardado
                // cuando la oferta ya tenia coordenadas reales.
                direccionVerificada = true;

                // Muestra las coordenadas cargadas al usuario.
                coordsLabel.setText("Ubicacion cargada: "
                        + String.format("%.6f", latObtenida) + ", "
                        + String.format("%.6f", lngObtenida));
            }
        }
    }

    // Registra el callback que se ejecutara cuando la oferta se guarde.
    // La lista de ofertas lo usa para recargarse automaticamente.
    public void setOnOfertaGuardada(Runnable callback) {
        this.onOfertaGuardada = callback;
    }

    // Metodo que se ejecuta cuando el usuario pulsa el boton "Verificar".
    // Llama a la API de geocodificacion de Google Maps con la direccion
    // introducida y muestra las coordenadas obtenidas si tiene exito.
    @FXML
    private void handleVerificarDireccion() {

        String direccion = direccionField.getText().trim();

        if (direccion.isEmpty()) {
            mostrarError("Escribe una direccion antes de verificar.");
            return;
        }

        // Desactiva el boton mientras se hace la peticion
        // para evitar llamadas multiples a la API.
        btnVerificar.setDisable(true);
        btnVerificar.setText("Buscando...");
        coordsLabel.setText("");
        ocultarError();

        // Ejecuta la llamada a la API en un hilo secundario
        // para no bloquear la interfaz durante la peticion.
        new Thread(() -> {
            GeocodingService.GeocodingResult result =
                    GeocodingService.geocodificar(direccion);

            // Vuelve al hilo de JavaFX para actualizar la interfaz.
            Platform.runLater(() -> {
                btnVerificar.setDisable(false);
                btnVerificar.setText("Verificar");

                if (result.exito) {
                    // Guarda las coordenadas obtenidas para usarlas al guardar.
                    latObtenida = result.lat;
                    lngObtenida = result.lng;
                    direccionVerificada = true;

                    // Actualiza el campo de direccion con la version normalizada
                    // que devuelve Google para estandarizar el formato.
                    direccionField.setText(result.direccionFormateada);

                    // Muestra las coordenadas al usuario como confirmacion visual.
                    coordsLabel.setText("Ubicacion verificada: "
                            + String.format("%.6f", result.lat) + ", "
                            + String.format("%.6f", result.lng));
                } else {
                    // Si falla, marca como no verificada y muestra el error.
                    direccionVerificada = false;
                    mostrarError(result.mensajeError);
                }
            });
        }).start();
    }

    // Metodo que se ejecuta cuando el usuario pulsa "Guardar oferta"
    // o "Guardar cambios". Valida todos los campos, verifica que la
    // direccion haya sido geocodificada y llama al servicio correspondiente
    // segun si es creacion o edicion.
    @FXML
    private void handleGuardar() {

        // Lee y limpia los valores de todos los campos.
        String titulo = tituloField.getText().trim();
        String descripcion = descripcionField.getText().trim();
        String categoria = categoriaCombo.getValue();
        String plazasStr = plazasField.getText().trim();
        String direccion = direccionField.getText().trim();
        String fechaInicioStr = fechaInicioField.getText().trim();
        String fechaFinStr = fechaFinField.getText().trim();

        // Validacion de campos obligatorios vacios.
        if (titulo.isEmpty() || descripcion.isEmpty()
                || plazasStr.isEmpty() || fechaInicioStr.isEmpty()
                || fechaFinStr.isEmpty()) {
            mostrarError("Por favor, rellena todos los campos.");
            return;
        }

        // Validacion de que el numero de plazas es un entero positivo.
        int plazas;
        try {
            plazas = Integer.parseInt(plazasStr);
            if (plazas <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarError("El numero de plazas debe ser un numero entero positivo.");
            return;
        }

        // Validacion de que la direccion ha sido verificada con la API.
        // Sin coordenadas reales la oferta no apareceria en el mapa movil.
        if (!direccionVerificada) {
            mostrarError("Pulsa 'Verificar' para confirmar la direccion antes de guardar.");
            return;
        }

        // Validacion y parseo de las fechas introducidas.
        // setLenient(false) impide fechas invalidas como 32/01/2026.
        Date fechaInicio;
        Date fechaFin;
        try {
            sdf.setLenient(false);
            fechaInicio = sdf.parse(fechaInicioStr);
            fechaFin = sdf.parse(fechaFinStr);
        } catch (ParseException e) {
            mostrarError("Formato de fecha incorrecto. Usa dd/MM/yyyy.");
            return;
        }

        // Validacion de que la fecha de fin es posterior a la de inicio.
        if (fechaFin.before(fechaInicio)) {
            mostrarError("La fecha de fin debe ser posterior a la fecha de inicio.");
            return;
        }

        // Desactiva el boton para evitar guardados multiples.
        saveButton.setDisable(true);
        saveButton.setText("Guardando...");
        ocultarError();

        // Obtiene los datos de sesion necesarios para crear la oferta.
        SessionManager session = SessionManager.getInstance();
        String orgId = session.getOrganizacionActual().getId();
        String uid = session.getUsuarioActual().getUid();

        // Copia las fechas a variables finales para usarlas dentro del hilo.
        final Date fi = fechaInicio;
        final Date ff = fechaFin;

        new Thread(() -> {
            OfferService.OfferResult result;

            if (ofertaParaEditar == null) {
                // Modo creacion: llama al servicio para crear la oferta nueva.
                result = OfferService.crearOferta(
                        titulo, descripcion, categoria,
                        plazas, direccion, fi, ff,
                        orgId, uid, latObtenida, lngObtenida);
            } else {
                // Modo edicion: llama al servicio para actualizar la oferta.
                result = OfferService.editarOferta(
                        ofertaParaEditar.getId(),
                        titulo, descripcion, categoria,
                        plazas, direccion, fi, ff,
                        latObtenida, lngObtenida);
            }

            Platform.runLater(() -> {
                // Reactiva el boton con el texto correcto segun el modo.
                saveButton.setDisable(false);
                saveButton.setText(ofertaParaEditar == null
                        ? "Guardar oferta" : "Guardar cambios");

                if (result.exito) {
                    // Notifica a la lista para que se recargue con los nuevos datos.
                    if (onOfertaGuardada != null) {
                        onOfertaGuardada.run();
                    }
                    // Cierra la ventana emergente al terminar.
                    cerrarVentana();
                } else {
                    mostrarError(result.mensajeError);
                }
            });
        }).start();
    }

    // Cierra la ventana emergente sin guardar nada.
    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    // Obtiene el Stage de la ventana emergente y lo cierra.
    private void cerrarVentana() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
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