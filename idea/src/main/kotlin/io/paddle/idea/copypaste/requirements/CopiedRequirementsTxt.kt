package io.paddle.idea.copypaste.requirements

import io.paddle.idea.copypaste.common.CopiedTextBase
import java.awt.datatransfer.DataFlavor

class CopiedRequirementsTxt(fileText: String, startOffsets: IntArray, endOffsets: IntArray) : CopiedTextBase(fileText, startOffsets, endOffsets) {

    override fun getFlavor() = DATA_FLAVOR

    companion object {
        val DATA_FLAVOR: DataFlavor = DataFlavor(RequirementsTxtCopyPasteProcessor::class.java, "class: PaddleRequirementsCopyPasteProcessor")
    }
}
