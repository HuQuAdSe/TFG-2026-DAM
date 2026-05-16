package com.voluntariado.madrid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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

@Composable
fun SettingsScreen(navController: NavController, auth: FirebaseAuth) {
    val textos = LocalAppTexts.current
    val db = FirebaseFirestore.getInstance()
    val idUsuario = auth.currentUser?.uid ?: ""

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var idioma by remember { mutableStateOf("es") }
    var interesesSeleccionados by remember { mutableStateOf(setOf<String>()) }

    var estaCargando by remember { mutableStateOf(true) }
    var guardado by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    val listaIntereses = listOf(
        "alimentacion", "educacion", "medio ambiente", "salud",
        "animales", "mayores", "infancia", "discapacidad"
    )

    val listaIdiomas = listOf(
        "es" to textos.spanishLabel,
        "en" to textos.englishLabel
    )

    LaunchedEffect(idUsuario) {
        if (idUsuario.isNotEmpty()) {
            db.collection("volunteer_users").document(idUsuario).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        nombre = doc.getString("nombre") ?: ""
                        apellidos = doc.getString("apellidos") ?: ""
                        idioma = doc.getString("idioma") ?: "es"
                        val intereses = doc.get("intereses") as? List<String> ?: emptyList()
                        interesesSeleccionados = intereses.toSet()
                    // toset() para meter los intereses sin que se repitan por si los selecciono varias veces
                    }
                    estaCargando = false
                }
                .addOnFailureListener {
                    estaCargando = false
                }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(NaranjaVoluntily, VerdeVoluntily)))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .statusBarsPadding()
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = textos.back, tint = Color.White)
                }
                Text(
                    text = textos.settingsTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    ) { padding ->
        if (estaCargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NaranjaVoluntily)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFFFF8F0))
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(textos.personalInfo, fontWeight = FontWeight.Bold, color = NaranjaVoluntily)
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; mensaje = "" },
                    label = { Text(textos.name) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdeVoluntily)
                )
                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { apellidos = it; mensaje = "" },
                    label = { Text(textos.surname) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdeVoluntily)
                )

                Text(textos.preferredLanguage, fontWeight = FontWeight.Bold, color = NaranjaVoluntily)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listaIdiomas.forEach { (code, label) ->
                        FilterChip(
                            selected = idioma == code,
                            onClick = { idioma = code; mensaje = "" },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VerdeVoluntily.copy(alpha = 0.2f),
                                selectedLabelColor = VerdeVoluntily
                            )
                        )
                    }
                }

                Text(textos.myInterests, fontWeight = FontWeight.Bold, color = NaranjaVoluntily)
                FlowRow(
                    //cuadricula para alinear los intereses segun el espacio de la pantalla
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listaIntereses.forEach { interes ->
                        val estaSeleccionado = interesesSeleccionados.contains(interes)
                        FilterChip(
                            // sustituto de los checkbox, esta encendido o apagado
                            selected = estaSeleccionado,
                            onClick = {
                                mensaje = ""
                                interesesSeleccionados = if (estaSeleccionado) {
                                    interesesSeleccionados - interes
                                } else {
                                    interesesSeleccionados + interes
                                }
                            },
                            label = { Text(getTranslatedInterest(interes, textos)) },
                            leadingIcon = if (estaSeleccionado) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VerdeVoluntily,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (mensaje.isNotEmpty()) {
                    Text(mensaje, color = if (mensaje.contains("Error")) Color.Red else VerdeVoluntily, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                Button(
                    onClick = {
                        if (idUsuario.isNotEmpty()) {
                            guardado = true
                            val updates = mapOf(
                                "nombre" to nombre,
                                "apellidos" to apellidos,
                                "idioma" to idioma,
                                "intereses" to interesesSeleccionados.toList()
                            )
                            db.collection("volunteer_users").document(idUsuario)
                                .update(updates)
                                .addOnSuccessListener {
                                    guardado = false
                                    mensaje = textos.profileUpdated
                                }
                                .addOnFailureListener {
                                    guardado = false
                                    mensaje = textos.updateError
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeVoluntily),
                    enabled = !guardado
                ) {
                    if (guardado) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    } else {
                        Text(textos.saveChanges, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                TextButton(
                    onClick = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(textos.logout, color = Color.Red.copy(alpha = 0.7f))
                }
            }
        }
    }
}