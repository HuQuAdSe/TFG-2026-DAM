package org.example.voluntariadomadrid.services;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.example.voluntariadomadrid.models.VolunteerOffer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Esta clase gestiona todas las operaciones relacionadas con las ofertas
// de voluntariado en Firestore. Crear, listar, editar y cambiar estado.
public class OfferService {

    // Resultado de cualquier operacion sobre ofertas.
    public static class OfferResult {

        // true si la operacion fue correcta, false si fallo.
        public boolean exito;

        // Mensaje de error legible. Es null si fue correcto.
        public String mensajeError;

        // Lista de ofertas. Se usa en la operacion de listar.
        public List<VolunteerOffer> ofertas;

        // Oferta creada o editada. Se usa en crear y editar.
        public VolunteerOffer oferta;
    }

    // Obtiene todas las ofertas de una organizacion.
    public static OfferResult listarOfertas(String organizacionId) {

        OfferResult result = new OfferResult();

        try {
            Firestore db = FirestoreClient.getFirestore();

            QuerySnapshot snapshot = db.collection("volunteer_offers")
                    .whereEqualTo("organizacionId", organizacionId)
                    .get()
                    .get();

            // Convierte cada documento de Firestore en un objeto VolunteerOffer.
            List<VolunteerOffer> lista = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                VolunteerOffer oferta = doc.toObject(VolunteerOffer.class);
                oferta.setId(doc.getId());
                lista.add(oferta);
            }

            result.exito = true;
            result.ofertas = lista;
            return result;

        } catch (Exception e) {
            result.exito = false;
            result.mensajeError = "Error al cargar las ofertas.";
            e.printStackTrace();
            return result;
        }
    }

    // Crea una nueva oferta de voluntariado en Firestore.
    // Recibe las coordenadas reales obtenidas por la API de geocodificacion.
    // La oferta se crea con estado "activo" y plazasOcupadas en 0 por defecto.
    public static OfferResult crearOferta(
            String titulo,
            String descripcion,
            String categoria,
            int plazasTotal,
            String direccion,
            Date fechaInicio,
            Date fechaFin,
            String organizacionId,
            String creadoPorUid,
            double lat,
            double lng) {

        OfferResult result = new OfferResult();

        try {
            Firestore db = FirestoreClient.getFirestore();

            // Construye el mapa de ubicacion con las coordenadas reales
            // obtenidas de la API de geocodificacion de Google Maps.
            Map<String, Object> ubicacion = new HashMap<>();
            ubicacion.put("direccion", direccion);
            ubicacion.put("lat", lat);
            ubicacion.put("lng", lng);

            // Construye el mapa de datos de la oferta para guardarlo en Firestore.
            Map<String, Object> ofertaData = new HashMap<>();
            ofertaData.put("titulo", titulo);
            ofertaData.put("descripcion", descripcion);
            ofertaData.put("categoria", categoria);
            ofertaData.put("plazasTotal", plazasTotal);
            ofertaData.put("plazasOcupadas", 0);
            ofertaData.put("estado", "activo");
            ofertaData.put("ubicacion", ubicacion);
            ofertaData.put("fechaInicio", fechaInicio);
            ofertaData.put("fechaFin", fechaFin);
            ofertaData.put("organizacionId", organizacionId);
            ofertaData.put("creadoPorUid", creadoPorUid);

            // add() genera un ID unico para el documento automaticamente.
            // get() espera a que Firestore confirme que se guardo correctamente.
            String id = db.collection("volunteer_offers")
                    .add(ofertaData)
                    .get()
                    .getId();

            // Construye el objeto del modelo con los datos recien creados
            // para devolverlo al controlador sin necesidad de otra consulta.
            VolunteerOffer oferta = new VolunteerOffer();
            oferta.setId(id);
            oferta.setTitulo(titulo);
            oferta.setDescripcion(descripcion);
            oferta.setCategoria(categoria);
            oferta.setPlazasTotal(plazasTotal);
            oferta.setPlazasOcupadas(0);
            oferta.setEstado("activo");
            oferta.setOrganizacionId(organizacionId);
            oferta.setCreadoPorUid(creadoPorUid);
            oferta.setFechaInicio(fechaInicio);
            oferta.setFechaFin(fechaFin);

            result.exito = true;
            result.oferta = oferta;
            return result;

        } catch (Exception e) {
            result.exito = false;
            result.mensajeError = "Error al crear la oferta. Intentalo de nuevo.";
            e.printStackTrace();
            return result;
        }
    }

    // Actualiza los campos editables de una oferta existente en Firestore.
    // Recibe las coordenadas reales obtenidas por la API de geocodificacion.
    // No modifica el estado ni las plazas ocupadas — esos campos
    // los gestiona el sistema automaticamente.
    public static OfferResult editarOferta(
            String ofertaId,
            String titulo,
            String descripcion,
            String categoria,
            int plazasTotal,
            String direccion,
            Date fechaInicio,
            Date fechaFin,
            double lat,
            double lng) {

        OfferResult result = new OfferResult();

        try {
            Firestore db = FirestoreClient.getFirestore();

            // Construye el mapa de ubicacion actualizado con coordenadas reales.
            Map<String, Object> ubicacion = new HashMap<>();
            ubicacion.put("direccion", direccion);
            ubicacion.put("lat", lat);
            ubicacion.put("lng", lng);

            // Solo se actualizan los campos editables.
            // El estado y plazasOcupadas no se tocan desde aqui.
            Map<String, Object> update = new HashMap<>();
            update.put("titulo", titulo);
            update.put("descripcion", descripcion);
            update.put("categoria", categoria);
            update.put("plazasTotal", plazasTotal);
            update.put("ubicacion", ubicacion);
            update.put("fechaInicio", fechaInicio);
            update.put("fechaFin", fechaFin);

            // update() solo modifica los campos indicados.
            // El resto de campos del documento no se tocan.
            db.collection("volunteer_offers")
                    .document(ofertaId)
                    .update(update)
                    .get();

            result.exito = true;
            return result;

        } catch (Exception e) {
            result.exito = false;
            result.mensajeError = "Error al editar la oferta.";
            e.printStackTrace();
            return result;
        }
    }

    // Cambia el estado de una oferta entre "activo" y "desactivado".
    // Las ofertas con estado "completado" no deben cambiar de estado
    // desde aqui — ese estado lo gestiona el sistema automaticamente
    // cuando se cubren todas las plazas.
    public static OfferResult cambiarEstado(String ofertaId, String nuevoEstado) {

        OfferResult result = new OfferResult();

        try {
            Firestore db = FirestoreClient.getFirestore();

            // Construye el mapa con solo el campo a actualizar.
            Map<String, Object> update = new HashMap<>();
            update.put("estado", nuevoEstado);

            db.collection("volunteer_offers")
                    .document(ofertaId)
                    .update(update)
                    .get();

            result.exito = true;
            return result;

        } catch (Exception e) {
            result.exito = false;
            result.mensajeError = "Error al cambiar el estado de la oferta.";
            e.printStackTrace();
            return result;
        }
    }
}