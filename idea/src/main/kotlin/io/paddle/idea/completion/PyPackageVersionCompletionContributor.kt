package io.paddle.idea.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.packages.PyPackageVersionRelation
import io.paddle.plugin.python.extensions.repositories
import io.paddle.plugin.python.hasPython
import org.jetbrains.yaml.psi.YAMLDocument

class PyPackageVersionCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .inFile(PlatformPatterns.psiFile().withName(PlatformPatterns.string().equalTo("paddle.yaml")))
                .withSuperParent(2, PlatformPatterns.psiElement().withText(PlatformPatterns.string().startsWith("version:")))
                .withSuperParent(8, PlatformPatterns.psiElement().withText(PlatformPatterns.string().startsWith("requirements:")))
                .withSuperParent(10, YAMLDocument::class.java),
            PyPackageVersionCompletionProvider()
        )
    }
}

class PyPackageVersionCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val paddleProject = parameters.extractPaddleProject() ?: return
        if (!paddleProject.hasPython) return

        var prefix = parameters.position.text.trim().removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)
        for (it in PyPackageVersionRelation.values()) {
            if (prefix.startsWith(it.operator)) {
                prefix = prefix.removePrefix(it.operator)
                break
            }
        }

        val packageName = parameters.originalPosition?.parent?.parent?.parent?.children
            ?.firstOrNull { it.text.startsWith("name") }?.lastChild?.text
            ?: return
        val variants = paddleProject.repositories.resolved.findAvailableDistributionsByPackageName(packageName)

        variants.keys.toList()
            .filter { it.version.startsWith(prefix) }
            .sortedBy { PyPackage.Version.from(it.version) }
            .forEachIndexed { idx, distribution ->
                result.addElement(
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(distribution.version)
                            .withTailText("  ${distribution.ext}", true)
                            .withTypeText(variants[distribution]?.name, true),
                        idx.toDouble()
                    )
                )
            }
    }
}
