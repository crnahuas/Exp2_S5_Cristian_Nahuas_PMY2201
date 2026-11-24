package cl.duoc.pizzeria

import cl.duoc.pizzeria.modelo.*
import cl.duoc.pizzeria.utilidades.*
import cl.duoc.pizzeria.negocio.crearPizzeria
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IntegracionMovilTest {
    private val fecha = LocalDate.of(2026, 11, 9)
    private fun cliente() = Cliente("Ana", "561234567890", "ana@ejemplo.cl", "Calle 123", Zona.CENTRO)

    @Test fun `pedido integrado conserva descuentos e IVA de semana 3`() {
        val pizzeria = crearPizzeria()
        val cliente = cliente()
        pizzeria.registrarCliente(cliente)
        val menu = pizzeria.menu
        val propuesta = pizzeria.crearPropuesta(cliente, listOf(menu[0], menu[0], menu[1], menu[2]), fecha = fecha)
        val total = propuesta.calcularTotal()
        check(total.descuentoVolumen == BigDecimal("3550.00"))
        check(total.descuentoPromocional == BigDecimal("1575.00"))
        check(total.impuestos == BigDecimal("5771.25"))
        check(total.total == BigDecimal("36146.25"))
        check(pizzeria.inventario.consultar().getValue("masa") == 20)
        pizzeria.confirmarPropuesta(propuesta)
        check(pizzeria.inventario.consultar().getValue("masa") == 16)
        assertThrows<IllegalArgumentException> { pizzeria.confirmarPropuesta(propuesta) }
        check(pizzeria.inventario.consultar().getValue("masa") == 16)
    }

    @Test fun `stock insuficiente no consume ingredientes ni registra pedido`() {
        val pizzeria = crearPizzeria()
        val cliente = cliente()
        pizzeria.registrarCliente(cliente)
        val antes = pizzeria.inventario.consultar()
        val propuesta = pizzeria.crearPropuesta(cliente, List(21) { pizzeria.menu[0] }, fecha = fecha)
        assertThrows<StockInsuficienteException> { pizzeria.confirmarPropuesta(propuesta) }
        check(pizzeria.inventario.consultar() == antes)
        check(pizzeria.consultarPedidos().isEmpty())
        check(propuesta.estado == EstadoPedido.BORRADOR)
    }

    @Test fun `flujo movil completa entrega y libera repartidor`() {
        val pizzeria = crearPizzeria()
        val cliente = cliente()
        pizzeria.registrarCliente(cliente)
        val pedido = pizzeria.confirmarPedido(cliente, listOf(pizzeria.menu[1]), null, fecha)
        pedido.cambiarEstado(EstadoPedido.LISTO)
        val repartidor = requireNotNull(pizzeria.asignarRepartidor(pedido))
        check(repartidor.zona == Zona.CENTRO && !repartidor.disponible)
        check(pedido.estado == EstadoPedido.EN_CAMINO)
        repartidor.entregarPedido(pedido)
        check(pedido.estado == EstadoPedido.ENTREGADO && repartidor.disponible)
        check(pizzeria.inventario.consultar().getValue("masa") == 19)
    }

    @Test fun `reflection Java conserva promociones de los subtipos`() {
        check(crearPizzeria().menu.map(::porcentajePromocional) == listOf(5, 0, 10))
    }

    @Test fun `validaciones rechazan contacto y fechas invalidas`() {
        assertThrows<IllegalArgumentException> { cliente().let { Cliente(it.nombre, "123", it.correo, it.direccion, it.zona) } }
        assertThrows<IllegalArgumentException> { validarCorreo("ana@") }
        assertThrows<java.time.format.DateTimeParseException> { interpretarFecha("30/02/2026") }
        check(interpretarFecha("29/02/2028") == LocalDate.of(2028, 2, 29))
    }

    @Test fun `inventario adaptado para API 21 conserva proteccion contra desbordamiento`() {
        val inventario = Inventario(mapOf("masa" to Int.MAX_VALUE))
        assertThrows<ArithmeticException> { inventario.reabastecer("masa", 1) }
        check(inventario.consultar().getValue("masa") == Int.MAX_VALUE)
        assertThrows<ArithmeticException> {
            inventario.calcularIngredientesNecesarios(listOf(Ingrediente("masa", Int.MAX_VALUE)), 2)
        }
    }

    @Test fun `combinacion sigue disponible en el modelo movil`() {
        val cliente = cliente()
        val pizzeria = crearPizzeria()
        pizzeria.registrarCliente(cliente)
        val a = pizzeria.crearPropuesta(cliente, List(2) { pizzeria.menu[0] }, fecha = fecha)
        val b = pizzeria.crearPropuesta(cliente, listOf(pizzeria.menu[1], pizzeria.menu[2]), fecha = fecha)
        val combinado = a + b
        check(combinado.productos.size == 4 && combinado.calcularTotal().total == BigDecimal("36146.25"))
        check(pizzeria.consultarPedidos().isEmpty())
    }
}
