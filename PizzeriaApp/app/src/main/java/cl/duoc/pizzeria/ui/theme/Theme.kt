package cl.duoc.pizzeria.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
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

val Tomate = Color(0xFFAA3929)
val Albahaca = Color(0xFF263F32)
val Crema = Color(0xFFFFF8EC)
val Dorado = Color(0xFFE9B45E)

private val ColoresClaros = lightColorScheme(
    primary = Tomate,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFBE4DB),
    onPrimaryContainer = Color(0xFF702014),
    secondary = Albahaca,
    onSecondary = Crema,
    secondaryContainer = Color(0xFFE6EBD8),
    onSecondaryContainer = Color(0xFF283D25),
    background = Crema,
    onBackground = Color(0xFF30291F),
    surface = Color(0xFFFFFCF5),
    onSurface = Color(0xFF30291F),
    surfaceVariant = Color(0xFFF1E8D8),
    onSurfaceVariant = Color(0xFF716454),
    outline = Color(0xFF897A68),
    outlineVariant = Color(0xFFE5D9C8)
)

private val ColoresOscuros = darkColorScheme(
    primary = Color(0xFFFFB4A0),
    onPrimary = Color(0xFF622114),
    primaryContainer = Color(0xFF713023),
    onPrimaryContainer = Color(0xFFFFDACE),
    secondary = Color(0xFFB9CEAB),
    onSecondary = Color(0xFF24371D),
    secondaryContainer = Color(0xFF344B2C),
    onSecondaryContainer = Color(0xFFD7E9CA),
    background = Color(0xFF201E19),
    onBackground = Color(0xFFF2E8D9),
    surface = Color(0xFF2A2721),
    onSurface = Color(0xFFF2E8D9),
    surfaceVariant = Color(0xFF39342B),
    onSurfaceVariant = Color(0xFFD4C6B4),
    outlineVariant = Color(0xFF51483C)
)

private val Tipografia = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 25.sp, lineHeight = 30.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 26.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.2.sp)
)

@Composable
fun PizzeriaAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) ColoresOscuros else ColoresClaros,
        typography = Tipografia,
        content = content
    )
}
