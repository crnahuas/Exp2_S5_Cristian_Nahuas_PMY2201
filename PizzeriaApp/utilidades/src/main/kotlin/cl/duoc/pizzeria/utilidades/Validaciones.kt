package cl.duoc.pizzeria.utilidades

private val PATRON_CORREO = Regex("[A-Za-z0-9_+%-]+(?:\\.[A-Za-z0-9_+%-]+)*@[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*(?:\\.[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*)*\\.[A-Za-z]{2,}")
private val TELEFONO_DIGITOS = Regex("\\+?([0-9]{2})([0-9]{3})([0-9]{3})([0-9]{4})")
private val TELEFONO_FORMATEADO = Regex("\\+([0-9]{2}) \\(([0-9]{3})\\) ([0-9]{3})-([0-9]{4})")

fun validarCorreo(entrada: String): String {
    val correo = entrada.trim()
    require(correo.length <= 254 && PATRON_CORREO.matches(correo)) {
        "Correo inválido. Usa el formato nombre@dominio.com."
    }
    return correo
}

fun formatearTelefono(entrada: String): String {
    val telefono = entrada.trim()
    val grupos = TELEFONO_DIGITOS.matchEntire(telefono)?.groupValues
        ?: TELEFONO_FORMATEADO.matchEntire(telefono)?.groupValues
    require(grupos != null) {
        "Teléfono inválido. Ingresa 12 dígitos (2 de país y 10 nacionales) o +XX (XXX) XXX-XXXX."
    }
    return "+${grupos[1]} (${grupos[2]}) ${grupos[3]}-${grupos[4]}"
}

fun validarTelefono(entrada: String): String = formatearTelefono(entrada)

