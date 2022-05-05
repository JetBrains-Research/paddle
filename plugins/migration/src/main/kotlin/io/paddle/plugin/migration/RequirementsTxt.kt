package io.paddle.plugin.migration

import io.paddle.plugin.python.dependencies.packages.PyPackageMetadata
import io.paddle.plugin.python.utils.*
import io.paddle.project.PaddleProject
import io.paddle.utils.config.ConfigurationYAML
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.StringWriter

@Suppress("UNCHECKED_CAST")
class RequirementsTxt(val project: PaddleProject) {
    val file: File? = project.workDir.resolve("requirements.txt").takeIf { it.exists() }

    fun updateDefaultBuildFile(config: ConfigurationYAML) {
        val map = config.toMutableMap()

        val name = project.workDir.relativeTo(project.rootDir).path
            .split(File.separator)
            .joinToString("-")
        val descriptor = (map.getOrPut("descriptor") { LinkedHashMap<String, String>() } as MutableMap<String, String>)
        descriptor.putIfAbsent("name", name)
        descriptor.putIfAbsent("version", "0.1.0")

        val roots = (map.getOrPut("roots") { LinkedHashMap<String, ArrayList<String>>() } as MutableMap<String, MutableList<String>>)
        roots.putIfAbsent("sources", arrayListOf("src/main"))
        roots.putIfAbsent("tests", arrayListOf("src/tests"))

        val env = (map.getOrPut("environment") { LinkedHashMap<String, Any>() } as MutableMap<String, Any>)
        env.putIfAbsent("path", ".venv")
        env.putIfAbsent("python", 3.8)

        parseRequirementsTxt(map)

        val plugins = (map.getOrPut("plugins") { LinkedHashMap<String, ArrayList<String>>() } as MutableMap<String, MutableList<String>>)
        plugins.putIfAbsent("enabled", arrayListOf("python", "migration"))

        val options = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        }
        val writer = StringWriter()
        Yaml(options).dump(map, writer)

        project.buildFile.writeText(
            writer.toString().split('\n')
                .mapIndexed { idx, str ->
                    if (idx != 0 && str.isNotEmpty() && str.first() in 'a'..'z')
                        "\n" + str
                    else
                        str
                }
                .joinToString("\n")
        )
    }

    private fun parseRequirementsTxt(config: MutableMap<String, Any>) {
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
