group = rootProject.group
version = rootProject.version

plugins {
    kotlin("plugin.serialization") version "1.7.10" apply true
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation("org.codehaus.plexus:plexus-utils:3.4.2")
    implementation("commons-codec:commons-codec:1.15")
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
    implementation("com.charleskorn.kaml:kaml:0.46.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.yaml:snakeyaml:1.30")
    implementation("org.snakeyaml:snakeyaml-engine:2.3")
    implementation(kotlin("reflect"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}