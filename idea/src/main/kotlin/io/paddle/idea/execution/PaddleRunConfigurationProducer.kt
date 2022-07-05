package io.paddle.idea.execution

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.ConfigurationFromContext
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.externalSystem.service.execution.*
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.jetbrains.python.sdk.basePath
import io.paddle.idea.PaddleManager
import org.jetbrains.yaml.psi.YAMLKeyValue

class PaddleRunConfigurationProducer : AbstractExternalSystemRunConfigurationProducer() {
    override fun getConfigurationFactory(): ConfigurationFactory {
        return PaddleTaskConfigurationType.getInstance().factory
    }

    override fun setupConfigurationFromContext(
        configuration: ExternalSystemRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        if (context.location is ExternalSystemTaskLocation) {
            return super.setupConfigurationFromContext(configuration, context, sourceElement)
        }

        if (configuration.settings.externalSystemId != PaddleManager.ID || configuration !is PaddleRunConfiguration) return false
        if (context.location?.psiElement?.parent !is YAMLKeyValue) return false
        val runTaskId = (context.location?.psiElement?.parent as YAMLKeyValue).value?.text ?: return false
        val module = context.location?.module ?: return false

        configuration.settings.taskNames = listOf("run$$runTaskId")
        configuration.settings.externalProjectPath = module.basePath
        configuration.name = AbstractExternalSystemTaskConfigurationType.generateName(module.project, configuration.settings)

        return true
    }

    override fun isConfigurationFromContext(configuration: ExternalSystemRunConfiguration, context: ConfigurationContext): Boolean {
        if (context.location is ExternalSystemTaskLocation) {
            return super.isConfigurationFromContext(configuration, context)
        }

        if (configuration.settings.externalSystemId != PaddleManager.ID || configuration !is PaddleRunConfiguration) return false
        val module = context.location?.module ?: return false
        if (configuration.settings.externalProjectPath != module.basePath) return false

        if (context.location?.psiElement?.parent !is YAMLKeyValue) return false
        val runTaskId = (context.location?.psiElement?.parent as YAMLKeyValue).value?.text ?: return false
        val taskNames = configuration.settings.taskNames.takeIf { it.isNotEmpty() } ?: return false
        if (taskNames.first() != "run$$runTaskId") return false

        return true
    }

    override fun isPreferredConfiguration(self: ConfigurationFromContext?, other: ConfigurationFromContext): Boolean {
        return other.isProducedBy(PaddleRunConfigurationProducer::class.java)
    }
}
