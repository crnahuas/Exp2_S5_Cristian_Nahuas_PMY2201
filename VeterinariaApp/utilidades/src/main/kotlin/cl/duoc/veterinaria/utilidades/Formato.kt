package cl.duoc.veterinaria.utilidades

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

fun precio(monto: BigDecimal): String = (if (monto.signum() < 0) "−$" else "$") + NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CL")).apply {
    minimumFractionDigits = if (monto.stripTrailingZeros().scale() > 0) 2 else 0
    maximumFractionDigits = 2
}.format(monto.abs())
