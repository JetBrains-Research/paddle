import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import tanvd.kosogor.proxy.shadowJar

group = rootProject.group
version = rootProject.version


plugins {
    id("com.github.breadmoirai.github-release") version "2.2.12" apply true
}

dependencies {
    implementation(project(":core"))
    implementation(project(":plugins:python"))
    implementation(project(":plugins:docker"))
    implementation(project(":plugins:ssh"))

    implementation("com.github.ajalt.clikt", "clikt", "3.2.0")

    implementation("ch.qos.logback", "logback-classic", "1.2.3")
}

val shadowJar = shadowJar {
    jar {
        mainClass = "io.paddle.ApplicationKt"
    }
}.apply {
    task.archiveClassifier.set("")
    task.archiveBaseName.set("paddle")

    task.from(file("src/main/resources/version.txt").apply {
        if (!exists()) {
            parentFile.mkdirs()
            createNewFile()
        }

        writeText(project.version.toString())
    })
}

githubRelease {
    token(System.getenv("GITHUB_TOKEN"))
    owner("tanvd")
    repo("paddle")
    targetCommitish("main")
    releaseAssets(shadowJar.task.archiveFile.get())
}

tasks.withType(GithubReleaseTask::class) {
    dependsOn("shadowJar")
}
