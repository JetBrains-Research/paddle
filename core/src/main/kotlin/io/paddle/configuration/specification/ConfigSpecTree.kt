package io.paddle.configuration.specification

class ConfigSpecTree(private val root: ConfigSpecBlock) {
    fun buildSpecification(builder: BuildSpecificationVisitor) = builder.buildSpecializationFor(root)

    fun findBy(pathToNode: List<String>): Node? {
        if (pathToNode.isEmpty()) {
            return null
        }
        val firstName = pathToNode.first()
        if (!firstName.contentEquals(root.name)) {
            return null
        }
        var curNode: Node = root
        for (name in pathToNode.slice(1..pathToNode.size)) {
            if (curNode !is ConfigSpecBlock) {
                return null
            }
            curNode.children.find { it.name.contentEquals(name) }?.also {
                curNode = it
            } ?: return null
        }
        return curNode
    }

    abstract class Node(val name: String) {
        abstract fun accept(visitor: BuildSpecificationVisitor): Any
    }
}

abstract class NodeWithCachedSpec(name: String) : ConfigSpecTree.Node(name) {
    protected var isCacheActual = false
    var cachedSpec: Any? = null
        set(value) {
            isCacheActual = true
            field = value
        }
}

class ConfigSpecBlock(name: String) : NodeWithCachedSpec(name) {
    val children : MutableList<ConfigSpecTree.Node> = mutableListOf()

    fun append(child: ConfigSpecTree.Node): Boolean {
        if (children.any { it.name.contentEquals(child.name) }) {
            return false
        }
        val res = children.add(child)
        isCacheActual = false
        return res
    }

    override fun accept(visitor: BuildSpecificationVisitor) = visitor.buildSpecializationFor(this)
}

class ConfigSpecProperty(name : String, val type: Type) : NodeWithCachedSpec(name) {
    override fun accept(visitor: BuildSpecificationVisitor) = visitor.buildSpecializationFor(this)

    enum class Type {
        STRING, BOOL, INTEGER, ARRAY
    }
}
