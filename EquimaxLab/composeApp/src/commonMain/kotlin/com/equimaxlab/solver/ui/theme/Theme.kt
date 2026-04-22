package com.equimaxlab.solver.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta casino dark
val GreenFelt       = Color(0xFF0D2B1A)   // fondo principal
val GreenFeltLight  = Color(0xFF143622)   // fondo de tarjetas/secciones
val GreenAccent     = Color(0xFF2ECC71)   // victoria / acción primaria
val GoldAccent      = Color(0xFFE8C84A)   // empate / labels destacados
val RedAccent       = Color(0xFFE74C3C)   // derrota / palos rojos
val CardWhite       = Color(0xFFF8F5EE)   // fondo de carta
val TextPrimary     = Color(0xFFECF0F1)
val TextSecondary   = Color(0xFF7F9980)
val DividerColor    = Color(0xFF1F4A2E)

private val PokerColorScheme = darkColorScheme(
    primary         = GreenAccent,
    onPrimary       = Color.Black,
    secondary       = GoldAccent,
    onSecondary     = Color.Black,
    background      = GreenFelt,
    onBackground    = TextPrimary,
    surface         = GreenFeltLight,
    onSurface       = TextPrimary,
    error           = RedAccent,
    onError         = Color.White,
    outline         = DividerColor
)

@Composable
fun PokerLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PokerColorScheme,
        typography  = PokerTypography,
        content     = content
    )
}