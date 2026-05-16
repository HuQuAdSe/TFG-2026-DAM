package org.example.voluntariadomadrid.services;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import java.util.HashMap;
import java.util.Map;

// Esta clase consulta Firestore para obtener los datos
// necesarios para la grafica de analiticas.
public class AnalyticsService {

    // Clase que contiene el resultado de la consulta.
    public static class AnalyticsData {

        // true si la consulta fue correcta, false si fallo.
        public boolean exito;

        // Mensaje de error si fallo.
        public String mensajeError;

        // Numero de ofertas agrupadas por estado.
        // Clave: "activo", "desactivado", "completado"
        // Valor: numero de ofertas con ese estado
        public Map<String, Integer> ofertasPorEstado = new HashMap<>();
    }

    // Consulta todas las ofertas de la organizacion y las agrupa por estado.
    public static AnalyticsData cargarDatos(String organizacionId) {

        AnalyticsData data = new AnalyticsData();

        try {
            Firestore db = FirestoreClient.getFirestore();

            // Obtiene todas las ofertas de la organizacion.
            QuerySnapshot ofertas = db.collection("volunteer_offers")
                    .whereEqualTo("organizacionId", organizacionId)
                    .get()
                    .get();

            // Recorre cada oferta y cuenta cuantas hay por estado.
            for (QueryDocumentSnapshot doc : ofertas.getDocuments()) {
                String estado = doc.getString("estado");
                if (estado != null) {
                    // merge suma 1 si la clave ya existe,
                    // o pone 1 si es la primera vez que aparece.
                    data.ofertasPorEstado.merge(estado, 1, Integer::sum);
                }
            }

            data.exito = true;
            return data;

        } catch (Exception e) {
            data.exito = false;
            data.mensajeError = "Error al cargar los datos.";
            e.printStackTrace();
            return data;
        }
    }
}