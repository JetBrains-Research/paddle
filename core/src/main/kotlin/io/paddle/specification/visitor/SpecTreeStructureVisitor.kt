package io.paddle.specification.visitor

import io.paddle.specification.tree.*

class SpecTreeStructureVisitor: SpecTreeVisitor<Unit, MutableMap<String, Any?>> {
    override fun visit(compositeNode: CompositeSpecTreeNode, ctx: MutableMap<String, Any?>) {
        compositeNode.children.forEach { (name, node) ->
            val substructure = mutableMapOf<String, Any?>()
            node.accept(this, substructure)
            ctx[name] = substructure.ifEmpty { null }
        }
    }

    override fun visit(integerNode: IntegerSpecTreeNode, ctx: MutableMap<String, Any?>) {

    }

    override fun visit(booleanNode: BooleanSpecTreeNode, ctx: MutableMap<String, Any?>) {

    }

    override fun visit(stringNode: StringSpecTreeNode, ctx: MutableMap<String, Any?>) {

    }

    override fun <T : ConfigurationSpecification.SpecTreeNode> visit(arrayNode: ArraySpecTreeNode<T>, ctx: MutableMap<String, Any?>) {

    }
}
