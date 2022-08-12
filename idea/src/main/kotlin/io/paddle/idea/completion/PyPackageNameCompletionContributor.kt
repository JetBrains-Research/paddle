package io.paddle.idea.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ex.ApplicationUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.extensions.repositories
import io.paddle.plugin.python.hasPython
import io.paddle.plugin.python.utils.PyPackageName
import io.paddle.project.PaddleProject
import org.jetbrains.yaml.psi.YAMLDocument

class PyPackageNameCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            psiElement()
                .inFile(psiFile().withName(string().equalTo("paddle.yaml")))
                .withSuperParent(2, psiElement().withText(string().startsWith("name:")))
                .withSuperParent(8, psiElement().withText(string().startsWith("requirements:")))
                .withSuperParent(10, YAMLDocument::class.java),
            PyPackageNameCompletionProvider()
        )
    }
}

class PyPackageNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    private val logger = Logger.getInstance(PyPackageNameCompletionProvider::class.java)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val paddleProject = parameters.extractPaddleProject() ?: return
        if (!paddleProject.hasPython) return

        val prefix = parameters.position.text.trim().removeSuffix(DUMMY_IDENTIFIER_TRIMMED)
        val variants = fetchPackagesWithCheckCanceled(paddleProject, prefix)

        result.addAllElements(
            variants.map { (pkgName, repo) ->
                LookupElementBuilder.create(pkgName).withTypeText(repo.name, true)
            }
        )
    }

    private fun fetchPackagesWithCheckCanceled(
        paddleProject: PaddleProject,
        prefix: String
    ): Map<PyPackageName, PyPackageRepository> {
        return try {
            val indicator = EmptyProgressIndicator.notNullize(ProgressManager.getInstance().progressIndicator)
            ApplicationUtil.runWithCheckCanceled({
                return@runWithCheckCanceled paddleProject.repositories.resolved.findAvailablePackagesByPrefix(prefix)
            }, indicator)
        } catch (e: ProcessCanceledException) {
            logger.info("Fetching packages for prefix $prefix cancelled")
            emptyMap()
        } catch (e: Exception) {
            logger.info("Cannot fetch packages for prefix $prefix", e)
            emptyMap()
        }
    }
}
