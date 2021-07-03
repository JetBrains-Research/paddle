group = rootProject.group
version = rootProject.version


plugins {
    application apply true
}

dependencies {
    implementation(project(":core"))
    implementation(project(":plugins:python"))

    implementation("com.github.ajalt.clikt", "clikt", "3.2.0")
}

application {
    mainClass.set("io.paddle.ApplicationKt")
}
