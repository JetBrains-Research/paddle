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
    implementation(project(":plugins:migration")) {
        exclude("org.slf4j")
    }
}

intellij {
    pluginName.set("Paddle")

    type.set("PC")
    version.set("PC-2022.1.1")

    downloadSources.set(true)

    plugins.set(
        listOf(
            "PythonCore:221.5080.210",
            "org.jetbrains.plugins.yaml:221.5080.126"
        )
    )

    // updateSinceUntilBuild.set(false)
}

tasks {
    runIde {
        jvmArgs = listOf("-Xmx1024m")
    }
}
