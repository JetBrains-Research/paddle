import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask
import tanvd.kosogor.proxy.shadowJar

group = rootProject.group
version = rootProject.version

plugins {
    id("com.github.breadmoirai.github-release") version "2.2.12" apply true
    id("tanvd.kosogor") version "1.0.12" apply true
    id("org.graalvm.buildtools.native") version "0.9.20"
    application
}

application {
    mainClass.set("io.paddle.ApplicationKt")
    @Suppress("DEPRECATION")
    mainClassName = "io.paddle.ApplicationKt" // required by shadowJar
    tasks.run.get().workingDir = rootProject.projectDir.resolve("example")
}


dependencies {
    implementation(project(":core"))
    implementation(project(":plugins:python"))
    implementation(project(":plugins:docker"))
    implementation(project(":plugins:ssh"))

    implementation("com.github.ajalt.clikt", "clikt", "3.2.0")
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("io.insert-koin", "koin-core" ,"3.4.0")
}

val shadowJar = shadowJar {
    jar {
        mainClass = "io.paddle.ApplicationKt"
    }
}.apply {
    task.archiveClassifier.set("all")
    task.archiveBaseName.set("paddle")
}

tasks.withType(GithubReleaseTask::class) {
    dependsOn("shadowJar")
}


// To build a native image, run "./gradlew -Pagent nativeCompile"
graalvmNative {
    binaries {
        named("main") {
            imageName.set("paddle")
            mainClass.set("io.paddle.ApplicationKt")

            debug.set(true)
            verbose.set(true)
            fallback.set(false)

            buildArgs.add(
                "--initialize-at-build-time=" +
                        "org.snakeyaml.engine," +
                        "kotlinx.serialization," +
                        "org.antlr," +
                        "io.ktor," +
                        "kotlinx.coroutines," +
                        "kotlin"
            )

            buildArgs.add("--enable-url-protocols=https")

            buildArgs.add("-H:+InstallExitHandlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("-H:+ReportExceptionStackTraces")

            configurationFileDirectories.from(file("src/main/resources/META-INF/native-agent-config"))
        }
    }
}

tasks.named<BuildNativeImageTask>("nativeCompile") {
    classpathJar.set(shadowJar.task.archiveFile)
}



