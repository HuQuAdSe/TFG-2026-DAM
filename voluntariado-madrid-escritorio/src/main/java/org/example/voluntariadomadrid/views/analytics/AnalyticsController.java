package org.example.voluntariadomadrid.views.analytics;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.example.voluntariadomadrid.services.AnalyticsService;
import org.example.voluntariadomadrid.utils.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

// Controlador del panel de analiticas.
// Carga los datos de Firestore y rellena la grafica de barras.
public class AnalyticsController implements Initializable {

    // Grafica de barras que muestra las ofertas por estado.
    @FXML private BarChart<String, Number> ofertasChart;

    // Etiqueta de estado mientras cargan los datos.
    @FXML private Label statusLabel;

    // Se ejecuta automaticamente al cargar la vista.
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarDatos();
    }

    // Consulta Firestore y rellena la grafica en un hilo secundario.
    private void cargarDatos() {

        statusLabel.setVisible(true);
        String orgId = SessionManager.getInstance()
                .getOrganizacionActual().getId();

        new Thread(() -> {
            AnalyticsService.AnalyticsData data =
                    AnalyticsService.cargarDatos(orgId);

            Platform.runLater(() -> {

                if (!data.exito) {
                    statusLabel.setText(data.mensajeError);
                    return;
                }

                statusLabel.setVisible(false);

                // Crea una serie de datos con una barra por cada estado.
                XYChart.Series<String, Number> serie =
                        new XYChart.Series<>();

                // Toma cada par (estado, número de ofertas) que hay en el mapa, crea con ellos una barra
                // (o punto en el gráfico) con la etiqueta del estado y la altura según la cantidad, y
                // añade esa barra a la serie que después se mostrará en el gráfico.
                data.ofertasPorEstado.forEach((estado, cantidad) ->
                        serie.getData().add(
                                new XYChart.Data<>(estado, cantidad)));

                // Asigna la serie a la grafica.
                ofertasChart.setData(
                        FXCollections.observableArrayList(serie));
            });
        }).start();
    }
}