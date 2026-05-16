package org.example.voluntariadomadrid.services;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import org.example.voluntariadomadrid.models.ControllerUser;
import org.example.voluntariadomadrid.models.Organization;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// Esta clase gestiona todas las operaciones relacionadas con organizaciones
// en Firestore. Crear una organizacion, obtener sus datos, etc.
public class OrganizationService {

    // Resultado del intento de registro de una organizacion.
    // Encapsula si fue exitoso, el mensaje de error si fallo,
    // y los datos creados si funciono.
    public static class RegisterResult {

        // true si el registro fue correcto, false si fallo.
        public boolean exito;

        // Mensaje de error legible para mostrar al usuario.
        // Es null si el registro fue correcto.
        public String mensajeError;

        // Datos del usuario admin creado.
        public ControllerUser usuario;

        // Datos de la organizacion creada.
        public Organization organizacion;
    }

    // Registra una nueva organizacion en el sistema.
    // Crea el usuario en Firebase Authentication,
    // luego crea el documento de la organizacion en Firestore,
    // y finalmente crea el documento del usuario admin en controller_users.
    // Devuelve un RegisterResult con el resultado de la operacion.
    public static RegisterResult registrarOrganizacion(
            String nombreAdmin,
            String email,
            String password,
            String nombreOrg,
            String descripcionOrg) {

        RegisterResult result = new RegisterResult();

        try {
            Firestore db = FirestoreClient.getFirestore();

            // PASO 0: VERIFICAR QUE EL NOMBRE DE LA ORGANIZACION NO EXISTA YA
            QuerySnapshot nombreExistente = db.collection("organizations")
                    .whereEqualTo("nombre", nombreOrg)
                    .get()
                    .get();

            if (!nombreExistente.isEmpty()) {
                result.exito = false;
                result.mensajeError = "Ya existe una organizacion con el nombre '" + nombreOrg + "'. Por favor, elige otro nombre.";
                return result;  // Detenemos el registro aqui, no seguimos
            }

            // PASO 1: Crea el usuario en Firebase Authentication.
            // Firebase Authentication genera automaticamente un UID unico.
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setDisplayName(nombreAdmin);

            UserRecord userRecord = FirebaseAuth.getInstance()
                    .createUser(request);

            String uid = userRecord.getUid();

            // PASO 2: Crea el documento de la organizacion en Firestore.
            // Usamos un Map para construir el documento campo por campo.
            Map<String, Object> orgData = new HashMap<>();
            orgData.put("nombre", nombreOrg);
            orgData.put("descripcion", descripcionOrg);
            orgData.put("logoUrl", "");
            orgData.put("adminUid", uid);
            orgData.put("fechaRegistro", new Date());
            orgData.put("activo", true);

            // add() genera automaticamente un ID unico para el documento.
            // get() espera a que Firestore confirme que se guardo.
            String orgId = db.collection("organizations")
                    .add(orgData)
                    .get()
                    .getId();

            // PASO 3: Crea el documento del usuario admin en controller_users.
            // El ID del documento es el UID del usuario para facilitar las consultas.
            Map<String, Object> userData = new HashMap<>();
            userData.put("uid", uid);
            userData.put("nombre", nombreAdmin);
            userData.put("email", email);
            userData.put("organizacionId", orgId);
            userData.put("rol", "admin");
            userData.put("fechaCreacion", new Date());
            userData.put("activo", true);

            // document(uid) usa el UID como ID del documento explicitamente.
            // set() crea o sobreescribe el documento con los datos del Map.
            db.collection("controller_users")
                    .document(uid)
                    .set(userData)
                    .get();

            // PASO 4: Construye los objetos del modelo con los datos creados
            // para devolverlos al controlador y guardarlo en SessionManager.
            Organization organizacion = new Organization(
                    orgId, nombreOrg, descripcionOrg, "", uid, new Date(), true);

            ControllerUser usuario = new ControllerUser(
                    uid, nombreAdmin, email, orgId, "admin", new Date(), true);

            result.exito = true;
            result.usuario = usuario;
            result.organizacion = organizacion;
            return result;

        } catch (IllegalArgumentException e) {
            // CAPTURA ESPECÍFICA PARA EL EMAIL INCORRECTO
            result.exito = false;
            result.mensajeError = "El formato del correo electrónico no es válido (ejemplo@dominio.com).";
            return result;

        } catch (Exception e) {
            result.exito = false;

            // Traduce los errores mas comunes de Firebase a mensajes legibles.
            String mensaje = e.getMessage();
            if (mensaje != null && mensaje.contains("EMAIL_EXISTS")) {
                result.mensajeError = "Ya existe una cuenta con ese correo electronico.";
            } else if (mensaje != null && mensaje.contains("WEAK_PASSWORD")) {
                result.mensajeError = "La contrasena es demasiado debil. Usa al menos 6 caracteres.";
            } else {
                result.mensajeError = "Error al registrar. Comprueba tu conexion e intentalo de nuevo.";
            }

            return result;
        }
    }

    // Actualiza los datos editables de una organizacion en Firestore.
    // Solo el admin puede editar los datos de su organizacion.
    // No se permite cambiar el adminUid ni el estado desde aqui.
    public static RegisterResult actualizarOrganizacion(
            String orgId,
            String nuevoNombre,
            String nuevaDescripcion) {

        RegisterResult result = new RegisterResult();

        try {
            Firestore db = FirestoreClient.getFirestore();

            // Construye el mapa con solo los campos editables.
            // El resto de campos del documento no se modifican.
            Map<String, Object> update = new HashMap<>();
            update.put("nombre", nuevoNombre);
            update.put("descripcion", nuevaDescripcion);

            // update() modifica solo los campos indicados sin tocar los demas.
            db.collection("organizations")
                    .document(orgId)
                    .update(update)
                    .get();

            result.exito = true;
            return result;

        } catch (Exception e) {
            result.exito = false;
            result.mensajeError = "Error al actualizar los datos de la organizacion.";
            e.printStackTrace();
            return result;
        }
    }
}