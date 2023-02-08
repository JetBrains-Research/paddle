group = "io.paddle"
version = "0.4.8"

plugins {
    kotlin("jvm") version "1.7.10" apply false
}

subprojects {
    apply {
        plugin("kotlin")
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = "11"
            languageVersion = "1.7"
            apiVersion = "1.7"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }
}

