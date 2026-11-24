package cl.duoc.veterinaria.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val VerdeClinica = Color(0xFF15594E)
val FondoClaro = Color(0xFFF5F9F7)
private val Claro = lightColorScheme(
    primary = Color(0xFF006B5C), onPrimary = Color.White,
    primaryContainer = Color(0xFFD4EEE4), onPrimaryContainer = Color(0xFF114D40),
    secondary = Color(0xFF426482), secondaryContainer = Color(0xFFDFEAF5), onSecondaryContainer = Color(0xFF24435F),
    background = FondoClaro, onBackground = Color(0xFF203A34),
    surface = Color.White, onSurface = Color(0xFF203A34),
    surfaceVariant = Color(0xFFE9F1EE), onSurfaceVariant = Color(0xFF52675F),
    outlineVariant = Color(0xFFD8E5DE)
)
private val Oscuro = darkColorScheme(
    primary = Color(0xFF8CD6BF), onPrimary = Color(0xFF00382C),
    primaryContainer = Color(0xFF185342), onPrimaryContainer = Color(0xFFB2EDD7),
    secondary = Color(0xFFA8C9E8), secondaryContainer = Color(0xFF294762), onSecondaryContainer = Color(0xFFDAE9F8),
    background = Color(0xFF10211C), onBackground = Color(0xFFE0EDE5),
    surface = Color(0xFF192D25), onSurface = Color(0xFFE0EDE5),
    surfaceVariant = Color(0xFF263E34), onSurfaceVariant = Color(0xFFB5C9BE),
    outlineVariant = Color(0xFF3C5549)
)
private val Tipografia = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp)
)
@Composable fun VeterinariaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Oscuro else Claro, typography = Tipografia, content = content)
}
