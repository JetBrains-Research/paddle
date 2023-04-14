package io.paddle.idea.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import io.paddle.plugin.python.dependencies.interpreter.InterpreterVersion
import io.paddle.plugin.python.dependencies.interpreter.PythonPaths
import io.paddle.plugin.python.extensions.globalInterpreter
import io.paddle.plugin.python.hasPython
import kotlinx.coroutines.runBlocking
import org.jetbrains.yaml.psi.YAMLDocument

class PyInterpreterVersionCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .inFile(PlatformPatterns.psiFile().withName(PlatformPatterns.string().equalTo("paddle.yaml")))
                .withSuperParent(
                    2,
                    PlatformPatterns.psiElement().withText(PlatformPatterns.string().startsWith("python"))
                )
                .withSuperParent(
                    4,
                    PlatformPatterns.psiElement().withText(PlatformPatterns.string().startsWith("environment"))
                )
                .withSuperParent(6, YAMLDocument::class.java),
            PyInterpreterVersionCompletionProvider()
        )
    }
}

class PyInterpreterVersionCompletionProvider : CompletionProvider<CompletionParameters>() {
    private data class VersionTuple(val version: InterpreterVersion, val typeText: String) {
        companion object Type {
            const val CACHED = "internal paddle cache"
            const val LOCAL = "local installation"
            const val FTP = PythonPaths.PYTHON_DISTRIBUTIONS_BASE_URL
        }
    }

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) = runBlocking {
        val paddleProject = parameters.extractPaddleProject() ?: return@runBlocking
        if (!paddleProject.hasPython) return@runBlocking

        val prefix = parameters.position.text.trim().removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)
        val remoteVersions = InterpreterVersion.getAvailableRemoteVersions()

        val cachedVariants =
            paddleProject.globalInterpreter.cachedVersions.map { VersionTuple(it, VersionTuple.CACHED) }
        val localVariants =
            paddleProject.globalInterpreter.localVersions.map { VersionTuple(it, VersionTuple.LOCAL) }

        result.withPrefixMatcher(PlainPrefixMatcher(prefix, true)).addAllElements(
            (cachedVariants + localVariants)
                .map {
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(it.version.number).withTypeText(it.typeText, true),
                        when (it.typeText) {
                            VersionTuple.CACHED -> 100_001.0
                            VersionTuple.LOCAL -> 100_000.0
                            else -> Double.MIN_VALUE
                        }
                    )
                }
        )

        result.withPrefixMatcher(PlainPrefixMatcher(prefix, true)).addAllElements(
            remoteVersions.asSequence()
                .map { VersionTuple(it, VersionTuple.FTP) }
                .mapIndexed { idx, item ->
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(item.version.number).withTypeText(item.typeText, true),
                        idx.toDouble()
                    )
                }
                .toList()
        )
    }
}
