package io.paddle.idea.copypaste

import io.paddle.plugin.python.dependencies.packages.PyPackageMetadata
import io.paddle.plugin.python.utils.*
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.StringWriter

@Suppress("UNCHECKED_CAST")
class RequirementsTxtConverter private constructor(val requirements: List<String>, private val paddleConfig: MutableMap<String, Any>) {
    val yaml: String
        get() {
            val writer = StringWriter()
            Yaml(yamlDumpOptions).dump(paddleConfig, writer)
            return writer.toString() + "\n"
        }

    val requirementsYaml: String
        get() {
            val writer = StringWriter()
            Yaml(yamlDumpOptions).dump(paddleConfig.filterKeys { it == "requirements" }, writer)
            return writer.toString() + "\n"
        }

    val repositoriesYaml: String
        get() {
            val writer = StringWriter()
            Yaml(yamlDumpOptions).dump(paddleConfig.filterKeys { it == "repositories" }, writer)
            return writer.toString() + "\n"
        }

    private val yamlDumpOptions = DumperOptions().apply { defaultFlowStyle = DumperOptions.FlowStyle.BLOCK }

    companion object {
        fun from(requirements: List<String>, paddleConfig: MutableMap<String, Any> = hashMapOf()): RequirementsTxtConverter {
            parseRequirementsTxt(requirements, paddleConfig)
            return RequirementsTxtConverter(requirements, paddleConfig)
        }

        fun from(fileText: String, paddleConfig: MutableMap<String, Any> = hashMapOf()): RequirementsTxtConverter {
            return from(fileText.split("\n"), paddleConfig)
        }


        private fun parseRequirementsTxt(requirementsData: List<String>, config: MutableMap<String, Any>) {
            for (line in requirementsData) {
                when {
                    line.startsWith("--extra-index-url") -> parseRepoUrl(line, config, isExtra = true)
                    line.startsWith("--index-url") -> parseRepoUrl(line, config, isExtra = false)
                    line.isBlank() -> continue
                    else -> {
                        val spec = PyPackageMetadata.createDependencySpecificationParser(line)?.specification()
                        val name = spec?.nameReq()?.name()?.text ?: error("Unsupported line in requirements.txt file: $line")
                        val version = spec.nameReq()?.versionspec()?.versionMany()?.versionOne(0)?.text
                        val req = linkedMapOf("name" to name).also { version?.let { v -> it["version"] = v } }

                        val reqs = config.getOrPut("requirements") { HashMap<String, List<Map<String, Any>>>() }
                            as MutableMap<String, List<Map<String, Any>>>
                        val mainReqs = reqs.getOrPut("main") { ArrayList() } as MutableList<MutableMap<String, Any>>

                        var isUpdated = false
                        for (existingReq in mainReqs) {
                            if (req["name"] == existingReq["name"]) {
                                existingReq["version"] = req["version"] as String
                                isUpdated = true
                                break
                            }
                        }
                        if (!isUpdated) {
                            mainReqs.add(req as MutableMap<String, Any>)
                        }
                    }
                }
            }
        }

        private fun parseRepoUrl(line: String, config: MutableMap<String, Any>, isExtra: Boolean) {
            val url: PyPackagesRepositoryUrl =
                if (isExtra)
                    line.substringAfter("--extra-index-url ").getSimple()
                else
                    line.substringAfter("--index-url ").getSimple()
            val name: String = url.getDefaultName()

            val repos = config.getOrPut("repositories") { ArrayList<Map<String, Any>>() } as MutableList<Map<String, Any>>
            if (isExtra && !repos.any { it["name"] == name && it["url"] == url && it["secondary"] == "True" }) {
                repos.add(linkedMapOf("name" to name, "url" to url, "secondary" to "True"))
            } else if (!isExtra && !repos.any { it["name"] == name && it["url"] == url && it["default"] == "True" }) {
                repos.add(linkedMapOf("name" to name, "url" to url, "default" to "True"))
            }
        }
    }
}

