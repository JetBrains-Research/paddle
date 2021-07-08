group = rootProject.group
version = rootProject.version


plugins {
    application apply true
}

dependencies {
    implementation(project(":core"))
    implementation(project(":plugins:python"))
    implementation(project(":plugins:docker"))

    implementation("com.github.ajalt.clikt", "clikt", "3.2.0")

    implementation("ch.qos.logback", "logback-classic", "1.2.3")
}

application {
    mainClass.set("io.paddle.ApplicationKt")
}
