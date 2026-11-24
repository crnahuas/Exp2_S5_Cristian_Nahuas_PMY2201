package cl.duoc.veterinaria.utilidades

import java.time.format.DateTimeParseException

fun mensajeError(error: Exception): String = when (error) {
    is DateTimeParseException -> "Fecha inválida. Usa una fecha existente en formato DD/MM/AAAA."
    is ArithmeticException -> "El importe supera el límite permitido."
    is IllegalArgumentException -> error.message ?: "Revisa los datos ingresados."
    else -> "No pudimos completar la operación. Intenta nuevamente."
}
