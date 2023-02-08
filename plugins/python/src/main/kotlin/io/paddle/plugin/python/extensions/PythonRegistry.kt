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

    // FIXME: bool(String, Boolean?) doesn't work. There is a workaround
    val autoRemove by lazy {
        get<String>("autoRemove")?.toBoolean() ?: false
    }
    val noCacheDir by lazy {
        get<String>("noCacheDir")?.toBoolean() ?: false
    } //bool("noCacheDir", false)
    val autoRetry by lazy {
        get<String>("autoRetry")?.toBoolean() ?: true
    }
}
