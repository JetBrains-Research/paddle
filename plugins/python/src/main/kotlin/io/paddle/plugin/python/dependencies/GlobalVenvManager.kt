package io.paddle.plugin.python.dependencies

import io.paddle.execution.CommandExecutor
import io.paddle.execution.local.LocalCommandExecutor
import io.paddle.plugin.python.Config
import io.paddle.plugin.python.extensions.Requirements
import io.paddle.terminal.Terminal
import io.paddle.terminal.TextOutput
import java.io.IOException
import java.nio.file.Files

/**
 * A service for managing Paddle's internal virtual environment, where all the packages are installed for the first time.
 * Then, it moves them to the corresponding ~/.paddle/cache/<package>/<version> folder.
 */
object GlobalVenvManager {
    private val dummyOutput = TextOutput.Console // TODO: use internal logging instead of TextOutput.Console
    private val executor: CommandExecutor = LocalCommandExecutor(dummyOutput)
    private val terminal: Terminal = Terminal(dummyOutput)

    private val globalVenv: VenvDir

    init {
        if (!Files.exists(Config.venvDir)) {
            val code = executor.execute(
                command = "python3",
                args = listOf("-m", "venv", Config.venvDir.toAbsolutePath().toString()),
                workingDir = Config.paddleHome.toFile(),
                terminal = terminal
            )
            if (code != 0) {
                error("Failed to create Paddle's internal virtualenv. Check your python installation.")
            }
        }
        globalVenv = VenvDir(Config.venvDir.toFile())
    }

    fun install(dependency: Requirements.Descriptor): Int {
        val code = executor.execute(
            command = "${Config.venvDir}/bin/pip",
            args = listOf("install", "${dependency.name}==${dependency.version}"),
            workingDir = Config.paddleHome.toFile(),
            terminal = terminal
        )
        if (code == 0) {
            copyToGlobalCache(dependency)
        }
        return code
    }

    private fun copyToGlobalCache(dependency: Requirements.Descriptor) {
        val packageToCopy = globalVenv.sitePackages.resolve(dependency.name)
        try {
            packageToCopy.copyRecursively(
                target = Config.cacheDir.resolve(dependency.name).resolve(dependency.version).toFile(),
                overwrite = true
            )
        } catch (ex: IOException) {
            error("Some problems during caching the installed ${dependency.name}-${dependency.version} package.")
        }
    }
}