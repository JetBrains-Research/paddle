package io.paddle.idea.execution

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.service.execution.AbstractExternalSystemTaskConfigurationType
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import io.paddle.idea.PaddleManager

class PaddleExternalTaskConfigurationType : AbstractExternalSystemTaskConfigurationType(PaddleManager.ID) {
    companion object {
        fun getInstance(): PaddleExternalTaskConfigurationType {
            return ExternalSystemUtil.findConfigurationType(PaddleManager.ID) as PaddleExternalTaskConfigurationType
        }
    }

    override fun doCreateConfiguration(
        externalSystemId: ProjectSystemId,
        project: Project,
        factory: ConfigurationFactory,
        name: String
    ): PaddleRunConfiguration {
        return PaddleRunConfiguration(project, factory, name)
    }

    override fun getConfigurationFactoryId(): String = "Paddle"

    override fun isDumbAware(): Boolean = true

    override fun isEditableInDumbMode(): Boolean = true
}
