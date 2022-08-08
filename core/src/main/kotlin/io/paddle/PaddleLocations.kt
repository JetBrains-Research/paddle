package io.paddle

import io.paddle.utils.exists
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createFile

object PaddleLocations {
    val paddleHome: Path = System.getenv("PADDLE_HOME")?.let { Path.of(it) } ?: Paths.get(System.getProperty("user.home"), ".paddle")
        get() = field.also { if (!field.exists()) field.toFile().mkdirs() }


    val registry: Path = paddleHome.resolve("registry.yaml")
        get() = field.also { if (!field.exists()) field.createFile() }
}
