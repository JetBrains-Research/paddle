import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

group = "io.paddle"
version = "0.1.0"

plugins {
    id("tanvd.kosogor") version "1.0.12" apply true
    kotlin("jvm") version "1.5.10" apply true
}

repositories {
    mavenCentral()
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "11"
        languageVersion = "1.5"
        apiVersion = "1.5"
    }
}
