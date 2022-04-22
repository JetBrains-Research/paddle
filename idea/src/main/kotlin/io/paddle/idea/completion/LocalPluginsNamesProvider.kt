package io.paddle.idea.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import io.paddle.idea.utils.PaddleProject
import io.paddle.idea.utils.findPaddleInDirectory
import io.paddle.plugin.pyinjector.extensions.pyPluginsRepositories
import io.paddle.plugin.repositories.jarPluginsRepositories
import io.paddle.project.Project
import java.io.File
import java.nio.file.Path

interface ProjectProvider {
    fun project(parameters: CompletionParameters): Project {
        val projectPath = parameters.editor.project?.basePath!!
        val file = Path.of(projectPath).findPaddleInDirectory()!!.toFile()
        return PaddleProject.load(file, File(projectPath))
    }
}
// TODO: check why is not working
class LocalPluginsNamesProvider : ProjectProvider, CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val prefix = parameters.position.text.trim().removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)
        val project: Project = project(parameters)

        val jarVariants = project.jarPluginsRepositories.findAvailablePluginsBy(prefix)
        for ((pluginName, repos) in jarVariants) {
            repos.forEach {
                result.addElement(LookupElementBuilder.create(pluginName).withTypeText(it.name, true))
            }
        }

        val pyModuleVariants = project.pyPluginsRepositories.withPyModules.findAvailablePluginsBy(prefix)
        for ((pluginName, repos) in pyModuleVariants) {
            repos.forEach {
                result.addElement(LookupElementBuilder.create(pluginName).withTypeText(it.name, true))
            }
        }
    }
}
