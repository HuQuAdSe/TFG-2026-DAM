package org.example.voluntariadomadrid.services;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import org.example.voluntariadomadrid.models.ControllerUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Esta clase gestiona todas las operaciones relacionadas con los usuarios
// controladores de una organizacion. Crear controladores, listarlos,
// activarlos y desactivarlos.
public class ControllerService {

    // Resultado de una operacion sobre controladores.
    public static class ControllerResult {

        // true si la operacion fue correcta, false si fallo.
        public boolean exito;

        // Mensaje de error legible. Es null si la operacion fue correcta.
        public String mensajeError;

        // Lista de controladores. Se usa en la operacion de listar.
        public List<ControllerUser> controladores;

        // Controlador creado. Se usa en la operacion de crear.
        public ControllerUser controlador;
    }

    // Crea un nuevo usuario controlador para una organizacion.
    // El admin proporciona el email y la contrasena del nuevo controlador.
    // Se crea el usuario en Firebase Authentication y su documento
    // en la coleccion controller_users de Firestore.
    public static ControllerResult crearControlador(
            String nombre,
            String email,
            String password,
            String organizacionId) {

        ControllerResult result = new ControllerResult();

        try {
            // PASO 1: Crea el usuario en Firebase Authentication.
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setDisplayName(nombre);

            UserRecord userRecord = FirebaseAuth.getInstance()
                    .createUser(request);

            String uid = userRecord.getUid();

            // PASO 2: Crea el documento del controlador en Firestore.
            // El rol es "controlador", nunca "admin".
            Map<String, Object> userData = new HashMap<>();
            userData.put("uid", uid);
            userData.put("nombre", nombre);
            userData.put("email", email);
            userData.put("organizacionId", organizacionId);
            userData.put("rol", "controlador");
            userData.put("fechaCreacion", new Date());
            userData.put("activo", true);

            Firestore db = FirestoreClient.getFirestore();
            db.collection("controller_users")
                    .document(uid)
                    .set(userData)
                    .get();

            // Construye el objeto del modelo con los datos creados.
            ControllerUser controlador = new ControllerUser(
                    uid, nombre, email, organizacionId,
                    "controlador", new Date(), true);

            result.exito = true;
            result.controlador = controlador;
            return result;

        } catch (Exception e) {
            result.exito = false;

            String mensaje = e.getMessage();
            if (mensaje != null && mensaje.contains("EMAIL_EXISTS")) {
                result.mensajeError = "Ya existe una cuenta con ese correo electronico.";
            } else if (mensaje != null && mensaje.contains("WEAK_PASSWORD")) {
                result.mensajeError = "La contrasena es demasiado debil. Usa al menos 6 caracteres.";
            } else if (mensaje != null && mensaje.contains("INVALID_EMAIL")) {
                result.mensajeError = "El formato del correo electronico no es valido.";
            } else {
                result.mensajeError = "Error al crear el controlador. Intentalo de nuevo.";
            }

            e.printStackTrace();
            return result;
        }
    }

    // Obtiene la lista de todos los controladores de una organizacion.
    // Excluye al admin de la lista — el admin no se gestiona desde aqui.
    public static ControllerResult listarControladores(String organizacionId) {

        ControllerResult result = new ControllerResult();

        try {
            Firestore db = FirestoreClient.getFirestore();

            // Consulta todos los usuarios de la organizacion con rol "controlador".
            QuerySnapshot snapshot = db.collection("controller_users")
                    .whereEqualTo("organizacionId", organizacionId)
                    .whereEqualTo("rol", "controlador")
                    .get()
                    .get();

            // Convierte cada documento en un objeto ControllerUser.
            List<ControllerUser> lista = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                ControllerUser user = doc.toObject(ControllerUser.class);
                user.setUid(doc.getId());
                lista.add(user);
            }

            result.exito = true;
            result.controladores = lista;
            return result;

        } catch (Exception e) {
            result.exito = false;
            result.mensajeError = "Error al cargar los controladores.";
            e.printStackTrace();
            return result;
        }
    }

    // Cambia el estado activo/inactivo de un controlador.
    // Un controlador desactivado no puede iniciar sesion.
    // No se borra el usuario, solo se cambia el campo activo.
    public static ControllerResult cambiarEstado(String uid, boolean nuevoEstado) {

        ControllerResult result = new ControllerResult();

        try {
            Firestore db = FirestoreClient.getFirestore();

            // Actualiza solo el campo activo del documento.
            Map<String, Object> update = new HashMap<>();
            update.put("activo", nuevoEstado);

            db.collection("controller_users")
                    .document(uid)
                    .update(update)
                    .get();

            result.exito = true;
            return result;

        } catch (Exception e) {
            result.exito = false;
            result.mensajeError = "Error al cambiar el estado del controlador.";
            e.printStackTrace();
            return result;
        }
    }
}