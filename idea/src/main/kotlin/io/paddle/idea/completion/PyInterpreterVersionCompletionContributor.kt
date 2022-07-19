package io.paddle.idea.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import io.paddle.plugin.python.dependencies.PyInterpreter
import io.paddle.plugin.python.hasPython
import kotlinx.coroutines.runBlocking
import org.jetbrains.yaml.psi.YAMLDocument

class PyInterpreterVersionCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .inFile(PlatformPatterns.psiFile().withName(PlatformPatterns.string().equalTo("paddle.yaml")))
                .withSuperParent(2, PlatformPatterns.psiElement().withText(PlatformPatterns.string().startsWith("python")))
                .withSuperParent(4, PlatformPatterns.psiElement().withText(PlatformPatterns.string().startsWith("environment")))
                .withSuperParent(6, YAMLDocument::class.java),
            PyInterpreterVersionCompletionProvider()
        )
    }
}

class PyInterpreterVersionCompletionProvider : CompletionProvider<CompletionParameters>() {
    private data class VersionTuple(val version: PyInterpreter.Version, val typeText: String)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) = runBlocking {
        val paddleProject = parameters.extractPaddleProject() ?: return@runBlocking
        if (!paddleProject.hasPython) return@runBlocking

        val prefix = parameters.position.text.trim().removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)
        val remoteVersions = PyInterpreter.Version.getAvailableRemoteVersions()

        val localVariants = PyInterpreter.Version.cachedVersions.asSequence().map { VersionTuple(it, "cached") } +
            PyInterpreter.Version.locallyInstalledVersions.asSequence().map { VersionTuple(it, "local") }

        localVariants
            .filter { it.version.number.startsWith(prefix) }
            .forEach {
                result.addElement(
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(it.version.number).withTypeText(it.typeText, true),
                        when (it.typeText) {
                            "cached" -> 0.1
                            "local" -> 0.2
                            else -> Double.MAX_VALUE
                        }
                    )
                )
            }

        remoteVersions.reversed().asSequence()
            .map { VersionTuple(it, PyInterpreter.PYTHON_DISTRIBUTIONS_BASE_URL) }
            .filter { it.version.number.startsWith(prefix) }
            .forEachIndexed { idx, item ->
                result.addElement(
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(item.version.number).withTypeText(item.typeText, true),
                        idx.toDouble()
                    )
                )
            }
    }
}
