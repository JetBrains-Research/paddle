package io.paddle.idea.copypaste.poetry

import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import java.awt.datatransfer.DataFlavor

class CopiedPoetry(private val fileText: String, private val startOffsets: IntArray, private val endOffsets: IntArray) : TextBlockTransferableData {
    val textWithOffsetsApplied: String by lazy {
        val sb = StringBuilder()
        for (i in startOffsets.indices) {
            sb.append(fileText.substring(startOffsets[i], endOffsets[i]))
            if (i < startOffsets.size - 1) {
                sb.append("\n")
            }
        }
        sb.toString()
    }

    override fun getFlavor() = DATA_FLAVOR
    override fun getOffsetCount() = 0

    override fun getOffsets(offsets: IntArray?, index: Int) = index
    override fun setOffsets(offsets: IntArray?, index: Int) = index

    companion object {
        val DATA_FLAVOR: DataFlavor = DataFlavor(PoetryCopyPasteProcessor::class.java, "class: PaddlePoetryCopyPasteProcessor")
    }
}
