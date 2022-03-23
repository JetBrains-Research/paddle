package io.paddle.plugin.python.dependencies.migration

import io.paddle.plugin.python.dependencies.packages.PyPackageMetadata
import io.paddle.plugin.python.utils.PyPackagesRepositoryUrl
import io.paddle.plugin.python.utils.join
import io.paddle.project.Project
import io.paddle.utils.hash.StringHashable
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileWriter

@Suppress("UNCHECKED_CAST")
class RequirementsTxt(val project: Project) {
    val file: File? = project.workDir.resolve("requirements.txt").takeIf { it.exists() }

    fun createDefaultPaddleYAML(config: MutableMap<String, Any>) {
        val descriptor = (config.getOrPut("descriptor") { LinkedHashMap<String, String>() } as MutableMap<String, String>)
        descriptor.putIfAbsent("name", project.workDir.absoluteFile.parentFile.name)
        descriptor.putIfAbsent("version", "0.1.0")

        val roots = (config.getOrPut("roots") { LinkedHashMap<String, ArrayList<String>>() } as MutableMap<String, MutableList<String>>)
        roots.putIfAbsent("sources", arrayListOf("src/main"))
        roots.putIfAbsent("tests", arrayListOf("src/tests"))

        val env = (config.getOrPut("environment") { LinkedHashMap<String, Any>() } as MutableMap<String, Any>)
        env.putIfAbsent("path", ".venv")
        env.putIfAbsent("python", 3.8)

        parseRequirementsTxt(config)

        // We don't need it since the plugin should have been included already to run this task
        // val plugins = (config.getOrPut("plugins") { LinkedHashMap<String, ArrayList<String>>() } as MutableMap<String, MutableList<String>>)
        // plugins.putIfAbsent("enabled", arrayListOf("python"))

        val writer = FileWriter(project.buildFile.path)
        val options = DumperOptions()
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        Yaml(options).dump(config, writer)
    }

    private fun parseRequirementsTxt(config: MutableMap<String, Any>): MutableMap<String, Any> {
        file?.readLines()?.forEach { line ->
            when {
                line.startsWith("--extra-index-url") -> parseRepoUrl(line, config, isExtra = true)
                line.startsWith("--index-url") -> parseRepoUrl(line, config, isExtra = false)
                line.isBlank() -> return@forEach
                else -> {
                    val spec = PyPackageMetadata.createDependencySpecificationParser(line)?.specification()
                    val name = spec?.nameReq()?.name()?.text ?: error("Unsupported line in requirements.txt file: $line")
                    val version = spec.nameReq()?.versionspec()?.versionMany()?.versionOne(0)?.version()?.text

                    val req = linkedMapOf("name" to name).also { version?.let { v -> it["version"] = v } }
                    val reqs = (config.getOrPut("requirements") { ArrayList<Map<String, Any>>() } as MutableList<Map<String, Any>>)
                    if (!reqs.any { it["name"] == req["name"] && it["version"] == req["version"] }) {
                        reqs.add(req)
                    }
                }
            }
        }
        return config
    }

    private fun parseRepoUrl(line: String, config: MutableMap<String, Any>, isExtra: Boolean) {
        var url: PyPackagesRepositoryUrl =
            if (isExtra)
                line.substringAfter("--extra-index-url ")
            else
                line.substringAfter("--index-url ")
        if (!url.trim('/').endsWith("simple")) {
            url = url.join("simple")
        }
        val name: String = url.split("/").takeLast(2).getOrNull(0)
            ?: StringHashable(url).hash()

        val repos = config.getOrPut("repositories") { ArrayList<Map<String, Any>>() } as MutableList<Map<String, Any>>
        if (isExtra && !repos.any { it["name"] == name && it["url"] == url && it["secondary"] == "True" }) {
            repos.add(linkedMapOf("name" to name, "url" to url, "secondary" to "True"))
        } else if (!isExtra && !repos.any { it["name"] == name && it["url"] == url && it["default"] == "True" }) {
            repos.add(linkedMapOf("name" to name, "url" to url, "default" to "True"))
        }
    }
}
