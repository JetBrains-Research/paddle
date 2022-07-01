package io.paddle.idea.execution

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.service.execution.AbstractExternalSystemTaskConfigurationType
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import io.paddle.idea.PaddleManager

class PaddleTaskConfigurationType : AbstractExternalSystemTaskConfigurationType(PaddleManager.ID) {
    companion object {
        fun getInstance(): PaddleTaskConfigurationType {
            return ExternalSystemUtil.findConfigurationType(PaddleManager.ID) as PaddleTaskConfigurationType
        }
    }

    override fun getConfigurationFactoryId(): String = "Paddle"

    override fun doCreateConfiguration(
        externalSystemId: ProjectSystemId,
        project: Project,
        factory: ConfigurationFactory,
        name: String
    ): ExternalSystemRunConfiguration {
        return PaddleRunConfiguration(project, factory, name)
    }

    override fun isDumbAware(): Boolean = true

    override fun isEditableInDumbMode(): Boolean = true
}
