package io.paddle.idea.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import io.paddle.plugin.python.extensions.repositories
import io.paddle.project.PaddleProjectProvider
import org.jetbrains.yaml.psi.YAMLDocument
import java.io.File

class PyPackageNameCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            psiElement()
                .inFile(psiFile().withName(string().equalTo("paddle.yaml")))
                .withSuperParent(2, psiElement().withText(string().startsWith("name:")))
                .withSuperParent(6, psiElement().withText(string().startsWith("libraries:")))
                .withSuperParent(8, psiElement().withText(string().startsWith("requirements:")))
                .withSuperParent(10, YAMLDocument::class.java),
            PyPackageNameCompletionProvider()
        )
    }
}

class PyPackageNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val projectPath = File(parameters.editor.project?.basePath!!)
        val project = PaddleProjectProvider.getInstance(projectPath).rootProject ?: return

        val prefix = parameters.position.text.trim().removeSuffix(DUMMY_IDENTIFIER_TRIMMED)
        val variants = project.repositories.resolved.findAvailablePackagesByPrefix(prefix)

        for ((pkgName, repo) in variants) {
            result.addElement(LookupElementBuilder.create(pkgName).withTypeText(repo.name, true))
        }
    }
}
