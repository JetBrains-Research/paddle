package io.paddle.specification

import kotlinx.serialization.*

interface MutableConfigSpecTree {
    fun insert(name: String, node: SpecTreeNode, destination: List<String>)

    @Serializable
    abstract class SpecTreeNode {
        abstract val title: String?
        abstract val description: String?

        abstract fun accept(visitor: SpecTreeVisitor): Any
    }
}

abstract class ConfigurationSpecification : MutableConfigSpecTree {
    abstract fun build(): Any

    companion object {
        fun fromResource(configSpecUrl: String): ConfigurationSpecification = JsonSchemaSpecification(configSpecUrl)
    }
}
