package cl.duoc.veterinaria.utilidades

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UtilidadesTest {
    @Test fun `telefono admite formato chileno y rechaza letras`() {
        assertEquals("+56912345678", validarTelefono("+56 9 1234 5678"))
        assertEquals("912345678", validarTelefono("9 1234-5678"))
        assertThrows<IllegalArgumentException> { validarTelefono("abc12345") }
        assertThrows<IllegalArgumentException> { validarTelefono("123") }
    }
    @Test fun `correo rechaza entradas incompletas`() {
        assertEquals("ana@ejemplo.cl", validarCorreo(" ana@ejemplo.cl "))
        listOf("ana@", "ana..p@ejemplo.cl", "@ejemplo.cl").forEach { value -> assertThrows<IllegalArgumentException> { validarCorreo(value) } }
    }
    @Test fun `fecha respeta calendario y formato`() {
        assertEquals(LocalDate.of(2028, 2, 29), interpretarFecha("29/02/2028"))
        assertThrows<DateTimeParseException> { interpretarFecha("29/02/2026") }
        assertThrows<IllegalArgumentException> { interpretarFecha("1/2/2026") }
        assertEquals("23/11/2026", formatearFecha(LocalDate.of(2026, 11, 23)))
    }
    @Test fun `edad admite crias y rechaza valores imposibles`() {
        assertEquals(0, validarEdad("0"))
        listOf("-1", "41", "abc").forEach { value -> assertThrows<IllegalArgumentException> { validarEdad(value) } }
        assertThrows<IllegalArgumentException> { textoObligatorio("  ", "el nombre") }
    }
    @Test fun `errores informan sin detalles internos`() {
        assertTrue(mensajeError(DateTimeParseException("interno", "", 0)).contains("Fecha inválida"))
        assertEquals("Ingresa el nombre", mensajeError(IllegalArgumentException("Ingresa el nombre")))
        assertFalse(mensajeError(IllegalStateException("interno")).contains("interno"))
    }
    @Test fun `precio muestra moneda chilena`() {
        assertEquals("$40.000", precio(BigDecimal("40000.00")))
        assertEquals("$1.500,50", precio(BigDecimal("1500.50")))
    }
}
