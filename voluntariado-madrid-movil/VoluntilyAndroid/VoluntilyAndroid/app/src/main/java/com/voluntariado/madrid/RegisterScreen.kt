package com.voluntariado.madrid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.voluntariado.madrid.ui.theme.NaranjaVoluntily
import com.voluntariado.madrid.ui.theme.VerdeVoluntily

// Opciones fijas de BBDD
val INTERESES_OPCIONES = listOf(
    "alimentacion",
    "educacion",
    "medio ambiente",
    "salud",
    "animales",
    "mayores",
    "infancia",
    "discapacidad"
)

// Opciones de idioma reducidas
val IDIOMAS_OPCIONES = listOf("es", "en")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, auth: FirebaseAuth) {
    val textos = LocalAppTexts.current
    val db = FirebaseFirestore.getInstance()

    var nombre       by remember { mutableStateOf("") }
    var apellidos    by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var confirmarContrasena  by remember { mutableStateOf("") }
    var ciudad       by remember { mutableStateOf("") }
    var selectorIdioma  by remember { mutableStateOf("es") }
    var mensajeError     by remember { mutableStateOf("") }
    var estaCargando    by remember { mutableStateOf(false) }

    val interesesSelec = remember { mutableStateListOf<String>() }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val degradado = Brush.verticalGradient(
        colors = listOf(VerdeVoluntily, NaranjaVoluntily)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(degradado),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                Text(
                    text       = textos.registerTitle,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = VerdeVoluntily
                )

                OutlinedTextField(
                    value         = nombre,
                    onValueChange = { nombre = it; mensajeError = "" },
                    label         = { Text(textos.name) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = campoColoresYbordes()
                )

                OutlinedTextField(
                    value         = apellidos,
                    onValueChange = { apellidos = it; mensajeError = "" },
                    label         = { Text(textos.surname) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = campoColoresYbordes()
                )

                OutlinedTextField(
                    value           = email,
                    onValueChange   = { email = it; mensajeError = "" },
                    label           = { Text(textos.email) },
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape           = RoundedCornerShape(12.dp),
                    colors          = campoColoresYbordes()
                )

                OutlinedTextField(
                    value                = password,
                    onValueChange        = { password = it; mensajeError = "" },
                    label                = { Text(textos.password) },
                    singleLine           = true,
                    modifier             = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape                = RoundedCornerShape(12.dp),
                    colors               = campoColoresYbordes()
                )

                OutlinedTextField(
                    value                = confirmarContrasena,
                    onValueChange        = { confirmarContrasena = it; mensajeError = "" },
                    label                = { Text(textos.confirmPassword) },
                    singleLine           = true,
                    modifier             = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape                = RoundedCornerShape(12.dp),
                    colors               = campoColoresYbordes()
                )

                OutlinedTextField(
                    value         = ciudad,
                    onValueChange = { ciudad = it },
                    label         = { Text(textos.city) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = campoColoresYbordes()
                )

                ExposedDropdownMenuBox(
                    expanded        = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        value         = if (selectorIdioma == "en") textos.englishLabel else textos.spanishLabel,
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text(textos.languageLabel) },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape         = RoundedCornerShape(12.dp),
                        colors        = campoColoresYbordes()
                    )
                    ExposedDropdownMenu(
                        expanded        = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        IDIOMAS_OPCIONES.forEach { idioma ->
                            DropdownMenuItem(
                                text    = { Text(if (idioma == "en") textos.englishLabel else textos.spanishLabel) },
                                onClick = {
                                    selectorIdioma     = idioma
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text       = textos.interestsLabel,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                    color      = VerdeVoluntily,
                    modifier   = Modifier.align(Alignment.Start)
                )

                INTERESES_OPCIONES.chunked(2).forEach { fila ->
                    //dividir las columnas en 2 con chunked
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        fila.forEach { interes ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked         = interesesSelec.contains(interes),
                                    onCheckedChange = { seleccionados ->
                                        if (seleccionados) interesesSelec.add(interes)
                                        else interesesSelec.remove(interes)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = VerdeVoluntily
                                    )
                                )
                                Text(text = getTranslatedInterest(interes, textos), fontSize = 13.sp)
                            }
                        }
                        if (fila.size == 1) Spacer(Modifier.weight(1f))
                    }
                }

                if (mensajeError.isNotEmpty()) {
                    Text(text = mensajeError, color = Color.Red, fontSize = 13.sp)
                }

                Button(
                    onClick  = {
                        when {
                            nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                                mensajeError = textos.fillAllFields
                            }
                            password != confirmarContrasena -> {
                                mensajeError = textos.passwordsDontMatch
                            }
                            password.length < 6 -> {
                                mensajeError = textos.passwordTooShort
                            }
                            else -> {
                                estaCargando = true
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task -> //comprueba si existe y si se ha registrado, comprueba los datos de usuarios de firebase
                                        if (task.isSuccessful) {
                                            val idUsuario = task.result.user?.uid ?: ""
                                            val usuario = hashMapOf(
                                                "nombre"          to nombre,
                                                "apellidos"       to apellidos,
                                                "email"           to email,
                                                "idioma"          to selectorIdioma,
                                                "intereses"       to interesesSelec.toList(),
                                                "ubicacion"       to hashMapOf(
                                                    "direccion" to ciudad,
                                                    "lat"       to 0.0,
                                                    "lng"       to 0.0
                                                ),
                                                "visibilidadMapa" to false,
                                                "fechaRegistro"   to Timestamp.now()
                                            )

                                            db.collection("volunteer_users")
                                                .document(idUsuario)
                                                .set(usuario)
                                                .addOnSuccessListener {
                                                    estaCargando = false
                                                    navController.navigate("login") {
                                                        popUpTo("register") { inclusive = true }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    estaCargando = false
                                                    mensajeError = "Error al guardar datos: ${e.message}"
                                                }
                                        } else {
                                            estaCargando = false
                                            mensajeError = "Error al registrar: ${task.exception?.message}"
                                        }
                                    }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = VerdeVoluntily),
                    enabled  = !estaCargando
                ) {
                    if (estaCargando) {
                        CircularProgressIndicator(
                            color  = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(textos.registerButton, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(onClick = { navController.popBackStack() }) {
                    Text(
                        text       = textos.hasAccount,
                        color      = NaranjaVoluntily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun campoColoresYbordes() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NaranjaVoluntily,
    focusedLabelColor  = NaranjaVoluntily
)