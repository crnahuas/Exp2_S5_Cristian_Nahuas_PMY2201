package cl.duoc.veterinaria

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
class ClinicaFlujoTest {
    @get:Rule val app = createAndroidComposeRule<MainActivity>()

    @Test fun cargaInicialTerminaConResumenVacio() {
        // La espera es breve: la captura se toma solo si el estado aún está visible.
        if (runCatching { app.onNodeWithTag("cargando").isDisplayed() }.getOrDefault(false)) {
            capturar("00_carga.png")
        }
        esperarInicio()
        resumen("0", "0", "Sin clientes registrados")
        capturar("02_resumen_inicial.png")
    }

    @Test fun dueñosMascotasYConsultasSeActualizanYConservanAlRotar() {
        esperarInicio()
        nuevoDueno("Ana", "ana@ejemplo.cl")
        capturar("03_duenos.png")
        app.onNodeWithTag("nav_Inicio").performClick()
        resumen("1", "0", "Ana")
        nuevaMascota("Luna", 1, "Gato", "3")
        app.onNodeWithText("Dueño: Ana").assertIsDisplayed()
        capturar("04_mascotas.png")
        nuevaConsulta(1, "Control anual", true)
        app.onNodeWithTag("estado_1").assertTextEquals("Programada")
        capturar("05_consulta_registrada.png")
        nuevoDueno("Bruno", "bruno@ejemplo.cl")
        nuevaMascota("Toby", 2, "Perro", "5")
        nuevaConsulta(2, "Revisión general", false)
        app.onNodeWithTag("lista_consultas").performScrollToIndex(0)
        capturar("06_consultas.png")
        app.onNodeWithTag("lista_consultas").performScrollToNode(hasTestTag("consulta_1"))
        app.onNodeWithTag("avanzar_1").performScrollTo().performClick()
        app.onNodeWithTag("estado_1").assertTextEquals("En atención")
        app.onNodeWithTag("avanzar_1").performClick()
        app.onNodeWithTag("estado_1").assertTextEquals("Finalizada")
        capturar("07_consulta_finalizada.png")
        app.onNodeWithTag("nav_Inicio").performClick()
        resumen("2", "2", "Bruno")
        app.onNodeWithTag("total_mascotas").assertTextEquals("2 mascotas registradas")
        capturar("08_resumen_actualizado.png")
        capturar("09_antes_rotacion.png")
        try {
            app.runOnIdle { app.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            app.waitUntil(15_000) { app.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
            resumen("2", "2", "Bruno")
            android.os.SystemClock.sleep(1_500)
            capturar("10_despues_rotacion.png")
            app.activityRule.scenario.recreate()
            resumen("2", "2", "Bruno")
        } finally {
            app.runOnIdle { app.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
            app.waitUntil(15_000) { app.activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
        }
    }

    @Test fun formulariosInvalidosNoRegistranDatos() {
        esperarInicio()
        app.onNodeWithTag("nav_Mascotas").performClick()
        app.onNodeWithText("Registrar mascota").assertIsNotEnabled()
        app.onNodeWithTag("nav_Dueños").performClick()
        app.onNodeWithText("Registrar dueño").performClick()
        guardar("dueno")
        app.onNodeWithTag("error_formulario").assertTextContains("nombre", substring = true)
        campo("nombre_dueno", "Ana")
        campo("telefono", "+56912345678")
        campo("correo", "ana@")
        closeSoftKeyboard()
        guardar("dueno")
        app.onNodeWithTag("error_formulario").assertTextContains("correo", substring = true)
        app.onNodeWithTag("volver").performClick()
        app.onNodeWithTag("nav_Inicio").performClick()
        resumen("0", "0", "Sin clientes registrados")
    }

    @Test fun cancelarConsultaNoIncrementaElResumen() {
        esperarInicio()
        nuevoDueno("Ana", "ana@ejemplo.cl")
        nuevaMascota("Luna", 1, "Gato", "3")
        app.onNodeWithTag("agendar_1").performClick()
        campo("motivo", "Control anual")
        closeSoftKeyboard()
        app.onNodeWithTag("volver").performClick()
        app.onNodeWithTag("nav_Inicio").performClick()
        resumen("1", "0", "Ana")
    }

    private fun esperarInicio() { app.waitUntil(15_000) { app.onAllNodesWithTag("inicio").fetchSemanticsNodes().isNotEmpty() } }
    private fun nuevoDueno(nombre: String, correo: String) {
        app.onNodeWithTag("nav_Dueños").performClick()
        app.onNodeWithText("Registrar dueño").performClick()
        campo("nombre_dueno", nombre)
        campo("telefono", "+56912345678")
        campo("correo", correo)
        closeSoftKeyboard()
        guardar("dueno")
        app.onNodeWithTag("lista_duenos").assertExists()
    }
    private fun nuevaMascota(nombre: String, dueno: Int, especie: String, edad: String) {
        app.onNodeWithTag("nav_Mascotas").performClick()
        app.onNodeWithText("Registrar mascota").performClick()
        app.onNodeWithTag("dueno_selector").performClick()
        app.onNodeWithTag("dueno_selector_$dueno").performClick()
        campo("nombre_mascota", nombre)
        app.onNodeWithText(especie).performScrollTo().performClick()
        campo("raza", "Mestiza")
        campo("edad", edad)
        closeSoftKeyboard()
        guardar("mascota")
        app.onNodeWithTag("lista_mascotas").assertExists()
    }
    private fun nuevaConsulta(mascota: Int, motivo: String, vacuna: Boolean) {
        app.onNodeWithTag("nav_Consultas").performClick()
        app.onNodeWithText("Agendar consulta").performClick()
        app.onNodeWithTag("mascota_selector").performClick()
        app.onNodeWithTag("mascota_selector_$mascota").performClick()
        campo("fecha", "23/11/2026")
        campo("motivo", motivo)
        closeSoftKeyboard()
        if (vacuna) app.onNodeWithTag("servicio_2").performScrollTo().performClick()
        app.onNodeWithTag("total_consulta").performScrollTo().assertTextContains(if (vacuna) "40.000" else "18.000", substring = true)
        capturar("11_formulario_consulta.png")
        guardar("consulta")
        app.onNodeWithTag("lista_consultas").assertExists()
    }
    private fun campo(id: String, texto: String) { app.onNodeWithTag("campo_$id").performScrollTo().performTextReplacement(texto) }
    private fun guardar(tipo: String) { app.onNodeWithTag("guardar_$tipo").performScrollTo().performClick() }
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
        listOf("mkdir -p /sdcard/Download/VeterinariaSemana5", "cp ${archivo.absolutePath} /sdcard/Download/VeterinariaSemana5/$nombre").forEach {
            ParcelFileDescriptor.AutoCloseInputStream(inst.uiAutomation.executeShellCommand(it)).use { stream -> stream.readBytes() }
        }
    }
}
