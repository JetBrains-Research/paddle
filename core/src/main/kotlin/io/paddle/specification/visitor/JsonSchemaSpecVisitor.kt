package io.paddle.specification.visitor

import io.paddle.specification.tree.*
import io.paddle.utils.json.schema.JSONSCHEMA

class JsonSchemaSpecVisitor : SpecTreeVisitor<String, Unit> {
    override fun visit(integerNode: IntegerSpecTreeNode, ctx: Unit) = JSONSCHEMA.string(integerNode)

    override fun visit(booleanNode: BooleanSpecTreeNode, ctx: Unit) = JSONSCHEMA.string(booleanNode)

    override fun visit(stringNode: StringSpecTreeNode, ctx: Unit) = JSONSCHEMA.string(stringNode)

    override fun visit(arrayNode: ArraySpecTreeNode, ctx: Unit) = JSONSCHEMA.string(arrayNode)

    override fun visit(compositeNode: CompositeSpecTreeNode, ctx: Unit) = JSONSCHEMA.string(compositeNode)
}
