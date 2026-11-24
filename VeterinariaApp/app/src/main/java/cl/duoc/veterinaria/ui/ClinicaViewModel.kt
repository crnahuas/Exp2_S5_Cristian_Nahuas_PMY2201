package cl.duoc.veterinaria.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.veterinaria.datos.RepositorioClinica
import cl.duoc.veterinaria.modelo.*
import cl.duoc.veterinaria.utilidades.formatearFecha
import cl.duoc.veterinaria.utilidades.mensajeError
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class ClienteVista(val id: Int, val nombre: String, val telefono: String, val correo: String, val mascotas: Int)
data class MascotaVista(val id: Int, val nombre: String, val dueno: String, val especie: String, val raza: String, val edad: Int)
data class ConsultaVista(val id: Int, val mascota: String, val dueno: String, val fecha: String,
    val motivo: String, val servicios: String, val total: BigDecimal, val estado: EstadoConsulta)
data class EstadoClinica(
    val cargando: Boolean = true, val errorCarga: Boolean = false, val error: String? = null,
    val resumen: ResumenClinica = ResumenClinica(), val clientes: List<ClienteVista> = emptyList(),
    val mascotas: List<MascotaVista> = emptyList(), val consultas: List<ConsultaVista> = emptyList(),
    val catalogo: List<Producto> = emptyList()
)

class ClinicaViewModel : ViewModel() {
    private val repositorio = RepositorioClinica()
    private val _estado = MutableLiveData(EstadoClinica())
    val estado: LiveData<EstadoClinica> = _estado
    private val canalAvisos = Channel<String>(Channel.BUFFERED)
    val avisos = canalAvisos.receiveAsFlow()
    private var cargaIniciada = false
    init { cargar() }

    fun cargar() {
        if (_estado.value?.cargando == true && cargaIniciada) return
        cargaIniciada = true
        _estado.value = EstadoClinica()
        viewModelScope.launch {
            try {
                val catalogo = repositorio.cargarCatalogo()
                _estado.value = EstadoClinica(cargando = false, catalogo = catalogo)
                actualizar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = EstadoClinica(cargando = false, errorCarga = true,
                    error = "No pudimos cargar la clínica. Intenta nuevamente.")
            }
        }
    }

    fun limpiarError() { _estado.value = _estado.value?.copy(error = null) }
    fun cotizar(ids: List<Int>): BigDecimal = repositorio.cotizar(ids)
    fun registrarCliente(nombre: String, telefono: String, correo: String): Boolean = intentar("Dueño registrado.") {
        repositorio.registrarCliente(nombre, telefono, correo)
    }
    fun registrarMascota(cliente: Int, nombre: String, especie: Especie, raza: String, edad: String): Boolean = intentar("Mascota registrada.") {
        repositorio.registrarMascota(cliente, nombre, especie, raza, edad)
    }
    fun registrarConsulta(mascota: Int, fecha: String, motivo: String, servicios: List<Int>): Boolean = intentar("Consulta registrada.") {
        repositorio.registrarConsulta(mascota, fecha, motivo, servicios)
    }
    fun avanzarConsulta(id: Int) { intentar("Estado de la consulta actualizado.") { repositorio.avanzarConsulta(id) } }

    private fun actualizar() {
        val clientes = repositorio.clientes()
        val mascotas = repositorio.mascotas()
        val duenos = clientes.associateBy { it.id }
        val fichas = mascotas.associateBy { it.id }
        _estado.value = requireNotNull(_estado.value).copy(
            resumen = repositorio.resumen(),
            clientes = clientes.map { c -> ClienteVista(c.id, c.nombre, c.telefono, c.correo, mascotas.count { it.clienteId == c.id }) },
            mascotas = mascotas.map { m -> MascotaVista(m.id, m.nombre, requireNotNull(duenos[m.clienteId]).nombre, m.especie.descripcion, m.raza, m.edad) },
            consultas = repositorio.pedidos().asReversed().map { p -> ConsultaVista(p.id, requireNotNull(fichas[p.mascotaId]).nombre,
                requireNotNull(duenos[p.clienteId]).nombre, formatearFecha(p.fecha), p.motivo,
                p.servicios.joinToString { it.nombre }, p.total, p.estado) }
        )
    }

    private fun intentar(aviso: String, accion: () -> Unit): Boolean {
        limpiarError()
        return try {
            accion()
            actualizar()
            canalAvisos.trySend(aviso)
            true
        } catch (e: IllegalArgumentException) {
            _estado.value = _estado.value?.copy(error = mensajeError(e))
            false
        } catch (e: java.time.format.DateTimeParseException) {
            _estado.value = _estado.value?.copy(error = mensajeError(e))
            false
        }
    }
}
