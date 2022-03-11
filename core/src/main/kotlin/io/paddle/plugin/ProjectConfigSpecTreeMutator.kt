package io.paddle.plugin

import io.paddle.specification.tree.MutableConfigSpecTree
import io.paddle.specification.tree.MutableConfigSpecTree.SpecTreeNode

object ProjectConfigSpecTreeMutator {
    fun applySpecExtension(configSpecTree: MutableConfigSpecTree, extension: SpecTreeNode, dest: List<String>, name: String) {
        configSpecTree.insert(name, extension, dest)
    }
}
