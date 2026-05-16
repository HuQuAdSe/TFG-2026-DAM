package org.example.voluntariadomadrid.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import okhttp3.*;
import org.example.voluntariadomadrid.models.ControllerUser;
import org.example.voluntariadomadrid.models.Organization;
import org.example.voluntariadomadrid.utils.SessionManager;

import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.ExecutionException;

// Esta clase gestiona toda la autenticacion de usuarios de escritorio.
// Se encarga de hacer login contra Firebase Authentication usando
// la API REST, y de cargar los datos del usuario desde Firestore.
public class AuthService {

    // URL de la API REST de Firebase Authentication para login con email y contrasena.
    // El parametro al final es la Web API Key de tu proyecto Firebase.
    private static final String FIREBASE_AUTH_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=AIzaSyDnOdqS05HdN73UaQP4qfUsA3S3fxKp4JI";

    // Cliente HTTP que usamos para hacer las peticiones REST a Firebase.
    // Es estatico para reutilizar la misma instancia en todas las llamadas.
    private static final OkHttpClient httpClient = new OkHttpClient();

    // Tipo de contenido que enviamos en el cuerpo de la peticion HTTP.
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Resultado del intento de login. Encapsula si fue exitoso,
    // el mensaje de error si fallo, y los datos del usuario si funciono.
    public static class LoginResult {

        // true si el login fue correcto, false si fallo
        public boolean exito;

        // Mensaje de error legible para mostrar al usuario.
        // Es null si el login fue correcto.
        public String mensajeError;

        // Datos del usuario logueado cargados desde Firestore.
        // Es null si el login fallo.
        public ControllerUser usuario;

        // Datos de la organizacion del usuario logueado.
        // Es null si el login fallo.
        public Organization organizacion;
    }

    // Metodo principal de login. Recibe email y contraseña,
    // los verifica contra Firebase Authentication, y si son correctos
    // carga los datos del usuario y su organizacion desde Firestore.
    // Devuelve un LoginResult con el resultado de la operacion.
    public static LoginResult login(String email, String password) {

        LoginResult result = new LoginResult();

        try {
            // Construye el cuerpo JSON de la peticion.
            // Firebase Authentication espera email, password y returnSecureToken.
            String jsonBody = new JSONObject()
                    .put("email", email)
                    .put("password", password)
                    .put("returnSecureToken", true)
                    .toString();

            // Crea la peticion HTTP POST con el cuerpo JSON.
            Request request = new Request.Builder()
                    .url(FIREBASE_AUTH_URL)
                    .post(RequestBody.create(jsonBody, JSON))
                    .build();

            // Ejecuta la peticion y espera la respuesta.
            Response response = httpClient.newCall(request).execute();
            String responseBody = response.body().string();

            // Convierte la respuesta en un objeto JSON para leer sus campos.
            JSONObject jsonResponse = new JSONObject(responseBody);

            if (!response.isSuccessful()) {
                // Si Firebase devuelve error, extrae el mensaje y lo devuelve.
                String errorCode = jsonResponse
                        .getJSONObject("error")
                        .getString("message");
                result.exito = false;
                result.mensajeError = traducirError(errorCode);
                return result;
            }

            // Si el login fue correcto, Firebase devuelve el UID del usuario.
            // es localId porque en la API REST se llama localId
            // La API REST es un servicio que nos permite la comunicacion con Firebase a través de internet, enviando peticiones HTTP
            String uid = jsonResponse.getString("localId");

            // Con el UID buscamos los datos del usuario en Firestore
            // en la coleccion "controller_users".
            Firestore db = FirestoreClient.getFirestore();

            // Paso 1: Lanzamos la petición asíncrona. Esto devuelve un "ApiFuture"
            ApiFuture<DocumentSnapshot> future = db.collection("controller_users")
                    .document(uid)
                    .get();

            // Paso 2: Esperamos (bloqueamos) hasta que la petición termine
            // y obtenemos el DocumentSnapshot del resultado
            DocumentSnapshot userDoc = future.get();

            if (!userDoc.exists()) {
                // Si el UID no existe en controller_users, el usuario
                // existe en Authentication pero no es un usuario de escritorio.
                // Puede ser un voluntario de la app movil intentando entrar.
                result.exito = false;
                result.mensajeError = "Este usuario no tiene acceso a la aplicacion de escritorio.";
                return result;
            }

            // Convierte el documento de Firestore en un objeto ControllerUser.
            ControllerUser usuario = userDoc.toObject(ControllerUser.class);
            usuario.setUid(uid);

            if (!usuario.isActivo()) {
                // Si el usuario existe pero esta desactivado, bloquea el acceso.
                result.exito = false;
                result.mensajeError = "Tu cuenta ha sido desactivada. Contacta con tu administrador.";
                return result;
            }

            // Carga los datos de la organizacion a la que pertenece el usuario.
            // PASO 1: Lanzar la petición asíncrona a Firestore para obtener el documento de la organización.
            // El primer .get() pertenece a DocumentReference.
            // Devuelve un ApiFuture<DocumentSnapshot> que representa la operación en curso.
            ApiFuture<DocumentSnapshot> futureOrg = db.collection("organizations")
                    .document(usuario.getOrganizacionId())
                    .get();

            // PASO 2: Esperar (bloquear) hasta que Firestore responda.
            // El segundo .get() pertenece a ApiFuture.
            // Devuelve el DocumentSnapshot ya cargado con los datos de la organización.
            DocumentSnapshot orgDoc = futureOrg.get();

            if (!orgDoc.exists()) {
                result.exito = false;
                result.mensajeError = "No se encontro la organizacion asociada a este usuario.";
                return result;
            }

            // Convierte el documento de Firestore en un objeto Organization.
            Organization organizacion = orgDoc.toObject(Organization.class);
            organizacion.setId(orgDoc.getId());

            // Guarda el usuario y la organizacion en SessionManager
            // para que esten disponibles en toda la aplicacion.
            SessionManager.getInstance().iniciarSesion(usuario, organizacion);

            result.exito = true;
            result.usuario = usuario;
            result.organizacion = organizacion;
            return result;

        } catch (Exception e) {
            // Captura cualquier error inesperado (sin internet, timeout, etc.)
            result.exito = false;
            result.mensajeError = "Error de conexion. Comprueba tu internet e intentalo de nuevo.";
            e.printStackTrace();
            return result;
        }
    }

    // Traduce los codigos de error de Firebase Authentication
    // a mensajes legibles en espanol para mostrar al usuario.
    private static String traducirError(String errorCode) {
        switch (errorCode) {
            case "EMAIL_NOT_FOUND":
                return "No existe ninguna cuenta con ese correo electronico.";
            case "INVALID_PASSWORD":
                return "La contrasena es incorrecta.";
            case "USER_DISABLED":
                return "Esta cuenta ha sido desactivada.";
            case "INVALID_LOGIN_CREDENTIALS":
                return "Correo o contrasena incorrectos.";
            case "TOO_MANY_ATTEMPTS_TRY_LATER":
                return "Demasiados intentos fallidos. Espera unos minutos e intentalo de nuevo.";
            default:
                return "Error al iniciar sesion. Intentalo de nuevo.";
        }
    }
}