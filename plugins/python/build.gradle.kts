group = rootProject.group
version = rootProject.version

plugins {
    kotlin("plugin.serialization") version "1.6.21" apply true
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(project(":core"))

    implementation("org.antlr:antlr4-runtime:4.10.1")
    implementation("javax.mail:mail:1.4.7")

    implementation("org.jsoup:jsoup:1.15.2")
    implementation("io.ktor:ktor-client-core:2.0.3")
    implementation("io.ktor:ktor-client-cio:2.0.3")
    implementation("io.ktor:ktor-client-auth:2.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    implementation("org.yaml:snakeyaml:1.30")
    implementation("org.snakeyaml:snakeyaml-engine:2.3")
    implementation("org.ini4j:ini4j:0.5.4")

    implementation("org.codehaus.plexus:plexus-archiver:4.4.0")
    implementation("org.codehaus.plexus:plexus-utils:3.4.2")

    implementation("com.github.javakeyring:java-keyring:1.0.1")
    implementation("io.insert-koin:koin-core:3.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("io.kotest:kotest-runner-junit5:5.4.1")
    testImplementation("io.kotest:kotest-assertions-core:5.4.1")
    testImplementation("io.kotest:kotest-property:5.4.1")
    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:1.3.4")
    testImplementation("org.testcontainers:testcontainers:1.17.6")
    testImplementation("org.testcontainers:junit-jupiter:1.17.6")
    testImplementation("ch.qos.logback:logback-classic:1.4.6")
    testImplementation("com.github.docker-java:docker-java:3.3.0")
    testImplementation("io.insert-koin:koin-test:3.4.0")
    testImplementation("io.insert-koin:koin-test-junit5:3.4.0")


}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
