package io.paddle.plugin.python.extensions

import io.paddle.plugin.standard.extensions.registry
import io.paddle.project.PaddleProject
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable

val PaddleProject.pythonRegistry: PythonRegistry
    get() = extensions.getOrFail(PythonRegistry.Extension.key)

class PythonRegistry(project: PaddleProject) : ConfigurationView("python", project.registry) {
    object Extension : PaddleProject.Extension<PythonRegistry> {
        override val key: Extendable.Key<PythonRegistry> = Extendable.Key()

        override fun create(project: PaddleProject) = PythonRegistry(project)
    }

    val autoRemove by bool("autoRemove", false)
    val usePipCache by bool("usePipCache", true)
    val autoRetry by bool("autoRetry", true)
}
