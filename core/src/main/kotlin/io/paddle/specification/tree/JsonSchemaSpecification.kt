package io.paddle.specification.tree

import io.paddle.specification.*
import io.paddle.specification.tree.MutableConfigSpecTree.SpecTreeNode
import io.paddle.specification.visitor.JsonSchemaSpecVisitor
import io.paddle.utils.json.schema.JSONSCHEMA

class JsonSchemaSpecification(baseSchemaResourceUrl: String) : ConfigurationSpecification() {
    private val root = readSchemaBy(baseSchemaResourceUrl)

    private val visitor = JsonSchemaSpecVisitor()

    private fun readSchemaBy(url: String): CompositeSpecTreeNode {
        val inputStreamWithSchema = javaClass.classLoader.getResourceAsStream(url)
        val schema = inputStreamWithSchema?.bufferedReader()?.use {
            it.readText()
        }
        return schema?.let { JSONSCHEMA.parse(it) } ?: CompositeSpecTreeNode()
    }

    override fun build(): String = visitor.visit(root, Unit)

    override fun toString() = build()

    override fun insert(name: String, node: SpecTreeNode, destination: List<String>) {
        val printedDest = destination.joinToString(".", "'", "'")
        findBy(root, destination)?.also {
            if (it is CompositeSpecTreeNode) {
                it.children[name] = node
//                    throw ConfigSpecificationException("Cannot insert to children of node specified by $printedDest")

            } else
                throw ConfigSpecificationException("Path $printedDest specify a node with not a Composite type")
        } ?: throw ConfigSpecificationException("Cannot find a node by path $printedDest")
    }

    companion object {
        private fun findBy(root: CompositeSpecTreeNode, targetPath: List<String>): SpecTreeNode? {
            if (targetPath.isEmpty()) {
                return root
            }
            var curNode: SpecTreeNode? = root
            for (name in targetPath) {
                if (curNode !is CompositeSpecTreeNode) {
                    return null
                }
                curNode = curNode.children[name]
            }
            return curNode
        }
    }
}
