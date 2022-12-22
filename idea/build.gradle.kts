group = rootProject.group
version = rootProject.version

plugins {
    id("org.jetbrains.intellij") version "1.6.0" apply true
}

dependencies {
    implementation(project(":core")) {
        exclude("org.slf4j")
    }
    implementation(project(":plugins:python")) {
        exclude("org.slf4j")
    }
    implementation(project(":plugins:docker")) {
        exclude("org.slf4j")
    }
    implementation(project(":plugins:ssh")) {
        exclude("org.slf4j")
    }
    implementation("io.kotest:kotest-runner-junit5-jvm:5.4.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-toml:2.14.0")
}

intellij {
    pluginName.set("Paddle")

    type.set("PC")
    version.set("PC-2022.1")

    downloadSources.set(true)

    plugins.set(
        listOf(
            "PythonCore:221.5080.210",
            "org.jetbrains.plugins.yaml:221.5080.126"
        )
    )

    updateSinceUntilBuild.set(false)
}


tasks {
    runIde {
        jvmArgs = listOf("-Xmx1024m")
    }

    publishPlugin {
        token.set(System.getenv("MARKETPLACE_TOKEN") ?: "NONE")
        System.getenv("MARKETPLACE_CHANNEL")?.let {
            channels.set(listOf(it))
        }
    }
}
