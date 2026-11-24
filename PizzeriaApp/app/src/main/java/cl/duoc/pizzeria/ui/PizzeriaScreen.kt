package cl.duoc.pizzeria.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.pizzeria.Greetings
import cl.duoc.pizzeria.R
import cl.duoc.pizzeria.modelo.*
import cl.duoc.pizzeria.utilidades.*
import cl.duoc.pizzeria.ui.componentes.PizzaIlustracion
import cl.duoc.pizzeria.ui.theme.Albahaca
import cl.duoc.pizzeria.ui.theme.Crema
import cl.duoc.pizzeria.ui.theme.Dorado
import java.math.BigDecimal
import kotlinx.coroutines.flow.collectLatest

private data class FormularioEntrega(
    val nombre: String = "", val telefono: String = "", val correo: String = "",
    val direccion: String = "", val zona: String = Zona.CENTRO.name,
    val fecha: String = formatearFecha(fechaActual()), val notas: String = ""
)

private val GuardarFormulario = listSaver<FormularioEntrega, String>(
    save = { listOf(it.nombre, it.telefono, it.correo, it.direccion, it.zona, it.fecha, it.notas) },
    restore = { FormularioEntrega(it[0], it[1], it[2], it[3], it[4], it[5], it[6]) }
)

@Composable
fun PizzeriaApp(vm: PizzeriaViewModel = viewModel()) {
    val carga by vm.carga.observeAsState(EstadoCarga())
    when {
        carga.cargando -> Scaffold(topBar = { Cabecera(false) {} }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)) {
                PizzaIlustracion(0, Modifier.size(140.dp))
                CircularProgressIndicator(Modifier.testTag("cargando"))
                Text("Preparando la carta…", style = MaterialTheme.typography.titleMedium)
                Text("Tus favoritas estarán listas en un momento.", style = MaterialTheme.typography.bodySmall)
            }
        }
        carga.error != null -> Scaffold(topBar = { Cabecera(false) {} }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)) {
                Text(requireNotNull(carga.error), style = MaterialTheme.typography.titleMedium)
                Button(onClick = vm::cargar) { Text("Reintentar") }
            }
        }
        else -> PizzeriaScreen(vm)
    }
}

@Composable
private fun PizzeriaScreen(vm: PizzeriaViewModel) {
    var pantalla by rememberSaveable { mutableStateOf("Inicio") }
    var datos by rememberSaveable(stateSaver = GuardarFormulario) { mutableStateOf(FormularioEntrega()) }
    var cantidades by rememberSaveable { mutableStateOf(List(vm.menu.size) { 0 }) }
    val seleccion = vm.seleccion(cantidades)
    val totales = vm.estimar(cantidades, datos.fecha)
    val notificaciones = remember { SnackbarHostState() }
    val pantallaActual by rememberUpdatedState(pantalla)
    LaunchedEffect(vm) {
        vm.avisos.collectLatest { aviso ->
            // Los errores de entrega permanecen junto al formulario o en su diálogo.
            if (!aviso.esError || pantallaActual != "Entrega") {
                notificaciones.showSnackbar(aviso.texto)
            }
        }
    }
    fun navegar(destino: String) {
        if (pantalla != destino) {
            notificaciones.currentSnackbarData?.dismiss()
            vm.limpiarError()
            pantalla = destino
        }
    }
    BackHandler(enabled = pantalla == "Entrega" && vm.propuesta == null) { navegar("Carta") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { Cabecera(pantalla == "Entrega") { navegar("Carta") } },
        snackbarHost = { SnackbarHost(notificaciones) },
        bottomBar = {
            Column {
                if (pantalla == "Carta" && seleccion.isNotEmpty()) {
                    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("${seleccion.size} pizza(s) · Total estimado", style = MaterialTheme.typography.bodySmall)
                                Text(precio(requireNotNull(totales).total), style = MaterialTheme.typography.titleMedium)
                            }
                            Button(onClick = { navegar("Entrega") }, shape = RoundedCornerShape(14.dp)) { Text("Ver mi pedido") }
                        }
                    }
                }
                if (pantalla != "Entrega") {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                        listOf("Inicio" to R.drawable.ic_inicio, "Carta" to R.drawable.ic_carta, "Pedidos" to R.drawable.ic_pedidos, "Cocina" to R.drawable.ic_cocina).forEach { (nombre, icono) ->
                            NavigationBarItem(
                                selected = pantalla == nombre,
                                onClick = { navegar(nombre) },
                                icon = { Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(24.dp)) },
                                label = { Text(nombre) },
                                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        val contenido = Modifier.fillMaxSize().padding(padding)
            .then(if (pantalla == "Entrega") Modifier.navigationBarsPadding() else Modifier)
        when (pantalla) {
            "Inicio" -> Inicio(vm, contenido) { navegar("Carta") }
            "Carta" -> Carta(vm, cantidades, contenido) { indice, cambio ->
                cantidades = cantidades.toMutableList().also { it[indice] += cambio }
            }
            "Entrega" -> Entrega(datos, { datos = it }, seleccion, totales, vm.error, contenido.imePadding()) {
                vm.revisar(datos.nombre, datos.telefono, datos.correo, datos.direccion, Zona.valueOf(datos.zona), cantidades, datos.fecha, datos.notas)
            }
            "Pedidos" -> Pedidos(vm, contenido) { navegar("Carta") }
            "Cocina" -> Cocina(vm, contenido)
        }
    }
    vm.propuesta?.let { propuesta ->
        Confirmacion(propuesta, requireNotNull(vm.totalPropuesta), vm.error, vm::cancelar) {
            if (vm.confirmar()) {
                cantidades = List(vm.menu.size) { 0 }
                pantalla = "Pedidos"
            }
        }
    }
}

@Composable
private fun Cabecera(volver: Boolean, alVolver: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (volver) {
                IconButton(onClick = alVolver, modifier = Modifier.semantics { contentDescription = "Volver a la carta" }) {
                    Text("‹", style = MaterialTheme.typography.headlineLarge)
                }
            } else {
                Box(Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_carta), null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                }
            }
            Column {
                Text("La Pizzería", style = MaterialTheme.typography.titleLarge)
                Text(if (volver) "TU PRÓXIMO PEDIDO" else "MASA, HORNO Y BUENA MESA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Carta(vm: PizzeriaViewModel, cantidades: List<Int>, modifier: Modifier, cambiar: (Int, Int) -> Unit) {
    LazyColumn(modifier, contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = Albahaca, contentColor = Crema) {
                Column(Modifier.padding(20.dp)) {
                    Text("AMOR POR LA PIZZA", color = Dorado, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Greetings(Modifier.weight(1f))
                        PizzaIlustracion(1, Modifier.size(92.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Tu favorita, a un pedido de distancia.", style = MaterialTheme.typography.bodySmall, color = Crema.copy(alpha = .8f))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("10%", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Column {
                    Text("La pizza se comparte", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Desde 4 pizzas, descuento por volumen.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        item { TituloSeccion("LA CARTA", "¿Cuál se te antoja?", "Elige tus pizzas y ajusta las cantidades.") }
        items(vm.menu.size, key = { vm.menu[it].nombre }) { indice ->
            val producto = vm.menu[indice]
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(Modifier.size(92.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                            PizzaIlustracion(indice, Modifier.size(84.dp))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(listOf("LA CLÁSICA", "CON PEPPERONI", "DE LA HUERTA")[indice], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(producto.ingredientes.joinToString { it.nombre }.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(precio(producto.precio), style = MaterialTheme.typography.titleMedium)
                            Text("Precio sin IVA", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { cambiar(indice, -1) }, enabled = cantidades[indice] > 0, modifier = Modifier.semantics { contentDescription = "Quitar ${producto.nombre}" }) { Text("−", style = MaterialTheme.typography.titleMedium) }
                            Text("${cantidades[indice]}", Modifier.widthIn(min = 24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
                            FilledIconButton(onClick = { cambiar(indice, 1) }, enabled = cantidades.sum() < MAX_PRODUCTOS_POR_PEDIDO, modifier = Modifier.semantics { contentDescription = "Agregar ${producto.nombre}" }) { Text("+", style = MaterialTheme.typography.titleMedium) }
                        }
                    }
                }
            }
        }
        item { Text("Precios en pesos chilenos. El total incluye IVA y los descuentos que correspondan a la fecha del pedido. Máximo $MAX_PRODUCTOS_POR_PEDIDO pizzas por pedido.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun Entrega(datos: FormularioEntrega, cambiar: (FormularioEntrega) -> Unit, seleccion: List<Producto>, totales: Totales?, error: String, modifier: Modifier, revisar: () -> Unit) {
    Column(modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TituloSeccion("CASI EN EL HORNO", "¿Dónde la llevamos?", "Revisa tu selección y completa los datos de entrega.")
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                seleccion.groupingBy { it.nombre }.eachCount().forEach { (nombre, cantidad) -> Text("$cantidad × $nombre", color = MaterialTheme.colorScheme.onSecondaryContainer) }
                totales?.let { Text("Total estimado: ${precio(it.total)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer) }
            }
        }
        Campo("Nombre", "nombre", datos.nombre, { cambiar(datos.copy(nombre = it)) })
        Campo("Teléfono", "telefono", datos.telefono, { cambiar(datos.copy(telefono = it)) }, KeyboardType.Phone, "12 dígitos, por ejemplo 561234567890")
        Campo("Correo electrónico", "correo", datos.correo, { cambiar(datos.copy(correo = it)) }, KeyboardType.Email)
        Campo("Dirección", "direccion", datos.direccion, { cambiar(datos.copy(direccion = it)) })
        Text("Zona de entrega", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Zona.entries.forEach { zona -> FilterChip(selected = datos.zona == zona.name, onClick = { cambiar(datos.copy(zona = zona.name)) }, label = { Text(zona.descripcion) }) }
        }
        Campo("Fecha del pedido", "fecha", datos.fecha, { cambiar(datos.copy(fecha = it)) }, ayuda = "DD/MM/AAAA")
        Campo("Notas de entrega (opcional)", "notas", datos.notas, { cambiar(datos.copy(notas = it)) })
        Text("Promoción de temporada: Margarita 5% y Vegetariana 10%, del 01/11/2026 al 30/11/2026. Se combina con el descuento por volumen.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("error_formulario"))
        Button(onClick = revisar, enabled = seleccion.isNotEmpty(), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Revisar pedido") }
        Text("Podrás revisar el detalle y el total antes de confirmar.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun Pedidos(vm: PizzeriaViewModel, modifier: Modifier, irCarta: () -> Unit) {
    LazyColumn(modifier, contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { TituloSeccion("DEL HORNO A TU MESA", "Tus pedidos", "Sigue la preparación y la entrega de cada pedido.") }
        if (vm.pedidos.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PizzaIlustracion(0, Modifier.size(120.dp))
                    Text("El próximo puede ser tu favorito", style = MaterialTheme.typography.titleMedium)
                    Text("Elige una pizza de la carta para comenzar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = irCarta) { Text("Ver la carta") }
                }
            }
        }
        items(vm.pedidos, key = { it.id }) { pedido ->
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("PEDIDO #${pedido.id} · ${pedido.fecha}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(pedido.cliente, style = MaterialTheme.typography.titleMedium)
                    Text(pedido.detalle)
                    EstadoVisual(pedido.estado)
                    pedido.repartidor?.let { Text("Repartidor: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(precio(pedido.total), style = MaterialTheme.typography.titleLarge)
                    val accion = when (pedido.estado) {
                        EstadoPedido.EN_PREPARACION -> "Marcar listo"
                        EstadoPedido.LISTO -> "Asignar repartidor"
                        EstadoPedido.EN_CAMINO -> "Confirmar entrega"
                        else -> null
                    }
                    if (accion != null) Button(onClick = { vm.avanzar(pedido.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text(accion) }
                }
            }
        }
        item { Text("Los pedidos se guardan durante esta sesión. Al finalizar el proceso de la app, se reinician.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun EstadoVisual(estado: EstadoPedido) {
    val estados = listOf(EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO, EstadoPedido.EN_CAMINO, EstadoPedido.ENTREGADO)
    val indice = estados.indexOf(estado)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            estados.forEachIndexed { i, _ -> Box(Modifier.weight(1f).height(5.dp).clip(CircleShape).background(if (i <= indice) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)) }
        }
        Text(estado.descripcion, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Cocina(vm: PizzeriaViewModel, modifier: Modifier) {
    LazyColumn(modifier, contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { TituloSeccion("DETRÁS DE CADA PIZZA", "Nuestra cocina", "Consulta las existencias y repón los ingredientes.") }
        item {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("${vm.inventario.values.sum()} porciones", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("${vm.inventario.size} ingredientes en la cocina", color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
        items(vm.inventario.toList(), key = { it.first }) { (ingrediente, cantidad) ->
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(ingrediente.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                        Text("$cantidad porciones", modifier = Modifier.testTag("stock_$ingrediente"))
                        Text(if (cantidad == 0) "Agotado" else if (cantidad <= 5) "Stock bajo" else "Disponible", style = MaterialTheme.typography.bodySmall, color = if (cantidad <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
                    }
                    OutlinedButton(onClick = { vm.reponer(ingrediente) }, shape = RoundedCornerShape(12.dp), modifier = Modifier.semantics { contentDescription = "Reponer $ingrediente" }) { Text("+10") }
                }
            }
        }
        item { Text("Los ingredientes se descuentan al confirmar el pedido. Cancelar la revisión mantiene las existencias.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun Confirmacion(propuesta: Pedido, total: Totales, error: String, cancelar: () -> Unit, confirmar: () -> Unit) {
    AlertDialog(
        onDismissRequest = cancelar,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Directo al horno", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${propuesta.cliente.nombre} · ${propuesta.cliente.zona.descripcion}")
                Text(propuesta.cliente.direccion)
                Text(formatearFecha(propuesta.fecha))
                HorizontalDivider()
                propuesta.productos.groupingBy { it.nombre }.eachCount().forEach { (pizza, cantidad) -> Text("$cantidad × $pizza") }
                HorizontalDivider()
                LineaTotal("Subtotal", total.subtotal)
                LineaTotal("Descuento por volumen", total.descuentoVolumen.negate())
                LineaTotal("Descuento promocional", total.descuentoPromocional.negate())
                LineaTotal("IVA 19%", total.impuestos)
                Text("Total: ${precio(total.total)}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.testTag("total_confirmacion"))
                if (propuesta.notas != "Sin notas") Text("Notas: ${propuesta.notas}")
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { Button(onClick = confirmar, shape = RoundedCornerShape(12.dp)) { Text("Confirmar pedido") } },
        dismissButton = { TextButton(onClick = cancelar) { Text("Seguir editando") } }
    )
}

@Composable
private fun LineaTotal(etiqueta: String, monto: BigDecimal) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(etiqueta, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(precio(monto), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TituloSeccion(antetitulo: String, titulo: String, descripcion: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(antetitulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(titulo, style = MaterialTheme.typography.headlineLarge)
        Text(descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Campo(etiqueta: String, id: String, valor: String, cambiar: (String) -> Unit, teclado: KeyboardType = KeyboardType.Text, ayuda: String? = null) {
    OutlinedTextField(
        value = valor,
        onValueChange = cambiar,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth().testTag("campo_$id"),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        supportingText = ayuda?.let { { Text(it) } }
    )
}
