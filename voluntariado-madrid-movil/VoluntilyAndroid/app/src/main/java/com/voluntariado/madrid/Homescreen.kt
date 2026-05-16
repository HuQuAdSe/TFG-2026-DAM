package com.voluntariado.madrid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.voluntariado.madrid.ui.theme.NaranjaVoluntily
import com.voluntariado.madrid.ui.theme.VerdeVoluntily
import java.text.SimpleDateFormat
import java.util.*

data class VolunteerOffer(
    val id           : String = "",
    val titulo       : String = "",
    val descripcion  : String = "",
    val fechaInicio  : String = "",
    val plazasTotal  : Int    = 0,
    val plazasOcupadas: Int   = 0,
    val categoria    : String = "",
    val estado       : String = "",
    val organizacionId: String = "",
    val ubicacionDir : String = "",
    val fechaFin     : String = "",
    val lat          : Double = 0.0,
    val lng          : Double = 0.0
)

@Composable
fun HomeScreen(navController: NavController, auth: FirebaseAuth) {
    val texts = LocalAppTexts.current
    val db        = FirebaseFirestore.getInstance()
    var ofertas   by remember { mutableStateOf<List<VolunteerOffer>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensajeError  by remember { mutableStateOf("") }

    // boton de recargar pagina
    var refreshTrigger by remember { mutableStateOf(0) }

    //  refreshTrigger para que escuche los cambios del boton
    LaunchedEffect(refreshTrigger) {
        cargando = true // Activamos la carga cada vez que refrescamos
        mensajeError = ""

        //launcheffects para que se cargue una sola vez la pantalla y no se cargue una y otra vez,
        // que me quema los tokens de la api de google maps
        db.collection("volunteer_offers")
            .whereEqualTo("estado", "activo")
            .get()
            .addOnSuccessListener { result ->
                //escucha los datos que llegan de firestone para cargar las ofertas

                // lista mutable vacia
                val listaTemporal = mutableListOf<VolunteerOffer>()

                // formateador de fechas
                val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                // Recorremos los documentos con un for
                for (doc in result.documents) {
                    try {
                        // Manejo de fechas
                        val fechaInicioDate = doc.getTimestamp("fechaInicio")?.toDate()
                        val fechaInicioStr = if (fechaInicioDate != null) fecha.format(fechaInicioDate) else ""

                        val fechaFinDate = doc.getTimestamp("fechaFin")?.toDate()
                        val fechaFinStr = if (fechaFinDate != null) fecha.format(fechaFinDate) else ""

                        // Manejo del mapa de ubicación
                        val ubicacion = doc.get("ubicacion") as Map<String, Any>
                        val latitud = ubicacion?.get("lat")?.toString()?.toDouble() ?: 0.0
                        val longitud = ubicacion?.get("lng")?.toString()?.toDouble() ?: 0.0

                        // Creo el objeto
                        val oferta = VolunteerOffer(
                            id            = doc.id,
                            titulo        = doc.getString("titulo") ?: "",
                            descripcion   = doc.getString("descripcion")  ?: "",
                            fechaInicio   = fechaInicioStr,
                            fechaFin      = fechaFinStr,
                            plazasTotal   = (doc.getLong("plazasTotal")   ?: 0.toLong()).toInt(),
                            plazasOcupadas= (doc.getLong("plazasOcupadas")?: 0.toLong()).toInt(),
                            categoria     = doc.getString("categoria")    ?: "",
                            estado        = doc.getString("estado")       ?: "",
                            organizacionId= doc.getString("organizacionId")?: "",
                            ubicacionDir  = ubicacion?.get("direccion")?.toString() ?: "",
                            lat           = latitud,
                            lng           = longitud
                        )

                        // añado la oferta para que salga en la lista
                        listaTemporal.add(oferta)

                    } catch (e: Exception) {
                        // Si una oferta da error no se añade y el for sigue
                    }
                }

                // meto las ofertas en la lista temporal
                ofertas = listaTemporal
                cargando = false
            }
            .addOnFailureListener { e ->
                mensajeError  = "${texts.connectionError}: ${e.message}"
                cargando = false
            }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("settings") },
                containerColor = NaranjaVoluntily,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = texts.settingsTitle)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFF8F0))
            ) {

                // cabeceras con los botones de mis voluntariados y el titulo de la pantalla
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        // degradado para la cabecera que queda chuli
                        .background(Brush.horizontalGradient(listOf(NaranjaVoluntily, VerdeVoluntily)))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text       = texts.homeTitle,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                        modifier   = Modifier.align(Alignment.CenterStart)
                    )
                    TextButton(
                        onClick  = { navController.navigate("mis_voluntariados") },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text(
                            text       = texts.myVolunteering,
                            color      = Color.White,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // contenido de las ofertas que las busco por ID
                when {
                    cargando -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = NaranjaVoluntily)
                        }
                    }
                    mensajeError.isNotEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = mensajeError, color = Color.Red)
                        }
                    }
                    ofertas.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(texts.noOffers, color = Color.Gray)
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(ofertas) { oferta ->
                                OfertaCard(
                                    oferta  = oferta,
                                    onClick = { navController.navigate("oferta_detalle/${oferta.id}") }
                                )
                            }
                        }
                    }
                }
            }

            // botoncito chuli del mapa
            FloatingActionButton(
                onClick = { navController.navigate("mapa") },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                containerColor = VerdeVoluntily,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Ver mapa"
                )
            }

            // boton de recargar con un trigger que recarga los datos de firebase al momento
            FloatingActionButton(
                onClick = { refreshTrigger++ },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color.White,
                contentColor = NaranjaVoluntily,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refrescar ofertas"
                )
            }
        }
    }
}

@Composable
fun OfertaCard(oferta: VolunteerOffer, onClick: () -> Unit) {
    val texts = LocalAppTexts.current //metemos los textos en ingles

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Categoria)
            Box(
                modifier = Modifier
                    .background(VerdeVoluntily.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    //metodo para traducir al ingles todas las etiquetas sin alterar nada
                    text = getTranslatedInterest(oferta.categoria, texts),
                    fontSize = 12.sp,
                    color = VerdeVoluntily,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Título
            Text(oferta.titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NaranjaVoluntily)

            // Descripción
            Text(oferta.descripcion, fontSize = 14.sp, color = Color.DarkGray, maxLines = 3)

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

            // Fecha inicio y plazas
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(" ${oferta.fechaInicio}", fontSize = 13.sp, color = Color.Gray)
                Text(
                    " ${oferta.plazasOcupadas}/${oferta.plazasTotal}",
                    fontSize   = 13.sp,
                    color      = if (oferta.plazasOcupadas >= oferta.plazasTotal) Color.Red else Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}