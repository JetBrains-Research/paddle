package io.paddle.idea.copypaste.poetry

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import io.paddle.idea.copypaste.common.ConverterType
import io.paddle.idea.copypaste.common.CopyPasteProcessorBase
import java.awt.datatransfer.DataFlavor


class PoetryCopyPasteProcessor : CopyPasteProcessorBase() {
    override val LOG = Logger.getInstance(PoetryCopyPasteProcessor::class.java)
    override val DATA_FLAVOR: DataFlavor = CopiedPoetry.DATA_FLAVOR
    override val converterType = ConverterType.Poetry

    override fun collectTransferableData(
        file: PsiFile, editor: Editor, startOffsets: IntArray, endOffsets: IntArray
    ): List<TextBlockTransferableData> {
        if (!isPoetryTxtFile(file) || file.text == null) return listOf()
        return listOf(CopiedPoetry(file.text!!, startOffsets, endOffsets))
    }


    private fun isPoetryTxtFile(file: PsiFile): Boolean {
        return file.name == "pyproject.toml" && isPoetry(file.text)
    }

    private fun isPoetry(text: String): Boolean {
        return TomlMapper().readTree(text)["tool"]["poetry"] != null
    }
}
