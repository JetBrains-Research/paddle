package io.paddle.idea.copypaste.poetry

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import io.paddle.plugin.python.utils.*
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.StringWriter

@Suppress("UNCHECKED_CAST")
class PoetryConverter private constructor(val requirements: JsonNode, private val paddleConfig: MutableMap<String, Any>) {

    val yaml: String
        get() {
            val writer = StringWriter()
            Yaml(yamlDumpOptions).dump(paddleConfig, writer)
            return writer.toString() + "\n"
        }

    fun getYamlSectionString(section: String): String {
        val sectionMap = paddleConfig.filterKeys { it == section }
        if (sectionMap.isEmpty()) return ""

        val writer = StringWriter()
        Yaml(yamlDumpOptions).dump(sectionMap, writer)
        return writer.toString() + "\n"
    }


    val sections: Set<String> = setOf(
        "metadata", "environment", "requirements", "repositories"
    )

    private val yamlDumpOptions = DumperOptions().apply {
        defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    }

    companion object {
        fun from(fileText: String, paddleConfig: MutableMap<String, Any> = hashMapOf()): PoetryConverter {
            val tree = TomlMapper().readTree(fileText)
            return from(tree, paddleConfig)
        }

        private fun from(tomlTree: JsonNode, paddleConfig: MutableMap<String, Any>): PoetryConverter {
            parsePoetryPyprojectToml(tomlTree, paddleConfig)
            return PoetryConverter(tomlTree, paddleConfig)
        }


        private val metadataFields = listOf("version", "description", "authors", "license", "homepage", "readme")

        private fun parsePoetryPyprojectToml(requirementsData: JsonNode, config: MutableMap<String, Any>) {
            val reqs: MutableMap<String, List<Map<String, Any>>> by lazy {
                config.getOrPut("requirements") { HashMap<String, List<Map<String, Any>>>() } as MutableMap<String, List<Map<String, Any>>>
            }

            requirementsData.get("tool")?.get("poetry")?.apply {
                val metadata: MutableMap<String, Any> by lazy {
                    config.getOrPut("metadata") { HashMap<String, Any>() } as MutableMap<String, Any>
                }
                for (field in metadataFields) {
                    get(field)?.let { node ->
                        if (node.isTextual) {
                            metadata[field] = node.textValue()
                        } else if (node.isArray) {
                            metadata[field] = node.map { it.textValue() }
                        } else if (node.isObject) {
                            metadata[field] = node.fields().asSequence().map { it.key to it.value.textValue() }.toMap()
                        }
                    }
                }

                get("dependencies")?.fields()?.asSequence()?.forEach { (name, node) ->
                    addRequirements(reqs, node, name, "main", config)
                }

                val devDependenciesNode = get("group")?.get("dev")?.get("dependencies") ?: get("dev-dependencies")
                devDependenciesNode?.fields()?.asSequence()?.forEach { (dependencyName, node) ->
                    addRequirements(reqs, node, dependencyName, "dev", config)
                }

                val repos: MutableList<Map<String, Any>> by lazy {
                    config.getOrPut("repositories") { ArrayList<Map<String, Any>>() } as MutableList<Map<String, Any>>
                }

                (get("source") as? ArrayNode)?.forEach { node ->
                    val repo = node.fields().asSequence().map {
                        it.key to (it.value.textValue() ?: (it.value.booleanValue()))
                    }.toMap()

                    val name = repo["name"] as String
                    val url = repo["url"] as String
                    val isExtra = repo["secondary"] as Boolean
                    if (isExtra && !repos.any { it["name"] == name && it["url"] == url && it["secondary"] == "True" }) {
                        repos.add(linkedMapOf("name" to name, "url" to url, "secondary" to "True"))
                    } else if (!isExtra && !repos.any { it["name"] == name && it["url"] == url && it["default"] == "True" }) {
                        repos.add(linkedMapOf("name" to name, "url" to url, "default" to "True"))
                    }
                }
            } ?: requirementsData.run { // in case user copied part of requirements
                fields()?.asSequence()?.forEach { (name, node) ->
                    addRequirements(reqs, node, name, "main", config)
                }
            }
        }

        private fun addRequirements(
            reqs: MutableMap<String, List<Map<String, Any>>>,
            node: JsonNode,
            dependencyName: String,
            dependencyType: String,
            config: MutableMap<String, Any>
        ) {
            if (dependencyName == "python") {
                val environment: MutableMap<String, Any> =
                    config.getOrPut("environment") { HashMap<String, Any>() } as MutableMap<String, Any>
                environment["python"] = node.textValue()
                return
            }
            val paddleReqs: MutableList<MutableMap<String, Any>> by lazy {
                reqs.getOrPut(dependencyType) { ArrayList() } as MutableList<MutableMap<String, Any>>
            }

            val version = node["version"]?.textValue() ?: node.textValue() ?: return
            var isUpdated = false
            for (existingReq in paddleReqs) {
                if (dependencyName == existingReq["name"]) {
                    existingReq["version"] = version
                    isUpdated = true
                    break
                }
            }
            if (!isUpdated) {
                val req = linkedMapOf("name" to dependencyName).also { it["version"] = version } as MutableMap<String, Any>
                paddleReqs.add(req)
            }
        }
    }


}

