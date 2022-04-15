package io.paddle.plugin.python.extensions

import io.paddle.execution.ExecutionResult
import io.paddle.plugin.python.dependencies.setup.SetupConfig
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.*
import java.io.File
import kotlin.io.path.absolutePathString

val PaddleProject.buildEnvironment: BuildEnvironment
    get() = this.extensions.get(BuildEnvironment.Extension.key)!!

class BuildEnvironment(val project: PaddleProject) : Hashable {
    val distDir: File
        get() = project.workDir.resolve("dist")

    val pyprojectToml: File
        get() = project.workDir.resolve("pyproject.toml")

    val setupCfg: File
        get() = project.workDir.resolve("setup.cfg")

    object Extension : PaddleProject.Extension<BuildEnvironment> {
        override val key: Extendable.Key<BuildEnvironment> = Extendable.Key()

        override fun create(project: PaddleProject): BuildEnvironment {
            return BuildEnvironment(project)
        }
    }

    fun build(): ExecutionResult {
        if (!pyprojectToml.exists()) {
            project.terminal.info("Creating a new ${pyprojectToml.relativeTo(project.workDir).path} file...")
            pyprojectToml.createNewFile()
            pyprojectToml.writeText(
                listOf(
                    "[build-system]",
                    "requires = [\"setuptools>=42\"]",
                    "build-backend = \"setuptools.build_meta\"",
                ).joinToString("\n")
            )
        } else {
            project.terminal.info("Using existing ${pyprojectToml.relativeTo(project.workDir).path} file...")
            val src = pyprojectToml.readText()
            if (!src.contains("[build-system]") || !src.contains("build-backend = \"setuptools.build_meta\"")) {
                throw Task.ActException("Build backend was not configured properly in pyproject.toml")
            }
        }

        if (!setupCfg.exists()) {
            project.terminal.info("Creating a new ${setupCfg.relativeTo(project.workDir).path} file...")
            SetupConfig(project).also { it.create(setupCfg) }
        } else {
            project.terminal.info("Using existing ${setupCfg.relativeTo(project.workDir).path} file...")
        }

        return project.executor.execute(
            project.environment.interpreterPath.absolutePathString(),
            listOf("-m", "pip", "install", "--upgrade", "build"),
            project.workDir,
            project.terminal
        ).then {
            project.executor.execute(
                project.environment.interpreterPath.absolutePathString(),
                listOf("-m", "build"),
                project.workDir,
                project.terminal
            )
        }
    }

    override fun hash(): String {
        return AggregatedHashable(listOf(pyprojectToml.hashable(), setupCfg.hashable())).hash()
    }
}
