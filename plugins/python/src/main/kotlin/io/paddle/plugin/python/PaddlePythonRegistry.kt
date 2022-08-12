package io.paddle.plugin.python

import io.paddle.PaddleRegistry
import io.paddle.utils.config.ConfigurationView

object PaddlePythonRegistry : ConfigurationView("python", PaddleRegistry) {
    val autoRemove by bool("autoRemove", false)
}
