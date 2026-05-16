package com.voluntariado.madrid

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.voluntariado.madrid.ui.theme.NaranjaVoluntily
import com.voluntariado.madrid.ui.theme.VerdeVoluntily

@Composable
fun LoginScreen(navController: NavController, auth: FirebaseAuth) {
    val texts = LocalAppTexts.current

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    val degradadoCabecera = Brush.verticalGradient( //difuminado chuli
        colors = listOf(NaranjaVoluntily, VerdeVoluntily)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(degradadoCabecera),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Logo
                Image(
                    painter            = painterResource(id = R.drawable.voluntily_logo),
                    contentDescription = "Logo Voluntily",
                    modifier           = Modifier.size(120.dp)
                )

                Text(
                    text       = texts.loginTitle,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = NaranjaVoluntily
                )

                // Campo email
                OutlinedTextField(
                    value           = email,
                    onValueChange   = { email = it; errorMsg = "" },
                    label           = { Text(texts.email) },
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // para el @ salga
                    shape           = RoundedCornerShape(12.dp),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NaranjaVoluntily,
                        focusedLabelColor  = NaranjaVoluntily
                    )
                )

                // Campo contraseña
                OutlinedTextField(
                    value                = password,
                    onValueChange        = { password = it; errorMsg = "" },
                    label                = { Text(texts.password) },
                    singleLine           = true,
                    modifier             = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(), // pa que salgan asteriscos
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape                = RoundedCornerShape(12.dp),
                    colors               = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NaranjaVoluntily,
                        focusedLabelColor  = NaranjaVoluntily
                    )
                )

                // Mensaje de error
                if (errorMsg.isNotEmpty()) {
                    Text(text = errorMsg, color = Color.Red, fontSize = 13.sp)
                }

                // Boton login
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) { // task es si el usuario existe y la contraseña es correcta
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }// para que no pueda volver a la pantalla de login, si cierra la app vuelva con el usuario actual
                                    } else {
                                        errorMsg = "Email o contraseña incorrectos"
                                    }
                                }
                        } else {
                            errorMsg = texts.fillAllFields
                            //mensaje de completa todos los campos
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = NaranjaVoluntily)
                ) {
                    Text(texts.loginButton, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                // Ir a registro
                TextButton(onClick = { navController.navigate("register") }) {
                    Text(
                        text       = texts.noAccount,
                        color      = VerdeVoluntily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}