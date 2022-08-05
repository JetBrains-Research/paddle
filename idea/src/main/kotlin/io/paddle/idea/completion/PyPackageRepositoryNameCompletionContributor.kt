package io.paddle.idea.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import io.paddle.plugin.python.extensions.getAllPyPackageRepoDescriptors
import org.jetbrains.yaml.psi.YAMLDocument

class PyPackageRepositoryNameCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .inFile(PlatformPatterns.psiFile().withName(PlatformPatterns.string().equalTo("paddle.auth.yaml")))
                .withSuperParent(2, PlatformPatterns.psiElement().withText(PlatformPatterns.string().startsWith("name")))
                .withSuperParent(6, PlatformPatterns.psiElement().withText(PlatformPatterns.string().startsWith("repositories")))
                .withSuperParent(8, YAMLDocument::class.java),
            PyPackageRepositoryNameCompletionProvider()
        )
    }
}

class PyPackageRepositoryNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        try {
            val paddleProject = parameters.extractPaddleProject() ?: return

            val repoDescriptors = paddleProject.getAllPyPackageRepoDescriptors()
            val prefix = parameters.position.text.trim().removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)

            result.withPrefixMatcher(prefix).addAllElements(
                repoDescriptors.map {
                    LookupElementBuilder.create(it.name).withTypeText(it.url, true)
                }
            )
        } catch (exception: Throwable) {
            return
        }
    }
}
