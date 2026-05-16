package com.voluntariado.madrid

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.app.NotificationCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.voluntariado.madrid.ui.theme.VoluntilyTheme

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    //inicio la variable auth vacia para evitar errores, pero que mas tarde se pone con el usuario que se loguea

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        // "escuchador" de ofertas nuevas
        var cargaInicial = true
        FirebaseFirestore.getInstance().collection("volunteer_offers")
            .addSnapshotListener { snapshots, e ->
                // Si hay un error o no hay datos,nada, salimos de este bloque para que no crashee
                if (e != null || snapshots == null) return@addSnapshotListener

                // Ignoro la primera vez que se lee la base de datos al ejecutarlo para que no salgan notificaciones de toooodas las ofertas que hay
                if (cargaInicial) {
                    cargaInicial = false
                    return@addSnapshotListener
                }

                // Busco si se ha añadido una oferta nueva
                for (cambio in snapshots.documentChanges) {
                    if (cambio.type == DocumentChange.Type.ADDED) { // added es para escuchar el cambio de datos, es decir, añadirlos
                        mostrarNotificacion(this)
                    }
                }
            }


        enableEdgeToEdge()
        setContent {
            VoluntilyTheme {
                val navController = rememberNavController()
                val db = FirebaseFirestore.getInstance()


                // Estado global para el idioma
                var lenguajeUsuario by remember { mutableStateOf("es") }

                //  estado reactivo para el UID del usuario
                var idUsuarioActivo by remember { mutableStateOf(auth.currentUser?.uid ?: "") }

                //  escuchador de sesión para saber cuándo el usuario entra o sale
                DisposableEffect(Unit) {
                    val autenticadorDeUsuario = FirebaseAuth.AuthStateListener { firebaseAuth ->
                        idUsuarioActivo = firebaseAuth.currentUser?.uid ?: ""
                    }
                    auth.addAuthStateListener(autenticadorDeUsuario) // para que tenga en cuenta el usuario y los cambios de idioma, que con el listener se solapaba
                    onDispose {
                        auth.removeAuthStateListener(autenticadorDeUsuario)
                    }
                }

                // Escuchar cambios en el perfil usando el UID reactivo
                LaunchedEffect(idUsuarioActivo) {
                    if (idUsuarioActivo.isNotEmpty()) {
                        db.collection("volunteer_users").document(idUsuarioActivo)
                            .addSnapshotListener { snapshot, _ ->
                                snapshot?.getString("idioma")?.let {
                                    lenguajeUsuario = it
                                }
                            }
                    } else {
                        // Si cierra sesion, vuelve al español por defecto
                        lenguajeUsuario = "es"
                    }
                }


                // Proveedor de textos para toda la app para cambiar el idioma  a los textos
                CompositionLocalProvider(LocalAppTexts provides getTexts(lenguajeUsuario)) {
                    NavHost(
                        navController    = navController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            LoginScreen(navController = navController, auth = auth)
                        }
                        composable("register") {
                            RegisterScreen(navController = navController, auth = auth)
                        }
                        composable("home") {
                            HomeScreen(navController = navController, auth = auth)
                        }
                        composable("mapa") {
                            MapScreen(navController = navController)
                        }
                        composable("oferta_detalle/{ofertaId}") { backStackEntry ->  // decirle a la pantalla de detalle que me pase el id de la oferta
                            val ofertaId = backStackEntry.arguments?.getString("ofertaId") ?: "" // sacar los argumentos de cada oferta
                            OfertaDetalleScreen(
                                navController = navController,
                                ofertaId      = ofertaId,
                                auth          = auth
                            )
                        }
                        composable("mis_voluntariados") {
                            MisVoluntariadosScreen(
                                navController = navController,
                                auth          = auth
                            )
                        }
                        composable("rating/{organizacionId}/{ofertaId}") { backStackEntry ->// le pasamos las variables mutables de la oferta
                            // le pasa los argumentos y los datos de cada organizacion, sino null para que no haya errores
                            val orgId = backStackEntry.arguments?.getString("organizacionId") ?: ""
                            val ofertaId = backStackEntry.arguments?.getString("ofertaId") ?: ""
                            RatingScreen(
                                navController = navController,
                                organizacionId = orgId,
                                ofertaId = ofertaId,
                                auth = auth
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                navController = navController,
                                auth = auth
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {

        }
    }
}

// Metodo para la notificacion de que hay nuevo voluntariado cerca de ti
fun mostrarNotificacion(context: Context) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Crear el canal de notificaciones (en la documentacion de google pone que funciona a partir de android OREO)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val canal = NotificationChannel(
            "voluntariados",
            "avisos",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(canal)
    }

    // Construimos la notificacion para que salga y que suene bien
    val notificacion = NotificationCompat.Builder(context, "voluntariados")
        .setSmallIcon(R.drawable.voluntily_logo) // el iconito de voluntily
        .setContentTitle("¡Nuevas Ofertas!")
        .setContentText("¡Eh, hay un nuevo voluntariado cerca de ti!")
        .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridad alta para que suene
        .setAutoCancel(true)
        .build()

    // Lanzar la notificación
    manager.notify(System.currentTimeMillis().toInt(), notificacion)
}