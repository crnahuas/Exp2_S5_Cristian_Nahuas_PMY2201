package cl.duoc.pizzeria.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import cl.duoc.pizzeria.Greetings
import cl.duoc.pizzeria.datos.ResumenPizzeria
import cl.duoc.pizzeria.ui.theme.Albahaca
import cl.duoc.pizzeria.ui.theme.Crema
import cl.duoc.pizzeria.ui.theme.Dorado

@Composable
internal fun Inicio(vm: PizzeriaViewModel, modifier: Modifier, irCarta: () -> Unit) {
    val resumen by vm.resumen.observeAsState(ResumenPizzeria())
    val clientes by vm.clientes.observeAsState(emptyList())
    LazyColumn(modifier.testTag("inicio"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = Albahaca, contentColor = Crema) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("SIEMPRE HAY ALGO PARA COMPARTIR", color = Dorado, style = MaterialTheme.typography.labelSmall)
                    Greetings()
                    Text("La carta, tus clientes y cada pedido, en un solo lugar.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Tu sesión de hoy", style = MaterialTheme.typography.titleLarge)
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text("${resumen.totalClientes}", Modifier.testTag("total_clientes"), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                                Text("Clientes registrados", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(Modifier.weight(1f)) {
                                Text("${resumen.totalPedidos}", Modifier.testTag("total_pedidos"), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                                Text("Pedidos realizados", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        HorizontalDivider()
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Último cliente registrado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(resumen.ultimoCliente ?: "Sin clientes registrados", Modifier.testTag("ultimo_cliente"), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                Button(onClick = irCarta, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) {
                    Text("Elegir pizzas")
                }
            }
        }
        item {
            Text("Nuestros clientes", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(if (clientes.isEmpty()) "Al revisar tu primer pedido, el cliente aparecerá aquí." else "Los datos se actualizan al registrar un cliente.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(clientes, key = { it.nombre.lowercase(java.util.Locale.ROOT) + ":" + it.correo.lowercase(java.util.Locale.ROOT) }) { cliente ->
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(cliente.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(cliente.correo, style = MaterialTheme.typography.bodyMedium)
                    Text(cliente.contacto, style = MaterialTheme.typography.bodyMedium)
                    Text("${cliente.direccion} · ${cliente.zona}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Text("Los clientes y pedidos se mantienen durante esta sesión, incluso al girar la pantalla.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
