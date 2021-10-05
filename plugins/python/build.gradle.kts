group = rootProject.group
version = rootProject.version

plugins {
    id("org.jetbrains.intellij") version "1.1.2" apply true
    id("application")
    kotlin("plugin.serialization")
    java
}

application {
    mainClass.set("io.paddle.plugin.python.dependencies.index.PyPackagesRepositoryIndexerKt")
}

tasks {
    register("index") {
        dependsOn("run")
    }
}

dependencies {
    implementation(project(":core"))

    implementation("org.antlr:antlr4-runtime:4.8")
    implementation("javax.mail:mail:1.4.7")

    implementation("org.jsoup:jsoup:1.14.2")
    implementation("io.ktor:ktor-client-core:1.6.3")
    implementation("io.ktor:ktor-client-cio:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.3.0")

    implementation("org.slf4j:slf4j-api:2.0.0-alpha5")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha5")
}

intellij {
    type.set("IC")
    version.set("IC-2021.1.3")

    plugins.set(
        listOf(
            "PythonCore:211.7628.24",
            "org.jetbrains.plugins.yaml:211.7142.37",
            "ru.meanmail.plugin.requirements:2021.4.1-211",
            "com.intellij.java"
        )
    )
}
