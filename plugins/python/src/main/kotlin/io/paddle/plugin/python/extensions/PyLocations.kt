package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.utils.deepResolve
import io.paddle.plugin.standard.extensions.locations
import io.paddle.project.PaddleProject
import io.paddle.utils.exists
import io.paddle.utils.ext.Extendable
import java.nio.file.Path
import kotlin.io.path.createDirectories

val PaddleProject.pyLocations: PyLocations
    get() = extensions.getOrFail(PyLocations.Extension.key)

class PyLocations private constructor(val project: PaddleProject) {
    object Extension : PaddleProject.Extension<PyLocations> {
        override val key: Extendable.Key<PyLocations> = Extendable.Key()

        override fun create(project: PaddleProject) = PyLocations(project)
    }

    /**
     * A main storage of cached Python packages, structured by repo - name - version.
     *
     * Used mainly by [GlobalCacheRepository].
     */
    val packagesDir: Path
        get() = project.locations.paddleHome.resolve("packages")
            .apply { if (!exists()) createDirectories() }

    /**
     * A path to the internal directory which contains temporary virtual environments for Paddle projects.
     *
     * Such environments are used to install all new packages since python's venv
     * does not support installation of the multiple versions for a single package to the same environment.
     *
     * Then, the installed packages are copied to the [PyLocations.packagesDir].
     */
    val venvsDir: Path
        get() = project.locations.paddleHome.resolve("venvs")
            .apply { if (!exists()) createDirectories() }

    /**
     * A directory to store indexes (in JSON format) of PyPI repos.
     *
     * Used by autocompletion in PyCharm IDE plugin.
     */
    val indexDir: Path
        get() = project.locations.paddleHome.resolve("index")
            .apply { if (!exists()) createDirectories() }

    /**
     * A directory to store installed and cached Python interpreters.
     */
    val interpretersDir: Path
        get() = project.locations.paddleHome.resolve("interpreters")
            .apply { if (!exists()) createDirectories() }

    val distResolverCachePath: Path
        get() = project.locations.paddleHome.deepResolve("cache", "distResolverCache.json")

    val pipResolverCachePath: Path
        get() = project.locations.paddleHome.deepResolve("cache", "pipResolverCache.json")

    val profiles: Path
        get() = project.locations.paddleHome.resolve("profiles.yaml")
}

