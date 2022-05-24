package io.paddle.idea.execution

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.externalSystem.service.execution.AbstractExternalSystemRunConfigurationProducer

class PaddleRuntimeConfigurationProducer : AbstractExternalSystemRunConfigurationProducer() {
    override fun getConfigurationFactory(): ConfigurationFactory {
        return PaddleExternalTaskConfigurationType.getInstance().factory
    }
}
