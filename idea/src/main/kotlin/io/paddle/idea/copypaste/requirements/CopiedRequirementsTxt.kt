package io.paddle.idea.copypaste.requirements

import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import java.awt.datatransfer.DataFlavor

class CopiedRequirementsTxt(val fileText: String, val startOffsets: IntArray, val endOffsets: IntArray) : TextBlockTransferableData {
    override fun getFlavor() = DATA_FLAVOR
    override fun getOffsetCount() = 0

    override fun getOffsets(offsets: IntArray?, index: Int) = index
    override fun setOffsets(offsets: IntArray?, index: Int) = index

    companion object {
        val DATA_FLAVOR: DataFlavor = DataFlavor(RequirementsTxtCopyPasteProcessor::class.java, "class: PaddleRequirementsCopyPasteProcessor")
    }
}
