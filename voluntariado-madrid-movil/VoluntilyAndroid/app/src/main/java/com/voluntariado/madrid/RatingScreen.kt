package com.voluntariado.madrid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
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

@Composable
fun RatingScreen(
    navController: NavController,
    organizacionId: String,
    ofertaId: String,
    auth : FirebaseAuth
) {
    val texts = LocalAppTexts.current // meto traducciones
    val db = FirebaseFirestore.getInstance()
    var puntuacion by remember { mutableStateOf(5) }
    var comentario by remember { mutableStateOf("") }
    var enviando   by remember { mutableStateOf(false) }
    var mensajeError   by remember { mutableStateOf("") }

    val idUsuario = auth.currentUser?.uid ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F0))
    ) {
        // Cabecera
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(NaranjaVoluntily, VerdeVoluntily)))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },// vuelve sin cerrar la sesion
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = texts.back, tint = Color.White)
            }
            Text(
                text = texts.rateExperience,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = texts.howWasExperience,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = NaranjaVoluntily
            )

            // Selector de estrellas
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(5) { index ->
                    val numeroEstrellas = index + 1
                    IconButton(onClick = { puntuacion = numeroEstrellas }) {
                        Icon(
                            imageVector = if (numeroEstrellas <= puntuacion) Icons.Filled.Star else Icons.Outlined.Star, //rellenar o quitar estrellas
                            contentDescription = null,
                            tint = if (numeroEstrellas <= puntuacion) Color(0xFFFFB300) else Color.Gray, //color de las estrellas
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text(texts.commentOptional) },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeVoluntily,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            if (mensajeError.isNotEmpty()) {
                Text(mensajeError, color = Color.Red, fontSize = 14.sp)
            }

            Button(
                onClick = {
                    if (idUsuario.isEmpty()) return@Button // si no hay usuario no hace nada, para controlar el error
                    enviando = true
                    val puntuaciones = hashMapOf(
                        "puntuacion"    to puntuacion,
                        "comentario"    to comentario,
                        "organizacionId" to organizacionId,
                        "ofertaId"       to ofertaId,
                        "voluntarioUid"  to idUsuario,
                        "fecha"         to Timestamp.now()
                    )

                    db.collection("ratings")
                        .add(puntuaciones)
                        .addOnSuccessListener {
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            mensajeError = texts.ratingError
                            enviando = false
                        }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !enviando,
                colors = ButtonDefaults.buttonColors(containerColor = VerdeVoluntily)
            ) {
                if (enviando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text(texts.sendRating, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}