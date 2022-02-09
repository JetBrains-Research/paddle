package specification

import io.paddle.specification.CompositeSpecTreeNode
import io.paddle.specification.MutableConfigSpecTree
import io.paddle.utils.json.schema.JSONSCHEMA
import kotlin.test.*

internal class SerializeNodesHierarchyTest {

    @Test
    fun testBaseSchemaDecodingPolymorphic() {
        val content = readContent(schemas[0])
        val root: MutableConfigSpecTree.SpecTreeNode = JSONSCHEMA.parse(content)
        assert(root is CompositeSpecTreeNode)
        assertContentEquals((root as CompositeSpecTreeNode).namesOfRequired, listOf("descriptor"))
    }

    @Test
    fun testBaseSchemaDecodingNonPolymorphic() {
        val content = readContent(schemas[0])
        val root: CompositeSpecTreeNode = JSONSCHEMA.parse(content)
        assertContentEquals(root.namesOfRequired, listOf("descriptor"))
    }

    @Test
    fun testDecodeEncodeIdentity() {
        val schemaString = readContent(schemas[0])
        val encodedSchemaString = JSONSCHEMA.string(JSONSCHEMA.parse<MutableConfigSpecTree.SpecTreeNode>(schemaString))
        assertEquals(schemaString.trim(), encodedSchemaString.trim())
    }

    companion object {
        private val schemas = listOf("base-json-schema.json")

        private fun readContent(schemaPath: String): String {
            val jsonSchema = SerializeNodesHierarchyTest::class.java.getResource("/schemas/$schemaPath")
            assert(jsonSchema != null)
            return jsonSchema!!.readText()
        }
    }
}
