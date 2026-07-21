package com.scoutingsampdoria.persone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Palette ispirata ai colori U.C. Sampdoria:
 * blu blucerchiato come colore dominante, con gli accenti
 * bianco / rosso / nero della fascia dello scudetto.
 */
object SampColors {
    val Blu = Color(0xFF00539F)          // blu Sampdoria
    val BluScuro = Color(0xFF003A70)     // variante scura per barre/contrasti
    val BluChiaro = Color(0xFF3B7BC4)    // variante chiara per superfici attive
    val Rosso = Color(0xFFD3010C)        // rosso della fascia
    val Nero = Color(0xFF1A1A1A)         // nero della fascia
    val Bianco = Color(0xFFFFFFFF)
    val SfondoChiaro = Color(0xFFF4F7FB) // sfondo generale, azzurrino freddo
    val CardChiaro = Color(0xFFFFFFFF)
    val GrigioTesto = Color(0xFF5A6572)
}

private val LightColors = lightColorScheme(
    primary = SampColors.Blu,
    onPrimary = SampColors.Bianco,
    primaryContainer = SampColors.BluChiaro,
    onPrimaryContainer = SampColors.Bianco,
    secondary = SampColors.Rosso,
    onSecondary = SampColors.Bianco,
    tertiary = SampColors.Nero,
    background = SampColors.SfondoChiaro,
    onBackground = SampColors.Nero,
    surface = SampColors.CardChiaro,
    onSurface = SampColors.Nero,
    surfaceVariant = Color(0xFFE6EDF5),
    onSurfaceVariant = SampColors.GrigioTesto,
    error = SampColors.Rosso,
)

private val DarkColors = darkColorScheme(
    primary = SampColors.BluChiaro,
    onPrimary = SampColors.Bianco,
    primaryContainer = SampColors.BluScuro,
    onPrimaryContainer = SampColors.Bianco,
    secondary = SampColors.Rosso,
    onSecondary = SampColors.Bianco,
    background = Color(0xFF0E1621),
    onBackground = SampColors.Bianco,
    surface = Color(0xFF16222F),
    onSurface = SampColors.Bianco,
    surfaceVariant = Color(0xFF223140),
    onSurfaceVariant = Color(0xFFB8C4D0),
    error = SampColors.Rosso,
)

@Composable
fun SampdoriaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
