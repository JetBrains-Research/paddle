package io.paddle.idea.schema

import com.fasterxml.jackson.databind.JsonNode
import io.paddle.config.specification.*
import io.paddle.config.specification.MutableConfigSpecTree.*

class JsonSchemaBuilder(private val root: CompositeSpecTreeNode) : ConfigSpecBuilder {
    private val visitor: JsonSchemaSpecVisitor = JsonSchemaSpecVisitor()

    override fun build(): JsonNode = visitor.visit(root)

    override fun insert(destination: List<String>, node: SpecTreeNode) {
        val printedDest = destination.joinToString(".", "'", "'")
        findBy(root, destination)?.also {
            if (it is CompositeSpecTreeNode) {
                if (!it.children.add(node)) {
                    throw ConfigSpecificationException("Cannot insert to children of node specified by $printedDest")
                }
            } else
                throw ConfigSpecificationException("Path $printedDest specify a node with not a Composite type")
        } ?: throw ConfigSpecificationException("Cannot find a node by path $printedDest")
    }

    companion object {
        private fun findBy(root: CompositeSpecTreeNode, targetPath: List<String>): SpecTreeNode? {
            if (targetPath.isEmpty()) {
                return null
            }
            val firstName = targetPath.first()
            if (!firstName.contentEquals(root.name)) {
                return null
            }
            var curSpecTreeNode: SpecTreeNode = root
            for (name in targetPath.slice(1..targetPath.size)) {
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
}
