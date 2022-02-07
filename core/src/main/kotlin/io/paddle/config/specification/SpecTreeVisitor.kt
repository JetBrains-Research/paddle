package io.paddle.config.specification

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface SpecTreeVisitor {
    fun visit(integerNode: IntegerSpecTreeNode): Any
    fun visit(booleanNode: BooleanSpecTreeNode): Any
    fun visit(stringNode: StringSpecTreeNode): Any
    fun visit(arrayNode: ArraySpecTreeNode): Any
    fun visit(compositeNode: CompositeSpecTreeNode): Any
}

class JsonSchemaSpecVisitor(private val jsonSerializer: Json) : SpecTreeVisitor {

    override fun visit(integerNode: IntegerSpecTreeNode): String {
        return jsonSerializer.encodeToString(integerNode)
    }

    override fun visit(booleanNode: BooleanSpecTreeNode): String {
        return jsonSerializer.encodeToString(booleanNode)
    }

    override fun visit(stringNode: StringSpecTreeNode): String {
        return jsonSerializer.encodeToString(stringNode)
    }

    override fun visit(arrayNode: ArraySpecTreeNode): String {
        return jsonSerializer.encodeToString(arrayNode)
    }

    override fun visit(compositeNode: CompositeSpecTreeNode): String {
        return jsonSerializer.encodeToString(compositeNode)
    }
}
