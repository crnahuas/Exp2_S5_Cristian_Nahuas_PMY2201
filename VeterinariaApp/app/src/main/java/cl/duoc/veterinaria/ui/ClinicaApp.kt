package cl.duoc.veterinaria.ui

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.veterinaria.R
import cl.duoc.veterinaria.modelo.Especie
import cl.duoc.veterinaria.ui.theme.VerdeClinica
import cl.duoc.veterinaria.utilidades.fechaActual
import cl.duoc.veterinaria.utilidades.formatearFecha
import cl.duoc.veterinaria.utilidades.precio
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ClinicaApp(vm: ClinicaViewModel = viewModel()) {
    val estado by vm.estado.observeAsState(EstadoClinica())
    var ruta by rememberSaveable { mutableStateOf("Inicio") }
    var mascotaElegida by rememberSaveable { mutableIntStateOf(0) }
    val avisos = remember { SnackbarHostState() }
    val esFormulario = ruta.startsWith("Nueva") || ruta == "Nuevo dueño"
    fun navegar(destino: String) {
        avisos.currentSnackbarData?.dismiss()
        vm.limpiarError()
        ruta = destino
    }
    fun volver() { navegar(when (ruta) { "Nuevo dueño" -> "Dueños"; "Nueva mascota" -> "Mascotas"; else -> "Consultas" }) }
    BackHandler(enabled = esFormulario) { volver() }
    LaunchedEffect(vm) { vm.avisos.collectLatest { avisos.showSnackbar(it) } }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { Cabecera(esFormulario, ::volver) },
        snackbarHost = { SnackbarHost(avisos) },
        bottomBar = {
            if (!esFormulario && !estado.cargando && !estado.errorCarga) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    listOf("Inicio" to R.drawable.ic_inicio, "Dueños" to R.drawable.ic_duenos,
                        "Mascotas" to R.drawable.ic_mascota, "Consultas" to R.drawable.ic_consulta).forEach { (nombre, icono) ->
                        NavigationBarItem(modifier = Modifier.testTag("nav_$nombre"), selected = ruta == nombre, onClick = { navegar(nombre) },
                            icon = { Icon(painterResource(icono), null, Modifier.size(24.dp)) }, label = { Text(nombre) })
                    }
                }
            }
        }
    ) { padding ->
        val contenido = Modifier.fillMaxSize().padding(padding)
            .padding(bottom = if (avisos.currentSnackbarData != null) 80.dp else 0.dp)
        when {
            estado.cargando -> Column(contenido.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)) {
                IconoMascota(88)
                CircularProgressIndicator(Modifier.testTag("cargando"))
                Text("Preparando la clínica…", style = MaterialTheme.typography.titleLarge)
                Text("Cargando los servicios disponibles.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            estado.errorCarga -> Column(contenido.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)) {
                Text(estado.error.orEmpty())
                Button(onClick = vm::cargar) { Text("Reintentar") }
            }
            else -> when (ruta) {
                "Inicio" -> Inicio(estado, contenido, { navegar("Nuevo dueño") }, { mascotaElegida = 0; navegar("Nueva consulta") })
                "Dueños" -> Duenos(estado, contenido) { navegar("Nuevo dueño") }
                "Mascotas" -> Mascotas(estado, contenido, { navegar("Nueva mascota") }) { id -> mascotaElegida = id; navegar("Nueva consulta") }
                "Consultas" -> Consultas(estado, contenido, { mascotaElegida = 0; navegar("Nueva consulta") }, vm::avanzarConsulta)
                "Nuevo dueño" -> FormularioDueno(estado, contenido.imePadding()) { nombre, telefono, correo ->
                    if (vm.registrarCliente(nombre, telefono, correo)) ruta = "Dueños"
                }
                "Nueva mascota" -> FormularioMascota(estado, contenido.imePadding()) { dueno, nombre, especie, raza, edad ->
                    if (vm.registrarMascota(dueno, nombre, especie, raza, edad)) ruta = "Mascotas"
                }
                "Nueva consulta" -> FormularioConsulta(estado, contenido.imePadding(), mascotaElegida, vm) { mascota, fecha, motivo, servicios ->
                    if (vm.registrarConsulta(mascota, fecha, motivo, servicios)) ruta = "Consultas"
                }
            }
        }
    }
}

@Composable private fun Cabecera(volver: Boolean, alVolver: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (volver) TextButton(onClick = alVolver, modifier = Modifier.testTag("volver")) { Text("‹ Volver") }
            else IconoMascota(42)
            Column(Modifier.weight(1f)) {
                Text("Clínica Veterinaria", style = MaterialTheme.typography.titleLarge)
                if (!volver) Text("Dueños, mascotas y consultas", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable private fun IconoMascota(tamano: Int) {
    Box(Modifier.size(tamano.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
        Icon(painterResource(R.drawable.ic_mascota), null, Modifier.size((tamano * .6f).dp), tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable private fun Inicio(estado: EstadoClinica, modifier: Modifier, nuevoDueno: () -> Unit, nuevaConsulta: () -> Unit) {
    val resumen = estado.resumen
    LazyColumn(modifier.testTag("inicio"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = VerdeClinica, contentColor = androidx.compose.ui.graphics.Color.White) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("BIENVENIDO", style = MaterialTheme.typography.labelMedium)
                    Text("La atención de tus mascotas, organizada", style = MaterialTheme.typography.headlineMedium)
                    Text("Registra a sus dueños y organiza cada visita a la clínica.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            Text("Resumen de la clínica", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(14.dp))
            Tarjeta {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("${resumen.totalClientes}", Modifier.testTag("total_clientes"), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                        Text("Clientes registrados", style = MaterialTheme.typography.bodySmall)
                        Text("Dueños", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("${resumen.totalPedidos}", Modifier.testTag("total_pedidos"), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                        Text("Consultas registradas", style = MaterialTheme.typography.bodySmall)
                        Text("Pedidos de atención", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider()
                Text("Último cliente registrado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(resumen.ultimoCliente ?: "Sin clientes registrados", Modifier.testTag("ultimo_cliente"), style = MaterialTheme.typography.titleMedium)
                Text("${resumen.totalMascotas} mascotas registradas", Modifier.testTag("total_mascotas"), color = MaterialTheme.colorScheme.secondary)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = nuevoDueno, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Registrar dueño") }
                OutlinedButton(onClick = nuevaConsulta, enabled = estado.mascotas.isNotEmpty(), modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Agendar consulta") }
            }
        }
        item { Text("Servicios veterinarios", style = MaterialTheme.typography.titleLarge) }
        items(estado.catalogo, key = { it.id }) { producto ->
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(producto.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(precio(producto.precio), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item { NotaSesion() }
    }
}

@Composable private fun Duenos(estado: EstadoClinica, modifier: Modifier, registrar: () -> Unit) {
    LazyColumn(modifier.testTag("lista_duenos"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Titulo("Dueños", "Registra los datos de contacto de cada responsable.") }
        item { Button(onClick = registrar, modifier = Modifier.fillMaxWidth()) { Text("Registrar dueño") } }
        if (estado.clientes.isEmpty()) item { Vacio("Aún no hay dueños registrados", "Agrega al primer dueño para registrar sus mascotas.") }
        items(estado.clientes, key = { it.id }) { cliente ->
            Tarjeta(Modifier.testTag("dueno_${cliente.id}")) {
                Text(cliente.nombre, style = MaterialTheme.typography.titleMedium)
                Text(cliente.correo)
                Text(cliente.telefono)
                Text("${cliente.mascotas} mascota(s) a su cargo", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable private fun Mascotas(estado: EstadoClinica, modifier: Modifier, registrar: () -> Unit, agendar: (Int) -> Unit) {
    LazyColumn(modifier.testTag("lista_mascotas"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Titulo("Mascotas", "Cada ficha está asociada a su dueño.") }
        item {
            Button(onClick = registrar, enabled = estado.clientes.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("Registrar mascota") }
            if (estado.clientes.isEmpty()) Text("Primero registra un dueño en la sección Dueños.", style = MaterialTheme.typography.bodySmall)
        }
        if (estado.mascotas.isEmpty()) item { Vacio("Aún no hay mascotas registradas", "Completa una ficha para comenzar a organizar sus consultas.") }
        items(estado.mascotas, key = { it.id }) { mascota ->
            Tarjeta(Modifier.testTag("mascota_${mascota.id}")) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconoMascota(48)
                    Column { Text(mascota.nombre, style = MaterialTheme.typography.titleLarge); Text("${mascota.especie} · ${mascota.edad} años") }
                }
                Text("Raza: ${mascota.raza}")
                Text("Dueño: ${mascota.dueno}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = { agendar(mascota.id) }, modifier = Modifier.fillMaxWidth().testTag("agendar_${mascota.id}")) { Text("Agendar consulta") }
            }
        }
    }
}

@Composable private fun Consultas(estado: EstadoClinica, modifier: Modifier, registrar: () -> Unit, avanzar: (Int) -> Unit) {
    LazyColumn(modifier.testTag("lista_consultas"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Titulo("Consultas", "Revisa cada atención y actualiza su estado.") }
        item {
            Button(onClick = registrar, enabled = estado.mascotas.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("Agendar consulta") }
            if (estado.mascotas.isEmpty()) Text("Primero registra una mascota con su dueño.", style = MaterialTheme.typography.bodySmall)
            ErrorFormulario(estado.error)
        }
        if (estado.consultas.isEmpty()) item { Vacio("Aún no hay consultas registradas", "Agenda una atención para una de tus mascotas.") }
        items(estado.consultas, key = { it.id }) { consulta ->
            Tarjeta(Modifier.testTag("consulta_${consulta.id}")) {
                Text("CONSULTA #${consulta.id} · ${consulta.fecha}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(consulta.mascota, style = MaterialTheme.typography.titleLarge)
                Text("Dueño: ${consulta.dueno}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(consulta.motivo)
                Text(consulta.servicios, style = MaterialTheme.typography.bodySmall)
                Text(consulta.estado.descripcion, Modifier.testTag("estado_${consulta.id}"), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                HorizontalDivider()
                Text(precio(consulta.total), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                consulta.estado.accion?.let { accion ->
                    Button(onClick = { avanzar(consulta.id) }, modifier = Modifier.fillMaxWidth().testTag("avanzar_${consulta.id}")) { Text(accion) }
                }
            }
        }
    }
}

@Composable private fun FormularioDueno(estado: EstadoClinica, modifier: Modifier, guardar: (String, String, String) -> Unit) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    Formulario(modifier) {
        Titulo("Nuevo dueño", "Estos datos permiten identificar al responsable de la mascota.")
        Campo("Nombre del dueño", "nombre_dueno", nombre, { nombre = it })
        Campo("Teléfono", "telefono", telefono, { telefono = it }, KeyboardType.Phone, "+56 9 1234 5678")
        Campo("Correo electrónico", "correo", correo, { correo = it }, KeyboardType.Email)
        ErrorFormulario(estado.error)
        Button(onClick = { guardar(nombre, telefono, correo) }, modifier = Modifier.fillMaxWidth().testTag("guardar_dueno")) { Text("Guardar dueño") }
    }
}

@Composable private fun FormularioMascota(estado: EstadoClinica, modifier: Modifier, guardar: (Int, String, Especie, String, String) -> Unit) {
    var dueno by rememberSaveable { mutableIntStateOf(estado.clientes.firstOrNull()?.id ?: 0) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var especie by rememberSaveable { mutableStateOf(Especie.PERRO.name) }
    var raza by rememberSaveable { mutableStateOf("") }
    var edad by rememberSaveable { mutableStateOf("") }
    Formulario(modifier) {
        Titulo("Nueva mascota", "Asocia la ficha al dueño responsable de su atención.")
        Selector("Dueño", "dueno_selector", estado.clientes.map { it.id to it.nombre }, dueno) { dueno = it }
        Campo("Nombre de la mascota", "nombre_mascota", nombre, { nombre = it })
        Text("Especie", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Especie.entries.forEach { opcion ->
                FilterChip(selected = especie == opcion.name, onClick = { especie = opcion.name }, label = { Text(opcion.descripcion) })
            }
        }
        Campo("Raza (opcional)", "raza", raza, { raza = it })
        Campo("Edad en años", "edad", edad, { edad = it }, KeyboardType.Number, "Usa 0 si aún no cumple un año.")
        ErrorFormulario(estado.error)
        Button(onClick = { guardar(dueno, nombre, Especie.valueOf(especie), raza, edad) }, modifier = Modifier.fillMaxWidth().testTag("guardar_mascota")) { Text("Guardar mascota") }
    }
}

@Composable private fun FormularioConsulta(estado: EstadoClinica, modifier: Modifier, preseleccion: Int, vm: ClinicaViewModel,
    guardar: (Int, String, String, List<Int>) -> Unit) {
    var mascota by rememberSaveable { mutableIntStateOf(preseleccion.takeIf { id -> estado.mascotas.any { it.id == id } } ?: estado.mascotas.firstOrNull()?.id ?: 0) }
    var fecha by rememberSaveable { mutableStateOf(formatearFecha(fechaActual())) }
    var motivo by rememberSaveable { mutableStateOf("") }
    var servicios by rememberSaveable { mutableStateOf(listOf(1)) }
    val total = vm.cotizar(servicios)
    Formulario(modifier) {
        Titulo("Nueva consulta", "Elige una mascota, la fecha y los servicios de su atención.")
        Selector("Mascota y dueño", "mascota_selector", estado.mascotas.map { it.id to "${it.nombre} · ${it.dueno}" }, mascota) { mascota = it }
        Campo("Fecha de atención", "fecha", fecha, { fecha = it }, ayuda = "DD/MM/AAAA")
        Campo("Motivo de la consulta", "motivo", motivo, { motivo = it })
        Text("Servicios", style = MaterialTheme.typography.titleMedium)
        estado.catalogo.forEach { servicio ->
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Checkbox(checked = servicio.id in servicios, onCheckedChange = { marcado ->
                        servicios = if (marcado) servicios + servicio.id else servicios - servicio.id
                    }, modifier = Modifier.testTag("servicio_${servicio.id}"))
                    Column(Modifier.weight(1f)) {
                        Text(servicio.nombre, style = MaterialTheme.typography.bodyLarge)
                        Text(precio(servicio.precio), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Text("Total: ${precio(total)}", Modifier.testTag("total_consulta"), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Text("Valores finales en pesos chilenos.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ErrorFormulario(estado.error)
        Button(onClick = { guardar(mascota, fecha, motivo, servicios) }, modifier = Modifier.fillMaxWidth().testTag("guardar_consulta")) { Text("Registrar consulta") }
    }
}

@Composable private fun Selector(etiqueta: String, id: String, opciones: List<Pair<Int, String>>, seleccionado: Int, cambiar: (Int) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.labelLarge)
        OutlinedButton(onClick = { abierto = true }, modifier = Modifier.fillMaxWidth().testTag(id), shape = RoundedCornerShape(12.dp)) {
            Text(opciones.find { it.first == seleccionado }?.second ?: "Seleccionar", Modifier.weight(1f))
            Text("⌄")
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { (clave, nombre) ->
                DropdownMenuItem(text = { Text(nombre) }, onClick = { cambiar(clave); abierto = false }, modifier = Modifier.testTag("${id}_$clave"))
            }
        }
    }
}

@Composable private fun Formulario(modifier: Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Column(modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        contenido()
        Spacer(Modifier.height(24.dp))
    }
}
@Composable private fun Campo(etiqueta: String, id: String, valor: String, cambiar: (String) -> Unit, teclado: KeyboardType = KeyboardType.Text, ayuda: String? = null) {
    OutlinedTextField(value = valor, onValueChange = cambiar, label = { Text(etiqueta) }, singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("campo_$id"), shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = teclado), supportingText = ayuda?.let { { Text(it) } })
}
@Composable private fun Titulo(titulo: String, descripcion: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo, style = MaterialTheme.typography.headlineLarge)
        Text(descripcion, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable private fun Tarjeta(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = contenido)
    }
}
@Composable private fun Vacio(titulo: String, detalle: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        IconoMascota(64)
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Text(detalle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable private fun ErrorFormulario(error: String?) {
    if (error != null) Text(error, Modifier.testTag("error_formulario"), color = MaterialTheme.colorScheme.error)
}
@Composable private fun NotaSesion() {
    Text("Los datos se mantienen durante esta sesión, incluso al girar la pantalla.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
