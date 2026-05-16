package com.voluntariado.madrid.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NaranjaVoluntily = Color(0xFFF47421)
val VerdeVoluntily   = Color(0xFF4CAF50)
val FondoCrema       = Color(0xFFFFF8F0)

private val colores = lightColorScheme(
    primary    = NaranjaVoluntily,
    secondary  = VerdeVoluntily,
    background = FondoCrema,
    surface    = Color.White
)

@Composable
fun VoluntilyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colores,
        content     = content
    )
}