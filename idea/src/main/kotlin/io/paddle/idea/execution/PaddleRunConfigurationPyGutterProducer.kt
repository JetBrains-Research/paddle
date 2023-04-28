package io.paddle.idea.execution

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.ConfigurationFromContext
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.externalSystem.service.execution.*
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import io.paddle.idea.PaddleManager

class PaddleRunConfigurationPyGutterProducer : AbstractExternalSystemRunConfigurationProducer() {
    override fun getConfigurationFactory(): ConfigurationFactory {
        return PaddleTaskConfigurationType.getInstance().factory
    }

    override fun setupConfigurationFromContext(
        configuration: ExternalSystemRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean = false

    override fun isConfigurationFromContext(configuration: ExternalSystemRunConfiguration, context: ConfigurationContext): Boolean {
        if (context.location is ExternalSystemTaskLocation) {
            return super.isConfigurationFromContext(configuration, context)
        }

        if (configuration.settings.externalSystemId != PaddleManager.ID || configuration !is PaddleRunConfiguration) return false
        return true
//        val module = context.location?.module ?: return false
//        if (configuration.settings.externalProjectPath != module.basePath) return false
//        val location = context.location?.psiElement ?: return false
//        if (!location.isMainClauseOnTopLevel()) return false
//
//        val taskNames = configuration.settings.taskNames.takeIf { it.isNotEmpty() } ?: return false
//        val moduleDir = configuration.settings.externalProjectPath?.let { File(it) } ?: return false
//        val rootDir = configuration.project.basePath?.let { File(it) } ?: return false
//        val paddleProject = PaddleProjectProvider.getInstance(rootDir).getProject(moduleDir) ?: return false
//        val task = paddleProject.tasks.get(taskNames.first()) ?: return false
//        if (task.group != TaskDefaultGroups.RUN) return false
//        val runTask = task as RunTask
//        val entrypoint = paddleProject.roots.sources.resolve(runTask.entrypoint)
//        return !runTask.isModuleMode
    }

    override fun isPreferredConfiguration(self: ConfigurationFromContext?, other: ConfigurationFromContext): Boolean {
        return true
    }


}
