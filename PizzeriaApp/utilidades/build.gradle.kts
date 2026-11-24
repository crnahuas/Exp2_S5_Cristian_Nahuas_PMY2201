import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { id("org.jetbrains.kotlin.jvm") }

kotlin { compilerOptions.jvmTarget.set(JvmTarget.JVM_17) }
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
tasks.test { useJUnitPlatform() }
