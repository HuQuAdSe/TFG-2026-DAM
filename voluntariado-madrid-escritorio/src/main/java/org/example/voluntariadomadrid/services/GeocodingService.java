package org.example.voluntariadomadrid.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

// Esta clase gestiona las llamadas a la API de geocodificacion de Google Maps.
// Convierte una direccion de texto en coordenadas geograficas (lat y lng).
public class GeocodingService {

    // Clave de la API de Google Maps Geocoding.
    private static final String API_KEY =
            "AIzaSyC3Dj5fhs7HCZoqIP-Uu78ywGo95JUoLHk";

    // URL base de la API de geocodificacion de Google.
    private static final String BASE_URL =
            "https://maps.googleapis.com/maps/api/geocode/json";

    // Cliente HTTP reutilizable para todas las peticiones.
    private static final OkHttpClient httpClient = new OkHttpClient();

    // Clase que encapsula el resultado de una geocodificacion.
    public static class GeocodingResult {

        // true si se encontraron coordenadas, false si fallo.
        public boolean exito;

        // Latitud de la direccion encontrada.
        public double lat;

        // Longitud de la direccion encontrada.
        public double lng;

        // Direccion formateada que devuelve Google.
        // Es la version normalizada de la direccion introducida.
        public String direccionFormateada;

        // Mensaje de error si fallo.
        public String mensajeError;
    }

    // Convierte una direccion de texto en coordenadas lat y lng.
    // Añade "Madrid, Espana" al final si el usuario no lo incluyo
    // para mejorar la precision de los resultados en la Comunidad de Madrid.
    public static GeocodingResult geocodificar(String direccion) {

        GeocodingResult result = new GeocodingResult();

        try {
            // Añade contexto geografico a la busqueda para mejorar precision.
            String direccionCompleta = direccion.trim();
            if (!direccionCompleta.toLowerCase().contains("madrid")) {
                direccionCompleta += ", Madrid, Espana";
            }

            // Codifica la direccion para incluirla en la URL.
            // Los espacios se convierten en %20 y caracteres especiales se escapan.
            String direccionCodificada = java.net.URLEncoder.encode(
                    direccionCompleta, "UTF-8");

            // Construye la URL completa de la peticion.
            String url = BASE_URL + "?address=" + direccionCodificada
                    + "&key=" + API_KEY;

            // Construye y ejecuta la peticion HTTP GET.
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            Response response = httpClient.newCall(request).execute();
            String responseBody = response.body().string();

            // Parsea la respuesta JSON de Google.
            JSONObject json = new JSONObject(responseBody);
            String status = json.getString("status");

            if (!"OK".equals(status)) {
                // Google devuelve distintos status de error.
                result.exito = false;
                result.mensajeError = traducirEstado(status);
                return result;
            }

            // Extrae el primer resultado de la lista de resultados.
            JSONArray results = json.getJSONArray("results");
            JSONObject primerResultado = results.getJSONObject(0);

            // Extrae las coordenadas del campo geometry.location.
            JSONObject location = primerResultado
                    .getJSONObject("geometry")
                    .getJSONObject("location");

            result.exito = true;
            result.lat = location.getDouble("lat");
            result.lng = location.getDouble("lng");
            result.direccionFormateada = primerResultado
                    .getString("formatted_address");

            return result;

        } catch (Exception e) {
            result.exito = false;
            result.mensajeError = "Error de conexion al verificar la direccion.";
            e.printStackTrace();
            return result;
        }
    }

    // Traduce los codigos de estado de la API de Google
    // a mensajes legibles en espanol.
    private static String traducirEstado(String status) {
        switch (status) {
            case "ZERO_RESULTS":
                return "No se encontro ninguna ubicacion con esa direccion.";
            case "OVER_DAILY_LIMIT":
                return "Se ha superado el limite diario de la API de mapas.";
            case "OVER_QUERY_LIMIT":
                return "Demasiadas peticiones. Espera un momento e intentalo de nuevo.";
            case "REQUEST_DENIED":
                return "La clave de la API de mapas no es valida.";
            case "INVALID_REQUEST":
                return "La direccion introducida no es valida.";
            default:
                return "Error al verificar la direccion. Intentalo de nuevo.";
        }
    }
}