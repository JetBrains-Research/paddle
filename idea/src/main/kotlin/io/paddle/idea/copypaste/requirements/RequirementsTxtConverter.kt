package io.paddle.idea.copypaste.requirements

import io.paddle.idea.copypaste.common.ConverterBase
import io.paddle.plugin.python.dependencies.packages.PyPackageMetadata
import io.paddle.plugin.python.utils.*

@Suppress("UNCHECKED_CAST")
class RequirementsTxtConverter private constructor(paddleConfig: MutableMap<String, Any>) : ConverterBase(paddleConfig) {

    override val sections = setOf("requirements", "repositories")

    companion object {
        private fun from(requirements: List<String>, paddleConfig: MutableMap<String, Any> = hashMapOf()): RequirementsTxtConverter {
            parseRequirementsTxt(requirements, paddleConfig)
            return RequirementsTxtConverter(paddleConfig)
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

            putRepo(config, isExtra, name, url)
        }
    }
}

