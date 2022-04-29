group = rootProject.group
version = rootProject.version

plugins {
    kotlin("plugin.serialization")
    java
}

dependencies {
    implementation(project(":core"))
    implementation(project(":plugins:python"))

    implementation("org.yaml:snakeyaml:1.30")
    implementation("org.snakeyaml:snakeyaml-engine:2.3")
    implementation("org.antlr:antlr4-runtime:4.8")
}
