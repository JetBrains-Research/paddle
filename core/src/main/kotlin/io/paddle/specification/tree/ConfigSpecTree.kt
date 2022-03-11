package io.paddle.specification.tree

import io.paddle.specification.visitor.SpecTreeVisitor
import kotlinx.serialization.*

interface MutableConfigSpecTree {
    fun insert(name: String, node: SpecTreeNode, destination: List<String>)

    @Serializable
    abstract class SpecTreeNode {
        abstract val title: String?
        abstract val description: String?

        abstract fun <R, D> accept(visitor: SpecTreeVisitor<R, D>, ctx: D): R
    }
}

abstract class ConfigurationSpecification : MutableConfigSpecTree {
    abstract fun build(): Any

    companion object {
        fun fromResource(configSpecUrl: String): ConfigurationSpecification = JsonSchemaSpecification(configSpecUrl)
    }
}
