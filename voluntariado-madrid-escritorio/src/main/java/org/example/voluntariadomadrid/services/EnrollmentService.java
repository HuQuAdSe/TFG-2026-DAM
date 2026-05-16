package org.example.voluntariadomadrid.services;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Esta clase gestiona todas las operaciones relacionadas con las inscripciones
// de voluntarios en Firestore. Listar inscritos por oferta y consultar
// los datos del voluntario asociado a cada inscripcion.
public class EnrollmentService {

    // Clase que representa una inscripcion enriquecida con los datos
    // del voluntario. Combina datos de la coleccion enrollments
    // y de la coleccion volunteer_users para mostrarlos juntos en la tabla.
    public static class EnrollmentInfo {

        // ID del documento de inscripcion en Firestore.
        public String enrollmentId;

        // UID del voluntario inscrito.
        public String voluntarioUid;

        // Nombre completo del voluntario obtenido de volunteer_users.
        public String nombreVoluntario;

        // Apellidos del voluntario obtenidos de volunteer_users.
        public String apellidosVoluntario;

        // Email del voluntario obtenido de volunteer_users.
        public String emailVoluntario;

        // Fecha en la que el voluntario se inscribio en la oferta.
        public Date fechaInscripcion;

        // Estado actual de la inscripcion.
        // Puede ser "pendiente", "confirmado" o "completado".
        public String estado;

        // Indica si ya se ha emitido el certificado de participacion
        // para este voluntario en esta oferta.
        public boolean certificadoEmitido;

        // Devuelve el nombre completo del voluntario concatenando
        // nombre y apellidos para mostrarlo en la tabla.
        public String getNombreCompleto() {
            return nombreVoluntario + " " + apellidosVoluntario;
        }
    }

    // Resultado de cualquier operacion sobre inscripciones.
    public static class EnrollmentResult {

        // true si la operacion fue correcta, false si fallo.
        public boolean exito;

        // Mensaje de error legible. Es null si fue correcto.
        public String mensajeError;

        // Lista de inscripciones enriquecidas con datos del voluntario.
        public List<EnrollmentInfo> inscripciones;
    }

    // Obtiene todas las inscripciones de una oferta concreta.
    // Para cada inscripcion consulta tambien los datos del voluntario
    // en la coleccion volunteer_users para poder mostrar nombre y email.
    public static EnrollmentResult listarInscritosPorOferta(String ofertaId) {

        EnrollmentResult result = new EnrollmentResult();

        try {
            Firestore db = FirestoreClient.getFirestore();

            // Consulta todas las inscripciones de esta oferta.
            QuerySnapshot enrollments = db.collection("enrollments")
                    .whereEqualTo("ofertaId", ofertaId)
                    .get()
                    .get();

            List<EnrollmentInfo> lista = new ArrayList<>();

            for (QueryDocumentSnapshot doc : enrollments.getDocuments()) {

                EnrollmentInfo info = new EnrollmentInfo();
                info.enrollmentId = doc.getId();
                info.voluntarioUid = doc.getString("voluntarioUid");
                info.estado = doc.getString("estado");
                info.certificadoEmitido = Boolean.TRUE.equals(
                        doc.getBoolean("certificadoEmitido"));

                // Convierte el timestamp de Firestore a Date para
                // poder formatearlo en la tabla.
                com.google.cloud.Timestamp ts =
                        doc.getTimestamp("fechaInscripcion");
                if (ts != null) {
                    info.fechaInscripcion = ts.toDate();
                }

                // Consulta los datos del voluntario en volunteer_users
                // usando su UID como ID del documento.
                if (info.voluntarioUid != null) {
                    com.google.cloud.firestore.DocumentSnapshot userDoc =
                            db.collection("volunteer_users")
                                    .document(info.voluntarioUid)
                                    .get()
                                    .get();

                    if (userDoc.exists()) {
                        info.nombreVoluntario = userDoc.getString("nombre");
                        info.apellidosVoluntario = userDoc.getString("apellidos");
                        info.emailVoluntario = userDoc.getString("email");
                    } else {
                        // Si no se encuentra el usuario en volunteer_users
                        // muestra un texto por defecto para no dejar el campo vacio.
                        info.nombreVoluntario = "Usuario";
                        info.apellidosVoluntario = "no encontrado";
                        info.emailVoluntario = "-";
                    }
                }

                lista.add(info);
            }

            result.exito = true;
            result.inscripciones = lista;
            return result;

        } catch (Exception e) {
            result.exito = false;
            result.mensajeError = "Error al cargar los voluntarios inscritos.";
            e.printStackTrace();
            return result;
        }
    }


}