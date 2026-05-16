package org.example.voluntariadomadrid.models;

import java.util.Date;
import java.util.Map;

// Esta clase representa una oferta de voluntariado publicada
// por una organizacion. Cada objeto corresponde a un documento
// de la coleccion "volunteer_offers" en Firestore.
// Una organizacion puede tener muchas ofertas.
public class VolunteerOffer {

    // Identificador unico de la oferta generado por Firestore.
    private String id;

    // ID de la organizacion que publica esta oferta.
    // Referencia al campo "id" de un documento en "organizations".
    private String organizacionId;

    // UID del usuario (admin o controlador) que creo esta oferta.
    // Referencia al campo "uid" de un documento en "controller_users".
    private String creadoPorUid;

    // Titulo corto y descriptivo de la oferta.
    // Ejemplo: "Reparto de alimentos en banco de alimentos"
    private String titulo;

    // Descripcion detallada de la oferta: en que consiste,
    // que se necesita, horarios, etc.
    private String descripcion;

    // Categoria de la oferta para facilitar la busqueda y filtrado.
    // Ejemplos: "alimentacion", "educacion", "medio ambiente"
    private String categoria;

    // Mapa con los datos de ubicacion de la actividad.
    // Contiene tres campos: "lat" (latitud), "lng" (longitud)
    // y "direccion" (texto legible). Se usa Map porque Firestore
    // guarda este tipo de datos como un objeto anidado.
    private Map<String, Object> ubicacion;

    // Numero total de plazas disponibles para esta oferta.
    private int plazasTotal;

    // Numero de plazas ya ocupadas por voluntarios inscritos.
    // Cuando plazasOcupadas == plazasTotal, la oferta se cierra automaticamente.
    private int plazasOcupadas;

    // Estado actual de la oferta. Solo puede tener tres valores:
    // "activo"      -> visible y aceptando inscripciones
    // "desactivado" -> oculta temporalmente por la organizacion
    // "completado"  -> todas las plazas cubiertas, cerrada automaticamente
    private String estado;

    // Fecha de inicio de la actividad de voluntariado.
    private Date fechaInicio;

    // Fecha de fin de la actividad. Cuando se supera esta fecha,
    // el sistema puede cerrar la oferta automaticamente.
    private Date fechaFin;

    // Constructor vacio necesario para que Firestore pueda
    // reconstruir objetos de esta clase desde los documentos.
    public VolunteerOffer() {
    }

    // --- GETTERS Y SETTERS ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizacionId() {
        return organizacionId;
    }

    public void setOrganizacionId(String organizacionId) {
        this.organizacionId = organizacionId;
    }

    public String getCreadoPorUid() {
        return creadoPorUid;
    }

    public void setCreadoPorUid(String creadoPorUid) {
        this.creadoPorUid = creadoPorUid;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Map<String, Object> getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Map<String, Object> ubicacion) {
        this.ubicacion = ubicacion;
    }

    public int getPlazasTotal() {
        return plazasTotal;
    }

    public void setPlazasTotal(int plazasTotal) {
        this.plazasTotal = plazasTotal;
    }

    public int getPlazasOcupadas() {
        return plazasOcupadas;
    }

    public void setPlazasOcupadas(int plazasOcupadas) {
        this.plazasOcupadas = plazasOcupadas;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    // Metodo de utilidad que calcula cuantas plazas quedan disponibles.
    // Se usa directamente en la interfaz para mostrar al usuario.
    public int getPlazasDisponibles() {
        return plazasTotal - plazasOcupadas;
    }

    // Metodo de utilidad que devuelve true si la oferta esta llena.
    // Cuando devuelve true, el backend debe cambiar el estado a "completado".
    public boolean isCompleta() {
        return plazasOcupadas >= plazasTotal;
    }
}