package io.paddle.idea.copypaste.requirements

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
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.FileContentUtil
import io.paddle.idea.settings.global.PaddleAppSettings
import io.paddle.plugin.python.dependencies.packages.PyPackageMetadata
import io.paddle.project.PaddleProjectProvider
import io.paddle.utils.config.ConfigurationYAML
import org.jetbrains.yaml.psi.YAMLFile
import java.awt.datatransfer.Transferable
import java.io.File

class RequirementsTxtCopyPasteProcessor : CopyPastePostProcessor<TextBlockTransferableData>() {
    private val LOG = Logger.getInstance(RequirementsTxtCopyPasteProcessor::class.java)

    override fun extractTransferableData(content: Transferable): List<TextBlockTransferableData> {
        try {
            // TODO: it is also possible to track copy-paste from external source,
            //  but due to very liberal grammar checks it creates too many false-positives
            if (content.isDataFlavorSupported(CopiedRequirementsTxt.DATA_FLAVOR)) {
                return listOf(content.getTransferData(CopiedRequirementsTxt.DATA_FLAVOR) as TextBlockTransferableData)
            }
        } catch (e: Throwable) {
            if (e is ControlFlowException) throw e
            LOG.error(e)
        }
        return listOf()
    }

    override fun collectTransferableData(
        file: PsiFile,
        editor: Editor,
        startOffsets: IntArray,
        endOffsets: IntArray
    ): List<TextBlockTransferableData> {
        if (!isRequirementsTxtFile(file) || file.text == null) return listOf()
        return listOf(CopiedRequirementsTxt(file.text!!, startOffsets, endOffsets))
    }

    override fun processTransferableData(
        project: Project,
        editor: Editor,
        bounds: RangeMarker,
        caretOffset: Int,
        indented: Ref<in Boolean>,
        values: List<TextBlockTransferableData>
    ) {
        if (DumbService.getInstance(project).isDumb) return

        val data = values.single() as CopiedRequirementsTxt

        val document = editor.document
        var targetFile = PsiDocumentManager.getInstance(project).getPsiFile(document) as? YAMLFile ?: return
        if (targetFile.name != "paddle.yaml") return

        val rootDir = project.basePath?.let { File(it) } ?: return
        val workDir = targetFile.containingDirectory.virtualFile.canonicalPath?.let { File(it) } ?: return
        val paddleProject = PaddleProjectProvider.getInstance(rootDir).getProject(workDir) ?: return

        fun doConversion() {
            val converter = RequirementsTxtConverter.from(
                fileText = data.fileText,
                paddleConfig = (paddleProject.config as ConfigurationYAML).toMutableMap()
            )

            runWriteAction {
                document.replaceString(bounds.startOffset, bounds.endOffset, "")
                PsiDocumentManager.getInstance(project).commitDocument(document)
            }

            targetFile = PsiDocumentManager.getInstance(project).getPsiFile(document) as? YAMLFile ?: return

            val requirementsTextRange = document.text.indexOf("requirements:").takeIf { it != -1 }?.let { startOffset ->
                targetFile.findElementAt(startOffset)?.parent?.textRange
            }

            val repositoriesTextRange = document.text.indexOf("repositories:").takeIf { it != -1 }?.let { startOffset ->
                targetFile.findElementAt(startOffset)?.parent?.textRange
            }

            runWriteAction {
                requirementsTextRange?.let {
                    document.replaceString(it.startOffset, it.endOffset, converter.requirementsYaml)
                } ?: document.insertString(document.textLength, converter.requirementsYaml)

                repositoriesTextRange?.let {
                    document.replaceString(it.startOffset, it.endOffset, converter.repositoriesYaml)
                } ?: document.insertString(document.textLength, converter.repositoriesYaml)

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

        if (confirmConvertRequirementsTxtOnPaste(project)) {
            doConversion()
            reformatYAML()
        }
    }

    private fun confirmConvertRequirementsTxtOnPaste(project: Project): Boolean {
        if (PaddleAppSettings.getInstance().isDontShowDialogOnRequirementTxtPaste) return true

        val dialog = PasteFromRequirementsTxtDialog(project)
        return dialog.showAndGet()
    }

    private fun isRequirementsTxtFile(file: PsiFile): Boolean {
        return file.name == "requirements.txt" && isRequirementsTxt(file.text)
    }

    private fun isRequirementsTxt(text: String): Boolean {
        return text.split("\n")
            .filter { it.isNotBlank() }
            .all { line ->
                line.startsWith("--extra-index-url") ||
                    line.startsWith("--index-url") ||
                    PyPackageMetadata.createDependencySpecificationParser(line)?.specification()?.let {
                        it.nameReq()?.name() != null
                    } ?: false
            }
    }
}
