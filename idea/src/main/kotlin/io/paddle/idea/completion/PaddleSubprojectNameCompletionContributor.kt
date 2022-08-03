package io.paddle.idea.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import io.paddle.project.PaddleProjectProvider
import io.paddle.project.extensions.descriptor
import org.jetbrains.yaml.psi.YAMLDocument

class PaddleSubprojectNameCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .inFile(PlatformPatterns.psiFile().withName(PlatformPatterns.string().equalTo("paddle.yaml")))
                .withSuperParent(4, PlatformPatterns.psiElement().withText(PlatformPatterns.string().startsWith("subprojects")))
                .withSuperParent(6, YAMLDocument::class.java),
            PaddleSubprojectNameCompletionProvider()
        )
    }
}

class PaddleSubprojectNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val paddleProject = parameters.extractPaddleProject() ?: return
        val prefix = parameters.position.text.trim().removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)

        result.withPrefixMatcher(prefix).addAllElements(
            PaddleProjectProvider.getInstance(paddleProject.rootDir).allProjects.map {
                LookupElementBuilder.create(it.descriptor.name)
            }
        )
    }
}
