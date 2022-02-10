rootProject.name = "paddle"

include(":cli")
include(":idea")
include(":core")
include(":plugins:python")
include(":plugins:docker")
include(":plugins:ssh")

pluginManagement {
    val kotlinVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }
}

