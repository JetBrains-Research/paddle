package io.paddle.plugin.python.extensions

import io.paddle.project.PaddleProject
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable


val PaddleProject.pythonCliConfig: PythonCliConfig
    get() = extensions.getOrFail(PythonCliConfig.Extension.key)

class PythonCliConfig(project: PaddleProject): ConfigurationView("python", project.cliConfig) {
    object Extension : PaddleProject.Extension<PythonCliConfig> {
        override val key: Extendable.Key<PythonCliConfig> = Extendable.Key()

        override fun create(project: PaddleProject) = PythonCliConfig(project)
    }
}
