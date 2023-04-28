package io.paddle.idea.execution

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.ConfigurationFromContext
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.externalSystem.service.execution.*
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyUtil
import com.jetbrains.python.psi.impl.getIfStatementByIfKeyword
import com.jetbrains.python.run.PythonRunConfigurationProducer
import com.jetbrains.python.sdk.basePath
import io.paddle.idea.PaddleManager

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
        val module = context.location?.module ?: return false

        val element = context.location?.psiElement ?: return false
        if (element.isMainClauseOnTopLevel() || element is PyFile) {
            configuration.settings.taskNames = listOf("testTask")
            configuration.settings.externalProjectPath = module.basePath
            configuration.name = AbstractExternalSystemTaskConfigurationType.generateName(module.project, configuration.settings)
            return true
        }
        return false
    }

    override fun isConfigurationFromContext(configuration: ExternalSystemRunConfiguration, context: ConfigurationContext): Boolean {
        if (context.location is ExternalSystemTaskLocation) {
            return super.isConfigurationFromContext(configuration, context)
        }

        if (configuration.settings.externalSystemId != PaddleManager.ID || configuration !is PaddleRunConfiguration) return false
        return true
    }

    override fun isPreferredConfiguration(self: ConfigurationFromContext?, other: ConfigurationFromContext): Boolean {
        return true
    }

    private fun PsiElement.isMainClauseOnTopLevel(): Boolean {
        if (node.elementType != PyTokenTypes.IF_KEYWORD) {
            return false
        }
        val statement = getIfStatementByIfKeyword(this) ?: return false
        val containingFile = statement as? PyFile ?: return false
        return containingFile.virtualFile.fileType == PythonFileType.INSTANCE && PyUtil.isIfNameEqualsMain(statement)
    }

    override fun shouldReplace(self: ConfigurationFromContext, other: ConfigurationFromContext): Boolean {
        return other.isProducedBy(PythonRunConfigurationProducer::class.java)
    }
}
