import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

group = "io.paddle"
version = "0.2.0"

val kotlinVersion: String by project

plugins {
    id("tanvd.kosogor") version "1.0.12" apply true
    kotlin("jvm") apply false
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("tanvd.kosogor")
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = "11"
            languageVersion = "1.5"
            apiVersion = "1.5"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }
}

