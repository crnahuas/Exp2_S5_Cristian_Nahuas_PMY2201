package cl.duoc.veterinaria

import cl.duoc.veterinaria.datos.RepositorioClinica
import cl.duoc.veterinaria.modelo.*
import java.math.BigDecimal
import java.time.format.DateTimeParseException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RepositorioClinicaTest {
    private fun dueno(repo: RepositorioClinica) = repo.registrarCliente("Ana", "+56 9 1234 5678", "ana@ejemplo.cl")
    private fun mascota(repo: RepositorioClinica): Mascota {
        val cliente = dueno(repo)
        return repo.registrarMascota(cliente.id, "Luna", Especie.GATO, "Mestiza", "3")
    }

    @Test fun `catalogo carga sin crear registros`() = runTest {
        val repo = RepositorioClinica()
        assertEquals(3, repo.cargarCatalogo().size)
        assertEquals(ResumenClinica(), repo.resumen())
    }

    @Test fun `dueños repetidos no cambian conteo ni ultimo cliente`() {
        val repo = RepositorioClinica()
        val ana = dueno(repo)
        repo.registrarCliente("Bruno", "+56987654321", "bruno@ejemplo.cl")
        assertSame(ana, repo.registrarCliente("ANA", "+56912345678", "ANA@EJEMPLO.CL"))
        assertEquals(ResumenClinica(2, 0, "Bruno", 0), repo.resumen())
    }

    @Test fun `correo compartido con otros datos no sustituye dueño existente`() {
        val repo = RepositorioClinica()
        val ana = dueno(repo)
        assertThrows<IllegalArgumentException> { repo.registrarCliente("Otra persona", "+56911111111", ana.correo) }
        assertEquals("Ana", repo.clientes().single().nombre)
    }

    @Test fun `mascota requiere dueño y edad valida sin dejar registros parciales`() {
        val repo = RepositorioClinica()
        assertThrows<IllegalArgumentException> { repo.registrarMascota(99, "Luna", Especie.GATO, "", "3") }
        val cliente = dueno(repo)
        assertThrows<IllegalArgumentException> { repo.registrarMascota(cliente.id, "Luna", Especie.GATO, "", "-1") }
        assertTrue(repo.mascotas().isEmpty())
        assertEquals(1, repo.resumen().totalClientes)
    }

    @Test fun `ficha duplicada no se registra dos veces`() {
        val repo = RepositorioClinica()
        val luna = mascota(repo)
        assertThrows<IllegalArgumentException> { repo.registrarMascota(luna.clienteId, "LUNA", Especie.GATO, "", "3") }
        assertEquals(1, repo.resumen().totalMascotas)
    }

    @Test fun `consulta vincula mascota dueño y servicios con costo correcto`() {
        val repo = RepositorioClinica()
        val luna = mascota(repo)
        val pedido = repo.registrarConsulta(luna.id, "23/11/2026", "Control anual", listOf(1, 2, 2))
        assertEquals(luna.clienteId, pedido.clienteId)
        assertEquals(luna.id, pedido.mascotaId)
        assertEquals(2, pedido.servicios.size)
        assertEquals(BigDecimal("40000.00"), pedido.total)
        assertEquals(ResumenClinica(1, 1, "Ana", 1), repo.resumen())
    }

    @Test fun `consulta invalida no incrementa pedidos`() {
        val repo = RepositorioClinica()
        val luna = mascota(repo)
        assertThrows<DateTimeParseException> { repo.registrarConsulta(luna.id, "30/02/2026", "Control", listOf(1)) }
        assertThrows<IllegalArgumentException> { repo.registrarConsulta(luna.id, "23/11/2026", "", listOf(1)) }
        assertThrows<IllegalArgumentException> { repo.registrarConsulta(luna.id, "23/11/2026", "Control", emptyList()) }
        assertThrows<IllegalArgumentException> { repo.registrarConsulta(luna.id, "23/11/2026", "Control", listOf(99)) }
        assertThrows<IllegalArgumentException> { repo.registrarConsulta(99, "23/11/2026", "Control", listOf(1)) }
        assertEquals(0, repo.resumen().totalPedidos)
    }

    @Test fun `cotizacion es independiente y no produce consultas`() {
        val repo = RepositorioClinica()
        assertEquals(BigDecimal("55000.00"), repo.cotizar(listOf(1, 2, 3)))
        assertEquals(BigDecimal("0.00"), repo.cotizar(emptyList()))
        assertEquals(ResumenClinica(), repo.resumen())
    }

    @Test fun `atencion avanza hasta finalizada sin duplicar consultas`() {
        val repo = RepositorioClinica()
        val pedido = repo.registrarConsulta(mascota(repo).id, "23/11/2026", "Revisión", listOf(1))
        assertEquals(EstadoConsulta.PROGRAMADA, pedido.estado)
        repo.avanzarConsulta(pedido.id)
        assertEquals(EstadoConsulta.EN_ATENCION, pedido.estado)
        repo.avanzarConsulta(pedido.id)
        assertEquals(EstadoConsulta.FINALIZADA, pedido.estado)
        assertThrows<IllegalArgumentException> { repo.avanzarConsulta(pedido.id) }
        assertEquals(1, repo.resumen().totalPedidos)
    }

    @Test fun `producto rechaza precios negativos y decimales no monetarios`() {
        assertThrows<IllegalArgumentException> { Producto(1, "Servicio", "", BigDecimal("-1")) }
        assertThrows<IllegalArgumentException> { Producto(1, "Servicio", "", BigDecimal("10.001")) }
    }
}
