package io.paddle.idea.copypaste.poetry

import io.paddle.idea.copypaste.common.CopiedTextBase
import java.awt.datatransfer.DataFlavor

class CopiedPoetry(fileText: String, startOffsets: IntArray, endOffsets: IntArray) :
    CopiedTextBase(fileText, startOffsets, endOffsets) {

    override fun getFlavor() = DATA_FLAVOR

    companion object {
        val DATA_FLAVOR: DataFlavor = DataFlavor(PoetryCopyPasteProcessor::class.java, "class: PaddlePoetryCopyPasteProcessor")
    }
}
