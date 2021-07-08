plugins {
    id("org.jetbrains.intellij") version "1.1.2" apply true
}

dependencies {
    implementation(project(":core")) {
        exclude("org.slf4j")
    }
    implementation(project(":plugins:python")){
        exclude("org.slf4j")
    }
    implementation(project(":plugins:docker")){
        exclude("org.slf4j")
    }
}

intellij {
    pluginName.set("Paddle")

//    type.set("PC")
//    version.set("PC-2021.1.3")

    downloadSources.set(true)
}
