package com.voluntariado.madrid

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import com.voluntariado.madrid.ui.theme.NaranjaVoluntily
import com.voluntariado.madrid.ui.theme.VerdeVoluntily

@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Sol por defecto
    var localizacionUsuario by remember { mutableStateOf(LatLng(40.4168, -3.7038)) }
    var ofertas by remember { mutableStateOf<List<VolunteerOffer>>(emptyList()) }
    var permisosConcedidos by remember { mutableStateOf(false) }

    val localizacionMapaCliente = remember { LocationServices.getFusedLocationProviderClient(context) }
    val posicionCamaraMapa = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(localizacionUsuario, 12f)
    }


    // obtener la ubicacion del usuario
    val obtenerUbicacion = {
        try {
            localizacionMapaCliente.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let { // let para evitar nulos y que no de error
                    val nuevaUbicacion = LatLng(it.latitude, it.longitude)
                    localizacionUsuario = nuevaUbicacion
                    posicionCamaraMapa.position = CameraPosition.fromLatLngZoom(nuevaUbicacion, 12f)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // lanza los permisos de ubicacion
    val peticionDePErmisos = rememberLauncherForActivityResult( //preparar los permisos
        contract = ActivityResultContracts.RequestMultiplePermissions() //lanzar la peticion de permisos
    ) { permisos ->
        permisosConcedidos = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true || // localizacion exacta
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true // localizacion cercana
        if (permisosConcedidos) {
            obtenerUbicacion()
        }
    }

    // compruebo los permisos y cargo las ofertas de firebase
    LaunchedEffect(Unit) {
        // Comprobar si ya tengo el permiso
        val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!tienePermiso) {
            // solicitar los permisos por si no los tiene puestos, como cuando ejecutamos la app por primera vez
            peticionDePErmisos.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
        } else {
            permisosConcedidos = true
            obtenerUbicacion()
        }

        // Descarga ofertas de Firebase
        db.collection("volunteer_offers").whereEqualTo("estado", "activo").get()//solo las que esten activas
            .addOnSuccessListener { result ->
                ofertas = result.documents.mapNotNull { doc ->
                    val ubicacion = doc.get("ubicacion") as? Map<String, Any>
                    val latitud = (ubicacion?.get("lat") as? Number)?.toDouble() ?: 0.0
                    val longitud = (ubicacion?.get("lng") as? Number)?.toDouble() ?: 0.0

                    VolunteerOffer(
                        id = doc.id,
                        titulo = doc.getString("titulo") ?: "",
                        lat = latitud,
                        lng = longitud
                    )
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Cabecera
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(NaranjaVoluntily, VerdeVoluntily)))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text("Mapa de voluntariados", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.Center))
        }

        // Mapa
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = posicionCamaraMapa,
            properties = MapProperties(isMyLocationEnabled = permisosConcedidos)
        ) {
            // Dibujar un marcador por cada oferta
            ofertas.forEach { oferta ->
                if (oferta.lat != 0.0 && oferta.lng != 0.0) {
                    Marker(
                        state = MarkerState(position = LatLng(oferta.lat, oferta.lng)),
                        title = oferta.titulo,
                        snippet = "Toca para ver detalles",
                        onInfoWindowClick = {
                            navController.navigate("oferta_detalle/${oferta.id}")
                        }
                    )
                }
            }
        }
    }
}