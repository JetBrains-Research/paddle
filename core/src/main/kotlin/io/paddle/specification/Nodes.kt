package io.paddle.specification

import io.paddle.specification.MutableConfigSpecTree.SpecTreeNode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("object")
open class CompositeSpecTreeNode(
    override val title: String? = null,
    override val description: String? = null
) : SpecTreeNode() {

    @SerialName("required")
    val namesOfRequired: MutableList<String>? = null

    @SerialName("properties")
    val children: MutableMap<String, SpecTreeNode> = hashMapOf()

    override fun accept(visitor: SpecTreeVisitor) = visitor.visit(this)
}

@Serializable
@SerialName("array")
open class ArraySpecTreeNode(override val title: String? = null, override val description: String? = null, val items: SpecTreeNode) : SpecTreeNode() {
    override fun accept(visitor: SpecTreeVisitor) = visitor.visit(this)
}

@Serializable
abstract class SimpleSpecTreeNode : SpecTreeNode() {
    @SerialName("enum")
    abstract val validValues: MutableList<*>?
}

@Serializable
@SerialName("string")
class StringSpecTreeNode(
    override val title: String? = null,
    override val description: String? = null,
    @SerialName("enum")
    override val validValues: MutableList<String>? = null
) :
    SimpleSpecTreeNode() {
    override fun accept(visitor: SpecTreeVisitor) = visitor.visit(this)
}

@Serializable
@SerialName("boolean")
class BooleanSpecTreeNode(
    override val title: String? = null,
    override val description: String? = null,
    @SerialName("enum")
    override val validValues: MutableList<Boolean>? = null
) :
    SimpleSpecTreeNode() {
    override fun accept(visitor: SpecTreeVisitor) = visitor.visit(this)
}

@Serializable
@SerialName("number")
class IntegerSpecTreeNode(
    override val title: String? = null,
    override val description: String? = null,
    @SerialName("enum")
    override val validValues: MutableList<Int>? = null
) :
    SimpleSpecTreeNode() {
    override fun accept(visitor: SpecTreeVisitor) = visitor.visit(this)
}
