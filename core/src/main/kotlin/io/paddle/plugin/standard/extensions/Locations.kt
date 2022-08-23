package io.paddle.plugin.standard.extensions

import io.paddle.project.PaddleProject
import io.paddle.utils.exists
import io.paddle.utils.ext.Extendable
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile

val PaddleProject.locations: Locations
    get() = extensions.getOrFail(Locations.Extension.key)

class Locations private constructor(val project: PaddleProject) {
    object Extension : PaddleProject.Extension<Locations> {
        override val key: Extendable.Key<Locations> = Extendable.Key()

        override fun create(project: PaddleProject) = Locations(project)
    }

    val paddleHome: Path
        get() {
            val fromEnv = project.executor.env.get("PADDLE_HOME")?.let { Path.of(it) }
            val result = fromEnv ?: Paths.get(project.executor.os.userHome, ".paddle")
            if (!result.exists()) result.createDirectories()
            return result
        }

    val registry: Path
        get() = paddleHome.resolve("registry.yaml")
            .apply { if (!exists()) createFile() }
}
