package cl.duoc.veterinaria.utilidades

fun textoObligatorio(valor: String, campo: String, maximo: Int = 80): String {
    val texto = valor.trim()
    require(texto.isNotEmpty()) { "Ingresa $campo." }
    require(texto.length <= maximo) { "$campo admite hasta $maximo caracteres." }
    return texto
}

fun validarCorreo(valor: String): String {
    val correo = valor.trim()
    require(correo.length <= 254 && Regex("[A-Za-z0-9_+%-]+(?:\\.[A-Za-z0-9_+%-]+)*@[A-Za-z0-9]+(?:[-.][A-Za-z0-9]+)*\\.[A-Za-z]{2,}").matches(correo)) {
        "Ingresa un correo válido, como ana@ejemplo.cl."
    }
    return correo
}

fun validarTelefono(valor: String): String {
    val telefono = valor.trim().replace(Regex("[ ()-]"), "")
    require(Regex("\\+?[0-9]{7,15}").matches(telefono)) {
        "Ingresa un teléfono de 7 a 15 dígitos, por ejemplo +56 9 1234 5678."
    }
    return telefono
}

fun validarEdad(valor: String): Int {
    val edad = valor.trim().toIntOrNull()
    require(edad != null && edad in 0..40) { "Ingresa una edad entre 0 y 40 años." }
    return edad
}
