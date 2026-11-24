plugins {
    id("com.android.application") version "8.10.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.21" apply false
    id("org.jetbrains.kotlin.jvm") version "2.2.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21" apply false
}

// Ubicación opcional para compilar cuando el disco interno tiene poco espacio.
val salidaExterna = providers.gradleProperty("veterinaria.buildRoot").orNull
if (salidaExterna != null) {
    layout.buildDirectory.set(file("$salidaExterna/raiz"))
    subprojects { layout.buildDirectory.set(file("$salidaExterna/$name")) }
}
