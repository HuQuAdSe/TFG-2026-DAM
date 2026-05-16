package org.example.voluntariadomadrid.models;

import java.util.Date;

// Esta clase representa a un usuario de la aplicacion de escritorio.
// Puede tener dos roles: "admin" (el creador de la organizacion)
// o "controlador" (usuarios secundarios creados por el admin).
// Cada objeto corresponde a un documento en la coleccion "controller_users"
// en Firestore. Las credenciales reales (email y contrasena) las gestiona
// Firebase Authentication. Aqui solo guardamos los datos extra del usuario.
public class ControllerUser {

    // UID unico del usuario. Es el mismo que genera Firebase Authentication
    // al registrar al usuario. Se usa como ID del documento en Firestore.
    private String uid;

    // Nombre completo del usuario. Ejemplo: "Carlos Garcia"
    private String nombre;

    // Correo electronico del usuario. Debe coincidir con el
    // email registrado en Firebase Authentication.
    private String email;

    // ID de la organizacion a la que pertenece este usuario.
    // Referencia al campo "id" de un documento en "organizations".
    private String organizacionId;

    // Rol del usuario dentro de la organizacion.
    // Solo puede ser "admin" o "controlador".
    // El admin es unico por organizacion y tiene permisos totales.
    // El controlador solo puede gestionar ofertas.
    private String rol;

    // Fecha en la que fue creado este usuario en el sistema.
    private Date fechaCreacion;

    // Indica si el usuario puede acceder al sistema.
    // El admin puede desactivar a los controladores sin borrarlos.
    // true = puede acceder, false = acceso bloqueado
    private boolean activo;

    // Constructor vacio necesario para que Firestore pueda
    // reconstruir objetos de esta clase desde los documentos.
    public ControllerUser() {
    }

    // Constructor con todos los campos para crear un usuario
    // con todos sus datos cargados desde el codigo.
    public ControllerUser(String uid, String nombre, String email,
                          String organizacionId, String rol,
                          Date fechaCreacion, boolean activo) {
        this.uid = uid;
        this.nombre = nombre;
        this.email = email;
        this.organizacionId = organizacionId;
        this.rol = rol;
        this.fechaCreacion = fechaCreacion;
        this.activo = activo;
    }

    // --- GETTERS Y SETTERS ---

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganizacionId() {
        return organizacionId;
    }

    public void setOrganizacionId(String id) {
        this.organizacionId = id;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Metodo de utilidad que devuelve true si el usuario es administrador.
    // Evita tener que comparar el string "admin" en cada parte del codigo.
    public boolean isAdmin() {
        return "admin".equals(this.rol);
    }
}