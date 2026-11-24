package cl.duoc.pizzeria.utilidades

import java.time.format.DateTimeParseException

fun mensajeError(error: Exception): String = when (error) {
    is DateTimeParseException -> "Fecha inválida. Usa una fecha existente en formato DD/MM/AAAA."
    is ArithmeticException -> "La cantidad supera el límite del inventario."
    is IllegalArgumentException -> error.message ?: "Revisa los datos ingresados."
    else -> "No pudimos completar la operación. Intenta nuevamente."
}
