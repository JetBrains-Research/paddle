package io.paddle.plugin.python.extensions

import io.paddle.project.PaddleProject
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.*
import java.io.File

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

    override fun hash(): String {
        return AggregatedHashable(listOf(pyprojectToml.hashable(), setupCfg.hashable())).hash()
    }
}
