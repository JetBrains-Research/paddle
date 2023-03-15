package io.paddle.plugin.standard.extensions

import io.paddle.project.PaddleProject
import io.paddle.utils.config.PaddleApplicationSettings
import io.paddle.utils.ext.Extendable
import java.nio.file.Path

val PaddleProject.locations: Locations
    get() = extensions.getOrFail(Locations.Extension.key)

class Locations private constructor(val project: PaddleProject) {
    object Extension : PaddleProject.Extension<Locations> {
        override val key: Extendable.Key<Locations> = Extendable.Key()

        override fun create(project: PaddleProject) = Locations(project)
    }

    val paddleHome: Path
        get() = PaddleApplicationSettings.paddleHome

    val registry: Path
        get() = PaddleApplicationSettings.registry
}
