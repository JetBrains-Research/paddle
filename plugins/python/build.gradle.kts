group = rootProject.group
version = rootProject.version

plugins {
    java
}

dependencies {
    implementation(project(":core"))
    implementation("org.antlr:antlr4-runtime:4.8")
    implementation("javax.mail:mail:1.4.7")
}
