package io.paddle.plugin.python

import io.paddle.plugin.python.utils.deepResolve
import io.paddle.utils.exists
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

object PyLocations {
    /**
     * A home directory for Paddle. Contains installed package caches and internal virtual environment. Planning to support caching wheels, etc.
     */
    val paddleHome: Path = System.getenv("PADDLE_HOME")?.let { Path.of(it) } ?: Paths.get(System.getProperty("user.home"), ".paddle")
        get() = field.also { if (!field.exists()) field.toFile().mkdirs() }

    /**
     * A path to the main packages cache directory.
     * It has the following structure:
     *
     * ```
     *   $PADDLE_HOME/
     *       packages/
     *           repo_name_hash/
     *               package_name/
     *                   version_1/
     *                       package_name/
     *                       package_name-version_1.dist-info/
     *                       ...
     *                   version_2/
     *                       package_name/
     *                       package_name-version_2.dist-info/
     *                       ...
     *               another_package_name/
     *                   ...
     * ```
     */
    val packagesDir: Path = paddleHome.resolve("packages")

    /**
     * A path to the internal venvs directory which contains virtual environment per project.
     *
     * Such an environment is used to install all new packages since python's venv
     * do not support installation of the multiple versions for a single package to the same environment.
     */
    val venvsDir: Path = paddleHome.resolve("venvs")

    val indexDir: Path = paddleHome.resolve("index")
        get() = field.also { if (!field.exists()) field.toFile().mkdirs() }

    val interpretersDir: Path = paddleHome.resolve("interpreters")
        get() = field.also { if (!field.exists()) field.toFile().mkdirs() }

    val distResolverCachePath: Path = paddleHome.deepResolve("cache", "distResolverCache.json")

    val pipResolverCachePath: Path = paddleHome.deepResolve("cache", "pipResolverCache.json")

    val globalConfig: File = paddleHome.resolve("config.yaml").toFile()
}

