# Clínica Veterinaria

Aplicación Android para registrar dueños, mantener las fichas de sus mascotas y organizar consultas veterinarias. Permite seleccionar servicios, conocer el costo total y seguir cada atención desde su programación hasta su finalización.

## Ejecutar en Android Studio

1. En Android Studio selecciona **Open** y abre esta carpeta **VeterinariaApp**, donde están `settings.gradle.kts` y `gradlew`.
2. Selecciona Java 17 o 21 en **Gradle JDK** y espera la sincronización de Gradle. La primera sincronización necesita Internet para descargar dependencias.
3. En **SDK Manager**, instala Android SDK Platform 35, Build Tools 35.0.0, Platform Tools y Android Emulator.
4. En **Tools > Device Manager**, inicia un dispositivo virtual, por ejemplo Pixel 4 con API 31 o superior. También puedes conectar un teléfono con depuración USB.
5. Selecciona el dispositivo y presiona **Run** en la configuración **app**.

La aplicación admite Android 5.0, API 21, o superior. Android Studio guarda la ruta del SDK de cada equipo en `local.properties`.

Para compilar desde la terminal de Android Studio, dentro de esta carpeta:

```bash
./gradlew :app:assembleDebug
```

En Windows utiliza `gradlew.bat :app:assembleDebug`. El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Inicio

Al abrir la aplicación se carga el catálogo local de servicios. Inicio muestra el total de clientes registrados, el total de consultas registradas, el nombre del último cliente y la cantidad de mascotas. Los clientes corresponden a los dueños y cada consulta es un pedido de atención.

Los números cambian automáticamente al registrar un dueño, una mascota o una consulta. Cambiar el estado de una atención no crea una consulta adicional.

## Registrar dueños

En **Dueños**, pulsa **Registrar dueño** e ingresa nombre, teléfono y correo electrónico. Los tres campos son obligatorios.

El teléfono acepta entre 7 y 15 dígitos, con `+` opcional y separadores. Por ejemplo, `+56 9 1234 5678` se guarda como `+56912345678`. El correo debe tener un formato válido, como `ana@ejemplo.cl`.

El correo identifica a cada dueño, ignorando mayúsculas y minúsculas. Guardar nuevamente los mismos datos no duplica el registro ni cambia el último cliente. Si ese correo ya está asociado a otro nombre o teléfono, se muestra un aviso.

## Registrar mascotas

Primero registra al dueño. Después, en **Mascotas**, pulsa **Registrar mascota** y completa:

- Dueño responsable, elegido entre los clientes registrados.
- Nombre de la mascota.
- Especie: Perro, Gato, Conejo u Otro.
- Raza, opcional.
- Edad en años, entre 0 y 40. Usa 0 si aún no cumple un año.

**Guardar mascota** crea su ficha y muestra el dueño asociado. No se permite repetir una ficha con el mismo dueño, nombre y especie.

## Agendar consultas

En **Consultas**, pulsa **Agendar consulta**. También puedes hacerlo desde la ficha de una mascota.

Elige la mascota, ingresa una fecha válida en formato `DD/MM/AAAA` y escribe el motivo de la atención. Selecciona al menos un servicio:

| Servicio | Precio final |
| --- | ---: |
| Consulta general | $18.000 |
| Vacunación | $22.000 |
| Control preventivo | $15.000 |

El total suma los servicios seleccionados y se actualiza al marcar o desmarcar opciones. Por ejemplo, Consulta general y Vacunación suman **$40.000**.

**Registrar consulta** guarda la atención con su mascota, dueño, fecha, motivo, servicios y total. Volver sin registrar descarta el formulario y no incrementa el resumen.

## Seguir la atención

La sección **Consultas** presenta primero los registros más recientes.

| Estado | Acción | Resultado |
| --- | --- | --- |
| Programada | Iniciar atención | Cambia a En atención. |
| En atención | Finalizar consulta | Cambia a Finalizada. |
| Finalizada | Sin acciones | La atención está completada. |

Si un dato es inválido, aparece un mensaje para corregirlo y no se guarda un registro incompleto.

## Datos de la sesión

Los datos son locales y se mantienen en memoria mientras vive el proceso de la aplicación. Al girar el dispositivo se conservan los registros, el resumen, la sección abierta y los campos del formulario que estés completando. Si Android finaliza el proceso o fuerzas el cierre, los registros se reinician.
