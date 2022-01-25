package io.paddle.config.specification

import com.fasterxml.jackson.databind.JsonNode

interface SpecTreeVisitor {
    fun <T> visit(simpleNode: SimpleSpecTreeNode<T>): Any
    fun visit(arrayNode: ArraySpecTreeNode): Any
    fun visit(compositeNode: CompositeSpecTreeNode): Any
}

class JsonSchemaSpecVisitor : SpecTreeVisitor {
    override fun <T> visit(simpleNode: SimpleSpecTreeNode<T>): JsonNode {
        TODO("Not yet implemented")
    }

    override fun visit(arrayNode: ArraySpecTreeNode): JsonNode {
        TODO("Not yet implemented")
    }

    override fun visit(compositeNode: CompositeSpecTreeNode): JsonNode {
        TODO("Not yet implemented")
    }
}
