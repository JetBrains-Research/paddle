group = rootProject.group
version = rootProject.version

plugins {
    id("org.jetbrains.intellij") version "1.1.2" apply true
    kotlin("plugin.serialization")
    java
}

dependencies {
    implementation(project(":core"))
    implementation("org.antlr:antlr4-runtime:4.8")
    implementation("javax.mail:mail:1.4.7")
    implementation("org.jsoup:jsoup:1.14.2")
    implementation("io.ktor:ktor-client-core:1.6.3")
    implementation("io.ktor:ktor-client-cio:1.6.3")
}

intellij {
    type.set("IC")
    version.set("IC-2021.1.3")

    plugins.set(
        listOf(
            "PythonCore:211.7628.24",
            "ru.meanmail.plugin.requirements:2021.4.1-211",
            "com.intellij.java"
        )
    )
}
