buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.17")
    }
}

rootProject.name = "paddle"

include(":cli")
include(":idea")
include(":core")
include(":plugins:python")
include(":plugins:docker")
include(":plugins:ssh")
