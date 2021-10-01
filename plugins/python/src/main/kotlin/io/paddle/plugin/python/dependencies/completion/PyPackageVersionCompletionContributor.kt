package io.paddle.plugin.python.dependencies.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import io.paddle.plugin.python.dependencies.index.PyPackagesRepositoryIndexer
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import org.jetbrains.yaml.psi.YAMLDocument

class PyPackageVersionCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .inFile(PlatformPatterns.psiFile().withName(PlatformPatterns.string().equalTo("paddle.yaml")))
                .withSuperParent(2, PlatformPatterns.psiElement().withText(PlatformPatterns.string().startsWith("version:")))
                .withSuperParent(6, PlatformPatterns.psiElement().withText(PlatformPatterns.string().startsWith("libraries:")))
                .withSuperParent(8, PlatformPatterns.psiElement().withText(PlatformPatterns.string().startsWith("requirements:")))
                .withSuperParent(10, YAMLDocument::class.java),
            PyPackageVersionCompletionProvider()
        )
    }
}

class PyPackageVersionCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val prefix = parameters.position.text.trim().removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)
        val packageName = parameters.originalPosition?.parent?.parent?.prevSibling?.prevSibling?.prevSibling?.lastChild?.text ?: return
        val variants = PyPackagesRepositoryIndexer.findAvailableDistributionsByPackage(packageName)
        for ((repositoryUrl, distributions) in variants) {
            result.addAllElements(
                distributions
                    .mapNotNull { PyDistributionInfo.fromString(it) }
                    .filter { it.version.startsWith(prefix) }
                    .map {
                        LookupElementBuilder.create(it.version)
                            .withTailText("  ${it.ext}", true)
                            .withTypeText(repositoryUrl, true)
                    }
            )
        }
    }
}
