package io.paddle.specification.tree

import io.paddle.specification.tree.ConfigurationSpecification.SpecTreeNode
import io.paddle.specification.visitor.SpecTreeVisitor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("object")
open class CompositeSpecTreeNode(
    override val title: String? = null,
    override val description: String? = null,

    @SerialName("required")
    var namesOfRequired: MutableSet<String>? = null,

    @SerialName("properties")
    val children: MutableMap<String, SpecTreeNode> = hashMapOf(),

    @SerialName("enum")
    var validValues: MutableList<CompositeSpecTreeNode>? = null
) : SpecTreeNode() {
    override fun <R, D> accept(visitor: SpecTreeVisitor<R, D>, ctx: D) = visitor.visit(this, ctx)
}

@Serializable
@SerialName("array")
open class ArraySpecTreeNode<T : SpecTreeNode>(override val title: String? = null, override val description: String? = null, val items: T) : SpecTreeNode() {
    override fun <R, D> accept(visitor: SpecTreeVisitor<R, D>, ctx: D) = visitor.visit(this, ctx)
}

@Serializable
@SerialName("string")
class StringSpecTreeNode(
    override var title: String? = null,
    override var description: String? = null,

    @SerialName("enum")
    var validValues: MutableList<String>? = null
) : SpecTreeNode() {
    override fun <R, D> accept(visitor: SpecTreeVisitor<R, D>, ctx: D) = visitor.visit(this, ctx)
}

@Serializable
@SerialName("boolean")
class BooleanSpecTreeNode(
    override var title: String? = null,
    override var description: String? = null,

    @SerialName("enum")
    var validValues: MutableList<Boolean>? = null
) :
    SpecTreeNode() {
    override fun <R, D> accept(visitor: SpecTreeVisitor<R, D>, ctx: D) = visitor.visit(this, ctx)
}

@Serializable
@SerialName("number")
class IntegerSpecTreeNode(
    override var title: String? = null,
    override var description: String? = null,

    @SerialName("enum")
    var validValues: MutableList<Int>? = null
) :
    SpecTreeNode() {
    override fun <R, D> accept(visitor: SpecTreeVisitor<R, D>, ctx: D) = visitor.visit(this, ctx)
}
