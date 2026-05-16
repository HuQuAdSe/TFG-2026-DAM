package com.voluntariado.madrid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.voluntariado.madrid.ui.theme.NaranjaVoluntily
import com.voluntariado.madrid.ui.theme.VerdeVoluntily
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OfertaDetalleScreen(
    navController: NavController,
    ofertaId     : String,
    auth         : FirebaseAuth
) {
    val texts = LocalAppTexts.current
    val db           = FirebaseFirestore.getInstance()

    var oferta       by remember { mutableStateOf<VolunteerOffer?>(null) }
    var estaCargando    by remember { mutableStateOf(true) }
    var mensajeError     by remember { mutableStateOf("") }

    var inscripcionId by remember { mutableStateOf<String?>(null) }
    var cargando    by remember { mutableStateOf(false) }
    var mensajeExito  by remember { mutableStateOf("") }

    val idUsuario = auth.currentUser?.uid ?: ""

    // Descargar los datos una sola vez al abrir la pantalla
    LaunchedEffect(ofertaId) {
        // Obtenemos la oferta
        db.collection("volunteer_offers").document(ofertaId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val ubicacionOferta = snapshot.get("ubicacion") as? Map<String, Any>

                    oferta = VolunteerOffer(
                        id             = snapshot.id,
                        titulo         = snapshot.getString("titulo") ?: "",
                        descripcion    = snapshot.getString("descripcion") ?: "",
                        fechaInicio    = snapshot.getTimestamp("fechaInicio")?.toDate()?.let { fecha.format(it) } ?: "",
                        fechaFin       = snapshot.getTimestamp("fechaFin")?.toDate()?.let { fecha.format(it) } ?: "",
                        plazasTotal    = (snapshot.getLong("plazasTotal") ?: 0.toLong()).toInt(),
                        plazasOcupadas = (snapshot.getLong("plazasOcupadas") ?: 0.toLong()).toInt(),
                        categoria      = snapshot.getString("categoria") ?: "",
                        estado         = snapshot.getString("estado") ?: "",
                        organizacionId = snapshot.getString("organizacionId") ?: "",
                        ubicacionDir   = ubicacionOferta?.get("direccion")?.toString() ?: ""
                    )

                    // ya tengo la oferta asi que compruebo si el usuario esta inscrito
                    if (idUsuario.isNotEmpty()) {
                        db.collection("enrollments")
                            .whereEqualTo("voluntarioUid", idUsuario)
                            .whereEqualTo("ofertaId", ofertaId)
                            .get()
                            .addOnSuccessListener { enrollSnap ->
                                if (!enrollSnap.isEmpty) {
                                    inscripcionId = enrollSnap.documents[0].id
                                }
                                estaCargando = false
                            }
                            .addOnFailureListener {
                                estaCargando = false
                            }
                    } else {
                        estaCargando = false
                    }
                } else {
                    mensajeError = "La oferta no existe"
                    estaCargando = false
                }
            }
            .addOnFailureListener {
                mensajeError = texts.connectionError
                estaCargando = false
            }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF8F0))) {
        // Cabecera
        Box(modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(NaranjaVoluntily, VerdeVoluntily))).padding(12.dp)) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Default.ArrowBack, contentDescription = texts.back, tint = Color.White)
            }
            Text(texts.offerDetail, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.Center))
        }

        when {
            estaCargando -> Box(Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NaranjaVoluntily) }
            mensajeError.isNotEmpty() ->
                Box(Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center) {
                    Text(mensajeError, color = Color.Red) }

            oferta != null -> {
                val o = oferta!!
                val yaApuntado = inscripcionId != null
                val plazasLibres = o.plazasTotal - o.plazasOcupadas
                val sinPlazas = plazasLibres <= 0

                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), Arrangement.spacedBy(12.dp)) {
                            // Título y categoría
                            Box(
                                modifier = Modifier
                                    .background(VerdeVoluntily.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = getTranslatedInterest(o.categoria, texts),
                                    fontSize = 12.sp,
                                    color = VerdeVoluntily,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(o.titulo, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NaranjaVoluntily)
                            Text(o.descripcion, fontSize = 15.sp, color = Color.DarkGray)

                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                            if (o.fechaInicio.isNotEmpty()) FilaDetalle( etiqueta = texts.startDate, valor = o.fechaInicio)
                            if (o.fechaFin.isNotEmpty()) FilaDetalle( etiqueta = texts.endDate, valor = o.fechaFin)
                            if (o.ubicacionDir.isNotEmpty()) FilaDetalle( etiqueta = texts.address, valor = o.ubicacionDir)

                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                            FilaDetalle(
                                etiqueta = texts.availableSlots,
                                valor = "$plazasLibres ${texts.of} ${o.plazasTotal}",
                                valorColor = if (sinPlazas && !yaApuntado) Color.Red else VerdeVoluntily
                            )
                        }
                    }

                    if (mensajeExito.isNotEmpty()) Text(mensajeExito, color = VerdeVoluntily, fontWeight = FontWeight.Bold)

                    // Botón con lógica secuencial básica
                    Button(
                        onClick = {
                            if (idUsuario.isEmpty()) return@Button
                            // si falla la utenticacion o el usuario no existe no hace nada, para controlar el error
                            cargando = true
                            mensajeExito = ""
                            mensajeError = ""

                            if (yaApuntado) {
                                // si me desapunto que se borre la inscripcion y se actualiza el numero total de inscritos
                                db.collection("enrollments").document(inscripcionId!!).delete()
                                    .addOnSuccessListener {
                                        val nuevasOcupadas = o.plazasOcupadas - 1
                                        var nuevoEstado = o.estado
                                        if (nuevasOcupadas < o.plazasTotal) {
                                            nuevoEstado = "activo"
                                        }

                                        db.collection("volunteer_offers").document(ofertaId)
                                            .update(
                                                "plazasOcupadas", nuevasOcupadas,
                                                "estado", nuevoEstado
                                            )
                                            .addOnSuccessListener {
                                                // Actualizamos la variable local para que la pantalla se refresque
                                                oferta = o.copy(plazasOcupadas = nuevasOcupadas, estado = nuevoEstado)
                                                inscripcionId = null
                                                mensajeExito = texts.unappliedSuccess
                                                cargando = false
                                            }
                                    }
                            } else {
                                // compruebo las plazas y si puedo apuntarme,actualizamos oferta y huecos
                                if (o.plazasOcupadas < o.plazasTotal) {
                                    val nuevasOcupadas = o.plazasOcupadas + 1
                                    val seAcabaDeLlenar = nuevasOcupadas >= o.plazasTotal

                                    val nuevaInscripcionId = db.collection("enrollments").document()

                                    // Si llenamos la oferta, mi propia inscripción ya entra como completada
                                    val miEstado = if (seAcabaDeLlenar) "completado" else "pendiente"

                                    val inscripcion = hashMapOf(
                                        "voluntarioUid" to idUsuario,
                                        "ofertaId" to ofertaId,
                                        "organizacionId" to o.organizacionId,
                                        "estado" to miEstado,
                                        "fechaInscripcion" to Timestamp.now(),
                                        "certificadoEmitido" to false
                                    )

                                    nuevaInscripcionId.set(inscripcion)
                                        .addOnSuccessListener {
                                            var nuevoEstado = o.estado

                                            // Si se ha llenado, cambiamos la oferta y todas las inscripciones
                                            if (seAcabaDeLlenar) {
                                                nuevoEstado = "completado"

                                                db.collection("enrollments")
                                                    .whereEqualTo("ofertaId", ofertaId)
                                                    .get()
                                                    .addOnSuccessListener { querySnapshot ->
                       // escucha los cambios el listener y el querySnapshot es el resultado de la lista de inscripciones(que los extrae como documentos)
                                                        for (documento in querySnapshot.documents) {
                                                            db.collection("enrollments")
                                                                .document(documento.id)
                                                                .update("estado", "completado") // cambio el estado para que se pueda valorar
                                                        }
                                                    }
                                            }

                                            db.collection("volunteer_offers").document(ofertaId)
                                                .update(
                                                    "plazasOcupadas", nuevasOcupadas,
                                                    "estado", nuevoEstado
                                                )
                                                .addOnSuccessListener {
                                                    // Actualizo la variable para que se refresque el boton del estado de la inscripcion
                                                    oferta = o.copy(plazasOcupadas = nuevasOcupadas, estado = nuevoEstado)
                                                    inscripcionId = nuevaInscripcionId.id
                                                    mensajeExito = texts.appliedSuccess
                                                    cargando = false
                                                }
                                        }
                                } else {
                                    mensajeError = texts.noSlotsAvailable
                                    cargando = false
                                }
                            }
                        },
                        enabled = !cargando && (yaApuntado || !sinPlazas),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (yaApuntado) Color(0xFFE57373) else VerdeVoluntily)
                    ) {
                        if (cargando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        else Text(if (yaApuntado) texts.unapply else texts.apply, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaDetalle( etiqueta: String, valor: String, valorColor: Color = Color.DarkGray) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("  $etiqueta: ", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Text(valor, color = valorColor, fontSize = 15.sp)
    }
}