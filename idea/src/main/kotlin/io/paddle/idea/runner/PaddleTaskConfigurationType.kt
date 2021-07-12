package io.paddle.idea.runner

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.service.execution.AbstractExternalSystemTaskConfigurationType
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import io.paddle.idea.PaddleManager

class PaddleTaskConfigurationType : AbstractExternalSystemTaskConfigurationType(PaddleManager.ID) {
    override fun getConfigurationFactoryId() = "Paddle"

    override fun isDumbAware(): Boolean {
        return true
    }

    override fun isEditableInDumbMode(): Boolean {
        return true
    }

    override fun doCreateConfiguration(
        externalSystemId: ProjectSystemId,
        project: Project,
        factory: ConfigurationFactory,
        name: String
    ): ExternalSystemRunConfiguration {
        return PaddleRunConfiguration(project, factory, name)
    }

    companion object {
        val instance: PaddleTaskConfigurationType
            get() = ExternalSystemUtil.findConfigurationType(PaddleManager.ID) as PaddleTaskConfigurationType
    }
}
