# La Pizzería

Aplicación Android para elegir pizzas, registrar los datos de entrega y seguir los pedidos hasta su entrega. Incluye un resumen de clientes y pedidos y una sección para consultar y reponer ingredientes.

## Ejecutar en Android Studio

1. Selecciona **Open** y abre esta carpeta **PizzeriaApp**, donde están `settings.gradle.kts` y `gradlew`.
2. Configura **Gradle JDK** con Java 17 o 21 y espera la sincronización de Gradle. La primera sincronización necesita conexión a Internet para descargar las dependencias.
3. En **SDK Manager**, instala Android SDK Platform 35, Build Tools 35.0.0, Platform Tools y Android Emulator.
4. En **Tools > Device Manager**, inicia un dispositivo virtual, por ejemplo Pixel 4 con API 31 o superior, o conecta un teléfono con depuración USB.
5. Selecciona el dispositivo y presiona **Run** en la configuración **app**.

La aplicación admite Android 5.0, API 21, o superior. Android Studio configura la ruta del SDK del equipo en `local.properties`.

Para compilar desde la terminal de Android Studio, dentro de esta carpeta:

```bash
./gradlew :app:assembleDebug
```

En Windows utiliza `gradlew.bat :app:assembleDebug`. El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Inicio y clientes

Al abrir la aplicación se muestra brevemente la carga de la carta. Los datos son locales y no requieren una cuenta ni un servidor.

**Inicio** muestra el total de clientes registrados, el total de pedidos confirmados y el nombre del último cliente registrado. Debajo aparecen los clientes con su correo, teléfono, dirección y zona.

El cliente se registra al revisar un pedido válido. Si cancelas la revisión, el cliente permanece registrado, mientras el pedido sigue sin confirmar. Los pedidos se cuentan únicamente al confirmarlos.

Un cliente se identifica por nombre y correo, ignorando mayúsculas y minúsculas. Repetir un pedido con esos datos no crea otro cliente ni cambia quién fue el último registrado. Durante la sesión debe conservar la dirección y la zona del primer registro.

## Elegir pizzas

En **Carta**, usa **+** y **−** para ajustar las cantidades. Puedes combinar hasta 100 pizzas por pedido.

| Pizza | Precio antes de descuentos e IVA |
| --- | ---: |
| Margarita | $8.500 |
| Pepperoni | $9.500 |
| Vegetariana | $9.000 |

La barra inferior muestra la cantidad seleccionada y el total estimado. Pulsa **Ver mi pedido** para completar la entrega.

## Completar y confirmar un pedido

Ingresa nombre, teléfono, correo, dirección, zona y fecha. Las notas de entrega son opcionales.

- El teléfono admite 12 dígitos, con `+` opcional, o el formato `+XX (XXX) XXX-XXXX`. Por ejemplo, `561234567890` se muestra como `+56 (123) 456-7890`.
- El correo debe tener un formato válido, como `ana@ejemplo.cl`.
- La fecha usa `DD/MM/AAAA` y debe existir en el calendario.
- La zona puede ser Norte, Centro o Sur.

**Revisar pedido** muestra el detalle, descuentos, IVA y total. **Seguir editando** vuelve al formulario sin consumir ingredientes. **Confirmar pedido** registra el pedido, descuenta los ingredientes y abre **Pedidos**. Inicio actualiza el resumen automáticamente.

Si falta inventario, se informa qué ingredientes reponer y se conserva el borrador. Vuelve a la carta, entra en Cocina, repón y revisa nuevamente.

## Descuentos y total

Desde cuatro pizzas se aplica un 10% de descuento por volumen. Según la fecha del pedido, del 01/11/2026 al 30/11/2026 se aplica además un 5% promocional a Margarita y un 10% a Vegetariana. Pepperoni no tiene promoción.

Primero se aplica el descuento por volumen y después la promoción de cada variedad sobre el precio descontado. Finalmente se agrega IVA del 19%. Los importes se redondean a dos decimales.

Por ejemplo, dos Margarita, una Pepperoni y una Vegetariana con fecha `23/11/2026` tienen un subtotal de $35.500, descuento por volumen de $3.550, descuento promocional de $1.575, IVA de $5.771,25 y total de **$36.146,25**.

## Seguir pedidos

En **Pedidos** puedes consultar productos, cliente, fecha, total y estado.

| Estado | Botón | Resultado |
| --- | --- | --- |
| En preparación | Marcar listo | Pasa a Listo. |
| Listo | Asignar repartidor | Pasa a En camino y muestra al repartidor. |
| En camino | Confirmar entrega | Pasa a Entregado y libera al repartidor. |
| Entregado | Sin acciones | El pedido está completado. |

Se prioriza un repartidor disponible de la misma zona y después de zonas cercanas. Si todos están ocupados, el pedido permanece Listo. Cambiar de estado no vuelve a descontar ingredientes.

## Gestionar la cocina

**Cocina** muestra ingredientes disponibles, con stock bajo o agotados. Cada botón **+10** agrega diez porciones.

Todas las pizzas consumen una porción de masa, salsa y queso. Pepperoni utiliza además una de pepperoni; Vegetariana, una de champiñones. Los ingredientes se descuentan una sola vez, al confirmar el pedido.

## Datos de la sesión

Los clientes, pedidos e inventario se mantienen en memoria mientras vive el proceso de la aplicación. Al girar el dispositivo se conservan el resumen, los registros, el formulario, las cantidades seleccionadas y la sección abierta. Si Android finaliza el proceso o fuerzas el cierre, los registros se reinician.
