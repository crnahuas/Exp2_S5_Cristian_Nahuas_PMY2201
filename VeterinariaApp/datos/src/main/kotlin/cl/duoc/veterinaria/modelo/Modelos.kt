package cl.duoc.veterinaria.modelo

import cl.duoc.veterinaria.utilidades.textoObligatorio
import cl.duoc.veterinaria.utilidades.validarCorreo
import cl.duoc.veterinaria.utilidades.validarTelefono
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/** El cliente es el dueño responsable de una o más mascotas. */
class Cliente(val id: Int, nombre: String, telefono: String, correo: String) {
    val nombre = textoObligatorio(nombre, "el nombre del dueño")
    val telefono = validarTelefono(telefono)
    val correo = validarCorreo(correo)
    init { require(id > 0) { "El identificador del cliente debe ser positivo." } }
}

enum class Especie(val descripcion: String) { PERRO("Perro"), GATO("Gato"), CONEJO("Conejo"), OTRO("Otro") }

class Mascota(val id: Int, val clienteId: Int, nombre: String, val especie: Especie, raza: String, val edad: Int) {
    val nombre = textoObligatorio(nombre, "el nombre de la mascota")
    val raza = raza.trim().ifEmpty { "Sin especificar" }
    init {
        require(id > 0 && clienteId > 0) { "La mascota debe tener un dueño registrado." }
        require(edad in 0..40) { "Ingresa una edad entre 0 y 40 años." }
        require(this.raza.length <= 80) { "La raza admite hasta 80 caracteres." }
    }
}

/** Producto representa un servicio del catálogo de la clínica. */
data class Producto(val id: Int, val nombre: String, val descripcion: String, val precio: BigDecimal) {
    init {
        require(id > 0 && nombre.isNotBlank()) { "El servicio debe tener un identificador y un nombre." }
        require(precio >= BigDecimal.ZERO && precio.scale() <= 2) { "El precio debe ser positivo o cero y tener hasta dos decimales." }
    }
}

enum class EstadoConsulta(val descripcion: String, val accion: String?) {
    PROGRAMADA("Programada", "Iniciar atención"),
    EN_ATENCION("En atención", "Finalizar consulta"),
    FINALIZADA("Finalizada", null)
}

/** Un pedido de atención es una consulta para una mascota y su dueño. */
class Pedido(
    val id: Int, val mascotaId: Int, val clienteId: Int, val fecha: LocalDate,
    motivo: String, servicios: List<Producto>
) {
    val motivo = textoObligatorio(motivo, "el motivo de la consulta", 240)
    val servicios = servicios.toList()
    val total: BigDecimal = this.servicios.fold(BigDecimal.ZERO) { suma, servicio -> suma + servicio.precio }
        .setScale(2, RoundingMode.HALF_UP)
    var estado = EstadoConsulta.PROGRAMADA
        private set
    init {
        require(id > 0 && mascotaId > 0 && clienteId > 0) { "La consulta debe tener una mascota y un dueño registrados." }
        require(this.servicios.isNotEmpty()) { "Selecciona al menos un servicio." }
    }
    internal fun avanzar() {
        estado = when (estado) {
            EstadoConsulta.PROGRAMADA -> EstadoConsulta.EN_ATENCION
            EstadoConsulta.EN_ATENCION -> EstadoConsulta.FINALIZADA
            EstadoConsulta.FINALIZADA -> throw IllegalArgumentException("La consulta ya está finalizada.")
        }
    }
}

data class ResumenClinica(
    val totalClientes: Int = 0, val totalPedidos: Int = 0,
    val ultimoCliente: String? = null, val totalMascotas: Int = 0
)
