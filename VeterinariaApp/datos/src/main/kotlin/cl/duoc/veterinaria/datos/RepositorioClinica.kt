package cl.duoc.veterinaria.datos

import cl.duoc.veterinaria.modelo.*
import cl.duoc.veterinaria.utilidades.*
import java.math.BigDecimal
import java.util.Locale
import kotlinx.coroutines.delay

class RepositorioClinica {
    private val clientes = linkedMapOf<Int, Cliente>()
    private val mascotas = linkedMapOf<Int, Mascota>()
    private val pedidos = linkedMapOf<Int, Pedido>()
    private val catalogo = listOf(
        Producto(1, "Consulta general", "Evaluación veterinaria y revisión de tu mascota.", BigDecimal("18000.00")),
        Producto(2, "Vacunación", "Atención para el plan de vacunas de tu mascota.", BigDecimal("22000.00")),
        Producto(3, "Control preventivo", "Seguimiento del bienestar y control periódico.", BigDecimal("15000.00"))
    )

    // El catálogo es local; se simula una carga sin bloquear la interfaz.
    suspend fun cargarCatalogo(): List<Producto> { delay(900); return catalogo }
    fun clientes(): List<Cliente> = clientes.values.toList()
    fun mascotas(): List<Mascota> = mascotas.values.toList()
    fun pedidos(): List<Pedido> = pedidos.values.toList()
    fun resumen() = ResumenClinica(clientes.size, pedidos.size, clientes.values.lastOrNull()?.nombre, mascotas.size)

    fun registrarCliente(nombre: String, telefono: String, correo: String): Cliente {
        val nuevo = Cliente(clientes.size + 1, nombre, telefono, correo)
        val existente = clientes.values.find { it.correo.lowercase(Locale.ROOT) == nuevo.correo.lowercase(Locale.ROOT) }
        if (existente != null) {
            require(existente.nombre.equals(nuevo.nombre, ignoreCase = true) && existente.telefono == nuevo.telefono) {
                "Ya existe un dueño con ese correo y otros datos de contacto."
            }
            return existente
        }
        clientes[nuevo.id] = nuevo
        return nuevo
    }

    fun registrarMascota(clienteId: Int, nombre: String, especie: Especie, raza: String, edad: String): Mascota {
        require(clientes.containsKey(clienteId)) { "Selecciona un dueño registrado." }
        val nueva = Mascota(mascotas.size + 1, clienteId, nombre, especie, raza, validarEdad(edad))
        require(mascotas.values.none { it.clienteId == clienteId && it.nombre.equals(nueva.nombre, true) && it.especie == especie }) {
            "Este dueño ya tiene una mascota registrada con ese nombre y especie."
        }
        mascotas[nueva.id] = nueva
        return nueva
    }

    private fun servicios(ids: List<Int>): List<Producto> {
        require(ids.isNotEmpty()) { "Selecciona al menos un servicio." }
        require(ids.all { id -> catalogo.any { it.id == id } }) { "Selecciona servicios disponibles en el catálogo." }
        return catalogo.filter { it.id in ids }
    }

    fun cotizar(ids: List<Int>): BigDecimal = if (ids.isEmpty()) BigDecimal("0.00") else
        servicios(ids).fold(BigDecimal.ZERO) { suma, producto -> suma + producto.precio }

    fun registrarConsulta(mascotaId: Int, fecha: String, motivo: String, servicios: List<Int>): Pedido {
        val mascota = requireNotNull(mascotas[mascotaId]) { "Selecciona una mascota registrada." }
        val nuevo = Pedido(pedidos.size + 1, mascota.id, mascota.clienteId,
            interpretarFecha(fecha.trim()), motivo, servicios(servicios))
        pedidos[nuevo.id] = nuevo
        return nuevo
    }

    fun avanzarConsulta(id: Int) {
        requireNotNull(pedidos[id]) { "La consulta no existe." }.avanzar()
    }
}
