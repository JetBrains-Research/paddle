package io.paddle.idea.execution

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.ConfigurationFromContext
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.externalSystem.service.execution.*
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyUtil
import com.jetbrains.python.psi.impl.getIfStatementByIfKeyword
import com.jetbrains.python.run.PythonRunConfigurationProducer
import com.jetbrains.python.sdk.basePath
import io.paddle.idea.PaddleManager
import io.paddle.idea.utils.getSuperParent
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
        val module = context.location?.module ?: return false

        val element = context.location?.psiElement ?: return false
        if (element.isMainClauseOnTopLevel()) {
            return true
        }
        if (element.parent !is YAMLKeyValue) {
            return false
        }
        val taskId = (context.location?.psiElement?.parent as YAMLKeyValue?)?.value?.text ?: return false

        configuration.settings.taskNames = listOf(
            when {
                element.getSuperParent(5)?.text?.startsWith("pytest") ?: false -> taskId
                element.getSuperParent(5)?.text?.startsWith("run") ?: false -> taskId
                element.text.startsWith("twine") -> "twine"
                element.text.startsWith("requirements") -> "install"
                else -> return false
            }
        )
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
        val element = context.location?.psiElement ?: return false
        if (configuration.settings.externalProjectPath != module.basePath) return false

        val taskNames = configuration.settings.taskNames.takeIf { it.isNotEmpty() } ?: return false

        if (element.isMainClauseOnTopLevel()) {
            when (taskNames.first()) {
                "twine", "install" -> {}
                else -> return true // TODO: is run/test?
            }
        }

        if (context.location?.psiElement?.parent !is YAMLKeyValue) return false
        val taskId = (context.location?.psiElement?.parent as YAMLKeyValue).value?.text ?: return false

        return when (taskNames.first()) {
            taskId -> true
            "twine" -> context.location?.psiElement?.text?.startsWith("twine") ?: false
            "install" -> context.location?.psiElement?.text?.startsWith("requirements") ?: false
            else -> false
        }
    }

    override fun isPreferredConfiguration(self: ConfigurationFromContext?, other: ConfigurationFromContext): Boolean {
        return true
    }

    private fun PsiElement.isMainClauseOnTopLevel(): Boolean {
        if (node.elementType != PyTokenTypes.IF_KEYWORD) {
            return false
        }
        val statement = getIfStatementByIfKeyword(this) ?: return false
        return when (ScopeUtil.getScopeOwner(statement) is PyFile) {
            true -> PyUtil.isIfNameEqualsMain(statement)
            false -> false
        }
    }

    override fun shouldReplace(self: ConfigurationFromContext, other: ConfigurationFromContext): Boolean {
        return other.isProducedBy(PythonRunConfigurationProducer::class.java)
    }
}
