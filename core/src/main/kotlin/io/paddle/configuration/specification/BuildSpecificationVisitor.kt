package io.paddle.configuration.specification

interface BuildSpecificationVisitor {
    fun buildSpecializationFor(property: ConfigSpecProperty): Any
    fun buildSpecializationFor(block: ConfigSpecBlock): Any
}

class BuildJsonSchemaSpecificationVisitor : BuildSpecificationVisitor {
    override fun buildSpecializationFor(property: ConfigSpecProperty): Any {
        TODO("get spec part from the property cache or build json schema part for specified typed property")
    }

    override fun buildSpecializationFor(block: ConfigSpecBlock): Any {
        TODO("get spec part from the block cache or build json schema part for specified object with properties")
    }
}
