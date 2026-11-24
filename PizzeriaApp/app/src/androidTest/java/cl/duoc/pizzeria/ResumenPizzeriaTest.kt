package cl.duoc.pizzeria

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResumenPizzeriaTest {
    @get:Rule val app = createAndroidComposeRule<MainActivity>()

    @Test fun cargaInicialDaPasoAlResumen() {
        app.onNodeWithTag("cargando").assertIsDisplayed()
        capturar("00_carga.png")
        app.waitUntil(15_000) { app.onAllNodesWithTag("inicio").fetchSemanticsNodes().isNotEmpty() }
        resumen("0", "0", "Sin clientes registrados")
    }

    @Test fun resumenSeActualizaSinDuplicarClientesYSeConservaAlRotar() {
        app.waitUntil(15_000) { app.onAllNodesWithTag("inicio").fetchSemanticsNodes().isNotEmpty() }
        resumen("0", "0", "Sin clientes registrados")
        capturar("02_resumen_inicial.png")
        app.onNodeWithText("Carta").performClick()
        agregarPizza()
        entregar("Ana", "ana@ejemplo.cl")
        app.onNodeWithText("Seguir editando").performClick()
        app.onNodeWithContentDescription("Volver a la carta").performClick()
        app.onNodeWithText("Inicio").performClick()
        resumen("1", "0", "Ana")
        capturar("03_cliente_registrado.png")

        // El borrador conserva la selección y los datos tras cancelarlo.
        app.onNodeWithText("Carta").performClick()
        app.onNodeWithText("Ver mi pedido").performClick()
        revisar()
        app.onNodeWithText("Confirmar pedido").performClick()
        app.onNodeWithText("Inicio").performClick()
        resumen("1", "1", "Ana")

        nuevoPedido("Bruno", "bruno@ejemplo.cl")
        resumen("2", "2", "Bruno")
        nuevoPedido("Ana", "ana@ejemplo.cl")
        resumen("2", "3", "Bruno")
        capturar("04_resumen_actualizado.png")
        app.onNodeWithTag("inicio").performScrollToNode(hasText("bruno@ejemplo.cl"))
        app.onNodeWithText("bruno@ejemplo.cl").assertIsDisplayed()
        capturar("05_clientes.png")
        app.onNodeWithText("Pedidos").performClick()
        app.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("PEDIDO #3 · 23/11/2026"))
        app.onNodeWithText("PEDIDO #3 · 23/11/2026").assertIsDisplayed()
        capturar("06_pedidos.png")
        app.onNodeWithText("Inicio").performClick()
        resumen("2", "3", "Bruno")
        capturar("07_antes_rotacion.png")
        try {
            app.runOnIdle { app.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            app.waitUntil(15_000) { app.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
            resumen("2", "3", "Bruno")
            // La configuración cambia antes de que termine la animación del sistema.
            android.os.SystemClock.sleep(1_500)
            capturar("08_despues_rotacion.png")
            app.activityRule.scenario.recreate()
            resumen("2", "3", "Bruno")
        } finally {
            app.runOnIdle { app.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
            app.waitUntil(15_000) { app.activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
        }
    }

    private fun nuevoPedido(nombre: String, correo: String) {
        app.onNodeWithText("Carta").performClick()
        agregarPizza()
        entregar(nombre, correo)
        app.onNodeWithText("Confirmar pedido").performClick()
        app.onNodeWithText("Inicio").performClick()
    }

    private fun agregarPizza() {
        app.onNode(hasScrollToIndexAction()).performScrollToIndex(3)
        app.onNodeWithContentDescription("Agregar Margarita").performScrollTo().performClick()
    }

    private fun entregar(nombre: String, correo: String) {
        app.onNodeWithText("Ver mi pedido").performClick()
        listOf("nombre" to nombre, "telefono" to "561234567890", "correo" to correo, "direccion" to "Calle 123", "fecha" to "23/11/2026").forEach { (campo, valor) ->
            app.onNodeWithTag("campo_$campo").performScrollTo().performTextReplacement(valor)
        }
        closeSoftKeyboard()
        revisar()
    }

    private fun revisar() { app.onNodeWithText("Revisar pedido").performScrollTo().performClick() }

    private fun resumen(clientes: String, pedidos: String, ultimo: String) {
        app.onNodeWithTag("inicio").performScrollToIndex(1)
        app.onNodeWithTag("total_clientes").performScrollTo().assertTextEquals(clientes)
        app.onNodeWithTag("total_pedidos").assertTextEquals(pedidos)
        app.onNodeWithTag("ultimo_cliente").performScrollTo().assertTextEquals(ultimo)
    }

    private fun capturar(nombre: String) {
        app.waitForIdle()
        val inst = InstrumentationRegistry.getInstrumentation()
        val carpeta = File(inst.targetContext.getExternalFilesDir(null), "evidencias").apply { mkdirs() }
        val archivo = File(carpeta, nombre)
        val bitmap = requireNotNull(inst.uiAutomation.takeScreenshot())
        archivo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        listOf("mkdir -p /sdcard/Download/PizzeriaSemana5", "cp ${archivo.absolutePath} /sdcard/Download/PizzeriaSemana5/$nombre").forEach {
            ParcelFileDescriptor.AutoCloseInputStream(inst.uiAutomation.executeShellCommand(it)).use { stream -> stream.readBytes() }
        }
    }
}
