package cl.duoc.pizzeria.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.cos
import kotlin.math.sin

/** Ilustración vectorial sin imágenes descargadas ni dependencias adicionales. */
@Composable
fun PizzaIlustracion(variedad: Int, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val escala = size.minDimension / 200f
        scale(escala, escala, pivot = Offset.Zero) {
            drawCircle(Color(0x24000000), 91f, Offset(103f, 108f))
            drawCircle(Color(0xFFF3D6A4), 94f, Offset(100f, 100f))
            drawCircle(Color(0xFFC9863E), 88f, Offset(100f, 100f))
            drawCircle(Color(0xFFE9AD59), 83f, Offset(100f, 100f))
            drawCircle(Color(0xFFB84129), 73f, Offset(100f, 100f))
            drawCircle(Color(0xFFF8D477), 67f, Offset(100f, 100f))
            for (i in 0..16) {
                val angulo = i * 2f * Math.PI.toFloat() / 17f
                val p = Offset(100 + 79 * cos(angulo), 100 + 79 * sin(angulo))
                drawCircle(Color(0xFFC88741), if (i % 3 == 0) 3f else 1.8f, p)
            }
            val centros = listOf(Offset(76f, 61f), Offset(127f, 64f), Offset(144f, 110f), Offset(114f, 140f), Offset(65f, 125f), Offset(94f, 102f))
            centros.forEachIndexed { i, centro ->
                when (variedad) {
                    1 -> {
                        drawCircle(Color(0xFFAC3020), 14f, centro)
                        drawCircle(Color(0xFFCB4B2A), 11.5f, centro)
                        drawCircle(Color(0xFFF08C50), 2f, centro + Offset(-3f, -4f))
                        drawCircle(Color(0xFFF08C50), 1.5f, centro + Offset(4f, 5f))
                    }
                    2 -> rotate(i * 41f, centro) {
                        drawRoundRect(Color(0xFFBDA17A), centro + Offset(-3f, 0f), Size(6f, 17f), androidx.compose.ui.geometry.CornerRadius(2f))
                        drawArc(Color(0xFF76503A), 180f, 180f, true, centro + Offset(-14f, -10f), Size(28f, 24f))
                        drawArc(Color(0xFFE1CDA7), 180f, 180f, true, centro + Offset(-11f, -8f), Size(22f, 18f))
                    }
                    else -> {
                        drawCircle(Color(0xFFFFF4CB), 15f, centro)
                        drawCircle(Color(0xFFE6BE65), 3f, centro + Offset(-4f, 2f))
                    }
                }
            }
            // Hojas y especias aportan el acabado de una pizza ilustrada.
            listOf(Offset(66f, 88f), Offset(126f, 114f), Offset(111f, 47f)).forEachIndexed { i, centro ->
                rotate(i * 65f - 30f, centro) {
                    val hoja = Path().apply {
                        moveTo(centro.x, centro.y + 14f)
                        cubicTo(centro.x - 17f, centro.y, centro.x - 9f, centro.y - 13f, centro.x + 3f, centro.y - 13f)
                        cubicTo(centro.x + 15f, centro.y - 4f, centro.x + 11f, centro.y + 6f, centro.x, centro.y + 14f)
                    }
                    drawPath(hoja, Color(0xFF426E37))
                    drawLine(Color(0xFF8DA766), centro + Offset(1f, -8f), centro + Offset(0f, 10f), 1.3f)
                }
            }
        }
    }
}
