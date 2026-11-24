package cl.duoc.pizzeria.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pizzeria.datos.RepositorioPizzeria
import cl.duoc.pizzeria.datos.ResumenPizzeria
import cl.duoc.pizzeria.modelo.*
import cl.duoc.pizzeria.utilidades.formatearFecha
import cl.duoc.pizzeria.utilidades.mensajeError
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class AvisoPizzeria(val texto: String, val esError: Boolean = false)
data class EstadoCarga(val cargando: Boolean = true, val error: String? = null)
data class ClienteVista(val nombre: String, val correo: String, val contacto: String, val direccion: String, val zona: String)
data class PedidoVista(
    val id: Int, val cliente: String, val total: BigDecimal, val estado: EstadoPedido,
    val detalle: String, val fecha: String, val repartidor: String?
)

class PizzeriaViewModel : ViewModel() {
    private val repositorio = RepositorioPizzeria()
    private val _carga = MutableLiveData(EstadoCarga())
    val carga: LiveData<EstadoCarga> = _carga
    private val _resumen = MutableLiveData(ResumenPizzeria())
    val resumen: LiveData<ResumenPizzeria> = _resumen
    private val _clientes = MutableLiveData<List<ClienteVista>>(emptyList())
    val clientes: LiveData<List<ClienteVista>> = _clientes

    var menu by mutableStateOf<List<Producto>>(emptyList())
        private set
    val promocion = repositorio.promocion
    var propuesta by mutableStateOf<Pedido?>(null)
        private set
    var pedidos by mutableStateOf<List<PedidoVista>>(emptyList())
        private set
    var inventario by mutableStateOf(repositorio.inventario())
        private set
    private val canalAvisos = Channel<AvisoPizzeria>(Channel.BUFFERED)
    val avisos = canalAvisos.receiveAsFlow()
    var error by mutableStateOf("")
        private set

    private var cargaIniciada = false

    init { cargar() }

    fun cargar() {
        if (_carga.value?.cargando == true && cargaIniciada) return
        cargaIniciada = true
        _carga.value = EstadoCarga()
        viewModelScope.launch {
            try {
                menu = repositorio.cargarMenu()
                actualizar()
                _carga.value = EstadoCarga(cargando = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _carga.value = EstadoCarga(false, "No pudimos cargar la carta. Intenta nuevamente.")
            }
        }
    }

    fun revisar(
        nombre: String, telefono: String, correo: String, direccion: String, zona: Zona,
        cantidades: List<Int>, fecha: String, notas: String
    ) = intentar {
        propuesta = repositorio.revisar(nombre, telefono, correo, direccion, zona, cantidades, fecha, notas)
    }

    fun seleccion(cantidades: List<Int>): List<Producto> = repositorio.seleccion(cantidades)
    fun estimar(cantidades: List<Int>, fecha: String): Totales? = repositorio.estimar(cantidades, fecha)
    val totalPropuesta: Totales? get() = propuesta?.calcularTotal()

    fun cancelar() {
        propuesta = null
        error = ""
    }

    fun limpiarError() { error = "" }

    fun confirmar(): Boolean {
        var confirmado = false
        intentar {
            val pedido = repositorio.confirmar(requireNotNull(propuesta))
            propuesta = null
            canalAvisos.trySend(AvisoPizzeria("¡Pedido #${pedido.id} confirmado! Ya está en preparación."))
            confirmado = true
        }
        return confirmado
    }

    fun avanzar(id: Int) = intentar {
        val pedido = repositorio.avanzar(id)
        canalAvisos.trySend(AvisoPizzeria("Pedido #$id: ${pedido.estado.descripcion}."))
    }

    fun reponer(ingrediente: String) = intentar {
        repositorio.reponer(ingrediente)
        canalAvisos.trySend(AvisoPizzeria("Se agregaron 10 porciones de $ingrediente."))
    }

    private fun actualizar() {
        inventario = repositorio.inventario()
        pedidos = repositorio.pedidos().map {
            PedidoVista(
                it.id, it.cliente.nombre, it.calcularTotal().total, it.estado,
                it.productos.groupingBy { producto -> producto.nombre }.eachCount()
                    .entries.joinToString(" · ") { (nombre, cantidad) -> "$cantidad × $nombre" },
                formatearFecha(it.fecha), it.repartidor?.nombre
            )
        }
        _clientes.value = repositorio.clientes().map {
            ClienteVista(it.nombre, it.correo, it.contacto, it.direccion, it.zona.descripcion)
        }
        _resumen.value = repositorio.resumen()
    }

    private fun intentar(accion: () -> Unit) {
        error = ""
        try {
            accion()
        } catch (e: StockInsuficienteException) {
            error = "Falta reponer: " + e.faltantes.joinToString { "${it.ingrediente} (${it.cantidadAReponer})" }
        } catch (e: java.time.format.DateTimeParseException) {
            error = mensajeError(e)
        } catch (e: IllegalArgumentException) {
            error = mensajeError(e)
        } catch (e: ArithmeticException) {
            error = mensajeError(e)
        } finally {
            actualizar()
        }
        if (error.isNotBlank()) canalAvisos.trySend(AvisoPizzeria(error, esError = true))
    }
}
