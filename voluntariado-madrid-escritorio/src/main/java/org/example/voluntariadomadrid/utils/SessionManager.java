package org.example.voluntariadomadrid.utils;

import org.example.voluntariadomadrid.models.ControllerUser;
import org.example.voluntariadomadrid.models.Organization;

// Esta clase gestiona la sesion activa del usuario en la aplicacion.
// Guarda en memoria quien esta logueado y a que organizacion pertenece,
// para que cualquier parte de la app pueda consultar estos datos
// sin tener que hacer peticiones a Firestore constantemente.
//
// Usa el patron Singleton: solo puede existir UNA instancia de esta
// clase durante toda la ejecucion de la aplicacion.
// Se accede a ella con SessionManager.getInstance()
public class SessionManager {

    // La unica instancia de esta clase que existira en toda la app.
    // Es estatica para que sea compartida por todas las clases.
    private static SessionManager instance;

    // Datos del usuario que ha iniciado sesion.
    // Es null si no hay nadie logueado.
    private ControllerUser usuarioActual;

    // Datos de la organizacion a la que pertenece el usuario logueado.
    // Es null si no hay nadie logueado.
    private Organization organizacionActual;

    // Constructor privado para impedir que otras clases creen
    // instancias de SessionManager con "new SessionManager()".
    private SessionManager() {
    }

    // Metodo estatico que devuelve la unica instancia de SessionManager.
    // Si todavia no existe, la crea. Si ya existe, devuelve la misma.
    // Se usa asi desde cualquier clase: SessionManager.getInstance()
    public static SessionManager getInstance() {
        if (instance == null) {
            // Primera vez que se llama: crea la instancia
            instance = new SessionManager();
        }
        // Devuelve siempre la misma instancia
        return instance;
    }

    // Guarda los datos del usuario y su organizacion al iniciar sesion.
    // Se llama justo despues de que Firebase Authentication
    // confirma que el login es correcto.
    public void iniciarSesion(ControllerUser usuario, Organization organizacion) {
        this.usuarioActual = usuario;
        this.organizacionActual = organizacion;
    }

    // Borra los datos de sesion al cerrar sesion.
    // Despues de llamar a este metodo, haySesion() devolvera false.
    public void cerrarSesion() {
        this.usuarioActual = null;
        this.organizacionActual = null;
    }

    // Devuelve el objeto con los datos del usuario logueado.
    // Devuelve null si no hay sesion activa.
    public ControllerUser getUsuarioActual() {
        return usuarioActual;
    }

    // Devuelve el objeto con los datos de la organizacion del usuario logueado.
    // Devuelve null si no hay sesion activa.
    public Organization getOrganizacionActual() {
        return organizacionActual;
    }

    // Devuelve true si el usuario logueado tiene rol de administrador.
    // Se usa para mostrar u ocultar opciones del menu segun el rol.
    public boolean isAdmin() {
        return usuarioActual != null && usuarioActual.isAdmin();
    }

    // Devuelve true si hay un usuario logueado actualmente.
    // Se usa para saber si redirigir al login o al dashboard.
    public boolean haySesion() {
        return usuarioActual != null;
    }
}