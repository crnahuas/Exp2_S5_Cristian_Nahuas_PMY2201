package cl.duoc.pizzeria.modelo

import java.time.LocalDate

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Promocionable(val porcentaje: Int = 5)

data class PeriodoPromocion(val inicio: LocalDate, val fin: LocalDate) {
    init {
        require(inicio <= fin) { "El inicio de la promoción no puede ser posterior al término." }
    }

    // LocalDate es Comparable; el rango incluye las dos fechas límite.
    operator fun contains(fecha: LocalDate): Boolean = fecha in inicio..fin
}

// Fechas didácticas configurables. No representan una promoción real del negocio.
val PROMOCION_SEMANA_3 = PeriodoPromocion(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30))

fun porcentajePromocional(producto: Producto): Int {
    val porcentaje = producto.javaClass.getAnnotation(Promocionable::class.java)?.porcentaje ?: 0
    require(porcentaje in 0..100) { "El porcentaje promocional debe estar entre 0 y 100." }
    return porcentaje
}
