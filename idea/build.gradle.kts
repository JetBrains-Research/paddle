group = rootProject.group
version = rootProject.version

plugins {
    id("org.jetbrains.intellij") version "1.13.2" apply true
}

dependencies {
    implementation(project(":core")) {
        exclude("org.slf4j")
    }
    implementation(project(":cli")) {
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
}

intellij {
    pluginName.set("Paddle")

    type.set("PC")
    version.set("PC-2022.3.1")

    downloadSources.set(true)

    plugins.set(
        listOf(
            "PythonCore:223.7571.182",
            "org.jetbrains.plugins.yaml:223.8214.6"
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
    val test by getting(Test::class) {
        setScanForTestClasses(false)
        // Only run tests from classes that end with "Test"
        include("**/*Test.class")
    }
}
