package specification

import io.paddle.specification.tree.*
import io.paddle.specification.visitor.SpecTreeStructureVisitor
import io.paddle.utils.json.schema.JSONSCHEMA
import io.paddle.utils.resource.ResourceUtils
import java.io.File
import kotlin.test.*

internal class SerializeNodesHierarchyTest {

    @Test
    fun `simple polymorphic decoding test`() {
        parseAndTypingTreeFrom(schemas[0])
    }

    @Test
    fun `test structure correctness of base json-schema after decoding via polymorphic serializer`() {
        val typedRoot: CompositeSpecTreeNode = parseAndTypingTreeFrom(schemas[0])
        val expectedStructure: Map<String, Any?> = mapOf(
            "descriptor" to mapOf(
                "name" to null,
                "version" to null
            ),
            "roots" to mapOf(
                "sources" to null,
                "tests" to null,
                "resources" to null
            ),
            "executor" to mapOf(
                "type" to null
            ),
            "plugins" to mapOf(
                "enabled" to null,
                "jars" to null
            ),
            "tasks" to mapOf(
                "linter" to mapOf(
                    "mypy" to mapOf(
                        "version" to null
                    ),
                    "pylint" to mapOf(
                        "version" to null
                    )
                ),
                "tests" to mapOf(
                    "pytest" to mapOf(
                        "version" to null
                    )
                ),
                "run" to null
            )
        )
        assertEquals(expectedStructure, getSpecTreeStructure(typedRoot, mutableMapOf()))

        assertContentEquals(typedRoot.namesOfRequired, listOf("descriptor"))
    }

    @Test
    fun `thorough check of decoded base json-schema`() {
        val typedSchema: CompositeSpecTreeNode = parseAndTypingTreeFrom(schemas[0])
        assertEquals("Paddle.build", typedSchema.title)
        assertEquals("Configuration of Paddle build", typedSchema.description)
        assertContentEquals(listOf("descriptor"), typedSchema.namesOfRequired)

        val roots = typedSchema.children["roots"]
        assertIs<CompositeSpecTreeNode>(roots)
        assertEquals("Roots of the projects", roots.description)

        val rootsTests = roots.children["tests"]
        assertIs<ArraySpecTreeNode>(rootsTests)
        assertEquals("Tests locations that should be used", rootsTests.description)
        assertIs<StringSpecTreeNode>(rootsTests.items)

        val executor = typedSchema.children["executor"]
        assertIs<CompositeSpecTreeNode>(executor)
        assertEquals("Executor to be used by Paddle to execute build commands", executor.description)
        val executorType = executor.children["type"]
        assertIs<StringSpecTreeNode>(executorType)
        assertContentEquals(listOf("local"), executorType.validValues)

        val tasks = typedSchema.children["tasks"]
        assertIs<CompositeSpecTreeNode>(tasks)
        val linter = tasks.children["linter"]
        assertIs<CompositeSpecTreeNode>(linter)
        val pylint = linter.children["pylint"]
        assertIs<CompositeSpecTreeNode>(pylint)
        assertIs<StringSpecTreeNode>(pylint.children["version"])
    }

    @Test
    fun `test that encoding via non polymorphic serializer does not write down type property`() {
        val content: String = readContent(schemas[0])
        val encodedContent = JSONSCHEMA.string<CompositeSpecTreeNode>(JSONSCHEMA.parse(content))
        assertNotEquals(content.trim(), encodedContent.trim())
    }

    @Test
    fun `test decode then encode is identity function`() {
        val content: String = readContent(schemas[0])
        val encodedContent = JSONSCHEMA.string(JSONSCHEMA.parse<MutableConfigSpecTree.SpecTreeNode>(content))
        assertEquals(content.trim(), encodedContent.trim())
    }

    companion object {
        private val schemas = listOf("base-json-schema.json")

        private fun readContent(schemaPath: String): String {
            val jsonSchema: File? = ResourceUtils.getResourceFileBy("schemas/$schemaPath")
            assertNotNull(jsonSchema)
            return jsonSchema.readText()
        }

        private fun parseAndTypingTreeFrom(schema: String): CompositeSpecTreeNode {
            val content: String = readContent(schema)
            val root: MutableConfigSpecTree.SpecTreeNode = JSONSCHEMA.parse(content)
            assertIs<CompositeSpecTreeNode>(root)
            return root
        }

        private fun getSpecTreeStructure(root: CompositeSpecTreeNode, buffer: MutableMap<String, Any?>): MutableMap<String, Any?> {
            root.accept(SpecTreeStructureVisitor(), buffer)
            return buffer
        }
    }
}
