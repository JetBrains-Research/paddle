package io.paddle.idea.copypaste.poetry

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.intellij.codeInsight.editorActions.CopyPastePostProcessor
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.diagnostic.ControlFlowException
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.FileContentUtil
import io.paddle.idea.settings.global.PaddleAppSettings
import io.paddle.project.PaddleProjectProvider
import io.paddle.utils.config.ConfigurationYAML
import org.jetbrains.yaml.psi.YAMLFile
import java.awt.datatransfer.Transferable
import java.io.File


class PoetryCopyPasteProcessor : CopyPastePostProcessor<TextBlockTransferableData>() {
    private val LOG = Logger.getInstance(PoetryCopyPasteProcessor::class.java)

    override fun extractTransferableData(content: Transferable): List<TextBlockTransferableData> {
        try {
            // TODO: it is also possible to track copy-paste from external source,
            //  but due to very liberal grammar checks it creates too many false-positives
            if (content.isDataFlavorSupported(CopiedPoetry.DATA_FLAVOR)) {
                return listOf(content.getTransferData(CopiedPoetry.DATA_FLAVOR) as TextBlockTransferableData)
            }
        } catch (e: Throwable) {
            if (e is ControlFlowException) throw e
            LOG.error(e)
        }
        return listOf()
    }

    override fun collectTransferableData(
        file: PsiFile, editor: Editor, startOffsets: IntArray, endOffsets: IntArray
    ): List<TextBlockTransferableData> {
        if (!isPoetryTxtFile(file) || file.text == null) return listOf()
        return listOf(CopiedPoetry(file.text!!, startOffsets, endOffsets))
    }

    override fun processTransferableData(
        project: Project, editor: Editor, bounds: RangeMarker, caretOffset: Int, indented: Ref<in Boolean>, values: List<TextBlockTransferableData>
    ) {
        if (DumbService.getInstance(project).isDumb) return

        val data = values.single() as CopiedPoetry

        val document = editor.document
        var targetFile = PsiDocumentManager.getInstance(project).getPsiFile(document) as? YAMLFile ?: return
        if (targetFile.name != "paddle.yaml") return

        val rootDir = project.basePath?.let { File(it) } ?: return
        val workDir = targetFile.containingDirectory.virtualFile.canonicalPath?.let { File(it) } ?: return
        val paddleProject = PaddleProjectProvider.getInstance(rootDir).getProject(workDir) ?: return


        // document.text != targetFile.text
        fun getTextRange(section: String, offset: Int): TextRange? = document.text.substring(offset).indexOf(section).takeIf { it != -1 }?.let { startOffsetInSubstring ->
            val startOffset = startOffsetInSubstring + offset
            val psiElement = targetFile.findElementAt(startOffset)
            if (psiElement is PsiComment) return getTextRange(section, startOffset + psiElement.textLength)
            targetFile.findElementAt(startOffset)?.parent?.textRange
        }


        fun TextRange?.insertNewYamlString(
            yaml: String
        ) {
            this?.let {
                document.replaceString(it.startOffset, it.endOffset, yaml)
            } ?: document.insertString(document.textLength, yaml)
        }

        fun doConversion() {
            val converter = PoetryConverter.from(
                fileText = data.textWithOffsetsApplied,
                paddleConfig = (paddleProject.config as ConfigurationYAML).toMutableMap()
            )

            runWriteAction {
                document.replaceString(bounds.startOffset, bounds.endOffset, "")
                PsiDocumentManager.getInstance(project).commitDocument(document)
            }

            targetFile = PsiDocumentManager.getInstance(project).getPsiFile(document) as? YAMLFile ?: return

            val textRanges = converter.sections.associateWith { getTextRange("$it:", 0) }

            runWriteAction {
                // first change sections in the end, so that the offsets are not invalidated
                converter.sections.sortedByDescending { textRanges[it]?.startOffset ?: -1 }.forEach { sectionName ->
                    val yamlResult = converter.getYamlSectionString(sectionName)
                    val textRange = textRanges[sectionName]
                    textRange.insertNewYamlString(yamlResult)
                }
                PsiDocumentManager.getInstance(project).commitDocument(document)
            }
        }

        fun reformatYAML() {
            FileContentUtil.reparseFiles(project, listOf(targetFile.virtualFile), false)
            targetFile = PsiDocumentManager.getInstance(project).getPsiFile(document) as? YAMLFile ?: return

            runWriteAction {
                CodeStyleManager.getInstance(project).reformat(targetFile)
            }
        }

        if (confirmConvertPoetryOnPaste(project)) {
            doConversion()
            reformatYAML()
        }
    }

    private fun confirmConvertPoetryOnPaste(project: Project): Boolean {
        if (PaddleAppSettings.getInstance().isDontShowDialogOnPoetryPaste) return true

        val dialog = PasteFromPoetryDialog(project)
        return dialog.showAndGet()
    }

    private fun isPoetryTxtFile(file: PsiFile): Boolean {
        return file.name == "pyproject.toml" && isPoetry(file.text)
    }

    private fun isPoetry(text: String): Boolean {
        return TomlMapper().readTree(text)["tool"]["poetry"] != null
    }
}
