package io.paddle.idea.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import io.paddle.idea.utils.PaddleProject
import io.paddle.idea.utils.findPaddleInDirectory
import io.paddle.plugin.python.extensions.requirements
import org.jetbrains.yaml.psi.YAMLDocument
import java.io.File
import java.nio.file.Path

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
        val projectPath = parameters.editor.project?.basePath!!
        val file = Path.of(projectPath).findPaddleInDirectory()!!.toFile()
        val project = PaddleProject.load(file, File(projectPath))

        val prefix = parameters.position.text.trim().removeSuffix(DUMMY_IDENTIFIER_TRIMMED)
        val variants = project.requirements.repositories.findAvailablePackagesByPrefix(prefix)

        for ((repository, names) in variants) {
            result.addAllElements(
                names.map { LookupElementBuilder.create(it).withTypeText(repository.url, true) }
            )
        }
    }
}
