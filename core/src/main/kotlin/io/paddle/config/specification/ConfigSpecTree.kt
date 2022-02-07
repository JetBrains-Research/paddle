package io.paddle.config.specification

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer

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
    var module = SerializersModule {
        polymorphic(MutableConfigSpecTree.SpecTreeNode::class) {
            subclass(CompositeSpecTreeNode::class, serializer())
        }
    }

    abstract fun build(): Any

    companion object {
        fun fromResource(configSpecUrl: String): ConfigurationSpecification = JsonSchemaSpecification(configSpecUrl)
    }
}
