package io.paddle.idea.schema

import com.fasterxml.jackson.databind.JsonNode
import io.paddle.config.specification.*
import io.paddle.config.specification.MutableConfigSpecTree.*

class JsonSchemaSpecBuilder(private val root: CompositeSpecTreeNode) : ConfigSpecBuilder {
    private val visitor: JsonSchemaSpecVisitor = JsonSchemaSpecVisitor()

    override fun build(): JsonNode = visitor.visit(root)

    override fun insert(dist: List<String>, node: SpecTreeNode) {
        TODO("Not yet implemented")
    }

    fun findBy(pathToNode: List<String>): SpecTreeNode? {
        if (pathToNode.isEmpty()) {
            return null
        }
        val firstName = pathToNode.first()
        if (!firstName.contentEquals(root.name)) {
            return null
        }
        var curSpecTreeNode: SpecTreeNode = root
        for (name in pathToNode.slice(1..pathToNode.size)) {
            if (curSpecTreeNode !is CompositeSpecTreeNode) {
                return null
            }
            curSpecTreeNode.children.find { it.name.contentEquals(name) }?.also {
                curSpecTreeNode = it
            } ?: return null
        }
        return curSpecTreeNode
    }
}
