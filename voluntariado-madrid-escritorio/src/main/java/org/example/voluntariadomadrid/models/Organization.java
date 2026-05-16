package org.example.voluntariadomadrid.models;

import java.util.Date;

// Esta clase representa una organizacion de voluntariado dentro del sistema.
// Cada objeto de esta clase corresponde a un documento de la coleccion
// "organizations" en Firestore.
public class Organization {

    // Identificador unico de la organizacion en Firestore.
    // Se genera automaticamente cuando se crea el documento.
    private String id;

    // Nombre visible de la organizacion. Ejemplo: "Cruz Roja Madrid"
    private String nombre;

    // Texto descriptivo sobre la organizacion y su actividad.
    private String descripcion;

    // URL de la imagen del logo de la organizacion.
    // Puede estar vacia si la organizacion no ha subido logo.
    private String logoUrl;

    // UID del usuario que tiene el rol de administrador en esta organizacion.
    // Este UID lo genera Firebase Authentication al registrar al usuario.
    // Solo puede haber un admin por organizacion.
    private String adminUid;

    // Fecha en la que la organizacion fue registrada en el sistema.
    private Date fechaRegistro;

    // Indica si la organizacion esta activa o ha sido desactivada.
    // true = activa, false = desactivada
    private boolean activo;

    // Constructor vacio obligatorio para que Firestore pueda
    // convertir documentos en objetos de esta clase automaticamente.
    public Organization() {
    }

    // Constructor con todos los campos para crear una organizacion
    // con todos sus datos desde el codigo.
    public Organization(String id, String nombre, String descripcion,
                        String logoUrl, String adminUid,
                        Date fechaRegistro, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.logoUrl = logoUrl;
        this.adminUid = adminUid;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
    }

    // --- GETTERS Y SETTERS ---
    // Los getters devuelven el valor del campo.
    // Los setters permiten modificar el valor del campo.

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getAdminUid() {
        return adminUid;
    }

    public void setAdminUid(String adminUid) {
        this.adminUid = adminUid;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}