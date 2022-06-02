package io.paddle.idea.execution

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.externalSystem.service.project.settings.RunConfigurationImporter
import com.intellij.openapi.project.Project

class PaddleRunConfigurationImporter : RunConfigurationImporter {
    override fun canImport(typeName: String): Boolean = typeName == "paddle"

    override fun getConfigurationFactory(): ConfigurationFactory = PaddleTaskConfigurationType.getInstance().factory

    override fun process(
        project: Project,
        runConfiguration: RunConfiguration,
        cfg: MutableMap<String, Any>,
        modelsProvider: IdeModifiableModelsProvider
    ) {
    }
}
