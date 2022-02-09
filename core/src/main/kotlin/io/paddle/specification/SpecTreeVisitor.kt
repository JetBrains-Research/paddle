package io.paddle.specification

import io.paddle.utils.json.schema.JSONSCHEMA

interface SpecTreeVisitor{
    fun visit(integerNode: IntegerSpecTreeNode): Any
    fun visit(booleanNode: BooleanSpecTreeNode): Any
    fun visit(stringNode: StringSpecTreeNode): Any
    fun visit(arrayNode: ArraySpecTreeNode): Any
    fun visit(compositeNode: CompositeSpecTreeNode): Any
}

class JsonSchemaSpecVisitor : SpecTreeVisitor {

    override fun visit(integerNode: IntegerSpecTreeNode): String {
        return JSONSCHEMA.string(integerNode)
    }

    override fun visit(booleanNode: BooleanSpecTreeNode): String {
        return JSONSCHEMA.string(booleanNode)
    }

    override fun visit(stringNode: StringSpecTreeNode): String {
        return JSONSCHEMA.string(stringNode)
    }

    override fun visit(arrayNode: ArraySpecTreeNode): String {
        return JSONSCHEMA.string(arrayNode)
    }

    override fun visit(compositeNode: CompositeSpecTreeNode): String {
        return JSONSCHEMA.string(compositeNode)
    }
}
