group = rootProject.group
version = rootProject.version

plugins {
    kotlin("plugin.serialization") version "1.4.31" apply true
}

dependencies {
    implementation("org.codehaus.plexus", "plexus-utils", "3.3.0")
    implementation("commons-codec", "commons-codec", "1.15")

    implementation("com.github.ajalt.clikt", "clikt", "3.2.0")

    implementation("org.snakeyaml", "snakeyaml-engine", "2.3")

    implementation("com.charleskorn.kaml", "kaml", "0.34.0")
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.2.1")

    implementation(kotlin("reflect"))
}
