import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

group = "io.paddle"
version = "0.1.0"

plugins {
    id("tanvd.kosogor") version "1.0.12" apply true
    kotlin("jvm") version "1.5.10" apply true
    kotlin("plugin.serialization") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.codehaus.plexus", "plexus-utils", "3.3.0")
    implementation("commons-codec", "commons-codec", "1.15")

    implementation("com.github.ajalt.clikt", "clikt", "3.2.0")

    implementation("com.charleskorn.kaml", "kaml", "0.34.0")
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.2.1")
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "11"
        languageVersion = "1.5"
        apiVersion = "1.5"
    }
}
