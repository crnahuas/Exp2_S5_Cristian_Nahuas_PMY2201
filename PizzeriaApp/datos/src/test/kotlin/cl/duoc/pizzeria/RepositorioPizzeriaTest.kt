package cl.duoc.pizzeria

import cl.duoc.pizzeria.datos.*
import cl.duoc.pizzeria.modelo.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RepositorioPizzeriaTest {
    private fun revisar(repo: RepositorioPizzeria, nombre: String = "Ana", correo: String = "ana@ejemplo.cl", cantidad: Int = 1) =
        repo.revisar(nombre, "561234567890", correo, "Calle 123", Zona.CENTRO, listOf(cantidad, 0, 0), "23/11/2026", "")

    @Test fun `carga devuelve catalogo y resumen vacio`() = runTest {
        val repo = RepositorioPizzeria()
        assertEquals(3, repo.cargarMenu().size)
        assertEquals(ResumenPizzeria(), repo.resumen())
    }

    @Test fun `revisar registra cliente pero solo confirmar suma pedido`() {
        val repo = RepositorioPizzeria()
        val borrador = revisar(repo)
        assertEquals(ResumenPizzeria(1, 0, "Ana"), repo.resumen())
        assertEquals(20, repo.inventario()["masa"])
        repo.confirmar(borrador)
        assertEquals(ResumenPizzeria(1, 1, "Ana"), repo.resumen())
        assertEquals(19, repo.inventario()["masa"])
        assertThrows<IllegalArgumentException> { repo.confirmar(borrador) }
        assertEquals(1, repo.resumen().totalPedidos)
    }

    @Test fun `cliente repetido no altera el ultimo registro`() {
        val repo = RepositorioPizzeria()
        repo.confirmar(revisar(repo))
        repo.confirmar(revisar(repo, "Bruno", "bruno@ejemplo.cl"))
        repo.confirmar(revisar(repo, "ANA", "ANA@EJEMPLO.CL"))
        assertEquals(ResumenPizzeria(2, 3, "Bruno"), repo.resumen())
        assertEquals(listOf("Ana", "Bruno"), repo.clientes().map { it.nombre })
    }

    @Test fun `datos invalidos no producen registros`() {
        val repo = RepositorioPizzeria()
        assertThrows<IllegalArgumentException> { revisar(repo, correo = "no-es-correo") }
        assertThrows<IllegalArgumentException> { revisar(repo, cantidad = 0) }
        assertThrows<IllegalArgumentException> { revisar(repo, cantidad = 101) }
        assertEquals(ResumenPizzeria(), repo.resumen())
    }

    @Test fun `estimacion aplica promociones sin registrar ni consumir inventario`() {
        val repo = RepositorioPizzeria()
        val total = requireNotNull(repo.estimar(listOf(2, 1, 1), "23/11/2026"))
        assertEquals(java.math.BigDecimal("36146.25"), total.total)
        assertEquals(ResumenPizzeria(), repo.resumen())
        assertEquals(20, repo.inventario()["masa"])
        assertNull(repo.estimar(listOf(0, 0, 0), "23/11/2026"))
        assertThrows<IllegalArgumentException> { repo.seleccion(listOf(100, 1, 0)) }
    }

    @Test fun `stock insuficiente mantiene cliente sin sumar pedido`() {
        val repo = RepositorioPizzeria()
        val borrador = revisar(repo, cantidad = 21)
        assertThrows<StockInsuficienteException> { repo.confirmar(borrador) }
        assertEquals(ResumenPizzeria(1, 0, "Ana"), repo.resumen())
        assertEquals(20, repo.inventario()["masa"])
    }
}
