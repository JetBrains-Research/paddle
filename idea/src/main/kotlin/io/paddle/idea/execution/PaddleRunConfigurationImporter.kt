package io.paddle.idea.execution

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.externalSystem.service.project.settings.RunConfigurationImporter
import com.intellij.openapi.project.Project
import com.intellij.util.ObjectUtils

class PaddleRunConfigurationImporter : RunConfigurationImporter {
    override fun canImport(typeName: String): Boolean = typeName == "paddle"

    override fun getConfigurationFactory(): ConfigurationFactory = PaddleExternalTaskConfigurationType.getInstance().factory

    override fun process(
        project: Project,
        runConfiguration: RunConfiguration,
        cfg: MutableMap<String, Any>,
        modelsProvider: IdeModifiableModelsProvider
    ) {
        if (runConfiguration !is PaddleRunConfiguration) {
            return
        }

        val settings = runConfiguration.settings

        ObjectUtils.consumeIfCast(cfg["projectPath"], String::class.java) { settings.externalProjectPath = it }
        ObjectUtils.consumeIfCast(cfg["taskNames"], List::class.java) { settings.taskNames = it as List<String> }
        ObjectUtils.consumeIfCast(cfg["envs"], Map::class.java) { settings.env = it as Map<String, String> }
        ObjectUtils.consumeIfCast(cfg["jvmArgs"], String::class.java) { settings.vmOptions = it }
        ObjectUtils.consumeIfCast(cfg["scriptParameters"], String::class.java) { settings.scriptParameters = it }
    }
}
