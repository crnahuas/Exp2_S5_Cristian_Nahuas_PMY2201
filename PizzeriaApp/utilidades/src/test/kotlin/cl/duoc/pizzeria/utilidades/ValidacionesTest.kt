package cl.duoc.pizzeria.utilidades

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.format.DateTimeParseException

class ValidacionesTest {
    @Test fun `telefono normaliza formatos equivalentes`() {
        assertEquals("+56 (123) 456-7890", validarTelefono("561234567890"))
        assertEquals(validarTelefono("+561234567890"), validarTelefono("+56 (123) 456-7890"))
        assertThrows<IllegalArgumentException> { validarTelefono("12345") }
    }

    @Test fun `correo rechaza dominios incompletos y puntos consecutivos`() {
        assertEquals("ana@ejemplo.cl", validarCorreo(" ana@ejemplo.cl "))
        listOf("ana@", "ana..p@ejemplo.cl", "@ejemplo.cl").forEach {
            assertThrows<IllegalArgumentException> { validarCorreo(it) }
        }
    }

    @Test fun `errores muestran orientaciones sin exponer detalles internos`() {
        assertTrue(mensajeError(DateTimeParseException("interno", "30/02/2026", 0)).contains("Fecha inválida"))
        assertTrue(mensajeError(ArithmeticException()).contains("inventario"))
        assertEquals("Ingresa tu nombre", mensajeError(IllegalArgumentException("Ingresa tu nombre")))
        assertFalse(mensajeError(IllegalStateException("detalle interno")).contains("detalle interno"))
    }

    @Test fun `fecha distingue anos bisiestos y exige formato completo`() {
        assertEquals(LocalDate.of(2028, 2, 29), interpretarFecha("29/02/2028"))
        assertThrows<DateTimeParseException> { interpretarFecha("29/02/2026") }
        assertThrows<IllegalArgumentException> { interpretarFecha("1/2/2026") }
        assertEquals("23/11/2026", formatearFecha(LocalDate.of(2026, 11, 23)))
    }
}
