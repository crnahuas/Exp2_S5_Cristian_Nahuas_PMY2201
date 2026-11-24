package cl.duoc.pizzeria.datos

import cl.duoc.pizzeria.modelo.*
import cl.duoc.pizzeria.negocio.crearPizzeria
import cl.duoc.pizzeria.utilidades.interpretarFecha
import cl.duoc.pizzeria.utilidades.fechaActual
import kotlinx.coroutines.delay

data class ResumenPizzeria(
    val totalClientes: Int = 0,
    val totalPedidos: Int = 0,
    val ultimoCliente: String? = null
)

/** Conserva los datos de una sesión y coordina las operaciones del negocio. */
class RepositorioPizzeria {
    private val pizzeria = crearPizzeria()
    val promocion get() = pizzeria.promocion

    // La fuente es local. Esta espera simula una carga sin bloquear el hilo principal.
    suspend fun cargarMenu(): List<Producto> {
        delay(900)
        return pizzeria.menu
    }

    fun resumen(): ResumenPizzeria {
        val clientes = pizzeria.consultarClientes()
        return ResumenPizzeria(clientes.size, pizzeria.consultarPedidos().size, clientes.lastOrNull()?.nombre)
    }

    fun seleccion(cantidades: List<Int>): List<Producto> {
        require(cantidades.size == pizzeria.menu.size && cantidades.all { it in 0..MAX_PRODUCTOS_POR_PEDIDO }) {
            "Revisa las cantidades de las pizzas."
        }
        require(cantidades.sum() <= MAX_PRODUCTOS_POR_PEDIDO) {
            "El pedido admite como máximo $MAX_PRODUCTOS_POR_PEDIDO pizzas."
        }
        return pizzeria.menu.flatMapIndexed { i, producto -> List(cantidades[i]) { producto } }
    }

    fun estimar(cantidades: List<Int>, fecha: String): Totales? {
        val productos = seleccion(cantidades)
        if (productos.isEmpty()) return null
        val dia = runCatching { interpretarFecha(fecha.trim()) }.getOrDefault(fechaActual())
        return calcularTotal(productos, fecha = dia, periodo = promocion)
    }

    fun clientes(): List<Cliente> = pizzeria.consultarClientes()
    fun pedidos(): List<Pedido> = pizzeria.consultarPedidos().toList()
    fun inventario(): Map<String, Int> = pizzeria.inventario.consultar()

    fun revisar(
        nombre: String, telefono: String, correo: String, direccion: String, zona: Zona,
        cantidades: List<Int>, fecha: String, notas: String
    ): Pedido {
        require(nombre.isNotBlank()) { "Ingresa tu nombre para continuar." }
        require(direccion.isNotBlank()) { "Ingresa la dirección de entrega." }
        val cliente = Cliente(nombre, telefono, correo, direccion.trim(), zona)
        val registrado = clientes().find { it == cliente }
        require(registrado == null || (registrado.direccion == cliente.direccion && registrado.zona == zona)) {
            "El cliente ya está registrado con otra dirección o zona. Usa los datos del primer pedido."
        }
        val dia = interpretarFecha(fecha.trim())
        val productos = seleccion(cantidades)
        require(productos.isNotEmpty()) { "Selecciona al menos una pizza." }
        pizzeria.registrarCliente(cliente)
        return pizzeria.crearPropuesta(cliente, productos, notas, dia)
    }

    fun confirmar(propuesta: Pedido): Pedido = pizzeria.confirmarPropuesta(propuesta)

    fun avanzar(id: Int): Pedido {
        val pedido = requireNotNull(pizzeria.buscarPedido(id)) { "El pedido no existe." }
        when (pedido.estado) {
            EstadoPedido.EN_PREPARACION -> pedido.cambiarEstado(EstadoPedido.LISTO)
            EstadoPedido.LISTO -> requireNotNull(pizzeria.asignarRepartidor(pedido)) {
                "Todos los repartidores están ocupados. Intenta nuevamente después de entregar otro pedido."
            }
            EstadoPedido.EN_CAMINO -> requireNotNull(pedido.repartidor).entregarPedido(pedido)
            else -> Unit
        }
        return pedido
    }

    fun reponer(ingrediente: String) = pizzeria.inventario.reabastecer(ingrediente, 10)
}
