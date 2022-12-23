package io.paddle.idea.copypaste.requirements

import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import io.paddle.idea.copypaste.common.ConverterType
import io.paddle.idea.copypaste.common.CopyPasteProcessorBase
import io.paddle.plugin.python.dependencies.packages.PyPackageMetadata
import java.awt.datatransfer.DataFlavor

class RequirementsTxtCopyPasteProcessor : CopyPasteProcessorBase() {
    override val LOG = Logger.getInstance(RequirementsTxtCopyPasteProcessor::class.java)
    override val DATA_FLAVOR: DataFlavor = CopiedRequirementsTxt.DATA_FLAVOR
    override val converterType = ConverterType.RequirementsTxt


    override fun collectTransferableData(
        file: PsiFile,
        editor: Editor,
        startOffsets: IntArray,
        endOffsets: IntArray
    ): List<TextBlockTransferableData> {
        if (!isRequirementsTxtFile(file) || file.text == null) return listOf()
        return listOf(CopiedRequirementsTxt(file.text!!, startOffsets, endOffsets))
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
