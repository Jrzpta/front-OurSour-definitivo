package cl.oursour.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Paleta de marca: coctelería premium, fondo verde oscuro con acentos cítricos.
val Ink = Color(0xFF0E1512)
val Pine = Color(0xFF17211C)
val PineLight = Color(0xFF213029)
val Lime = Color(0xFFC6F24E)
val Amber = Color(0xFFF4B942)
val Coral = Color(0xFFFF7A59)
val Cream = Color(0xFFF3EFE4)
val Mist = Color(0xFFB8C4BC)
val LimeDeep = Color(0xFF33441A)
val LimeSoft = Color(0xFFDDF7A0)

private val OurSourColors = darkColorScheme(
    primary = Lime,
    onPrimary = Ink,
    primaryContainer = LimeDeep,
    onPrimaryContainer = LimeSoft,
    secondary = Amber,
    onSecondary = Color(0xFF1A1200),
    secondaryContainer = LimeDeep,
    onSecondaryContainer = LimeSoft,
    tertiary = Coral,
    onTertiary = Ink,
    background = Ink,
    onBackground = Cream,
    surface = Pine,
    onSurface = Cream,
    surfaceVariant = PineLight,
    onSurfaceVariant = Mist,
    surfaceContainerLowest = Ink,
    surfaceContainerLow = Pine,
    surfaceContainer = Pine,
    surfaceContainerHigh = PineLight,
    surfaceContainerHighest = PineLight,
    outline = Color(0xFF4A5C52),
    outlineVariant = Color(0xFF2E3D35),
    error = Color(0xFFFF6B6B),
    onError = Ink
)

private val base = Typography()

// Títulos con tipografía serif para dar un aire "premium"; el resto usa la fuente por defecto.
private val OurSourTypography = Typography(
    headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold)
)

private val OurSourShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun OurSourTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OurSourColors,
        typography = OurSourTypography,
        shapes = OurSourShapes,
        content = content
    )
}
