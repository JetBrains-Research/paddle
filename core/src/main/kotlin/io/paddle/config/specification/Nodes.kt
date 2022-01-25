package io.paddle.config.specification

import io.paddle.config.specification.MutableConfigSpecTree.*

class CompositeSpecTreeNode(name: String, description: String?) : SpecTreeNode(name, description) {
    val children: MutableList<SpecTreeNode> = mutableListOf()
    val required: MutableList<String> = mutableListOf()

    override fun accept(visitor: SpecTreeVisitor) = visitor.visit(this)
}

class ArraySpecTreeNode(name: String, description: String?, val item: SpecTreeNode) : SpecTreeNode(name, description) {
    override fun accept(visitor: SpecTreeVisitor) = visitor.visit(this)
}

class SimpleSpecTreeNode<T>(name: String, description: String?, enum: List<T>) : SpecTreeNode(name, description) {
    override fun accept(visitor: SpecTreeVisitor) = visitor.visit(this)
}
