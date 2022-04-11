package io.paddle.specification.tree

import io.paddle.specification.visitor.JsonSchemaSpecVisitor
import io.paddle.utils.json.schema.JSONSCHEMA
import io.paddle.utils.splitAndTrim

class JsonSchemaSpecification(baseSchemaResourceUrl: String) : SpecializedConfigSpec<String, Unit>() {
    override val root = readSchemaBy(baseSchemaResourceUrl)

    override val visitor = JsonSchemaSpecVisitor()

    @Suppress("UNCHECKED_CAST")
    override fun <T : SpecTreeNode> get(key: String): T? {
        val parts = key.splitAndTrim(".").takeIf { it.isNotEmpty() } ?: return root as T?

        val path = parts.dropLast(1)
        val name = parts.last()

        var current: Map<String, SpecTreeNode> = root.children
        for (part in path) {
            current = (current[part] as? CompositeSpecTreeNode)?.children ?: return null
        }

        return current[name] as? T?
    }

    override fun specialize() = visitor.visit(root, Unit)

    override fun toString() = specialize()

    private fun readSchemaBy(url: String): CompositeSpecTreeNode {
        val inputStreamWithSchema = javaClass.classLoader.getResourceAsStream(url)
        val schema = inputStreamWithSchema?.bufferedReader()?.use {
            it.readText()
        }
        return schema?.let { JSONSCHEMA.parse(it) } ?: CompositeSpecTreeNode()
    }
}
