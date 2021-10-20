group = rootProject.group
version = rootProject.version

plugins {
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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")

    implementation("org.slf4j:slf4j-api:2.0.0-alpha5")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha5")
}
