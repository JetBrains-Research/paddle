package io.paddle.idea.execution

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.ConfigurationFromContext
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.externalSystem.service.execution.*
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyUtil
import com.jetbrains.python.psi.impl.getIfStatementByIfKeyword
import com.jetbrains.python.run.PythonRunConfigurationProducer
import com.jetbrains.python.sdk.basePath
import io.paddle.idea.PaddleManager
import io.paddle.idea.execution.marker.PyTestTargetFinder
import io.paddle.idea.utils.getSuperParent
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import io.paddle.project.PaddleProjectProvider
import org.jetbrains.yaml.psi.YAMLKeyValue
import java.io.File

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
            val path = element.getPath() ?: return false
            return getConfigurationForIfMain(path, configuration, context)
        }
        val testTask = findTestTask(element, context)
        if (testTask != null) {
            configuration.settings.taskNames = listOf(testTask["id"] as String)
            configuration.settings.externalProjectPath = module.basePath
            configuration.name = AbstractExternalSystemTaskConfigurationType.generateName(module.project, configuration.settings)
            return true
        }

        if (element.parent !is YAMLKeyValue) return false


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
            val path = element.getPath() ?: return false
            return when (taskNames.first()) {
                "twine", "install" -> false
                else -> ifFromIfMain(path, configuration, context)
            }
        }
        val testTask = findTestTask(element, context)
        if (testTask != null) {
            return (testTask["id"] as String) == taskNames.first()
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

    override fun shouldReplace(self: ConfigurationFromContext, other: ConfigurationFromContext): Boolean {
        return other.isProducedBy(PythonRunConfigurationProducer::class.java)
    }

    private fun findRunTaskForCurrentFile(currentFile: String, context: ConfigurationContext): Map<String, Any>? {
        val paddleProject = context.getPaddleProject() ?: return null

        val runTasks = paddleProject.config.get<List<Map<String, Any>>?>("tasks.run") ?: return null

        return runTasks.find {
            val entrypointPath = paddleProject.roots.sources.resolve(it["entrypoint"] as String).path
            entrypointPath == currentFile
        }
    }

    private fun findTestTask(element: PsiElement, context: ConfigurationContext): Map<String, Any>? {
        if (element is LeafPsiElement && element.elementType == PyTokenTypes.IDENTIFIER && element.parent != null) {
            return context.getPaddleProject()?.let { PyTestTargetFinder.findTestTaskForElement(element.parent, it) }
        }
        return null
    }

    private fun getConfigurationForIfMain(currentFile: String, configuration: PaddleRunConfiguration, context: ConfigurationContext): Boolean {
        val module = context.location?.module ?: return false
        val runTask = findRunTaskForCurrentFile(currentFile, context) ?: return false

        configuration.settings.taskNames = listOf(runTask["id"] as String)
        configuration.settings.externalProjectPath = module.basePath
        configuration.name = AbstractExternalSystemTaskConfigurationType.generateName(module.project, configuration.settings)
        return true
    }

    private fun ifFromIfMain(currentFile: String, configuration: PaddleRunConfiguration, context: ConfigurationContext): Boolean {
        val runTask = findRunTaskForCurrentFile(currentFile, context) ?: return false
        val taskNames = configuration.settings.taskNames.takeIf { it.isNotEmpty() } ?: return false

        return taskNames.first() == (runTask["id"] as String)
    }

    private fun PsiElement.isMainClauseOnTopLevel(): Boolean {
        if (node.elementType != PyTokenTypes.IF_KEYWORD) {
            return false
        }
        val statement = getIfStatementByIfKeyword(this) ?: return false
        return ScopeUtil.getScopeOwner(statement) is PyFile && PyUtil.isIfNameEqualsMain(statement)
    }

    private fun PsiElement.getPath(): String? {
        return containingFile?.virtualFile?.path
    }

    private fun ConfigurationContext.getPaddleProject(): PaddleProject? {
        val module = location?.module ?: return null
        val moduleDir = module.basePath?.let { File(it) } ?: return null
        val rootDir = project.basePath?.let { File(it) } ?: return null

        return PaddleProjectProvider.getInstance(rootDir).getProject(moduleDir)
    }
}
