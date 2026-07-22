package com.scoutingsampdoria.persone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Palette redesign ispirata ai colori U.C. Sampdoria ma modernizzata
 * per accessibilità (WCAG AA) e gerarchia visiva.
 *
 * Filosofia: blucerchiato dominante, rosso come accento (badge urgenti,
 * CTA distruttivi), scala di grigi tendente al blu per legare al brand.
 */
object SampColors {
    // --- Brand primari ---
    val Blu = Color(0xFF003D7A)              // Blu Sampdoria (primary)
    val BluScuro = Color(0xFF00285A)         // Blu Marino (deep, header PDF)
    val BluChiaro = Color(0xFF4A90D9)        // Blu Sky (focus, link)
    val BluNebbia = Color(0xFFEAF2FC)        // Blu Nebbia (wash, card selezionate)

    // --- Accento (rosso Genova) ---
    val Rosso = Color(0xFFD8232A)            // Rosso Genova (accento)
    val RossoSoft = Color(0xFFFCE7E8)        // Rosa cipria (sfondo alert)

    // --- Funzionali (semantici) ---
    val Success = Color(0xFF2E7D5B)          // Verde Prato
    val Warning = Color(0xFFE8A317)          // Ambra
    val ErrorColor = Color(0xFFC0392B)       // Rosso Cartellino
    val Info = Color(0xFF3D8FB5)             // Blu Ghiaccio

    // --- Neutri ---
    val Nero = Color(0xFF1A2332)             // Text primary (grigio-blu)
    val TestoSecondario = Color(0xFF5C6672)  // Text secondary
    val TestoMuto = Color(0xFF8A94A0)        // Text muted / placeholder
    val Divisore = Color(0xFFE1E5EB)         // Divider hairline
    val SfondoChiaro = Color(0xFFF7F8FA)     // Background pagina
    val Superficie = Color(0xFFFFFFFF)       // Surface (card)
    val SuperficieAlt = Color(0xFFF0F2F5)    // Surface alternate (input, righe zebra)
    val Bianco = Color(0xFFFFFFFF)

    // --- Compatibilità con codice esistente ---
    val CardChiaro = Superficie
    val GrigioTesto = TestoSecondario
}

private val LightColors = lightColorScheme(
    primary = SampColors.Blu,
    onPrimary = SampColors.Bianco,
    primaryContainer = SampColors.BluNebbia,
    onPrimaryContainer = SampColors.Blu,
    secondary = SampColors.Rosso,
    onSecondary = SampColors.Bianco,
    secondaryContainer = SampColors.RossoSoft,
    onSecondaryContainer = SampColors.Rosso,
    tertiary = SampColors.Info,
    onTertiary = SampColors.Bianco,
    background = SampColors.SfondoChiaro,
    onBackground = SampColors.Nero,
    surface = SampColors.Superficie,
    onSurface = SampColors.Nero,
    surfaceVariant = SampColors.SuperficieAlt,
    onSurfaceVariant = SampColors.TestoSecondario,
    outline = SampColors.Divisore,
    outlineVariant = SampColors.Divisore,
    error = SampColors.ErrorColor,
    onError = SampColors.Bianco,
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
    error = SampColors.ErrorColor,
)

/**
 * Scala tipografica sportiva ma leggibile.
 * - Display / Title: Roboto Condensed Bold (tono sportivo)
 * - Body: Roboto Regular
 * - Fallback: system default (SansSerif) se il font condensed non è disponibile
 */
private val Typography = Typography(
    // Titolo principale (TopBar, hero header)
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.sp,
    ),
    // Titoli di sezione
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
    ),
    // Card title (nome giocatore)
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
    ),
    // Corpo
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    // Label (badge, chip)
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
    ),
)

@Composable
fun SampdoriaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
