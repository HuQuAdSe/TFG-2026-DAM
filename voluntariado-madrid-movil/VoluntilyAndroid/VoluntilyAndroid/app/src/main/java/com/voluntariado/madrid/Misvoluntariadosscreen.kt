package com.voluntariado.madrid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
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

data class MiVoluntariado(
    val inscripcionId : String = "",
    val ofertaId     : String = "",
    val ofertaTitulo : String = "",
    val organizacionId: String = "",
    val estado       : String = "",
    val fechaInscripcion: String = "",
    val timestamp    : Long = 0.toLong(),
    val yaValorado   : Boolean = false,
    val certificadoEmitido: Boolean = false
)

@Composable
fun MisVoluntariadosScreen(navController: NavController, auth: FirebaseAuth) {
    val texts         = LocalAppTexts.current // metemos las traducciones aquí
    val db            = FirebaseFirestore.getInstance()
    val IdUsuario     = auth.currentUser?.uid ?: ""
    var lista         by remember { mutableStateOf<List<MiVoluntariado>>(emptyList()) }
    var cargando     by remember { mutableStateOf(true) }
    var mensajeError      by remember { mutableStateOf("") }

    val titulosOfertas = remember { mutableStateMapOf<String, String>() }

    DisposableEffect(IdUsuario) {
        if (IdUsuario.isEmpty()) {
            cargando = false
            return@DisposableEffect onDispose {}
            //si no hay usuario por algun error no crashea pero no muestra nada
        }

        val registration = db.collection("enrollments")
            .whereEqualTo("voluntarioUid", IdUsuario)
            .addSnapshotListener { enrollResult, e ->
                if (e != null) {
                    mensajeError = "${texts.connectionError}: ${e.message}"
                    cargando = false
                    return@addSnapshotListener
                    // si no encuentra nada, muestra el mensaje de error
                }

                if (enrollResult == null || enrollResult.isEmpty) {
                    lista = emptyList()
                    cargando = false
                    return@addSnapshotListener
                    //si no encuentra una inscripcion del usuario no muestra nada
                }

                val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                db.collection("ratings")
                    .whereEqualTo("voluntarioUid", IdUsuario)
                    .get()
                    .addOnSuccessListener { ratingsSnap ->
                        val idsOfertasValoradas = ratingsSnap.documents.mapNotNull { it.getString("ofertaId") }.toSet()

                        val nuevasInscripciones = mutableListOf<MiVoluntariado>()
                        val totalDocs = enrollResult.size()
                        var procesados = 0

                        enrollResult.documents.forEach { enrollDoc ->
                            val ofertaId    = enrollDoc.getString("ofertaId") ?: ""
                            val orgId       = enrollDoc.getString("organizacionId") ?: ""
                            val estadoDoc   = enrollDoc.getString("estado")?.trim()?.lowercase() ?: "pendiente"
                            val fechaTs     = enrollDoc.getTimestamp("fechaInscripcion")
                            val fechaStr    = fechaTs?.toDate()?.let { fecha.format(it) } ?: ""
                            val timestamp   = fechaTs?.toDate()?.time ?: 0.toLong()
                            val certEmitido = enrollDoc.getBoolean("certificadoEmitido") ?: false
                            val yaValorado  = idsOfertasValoradas.contains(ofertaId)

                            val tituloCache = titulosOfertas[ofertaId]
                            if (tituloCache != null) {
                                nuevasInscripciones.add(MiVoluntariado(enrollDoc.id, ofertaId, tituloCache, orgId, estadoDoc, fechaStr, timestamp, yaValorado, certEmitido))
                                procesados++
                                if (procesados == totalDocs) {
                                    lista = nuevasInscripciones.sortedByDescending { it.timestamp }
                                    //ordenarlas por fecha de inscripcion
                                    cargando = false
                                }
                            } else {
                                db.collection("volunteer_offers").document(ofertaId).get()
                                    .addOnSuccessListener { ofertaDoc ->
                                        val titulo = ofertaDoc.getString("titulo") ?: "Sin título"
                                        titulosOfertas[ofertaId] = titulo
                                        nuevasInscripciones.add(MiVoluntariado(enrollDoc.id, ofertaId, titulo, orgId, estadoDoc, fechaStr, timestamp, yaValorado, certEmitido))
                                        procesados++
                                        if (procesados == totalDocs) {
                                            lista = nuevasInscripciones.sortedByDescending { it.timestamp }
                                            cargando = false
                                        }
                                    }
                                    .addOnFailureListener {
                                        procesados++
                                        if (procesados == totalDocs) {
                                            lista = nuevasInscripciones.sortedByDescending { it.timestamp }
                                            cargando = false
                                        }
                                    }
                            }
                        }
                    }
            }

        onDispose { registration.remove() }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF8F0))) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(NaranjaVoluntily, VerdeVoluntily)))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Default.ArrowBack, contentDescription = texts.back, tint = Color.White)
            }
            Text(texts.myVolunteering, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.Center))
        }

        when {
            cargando ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NaranjaVoluntily) }
            mensajeError.isNotEmpty() ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(mensajeError, color = Color.Red) }
            lista.isEmpty() ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(texts.noVolunteering, color = Color.Gray) }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista, key = { it.inscripcionId }) { item ->
                        MiVoluntariadoCard(item = item, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiVoluntariadoCard(item: MiVoluntariado, navController: NavController) {
    val texts = LocalAppTexts.current // meto las traducciones
    val estadoLimpio = item.estado.trim().lowercase()


    val (colorEstado, textoEstado) = when (estadoLimpio) {
        "completado"  -> Color(0xFF1565C0) to texts.completed
        else          -> NaranjaVoluntily to texts.pending
    }

    var ventanitaDeValoraciones by remember { mutableStateOf(false) }
    var cargaValoraciones by remember { mutableStateOf(false) }
    var listaValoraciones by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val db = FirebaseFirestore.getInstance()

    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text(item.ofertaTitulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NaranjaVoluntily, modifier = Modifier.padding(end = 36.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.background(colorEstado.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(textoEstado, fontSize = 12.sp, color = colorEstado, fontWeight = FontWeight.SemiBold)
                    }
                    Text(" ${item.fechaInscripcion}",
                        fontSize = 12.sp, color = Color.Gray)
                }

                if (estadoLimpio == "completado") {
                    Button(
                        onClick = {
                            if (!item.yaValorado) {
                                navController.navigate("rating/${item.organizacionId}/${item.ofertaId}")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.yaValorado) Color(0xFF454545) else Color(0xFFFFB300)
                        ),
                        enabled = !item.yaValorado
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (item.yaValorado) texts.alreadyRated else texts.rateExperience,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    ventanitaDeValoraciones = true
                    cargaValoraciones = true

                    db.collection("ratings")
                        .whereEqualTo("ofertaId", item.ofertaId)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val listaTemporal = mutableListOf<Map<String, Any>>()
                            val totalRatings = snapshot.size()

                            if (totalRatings == 0) {
                                listaValoraciones = emptyList()
                                cargaValoraciones = false
                                return@addOnSuccessListener
                            }

                            var valorados = 0
                            for (doc in snapshot.documents) {
                                val estrellasValoracion = doc.data?.toMutableMap() ?: mutableMapOf()
                                val idVoluntario = estrellasValoracion["voluntarioUid"] as? String ?: ""

                                if (idVoluntario.isNotEmpty()) {
                                    db.collection("volunteer_users").document(idVoluntario).get()
                                        .addOnSuccessListener { userDoc ->
                                            estrellasValoracion["voluntarioNombre"] = userDoc.getString("nombre") ?: ""
                                            listaTemporal.add(estrellasValoracion)
                                            valorados++
                                            if (valorados == totalRatings) {
                                                listaValoraciones = listaTemporal
                                                cargaValoraciones = false
                                            }
                                        }
                                        .addOnFailureListener {
                                            estrellasValoracion["voluntarioNombre"] = "Anónimo"
                                            listaTemporal.add(estrellasValoracion)
                                            valorados++
                                            if (valorados == totalRatings) {
                                                listaValoraciones = listaTemporal
                                                cargaValoraciones = false
                                            }
                                        }
                                } else {
                                    estrellasValoracion["voluntarioNombre"] = "Anónimo"
                                    listaTemporal.add(estrellasValoracion)
                                    valorados++
                                    if (valorados == totalRatings) {
                                        listaValoraciones = listaTemporal
                                        cargaValoraciones = false
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { cargaValoraciones = false }
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = texts.ratingsTitle, tint = Color(0xFFFFB300))
            }
        }
    }

    if (ventanitaDeValoraciones) {
        AlertDialog( // ventanita de valoraciones chuli
            onDismissRequest = { ventanitaDeValoraciones = false },// si pulsa en la pantalla fuera de las valoraciones que se cierre
            title = { Text(text = texts.ratingsTitle, fontWeight = FontWeight.Bold, color = NaranjaVoluntily) },
            text = {
                when {
                    cargaValoraciones -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = NaranjaVoluntily)
                        }
                    }
                    listaValoraciones.isEmpty() -> {
                        Text(texts.noRatings, color = Color.Gray, fontSize = 15.sp)
                    }
                    else -> {
                        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                            listaValoraciones.forEach { datosValoracion ->
                                val puntuacion = (datosValoracion["puntuacion"] as? Long) ?: 0.toLong()
                                val comentario = (datosValoracion["comentario"] as? String) ?: ""
                                val nombreReal = (datosValoracion["voluntarioNombre"] as? String) ?: "Anónimo"

                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(nombreReal, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VerdeVoluntily)

                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(16.dp))
                                            Text(" $puntuacion/5", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }

                                        if (comentario.isNotEmpty()) {
                                            Text("«$comentario»", fontSize = 14.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { ventanitaDeValoraciones = false }) {
                    Text(texts.close, color = VerdeVoluntily, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}