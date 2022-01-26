package io.paddle.config.specification

interface MutableConfigSpecTree {
    fun insert(destination: List<String>, node: SpecTreeNode)

    abstract class SpecTreeNode(val name: String, val description: String?) {
        abstract fun accept(visitor: SpecTreeVisitor): Any
    }
}

interface ConfigSpecBuilder: MutableConfigSpecTree {
    fun build(): Any
}
