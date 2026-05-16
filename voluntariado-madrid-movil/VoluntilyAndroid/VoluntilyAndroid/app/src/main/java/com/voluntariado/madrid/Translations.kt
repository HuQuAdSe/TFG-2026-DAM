package com.voluntariado.madrid

import androidx.compose.runtime.compositionLocalOf

data class AppTexts(
    // Títulos y generales
    val homeTitle: String = "Ofertas de voluntariado",
    val myVolunteering: String = "Mis voluntariados",
    val settingsTitle: String = "Ajustes de perfil",
    val offerDetail: String = "Detalle de oferta",
    val back: String = "Volver",
    val loading: String = "Cargando",
    val noOffers: String = "No hay ofertas activas",
    val connectionError: String = "Error de conexión",
    val startDate: String = "Fecha de inicio",
    val endDate: String = "Fecha de fin",
    val address: String = "Dirección",

    // Login y Registro
    val loginTitle: String = "Iniciar Sesión",
    val registerTitle: String = "Crear cuenta",
    val email: String = "Correo electrónico",
    val password: String = "Contraseña",
    val confirmPassword: String = "Confirmar contraseña",
    val name: String = "Nombre",
    val surname: String = "Apellidos",
    val city: String = "Ciudad",
    val interestsLabel: String = "Intereses",
    val languageLabel: String = "Idioma",
    val spanishLabel: String = "Español",
    val englishLabel: String = "Inglés",
    val loginButton: String = "Entrar",
    val registerButton: String = "Registrarse",
    val noAccount: String = "¿No tienes cuenta? Regístrate",
    val registerNow: String = "Regístrate aquí",
    val hasAccount: String = "¿Ya tienes cuenta? Inicia sesión",
    val fillAllFields: String = "Rellena todos los campos obligatorios",
    val passwordsDontMatch: String = "Las contraseñas no coinciden",
    val passwordTooShort: String = "La contraseña debe tener al menos 6 caracteres",

    // Oferta Detalle
    val apply: String = "Apuntarme ahora",
    val unapply: String = "Desapuntarme",
    val alreadyEnrolled: String = "Ya estás apuntado/a",
    val availableSlots: String = "Plazas disponibles",
    val of: String = "de",
    val unappliedSuccess: String = "Te has desapuntado correctamente",
    val appliedSuccess: String = "Te has apuntado correctamente",
    val noSlotsAvailable: String = "Sin plazas disponibles",

    // Ajustes
    val personalInfo: String = "Información Personal",
    val preferredLanguage: String = "Idioma preferido",
    val myInterests: String = "Mis Intereses",
    val saveChanges: String = "Guardar cambios",
    val logout: String = "Cerrar Sesión",
    val profileUpdated: String = "Perfil actualizado",
    val updateError: String = "Error al actualizar",

    // Mis Voluntariados y Rating
    val pending: String = "Pendiente ",
    val confirmed: String = "Confirmado ",
    val completed: String = "Completado ",
    val rateExperience: String = "Valorar experiencia",
    val alreadyRated: String = "Ya has valorado esta actividad",
    val howWasExperience: String = "Valora tu experiencia",
    val commentOptional: String = "Comentario (opcional)",
    val sendRating: String = "Enviar valoración",
    val ratingError: String = "Error al enviar la valoración",
    val noVolunteering: String = "No tienes voluntariados",
    val ratingsTitle: String = "Valoraciones",
    val close: String = "Cerrar",
    val noRatings: String = "Este voluntariado no tiene valoraciones.",

    // Intereses
    val intAlimentacion: String = "alimentacion",
    val intEducacion: String = "educacion",
    val intMedioAmbiente: String = "medio ambiente",
    val intSalud: String = "salud",
    val intAnimales: String = "animales",
    val intMayores: String = "mayores",
    val intInfancia: String = "infancia",
    val intDiscapacidad: String = "discapacidad"
)

val TextosEspanol = AppTexts()

val TextosIngles = AppTexts(
    homeTitle = "Volunteer Offers",
    myVolunteering = "My Volunteering",
    settingsTitle = "Profile Settings",
    offerDetail = "Offer Detail",
    back = "Back",
    loading = "Loading",
    noOffers = "No active offers",
    connectionError = "Connection error",
    loginTitle = "Login",
    registerTitle = "Create account",
    email = "Email",
    password = "Password",
    confirmPassword = "Confirm Password",
    name = "First Name",
    surname = "Last Name",
    city = "City",
    interestsLabel = "Interests",
    languageLabel = "Language",
    spanishLabel = "Spanish",
    englishLabel = "English",
    loginButton = "Sign In",
    registerButton = "Register",
    noAccount = "Don't have an account?",
    registerNow = "Register here",
    hasAccount = "Already have an account? Login",
    fillAllFields = "Please fill all required fields",
    passwordsDontMatch = "Passwords do not match",
    passwordTooShort = "Password must be at least 6 characters",
    apply = "Apply now",
    unapply = "Unsubscribe",
    alreadyEnrolled = "Already enrolled",
    availableSlots = "Available slots",
    of = "of",
    unappliedSuccess = "Unsubscribed successfully",
    appliedSuccess = "Applied successfully!",
    noSlotsAvailable = "No slots available",
    personalInfo = "Personal Information",
    preferredLanguage = "Preferred Language",
    myInterests = "My Interests",
    saveChanges = "Save Changes",
    logout = "Log Out",
    profileUpdated = "Profile updated!",
    updateError = "Update error",
    pending = "Pending ",
    confirmed = "Confirmed ",
    completed = "Completed ",
    rateExperience = "Rate Experience",
    alreadyRated = "Activity already rated",
    howWasExperience = "How was your experience?",
    commentOptional = "Comment (optional)",
    sendRating = "Send Rating",
    ratingError = "Error sending rating",
    startDate = "Start date",
    endDate = "End date",
    address = "Address",
    noVolunteering = "You have no volunteering activities",
    ratingsTitle = "Ratings",
    close = "Close",
    noRatings = "This activity has no ratings.",

    // Intereses
    intAlimentacion = "food",
    intEducacion = "education",
    intMedioAmbiente = "environment",
    intSalud = "health",
    intAnimales = "animals",
    intMayores = "elderly",
    intInfancia = "children",
    intDiscapacidad = "disability"
)

val LocalAppTexts = compositionLocalOf { TextosEspanol }

fun getTexts(lang: String?): AppTexts {
    return if (lang?.trim()?.lowercase() == "en") TextosIngles else TextosEspanol
}

fun getTranslatedInterest(interest: String, texts: AppTexts): String {
    return when (interest.lowercase().trim()) {
        "alimentacion" -> texts.intAlimentacion
        "educacion" -> texts.intEducacion
        "medio ambiente" -> texts.intMedioAmbiente
        "salud" -> texts.intSalud
        "animales" -> texts.intAnimales
        "mayores" -> texts.intMayores
        "infancia" -> texts.intInfancia
        "discapacidad" -> texts.intDiscapacidad
        else -> interest
    }
}