package cl.duoc.pizzeria

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlujoPizzeriaTest {
    @get:Rule val app = createAndroidComposeRule<MainActivity>()

    @Before fun abrirCarta() {
        app.waitUntil(15_000) { app.onAllNodesWithTag("inicio").fetchSemanticsNodes().isNotEmpty() }
        app.onNodeWithText("Carta").performClick()
    }

    @Test
    fun pedidoCompletoConCancelacionInventarioYEntrega() {
        app.onNodeWithText("Bienvenidos a la mejor pizzería de la ciudad").assertIsDisplayed()
        capturar("03_app_bienvenida.png")
        agregar(0, "Margarita", 2)
        agregar(1, "Pepperoni", 1)
        agregar(2, "Vegetariana", 1)
        app.onNodeWithText("Ver mi pedido").performClick()
        completarDatos()
        app.onNodeWithTag("campo_nombre").performScrollTo()
        capturar("04_datos_entrega.png")
        revisar()
        app.onNodeWithTag("total_confirmacion").performScrollTo().assertTextContains("36.146,25", substring = true)
        capturar("05_resumen_pedido.png")

        // Revisar y cancelar nunca consume inventario.
        app.onNodeWithText("Seguir editando").performClick()
        app.onNodeWithContentDescription("Volver a la carta").performClick()
        app.onNodeWithText("Cocina").performClick()
        app.onNodeWithTag("stock_masa").assertTextEquals("20 porciones")

        app.onNodeWithText("Carta").performClick()
        app.onNodeWithText("Ver mi pedido").performClick()
        revisar()
        app.onNodeWithText("Confirmar pedido").performClick()
        app.onNodeWithText("En preparación").assertIsDisplayed()
        app.onNodeWithText("2 × Margarita · 1 × Pepperoni · 1 × Vegetariana").assertIsDisplayed()
        capturar("06_pedido_confirmado.png")

        app.onNodeWithText("Cocina").performClick()
        app.onNodeWithTag("stock_masa").assertTextEquals("16 porciones")
        capturar("07_cocina.png")
        app.onNodeWithContentDescription("Reponer masa").performClick()
        app.onNodeWithTag("stock_masa").assertTextEquals("26 porciones")
        app.onNodeWithText("Pedidos").performClick()
        app.onNodeWithText("Marcar listo").performScrollTo().performClick()
        app.onNodeWithText("Asignar repartidor").performScrollTo().performClick()
        app.onNodeWithText("En camino").assertIsDisplayed()
        app.onNodeWithText("Repartidor: Diego").assertIsDisplayed()
        app.onNodeWithText("Confirmar entrega").performScrollTo().performClick()
        app.onNodeWithText("Entregado").assertIsDisplayed()
        app.onNodeWithText("Pedido #1: Entregado.").assertIsDisplayed()
        app.onNodeWithText("Carta").performClick()
        app.onNodeWithText("Pedido #1: Entregado.").assertDoesNotExist()
        app.onNodeWithText("Pedidos").performClick()
        app.onNodeWithText("Pedido #1: Entregado.").assertDoesNotExist()
        app.activityRule.scenario.recreate()
        app.onNodeWithText("Entregado").assertIsDisplayed()
        app.onNodeWithText("Pedido #1: Entregado.").assertDoesNotExist()
        capturar("08_pedido_entregado.png")
    }

    @Test
    fun avisoDeReposicionSeCierraAlNavegarYUnaNuevaAccionLoMuestraOtraVez() {
        val aviso = "Se agregaron 10 porciones de masa."
        app.onNodeWithText("Cocina").performClick()
        app.onNodeWithContentDescription("Reponer masa").performClick()
        app.onNodeWithText(aviso).assertIsDisplayed()
        app.onNodeWithText("Pedidos").performClick()
        app.onNodeWithText(aviso).assertDoesNotExist()
        app.onNodeWithText("Cocina").performClick()
        app.onNodeWithText(aviso).assertDoesNotExist()
        app.onNodeWithContentDescription("Reponer masa").performClick()
        app.onNodeWithText(aviso).assertIsDisplayed()
        app.onNodeWithTag("stock_masa").assertTextEquals("40 porciones")
    }

    @Test
    fun datosInvalidosNoConfirmanPedidoYSePuedenCorregir() {
        agregar(0, "Margarita", 1)
        app.onNodeWithText("Ver mi pedido").performClick()
        revisar()
        app.onNodeWithTag("error_formulario").assertTextContains("nombre", substring = true)
        completarDatos()
        app.onNodeWithTag("campo_fecha").performScrollTo().performTextReplacement("30/02/2026")
        closeSoftKeyboard()
        revisar()
        app.onNodeWithTag("error_formulario").assertTextContains("Fecha inválida", substring = true)
        app.onNodeWithTag("campo_fecha").performScrollTo().performTextReplacement("09/11/2026")
        closeSoftKeyboard()
        revisar()
        app.onNodeWithTag("total_confirmacion").assertTextContains("9.609,25", substring = true)
    }

    private fun agregar(indice: Int, pizza: String, cantidad: Int) {
        app.onNode(hasScrollToIndexAction()).performScrollToIndex(indice + 3)
        repeat(cantidad) {
            app.onNodeWithContentDescription("Agregar $pizza").performScrollTo().performClick()
        }
    }

    private fun completarDatos() {
        listOf("nombre" to "Ana", "telefono" to "561234567890", "correo" to "ana@ejemplo.cl", "direccion" to "Calle 123", "fecha" to "09/11/2026").forEach { (campo, valor) ->
            app.onNodeWithTag("campo_$campo").performScrollTo().performTextReplacement(valor)
        }
        closeSoftKeyboard()
    }

    private fun revisar() {
        app.onNodeWithText("Revisar pedido").performScrollTo().performClick()
    }

    private fun capturar(nombre: String) {
        app.waitForIdle()
        val instrumentacion = InstrumentationRegistry.getInstrumentation()
        val destino = File(instrumentacion.targetContext.getExternalFilesDir(null), "evidencias").apply { mkdirs() }
        val bitmap = requireNotNull(instrumentacion.uiAutomation.takeScreenshot())
        val archivo = File(destino, nombre)
        archivo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        // Conserva las capturas aunque el runner desinstale la app al terminar.
        listOf("mkdir -p /sdcard/Download/PizzeriaSemana5", "cp ${archivo.absolutePath} /sdcard/Download/PizzeriaSemana5/$nombre").forEach { comando ->
            val descriptor = instrumentacion.uiAutomation.executeShellCommand(comando)
            ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
        }
    }
}
